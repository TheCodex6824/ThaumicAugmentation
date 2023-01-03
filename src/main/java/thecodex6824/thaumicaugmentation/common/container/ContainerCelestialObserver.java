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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import thecodex6824.thaumicaugmentation.common.entity.EntityCelestialObserver;

import javax.annotation.Nonnull;

public class ContainerCelestialObserver extends Container {

    @Nonnull
    protected static final ItemStack PAPER = new ItemStack(Items.PAPER);
    
    protected EntityCelestialObserver entity;
    
    public ContainerCelestialObserver(InventoryPlayer inv, EntityCelestialObserver e) {
        entity = e;
        IItemHandler item = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        addSlotToContainer(new SlotItemHandler(item, 0, 147, 18));
        for (int i = 0; i < item.getSlots() - 1; ++i)
            addSlotToContainer(new SlotItemHandler(item, i + 1, 8 + i % 9 * 18, 80 + i / 9 * 23));
        
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x)
                addSlotToContainer(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 134 + y * 18));
        }
        
        for (int x = 0; x < 9; ++x)
            addSlotToContainer(new Slot(inv, x, 8 + x * 18, 192));
    }
    
    public EntityCelestialObserver getEntity() {
        return entity;
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.equals(entity.getOwner());
    }
    
    @Override
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
