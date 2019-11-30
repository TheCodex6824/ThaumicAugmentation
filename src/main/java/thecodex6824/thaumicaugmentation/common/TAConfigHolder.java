/**
 *  Thaumic Augmentation
 *  Copyright (c) 2019 TheCodex6824.
 *
 *  This file is part of Thaumic Augmentation.
 *
 *  Thaumic Augmentation is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Thaumic Augmentation is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Thaumic Augmentation.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumicaugmentation.common;

import java.util.ArrayList;
import java.util.HashMap;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAConfig.TileWardMode;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionBoolean;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionDouble;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionDoubleList;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionEnum;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionInt;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionIntList;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionLong;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionStringToIntMap;
import thecodex6824.thaumicaugmentation.api.config.IEnumSerializer;
import thecodex6824.thaumicaugmentation.api.config.TAConfigManager;
import thecodex6824.thaumicaugmentation.common.network.PacketConfigSync;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

/**
 * Holds configuration variables for Thaumic Augmentation.
 * @author TheCodex6824
 */
@Config(modid = ThaumicAugmentationAPI.MODID)
@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class TAConfigHolder {

    private TAConfigHolder() {}
    
    // TODO localize all the strings here

    public static GeneralOptions general = new GeneralOptions();
    public static GameplayOptions gameplay = new GameplayOptions();
    public static WorldOptions world = new WorldOptions();
    public static ClientOptions client = new ClientOptions();
    
    public static class GeneralOptions {
        
        @Name("disableCoremod")
        @Comment({
            "Completely disables the Thaumic Augmentation coremod.",
            "It will still appear in the list of loaded coremods, but won't do anything.",
            "The coremod is a neccessary evil to get warded blocks to behave properly with other mods,",
            "disabling it may make warded blocks less durable than intended."
        })
        @RequiresMcRestart
        public boolean disableCoremod = false;
        
    }
    
    public static class GameplayOptions {
        
        public WardOptions ward = new WardOptions();
        public ImpetusCosts impetusCosts = new ImpetusCosts();
        
        private static class WardOptions {
            
            @Name("allowSingleplayerWardOverride")
            @Comment({
                "Allows you to always be able to interact with or destroy any warded block/tile while in singleplayer.",
                "For multiplayer see AllowOPWardOverride."
            })
            public boolean singlePlayerWardOverride = false;
            
            @Name("allowOPWardOverride")
            @Comment({
                "Allow server operators to always be able to interact with or destroy any warded block/tile.",
                "For singleplayer see AllowSingleplayerWardOverride."
            })
            public boolean opWardOverride = false;
            
            @Name("disableWardFocus")
            @Comment({
                "Disables the ward focus. This will remove the research entry, disable existing wards, and make existing foci do nothing.",
                "This is a server-side setting, although the ward research may not sync properly if the value is not the same on both sides."
            })
            @RequiresMcRestart
            public boolean disableWardFocus = false;
            
            @Name("wardTileMode")
            @Comment({
                "Optionally allows tile entities to be warded in addition to normal blocks.",
                "While \"all\" and \"none\" should be self explanatory, \"notick\" will",
                "only allow tiles that do not tick (aka do not implement ITickable).",
                "Allowing all tiles may be very overpowered - use at your own risk!"
            })
            public TileWardMode tileWardMode = TileWardMode.NOTICK;
            
        }
        
        private static class ImpetusCosts {
            
            @Name("terraformerCost")
            @Comment({
                "The amount of Impetus the Arcane Terraformer consumes per block terraformed."
            })
            public int terraformerCost = 5;
            
            @Name("shieldFocusCost")
            @Comment({
                "The amount of Impetus the Void Shield focus effect consumes to create the shield.",
                "Note that a proportion of this amount will be consumed to heal a damaged shield."
            })
            public int shieldFocusCost = 10;
            
        }
        
        @Name("gauntletVisDiscounts")
        @Comment({
            "The discounts that will be applied to the vis cost of foci used in the thaumium and void metal caster gauntlets."
        })
        @RangeDouble(min = 0.0F, max = 1.0F)
        public double[] gauntletVisDiscounts = {0.1, 0.3};
        
        @Name("gauntletCooldownModifiers")
        @Comment({
            "The multipliers that will be applied to the use cooldowns of the Thaumium and Void Metal caster gauntlets."
        })
        @RangeDouble(min = 0.0F, max = 1.0F)
        public double[] gauntletCooldownModifiers = {0.80, 0.9};

        @Name("voidseerExtraArea")
        @Comment({
            "The extra square area for the voidseer gauntlet, in chunks.",
            "An area of 3, for example, will mean vis will be taken in a 3x3 chunk area around the caster.",
            "Note that chunks still need to be loaded to take Vis from them, so the chunk load distance for your",
            "singleplayer/server is another limiting factor."
        })
        @RangeInt(min = 1, max = 32)
        public int voidseerArea = 3;

        @Name("voidBootsLandSpeedBoost")
        @Comment({
            "The boost applied while the wearer is on the ground, and on dry land.",
            "This is added to the base movement of the player per tick."
        })
        @RangeDouble(min = 0.0F, max = 10.0F)
        public double voidBootsLandSpeedBoost = 0.09;

        @Name("voidBootsWaterSpeedBoost")
        @Comment({
            "The boost applied while the wearer is in water.",
            "This is added to the base movement of the player per tick."
        })
        @RangeDouble(min = 0.0F, max = 10.0F)
        public double voidBootsWaterSpeedBoost = 0.045;

        @Name("voidBootsJumpBoost")
        @Comment({
            "The boost applied when the wearer jumps.",
            "This is added to the base jump height of the player."
        })
        @RangeDouble(min = 0.0F, max = 10.0F)
        public double voidBootsJumpBoost = 0.4;

        @Name("voidBootsJumpFactor")
        @Comment({
            "The boost applied to player movement while in the air.",
            "This itself is a speed, so it can make movement faster in the air than on the ground."
        })
        @RangeDouble(min = 0.0F, max = 10.0F)
        public double voidBootsJumpFactor = 0.04;

        @Name("voidBootsStepHeight")
        @Comment({
            "The boost applied to the player's step height (while not sneaking).",
            "This is added to the vanilla default value of 0.6."
        })
        @RangeDouble(min = 0.0F, max = 10.0F)
        public double voidBootsStepHeight = 0.47;

        @Name("voidBootsSneakReduction")
        @Comment({
            "Any speed boosts (not jump) will be divided by this value while sneaking."
        })
        @RangeDouble(min = 1.0F, max = 10.0F)
        public double voidBootsSneakReduction = 4.0F;
        
        @Name("serverSideMovementCalculation")
        @Comment({
            "Makes the server calculate positions and velocities from the Boots of the Riftstrider in addition to the client.",
            "Normally the client is left to update their position, and the server just takes it from the client.",
            "If you don't know what this does, you probably don't need to and shouldn't enable this."
        })
        public boolean serverMovementCalculation = false;
        
        @Name("defaultCastingGauntletColors")
        @Comment({
            "The default dye colors for the thaumium and void gauntlets when crafted, in that order.",
            "The dyed color is multiplied with the color of the texture.",
            "This is a server-side setting."
        })
        public int[] defaultGauntletColors = new int[] {0x7A68C0, 0x262157};

        @Name("defaultVoidBootsColor")
        @Comment({
            "The default dye color for the Boots of the Riftstrider when crafted.",
            "The dyed color is multiplied with the color of the texture.",
            "This is a server-side setting."
        })
        public int defaultVoidBootsColor = 0x6A3880;
        
    }
    
    public static class WorldOptions {
        
        @Name("emptinessDimID")
        @Comment({
            "The dimension ID to use for the Emptiness dimension.",
            "If this ID is already taken, a new one will automatically be assigned."
        })
        @RequiresMcRestart
        public int emptinessDimID = 14676;

        @Name("emptinessMoveFactor")
        @Comment({
            "The scaling factor applied to distances in the Emptiness dimension.",
            "For example, the nether has a value of 8 since it multiplies coords by 8.",
            "Note that move factors for the Emptiness are calculated based on chunk rather than position, so final values",
            "may be slightly different than expected."
        })
        public double emptinessMoveFactor = 16.0;

        @Name("fractureGenChance")
        @Comment({
            "The chance for a fracture to generate in a chunk in the Emptiness dimension.",
            "The approximate chance will be 1 / chance (assuming the chunk meets all other conditions).",
            "Set this to 0 to stop fractures from spawning completely, but be warned that there is no",
            "other way in Thaumic Augmentation to access the Emptiness in survival."
        })
        public int fractureGenChance = 35;
        
        @Name("fractureDimList")
        @Comment({
            "Lists the whitelisted dimensions for fractures (not including the Emptiness dim), and their associated weights.",
            "Higher weights (compared to lower weights) will be more likely to spawn.",
            "This WILL affect worldgen, so use with caution on existing worlds.",
            "The config GUI does not seem to support the addition or removal of entries, edit this",
            "value in a text editor outside Minecraft instead.",
            "Default dimensions: 0 = Overworld, -1 = Nether, 1 = End, 7 = Twilight Forest, 17 = Atum 2,",
            "20 = Betweenlands, 111 = Lost Cities, 66 = Erebus, 33 = Wizardry (Underworld)"
        })
        @RequiresMcRestart
        public HashMap<String, Integer> fractureDimList = new HashMap<>();
        
        public WorldOptions() {
            // vanilla
            fractureDimList.put("0", 35);
            fractureDimList.put("-1", 15);
            fractureDimList.put("1", 10);
            
            // twilight forest
            fractureDimList.put("7", 5);
            
            // atum 2
            fractureDimList.put("17", 5);
            
            // betweenlands
            fractureDimList.put("20", 5);
            
            // lost cities
            fractureDimList.put("111", 5);
            
            // erebus
            fractureDimList.put("66", 5);
            
            // wizardry (slightly less because 2 dims)
            fractureDimList.put("33", 5);
            //fractureDimList.put("34", 4); dimension currently crashes due to unfinished biome
        }
        
        @Name("fractureLocatorUpdateInterval")
        @Comment({
            "How often the location pointed to by the Fracture Locator should be updated, in milliseconds.",
            "This is a server-side setting."
        })
        public int fractureLocatorUpdateInterval = 2000;
        
        @Name("validFracturesAlwaysTeleport")
        @Comment({
            "If this is set, fractures that previously found a valid location will always teleport the player, even if it is now invalid.",
            "Normally, fractures check if there is a fracture at the destination to make sure players can get back.",
            "This is a server-side setting."
        })
        public boolean fracturesAlwaysTeleport = false;
        
        @Name("disableEmptinessDimension")
        @Comment({
            "Completely disables the Emptiness dimension, *including* all fracture generation.",
            "This is not the intended way to experience the mod but is included here for modpack authors.",
            "This is a server-side setting, but will probably cause problems if the client does not have the same value."
        })
        @RequiresMcRestart
        public boolean disableEmptiness = false;
        
    }
    
    public static class ClientOptions {
        
        @Name("simpleCastedLightRendering")
        @Comment({
            "Disables some unneccessary particle effects.",
            "This includes the special effect of the casted light, as well as most of the particles on the Metaspatial Accumulator/Extruder"
        })
        public boolean reducedEffects = false;
        
        @Name("gauntletCastAnimation")
        @Comment({
            "Enables a simple animation where an entity holds their arm out after casting.",
            "This is a client-side setting."
        })
        public boolean gauntletCastAnimation = true;
        
    }
    
    private static ArrayList<Runnable> listeners = new ArrayList<>();
    
    public static void addListener(Runnable r) {
        listeners.add(r);
    }
    
    public static boolean removeListener(Runnable r) {
        return listeners.remove(r);
    }

    @SubscribeEvent
    public static void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(ThaumicAugmentationAPI.MODID)) {
            syncLocally();
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                if (Minecraft.getMinecraft().isSingleplayer())
                    loadConfigValues(Side.SERVER);
                else
                    loadConfigValues(Side.CLIENT);
            }
            else
                loadConfigValues(Side.SERVER);
            
            for (Runnable r : listeners)
                r.run();
        }
    }

    public static void loadConfigValues(Side side) {
        TAConfig.gauntletVisDiscounts.setValue(gameplay.gauntletVisDiscounts, side);
        TAConfig.gauntletCooldownModifiers.setValue(gameplay.gauntletCooldownModifiers, side);

        TAConfig.voidseerArea.setValue(gameplay.voidseerArea, side);

        TAConfig.voidBootsLandSpeedBoost.setValue(gameplay.voidBootsLandSpeedBoost, side);
        TAConfig.voidBootsWaterSpeedBoost.setValue(gameplay.voidBootsWaterSpeedBoost, side);
        TAConfig.voidBootsJumpBoost.setValue(gameplay.voidBootsJumpBoost, side);
        TAConfig.voidBootsJumpFactor.setValue(gameplay.voidBootsJumpFactor, side);
        TAConfig.voidBootsStepHeight.setValue(gameplay.voidBootsStepHeight, side);
        TAConfig.voidBootsSneakReduction.setValue(gameplay.voidBootsSneakReduction, side);
        TAConfig.serverMovementCalculation.setValue(gameplay.serverMovementCalculation, side);

        TAConfig.opWardOverride.setValue(gameplay.ward.opWardOverride, side);
        TAConfig.singlePlayerWardOverride.setValue(gameplay.ward.singlePlayerWardOverride, side);
        TAConfig.tileWardMode.setValue(gameplay.ward.tileWardMode, side);

        TAConfig.reducedEffects.setValue(client.reducedEffects, side);

        TAConfig.defaultGauntletColors.setValue(gameplay.defaultGauntletColors, side);
        TAConfig.defaultVoidBootsColor.setValue(gameplay.defaultVoidBootsColor, side);

        TAConfig.emptinessMoveFactor.setValue(world.emptinessMoveFactor, side);
        TAConfig.fractureGenChance.setValue(world.fractureGenChance, side);
        TAConfig.fractureLocatorUpdateInterval.setValue(world.fractureLocatorUpdateInterval, side);
        TAConfig.fracturesAlwaysTeleport.setValue(world.fracturesAlwaysTeleport, side);
        
        TAConfig.gauntletCastAnimation.setValue(client.gauntletCastAnimation, side);
        
        TAConfig.terraformerImpetusCost.setValue((long) gameplay.impetusCosts.terraformerCost);
        TAConfig.shieldFocusImpetusCost.setValue((long) gameplay.impetusCosts.shieldFocusCost);
    }

    public static void syncLocally() {
        ConfigManager.sync(ThaumicAugmentationAPI.MODID, Type.INSTANCE);
    }

    public static void syncConfig() {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers())
            syncConfig(player);
    }

    public static void syncConfig(EntityPlayerMP target) {
        TANetwork.INSTANCE.sendTo(new PacketConfigSync(TAConfigManager.createSyncBuffer(Side.CLIENT)), target);
    }

    public static void loadOrSyncConfig(EntityPlayer player) {
        if (!player.world.isRemote && player instanceof EntityPlayerMP) {
            loadConfigValues(Side.SERVER);
            syncConfig((EntityPlayerMP) player);
        }
        else if (!player.world.isRemote)
            loadConfigValues(Side.SERVER);
        else
            loadConfigValues(Side.CLIENT);
    }

    public static void preInit() {
        TAConfig.gauntletVisDiscounts = TAConfigManager.addOption(new ConfigOptionDoubleList(false, gameplay.gauntletVisDiscounts));
        TAConfig.gauntletCooldownModifiers = TAConfigManager.addOption(new ConfigOptionDoubleList(false, gameplay.gauntletCooldownModifiers));

        TAConfig.voidseerArea = TAConfigManager.addOption(new ConfigOptionInt(false, gameplay.voidseerArea));

        TAConfig.voidBootsLandSpeedBoost = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsLandSpeedBoost));
        TAConfig.voidBootsWaterSpeedBoost = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsWaterSpeedBoost));
        TAConfig.voidBootsJumpBoost = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsJumpBoost));
        TAConfig.voidBootsJumpFactor = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsJumpFactor));
        TAConfig.voidBootsStepHeight = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsStepHeight));
        TAConfig.voidBootsSneakReduction = TAConfigManager.addOption(new ConfigOptionDouble(true, gameplay.voidBootsSneakReduction));
        TAConfig.serverMovementCalculation = TAConfigManager.addOption(new ConfigOptionBoolean(false, gameplay.serverMovementCalculation));
        
        TAConfig.opWardOverride = TAConfigManager.addOption(new ConfigOptionBoolean(false, gameplay.ward.opWardOverride));
        TAConfig.singlePlayerWardOverride = TAConfigManager.addOption(new ConfigOptionBoolean(false, gameplay.ward.singlePlayerWardOverride));
        TAConfig.disableWardFocus = TAConfigManager.addOption(new ConfigOptionBoolean(true, gameplay.ward.disableWardFocus));
        TAConfig.tileWardMode = TAConfigManager.addOption(new ConfigOptionEnum<>(false, gameplay.ward.tileWardMode, new IEnumSerializer<TileWardMode>() {
           
            @Override
            public void serialize(TileWardMode value, ByteBuf buf) {
                buf.writeInt(value.ordinal());
            }
            
            @Override
            public TileWardMode deserialize(ByteBuf buf) {
                return TileWardMode.values()[Math.min(TileWardMode.values().length - 1, Math.max(buf.readInt(), 0))];
            }
            
        }));
        
        TAConfig.reducedEffects = TAConfigManager.addOption(new ConfigOptionBoolean(false, client.reducedEffects));

        TAConfig.defaultGauntletColors = TAConfigManager.addOption(new ConfigOptionIntList(true, gameplay.defaultGauntletColors));
        TAConfig.defaultVoidBootsColor = TAConfigManager.addOption(new ConfigOptionInt(true, gameplay.defaultVoidBootsColor));

        TAConfig.emptinessDimID = TAConfigManager.addOption(new ConfigOptionInt(true, world.emptinessDimID));
        TAConfig.emptinessMoveFactor = TAConfigManager.addOption(new ConfigOptionDouble(true, world.emptinessMoveFactor));
        TAConfig.fractureGenChance = TAConfigManager.addOption(new ConfigOptionInt(false, world.fractureGenChance));
        TAConfig.fractureDimList = TAConfigManager.addOption(new ConfigOptionStringToIntMap(false, world.fractureDimList));
        TAConfig.fractureLocatorUpdateInterval = TAConfigManager.addOption(new ConfigOptionInt(false, world.fractureLocatorUpdateInterval));
        TAConfig.fracturesAlwaysTeleport = TAConfigManager.addOption(new ConfigOptionBoolean(false, world.fracturesAlwaysTeleport));
        TAConfig.disableEmptiness = TAConfigManager.addOption(new ConfigOptionBoolean(false, world.disableEmptiness));
    
        TAConfig.disableCoremod = TAConfigManager.addOption(new ConfigOptionBoolean(false, general.disableCoremod));
        
        TAConfig.gauntletCastAnimation = TAConfigManager.addOption(new ConfigOptionBoolean(false, client.gauntletCastAnimation));
    
        TAConfig.terraformerImpetusCost = TAConfigManager.addOption(new ConfigOptionLong(false, (long) gameplay.impetusCosts.terraformerCost));
        TAConfig.shieldFocusImpetusCost = TAConfigManager.addOption(new ConfigOptionLong(false, (long) gameplay.impetusCosts.shieldFocusCost));
    }

}
