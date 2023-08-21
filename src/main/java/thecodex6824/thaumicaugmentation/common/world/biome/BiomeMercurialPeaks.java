/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.world.biome;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.chunk.ChunkPrimer;
import thaumcraft.api.blocks.BlocksTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.ITASandType;
import thecodex6824.thaumicaugmentation.api.block.property.ITASandType.SandType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;

public class BiomeMercurialPeaks extends BiomeEmptinessBase {
	
	protected IBlockState belowTopBlock;
	
    public BiomeMercurialPeaks() {
        super(new BiomeProperties("Mercurial Peaks").setBaseHeight(1.25F).setHeightVariation(1.25F).setRainDisabled().setTemperature(
                0.25F).setWaterColor(0xAA0055), 0.25F, 0xbb0099);

        topBlock = TABlocks.SAND.getDefaultState().withProperty(ITASandType.SAND_TYPE, SandType.SAND_MERCURIAL);
        belowTopBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_MERCURIAL);
        undergroundTunnelBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_ANCIENT_BLUE);
        addFlower(BlocksTC.cinderpearl.getDefaultState(), 5);
        addFlower(BlocksTC.shimmerleaf.getDefaultState(), 5);
    }

    @Override
    public boolean canRain() {
        return false;
    }
    
    @Override
    public int getFlowersPerChunk(Random random) {
    	return random.nextInt(12) + 3;
    }
    
    @Override
    public Vec3d getFogColor(Entity view, float angle, float partialTicks) {
    	double heightColor = MathHelper.clampedLerp(0.2, 0.7, (view.posY - 40) / 80.0);
    	return new Vec3d(heightColor, 0.9 - heightColor, 1.0 - heightColor);
    }
    
    @Override
    public int getGrassColorAtPos(BlockPos pos) {
    	int heightColor = ((int) MathHelper.clampedLerp(51, 179, (pos.getY() - 64) / 64.0)) & 0xff;
    	double noise = plantColorNoise.getValue(pos.getX(), pos.getZ());
        double colorMod = 1.0 / (1.0 + Math.exp(-noise));
        return getModdedBiomeGrassColor(
        		((int) (heightColor * colorMod) << 16) +
        		((int) ((230 - heightColor) * colorMod) << 8) +
        		(int) ((255 - heightColor) * colorMod)
        );
    }
    
    @Override
    public int getFoliageColorAtPos(BlockPos pos) {
    	int heightColor = ((int) MathHelper.clampedLerp(51, 179, (pos.getY() - 64) / 64.0)) & 0xff;
    	double noise = plantColorNoise.getValue(pos.getX(), pos.getZ());
        double colorMod = 1.0 / (1.0 + Math.exp(-noise));
        return getModdedBiomeFoliageColor(
        		((int) (heightColor * colorMod) << 16) +
        		((int) ((230 - heightColor) * colorMod) << 8) +
        		(int) ((255 - heightColor) * colorMod)
        );
    }

    @Override
    public void genTerrainBlocks(World world, Random rand, ChunkPrimer chunkPrimer, int x, int z, double noiseVal) {
    	int ground = BiomeUtil.getHeightFromPrimer(world, chunkPrimer, x, z);
    	if (ground >= world.provider.getAverageGroundLevel() * 1.5 - rand.nextInt(5)) {
	    	int numBiome = (int) (noiseVal + (ground / 10));
			for (int y = 0; y < numBiome; ++y) {
				if (ground - y >= 0 && ground - y < world.provider.getActualHeight()) {
					if (y >= numBiome / 2) {
						chunkPrimer.setBlockState(x & 15, ground - y, z & 15, belowTopBlock);
					}
					else {
						chunkPrimer.setBlockState(x & 15, ground - y, z & 15, topBlock);
					}
				}
				else if (ground - y < 0) {
					break;
				}
			}
    	}
    }

    @Override
    public BiomeDecorator createBiomeDecorator() {
        return new BiomeDecoratorEmptinessBase();
    }

}
