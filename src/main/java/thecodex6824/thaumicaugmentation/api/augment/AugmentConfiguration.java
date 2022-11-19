/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 KevoHoff.
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

package thecodex6824.thaumicaugmentation.api.augment;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.oredict.OreDictionary;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;

import java.util.ArrayList;
import java.util.Arrays;

/**
* Represents a configuration of augments for an item, with empty slots included.
* @author KevoHoff
*/
public class AugmentConfiguration implements INBTSerializable<NBTTagCompound> {

    protected ItemStack configOwner;
    protected String name;
    protected ItemStack[] configuration;
    
    public AugmentConfiguration(NBTTagCompound deserialize) {
        configuration = new ItemStack[0];
        deserializeNBT(deserialize);
    }
    
    public AugmentConfiguration(ItemStack augmentable) {
        int slots = augmentable.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).getTotalAugmentSlots();
        configOwner = augmentable.copy();
        configuration = new ItemStack[slots];
        Arrays.fill(configuration, ItemStack.EMPTY);
    }
    
    /**
     * Returns the name of this configuration.
     * The name is not guaranteed to be unique or non-empty.
     * @return The name of this configuration.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of this configuration.
     * The name should not be null, but can be empty or non-unique.
     * @param newName The new name of this configuration
     */
    public void setName(String newName) {
        name = newName;
    }
    
    /**
     * Returns the ItemStack that is intended to hold this configuration.
     * The stack should have the minimum amount of information (metadata, NBT, etc)
     * needed to match an input ItemStack.
     * @return The item this configuration is intended for
     */
    public ItemStack getConfigurationItemStack() {
        return configOwner;
    }

    protected void verifyIndex(int slot) {
        if (slot < 0 || slot >= configuration.length)
            throw new IndexOutOfBoundsException("Illegal slot index: " + slot + ", valid range: [0, " + configuration.length + ")");
    }

    public ItemStack getAugment(int slot) {
        verifyIndex(slot);
        return configuration[slot].copy();
    }
    
    /**
     * Returns a read-only view of the augments in this configuration.
     * @return The specific augments contained in the configuration
     */
    public ImmutableList<ItemStack> getAugmentConfig() {
        return ImmutableList.copyOf(configuration);
    }

    /**
     * Sets an augment in the augment configuration.
     * @param augment The augment to set in some slot
     * @param slot The slot the augment is being added to
     * @return The augment previously in this slot, or the empty ItemStack if it was empty
     */
    public ItemStack setAugment(ItemStack augment, int slot) {
        verifyIndex(slot);
        ItemStack ret = configuration[slot];
        configuration[slot] = augment.copy();
        return ret;
    }

    /**
     * Removes an augment from the configuration.
     * @param slot The slot the augment is being removed from
     * @return The removed augment if it was present, the empty ItemStack otherwise.
     */
    public ItemStack removeAugment(int slot) {
        return setAugment(ItemStack.EMPTY, slot);
    }
    
    /**
     * Returns if the provided augment can be inserted into the configuration.
     * @param augment The augment to check
     * @param slot The slot the augment would go into
     * @return If the given augment can be inserted into the configuration
     */
    public boolean isAugmentAcceptable(ItemStack augment, int slot) {
        for (int i = 0; i < configuration.length; ++i) {
            if (i != slot) {
                ItemStack aug = configuration[i];
                if (!aug.isEmpty() && !aug.getCapability(CapabilityAugment.AUGMENT, null).isCompatible(augment))
                    return false;
            }
        }
        
        return true;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = new NBTTagCompound();
        data.setTag("owner", configOwner.serializeNBT());
        data.setString("name", name);
        for (int i = 0; i < configuration.length; ++i) {
            ItemStack stack = configuration[i];
            if (!stack.isEmpty())
                data.setTag("slot" + i, stack.serializeNBT());
        }
        
        return data;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        configOwner = new ItemStack(nbt.getCompoundTag("owner"));
        configuration = new ItemStack[configOwner.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).getTotalAugmentSlots()];
        Arrays.fill(configuration, ItemStack.EMPTY);
        name = nbt.getString("name");
        for (String key : nbt.getKeySet()) {
            if (nbt.hasKey(key, NBT.TAG_COMPOUND) && key.startsWith("slot")) {
                String slotNum = key.substring("slot".length());
                try {
                    int slot = Integer.parseInt(slotNum);
                    if (slot >= 0 && slot < configuration.length) {
                        configuration[slot] = new ItemStack(nbt.getCompoundTag(key));
                    }
                    else {
                        ThaumicAugmentation.getLogger().warn("Out of bounds slot number for augment configuration, discarding");
                    }
                }
                catch (NumberFormatException ex) {
                    ThaumicAugmentation.getLogger().warn("Invalid slot number for augment configuration, discarding");
                }
            }
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AugmentConfiguration) {
            AugmentConfiguration other = (AugmentConfiguration) obj;
            if (!ItemStack.areItemStacksEqual(configOwner, other.configOwner))
                return false;
            else if (!name.equals(other.name))
                return false;
            else if (!Arrays.equals(configuration, other.configuration))
                return false;
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        ArrayList<Object> toHash = new ArrayList<>();
        toHash.add(configOwner.getItem().getRegistryName());
        toHash.add(configOwner.getHasSubtypes() ? configOwner.getMetadata() : OreDictionary.WILDCARD_VALUE);
        toHash.add(configOwner.getTagCompound());
        toHash.add(name);
        for (ItemStack s : configuration) {
            toHash.add(s.getItem().getRegistryName());
            toHash.add(s.getHasSubtypes() ? s.getMetadata() : OreDictionary.WILDCARD_VALUE);
            toHash.add(s.getTagCompound());
        }
        
        return Arrays.deepHashCode(toHash.toArray());
    }
    
}
