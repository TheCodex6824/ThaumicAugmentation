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
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.block.property.*;
import thecodex6824.thaumicaugmentation.api.ward.WardHelper;
import thecodex6824.thaumicaugmentation.api.ward.tile.CapabilityWardedTile;
import thecodex6824.thaumicaugmentation.api.ward.tile.IWardedTile;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedButton;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockWardedButton extends BlockTABase implements IItemBlockProvider, IDirectionalBlock, IWardOpeningBlock,
    IUnwardableBlock, IWardParticles {

    protected static final AxisAlignedBB AABB_DOWN_OFF = new AxisAlignedBB(0.3125, 0.875, 0.375, 0.6875, 1.0, 0.625);
    protected static final AxisAlignedBB AABB_UP_OFF = new AxisAlignedBB(0.3125, 0.0, 0.375, 0.6875, 0.125, 0.625);
    protected static final AxisAlignedBB AABB_NORTH_OFF = new AxisAlignedBB(0.3125, 0.375, 0.875, 0.6875, 0.625, 1.0);
    protected static final AxisAlignedBB AABB_SOUTH_OFF = new AxisAlignedBB(0.3125, 0.375, 0.0, 0.6875, 0.625, 0.125);
    protected static final AxisAlignedBB AABB_WEST_OFF = new AxisAlignedBB(0.875, 0.375, 0.3125, 1.0, 0.625, 0.6875);
    protected static final AxisAlignedBB AABB_EAST_OFF = new AxisAlignedBB(0.0, 0.375, 0.3125, 0.125, 0.625, 0.6875);
    protected static final AxisAlignedBB AABB_DOWN_ON = new AxisAlignedBB(0.3125, 0.9375, 0.375, 0.6875, 1.0, 0.625);
    protected static final AxisAlignedBB AABB_UP_ON = new AxisAlignedBB(0.3125, 0.0, 0.375, 0.6875, 0.0625, 0.625);
    protected static final AxisAlignedBB AABB_NORTH_ON = new AxisAlignedBB(0.3125, 0.375, 0.9375, 0.6875, 0.625, 1.0);
    protected static final AxisAlignedBB AABB_SOUTH_ON = new AxisAlignedBB(0.3125, 0.375, 0.0, 0.6875, 0.625, 0.0625);
    protected static final AxisAlignedBB AABB_WEST_ON = new AxisAlignedBB(0.9375, 0.375, 0.3125, 1.0, 0.625, 0.6875);
    protected static final AxisAlignedBB AABB_EAST_ON = new AxisAlignedBB(0.0, 0.375, 0.3125, 0.0625, 0.625, 0.6875);
    
    protected final int tick;
    protected final boolean wood;
    
    public BlockWardedButton(SoundType sound, MapColor color, int tickRate, boolean woodBehavior) {
        super(Material.CIRCUITS, color);
        setSoundType(sound);
        tick = tickRate;
        wood = woodBehavior;
        setBlockUnbreakable();
        setResistance(Float.MAX_VALUE / 16.0F);
        setTickRandomly(true);
        setDefaultState(getDefaultState().withProperty(IDirectionalBlock.DIRECTION, EnumFacing.UP).withProperty(
                IWardOpeningBlock.WARD_OPENING, false));
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IDirectionalBlock.DIRECTION, IWardOpeningBlock.WARD_OPENING,
                IWardOpeningWeakPower.WEAK_POWER);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(IDirectionalBlock.DIRECTION).getIndex();
        return BitUtil.setBit(meta, 3, state.getValue(IWardOpeningBlock.WARD_OPENING));
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState().withProperty(IDirectionalBlock.DIRECTION,
                EnumFacing.byIndex(BitUtil.getBits(meta, 0, 3)));
        return state.withProperty(IWardOpeningBlock.WARD_OPENING, BitUtil.isBitSet(meta, 3));
    }
    
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(IWardOpeningWeakPower.WEAK_POWER, true);
    }
    
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return null;
    }
    
    @Override
    public int tickRate(World world) {
        return tick;
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
    
    protected void playPressSound(World world, BlockPos pos) {
        if (wood)
            world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
        else
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
    }
    
    protected void playReleaseSound(World world, BlockPos pos) {
        if (wood)
            world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
        else
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
    }
    
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        
        return getDefaultState().withProperty(IDirectionalBlock.DIRECTION, facing);
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        boolean pressed = state.getValue(IWardOpeningBlock.WARD_OPENING);
        switch (state.getValue(IDirectionalBlock.DIRECTION)) {
            case EAST:
                return pressed ? AABB_EAST_ON : AABB_EAST_OFF;
            case WEST:
                return pressed ? AABB_WEST_ON : AABB_WEST_OFF;
            case SOUTH:
                return pressed ? AABB_SOUTH_ON : AABB_SOUTH_OFF;
            case NORTH:
            default:
                return pressed ? AABB_NORTH_ON : AABB_NORTH_OFF;
            case UP:
                return pressed ? AABB_UP_ON : AABB_UP_OFF;
            case DOWN:
                return pressed ? AABB_DOWN_ON : AABB_DOWN_OFF;
        }
    }
    
    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.getValue(IWardOpeningBlock.WARD_OPENING) ? 15 : 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (!state.getValue(IWardOpeningBlock.WARD_OPENING))
            return 0;
        else
            return state.getValue(IDirectionalBlock.DIRECTION) == side ? 15 : 0;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (state.getValue(IWardOpeningBlock.WARD_OPENING))
            return true;
        else if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) {
                IWardedTile warded = tile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
                if (warded != null && warded.hasPermission(player)) {
                    world.setBlockState(pos, state.withProperty(IWardOpeningBlock.WARD_OPENING, true), 3);
                    world.markBlockRangeForRenderUpdate(pos, pos);
                    playPressSound(world, pos);
                    world.notifyNeighborsOfStateChange(pos, this, false);
                    world.notifyNeighborsOfStateChange(pos.offset(state.getValue(IDirectionalBlock.DIRECTION).getOpposite()), this, false);
                    world.scheduleUpdate(pos, this, tickRate(world));
                    return true;
                }
            }
        }

        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(IWardOpeningBlock.WARD_OPENING)) {
            world.notifyNeighborsOfStateChange(pos, this, false);
            world.notifyNeighborsOfStateChange(pos.offset(state.getValue(IDirectionalBlock.DIRECTION).getOpposite()), this, false);
        }

        super.breakBlock(world, pos, state);
    }
    
    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {}
    
    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote && state.getValue(IWardOpeningBlock.WARD_OPENING)) {
            if (wood)
                checkWoodPressState(state, world, pos);
            else {
                world.setBlockState(pos, state.withProperty(IWardOpeningBlock.WARD_OPENING, false));
                world.notifyNeighborsOfStateChange(pos, this, false);
                world.notifyNeighborsOfStateChange(pos.offset(state.getValue(IDirectionalBlock.DIRECTION).getOpposite()), this, false);
                playReleaseSound(world, pos);
                world.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entityIn) {
        if (!world.isRemote && wood) {
            if (!state.getValue(IWardOpeningBlock.WARD_OPENING))
                checkWoodPressState(state, world, pos);
        }
    }

    protected boolean isEntityValidForWood(EntityArrow e, IWardedTile owner) {
        Entity shooter = e.shootingEntity;
        if (shooter instanceof EntityLivingBase)
            return owner.hasPermission((EntityLivingBase) shooter);
        else
            return false;
    }
    
    protected void checkWoodPressState(IBlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null) {
            IWardedTile warded = tile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
            if (warded != null) {
                List<? extends Entity> list = world.getEntitiesWithinAABB(EntityArrow.class, state.getBoundingBox(world, pos).offset(pos),
                        e -> isEntityValidForWood(e, warded));
                boolean powered = state.getValue(IWardOpeningBlock.WARD_OPENING);
                if (!list.isEmpty() && !powered) {
                    world.setBlockState(pos, state.withProperty(IWardOpeningBlock.WARD_OPENING, true));
                    world.notifyNeighborsOfStateChange(pos, this, false);
                    world.notifyNeighborsOfStateChange(pos.offset(state.getValue(IDirectionalBlock.DIRECTION).getOpposite()), this, false);
                    world.markBlockRangeForRenderUpdate(pos, pos);
                    playPressSound(world, pos);
                }
                else if (list.isEmpty() && powered) {
                    world.setBlockState(pos, state.withProperty(IWardOpeningBlock.WARD_OPENING, false));
                    world.notifyNeighborsOfStateChange(pos, this, false);
                    world.notifyNeighborsOfStateChange(pos.offset(state.getValue(IDirectionalBlock.DIRECTION).getOpposite()), this, false);
                    world.markBlockRangeForRenderUpdate(pos, pos);
                    playReleaseSound(world, pos);
                }

                if (!list.isEmpty())
                    world.scheduleUpdate(new BlockPos(pos), this, tickRate(world));
            }
        }
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(IDirectionalBlock.DIRECTION, rot.rotate(state.getValue(IDirectionalBlock.DIRECTION)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(IDirectionalBlock.DIRECTION)));
    }
    
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
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
        return new TileWardedButton();
    }
    
}
