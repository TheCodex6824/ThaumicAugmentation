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

package thecodex6824.thaumicaugmentation.common.container;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class MainHandItemHandler implements IItemHandlerModifiable {

    protected EntityLivingBase entity;
    
    public MainHandItemHandler(EntityLivingBase e) {
        entity = e;
    }
    
    protected void validateSlotIndex(int slot) {
        if (slot != 0)
            throw new RuntimeException("Slot " + slot + " not in valid range - [0,1)");
    }
    
    @Override
    @Nonnull
    @SuppressWarnings("null")
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);
        ItemStack existing = getStackInSlot(slot);
        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                entity.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
            
            return existing;
        }
        else {
            if (!simulate) {
                entity.setHeldItem(EnumHand.MAIN_HAND, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
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
        validateSlotIndex(slot);
        ItemStack stack = entity.getHeldItemMainhand();
        return stack != null ? stack : ItemStack.EMPTY;
    }
    
    @Override
    @Nonnull
    @SuppressWarnings("null")
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        validateSlotIndex(slot);
        ItemStack existing = getStackInSlot(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty())
                entity.setHeldItem(EnumHand.MAIN_HAND, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            else
                existing.grow(reachedLimit ? limit : stack.getCount());
            
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
    }
    
    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        validateSlotIndex(slot);
        entity.setHeldItem(EnumHand.MAIN_HAND, stack);
        onContentsChanged(slot);
    }
    
    public void onContentsChanged(int slot) {}
    
}
