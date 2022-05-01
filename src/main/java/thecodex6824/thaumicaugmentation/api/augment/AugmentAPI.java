/*
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

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/*
 * Contains utility methods for working with augments.
 * @author TheCodex6824
 */
public final class AugmentAPI {

    private AugmentAPI() {}
    
    private static final HashMap<String, Function<Entity, Iterable<ItemStack>>> additionalItemSources = new HashMap<>();
    
    /*
     * Registers a callback that returns a source of ItemStacks to check for augmentable items on an entity.
     * @param key A unique identifier for this source
     * @param source The callback that returns the ItemStack instances to check for augmentable items
     */
    public static void addAugmentableItemSource(ResourceLocation key, Function<Entity, Iterable<ItemStack>> source) {
        additionalItemSources.put(key.toString(), source);
    }
    
    /*
     * Removes a previously registered augmentable item source.
     * @param key The unique identifier of the callback to remove
     * @return If a callback matching the key existed and was removed
     */
    public static boolean removeAugmentableItemSource(ResourceLocation key) {
        return additionalItemSources.remove(key.toString()) != null;
    }
    
    /*
     * Returns a collection of all augmentable item sources.
     * @return All augmentable item sources
     */
    public static Collection<Function<Entity, Iterable<ItemStack>>> getAugmentableItemSources() {
        return additionalItemSources.values();
    }
    
    public static AugmentConfigurationApplyResult tryApplyConfiguration(AugmentConfiguration config, IAugmentableItem target, boolean simulate) {
        for (Map.Entry<Integer, ItemStack> entry : config.getAugmentConfig().entrySet()) {
            if (entry.getKey() < 0 || entry.getKey() >= target.getTotalAugmentSlots())
                return AugmentConfigurationApplyResult.INVALID_SLOT;
            else if (!target.isAugmentAcceptable(entry.getValue(), entry.getKey()))
                return AugmentConfigurationApplyResult.INVALID_AUGMENT;
        }
        
        if (!simulate) {
            for (Map.Entry<Integer, ItemStack> entry : config.getAugmentConfig().entrySet())
                target.setAugment(entry.getValue(), entry.getKey());
        }
        
        return AugmentConfigurationApplyResult.OK;
    }
    
    public static AugmentConfiguration makeConfiguration(ItemStack stack) {
        IAugmentableItem item = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (item == null)
            throw new IllegalArgumentException();
        
        AugmentConfiguration config = new AugmentConfiguration(item.createConfigurationStack(stack));
        for (int i = 0; i < item.getTotalAugmentSlots(); ++i) {
            ItemStack augment = item.getAugment(i);
            if (!augment.isEmpty())
                config.setAugment(augment, i);
        }
        
        return config;
    }
    
}
