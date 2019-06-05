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

import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import thaumcraft.api.ThaumcraftMaterials;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.world.BiomeTerrainBlocks;
import thecodex6824.thaumicaugmentation.api.world.BiomeTerrainBlocks.TerrainBlocks;
import thecodex6824.thaumicaugmentation.api.world.TABiomes;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.common.entity.EntityDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.util.WeightedRandom;
import thecodex6824.thaumicaugmentation.common.world.WorldProviderCache;

public class WorldGenDimensionalFracture extends WorldGenerator {

    private static final int WORLD_BORDER_MAX = 29999984;

    private static WeightedRandom<Integer> dimPicker;

    private static void reloadDimensionCache() {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int dim : WorldProviderCache.listAllDimensions()) {
            if (dim != TADimensions.EMPTINESS.getId() && TAConfig.fractureDimList.getValue().containsKey(Integer.valueOf(dim).toString()))
                map.put(dim, TAConfig.fractureDimList.getValue().get(Integer.toString(dim)));
        }
        
        dimPicker = new WeightedRandom<>(map.keySet(), map.values());
    }
    
    private static void initDimensionCache() {
        reloadDimensionCache();
        TAConfig.addConfigListener(() -> {
            reloadDimensionCache();
        });
    }

    protected static WorldProvider pickRandomDimension(Random rand, double maxFactor) {
        if (dimPicker == null)
            initDimensionCache();

        if (dimPicker.isEmpty())
            return null;
        
        WeightedRandom<Integer> currentPicker = dimPicker;
        do {
            int dimID = currentPicker.get(rand);
            WorldProvider dim = WorldProviderCache.getProvider(dimID);
            if (dim != null && dim.getMovementFactor() <= maxFactor + 0.00001)
                return dim;
            else
                currentPicker = dimPicker.removeChoice(dimID);
        } while (!currentPicker.isEmpty());

        return null;
    }

    protected double calcMaxSafeFactor(double moveFactor, int chunkX, int chunkZ) {
        return Math.min(Math.abs(chunkX), Math.abs(chunkZ)) * moveFactor + moveFactor;
    }

    protected BlockPos scaleBlockPos(WorldProvider target, BlockPos pos) {
        double factor = TAConfig.emptinessMoveFactor.getValue() / target.getMovementFactor();
        int chunkX = MathHelper.floor((pos.getX() >> 4) * factor);
        int chunkZ = MathHelper.floor((pos.getZ() >> 4) * factor);
        Random rand = new Random(target.getSeed());
        long xSeed = rand.nextLong() >> 2 + 1;
        long zSeed = rand.nextLong() >> 2 + 1;
        rand.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ target.getSeed());
        return new BlockPos(chunkX * 16 + 8 + MathHelper.getInt(rand, -4, 4), 
                0, chunkZ * 16 + 8 + MathHelper.getInt(rand, -4, 4));
    }

    protected BlockPos scaleBlockPosReverse(WorldProvider target, BlockPos pos) {
        double factor = target.getMovementFactor() / TAConfig.emptinessMoveFactor.getValue();
        int chunkX = MathHelper.floor((pos.getX() >> 4) * factor);
        int chunkZ = MathHelper.floor((pos.getZ() >> 4) * factor);
        Random rand = new Random(target.getSeed());
        long xSeed = rand.nextLong() >> 2 + 1;
        long zSeed = rand.nextLong() >> 2 + 1;
        rand.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ target.getSeed());
        rand.nextInt();
        return new BlockPos(chunkX * 16 + 8 + MathHelper.getInt(rand, -4, 4), 
                0, chunkZ * 16 + 8 + MathHelper.getInt(rand, -4, 4));
    }

    public boolean isDimAllowedForLinking(int dim) {
        if (dimPicker == null)
            initDimensionCache();

        return dimPicker.hasChoice(dim);
    }

    public boolean wouldLinkToDim(World world, Random rand, int chunkX, int chunkZ, int targetDim) {
        return targetDim == pickRandomDimension(rand, calcMaxSafeFactor(TAConfig.emptinessMoveFactor.getValue(), 
                chunkX, chunkZ)).getDimension();
    }

    @Override
    protected void setBlockAndNotifyAdequately(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state, 2 | 16);
    }

    protected void generateBiomeTerrain(World world, Random rand, BlockPos fracture, TerrainBlocks blocks) {

        boolean oldFall = BlockFalling.fallInstantly;
        BlockFalling.fallInstantly = false;
        int depth = rand.nextInt(2) + 1;
        for (int x = -3; x < 4; ++x) {
            for (int y = 3; y > -4 - depth; --y) {
                for (int z = -3; z < 4; ++z) {
                    BlockPos pos = fracture.add(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    if (Math.abs(x) < 2 && Math.abs(y) < 2 && Math.abs(z) < 2) {
                        if (!state.getBlock().isAir(state, world, pos) && state.getBlockHardness(world, pos) >= 0.0F)
                            setBlockAndNotifyAdequately(world, pos, Blocks.AIR.getDefaultState());
                    }
                    else {
                        if (((!state.getBlock().isAir(state, world, pos) && (state.getMaterial() == Material.GRASS || state.getMaterial() == Material.GROUND || 
                                state.getMaterial() == Material.ROCK || state.getMaterial() == Material.SAND || 
                                state.getMaterial() == Material.SNOW || state.getMaterial() == Material.CLAY || state.getMaterial() == ThaumcraftMaterials.MATERIAL_TAINT)) || 
                                (y == -2 && (Math.abs(x) != 3 || Math.abs(z) != 3))) && !state.getMaterial().isLiquid() && 
                                state.getBlockHardness(world, pos) >= 0.0F) {
                            IBlockState up = world.getBlockState(pos.up());
                            setBlockAndNotifyAdequately(world, pos, up.getBlock().isAir(up, world, pos.up()) ? blocks.getTopState() : blocks.getFillerState());
                        }
                    }
                }
            }
        }
        BlockFalling.fallInstantly = oldFall;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        if (world.provider.getDimension() == TADimensions.EMPTINESS.getId()) {
            WorldProvider dim = pickRandomDimension(rand, calcMaxSafeFactor(TAConfig.emptinessMoveFactor.getValue(), position.getX() >> 4, position.getZ() >> 4));
            if (dim != null) {
                BlockPos scaled = scaleBlockPos(dim, position);
                if (Math.abs(scaled.getX()) < WORLD_BORDER_MAX && Math.abs(scaled.getZ()) < WORLD_BORDER_MAX) {
                    Biome linkedBiome = dim.getBiomeProvider().getBiome(scaled);
                    generateBiomeTerrain(world, rand, position, BiomeTerrainBlocks.getTerrainBlocksForBiome(linkedBiome));
                    EntityDimensionalFracture fracture = new EntityDimensionalFracture(world);
                    fracture.setLocationAndAngles(position.getX() + 0.5, position.getY() - 1.0, position.getZ() + 0.5, rand.nextInt(360), 0.0F);
                    fracture.setLinkedDimension(dim.getDimension());
                    fracture.setLinkedPosition(scaled);
                    world.spawnEntity(fracture);

                    return true;
                }
            }
        }
        else {
            BlockPos scaled = scaleBlockPosReverse(world.provider, position);
            if (Math.abs(scaled.getX()) < WORLD_BORDER_MAX && Math.abs(scaled.getZ()) < WORLD_BORDER_MAX) {
                generateBiomeTerrain(world, rand, position, BiomeTerrainBlocks.getTerrainBlocksForBiome(TABiomes.EMPTINESS));
                EntityDimensionalFracture fracture = new EntityDimensionalFracture(world);
                fracture.setLocationAndAngles(position.getX() + 0.5, position.getY() - 1.0, position.getZ() + 0.5, 0.0F, 0.0F);
                fracture.setLinkedDimension(TADimensions.EMPTINESS.getId());
                fracture.setLinkedPosition(scaled);
                world.spawnEntity(fracture);

                return true;
            }
        }

        return false;
    }

}
