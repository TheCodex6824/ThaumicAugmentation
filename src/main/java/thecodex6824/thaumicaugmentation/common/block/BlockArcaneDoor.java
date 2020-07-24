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

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoor.EnumHingePosition;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IUnwardableBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IWardOpenedBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IWardParticles;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorHalf;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorHinge;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorOpen;
import thecodex6824.thaumicaugmentation.api.ward.WardHelper;
import thecodex6824.thaumicaugmentation.api.ward.tile.CapabilityWardedTile;
import thecodex6824.thaumicaugmentation.api.ward.tile.IWardedTile;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneDoor;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;

public class BlockArcaneDoor extends BlockTABase implements IHorizontallyDirectionalBlock, IArcaneDoorHalf, 
    IArcaneDoorHinge, IArcaneDoorOpen, IUnwardableBlock, IWardParticles {

    protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
    protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);
    
    private int assignedMeta;

    public BlockArcaneDoor(Material mat, int meta) {
        super(mat);
        assignedMeta = meta;
        setBlockUnbreakable();
        setResistance(Float.MAX_VALUE / 16.0F);
        IBlockState state = this.blockState.getBaseState();
        state = state.withProperty(IArcaneDoorHalf.DOOR_HALF, ArcaneDoorHalf.LOWER);
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
        return state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.LOWER ? state.getValue(IArcaneDoorOpen.DOOR_OPEN) :
            world.getBlockState(pos.down()).getValue(IArcaneDoorOpen.DOOR_OPEN);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IArcaneDoorHalf.DOOR_HALF, 
                IHorizontallyDirectionalBlock.DIRECTION, IArcaneDoorOpen.DOOR_OPEN, IArcaneDoorHinge.HINGE_SIDE,
                IWardOpenedBlock.WARD_OPENED);
    }

    protected SoundEvent getOpenSound(IBlockState state) {
        return material == Material.IRON ? SoundEvents.BLOCK_IRON_DOOR_OPEN : 
            SoundEvents.BLOCK_WOODEN_DOOR_OPEN;
    }

    protected SoundEvent getCloseSound(IBlockState state) {
        return material == Material.IRON ? SoundEvents.BLOCK_IRON_DOOR_CLOSE : 
            SoundEvents.BLOCK_WOODEN_DOOR_CLOSE;
    }

    @Override
    public SoundType getSoundType() {
        return material == Material.IRON ? SoundType.METAL : SoundType.WOOD;
    }
    
    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos) &&
                world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!world.isRemote) {
            BlockPos blockpos = state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.LOWER ? pos : pos.down();
            IBlockState lower = pos.equals(blockpos) ? state : world.getBlockState(blockpos);
            IBlockState upper = pos.equals(blockpos.up()) ? state : world.getBlockState(blockpos.up());

            if (lower.getBlock() != this)
                return false;
            else {
                TileEntity tile = world.getTileEntity(blockpos);
                if (tile != null) {
                    IWardedTile warded = tile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
                    if (warded != null && warded.hasPermission(player)) {
                        state = lower.cycleProperty(IArcaneDoorOpen.DOOR_OPEN);
                        world.setBlockState(blockpos, state, 3);
                        world.markBlockRangeForRenderUpdate(blockpos, pos);
                        world.playSound(null, blockpos, state.getValue(IArcaneDoorOpen.DOOR_OPEN) ? getOpenSound(state) : getCloseSound(state),
                                SoundCategory.BLOCKS, 1.0F, 1.0F);
                        
                        EnumFacing doorFacing = upper.getValue(IHorizontallyDirectionalBlock.DIRECTION);
                        EnumFacing offset = state.getValue(IArcaneDoorHinge.HINGE_SIDE) == EnumHingePosition.LEFT ? 
                                doorFacing.rotateY() : doorFacing.rotateYCCW();
                        IBlockState otherDoorLower = world.getBlockState(blockpos.offset(offset));
                        if (otherDoorLower.getBlock() == this && otherDoorLower.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.LOWER && 
                                otherDoorLower.getValue(IArcaneDoorOpen.DOOR_OPEN) == lower.getValue(IArcaneDoorOpen.DOOR_OPEN) && 
                                otherDoorLower.getValue(IArcaneDoorHinge.HINGE_SIDE) != lower.getValue(IArcaneDoorHinge.HINGE_SIDE)) {
                            
                            otherDoorLower.getBlock().onBlockActivated(world, blockpos.offset(offset), otherDoorLower, player, 
                                    hand, facing, hitX, hitY, hitZ);
                        }
                        
                        return true;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        boolean isLower = !BitUtil.isBitSet(meta, 0);
        state = state.withProperty(IArcaneDoorHalf.DOOR_HALF, isLower ? ArcaneDoorHalf.LOWER : ArcaneDoorHalf.UPPER);
        if (isLower) {
            state = state.withProperty(IArcaneDoorOpen.DOOR_OPEN, BitUtil.isBitSet(meta, 1));
            state = state.withProperty(IArcaneDoorHinge.HINGE_SIDE, BitUtil.isBitSet(meta, 2) ? EnumHingePosition.RIGHT : EnumHingePosition.LEFT);
        }
        else {
            state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.byHorizontalIndex(BitUtil.getBits(meta, 1, 3)));
            state = state.withProperty(IWardOpenedBlock.WARD_OPENED, BitUtil.isBitSet(meta, 3));
        }

        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        meta = BitUtil.setBit(meta, 0, state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.UPPER);
        if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.LOWER) {
            meta = BitUtil.setBit(meta, 1, state.getValue(IArcaneDoorOpen.DOOR_OPEN));
            meta = BitUtil.setBit(meta, 2, state.getValue(IArcaneDoorHinge.HINGE_SIDE) == EnumHingePosition.RIGHT);
        }
        else {
            meta = BitUtil.setBits(meta, 1, 3, state.getValue(IHorizontallyDirectionalBlock.DIRECTION).getHorizontalIndex());
            meta = BitUtil.setBit(meta, 3, state.getValue(IWardOpenedBlock.WARD_OPENED));
        }

        return meta;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.LOWER && world.getBlockState(pos.up()).getBlock() == this) {
            IBlockState upper = world.getBlockState(pos.up());
            return state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, 
                    upper.getValue(IHorizontallyDirectionalBlock.DIRECTION)).withProperty(
                    IWardOpenedBlock.WARD_OPENED, upper.getValue(IWardOpenedBlock.WARD_OPENED));
        }
        else if (world.getBlockState(pos.down()).getBlock() == this){
            IBlockState lower = world.getBlockState(pos.down());
            state = state.withProperty(IArcaneDoorOpen.DOOR_OPEN, lower.getValue(IArcaneDoorOpen.DOOR_OPEN));
            state = state.withProperty(IArcaneDoorHinge.HINGE_SIDE, lower.getValue(IArcaneDoorHinge.HINGE_SIDE));
            return state;
        }

        return state;
    }
    
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        boolean removed = false;
        if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.UPPER) {
            if (world.getBlockState(pos.down()).getBlock() != this) {
                world.setBlockToAir(pos);
                removed = true;
            }
        }
        else {
            if (world.getBlockState(pos.up()).getBlock() != this) {
                world.setBlockToAir(pos);
                removed = true;
            }
        }
        
        if (!removed) {
            BlockPos openPos = pos;
            BlockPos powerPos = pos;
            IBlockState setOpenOn = state;
            IBlockState setPoweredOn = state;
            if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.LOWER) {
                IBlockState upper = world.getBlockState(pos.up());
                if (upper.getBlock() == this) {
                    setPoweredOn = upper;
                    powerPos = pos.up();
                }
            }
            else if (world.getBlockState(pos.down()).getBlock() == this) {
                IBlockState lower = world.getBlockState(pos.down());
                if (lower.getBlock() == this) {
                    setOpenOn = lower;
                    openPos = pos.down();
                }
            }
            
            TileEntity tile = world.getTileEntity(openPos);
            if (tile != null) {
                IWardedTile warded = tile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
                if (warded != null) {
                    boolean shouldOpen = WardHelper.isOpenedByWardOpeningBlock(world, openPos.equals(pos) ? openPos : powerPos, warded.getOwner());
                    if (!shouldOpen)
                        shouldOpen = WardHelper.isOpenedByWardOpeningBlock(world, openPos.equals(pos) ? powerPos : openPos, warded.getOwner());
                    
                    if (!shouldOpen) {
                        EnumFacing doorFacing = world.getBlockState(powerPos).getValue(IHorizontallyDirectionalBlock.DIRECTION);
                        IBlockState lower =  world.getBlockState(openPos);
                        EnumFacing offset = lower.getValue(IArcaneDoorHinge.HINGE_SIDE) == EnumHingePosition.LEFT ? 
                                doorFacing.rotateY() : doorFacing.rotateYCCW();
                        BlockPos otherDoor = openPos.offset(offset);
                        IBlockState otherDoorLower = world.getBlockState(otherDoor);
                        if (otherDoorLower.getBlock() == this && otherDoorLower.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.LOWER && 
                                world.getBlockState(otherDoor.up()).getValue(IWardOpenedBlock.WARD_OPENED) && 
                                otherDoorLower.getValue(IArcaneDoorHinge.HINGE_SIDE) != lower.getValue(IArcaneDoorHinge.HINGE_SIDE)) {
                            
                            TileEntity otherDoorTile = world.getTileEntity(otherDoor);
                            if (otherDoorTile != null) {
                                IWardedTile wardedOther = otherDoorTile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
                                if (wardedOther != null && wardedOther.getOwner().equals(warded.getOwner())) {
                                    shouldOpen = WardHelper.isOpenedByWardOpeningBlock(world, otherDoor, wardedOther.getOwner());
                                    if (!shouldOpen)
                                        shouldOpen = WardHelper.isOpenedByWardOpeningBlock(world, otherDoor.up(), wardedOther.getOwner());
                                }
                            }
                        }
                    }
                    
                    if (shouldOpen != setPoweredOn.getValue(IWardOpenedBlock.WARD_OPENED)) {
                        world.setBlockState(powerPos, setPoweredOn.withProperty(IWardOpenedBlock.WARD_OPENED, shouldOpen), 3);
                        if (shouldOpen != setOpenOn.getValue(IArcaneDoorOpen.DOOR_OPEN)) {
                            world.setBlockState(openPos, setOpenOn.withProperty(IArcaneDoorOpen.DOOR_OPEN, shouldOpen), 3);
                            world.playSound(null, openPos, shouldOpen ? getOpenSound(state) : getCloseSound(state),
                                    SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.UPPER) {
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

        if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.LOWER)
            drops.add(new ItemStack(TAItems.ARCANE_DOOR, 1, assignedMeta));
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.LOWER)
            return new ItemStack(TAItems.ARCANE_DOOR, 1, assignedMeta);
        else {
            IBlockState below = worldIn.getBlockState(pos.down());
            if (below.getBlock() == this)
                return new ItemStack(TAItems.ARCANE_DOOR, 1, assignedMeta);
            else
                return ItemStack.EMPTY;
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {

        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(
                IHorizontallyDirectionalBlock.DIRECTION, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, 
                rot.rotate(state.getValue(IHorizontallyDirectionalBlock.DIRECTION)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(IHorizontallyDirectionalBlock.DIRECTION)));
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
            ItemStack stack) {

        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) {
                IWardedTile warded = tile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
                if (warded != null)
                    warded.setOwner(placer.getUniqueID());
            }
        }

        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }
    
    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
    
    @Override
    public boolean isReplaceableOreGen(IBlockState state, IBlockAccess world, BlockPos pos,
            Predicate<IBlockState> target) {

        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 0;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 0;
    }

    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.IGNORE;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
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
    public boolean isNormalCube(IBlockState state) {
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
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
    
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
            boolean willHarvest) {
        
        if (WardHelper.doesEntityHaveSpecialPermission(player))
            return super.removedByPlayer(state, world, pos, player, willHarvest);
        else
            return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.LOWER;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileArcaneDoor();
    }

}
