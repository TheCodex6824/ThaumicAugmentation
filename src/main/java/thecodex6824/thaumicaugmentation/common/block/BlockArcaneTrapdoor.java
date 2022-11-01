/*
 *  Thaumic Augmentation
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

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
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
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IUnwardableBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IWardParticles;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorHalf;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorOpen;
import thecodex6824.thaumicaugmentation.api.ward.WardHelper;
import thecodex6824.thaumicaugmentation.api.ward.tile.CapabilityWardedTile;
import thecodex6824.thaumicaugmentation.api.ward.tile.IWardedTile;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneTrapdoor;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;

public class BlockArcaneTrapdoor extends BlockTABase implements IHorizontallyDirectionalBlock, IArcaneDoorHalf,
    IArcaneDoorOpen, IItemBlockProvider, IUnwardableBlock, IWardParticles {

    protected static final AxisAlignedBB EAST_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);
    protected static final AxisAlignedBB WEST_OPEN_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB SOUTH_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
    protected static final AxisAlignedBB NORTH_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D);
    protected static final AxisAlignedBB TOP_AABB = new AxisAlignedBB(0.0D, 0.8125D, 0.0D, 1.0D, 1.0D, 1.0D);
    
    public BlockArcaneTrapdoor(Material type) {
        super(type);
        setBlockUnbreakable();
        setResistance(Float.MAX_VALUE / 16.0F);
        IBlockState state = getDefaultState();
        state = state.withProperty(IArcaneDoorHalf.DOOR_HALF, ArcaneDoorHalf.LOWER);
        state = state.withProperty(IArcaneDoorOpen.DOOR_OPEN, false);
        state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.SOUTH);
        setDefaultState(state);
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IArcaneDoorHalf.DOOR_HALF,
                IHorizontallyDirectionalBlock.DIRECTION, IArcaneDoorOpen.DOOR_OPEN);
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(IArcaneDoorOpen.DOOR_OPEN)) {
            switch (state.getValue(IHorizontallyDirectionalBlock.DIRECTION)) {
                case NORTH:
                default:
                    return NORTH_OPEN_AABB;
                case SOUTH:
                    return SOUTH_OPEN_AABB;
                case WEST:
                    return WEST_OPEN_AABB;
                case EAST:
                    return EAST_OPEN_AABB;
            }
        }
        else if (state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.UPPER)
            return TOP_AABB;
        else
            return BOTTOM_AABB;
    }
    
    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return !world.getBlockState(pos).getValue(IArcaneDoorOpen.DOOR_OPEN);
    }

    protected SoundEvent getOpenSound(IBlockState state) {
        return material == Material.IRON ? SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN : 
            SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN;
    }

    protected SoundEvent getCloseSound(IBlockState state) {
        return material == Material.IRON ? SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE : 
            SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE;
    }
    
    @Override
    public SoundType getSoundType() {
        return material == Material.IRON ? SoundType.METAL : SoundType.WOOD;
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) {
                IWardedTile warded = tile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
                if (warded != null && warded.hasPermission(player)) {
                    state = state.cycleProperty(IArcaneDoorOpen.DOOR_OPEN);
                    world.setBlockState(pos, state, 10);
                    world.markBlockRangeForRenderUpdate(pos, pos);
                    world.playSound(null, pos, state.getValue(IArcaneDoorOpen.DOOR_OPEN) ? 
                            getOpenSound(state) : getCloseSound(state), SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return true;
                }
            }
        }

        return true;
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        state = state.withProperty(IArcaneDoorHalf.DOOR_HALF, !BitUtil.isBitSet(meta, 0) ? ArcaneDoorHalf.LOWER : ArcaneDoorHalf.UPPER);
        state = state.withProperty(IArcaneDoorOpen.DOOR_OPEN, BitUtil.isBitSet(meta, 1));
        state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.byHorizontalIndex(BitUtil.getBits(meta, 2, 4)));
    
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        meta = BitUtil.setBit(meta, 0, state.getValue(IArcaneDoorHalf.DOOR_HALF) == ArcaneDoorHalf.UPPER);
        meta = BitUtil.setBit(meta, 1, state.getValue(IArcaneDoorOpen.DOOR_OPEN));
        meta = BitUtil.setBits(meta, 2, 4, state.getValue(IHorizontallyDirectionalBlock.DIRECTION).getHorizontalIndex());

        return meta;
    }
    
    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return true;
    }
    
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {

        IBlockState state = getDefaultState();
        if (facing.getAxis().isHorizontal()) {
            state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, facing).withProperty(IArcaneDoorOpen.DOOR_OPEN, false);
            state = state.withProperty(IArcaneDoorHalf.DOOR_HALF, hitY > 0.5F ? ArcaneDoorHalf.UPPER : ArcaneDoorHalf.LOWER);
        }
        else {
            state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, placer.getHorizontalFacing().getOpposite()).withProperty(IArcaneDoorOpen.DOOR_OPEN, false);
            state = state.withProperty(IArcaneDoorHalf.DOOR_HALF, facing == EnumFacing.UP ? ArcaneDoorHalf.LOWER : ArcaneDoorHalf.UPPER);
        }

        return state;
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
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileArcaneTrapdoor) {
            TileArcaneTrapdoor trapdoor = (TileArcaneTrapdoor) tile;
            IWardedTile warded = trapdoor.getCapability(CapabilityWardedTile.WARDED_TILE, null);
            if (warded != null) {
                boolean shouldOpen = WardHelper.isOpenedByWardOpeningBlock(world, pos, warded.getOwner());
                if (shouldOpen != trapdoor.isPowered()) {
                    trapdoor.setPowered(shouldOpen);
                    if (shouldOpen != state.getValue(IArcaneDoorOpen.DOOR_OPEN)) {
                        world.setBlockState(pos, state.withProperty(IArcaneDoorOpen.DOOR_OPEN, shouldOpen), 2);
                        world.playSound(null, pos, shouldOpen ? getOpenSound(state) : getCloseSound(state),
                                SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }
    
    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        if (state.getValue(IArcaneDoorOpen.DOOR_OPEN)) {
            IBlockState down = world.getBlockState(pos.down());
            if (down.getBlock().isLadder(down, world, pos.down(), entity) && down.getPropertyKeys().contains(BlockLadder.FACING))
                return down.getValue(BlockLadder.FACING) == state.getValue(IHorizontallyDirectionalBlock.DIRECTION);
        }
        return false;
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
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileArcaneTrapdoor();
    }

}
