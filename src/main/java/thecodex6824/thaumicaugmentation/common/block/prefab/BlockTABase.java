/**
 *	Thaumic Augmentation
 *	Copyright (c) 2019 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.block.prefab;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.common.block.trait.IDeferPropertyLoading;

public class BlockTABase extends Block {

	protected Class<? extends TileEntity> tileClass;
	
	public BlockTABase(Material mat) {
		super(mat);
	}
	
	public BlockTABase(String name, Material mat, Class<? extends TileEntity> tClass) {
		super(mat);
		setRegistryName(name);
		setTranslationKey(ThaumicAugmentationAPI.MODID + "." + name);
		setCreativeTab(TAItems.CREATIVE_TAB);
		tileClass = tClass;
		
		if (!(this instanceof IDeferPropertyLoading)) {
			IBlockState state = blockState.getBaseState();
			if (this instanceof IEnabledBlock)
				state = state.withProperty(IEnabledBlock.ENABLED, true);
			if (this instanceof IHorizontallyDirectionalBlock)
				state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.SOUTH);
	
			setDefaultState(state);
		}
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		ArrayList<IProperty<?>> list = new ArrayList<>();
		if (this instanceof IEnabledBlock)
			list.add(IEnabledBlock.ENABLED);
		if (this instanceof IHorizontallyDirectionalBlock)
			list.add(IHorizontallyDirectionalBlock.DIRECTION);
		
		return new BlockStateContainer(this, list.toArray(new IProperty<?>[list.size()]));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		if (!(this instanceof IDeferPropertyLoading)) {
			int meta = 0;
			if (this instanceof IEnabledBlock)
				meta |= state.getValue(IEnabledBlock.ENABLED) ? 1 : 0;
			if (this instanceof IHorizontallyDirectionalBlock)
				meta |= state.getValue(IHorizontallyDirectionalBlock.DIRECTION).getHorizontalIndex() << 1;
			
			return meta;
		}
		else
			return super.getMetaFromState(state);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(int meta) {
		if (!(this instanceof IDeferPropertyLoading)) {
			IBlockState state = getDefaultState();
			if (this instanceof IEnabledBlock)
				state = state.withProperty(IEnabledBlock.ENABLED, (meta & 1) == 1 ? true : false);
			if (this instanceof IHorizontallyDirectionalBlock)
				state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.byHorizontalIndex((meta & 6) >> 1));
			
			return state;
		}
		else
			return super.getStateFromMeta(meta);
	}
	
	@Override
	public boolean hasTileEntity() {
		return tileClass != null;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return tileClass != null;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		if (tileClass != null) {
			try {
				return tileClass.newInstance();
			}
			catch (IllegalAccessException | InstantiationException ex) {
				ThaumicAugmentation.getLogger().error("Failed to create new TileEntity of type {0}: ", tileClass.getName());
				ThaumicAugmentation.getLogger().error(ex);
				return null;
			}
		}
		else
			return null;
	}
	
	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}
	
	protected void update(IBlockState state, World world, BlockPos pos) {
		if (this instanceof IEnabledBlock) {
			boolean powered = world.isBlockPowered(pos);
			if (powered == state.getValue(IEnabledBlock.ENABLED))
				world.setBlockState(pos, state.withProperty(IEnabledBlock.ENABLED, !state.getValue(IEnabledBlock.ENABLED)), 3);
		}
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		update(state, world, pos);
		super.onBlockAdded(world, pos, state);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		update(state, world, pos);
		super.neighborChanged(state, world, pos, block, fromPos);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		
		IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
		if (this instanceof IHorizontallyDirectionalBlock)
			state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, placer.getHorizontalFacing().getOpposite());
		
		return state;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return this instanceof IHorizontallyDirectionalBlock ? 
				state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, 
				rot.rotate((EnumFacing)state.getValue(IHorizontallyDirectionalBlock.DIRECTION))) : 
					super.withRotation(state, rot);
	}
	
	@Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation((EnumFacing)state.getValue(IHorizontallyDirectionalBlock.DIRECTION)));
    }
	
	@Override
	public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> items) {
		if (item == TAItems.CREATIVE_TAB || item == CreativeTabs.SEARCH)
			items.add(new ItemStack(this, 1, 0));
	}
	
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(int metadata) {
		return new ModelResourceLocation(getRegistryName().toString(), "inventory");
	}
	
}
