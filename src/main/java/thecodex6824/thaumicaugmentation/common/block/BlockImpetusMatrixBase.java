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

package thecodex6824.thaumicaugmentation.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.blocks.BlocksTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.block.property.IImpetusCellInfo;
import thecodex6824.thaumicaugmentation.api.block.property.IVerticallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusMatrix;
import thecodex6824.thaumicaugmentation.common.util.ItemHelper;

public class BlockImpetusMatrixBase extends BlockTABase implements IImpetusCellInfo, IItemBlockProvider {

    public BlockImpetusMatrixBase() {
        super(Material.ROCK);
        setHardness(7.5F);
        setResistance(500.0F);
        setDefaultState(getDefaultState().withProperty(IImpetusCellInfo.CELL_INFO, 0));
        setSoundType(SoundType.STONE);
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(IImpetusCellInfo.CELL_INFO).add(IVerticallyDirectionalBlock.DIRECTION).build();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IImpetusCellInfo.CELL_INFO, meta);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(IImpetusCellInfo.CELL_INFO);
    }
    
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(IVerticallyDirectionalBlock.DIRECTION, 
                world.getBlockState(pos.down()).getBlock() == TABlocks.IMPETUS_MATRIX ? EnumFacing.DOWN : EnumFacing.UP);
    }
    
    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }
    
    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        if (world.getBlockState(pos.down()).getBlock() == TABlocks.IMPETUS_MATRIX) {
            TileEntity tile = world.getTileEntity(pos.down());
            if (tile instanceof TileImpetusMatrix)
                return ((TileImpetusMatrix) tile).getComparatorOutput();
        }
        else if (world.getBlockState(pos.up()).getBlock() == TABlocks.IMPETUS_MATRIX) {
            TileEntity tile = world.getTileEntity(pos.up());
            if (tile instanceof TileImpetusMatrix)
                return ((TileImpetusMatrix) tile).getComparatorOutput();
        }
        
        return 0;
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        
        if (world.isRemote)
            return true;
        else if (facing.getHorizontalIndex() > -1){
            int value = state.getValue(IImpetusCellInfo.CELL_INFO);
            facing = facing.getAxis() == Axis.Z && world.getBlockState(pos.down()).getBlock() == TABlocks.IMPETUS_MATRIX ?
                    facing.getOpposite() : facing;
            if (!IImpetusCellInfo.isCellPresent(state.getValue(IImpetusCellInfo.CELL_INFO), facing) && 
                    hand != null && !player.getHeldItem(hand).isEmpty()) {
                ItemStack stack = player.getHeldItem(hand);
                if (stack.getItem() == TAItems.MATERIAL && stack.getMetadata() == 3) {
                    if (!player.isCreative())
                        stack.shrink(1);
                    
                    world.setBlockState(pos, state.withProperty(IImpetusCellInfo.CELL_INFO, IImpetusCellInfo.setCellPresent(value, facing, true)));
                    world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return true;
                }
            }
            else if (IImpetusCellInfo.isCellPresent(state.getValue(IImpetusCellInfo.CELL_INFO), facing) && 
                    hand != null && (player.getHeldItem(hand).isEmpty() || ItemStack.areItemsEqual(player.getHeldItem(hand), new ItemStack(TAItems.MATERIAL, 1, 3)))) {
                
                world.setBlockState(pos, state.withProperty(IImpetusCellInfo.CELL_INFO, IImpetusCellInfo.setCellPresent(value, facing, false)));
                world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (player.getHeldItem(hand).isEmpty())
                    player.setHeldItem(hand, new ItemStack(TAItems.MATERIAL, 1, 3));
                else if (player.getHeldItem(hand).getCount() < player.getHeldItem(hand).getMaxStackSize())
                    player.getHeldItem(hand).grow(1);
                else {
                    world.spawnEntity(ItemHelper.makeItemEntity(world, pos.getX() + 0.5F, pos.getY() + 0.5F,
                            pos.getZ() + 0.5F, new ItemStack(TAItems.MATERIAL, 1, 3)));
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }
    
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        
        drops.add(new ItemStack(BlocksTC.pedestalEldritch));
        int cells = IImpetusCellInfo.getNumberOfCells(state.getValue(IImpetusCellInfo.CELL_INFO));
        if (cells > 0)
            drops.add(new ItemStack(TAItems.MATERIAL, cells, 3));
    }
    
    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }
    
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        
        if (world.getBlockState(pos.down()).getBlock() == TABlocks.IMPETUS_MATRIX) {
            world.setBlockState(pos.down(), BlocksTC.infusionMatrix.getDefaultState());
            if (world.getBlockState(pos.down(2)).getBlock() == this) {
                int cellCount = IImpetusCellInfo.getNumberOfCells(world.getBlockState(pos.down(2)).getValue(IImpetusCellInfo.CELL_INFO));
                if (cellCount > 0)
                    spawnAsEntity(world, pos.down(2), new ItemStack(TAItems.MATERIAL, cellCount, 3));
                
                world.setBlockState(pos.down(2), BlocksTC.pedestalEldritch.getDefaultState());
            }
        }
        else if (world.getBlockState(pos.up()).getBlock() == TABlocks.IMPETUS_MATRIX) {
            world.setBlockState(pos.up(), BlocksTC.infusionMatrix.getDefaultState());
            if (world.getBlockState(pos.up(2)).getBlock() == this) {
                int cellCount = IImpetusCellInfo.getNumberOfCells(world.getBlockState(pos.up(2)).getValue(IImpetusCellInfo.CELL_INFO));
                if (cellCount > 0)
                    spawnAsEntity(world, pos.up(2), new ItemStack(TAItems.MATERIAL, cellCount, 3));
                
                world.setBlockState(pos.up(2), BlocksTC.pedestalEldritch.getDefaultState());
            }
        }
        
        super.breakBlock(world, pos, state);
    }
    
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        if (face == EnumFacing.UP && world.getBlockState(pos.down()).getBlock() == TABlocks.IMPETUS_MATRIX)
            return BlockFaceShape.SOLID;
        else if (face == EnumFacing.DOWN && world.getBlockState(pos.up()).getBlock() == TABlocks.IMPETUS_MATRIX)
            return BlockFaceShape.SOLID;
        
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
    
}
