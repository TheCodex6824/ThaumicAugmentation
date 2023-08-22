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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.oredict.OreDictionary;

public class AugmentConfigurationStorage implements IAugmentConfigurationStorageSerializable {

    protected Object2ObjectOpenCustomHashMap<ItemStack, ArrayList<AugmentConfiguration>> configs;
    
    public AugmentConfigurationStorage() {
        configs = new Object2ObjectOpenCustomHashMap<>(new Strategy<ItemStack>() {
            @Override
            public boolean equals(ItemStack a, ItemStack b) {
                if (a == null && b == null)
                    return true;
                else if (a != null && b == null || a == null && b != null)
                    return false;
                else if (a.getItem() != b.getItem())
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
    
    protected ItemStack createConfigurationStack(ItemStack input) {
    	IAugmentableItem augmentable = input.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
    	if (augmentable == null) {
    		throw new IllegalArgumentException("Stack passed to AugmentConfigurationStorage is not augmentable");
    	}
    	
    	return augmentable.createConfigurationStack(input);
    }
    
    @Override
    public boolean addConfiguration(AugmentConfiguration config) {
        boolean ret = configs.computeIfAbsent(config.getConfigurationItemStack(),
                s -> new ArrayList<>()).add(config);
        return ret;
    }
    
    @Override
    public boolean removeConfiguration(AugmentConfiguration config) {
        return configs.computeIfAbsent(config.getConfigurationItemStack(),
                s -> new ArrayList<>()).remove(config);
    }
    
    @Override
    public List<AugmentConfiguration> getAllConfigurationsForItem(ItemStack input) {
    	return configs.getOrDefault(createConfigurationStack(input), new ArrayList<>());
    }
    
    @Override
    public List<AugmentConfiguration> removeAllConfigurationsForItem(ItemStack input) {
    	return configs.remove(createConfigurationStack(input));
    }
    
    @Override
    public NBTTagCompound serializeConfigsForSingleItem(ItemStack input) {
    	input = createConfigurationStack(input);
    	NBTTagCompound config = new NBTTagCompound();
    	if (configs.containsKey(input)) {
    		config.setTag("owner", input.serializeNBT());
        	NBTTagList configsList = new NBTTagList();
        	for (AugmentConfiguration c : configs.get(input)) {
        		configsList.appendTag(c.serializeNBT());
            }
        	
        	config.setTag("configs", configsList);
    	}
    	
    	return config;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList configurations = new NBTTagList();
        for (ItemStack owner : configs.keySet()) {
        	NBTTagCompound config = serializeConfigsForSingleItem(owner);
        	if (!config.isEmpty()) {
        		configurations.appendTag(config);
        	}
        }
        
        tag.setTag("configurations", configurations);
        return tag;
    }
    
    @Override
    public void deserializeConfigsForSingleItem(NBTTagCompound configsTag) {
    	NBTTagCompound ownerTag = configsTag.getCompoundTag("owner");
		if (!ownerTag.isEmpty()) {
			ItemStack owner = new ItemStack(ownerTag);
			configs.remove(owner);
			NBTTagList configList = configsTag.getTagList("configs", NBT.TAG_COMPOUND);
			ArrayList<AugmentConfiguration> loadedConfigs = new ArrayList<>();
			for (NBTBase configUncasted : configList) {
				NBTTagCompound config = (NBTTagCompound) configUncasted;
				loadedConfigs.add(new AugmentConfiguration(owner, config));
			}
			
			configs.put(owner, loadedConfigs);
		}
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("configurations", NBT.TAG_LIST)) {
            NBTTagList configurations = nbt.getTagList("configurations", NBT.TAG_COMPOUND);
        	for (NBTBase configsListUncasted : configurations) {
        		NBTTagCompound configsList = (NBTTagCompound) configsListUncasted;
        		NBTTagCompound ownerTag = configsList.getCompoundTag("owner");
        		if (!ownerTag.isEmpty()) {
        			ItemStack owner = new ItemStack(ownerTag);
        			NBTTagList configList = configsList.getTagList("configs", NBT.TAG_COMPOUND);
        			ArrayList<AugmentConfiguration> loadedConfigs = new ArrayList<>();
        			for (NBTBase configUncasted : configList) {
        				NBTTagCompound config = (NBTTagCompound) configUncasted;
        				loadedConfigs.add(new AugmentConfiguration(owner, config));
        			}
        			
        			configs.put(owner, loadedConfigs);
        		}
        	}
        }
    }
    
}
