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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.augment.AugmentAPI;
import thecodex6824.thaumicaugmentation.api.augment.AugmentConfiguration;
import thecodex6824.thaumicaugmentation.api.augment.AugmentConfigurationApplyResult;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentConfigurationStorage;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentConfigurationStorage;

public class ContainerAugmentationStation extends Container {
    
    protected EntityPlayer player;
    protected AugmentableItemSlot centralSlot;
    protected IntArrayList trackedAugmentSlots;
    protected int selectedConfiguration;
    
    public ContainerAugmentationStation(InventoryPlayer inv) {
        // store player so we can do some lookups later
        player = inv.player;
        
        // add the central augmentable item slot
        // this is *not* saved in any way
        centralSlot = new AugmentableItemSlot(this, 80, 90);
        addSlotToContainer(centralSlot);
        
        // the loops below set up slots for the player's inventory
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x)
                addSlotToContainer(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 131 + y * 18));
        }
        
        for (int x = 0; x < 9; ++x)
            addSlotToContainer(new Slot(inv, x, 8 + x * 18, 189));
        
        trackedAugmentSlots = new IntArrayList();
    }
    
    public void addAugmentSlot(Slot slot) {
        addSlotToContainer(slot);
        trackedAugmentSlots.add(slot.slotNumber);
        // TODO network sync???
    }
    
    public void removeAllAugmentSlots() {
        for (int i = trackedAugmentSlots.size() - 1; i >= 0; --i) {
            int slot = trackedAugmentSlots.getInt(i);
            inventoryItemStacks.remove(slot);
            inventorySlots.remove(slot);
        }
        
        // TODO sync?
        trackedAugmentSlots.clear();
    }
    
    public int[] getAugmentSlotIndices() {
        return trackedAugmentSlots.toIntArray();
    }
    
    public boolean hasAugmentableItem() {
        return centralSlot.getHasStack();
    }
    
    public int getMaxConfigurations() {
        return 8;
    }
    
    protected void ensureConfigIndex(ItemStack augmentable, IAugmentConfigurationStorage storage, int index) {
        if (storage.getAllConfigurationsForItem(augmentable).size() <= index) {
            for (int i = 0; i < index - storage.getAllConfigurationsForItem(augmentable).size() + 1; ++i)
                storage.addConfiguration(new AugmentConfiguration(augmentable));
        }
    }
    
    public void setSelectedConfiguration(int newConfig) {
        IAugmentConfigurationStorage storage = player.getCapability(
                CapabilityAugmentConfigurationStorage.AUGMENT_CONFIGURATION_STORAGE, null);
        if (storage != null) {
            ensureConfigIndex(centralSlot.getStack(), storage, newConfig);
            AugmentConfiguration c = storage.getAllConfigurationsForItem(centralSlot.getStack()).get(newConfig);
            for (int index : trackedAugmentSlots) {
                Slot s = inventorySlots.get(index);
                if (s instanceof AugmentSlot)
                    ((AugmentSlot) s).changeConfiguration(c);
            }
            
            selectedConfiguration = newConfig;
            detectAndSendChanges();
        }
    }
    
    public void onCentralSlotChanged(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {
        boolean doRemove = false, doAdd = false;
        if (!newStack.isEmpty())
            doAdd = true;
        if (!oldStack.isEmpty() || newStack.isEmpty())
            doRemove = true;
        
        if (doRemove)
            removeAllAugmentSlots();
        
        if (doAdd) {
            IAugmentConfigurationStorage storage = player.getCapability(
                    CapabilityAugmentConfigurationStorage.AUGMENT_CONFIGURATION_STORAGE, null);
            if (storage != null) {
                ensureConfigIndex(newStack, storage, 0);
                AugmentConfiguration config = storage.getAllConfigurationsForItem(newStack).get(0);
                int augmentSlots = newStack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).getTotalAugmentSlots();
                double xIncrement = Math.PI / (augmentSlots - 1);
                int xSlotPosition, ySlotPosition;
                for (int i = 0; i < augmentSlots; ++i) {
                    xSlotPosition = (int) Math.round(Math.cos(Math.PI + xIncrement*i))*augmentSlots*16;
                    ySlotPosition = (int) Math.round(Math.sin(Math.PI + xIncrement*i))*augmentSlots*16;
                    addAugmentSlot(new AugmentSlot(config, i, centralSlot.xPos + xSlotPosition, centralSlot.yPos + ySlotPosition));
                }
                
                setSelectedConfiguration(0);
            }
        }
    }
    
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        if (slotId > player.inventory.mainInventory.size()) {
            Slot clicked = inventorySlots.get(slotId);
            if (clickType != ClickType.CLONE && clicked instanceof AugmentSlot) {
                switch (clickType) {
                    case SWAP: {
                        clicked.putStack(player.inventory.getStackInSlot(dragType).copy());
                        break;
                    }
                    case PICKUP:
                    case PICKUP_ALL: {
                        clicked.putStack(player.inventory.getItemStack().copy());
                        break;
                    }
                    default: break;
                }
                
                detectAndSendChanges();
                return player.inventory.getStackInSlot(dragType);
            }
        }
        
        return super.slotClick(slotId, dragType, clickType, player);
    }
    
    public AugmentConfigurationApplyResult tryApplyConfiguration() {
        IAugmentConfigurationStorage storage = player.getCapability(
                CapabilityAugmentConfigurationStorage.AUGMENT_CONFIGURATION_STORAGE, null);
        if (storage != null) {
            AugmentConfigurationApplyResult result = AugmentAPI.trySwapConfiguration(player,
                    storage.getAllConfigurationsForItem(centralSlot.getStack()).get(selectedConfiguration),
                    centralSlot.getStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null),
                    false
            );
            if (result == AugmentConfigurationApplyResult.OK)
                player.getEntityWorld().playSound(null, player.getPosition(), SoundsTC.ticks, SoundCategory.PLAYERS, 1.0F, 1.0F);
                
            detectAndSendChanges();
            return result;
        }
        
        return AugmentConfigurationApplyResult.OTHER_PROBLEM;
    }
    
    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (centralSlot.getHasStack()) {
            ItemStack central = centralSlot.getStack();
            centralSlot.onTake(player, central);
            if (!central.isEmpty())
                player.dropItem(central, false);
        }
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

            if (index == 0 || index > player.inventory.mainInventory.size()) {
                if (!this.mergeItemStack(otherStack, 1, player.inventory.mainInventory.size() + 1, true))
                    return ItemStack.EMPTY;
            }
            else if (!this.mergeItemStack(otherStack, 0, 1, false) &&
                    !this.mergeItemStack(otherStack, player.inventory.mainInventory.size(), inventorySlots.size(), false)) {
                
                return ItemStack.EMPTY;
            }

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
