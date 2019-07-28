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

package thecodex6824.thaumicaugmentation.common.internal;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thecodex6824.thaumicaugmentation.api.internal.IInternalMethodProvider;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterEffectProvider;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterStrengthProvider;

public class InternalMethodProvider implements IInternalMethodProvider {
    
    @Override
    public void addConfigListener(Runnable listener) {
        TAConfigHolder.addListener(listener);
    }
    
    @Override
    public boolean removeConfigListener(Runnable listener) {
        return TAConfigHolder.removeListener(listener);
    }
    
    @Override
    public ItemStack createCasterStrengthProviderStack(ResourceLocation id) {
        return ItemCustomCasterStrengthProvider.create(id);
    }
    
    @Override
    public String getCasterStrengthProviderID(ItemStack stack) {
        return ItemCustomCasterStrengthProvider.getProviderIDString(stack);
    }
    
    @Override
    public ItemStack createCasterEffectProviderStack(ResourceLocation id) {
        return ItemCustomCasterEffectProvider.create(id);
    }
    
    @Override
    public String getCasterEffectProviderID(ItemStack stack) {
        return ItemCustomCasterEffectProvider.getProviderIDString(stack);
    }
    
}
