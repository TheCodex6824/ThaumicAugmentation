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

import java.util.Iterator;
import java.util.Random;

import com.google.common.base.Predicate;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;
import thaumcraft.api.casters.ICaster;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IUnwardableBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IWardParticles;
import thecodex6824.thaumicaugmentation.api.tile.INameableTile;
import thecodex6824.thaumicaugmentation.api.ward.WardHelper;
import thecodex6824.thaumicaugmentation.api.ward.tile.CapabilityWardedInventory;
import thecodex6824.thaumicaugmentation.api.ward.tile.CapabilityWardedTile;
import thecodex6824.thaumicaugmentation.api.ward.tile.IWardedInventory;
import thecodex6824.thaumicaugmentation.api.ward.tile.IWardedTile;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedChest;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;
import thecodex6824.thaumicaugmentation.init.GUIHandler;

public class BlockWardedChest extends BlockTABase implements IHorizontallyDirectionalBlock, IItemBlockProvider,
    IUnwardableBlock, IWardParticles {

    protected static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375);

    public BlockWardedChest() {
        super(Material.IRON);
        setBlockUnbreakable();
        setResistance(Float.MAX_VALUE / 16.0F);
        setDefaultState(getDefaultState().withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.SOUTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(IHorizontallyDirectionalBlock.DIRECTION).add(
                Properties.AnimationProperty).add(Properties.StaticProperty).build();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        // this is a legacy thing from when all blocks shared meta space
        // horizontal direction took bits 1 and 2 here (bit 0 was for enabled block)
        // yeah that system needed to go away a long time ago...
        return BitUtil.setBits(0, 1, 3, state.getValue(IHorizontallyDirectionalBlock.DIRECTION).getHorizontalIndex());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.byHorizontalIndex(BitUtil.getBits(meta, 1, 3)));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {

        return getDefaultState().withProperty(IHorizontallyDirectionalBlock.DIRECTION, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileWardedChest();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    protected void dropContents(World world, BlockPos pos) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                IWardedInventory items = te.getCapability(CapabilityWardedInventory.WARDED_INVENTORY, null);
                if (items != null) {
                    for (int i = 0; i < items.getSlots(); ++i) {
                        ItemStack stack = items.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            world.spawnEntity(new EntityItem(world, pos.getX() + 0.5 + world.rand.nextGaussian() / 2, 
                                    pos.getY() + 0.5 + Math.abs(world.rand.nextGaussian() / 2), pos.getZ() + 0.5 + world.rand.nextGaussian() / 2, stack));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        dropContents(world, pos);
        super.breakBlock(world, pos, state);
    }
    
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        
        super.getDrops(drops, world, pos, state, fortune);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof INameableTile && ((INameableTile) te).hasCustomName()) {
            Random rand = world instanceof World ? ((World) world).rand : RANDOM;
            ItemStack renamed = ItemStack.EMPTY;
            Iterator<ItemStack> it = drops.iterator();
            while (it.hasNext()) {
                ItemStack stack = it.next();
                if (stack.getItem() == getItemDropped(state, rand, fortune)) {
                    it.remove();
                    renamed = stack.copy();
                    renamed.setStackDisplayName(((INameableTile) te).getCustomName());
                }
            }
            
            drops.add(renamed);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) {
                IWardedTile warded = tile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
                if (warded != null && warded.hasPermission(player)) {
                    if (!(player.getHeldItem(hand).getItem() instanceof ICaster) || !player.isSneaking())
                        player.openGui(ThaumicAugmentation.instance, GUIHandler.TAInventory.WARDED_CHEST.getID(), world, 
                                pos.getX(), pos.getY(), pos.getZ());
    
                    return true;
                }
                else
                    return false;
            }
        }

        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
            ItemStack stack) {

        TileEntity tile = world.getTileEntity(pos);
        if (!world.isRemote) {
            if (tile != null) {
                IWardedTile warded = tile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
                if (warded != null)
                    warded.setOwner(placer.getUniqueID());
            }
        }
        
        if (stack.hasDisplayName() && tile instanceof INameableTile)
            ((INameableTile) tile).setCustomName(stack.getDisplayName());

        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }
    
    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
        if (!world.isRemote)
            return true;
        else {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null)
                return tile.receiveClientEvent(id, param);
            else
                return false;
        }
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
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
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
