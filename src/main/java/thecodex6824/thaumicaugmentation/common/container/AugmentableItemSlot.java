/**
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

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;

public class AugmentableItemSlot extends SlotItemHandler {
    
    protected ContainerAugmentationStation parent;
    
    public AugmentableItemSlot(ContainerAugmentationStation parentContainer, int xPosition, int yPosition) {
        super(new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null) &&
                        super.isItemValid(slot, stack);
            }
            
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        }, 0, xPosition, yPosition);
        
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
        boolean doRemove = false, doAdd = false;
        if (!newStack.isEmpty())
            doAdd = true;
        if (!oldStack.isEmpty() || newStack.isEmpty())
            doRemove = true;
        
        if (doRemove)
            parent.removeAllAugmentSlots();
        
        if (doAdd) {
            AugmentItemHandler handler = new AugmentItemHandler(newStack.getCapability(
                    CapabilityAugmentableItem.AUGMENTABLE_ITEM, null));
            int augmentSlots = handler.getSlots();
            double xIncrement = Math.PI / (augmentSlots - 1);
            int xSlotPosition, ySlotPosition;
            for (int i = 0; i < augmentSlots; ++i) {
                xSlotPosition = (int) Math.round(Math.cos(Math.PI + xIncrement*i))*augmentSlots*16;
                ySlotPosition = (int) Math.round(Math.sin(Math.PI + xIncrement*i))*augmentSlots*16;
                parent.addAugmentSlot(new SlotItemHandler(handler, i, xPos + xSlotPosition, yPos + ySlotPosition));
            }
        }
    }
    
}
