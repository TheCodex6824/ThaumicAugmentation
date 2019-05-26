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

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import thecodex6824.thaumicaugmentation.api.TABlocks;

public class WorldGenVoidStoneSpike extends WorldGenerator {

	@Override
	protected void setBlockAndNotifyAdequately(World world, BlockPos pos, IBlockState state) {
		world.setBlockState(pos, state, 2 | 16);
	}
	
	@Override
	public boolean generate(World world, Random rand, BlockPos position) {
		position = position.down();
		if (!world.isAirBlock(position)) {
			Biome biome = world.getBiome(position);
			position = position.up(rand.nextInt(4));
            int height = rand.nextInt(4) + 7;
            int width = height / 4 + rand.nextInt(2);

            if (width > 1 && rand.nextInt(5) == 0)
                position = position.up(10 + rand.nextInt(30));

            for (int y = 0; y < height; ++y) {
                float f = (1.0F - y / (float) height) * width;
                int l = MathHelper.ceil(f);
                for (int x = -l; x <= l; ++x) {
                    float f1 = MathHelper.abs(x) - 0.25F;
                    for (int z = -l; z <= l; ++z) {
                        float f2 = MathHelper.abs(z) - 0.25F;

                        if ((x == 0 && z == 0 || f1 * f1 + f2 * f2 <= f * f) && ((x != -l && x != l && z != -l && z != l) || rand.nextFloat() <= 0.75F)) {
                        	BlockPos pos = position.add(x, y, z);
                            if (world.isAirBlock(pos) || world.getBlockState(pos).getBlock() == TABlocks.STONE)
                            	setBlockAndNotifyAdequately(world, pos, biome.fillerBlock);

                            if (y != 0 && l > 1) {
                                pos = position.add(x, -y, z);
                                if (world.isAirBlock(pos) || world.getBlockState(pos).getBlock() == TABlocks.STONE)
                                    setBlockAndNotifyAdequately(world, pos, biome.fillerBlock);
                            }
                        }
                    }
                }
            }

            int length = MathHelper.clamp(width - 1, 0, 1);
            for (int x = -length; x <= length; ++x) {
                for (int z = -length; z <= length; ++z) {
                    BlockPos pos = position.add(x, -1, z);
                    int heightLeft = 50;

                    if (Math.abs(x) == 1 && Math.abs(z) == 1)
                        heightLeft = rand.nextInt(5);

                    while (pos.getY() > 0) {
                        if (!world.isAirBlock(pos) && world.getBlockState(pos).getBlock() != biome.fillerBlock.getBlock())
                            break;

                        setBlockAndNotifyAdequately(world, pos, biome.fillerBlock);
                        pos = pos.down();
                        --heightLeft;
                        
                        if (heightLeft <= 0) {
                            pos = pos.down(rand.nextInt(5) + 1);
                            heightLeft = rand.nextInt(5);
                        }
                    }
                }
            }

            return true;
		}
		
		return false;
	}
	
}
