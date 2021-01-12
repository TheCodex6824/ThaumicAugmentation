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

package thecodex6824.thaumicaugmentation.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;

public class TileItemGrate extends TileEntity {

    protected class ItemGrateInventory implements IItemHandler {
        
        @Override
        @Nonnull
        @SuppressWarnings("null")
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
        
        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
        
        @Override
        public int getSlots() {
            return 1;
        }
        
        @Override
        @Nonnull
        @SuppressWarnings("null")
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }
        
        @Override
        @Nonnull
        @SuppressWarnings("null")
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!world.getBlockState(pos).getValue(IEnabledBlock.ENABLED))
                return stack;
            else {
                if (!simulate && !stack.isEmpty() && !world.isRemote) {
                    Entity item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    Entity newItem = stack.getItem().createEntity(world, item, stack);
                    item = newItem != null ? newItem : item;
                    item.setPosition(item.posX, item.posY - item.height, item.posZ);
                    item.motionX = 0.0;
                    item.motionY = -Math.abs(item.motionY);
                    item.motionZ = 0.0;
                    item.velocityChanged = true;
                    world.spawnEntity(item);
                }
                
                return ItemStack.EMPTY;
            }
        }
        
    }
    
    protected ItemGrateInventory inventory;
    
    public TileItemGrate() {
        inventory = new ItemGrateInventory();
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return (facing == EnumFacing.UP && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) ||
                super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (facing == EnumFacing.UP && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        else
            return super.getCapability(capability, facing);
    }
    
}
