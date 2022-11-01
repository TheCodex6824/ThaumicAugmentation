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

package thecodex6824.thaumicaugmentation.api.entity;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.item.ItemStack;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.common.items.casters.ItemFocus;

public final class AutocasterFocusRegistry {

    private AutocasterFocusRegistry() {}
    
    private static final Object2DoubleOpenHashMap<String> DISTANCES = new Object2DoubleOpenHashMap<>();
    
    public static void registerMaxDistance(String focusKey, double max) {
        DISTANCES.put(focusKey, max);
    }
    
    public static double getMaxDistance(String focusKey) {
        return DISTANCES.getOrDefault(focusKey, Double.MAX_VALUE);
    }
    
    public static double getMaxDistance(ItemStack focusStack) {
        if (focusStack.getItem() instanceof ItemFocus) {
            FocusPackage f = ItemFocus.getPackage(focusStack);
            if (f != null) {
                double min = Double.MAX_VALUE;
                for (IFocusElement element : f.nodes) {
                    double dist = getMaxDistance(element.getKey());
                    if (dist < min)
                        min = dist;
                }
                
                return min;
            }
        }
        
        return 0;
    }
    
}
