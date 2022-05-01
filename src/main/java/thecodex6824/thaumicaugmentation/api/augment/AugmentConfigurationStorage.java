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

package thecodex6824.thaumicaugmentation.api.augment;

import java.util.Collection;
import java.util.HashSet;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.oredict.OreDictionary;

public class AugmentConfigurationStorage implements IAugmentConfigurationStorage, INBTSerializable<NBTTagCompound> {

    protected Object2ObjectOpenCustomHashMap<ItemStack, HashSet<AugmentConfiguration>> configs;
    
    public AugmentConfigurationStorage() {
        configs = new Object2ObjectOpenCustomHashMap<>(new Strategy<ItemStack>() {
            @Override
            public boolean equals(ItemStack a, ItemStack b) {
                if (a.getItem() != b.getItem())
                    return false;
                else if ((a.getHasSubtypes() || b.getHasSubtypes()) && a.getMetadata() != b.getMetadata())
                    return false;
                else if (!ItemStack.areItemStackTagsEqual(a, b))
                    return false;
                
                return true;
            }
            
            @Override
            public int hashCode(ItemStack o) {
                return Objects.hashCode(o.getItem().getRegistryName(),
                        o.getHasSubtypes() ? o.getMetadata() : OreDictionary.WILDCARD_VALUE,
                        o.getTagCompound());
            }
        });
    }
    
    @Override
    public boolean addConfiguration(AugmentConfiguration config) {
        return configs.computeIfAbsent(config.getConfigurationItemStack(),
                s -> new HashSet<>()).add(config);
    }
    
    @Override
    public boolean removeConfiguration(AugmentConfiguration config) {
        return configs.computeIfAbsent(config.getConfigurationItemStack(),
                s -> new HashSet<>()).remove(config);
    }
    
    protected boolean areStacksEqualEnough(ItemStack template, ItemStack input) {
        if (template.getItem() != input.getItem())
            return false;
        else if (template.getHasSubtypes() && template.getMetadata() != input.getMetadata())
            return false;
        else if (template.hasTagCompound()) {
            if (!input.hasTagCompound())
                return false;
            
            for (String key : template.getTagCompound().getKeySet()) {
                if (!input.getTagCompound().hasKey(key))
                    return false;
                else if (!template.getTagCompound().getTag(key).equals(input.getTagCompound().getTag(key)))
                    return false;
            }
        }
        
        return true;
    }
    
    @Override
    public Collection<AugmentConfiguration> getAllConfigurationsForItem(ItemStack input) {
        for (ItemStack stack : configs.keySet()) {
            if (areStacksEqualEnough(stack, input))
                return ImmutableList.copyOf(configs.get(stack));
        }
        
        return ImmutableList.of();
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound configurations = new NBTTagCompound();
        int i = 0;
        for (HashSet<AugmentConfiguration> set : configs.values()) {
            for (AugmentConfiguration config : set) {
                configurations.setTag(Integer.toString(i), config.serializeNBT());
                ++i;
            }
        }
        
        tag.setTag("configs", configurations);
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagCompound configurations = nbt.getCompoundTag("configurations");
        if (configurations != null) {
            for (String key : configurations.getKeySet()) {
                NBTTagCompound c = nbt.getCompoundTag(key);
                if (c != null)
                    addConfiguration(new AugmentConfiguration(c));
            }
        }
    }
    
}
