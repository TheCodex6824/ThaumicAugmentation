/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AugmentableItemSlot extends SlotItemHandler {
    
    protected IAugmentableItemSlotListener parent;
    
    public AugmentableItemSlot(IItemHandler itemHandler, int index, IAugmentableItemSlotListener parentContainer, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        parent = parentContainer;
    }
    
    @Override
    public void putStack(@Nonnull ItemStack stack) {
        ItemStack old = getStack();
        super.putStack(stack);
        if (!old.isItemEqual(stack))
            onSlotChange(old, stack);
    }
    
    @Override
    @Nonnull
    public ItemStack decrStackSize(int amount) {
        ItemStack old = getStack();
        ItemStack ret = super.decrStackSize(amount);
        if (!old.isItemEqual(getStack()))
            onSlotChange(old, getStack());
        
        return ret;
    }
    
    @Override
    public void onSlotChange(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {
        onSlotChanged();
        parent.onAugmentableItemSlotChanged(oldStack, newStack);
    }
    
}
