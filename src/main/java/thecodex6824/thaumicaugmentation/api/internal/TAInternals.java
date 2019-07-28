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

package thecodex6824.thaumicaugmentation.api.internal;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class TAInternals {

    private static IInternalMethodProvider provider;
    
    private TAInternals() {}
    
    public static void setInternalMethodProvider(IInternalMethodProvider provider) {
        TAInternals.provider = provider;
    }
    
    public static void addConfigListener(Runnable listener) {
        provider.addConfigListener(listener);
    }
    
    public static boolean removeConfigListener(Runnable listener) {
        return provider.removeConfigListener(listener);
    }
    
    public static ItemStack createCasterStrengthProviderStack(ResourceLocation id) {
        return provider.createCasterStrengthProviderStack(id);
    }
    
    public static String getCasterStrengthProviderID(ItemStack stack) {
        return provider.getCasterStrengthProviderID(stack);
    }
    
    public static ItemStack createCasterEffectProviderStack(ResourceLocation id) {
        return provider.createCasterEffectProviderStack(id);
    }
    
    public static String getCasterEffectProviderID(ItemStack stack) {
        return provider.getCasterEffectProviderID(stack);
    }
    
}
