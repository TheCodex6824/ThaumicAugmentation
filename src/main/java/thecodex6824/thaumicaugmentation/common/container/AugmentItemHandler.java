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

package thecodex6824.thaumicaugmentation.common.container;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;

public class AugmentItemHandler implements IItemHandlerModifiable {

    protected IAugmentableItem item;
    
    public AugmentItemHandler(IAugmentableItem item) {
        this.item = item;
    }
    
    @Override
    public int getSlots() {
        return item.getTotalAugmentSlots();
    }
    
    @Override
    public int getSlotLimit(int slot) {
        checkIndex(slot);
        return 64;
    }
    
    @Override
    @Nonnull
    @SuppressWarnings("null")
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        checkIndex(slot);
        if (amount == 0)
            return ItemStack.EMPTY;
        
        ItemStack augment = item.getAugment(slot).copy();
        augment.setCount(Math.min(augment.getCount(), amount));
        if (!simulate && !augment.isEmpty()) {
            augment = item.removeAugment(slot); 
            if (amount < augment.getCount()) {
                ItemStack reduced = augment.copy();
                reduced.setCount(augment.getCount() - amount);
                if (item.isAugmentAcceptable(reduced, slot))
                    item.setAugment(reduced, slot);
                else
                    augment.grow(augment.getCount() - amount);
            }
        }
        
        return augment;
    }
    
    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        checkIndex(slot);
        return item.getAugment(slot);
    }
    
    @Override
    @Nonnull
    @SuppressWarnings("null")
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        checkIndex(slot);
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        else if (!item.isAugmentAcceptable(stack, slot))
            return stack;
        
        ItemStack existing = item.getAugment(slot);
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
                item.setAugment(reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack, slot);
            else
                existing.grow(reachedLimit ? limit : stack.getCount());
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
    }
    
    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return item.isAugmentAcceptable(stack, slot);
    }
    
    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        checkIndex(slot);
        item.setAugment(stack, slot);
    }
    
    protected void checkIndex(int slot) {
        if (slot < 0 || slot >= item.getTotalAugmentSlots()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," +
                    item.getTotalAugmentSlots() + ")");
        }
    }
    
}
