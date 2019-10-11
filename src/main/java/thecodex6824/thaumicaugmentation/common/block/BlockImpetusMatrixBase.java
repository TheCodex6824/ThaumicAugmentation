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

package thecodex6824.thaumicaugmentation.common.block;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
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

public class BlockImpetusMatrixBase extends BlockTABase implements IImpetusCellInfo, IItemBlockProvider {

    public BlockImpetusMatrixBase() {
        super(Material.ROCK);
        setHardness(7.5F);
        setResistance(500.0F);
        setDefaultState(getDefaultState().withProperty(IImpetusCellInfo.CELL_INFO, 0));
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
                    stack.shrink(1);
                    world.setBlockState(pos, state.withProperty(IImpetusCellInfo.CELL_INFO, IImpetusCellInfo.setCellPresent(value, facing)));
                    return true;
                }
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
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        
        //int cells = IImpetusCellInfo.getNumberOfCells(state.getValue(IImpetusCellInfo.CELL_INFO));
        //if (cells > 0)
        //    AuraHelper.polluteAura(world, pos, cells * 10, true);
            
        if (world.getBlockState(pos.down()).getBlock() == TABlocks.IMPETUS_MATRIX) {
            world.setBlockState(pos.down(), BlocksTC.infusionMatrix.getDefaultState());
            if (world.getBlockState(pos.down(2)).getBlock() == this)
                world.setBlockState(pos.down(2), BlocksTC.pedestalEldritch.getDefaultState());
        }
        else if (world.getBlockState(pos.up()).getBlock() == TABlocks.IMPETUS_MATRIX) {
            world.setBlockState(pos.up(), BlocksTC.infusionMatrix.getDefaultState());
            if (world.getBlockState(pos.up(2)).getBlock() == this)
                world.setBlockState(pos.up(2), BlocksTC.pedestalEldritch.getDefaultState());
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
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
