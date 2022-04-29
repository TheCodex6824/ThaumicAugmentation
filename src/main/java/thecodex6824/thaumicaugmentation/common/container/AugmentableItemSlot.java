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
    
    public AugmentableItemSlot(ContainerAugmentationStation parentContainer, int index, int xPosition, int yPosition) {
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
        }, index, xPosition, yPosition);
        
        parent = parentContainer;
    }
    
    @Override
    public void onSlotChange(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {
        // TODO add/remove augment slots as needed
    }
    
}
