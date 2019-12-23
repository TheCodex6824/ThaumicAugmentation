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

package thecodex6824.thaumicaugmentation.common.world.feature;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import thaumcraft.api.ThaumcraftMaterials;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.world.BiomeTerrainBlocks;
import thecodex6824.thaumicaugmentation.api.world.BiomeTerrainBlocks.TerrainBlocks;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.api.world.capability.CapabilityFractureLocations;
import thecodex6824.thaumicaugmentation.api.world.capability.IFractureLocations;
import thecodex6824.thaumicaugmentation.common.entity.EntityDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.util.FractureLocatorSearchManager;
import thecodex6824.thaumicaugmentation.common.world.WorldDataCache;
import thecodex6824.thaumicaugmentation.common.world.WorldDataCache.WorldData;

public class WorldGenDimensionalFracture extends WorldGenerator {

    private static final int WORLD_BORDER_MAX = 29999984;

    @Override
    protected void setBlockAndNotifyAdequately(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state, 2 | 16);
    }

    protected static boolean isMaterialReplaceable(Material mat) {
        return mat == Material.AIR || mat == Material.GRASS || mat == Material.GROUND || mat == Material.ROCK || 
                mat == Material.SAND || mat == Material.SNOW ||mat == Material.CLAY || mat == ThaumcraftMaterials.MATERIAL_TAINT;
    }
    
    protected void generateBiomeTerrain(World world, Random rand, BlockPos fracture, TerrainBlocks blocks) {
        int depth = rand.nextInt(2) + 1;
        for (int x = -5; x < 6; ++x) {
            for (int y = 5; y > -6 - depth; --y) {
                for (int z = -5; z < 6; ++z) {
                    BlockPos pos = fracture.add(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    if (Math.abs(x) < 2 && Math.abs(y) < 2 && Math.abs(z) < 2) {
                        if (!state.getBlock().isAir(state, world, pos) && state.getBlockHardness(world, pos) >= 0.0F)
                            setBlockAndNotifyAdequately(world, pos, Blocks.AIR.getDefaultState());
                    }
                    else if (Math.abs(x) + Math.abs(y) + Math.abs(z) < 9) {
                        if (((!world.isAirBlock(pos) && isMaterialReplaceable(state.getMaterial())) || y < -1) && !state.getMaterial().isLiquid() && state.getBlockHardness(world, pos) >= 0.0F) {
                            int chanceFactor = Math.abs(x * x) + Math.abs(y) + Math.abs(z * z);
                            if (chanceFactor < 5 || rand.nextInt(chanceFactor - 4) == 0) {
                                IBlockState up = world.getBlockState(pos.up());
                                setBlockAndNotifyAdequately(world, pos, up.getBlock().isAir(up, world, pos.up()) ? blocks.getTopState() : blocks.getFillerState());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        if (world.provider.getDimension() == TADimensions.EMPTINESS.getId()) {
            WorldData dim = FractureUtils.pickRandomDimension(rand, FractureUtils.calcMaxSafeFactor(TAConfig.emptinessMoveFactor.getValue(), position.getX() >> 4, position.getZ() >> 4));
            if (dim != null) {
                BlockPos scaled = FractureUtils.scaleBlockPosFromEmptiness(position, dim.getMovementFactor(), dim.getWorldSeed());
                if (Math.abs(scaled.getX()) < WORLD_BORDER_MAX && Math.abs(scaled.getZ()) < WORLD_BORDER_MAX) {
                    Biome linkedBiome = dim.getBiomeProvider().getBiome(scaled);
                    generateBiomeTerrain(world, rand, position, BiomeTerrainBlocks.getTerrainBlocksForBiome(linkedBiome));
                    BlockPos placeAt = new BlockPos(position.getX(), position.getY() - 1, position.getZ());
                    EntityDimensionalFracture fracture = new EntityDimensionalFracture(world);
                    fracture.setLocationAndAngles(placeAt.getX() + 0.5, placeAt.getY(), placeAt.getZ() + 0.5, rand.nextInt(360), 0.0F);
                    fracture.setLinkedDimension(dim.getDimensionID());
                    fracture.setLinkedPosition(scaled);
                    fracture.setDestinationBiome(linkedBiome);
                    world.spawnEntity(fracture);

                    if (world.getChunk(placeAt).hasCapability(CapabilityFractureLocations.FRACTURE_LOCATIONS, null)) {
                        IFractureLocations loc = world.getChunk(placeAt).getCapability(CapabilityFractureLocations.FRACTURE_LOCATIONS, null);
                        loc.addFractureLocation(placeAt);
                        
                        // onLoad in Chunk is called before populate, so this catches new fractures
                        FractureLocatorSearchManager.addFractureLocation(world, placeAt);
                    }
                    
                    return true;
                }
            }
        }
        else {
            WorldData dim = WorldDataCache.getData(TADimensions.EMPTINESS.getId());
            if (dim != null) {
                BlockPos scaled = FractureUtils.scaleBlockPosToEmptiness(position, world.provider.getMovementFactor(), dim.getWorldSeed());
                if (Math.abs(scaled.getX()) < WORLD_BORDER_MAX && Math.abs(scaled.getZ()) < WORLD_BORDER_MAX) {
                    Biome linkedBiome = dim.getBiomeProvider().getBiome(scaled);
                    generateBiomeTerrain(world, rand, position, BiomeTerrainBlocks.getTerrainBlocksForBiome(linkedBiome));
                    BlockPos placeAt = new BlockPos(position.getX(), position.getY() - 1, position.getZ());
                    EntityDimensionalFracture fracture = new EntityDimensionalFracture(world);
                    fracture.setLocationAndAngles(placeAt.getX() + 0.5, placeAt.getY(), placeAt.getZ() + 0.5, rand.nextInt(360), 0.0F);
                    fracture.setLinkedDimension(dim.getDimensionID());
                    fracture.setLinkedPosition(scaled);
                    fracture.setDestinationBiome(linkedBiome);
                    world.spawnEntity(fracture);

                    if (world.getChunk(placeAt).hasCapability(CapabilityFractureLocations.FRACTURE_LOCATIONS, null)) {
                        IFractureLocations loc = world.getChunk(placeAt).getCapability(CapabilityFractureLocations.FRACTURE_LOCATIONS, null);
                        loc.addFractureLocation(placeAt);
                        FractureLocatorSearchManager.addFractureLocation(world, placeAt);
                    }
                    
                    return true;
                }
            }
        }

        return false;
    }

}
