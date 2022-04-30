/**
 *  Thaumic Augmentation
 *  Copyright (c) 2022 WillPastor.
 *  Copyright (c) 2022 TheCodex6824.
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

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;
import thecodex6824.thaumicaugmentation.init.GUIHandler;

public class BlockAugmentationStation extends BlockTABase implements IItemBlockProvider, IHorizontallyDirectionalBlock {
	
	public BlockAugmentationStation() {
		super(Material.WOOD);
		setDefaultState(getDefaultState().withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.NORTH));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		// TODO Auto-generated method stub
		return new BlockStateContainer(this, IHorizontallyDirectionalBlock.DIRECTION);
	}
	
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.byHorizontalIndex(BitUtil.getBits(meta, 0, 2)));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return BitUtil.setBits(0, 0, 2, state.getValue(IHorizontallyDirectionalBlock.DIRECTION).getHorizontalIndex());
	}
	
	@Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {

        return getDefaultState().withProperty(IHorizontallyDirectionalBlock.DIRECTION, placer.getHorizontalFacing().getOpposite());
    }


	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
	        EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
	    
	    if (!world.isRemote) {
            if (!player.isSneaking()) {
                player.openGui(ThaumicAugmentation.instance, GUIHandler.TAInventory.AUGMENTATION_STATION.getID(), world, 
                        pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
            else
                return false;
        }

        return true;
	}
	
	
	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		// TODO Auto-generated method stub
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		// TODO Auto-generated method stub
		return BlockRenderLayer.SOLID;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		// TODO Auto-generated method stub
		return EnumBlockRenderType.MODEL;
	}
	
	
}


