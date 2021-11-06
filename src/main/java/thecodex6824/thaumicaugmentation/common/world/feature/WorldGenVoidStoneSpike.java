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
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.common.world.biome.IBiomeSpecificSpikeBlockProvider;

public class WorldGenVoidStoneSpike extends WorldGenerator {

    @Override
    protected void setBlockAndNotifyAdequately(World world, BlockPos pos, IBlockState state) {
        
        //Fix java.lang.ArrayIndexOutOfBoundsException: -1
        if (pos.getY() < 0) {
            return;
        }
        
        world.setBlockState(pos, state, 2 | 16);
    }
    
    protected IBlockState getBlockStateToPlace(World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        if (biome instanceof IBiomeSpecificSpikeBlockProvider)
            return ((IBiomeSpecificSpikeBlockProvider) biome).getSpikeState(world, pos);
        else
            return biome.fillerBlock;
    }
    
    protected boolean isValidGroundBlock(IBlockState state) {
        return state.getBlock() == TABlocks.STONE && (state.getValue(ITAStoneType.STONE_TYPE) == StoneType.STONE_VOID
                || state.getValue(ITAStoneType.STONE_TYPE) == StoneType.STONE_TAINT_NODECAY
                || state.getValue(ITAStoneType.STONE_TYPE) == StoneType.SOIL_STONE_TAINT_NODECAY);
    }
    
    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        position = position.down();
        if (isValidGroundBlock(world.getBlockState(position))) {
            BlockPos ground = position;
            position = position.up(rand.nextInt(4));
            int height = rand.nextInt(4) + 7;
            int width = height / 4 + rand.nextInt(2);

            boolean tall = false;
            if (width > 1 && rand.nextInt(5) == 0) {
                position = position.up(10 + rand.nextInt(30));
                tall = true;
            }
            
            MutableBlockPos pos = new MutableBlockPos(position);
            for (int y = position.getY(); y > ground.getY(); --y) {
                for (int x = -1; x < 2; ++x) {
                    for (int z = -1; z < 2; ++z) {
                        pos.setPos(x + ground.getX(), y, z + ground.getZ());
                        if (!world.isAirBlock(pos) && !isValidGroundBlock(world.getBlockState(pos)))
                            return false;
                    }
                }
            }

            if (!tall || rand.nextBoolean()) {
                for (int y = 0; y < height; ++y) {
                    float f = (1.0F - y / (float) height) * width;
                    int l = MathHelper.ceil(f);
                    for (int x = -l; x <= l; ++x) {
                        float f1 = MathHelper.abs(x) - 0.25F;
                        for (int z = -l; z <= l; ++z) {
                            float f2 = MathHelper.abs(z) - 0.25F;
    
                            if ((x == 0 && z == 0 || f1 * f1 + f2 * f2 <= f * f) && ((x != -l && x != l && z != -l && z != l) || rand.nextFloat() <= 0.75F)) {
                                pos.setPos(x + position.getX(), y + position.getY(), z + position.getZ());
                                if (world.isAirBlock(pos))
                                    setBlockAndNotifyAdequately(world, pos, getBlockStateToPlace(world, pos));
                                
                                if (y != 0 && l > 1) {
                                    pos.setY(position.getY() - y);
                                    if (world.isAirBlock(pos))
                                        setBlockAndNotifyAdequately(world, pos, getBlockStateToPlace(world, pos));
                                }
                            }
                        }
                    }
                }
            }

            int length = MathHelper.clamp(width - 1, 0, 1);
            for (int x = -length; x <= length; ++x) {
                for (int z = -length; z <= length; ++z) {
                    pos.setPos(position.getX() + x, position.getY() - 1, position.getZ());
                    int heightLeft = 50;

                    if (Math.abs(x) == 1 && Math.abs(z) == 1)
                        heightLeft = rand.nextInt(5);

                    while (pos.getY() > 0) {
                        if (!world.isAirBlock(pos) && !isValidGroundBlock(world.getBlockState(pos)))
                            break;

                        setBlockAndNotifyAdequately(world, pos, getBlockStateToPlace(world, pos));
                        if (rand.nextBoolean()) {
                            if (x == -length || x == length) {
                                int zDir = rand.nextBoolean() ? 1 : -1;
                                int numBlocks = rand.nextInt(4) + 2;
                                numBlocks = numBlocks > 4 ? 1 : numBlocks;
                                int zOffset = 1;
                                for (int i = 0; i < numBlocks; ++i) {
                                    if (world.isAirBlock(pos.add(0, 0, zOffset)))
                                        setBlockAndNotifyAdequately(world, pos.add(0, 0, zOffset), getBlockStateToPlace(world, pos));
                                    
                                    zOffset += zDir;
                                }
                            }
                        }
                        else {
                            if (z == -length || z == length) {
                                int xDir = rand.nextBoolean() ? 1 : -1;
                                int numBlocks = rand.nextInt(5) + 3;
                                numBlocks = numBlocks > 5 ? 1 : numBlocks;
                                int xOffset = 1;
                                for (int i = 0; i < numBlocks; ++i) {
                                    if (world.isAirBlock(pos.add(xOffset, 0, 0)))
                                        setBlockAndNotifyAdequately(world, pos.add(xOffset, 0, 0), getBlockStateToPlace(world, pos));
                                    
                                    xOffset += xDir;
                                }
                            }
                        }
                        pos.setY(pos.getY() - 1);
                        --heightLeft;
                        
                        if (heightLeft <= 0) {
                            pos.setY(pos.getY() - (rand.nextInt(5) + 1));
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
