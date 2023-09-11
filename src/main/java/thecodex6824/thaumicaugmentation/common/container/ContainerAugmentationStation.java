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

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.AugmentAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.common.capability.CapabilityEquipmentTracker;
import thecodex6824.thaumicaugmentation.common.capability.IEquipmentTracker;
import thecodex6824.thaumicaugmentation.common.tile.TileAugmentationStation;

public class ContainerAugmentationStation extends Container implements IAugmentableItemSlotListener {
    
    protected WeakReference<EntityPlayer> thePlayer;
    protected TileAugmentationStation station;
    protected AugmentableItemSlot centralSlot;
    protected IntArrayList trackedAugmentSlots;
    protected int selectedConfiguration;
    protected int pouchSlot;
    
    public ContainerAugmentationStation(InventoryPlayer inv, TileAugmentationStation station) {
        // store player so we can do some lookups later
        thePlayer = new WeakReference<>(inv.player);
        this.station = station;
        
        // the loops below set up slots for the player's inventory
        for (int x = 0; x < 9; ++x) {
            addSlotToContainer(new Slot(inv, x, 8 + x * 18, 179));
        }
        
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                addSlotToContainer(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 121 + y * 18));
            }
        }
        
        // add the central augmentable item slot
        IItemHandler inventory = station.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        centralSlot = new AugmentableItemSlot(inventory, 0, this, 80, 80);
        addSlotToContainer(centralSlot);
        
        IEquipmentTracker tracker = inv.player.getCapability(CapabilityEquipmentTracker.EQUIPMENT_TRACKER, null);
        if (tracker != null) {
	        IItemHandler playerItems = tracker.getLiveEquipment();
	        pouchSlot = -1;
	        for (int i = 0; i < playerItems.getSlots(); ++i) {
	        	ItemStack stack = playerItems.getStackInSlot(i);
	        	if (stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
	        		pouchSlot = i;
	        		break;
	        	}
	        }
	        
	        if (pouchSlot != -1) {
	        	if (inventory.insertItem(1, playerItems.extractItem(pouchSlot, 64, true), true).isEmpty()) {
	        		ItemStack extracted = playerItems.extractItem(pouchSlot, 64, false);
	            	ItemStack remaining = inventory.insertItem(1, extracted, false);
	            	station.markDirty();
	            	if (!remaining.isEmpty()) {
	            		playerItems.insertItem(pouchSlot, remaining, false);
	            	}
	        	}
	        }
        }
        
    	IItemHandler pouchInv = inventory.getStackInSlot(1).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    	if (pouchInv != null) {
    		addSlotToContainer(new SlotItemHandler(inventory, 1, 209, 3) {
            	@Override
            	public boolean canTakeStack(EntityPlayer player) {
            		return false;
            	}
            });
    		
    		int numSlotsAdded = 0;
	    	for (int pouchSlot = 0; pouchSlot < pouchInv.getSlots(); ++pouchSlot) {
	    		if (pouchInv.isItemValid(pouchSlot, new ItemStack(TAItems.AUGMENT_VIS_BATTERY))) {
	                addSlotToContainer(new SlotItemHandler(pouchInv, pouchSlot,
	                		191 + (numSlotsAdded % 3) * 18, 22 + (numSlotsAdded / 3) * 18));
	                if (++numSlotsAdded >= 27) {
	                	break;
	                }
	    		}
	    	}
    	}
        
        trackedAugmentSlots = new IntArrayList();
    }
    
    public void addAugmentSlot(int index, int xPos, int yPos) {
    	SlotItemHandler newSlot = new SlotItemHandler(AugmentAPI.createAugmentItemHandler(centralSlot.getStack()),
        		index, xPos, yPos);
        addSlotToContainer(newSlot);
        trackedAugmentSlots.add(newSlot.slotNumber);
    }
    
    public void removeAllAugmentSlots() {
        for (int i = trackedAugmentSlots.size() - 1; i >= 0; --i) {
            int slot = trackedAugmentSlots.getInt(i);
            inventoryItemStacks.remove(slot);
            inventorySlots.remove(slot);
        }

        trackedAugmentSlots.clear();
    }
    
    public int[] getAugmentSlotIndices() {
        return trackedAugmentSlots.toIntArray();
    }
    
    @Override
    public ItemStack getAugmentableItem() {
    	return centralSlot.getStack();
    }
    
    @Override
    public void onAugmentableItemSlotChanged(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {
        boolean doRemove = false, doAdd = false;
        if (!newStack.isEmpty())
            doAdd = true;
        if (!oldStack.isEmpty() || newStack.isEmpty())
            doRemove = true;
        
        if (doRemove)
            removeAllAugmentSlots();
        
        if (doAdd) {
            int augmentSlots = newStack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).getTotalAugmentSlots();
            if (augmentSlots == 1) {
            	addAugmentSlot(0, centralSlot.xPos, centralSlot.yPos - 36);
            }
            else {
                double xIncrement = Math.PI / (augmentSlots - 1);
                int xSlotPosition, ySlotPosition;
                for (int i = 0; i < augmentSlots; ++i) {
                    xSlotPosition = (int) Math.round(Math.cos(Math.PI + xIncrement * i)) * augmentSlots * 16;
                    ySlotPosition = (int) Math.round(Math.sin(Math.PI + xIncrement * i)) * augmentSlots * 16;
                    addAugmentSlot(i, centralSlot.xPos + xSlotPosition, centralSlot.yPos + ySlotPosition);
                }
            }
        }

        if (doRemove || doAdd)
            detectAndSendChanges();
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer otherPlayer) {
        return otherPlayer.equals(thePlayer.get());
    }
    
    @Override
    public void onContainerClosed(EntityPlayer player) {
    	super.onContainerClosed(player);
    	if (player.equals(thePlayer.get())) {
    		IItemHandler inventory = station.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    		ItemStack pouch = inventory.extractItem(1, 64, false);
    		
    		IEquipmentTracker tracker = player.getCapability(CapabilityEquipmentTracker.EQUIPMENT_TRACKER, null);
    		if (tracker != null && pouchSlot != -1) {
    			IItemHandler playerItems = tracker.getLiveEquipment();
        		pouch = playerItems.insertItem(pouchSlot, pouch, false);
    		}
    		
    		if (!pouch.isEmpty() && !player.addItemStackToInventory(pouch)) {
				player.dropItem(pouch, false);
			}
    	}
    }
    
    @Override
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
