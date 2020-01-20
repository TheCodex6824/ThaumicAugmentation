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

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.tile.TileVoidRechargePedestal;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class BlockVoidRechargePedestal extends BlockTABase implements IItemBlockProvider {

    public BlockVoidRechargePedestal() {
        super(Material.ROCK);
        setHardness(2.0F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);
    }
    
    @Override
    @SuppressWarnings("null")
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) {
                IItemHandler inventory = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
                if (inventory != null) {
                    boolean didSomething = false;
                    ItemStack old = inventory.extractItem(0, 1, false);
                    if (hand != null && player != null && inventory.isItemValid(0, player.getHeldItem(hand))) {
                        ItemStack copy = player.getHeldItem(hand).copy();
                        copy.setCount(1);
                        inventory.insertItem(0, copy, false);
                        player.getHeldItem(hand).shrink(1);
                        didSomething = true;
                    }
                    
                    if (!old.isEmpty()) {
                        if (hand != null && player.getHeldItem(hand).isEmpty())
                            player.setHeldItem(hand, old);
                        else if (!player.inventory.addItemStackToInventory(old))
                            world.spawnEntity(old.getItem().createEntity(world, player, old));
                        
                        didSomething = true;
                    }
                    
                    return didSomething;
                }
            }
        }
        else if (world.isRemote)
            return true;
        
        return false;
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    
    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileVoidRechargePedestal();
    }
    
    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }
    
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && !world.isRemote) {
            IItemHandler inv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (inv != null) {
                for (int i = 0; i < inv.getSlots(); ++i) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (stack != null && !stack.isEmpty()) {
                        world.spawnEntity(new EntityItem(world, pos.getX() + 0.5 + world.rand.nextGaussian() / 2, 
                                pos.getY() + 0.5 + Math.abs(world.rand.nextGaussian() / 2), pos.getZ() + 0.5 + world.rand.nextGaussian() / 2, stack));
                    }
                }
            }
        }
        
        if (tile instanceof IBreakCallback)
            ((IBreakCallback) tile).onBlockBroken();
        
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
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
    
}
