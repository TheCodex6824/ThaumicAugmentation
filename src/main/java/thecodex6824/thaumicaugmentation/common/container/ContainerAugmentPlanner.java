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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.augment.*;
import thecodex6824.thaumicaugmentation.common.network.PacketPartialAugmentConfigurationStorageSync;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

import java.util.List;

import javax.annotation.Nonnull;

public class ContainerAugmentPlanner extends Container implements IAugmentableItemSlotListener {
    
    protected EntityPlayer player;
    protected AugmentableItemSlot centralSlot;
    protected IntArrayList trackedAugmentSlots;
    protected int selectedConfiguration;
    
    public ContainerAugmentPlanner(InventoryPlayer inv) {
        // store player so we can do some lookups later
        player = inv.player;
        
        // add the central augmentable item slot
        // this is *not* saved in any way
        centralSlot = new AugmentableItemSlot(null, 0, this, 80, 90);
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
    
    public int getMaxConfigurations() {
        return 8;
    }
    
    public boolean doesCurrentConfigurationExist() {
    	IAugmentConfigurationStorage storage = player.getCapability(
                CapabilityAugmentConfigurationStorage.AUGMENT_CONFIGURATION_STORAGE, null);
    	return storage.getAllConfigurationsForItem(centralSlot.getStack()).size() > selectedConfiguration;
    }
    
    public AugmentConfiguration getOrCreateCurrentConfiguration() {
    	IAugmentConfigurationStorage storage = player.getCapability(
                CapabilityAugmentConfigurationStorage.AUGMENT_CONFIGURATION_STORAGE, null);
    	List<AugmentConfiguration> allConfigs = storage.getAllConfigurationsForItem(centralSlot.getStack());
    	if (allConfigs.size() <= selectedConfiguration) {
    		for (int i = 0; i < selectedConfiguration - allConfigs.size() + 1; ++i) {
                storage.addConfiguration(new AugmentConfiguration(centralSlot.getStack()));
    		}
    	}
        
    	return allConfigs.get(selectedConfiguration);
    }
    
    public void setSelectedConfiguration(int newConfig) {
        for (int index : trackedAugmentSlots) {
            Slot s = inventorySlots.get(index);
            if (s instanceof ConfigurationAugmentSlot) {
                ((ConfigurationAugmentSlot) s).changeConfiguration(newConfig);
            }
        }
        
        selectedConfiguration = newConfig;
        detectAndSendChanges();
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
            IAugmentConfigurationStorage storage = player.getCapability(
                    CapabilityAugmentConfigurationStorage.AUGMENT_CONFIGURATION_STORAGE, null);
            if (storage != null) {
                int augmentSlots = newStack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).getTotalAugmentSlots();
                if (augmentSlots == 1) {
                	addAugmentSlot(new ConfigurationAugmentSlot(this, selectedConfiguration, 0, centralSlot.xPos, centralSlot.yPos - 36));
                }
                else {
	                double xIncrement = Math.PI / (augmentSlots - 1);
	                int xSlotPosition, ySlotPosition;
	                for (int i = 0; i < augmentSlots; ++i) {
	                    xSlotPosition = (int) Math.round(Math.cos(Math.PI + xIncrement * i)) * augmentSlots * 16;
	                    ySlotPosition = (int) Math.round(Math.sin(Math.PI + xIncrement * i)) * augmentSlots * 16;
	                    addAugmentSlot(new ConfigurationAugmentSlot(this, selectedConfiguration, i, centralSlot.xPos + xSlotPosition, centralSlot.yPos + ySlotPosition));
	                }
                }

                setSelectedConfiguration(0);
            }
        }

        if (doRemove || doAdd)
            detectAndSendChanges();
    }
    
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        if (slotId > player.inventory.mainInventory.size()) {
            Slot clicked = inventorySlots.get(slotId);
            if (clickType != ClickType.CLONE && clicked instanceof ConfigurationAugmentSlot) {
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
        return otherPlayer.equals(player);
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
    
    @Override
    public void detectAndSendChanges() {
    	super.detectAndSendChanges();
    	if (!centralSlot.getStack().isEmpty()) {
	    	IAugmentConfigurationStorage storage = player.getCapability(CapabilityAugmentConfigurationStorage.AUGMENT_CONFIGURATION_STORAGE, null);
	    	if (player instanceof EntityPlayerMP && storage instanceof IAugmentConfigurationStorageSerializable) {
	        	IAugmentConfigurationStorageSerializable save = (IAugmentConfigurationStorageSerializable) storage;
	        	TANetwork.INSTANCE.sendTo(new PacketPartialAugmentConfigurationStorageSync(save.serializeConfigsForSingleItem(centralSlot.getStack())),
	        			(EntityPlayerMP) player);
	        }
    	}
    }
    
    protected static class ConfigurationAugmentSlot extends Slot {

        protected static final IInventory EMPTY_INV = new InventoryBasic("[Null]", true, 0);
        
        protected ContainerAugmentPlanner container;
        protected int configIndex;
        
        public ConfigurationAugmentSlot(ContainerAugmentPlanner container, int configIndex, int index, int xPosition, int yPosition) {
            super(EMPTY_INV, index, xPosition, yPosition);
            this.container = container;
            this.configIndex = configIndex;
        }
        
        public void changeConfiguration(int newConfigIndex) {
            configIndex = newConfigIndex;
        }
        
        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
        }
        
        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return false;
        }
        
        @Override
        public ItemStack decrStackSize(int amount) {
            return getStack();
        }
        
        @Override
        public boolean isSameInventory(Slot other) {
            return other instanceof ConfigurationAugmentSlot;
        }
        
        @Override
        public void onSlotChange(ItemStack oldStack, ItemStack newStack) {}
        
        @Override
        public void putStack(ItemStack stack) {
        	AugmentConfiguration config = container.getOrCreateCurrentConfiguration();
            if (config.isAugmentAcceptable(stack, getSlotIndex())) {
                config.setAugment(stack, getSlotIndex());
            }
        }
        
        @Override
        public ItemStack getStack() {
        	if (!container.doesCurrentConfigurationExist()) {
        		return ItemStack.EMPTY;
        	}
        	
        	AugmentConfiguration config = container.getOrCreateCurrentConfiguration();
            return config.getAugment(getSlotIndex());
        }
        
    }
}
