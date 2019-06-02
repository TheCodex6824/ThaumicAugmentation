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

import com.google.common.math.DoubleMath;

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
import thecodex6824.thaumicaugmentation.common.world.biome.IFluxBiome;
import thecodex6824.thaumicaugmentation.common.world.feature.WorldGenDimensionalFracture;

public class TAWorldGenerator implements IWorldGenerator {
    
    private static final int WORLD_BORDER_MAX = 29999984;

    private WorldGenDimensionalFracture fractureGen = new WorldGenDimensionalFracture();

    private double movementRatio(World world) {
        return world.provider.getMovementFactor() / TAConfig.emptinessMoveFactor.getValue();
    }

    private boolean couldPossiblyHaveLink(World world, int chunkX, int chunkZ) {
        double ratio = movementRatio(world);
        // disallow non-integral scale results because otherwise multiple chunks will mathematically work
        return Math.abs(chunkX) < WORLD_BORDER_MAX / 16 && Math.abs(chunkZ) < WORLD_BORDER_MAX / 16 &&
                DoubleMath.isMathematicalInteger(chunkX * ratio) && DoubleMath.isMathematicalInteger(chunkZ * ratio);
    }

    private boolean wouldLink(World world, int chunkX, int chunkZ) {
        if (couldPossiblyHaveLink(world, chunkX, chunkZ)) {
            double ratio = movementRatio(world);
            int scaleX = MathHelper.floor(chunkX * ratio);
            int scaleZ = MathHelper.floor(chunkZ * ratio);

            // chunk seed algorithm used by forge (what is passed to generate)
            Random test = new Random(world.getSeed());
            long xSeed = test.nextLong() >> 2 + 1;
            long zSeed = test.nextLong() >> 2 + 1;
            test.setSeed((xSeed * scaleX + zSeed * scaleZ) ^ world.getSeed());
            if (test.nextInt(TAConfig.fractureGenChance.getValue()) == 0) {
                test.nextInt(); 
                test.nextInt();

                return fractureGen.wouldLinkToDim(world, test, scaleX, scaleZ, world.provider.getDimension());
            }
        }

        return false;
    }

    private BlockPos getTopValidSpot(World world, int x, int z, boolean allowVoid) {
        BlockPos pos = new BlockPos(x, 0, z);
        for (int y = world.getActualHeight() - 1; y >= 0; --y) {
            BlockPos check = pos.add(0, y, 0);
            IBlockState state = world.getBlockState(check);
            if (state.getMaterial().blocksMovement() && !state.getBlock().isLeaves(state, world, check) &&
                    !state.getBlock().isFoliage(world, check) && state.getBlockHardness(world, check) >= 0.0F &&
                    world.getBlockState(check.up()).getBlock().isAir(world.getBlockState(check.up()), world, check))
                return pos.add(0, y + 2, 0);
        }

        return allowVoid ? new BlockPos(x, world.provider.getAverageGroundLevel(), z) : null;
    }

    private void generateFractures(Random random, int chunkX, int chunkZ, World world) {
        if (world.provider.getDimension() == TADimensions.EMPTINESS.getId()) {
            if (random.nextInt(TAConfig.fractureGenChance.getValue()) == 0) {
                int posX = chunkX * 16 + 8 + MathHelper.getInt(random, -4, 4);
                int posZ = chunkZ * 16 + 8 + MathHelper.getInt(random, -4, 4);
                if (Math.abs(posX) < WORLD_BORDER_MAX && Math.abs(posZ) < WORLD_BORDER_MAX)
                    fractureGen.generate(world, random, getTopValidSpot(world, posX, posZ, true));
            }
        }
        else if (fractureGen.isDimAllowedForLinking(world.provider.getDimension()) && wouldLink(world, chunkX, chunkZ)) {
            int posX = chunkX * 16 + 8 + MathHelper.getInt(random, -4, 4);
            int posZ = chunkZ * 16 + 8 + MathHelper.getInt(random, -4, 4);
            // being in the max border size is checked in wouldLink
            fractureGen.generate(world, random, getTopValidSpot(world, posX, posZ, true));
        }
    }
    
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider) {

        BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
        Biome biome = world.getBiomeProvider().getBiome(pos);
        if (biome instanceof IFluxBiome) {
            float flux = AuraHelper.getAuraBase(world, pos) * ((IFluxBiome) biome).getBaseFluxConcentration();
            AuraHelper.drainVis(world, pos, flux, false);
            AuraHelper.polluteAura(world, pos, flux, false);
        }
        
        if (!ModConfig.CONFIG_MISC.wussMode)
            generateFractures(random, chunkX, chunkZ, world);
    }

}
