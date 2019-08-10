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

package thecodex6824.thaumicaugmentation.common.world;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ModConfig;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.common.event.QueuedWorldGenManager;
import thecodex6824.thaumicaugmentation.common.world.WorldDataCache.WorldData;
import thecodex6824.thaumicaugmentation.common.world.biome.IFluxBiome;
import thecodex6824.thaumicaugmentation.common.world.feature.FractureUtils;
import thecodex6824.thaumicaugmentation.common.world.feature.WorldGenDimensionalFracture;

public final class TAWorldGenerator implements IWorldGenerator {
    
    private static final int WORLD_BORDER_MAX = 29999984;

    private static WorldGenDimensionalFracture FRACTURE_GEN = new WorldGenDimensionalFracture();

    private static boolean wouldLink(World world, int chunkX, int chunkZ) {
        double factor = FractureUtils.movementRatio(world);
        int scaledX = FractureUtils.scaleChunkCoord(chunkX, factor);
        int scaledZ = FractureUtils.scaleChunkCoord(chunkZ, factor);
        
        if (Math.abs(scaledX) >= WORLD_BORDER_MAX / 16 || Math.abs(scaledZ) >= WORLD_BORDER_MAX / 16 ||
                scaledX == FractureUtils.scaleChunkCoord(chunkX - (int) Math.signum(chunkX), factor) || 
                scaledZ == FractureUtils.scaleChunkCoord(chunkZ - (int) Math.signum(chunkZ), factor))
            return false;

        WorldData emptiness = WorldDataCache.getData(TADimensions.EMPTINESS.getId());
        if (emptiness != null) {
            Random test = new Random(emptiness.getWorldSeed());
            long xSeed = test.nextLong() >> 2 + 1;
            long zSeed = test.nextLong() >> 2 + 1;
            test.setSeed((xSeed * scaledX + zSeed * scaledZ) ^ emptiness.getWorldSeed());
            if (test.nextInt(TAConfig.fractureGenChance.getValue()) == 0) {
                MathHelper.getInt(test, -2, 2);
                MathHelper.getInt(test, -2, 2);
    
                return FractureUtils.wouldLinkToDim(test, scaledX, scaledZ, world.provider.getDimension());
            }
        }

        return false;
    }

    private static BlockPos getTopValidSpot(World world, int x, int z, boolean allowVoid) {
        BlockPos pos = new BlockPos(x, 0, z);
        for (int y = Math.max(world.getActualHeight() - 1, 0); y >= 0; --y) {
            BlockPos check = pos.add(0, y, 0);
            IBlockState state = world.getBlockState(check);
            if (state.getMaterial().blocksMovement() && !state.getBlock().isLeaves(state, world, check) &&
                    !state.getBlock().isFoliage(world, check) && state.getBlockHardness(world, check) >= 0.0F &&
                    world.getBlockState(check.up()).getBlock().isAir(world.getBlockState(check.up()), world, check))
                return pos.add(0, y + 2, 0);
        }

        return allowVoid ? new BlockPos(x, Math.min(world.provider.getAverageGroundLevel(), Math.max(world.getActualHeight() - 1, 0)), z) : null;
    }

    public static void generateFractures(Random random, int chunkX, int chunkZ, World world) {
        if (world.provider.getDimension() == TADimensions.EMPTINESS.getId()) {
            if (random.nextInt(TAConfig.fractureGenChance.getValue()) == 0) {
                int posX = chunkX * 16 + 8 + MathHelper.getInt(random, -2, 2);
                int posZ = chunkZ * 16 + 8 + MathHelper.getInt(random, -2, 2);
                if (Math.abs(posX) < WORLD_BORDER_MAX && Math.abs(posZ) < WORLD_BORDER_MAX)
                    FRACTURE_GEN.generate(world, random, getTopValidSpot(world, posX, posZ, true));
            }
        }
        else if (FractureUtils.isDimAllowedForLinking(world.provider.getDimension()) && wouldLink(world, chunkX, chunkZ)) {
            int posX = chunkX * 16 + 8 + MathHelper.getInt(random, -2, 2);
            int posZ = chunkZ * 16 + 8 + MathHelper.getInt(random, -2, 2);
            // being in the max border size is checked in wouldLink
            FRACTURE_GEN.generate(world, random, getTopValidSpot(world, posX, posZ, true));
        }
    }
    
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider) {

        random = new Random(world.getSeed());
        long xSeed = random.nextLong() >> 2 + 1;
        long zSeed = random.nextLong() >> 2 + 1;
        random.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ world.getSeed());
        
        if (world.provider.getDimension() == TADimensions.EMPTINESS.getId()) {
            BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
            Biome biome = world.getBiome(pos);
            if (biome instanceof IFluxBiome) {
                float flux = AuraHelper.getAuraBase(world, pos) * ((IFluxBiome) biome).getBaseFluxConcentration();
                AuraHelper.drainVis(world, pos, flux, false);
                AuraHelper.polluteAura(world, pos, flux, false);
            }
        }
        
        if (!TAConfig.disableEmptiness.getValue() && !ModConfig.CONFIG_MISC.wussMode) {
            if (WorldDataCache.isInitialized())
                generateFractures(random, chunkX, chunkZ, world);
            else {
                final Random rand = new Random(world.getSeed());
                xSeed = random.nextLong() >> 2 + 1;
                zSeed = random.nextLong() >> 2 + 1;
                rand.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ world.getSeed());
                QueuedWorldGenManager.enqueueGeneration(() -> {
                    generateFractures(rand, chunkX, chunkZ, world);
                    world.getChunk(chunkX, chunkZ).markDirty();
                });
            }
        }
    }

}
