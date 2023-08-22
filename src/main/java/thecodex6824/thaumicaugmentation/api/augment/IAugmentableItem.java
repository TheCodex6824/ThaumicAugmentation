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

package thecodex6824.thaumicaugmentation.api.augment;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Interface for the AugmentableItem capability. This interface allows arbitrary items to hold {@link IAugment}
 * instances.
 * @author TheCodex6824
 */
public interface IAugmentableItem {

    /**
     * Returns the current amount of augments equipped on this augmentable item.
     * @return The amount of augments equipped to this item
     */
    int getUsedAugmentSlots();
    
    /**
     * Returns the maximum amount of augments that can be equipped on this augmentable item.
     * @return The maximum amount of augments that can be equipped
     */
    int getTotalAugmentSlots();
    
    /**
     * Returns if the provided augment can be inserted into the given augment slot.
     * @param augment The augment to check
     * @param slot The slot to check
     * @return If the given augment can be inserted into the provided slot
     */
    boolean isAugmentAcceptable(ItemStack augment, int slot);
    
    /**
     * Inserts the given augment into the given slot. {@link #isAugmentAcceptable} returning true
     * acts as the precondition for this method.
     * @param augment The augment to insert
     * @param slot The slot to insert the augment into
     */
    void setAugment(ItemStack augment, int slot);
    
    /**
     * Returns the augment currently in the given slot, or {@link ItemStack#EMPTY} if no augment
     * is in that slot.
     * @param slot The slot to get the augment in
     * @return The stack of the augment, or an empty stack if no augment is in the slot
     */
    ItemStack getAugment(int slot);
    
    /**
     * Returns an array of all augments in this augmentable item.
     * @return An array of all the augments
     */
    ItemStack[] getAllAugments();
    
    /**
     * Sets all augment slots in the augmentable item to the augments in the provided array. Slots will be filled whether
     * they are already occupied or not, starting from 0 and going to either the augmentable item slot limited or the passed
     * array size, whichever is smaller. Any unset augments left in this augmentable item will be changed to {@link ItemStack#EMPTY}.
     * @param augs The augments to set
     * @return The array of old augments that used to be in this augmentable item
     */
    ItemStack[] setAllAugments(ItemStack[] augs);
    
    /**
     * Removes and returns the augment from the given slot.
     * @param slot The slot to remove an augment from
     * @return The removed augment or {@link ItemStack#EMPTY} if the slot was empty
     */
    ItemStack removeAugment(int slot);
    
    /**
     * Returns the first available augment slot in this item, or -1 if no slots are available.
     * @return The first open slot, or -1 if all slots are filled
     */
    int getNextAvailableSlot();
    
    /**
     * Returns if this augmentable item has any augments in it.
     * @return If this augmentable item has any augments in it
     */
    boolean isAugmented();

    /**
     * Returns the ItemStack that should be used to find applicable configurations.
     * This stack should have the least amount of extra data (metadata, NBT, etc)
     * needed to match the stack.
     * @param input The stack that may have extra data that needs to not be compared. This stack itself should not be modified.
     * @return A copy of the stack with extra data removed
     */
    public default ItemStack createConfigurationStack(ItemStack input) {
    	return new ItemStack(input.getItem(), 1, OreDictionary.WILDCARD_VALUE);
    }
    
    /**
     * Returns if this augmentable item can use the generic TA crafting recipe system.
     * Set to false if you plan on using another way of adding augments.
     * @return If the augment addition recipe is enabled for this augmentable item
     */
    default boolean shouldAllowDefaultAddition() {
        return true;
    }
    
    /**
     * Returns if this augmentable item can use the generic TA crafting recipe system.
     * Set to false if you plan on using another way of removing augments.
     * @return If the augment removal recipe is enabled for this augmentable item
     */
    default boolean shouldAllowDefaultRemoval() {
        return true;
    }
    
    NBTTagCompound getSyncNBT();
    
    void readSyncNBT(NBTTagCompound tag);
    
}
