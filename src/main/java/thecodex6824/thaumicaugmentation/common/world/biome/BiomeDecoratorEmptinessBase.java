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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.util.Constants.BlockFlags;

public class BiomeDecoratorEmptinessBase extends BiomeDecorator {
	
	protected boolean isNonLiquidReplaceable(IBlockState state) {
		return !state.getMaterial().isLiquid() && state.getMaterial().isReplaceable();
	}
	
	protected void fixLiquidBorders(World world, Biome biome, BlockPos pos, EnumFacing checkDir) {
		int topBlock = BiomeUtil.getHeightOpaqueOnly(world, pos.add(0, world.provider.getAverageGroundLevel(), 0));
		MutableBlockPos mutable = new MutableBlockPos(pos);
		mutable.setY(topBlock);
		boolean liquidFound = false;
		IBlockState liquid = null;
		while (mutable.getY() < topBlock + 16) {
			mutable.setPos(pos.getX(), mutable.getY(), pos.getZ());
			IBlockState state = world.getBlockState(mutable);
			mutable.setPos(pos.getX() + checkDir.getXOffset(), mutable.getY(), pos.getZ() + checkDir.getZOffset());
			IBlockState offsetState = world.getBlockState(mutable);
			if (!liquidFound) {
				if (isNonLiquidReplaceable(state) && offsetState.getMaterial().isLiquid()) {
					liquid = offsetState;
					liquidFound = true;
				}
			}
			else if (!isNonLiquidReplaceable(state) || !offsetState.getMaterial().isLiquid()) {
				break;
			}
			
			mutable.setY(mutable.getY() + 1);
		}
		
		if (liquidFound) {
			int liquidEnd = mutable.getY();
			mutable.setPos(pos.getX(), topBlock - 1, pos.getZ());
			world.setBlockState(mutable, biome.fillerBlock, BlockFlags.SEND_TO_CLIENTS | BlockFlags.NO_OBSERVERS);
			mutable.setY(topBlock);
			while (mutable.getY() < liquidEnd) {
				if (isNonLiquidReplaceable(world.getBlockState(mutable))) {
					world.setBlockState(mutable, liquid, BlockFlags.SEND_TO_CLIENTS | BlockFlags.NO_OBSERVERS);
				}
				mutable.setY(mutable.getY() + 1);
			}
		}
	}
	
	@Override
	public void decorate(World world, Random random, Biome biome, BlockPos pos) {
		NoiseGeneratorPerlin noiseHeight = new NoiseGeneratorPerlin(random, 4);
		MutableBlockPos mutable = new MutableBlockPos(pos);
		for (int dZ = 15; dZ >= 0; --dZ) {
			for (int dX = 0; dX <= 15; ++dX) {
				mutable.setPos(pos.getX() + dX + 8, pos.getY(), pos.getZ() + dZ + 8);
				fixLiquidBorders(world, biome, mutable, EnumFacing.SOUTH);
				fixLiquidBorders(world, biome, mutable, EnumFacing.EAST);
				fixLiquidBorders(world, biome, mutable, EnumFacing.NORTH);
				fixLiquidBorders(world, biome, mutable, EnumFacing.WEST);
				mutable.setPos(pos.getX() + dX + 8 + random.nextInt(3) - 1, pos.getY(), pos.getZ() + dZ + 8 + random.nextInt(3) - 1);
				Biome biomeHere = world.getBiome(mutable);
				if (biomeHere instanceof BiomeEmptinessBase) {
					IBlockState underground = ((BiomeEmptinessBase) biomeHere).undergroundTunnelBlock;
					mutable.setPos(pos.getX() + dX + 8, pos.getY(), pos.getZ() + dZ + 8);
					for (int y = world.getSeaLevel() - 8 - (int) (noiseHeight.getValue(pos.getX() + dX + 8, pos.getZ() + dZ + 8)); y >= 0; --y) {
			        	mutable.setY(y);
			            IBlockState current = world.getBlockState(mutable);
			            if (current.isNormalCube()) {
			            	boolean visibleFace = false;
			            	for (EnumFacing facing : EnumFacing.VALUES) {
			            		mutable.setPos(pos.getX() + dX + 8 + facing.getXOffset(), y + facing.getYOffset(),
			            				pos.getZ() + dZ + 8 + facing.getZOffset());
			            		IBlockState adj = world.getBlockState(mutable);
			            		if (!adj.isOpaqueCube()) {
			            			visibleFace = true;
			            			break;
			            		}
			            	}
			            	
			            	mutable.setPos(pos.getX() + dX + 8, y, pos.getZ() + dZ + 8);
			            	if (visibleFace) {
			            		world.setBlockState(mutable, underground, BlockFlags.SEND_TO_CLIENTS | BlockFlags.NO_OBSERVERS);
			            	}
			            }
			        }
				}
			}
		}
		
		int flowersPerChunk = biome instanceof BiomeEmptinessBase ? ((BiomeEmptinessBase) biome).getFlowersPerChunk(random) : this.flowersPerChunk;
		if (flowersPerChunk > 0) {
			for (int i = 0; i < flowersPerChunk; ++i) {
				int x = random.nextInt(16) + 8;
				int z = random.nextInt(16) + 8;
				BlockPos placeAt = world.getHeight(pos.add(x, 0, z));
				if (placeAt.getY() > 0) {
					biome.plantFlower(world, random, placeAt);
				}
			}
		}
	}
	
}
