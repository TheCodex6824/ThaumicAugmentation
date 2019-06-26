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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionBoolean;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionDouble;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionDoubleList;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionInt;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionIntList;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionStringToIntMap;
import thecodex6824.thaumicaugmentation.api.config.TAConfigManager;
import thecodex6824.thaumicaugmentation.common.network.PacketConfigSync;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

/**
 * Holds configuration variables for Thaumic Augmentation.
 * @author TheCodex6824
 * 
 */
@Config(modid = ThaumicAugmentationAPI.MODID)
@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class TAConfigHolder {

    private TAConfigHolder() {}
    
    // TODO localize all the strings here

    @Name("GauntletVisDiscounts")
    @Comment({
        "The discounts that will be applied to the vis cost of foci used in the thaumium and void metal caster gauntlets."
    })
    @RangeDouble(min = 0.0F, max = 1.0F)
    public static double[] gauntletVisDiscounts = {0.1, 0.3};

    @Name("GauntletCooldownModifiers")
    @Comment({
        "The multipliers that will be applied to the use cooldowns of the Thaumium and Void Metal caster gauntlets."
    })
    @RangeDouble(min = 0.0F, max = 1.0F)
    public static double[] gauntletCooldownModifiers = {0.80, 0.9};

    @Name("VoidseerExtraArea")
    @Comment({
        "The extra square area for the voidseer gauntlet, in chunks.",
        "An area of 3, for example, will mean vis will be taken in a 3x3 chunk area around the caster."
    })
    @RangeInt(min = 1, max = 32)
    public static int voidseerArea = 3;

    @Name("VoidBootsLandSpeedBoost")
    @Comment({
        "The boost applied while the wearer is on the ground, and on dry land.",
        "This is added to the base movement of the player per tick."
    })
    @RangeDouble(min = 0.0F, max = 10.0F)
    public static double voidBootsLandSpeedBoost = 0.09;

    @Name("VoidBootsWaterSpeedBoost")
    @Comment({
        "The boost applied while the wearer is in water.",
        "This is added to the base movement of the player per tick."
    })
    @RangeDouble(min = 0.0F, max = 10.0F)
    public static double voidBootsWaterSpeedBoost = 0.045;

    @Name("VoidBootsJumpBoost")
    @Comment({
        "The boost applied when the wearer jumps.",
        "This is added to the base jump height of the player."
    })
    @RangeDouble(min = 0.0F, max = 10.0F)
    public static double voidBootsJumpBoost = 0.4;

    @Name("VoidBootsJumpFactor")
    @Comment({
        "The boost applied to player movement while in the air.",
        "This itself is a speed, so it can make movement faster in the air than on the ground."
    })
    @RangeDouble(min = 0.0F, max = 10.0F)
    public static double voidBootsJumpFactor = 0.04;

    @Name("VoidBootsStepHeight")
    @Comment({
        "The boost applied to the player's step height (while not sneaking).",
        "This is added to the vanilla default value of 0.6."
    })
    @RangeDouble(min = 0.0F, max = 10.0F)
    public static double voidBootsStepHeight = 0.41;

    @Name("VoidBootsSneakReduction")
    @Comment({
        "Any speed boosts (not jump) will be divided by this value while sneaking."
    })
    @RangeDouble(min = 1.0F, max = 10.0F)
    public static double voidBootsSneakReduction = 4.0F;

    @Name("AllowOPWardOverride")
    @Comment({
        "Allow server operators to always be able to interact with any warded block.",
        "Note that if this is set to true, other mods will NOT be able to stop them from interacting with the block."
    })
    public static boolean opWardOverride = false;
    
    @Name("DisableWardFocus")
    @Comment({
        "Disables the ward focus. This will remove the research entry, disable existing wards, and make exisiting foci do nothing.",
        "This is a server-side setting, although the ward research may not sync properly if the value is not the same on both sides."
    })
    @RequiresMcRestart
    public static boolean disableWardFocus = false;

    @Name("SimpleCastedLightRendering")
    @Comment({
        "Disables the casted light from rendering particles, falling back to a (ugly) static model instead."
    })
    public static boolean castedLightSimpleRenderer = false;

    @Name("DefaultCastingGauntletColors")
    @Comment({
        "The default dye colors for the thaumium and void gauntlets, in that order.",
        "The dyed color is multiplied with the color of the texture.",
        "This is a server-side setting."
    })
    public static int[] defaultGauntletColors = new int[] {0xC881D4, 0x6A3880};

    @Name("DefaultVoidBootsColor")
    @Comment({
        "The default dye color for the Boots of the Riftstrider.",
        "The dyed color is multiplied with the color of the texture.",
        "This is a server-side setting."
    })
    public static int defaultVoidBootsColor = 0x6A3880;

    @Name("EmptinessDimensionID")
    @Comment({
        "The dimension ID to use for the Emptiness dimension.",
        "If this ID is already taken, a new one will automatically be assigned."
    })
    @RequiresMcRestart
    public static int emptinessDimID = 14676;

    @Name("EmptinessMoveFactor")
    @Comment({
        "The scaling factor applied to distances in the Void dimension.",
        "For example, the nether has a value of 8 since it multiplies coords by 8."
    })
    public static double emptinessMoveFactor = 16.0;

    @Name("FractureGenChance")
    @Comment({
        "The chance for a fracture to generate in a chunk in the Void dimension.",
        "The approximate chance will be 1 / chance (assuming the chunk meets all other conditions)."
    })
    public static int fractureGenChance = 35;
    
    @Name("FractureDimList")
    @Comment({
        "Lists the whitelisted dimensions for fractures (not including this mod's dim), and their associated weights.",
        "Higher weights (compared to lower weights) will be more likely to spawn.",
        "This WILL affect worldgen, so use with caution on existing worlds.",
        "The config GUI does not seem to support the addition or removal of entries, edit this",
        "value in a text editor outside Minecraft instead."//,
        //"Default dimensions: 0 = Overworld, -1 = Nether, 1 = End, 7 = Twilight Forest, 17 = Atum 2,",
        //"20 = Betweenlands, 111 = Lost Cities, 66 = Erebus, 33 = Wizardry (Underworld)", 34 = Wizardry (Torikki)"
    })
    @RequiresWorldRestart
    public static HashMap<String, Integer> fractureDimList = new HashMap<>();
    
    @Name("FractureLocatorUpdateInterval")
    @Comment({
        "How often the location pointed to by the Fracture Locator should be updated, in milliseconds.",
        "This is a server-side setting."
    })
    public static int fractureLocatorUpdateInterval = 2000;
    
    static {
        // vanilla
        fractureDimList.put("0", 35);
        fractureDimList.put("-1", 15);
        fractureDimList.put("1", 10);
        
        // twilight forest
        //fractureDimList.put("7", 7);
        
        // atum 2
        //fractureDimList.put("17", 7);
        
        // betweenlands
        //fractureDimList.put("20", 7);
        
        // lost cities
        //fractureDimList.put("111", 7);
        
        // erebus
        //fractureDimList.put("66", 7);
        
        // wizardry (slightly less because 2 dims)
        //fractureDimList.put("33", 4);
        //fractureDimList.put("34", 4);
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
        TAConfig.gauntletVisDiscounts.setValue(gauntletVisDiscounts, side);
        TAConfig.gauntletCooldownModifiers.setValue(gauntletCooldownModifiers, side);

        TAConfig.voidseerArea.setValue(voidseerArea, side);

        TAConfig.voidBootsLandSpeedBoost.setValue(voidBootsLandSpeedBoost, side);
        TAConfig.voidBootsWaterSpeedBoost.setValue(voidBootsWaterSpeedBoost, side);
        TAConfig.voidBootsJumpBoost.setValue(voidBootsJumpBoost, side);
        TAConfig.voidBootsJumpFactor.setValue(voidBootsJumpFactor, side);
        TAConfig.voidBootsStepHeight.setValue(voidBootsStepHeight, side);
        TAConfig.voidBootsSneakReduction.setValue(voidBootsSneakReduction, side);

        TAConfig.opWardOverride.setValue(opWardOverride, side);
        TAConfig.disableWardFocus.setValue(disableWardFocus, side);

        TAConfig.castedLightSimpleRenderer.setValue(castedLightSimpleRenderer, side);

        TAConfig.defaultGauntletColors.setValue(defaultGauntletColors, side);
        TAConfig.defaultVoidBootsColor.setValue(defaultVoidBootsColor, side);

        TAConfig.emptinessDimID.setValue(emptinessDimID, side);
        TAConfig.emptinessMoveFactor.setValue(emptinessMoveFactor, side);
        TAConfig.fractureGenChance.setValue(fractureGenChance, side);
        
        TAConfig.fractureDimList.setValue(fractureDimList, side);
        TAConfig.fractureLocatorUpdateInterval.setValue(fractureLocatorUpdateInterval, side);
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
        TAConfig.gauntletVisDiscounts = TAConfigManager.addOption(new ConfigOptionDoubleList(false, gauntletVisDiscounts));
        TAConfig.gauntletCooldownModifiers = TAConfigManager.addOption(new ConfigOptionDoubleList(false, gauntletCooldownModifiers));

        TAConfig.voidseerArea = TAConfigManager.addOption(new ConfigOptionInt(false, voidseerArea));

        TAConfig.voidBootsLandSpeedBoost = TAConfigManager.addOption(new ConfigOptionDouble(true, voidBootsLandSpeedBoost));
        TAConfig.voidBootsWaterSpeedBoost = TAConfigManager.addOption(new ConfigOptionDouble(true, voidBootsWaterSpeedBoost));
        TAConfig.voidBootsJumpBoost = TAConfigManager.addOption(new ConfigOptionDouble(true, voidBootsJumpBoost));
        TAConfig.voidBootsJumpFactor = TAConfigManager.addOption(new ConfigOptionDouble(true, voidBootsJumpFactor));
        TAConfig.voidBootsStepHeight = TAConfigManager.addOption(new ConfigOptionDouble(true, voidBootsStepHeight));
        TAConfig.voidBootsSneakReduction = TAConfigManager.addOption(new ConfigOptionDouble(true, voidBootsSneakReduction));

        TAConfig.opWardOverride = TAConfigManager.addOption(new ConfigOptionBoolean(false, opWardOverride));
        TAConfig.disableWardFocus = TAConfigManager.addOption(new ConfigOptionBoolean(true, disableWardFocus));
        
        TAConfig.castedLightSimpleRenderer = TAConfigManager.addOption(new ConfigOptionBoolean(false, castedLightSimpleRenderer));

        TAConfig.defaultGauntletColors = TAConfigManager.addOption(new ConfigOptionIntList(true, defaultGauntletColors));
        TAConfig.defaultVoidBootsColor = TAConfigManager.addOption(new ConfigOptionInt(true, defaultVoidBootsColor));

        TAConfig.emptinessDimID = TAConfigManager.addOption(new ConfigOptionInt(true, emptinessDimID));
        TAConfig.emptinessMoveFactor = TAConfigManager.addOption(new ConfigOptionDouble(true, emptinessMoveFactor));
        TAConfig.fractureGenChance = TAConfigManager.addOption(new ConfigOptionInt(false, fractureGenChance));
        
        TAConfig.fractureDimList = TAConfigManager.addOption(new ConfigOptionStringToIntMap(false, fractureDimList));
        TAConfig.fractureLocatorUpdateInterval = TAConfigManager.addOption(new ConfigOptionInt(false, fractureLocatorUpdateInterval));
    }

}
