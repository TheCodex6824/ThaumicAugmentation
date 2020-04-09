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

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockPressurePlate.Sensitivity;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.block.property.IUnwardableBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IWardOpeningBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IWardParticles;
import thecodex6824.thaumicaugmentation.api.warded.WardHelper;
import thecodex6824.thaumicaugmentation.api.warded.tile.CapabilityWardedTile;
import thecodex6824.thaumicaugmentation.api.warded.tile.IWardedTile;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedPressurePlate;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;

public class BlockWardedPressurePlate extends BlockTABase implements IWardOpeningBlock,
    IItemBlockProvider, IUnwardableBlock, IWardParticles {

    protected static final AxisAlignedBB PRESSED_AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.03125, 0.9375);
    protected static final AxisAlignedBB UNPRESSED_AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.0625, 0.9375);
    protected static final AxisAlignedBB PRESSURE_AABB = new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.25, 0.875);
    
    protected final Sensitivity sens;
    
    public BlockWardedPressurePlate(Material material, Sensitivity sensitivity, SoundType sound) {
        super(material);
        sens = sensitivity;
        setSoundType(sound);
        setBlockUnbreakable();
        setResistance(Float.MAX_VALUE / 16.0F);
        setTickRandomly(true);
        setDefaultState(getDefaultState().withProperty(IWardOpeningBlock.WARD_OPENING, false));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemBlock(this) {
            @Override
            public IRarity getForgeRarity(ItemStack stack) {
                return TAMaterials.RARITY_ARCANE;
            }
        };
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IWardOpeningBlock.WARD_OPENING);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(IWardOpeningBlock.WARD_OPENING) ? 1 : 0;
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IWardOpeningBlock.WARD_OPENING, BitUtil.isBitSet(meta, 0));
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(IWardOpeningBlock.WARD_OPENING) ? PRESSED_AABB : UNPRESSED_AABB;
    }

    @Override
    public int tickRate(World worldIn) {
        return 20;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSpawnInBlock() {
        return true;
    }
    
    protected void playPressSound(World world, BlockPos pos) {
        if (material == Material.WOOD)
            world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
        else
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
    }
    
    protected void playReleaseSound(World world, BlockPos pos) {
        if (material == Material.WOOD)
            world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
        else
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
    }
    
    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {}

    protected boolean isEntityValid(Entity firstEntity, IWardedTile owner) {
        HashSet<Entity> visited = new HashSet<>();
        ArrayDeque<Entity> toCheck = new ArrayDeque<>();
        toCheck.add(firstEntity);
        do {
            Entity e = toCheck.pop();
            if (!visited.contains(e)) {
                visited.add(e);
                if (e instanceof EntityLivingBase && owner.hasPermission((EntityLivingBase) e))
                    return true;
                else if (e instanceof EntityArrow && ((EntityArrow) e).shootingEntity != null)
                    toCheck.add(((EntityArrow) e).shootingEntity);
                else if (e instanceof EntityThrowable && ((EntityThrowable) e).thrower != null)
                    toCheck.add(((EntityThrowable) e).thrower);
                else if (e instanceof EntityFireball && ((EntityFireball) e).shootingEntity != null)
                    toCheck.add(((EntityFireball) e).shootingEntity);
            }
        } while (!toCheck.isEmpty());
        
        return false;
    }
    
    protected boolean shouldBePressed(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null) {
            IWardedTile warded = tile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
            if (warded != null) {
                List<Entity> list = null;
                if (sens == Sensitivity.EVERYTHING) {
                    list = world.getEntitiesWithinAABB(Entity.class, PRESSURE_AABB.offset(pos),
                            e -> isEntityValid(e, warded));
                }
                else {
                    list = world.getEntitiesWithinAABB(EntityLivingBase.class, PRESSURE_AABB.offset(pos),
                            e -> isEntityValid(e, warded));
                }

                for (Entity e : list) {
                    if (!e.doesEntityNotTriggerPressurePlate())
                        return true;
                }
            }
        }
        
        return false;
    }
    
    protected void updateState(World world, BlockPos pos, IBlockState state) {
        boolean wasPressed = state.getValue(IWardOpeningBlock.WARD_OPENING);
        boolean isPressed = shouldBePressed(world, pos, state);
        if (wasPressed != isPressed) {
            world.setBlockState(pos, state.withProperty(IWardOpeningBlock.WARD_OPENING, isPressed), 2);
            world.notifyNeighborsOfStateChange(pos, this, false);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }

        if (!isPressed && wasPressed)
            playReleaseSound(world, pos);
        else if (isPressed && !wasPressed)
            playPressSound(world, pos);

        if (isPressed)
            world.scheduleUpdate(pos, this, tickRate(world));
    }
    
    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote && state.getValue(IWardOpeningBlock.WARD_OPENING))
            updateState(world, pos, state);
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!world.isRemote && !state.getValue(IWardOpeningBlock.WARD_OPENING))
            updateState(world, pos, state);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(IWardOpeningBlock.WARD_OPENING))
            world.notifyNeighborsOfStateChange(pos, this, false);

        super.breakBlock(world, pos, state);
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.getValue(IWardOpeningBlock.WARD_OPENING) ? 15 : 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (side == EnumFacing.UP)
            return state.getValue(IWardOpeningBlock.WARD_OPENING) ? 15 : 0;
        else
            return 0;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }
    
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
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
        return new TileWardedPressurePlate();
    }
    
}
