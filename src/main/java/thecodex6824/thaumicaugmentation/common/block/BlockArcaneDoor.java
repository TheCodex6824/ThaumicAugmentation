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

package thecodex6824.thaumicaugmentation.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoor.EnumDoorHalf;
import net.minecraft.block.BlockDoor.EnumHingePosition;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorHalf;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorHinge;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorOpen;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorType;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorType.ArcaneDoorType;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockWardedCustomModel;
import thecodex6824.thaumicaugmentation.common.block.trait.IDeferPropertyLoading;
import thecodex6824.thaumicaugmentation.common.block.trait.INoAutomaticItemBlockRegistration;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneDoor;
import thecodex6824.thaumicaugmentation.common.util.BitUtils;

public class BlockArcaneDoor extends BlockWardedCustomModel implements IHorizontallyDirectionalBlock, IDeferPropertyLoading, INoAutomaticItemBlockRegistration {
	
	protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
    protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);
	
	public BlockArcaneDoor(String name) {
		super(name, Material.IRON, null);
		IBlockState state = this.blockState.getBaseState();
		state = state.withProperty(IArcaneDoorType.TYPE, ArcaneDoorType.WOOD);
		state = state.withProperty(IArcaneDoorHalf.DOOR_HALF, EnumDoorHalf.LOWER);
		state = state.withProperty(IArcaneDoorOpen.DOOR_OPEN, false);
		state = state.withProperty(IArcaneDoorHinge.HINGE_SIDE, EnumHingePosition.LEFT);
		
		// the facing property has to go on the top block (since there's not enough meta bits left)
		//state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.SOUTH);

		setDefaultState(state);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
		state = getActualState(state, source, pos);
		if (state.getBlock() == this) {
			EnumFacing enumfacing = state.getValue(IHorizontallyDirectionalBlock.DIRECTION);
	        boolean flag = !state.getValue(IArcaneDoorOpen.DOOR_OPEN);
	        boolean flag1 = state.getValue(IArcaneDoorHinge.HINGE_SIDE) == BlockDoor.EnumHingePosition.RIGHT;
	
	        switch (enumfacing)
	        {
	            case EAST:
	            default:
	                return flag ? EAST_AABB : (flag1 ? NORTH_AABB : SOUTH_AABB);
	            case SOUTH:
	                return flag ? SOUTH_AABB : (flag1 ? EAST_AABB : WEST_AABB);
	            case WEST:
	                return flag ? WEST_AABB : (flag1 ? SOUTH_AABB : NORTH_AABB);
	            case NORTH:
	                return flag ? NORTH_AABB : (flag1 ? WEST_AABB : EAST_AABB);
	        }
		}
		else
			return SOUTH_AABB;
    }
	
	@Override
	public boolean isPassable(IBlockAccess world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
        return state.getValue(IArcaneDoorHalf.DOOR_HALF) == EnumDoorHalf.LOWER ? state.getValue(IArcaneDoorOpen.DOOR_OPEN) :
        	world.getBlockState(pos.down()).getValue(IArcaneDoorOpen.DOOR_OPEN);
    }
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {IArcaneDoorType.TYPE, IArcaneDoorHalf.DOOR_HALF, 
				IHorizontallyDirectionalBlock.DIRECTION, IArcaneDoorOpen.DOOR_OPEN, IArcaneDoorHinge.HINGE_SIDE});
	}
    
	protected SoundEvent getOpenSound(IBlockState state) {
		return state.getValue(IArcaneDoorType.TYPE) == ArcaneDoorType.METAL ? SoundEvents.BLOCK_IRON_DOOR_OPEN : 
			SoundEvents.BLOCK_WOODEN_DOOR_OPEN;
	}
	
	protected SoundEvent getCloseSound(IBlockState state) {
		return state.getValue(IArcaneDoorType.TYPE) == ArcaneDoorType.METAL ? SoundEvents.BLOCK_IRON_DOOR_CLOSE : 
			SoundEvents.BLOCK_WOODEN_DOOR_CLOSE;
	}
	
	@Override
	public SoundType getSoundType() {
		return SoundType.WOOD;
	}
	
	@Override
	public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity) {
		return state.getValue(IArcaneDoorType.TYPE) == ArcaneDoorType.METAL ? SoundType.METAL : SoundType.WOOD;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		if (!world.isRemote) {
	        BlockPos blockpos = state.getValue(IArcaneDoorHalf.DOOR_HALF) == EnumDoorHalf.LOWER ? pos : pos.down();
	        IBlockState iblockstate = pos.equals(blockpos) ? state : world.getBlockState(blockpos);
	
	        if (iblockstate.getBlock() != this)
	            return false;
	        else if (world.getTileEntity(blockpos) instanceof TileArcaneDoor && ((TileArcaneDoor) world.getTileEntity(blockpos)).hasPermission(player)) {
	        	state = iblockstate.cycleProperty(IArcaneDoorOpen.DOOR_OPEN);
	            world.setBlockState(blockpos, state, 10);
	            world.markBlockRangeForRenderUpdate(blockpos, pos);
	            world.playSound(null, blockpos, state.getValue(IArcaneDoorOpen.DOOR_OPEN) ? getOpenSound(state) : getCloseSound(state),
	            		SoundCategory.BLOCKS, 1.0F, 1.0F);
	            return true;
	        }
		}
        
        return true;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = getDefaultState();
		boolean isLower = !BitUtils.isBitSet(meta, 0);
		state = state.withProperty(IArcaneDoorHalf.DOOR_HALF, isLower ? EnumDoorHalf.LOWER : EnumDoorHalf.UPPER);
		if (isLower) {
			state = state.withProperty(IArcaneDoorType.TYPE, BitUtils.isBitSet(meta, 1) ? ArcaneDoorType.METAL : ArcaneDoorType.WOOD);
			state = state.withProperty(IArcaneDoorOpen.DOOR_OPEN, BitUtils.isBitSet(meta, 2));
			state = state.withProperty(IArcaneDoorHinge.HINGE_SIDE, BitUtils.isBitSet(meta, 3) ? EnumHingePosition.RIGHT : EnumHingePosition.LEFT);
		}
		else
			state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.byHorizontalIndex(BitUtils.getBits(meta, 1, 3)));
		
		return state;
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = 0;
		meta = BitUtils.setBitIf(meta, 0, state.getValue(IArcaneDoorHalf.DOOR_HALF) == EnumDoorHalf.UPPER);
		if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == EnumDoorHalf.LOWER) {
			meta = BitUtils.setBitIf(meta, 1, state.getValue(IArcaneDoorType.TYPE) == ArcaneDoorType.METAL);
			meta = BitUtils.setBitIf(meta, 2, state.getValue(IArcaneDoorOpen.DOOR_OPEN));
			meta = BitUtils.setBitIf(meta, 3, state.getValue(IArcaneDoorHinge.HINGE_SIDE) == EnumHingePosition.RIGHT);
		}
		else
			meta = BitUtils.setBits(meta, 1, 3, state.getValue(IHorizontallyDirectionalBlock.DIRECTION).getHorizontalIndex());
		
		return meta;
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == EnumDoorHalf.LOWER && world.getBlockState(pos.up()).getBlock() == this) {
			IBlockState upper = world.getBlockState(pos.up());
			if (upper.getBlock() == this)
				return state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, 
						upper.getValue(IHorizontallyDirectionalBlock.DIRECTION));
		}
		else if (world.getBlockState(pos.down()).getBlock() == this){
			IBlockState lower = world.getBlockState(pos.down());
			if (lower.getBlock() == this) {
				state = state.withProperty(IArcaneDoorType.TYPE, lower.getValue(IArcaneDoorType.TYPE));
				state = state.withProperty(IArcaneDoorOpen.DOOR_OPEN, lower.getValue(IArcaneDoorOpen.DOOR_OPEN));
				state = state.withProperty(IArcaneDoorHinge.HINGE_SIDE, lower.getValue(IArcaneDoorHinge.HINGE_SIDE));
				return state;
			}
		}
		
		return state;
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == BlockDoor.EnumDoorHalf.UPPER) {
            if (world.getBlockState(pos.down()).getBlock() != this)
                world.setBlockToAir(pos);
        }
        else {
            if (world.getBlockState(pos.up()).getBlock() != this)
                world.setBlockToAir(pos);
        }
		
		super.neighborChanged(state, world, pos, block, fromPos);
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == BlockDoor.EnumDoorHalf.UPPER) {
            if (worldIn.getBlockState(pos.down()).getBlock() == this)
                worldIn.setBlockToAir(pos.down());
        }
        else {
            if (worldIn.getBlockState(pos.up()).getBlock() == this)
                worldIn.setBlockToAir(pos.up());
        }
		
		super.onBlockHarvested(worldIn, pos, state, player);
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
			int fortune) {
		
		if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == EnumDoorHalf.LOWER)
			drops.add(new ItemStack(TAItems.ARCANE_DOOR, 1, state.getValue(IArcaneDoorType.TYPE) == ArcaneDoorType.METAL ? 1 : 0));
	}
	
	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == EnumDoorHalf.LOWER)
			return new ItemStack(TAItems.ARCANE_DOOR, 1, state.getValue(IArcaneDoorType.TYPE) == ArcaneDoorType.METAL ? 1 : 0);
		else
			return ItemStack.EMPTY;
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return state.getValue(IArcaneDoorHalf.DOOR_HALF) == EnumDoorHalf.LOWER;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileArcaneDoor();
	}
	
}
