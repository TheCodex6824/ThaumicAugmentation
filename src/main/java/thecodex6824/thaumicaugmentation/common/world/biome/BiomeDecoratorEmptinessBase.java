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
import net.minecraftforge.common.util.Constants.BlockFlags;

public class BiomeDecoratorEmptinessBase extends BiomeDecorator {
	
	protected boolean isNonLiquidReplaceable(IBlockState state) {
		return !state.getMaterial().isLiquid() && state.getMaterial().isReplaceable();
	}
	
	protected void fixLiquidBorders(World world, Biome biome, BlockPos pos, EnumFacing checkDir) {
		int topBlock = BiomeUtil.getHeightOpaqueOnly(world, pos.add(0, world.provider.getAverageGroundLevel(), 0));
		MutableBlockPos mutable = new MutableBlockPos(pos);
		mutable.setY(topBlock);
		int liquidStart = -1;
		boolean liquidSide = false;
		IBlockState liquid = null;
		while (mutable.getY() < topBlock + 32) {
			mutable.setPos(pos.getX(), mutable.getY(), pos.getZ());
			IBlockState state = world.getBlockState(mutable);
			mutable.setPos(pos.getX() + checkDir.getXOffset(), mutable.getY(), pos.getZ() + checkDir.getZOffset());
			IBlockState offsetState = world.getBlockState(mutable);
			if (liquidStart == -1) {
				if (isNonLiquidReplaceable(state) && offsetState.getMaterial().isLiquid()) {
					liquid = offsetState;
					liquidStart = mutable.getY();
					liquidSide = false;
				}
				else if (state.getMaterial().isLiquid() && isNonLiquidReplaceable(offsetState)) {
					liquid = offsetState;
					liquidStart = mutable.getY();
					liquidSide = true;
				}
			}
			else if (liquidSide) {
				if (!state.getMaterial().isLiquid() || !isNonLiquidReplaceable(offsetState)) {
					break;
				}
			}
			else {
				if (!isNonLiquidReplaceable(state) || !offsetState.getMaterial().isLiquid()) {
					break;
				}
			}
			
			mutable.setY(mutable.getY() + 1);
		}
		
		if (liquidStart != -1) {
			int liquidEnd = mutable.getY();
			mutable.setPos(pos.getX(), topBlock - 1, pos.getZ());
			world.setBlockState(mutable, biome.fillerBlock, BlockFlags.SEND_TO_CLIENTS | BlockFlags.NO_OBSERVERS);
			mutable.setY(liquidStart);
			while (mutable.getY() < liquidEnd) {
				world.setBlockState(mutable, liquid, BlockFlags.SEND_TO_CLIENTS | BlockFlags.NO_OBSERVERS);
				mutable.setY(mutable.getY() + 1);
			}
		}
	}
	
	@Override
	public void decorate(World world, Random random, Biome biome, BlockPos pos) {
		MutableBlockPos mutable = new MutableBlockPos(pos.getX(), 0, pos.getZ());
		for (int dZ = 15; dZ >= 0; --dZ) {
			for (int dX = 0; dX <= 15; ++dX) {
				mutable.setPos(pos.getX() + dX, mutable.getY(), pos.getZ() + dZ);
				fixLiquidBorders(world, biome, mutable, EnumFacing.SOUTH);
			}
		}
		for (int dX = 15; dX >= 0; --dX) {
			for (int dZ = 0; dZ <= 15; ++dZ) {
				mutable.setPos(pos.getX() + dX, mutable.getY(), pos.getZ() + dZ);
				fixLiquidBorders(world, biome, mutable, EnumFacing.EAST);
			}
		}
	}
	
}
