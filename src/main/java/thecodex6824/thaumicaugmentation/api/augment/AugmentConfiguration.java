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

import javax.annotation.Nullable;

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
public class AugmentConfiguration implements IAugmentConfiguration, INBTSerializable<NBTTagCompound> {

    private Int2ObjectOpenHashMap<ItemStack> configuration;
    
    public AugmentConfiguration() {
        this(new ItemStack[0]);
    }
    
    public AugmentConfiguration(ItemStack[] augs) {
        configuration = new Int2ObjectOpenHashMap<>();
        int slot = 0;
        for (ItemStack aug : augs) {
            configuration.put(slot, aug);
            slot++;
        }
    }
    
    @Override
    public ImmutableMap<Integer, ItemStack> getAugmentConfig() {
        return ImmutableMap.copyOf(configuration);
    }

    @Override
    @Nullable
    public ItemStack setAugment(ItemStack augment, int slot) {
        ItemStack ret = configuration.put(slot, augment);
        if (ret == null)
            ret = ItemStack.EMPTY;
        
        return ret;
    }

    @Override
    @Nullable
    public ItemStack removeAugment(int slot) {
        ItemStack ret = configuration.remove(slot);
        if (ret == null)
            ret = ItemStack.EMPTY;
        
        return ret;
    }
    
    @Override
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
        for (Map.Entry<Integer, ItemStack> entry : configuration.entrySet()) {
            if (!entry.getValue().isEmpty())
                data.setTag("slot" + entry.getKey(), entry.getValue().serializeNBT());
        }
        
        return data;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
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
