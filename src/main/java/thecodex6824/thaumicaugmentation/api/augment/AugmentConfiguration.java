/**
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

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;

/**
* Default implementation of the Augmentable Item capability.
* @author KevoHoff
* 
*/
public class AugmentConfiguration implements INBTSerializable<NBTTagCompound> {

    protected ItemStack configOwner;
    protected String name;
    protected Int2ObjectOpenHashMap<ItemStack> configuration;
    
    public AugmentConfiguration(NBTTagCompound deserialize) {
        deserializeNBT(deserialize);
    }
    
    public AugmentConfiguration(ItemStack augmentable) {
        this(augmentable, new ItemStack[0], "");
    }
    
    public AugmentConfiguration(ItemStack augmentable, ItemStack[] augs, String name) {
        configOwner = augmentable.copy();
        this.name = name;
        configuration = new Int2ObjectOpenHashMap<>();
        int slot = 0;
        for (ItemStack aug : augs) {
            configuration.put(slot, aug);
            slot++;
        }
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
    
    /**
     * Returns a read-only view of the augments in this configuration.
     * @return The specific augments contained in the configuration
     */
    public ImmutableMap<Integer, ItemStack> getAugmentConfig() {
        return ImmutableMap.copyOf(configuration);
    }

    /**
     * Sets an augment in the augment configuration.
     * @param augment The augment to set in some slot
     * @param slot The slot the augment is being added to
     * @return The augment previously in this slot, or the empty ItemStack if it was empty
     */
    public ItemStack setAugment(ItemStack augment, int slot) {
        ItemStack ret = configuration.put(slot, augment);
        if (ret == null)
            ret = ItemStack.EMPTY;
        
        return ret;
    }

    /**
     * Removes an augment from the configuration.
     * @param slot The slot the augment is being removed from
     * @return The removed augment if it was present, the empty ItemStack otherwise.
     */
    public ItemStack removeAugment(int slot) {
        ItemStack ret = configuration.remove(slot);
        if (ret == null)
            ret = ItemStack.EMPTY;
        
        return ret;
    }
    
    /**
     * Returns if the provided augment can be inserted into the configuration.
     * @param augment The augment to check
     * @param slot The slot the augment would go into
     * @return If the given augment can be inserted into the configuration
     */
    public boolean isAugmentAcceptable(ItemStack augment, int slot) {
        for (ItemStack aug : configuration.values()) {
            if (!aug.isEmpty() && !aug.getCapability(CapabilityAugment.AUGMENT, null).isCompatible(augment))
                return false;
        }
        
        return true;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = new NBTTagCompound();
        data.setTag("owner", configOwner.serializeNBT());
        data.setString("name", name);
        for (Map.Entry<Integer, ItemStack> entry : configuration.entrySet()) {
            if (!entry.getValue().isEmpty())
                data.setTag("slot" + entry.getKey(), entry.getValue().serializeNBT());
        }
        
        return data;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        configOwner = new ItemStack(nbt.getCompoundTag("owner"));
        name = nbt.getString("name");
        for (String key : nbt.getKeySet()) {
            if (nbt.hasKey(key, NBT.TAG_COMPOUND) && key.startsWith("slot")) {
                String slotNum = key.substring("slot".length());
                try {
                    int slot = Integer.parseInt(slotNum);
                    configuration.put(slot, new ItemStack(nbt.getCompoundTag(key)));
                }
                catch (NumberFormatException ex) {
                    ThaumicAugmentation.getLogger().warn("Invalid slot number for augment configuration, discarding");
                }
            }
        }
    }
    
}
