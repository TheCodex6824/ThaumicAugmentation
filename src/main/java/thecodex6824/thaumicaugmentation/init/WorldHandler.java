package thecodex6824.thaumicaugmentation.init;

import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thaumcraft.common.world.biomes.BiomeHandler;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.world.BiomeMonitorColors;
import thecodex6824.thaumicaugmentation.api.world.BiomeTerrainBlocks;
import thecodex6824.thaumicaugmentation.api.world.IPurgeBiomeSpawns;
import thecodex6824.thaumicaugmentation.api.world.TABiomes;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.api.world.capability.CapabilityFractureLocations;
import thecodex6824.thaumicaugmentation.api.world.capability.IFractureLocations;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;
import thecodex6824.thaumicaugmentation.common.util.FractureLocatorSearchManager;
import thecodex6824.thaumicaugmentation.common.world.TAWorldGenerator;
import thecodex6824.thaumicaugmentation.common.world.WorldDataCache;
import thecodex6824.thaumicaugmentation.common.world.WorldProviderEmptiness;
import thecodex6824.thaumicaugmentation.common.world.feature.FractureUtils;

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

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class WorldHandler {

    private WorldHandler() {}
    
    private static int findFreeDimensionID() {
        for (int i = 2; i < Integer.MAX_VALUE; ++i) {
            if (!DimensionManager.isDimensionRegistered(i))
                return i;
        }

        throw new IllegalStateException("Could not find a free dimension ID (there are over 2 billion used?!?!)");
    }

    private static int currentIDOrSubstitute(int id) {
        boolean conflict = false;
        for (DimensionType type : DimensionType.values()) {
            if (type.getId() == id) {
                conflict = true;
                break;
            }
        }

        if (!conflict)
            return id;
        else
            return findFreeDimensionID();
    }

    public static void preInit() {
        if (!TAConfig.disableEmptiness.getValue()) {
            int emptinessID = currentIDOrSubstitute(TAConfig.emptinessDimID.getValue());
            if (emptinessID != TAConfig.emptinessDimID.getValue()) {
                ThaumicAugmentation.getLogger().warn("The dimension ID {} was already taken. Assigning {} instead and updating the config...",
                        TAConfig.emptinessDimID.getValue(), emptinessID);
                TAConfigHolder.world.emptinessDimID = emptinessID;
                TAConfigHolder.syncLocally();
                TAConfigHolder.syncConfig();
            }
    
            TADimensions.EMPTINESS = DimensionType.register("emptiness", "_emptiness", emptinessID, WorldProviderEmptiness.class, false);
            DimensionManager.registerDimension(emptinessID, TADimensions.EMPTINESS);
            BiomeHandler.addDimBlacklist(emptinessID, 0);
        }
    }

    public static void init() {
        GameRegistry.registerWorldGenerator(new TAWorldGenerator(), 20);
    }

    public static void postInit() {
        BiomeTerrainBlocks.init();
        BiomeTerrainBlocks.registerBiomeOverride(Biomes.HELL, Blocks.NETHERRACK.getDefaultState(), 
                Blocks.NETHERRACK.getDefaultState());
        BiomeTerrainBlocks.registerBiomeOverride(Biomes.SKY, Blocks.END_STONE.getDefaultState(), 
                Blocks.END_STONE.getDefaultState());
        
        BiomeMonitorColors.init();
        BiomeMonitorColors.registerMonitorColorOverride(Biomes.HELL, 0x881100, 0xA0522D, 0xFF4500);
        BiomeMonitorColors.registerMonitorColorOverride(Biomes.SKY, 0xFFF8DC, -1, -1);
        BiomeMonitorColors.registerMonitorColorOverride(TABiomes.EMPTINESS, 0x5B005B, 0x3B003B, -1);
        BiomeMonitorColors.registerMonitorColorOverride(TABiomes.EMPTINESS_HIGHLANDS, 0x3B003B, -1, -1);
        BiomeMonitorColors.registerMonitorColorOverride(TABiomes.TAINTED_LANDS, 0x9B009B, 0xFF00FF, -1);
        
        for (Biome biome : TABiomes.getAllBiomes()) {
            if (biome instanceof IPurgeBiomeSpawns)
                ((IPurgeBiomeSpawns) biome).purgeSpawns();
        }
    }
    
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote && !TAConfig.disableEmptiness.getValue())
            WorldDataCache.addOrUpdateData(event.getWorld());
    }
    
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getWorld().isRemote && !TAConfig.disableEmptiness.getValue() && FractureUtils.isDimAllowedForLinking(event.getWorld().provider.getDimension())) {
            if (event.getChunk().hasCapability(CapabilityFractureLocations.FRACTURE_LOCATIONS, null)) {
                IFractureLocations loc = event.getChunk().getCapability(CapabilityFractureLocations.FRACTURE_LOCATIONS, null);
                if (loc.hasFracture())
                    FractureLocatorSearchManager.addFractureLocations(event.getWorld(), loc.getFractureLocations());
            }
        }
    }
    
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote && !TAConfig.disableEmptiness.getValue() && FractureUtils.isDimAllowedForLinking(event.getWorld().provider.getDimension())) {
            if (event.getChunk().hasCapability(CapabilityFractureLocations.FRACTURE_LOCATIONS, null)) {
                IFractureLocations loc = event.getChunk().getCapability(CapabilityFractureLocations.FRACTURE_LOCATIONS, null);
                if (loc.hasFracture())
                    FractureLocatorSearchManager.removeFractureLocations(event.getWorld(), loc.getFractureLocations());
            }
        }
    }

}
