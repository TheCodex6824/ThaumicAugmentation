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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAugmentationStation extends Container {
    
    protected EntityPlayer player;
    
    public ContainerAugmentationStation(InventoryPlayer inv) {
        // store player so we can do some lookups later
        player = inv.player;
        // add the central augmentable item slot
        // this is *not* saved in any way
        addSlotToContainer(new AugmentableItemSlot(this, 0, 36, 29));
        
        // the loops below set up slots for the player's inventory
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x)
                addSlotToContainer(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
        }
        
        for (int x = 0; x < 9; ++x)
            addSlotToContainer(new Slot(inv, x, 8 + x * 18, 142));
    }
    
    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        // TODO return augmentable item to player (if it is still there)
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer otherPlayer) {
        return otherPlayer.equals(player); // don't let random people access this GUI
    }
    
    @Override
    /**
     * Boilerplate implementation to allow shift-clicking and other expected behavior.
     */
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack otherStack = slot.getStack();
            stack = otherStack.copy();

            int containerSlots = inventorySlots.size() - player.inventory.mainInventory.size();
            if (index < containerSlots) {
                if (!this.mergeItemStack(otherStack, containerSlots, inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            }
            else if (!this.mergeItemStack(otherStack, 0, containerSlots, false))
                return ItemStack.EMPTY;

            if (otherStack.getCount() == 0)
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();

            if (otherStack.getCount() == stack.getCount())
                return ItemStack.EMPTY;

            slot.onTake(player, otherStack);
        }

        return stack;
    }
}
