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
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneTerraformer;

public class ContainerArcaneTerraformer extends Container {

    protected TileArcaneTerraformer tile;

    public ContainerArcaneTerraformer(InventoryPlayer inv, TileArcaneTerraformer t) {
        tile = t;
        IItemHandler inventory = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        
        addSlotToContainer(new SlotItemHandler(inventory, 0, 74, 33) {
            @Override
            public void onSlotChanged() {
                tile.markDirty();
            }
        });

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x)
                this.addSlotToContainer(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 94 + y * 18));
        }

        for (int x = 0; x < 9; ++x)
            this.addSlotToContainer(new Slot(inv, x, 8 + x * 18, 152));
    }
    
    public TileArcaneTerraformer getTile() {
        return tile;
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
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
