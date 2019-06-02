/**
 *  Thaumic Augmentation
 *  Copyright (c) 2019 TheCodex6824.
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
import java.util.HashMap;
import java.util.function.Function;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class AugmentAPI {

    private AugmentAPI() {}
    
    private static HashMap<String, Function<Entity, Iterable<ItemStack>>> additionalItemSources = new HashMap<>();
    
    public static void addAugmentItemSource(ResourceLocation key, Function<Entity, Iterable<ItemStack>> source) {
        additionalItemSources.put(key.toString(), source);
    }
    
    public static boolean removeAugmentItemSource(ResourceLocation key) {
        return additionalItemSources.remove(key.toString()) != null;
    }
    
    public static Collection<Function<Entity, Iterable<ItemStack>>> getAugmentItemSources() {
        return additionalItemSources.values();
    }
    
}
