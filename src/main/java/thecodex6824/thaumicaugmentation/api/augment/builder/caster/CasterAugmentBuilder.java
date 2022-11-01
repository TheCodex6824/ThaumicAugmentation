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

package thecodex6824.thaumicaugmentation.api.augment.builder.caster;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.internal.TAInternals;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;

public final class CasterAugmentBuilder {

    private CasterAugmentBuilder() {}
    
    private static class ProviderEntry<T> {
        
        public T provider;
        public Consumer<ItemStack> stack;
        
        public ProviderEntry(T p, Consumer<ItemStack> s) {
            provider = p;
            stack = s;
        }
        
    }
    
    private static final LinkedHashMap<ResourceLocation, ProviderEntry<IBuilderCasterStrengthProvider>> STRENGTH = new LinkedHashMap<>();
    private static final LinkedHashMap<ResourceLocation, ProviderEntry<IBuilderCasterEffectProvider>> EFFECT = new LinkedHashMap<>();
    
    public static final IBuilderCasterStrengthProvider NULL_STRENGTH = new IBuilderCasterStrengthProvider() {
        @Override
        public double calculateStrength(ICustomCasterAugment augment, FocusWrapper focus, Entity user) {
            ThaumicAugmentation.getLogger().warn("A null strength provider was invoked! This is probably a bug.\n{}", Arrays.toString(Thread.currentThread().getStackTrace()));
            return 1.0;
        }
        
        @Override
        public int calculateTintColor(ICustomCasterAugment augment) {
            ThaumicAugmentation.getLogger().warn("A null strength provider was invoked! This is probably a bug.\n{}", Arrays.toString(Thread.currentThread().getStackTrace()));
            return 0xFFFFFFFF;
        }
    };
    public static final IBuilderCasterEffectProvider NULL_EFFECT = new IBuilderCasterEffectProvider() {
        @Override
        public void apply(ICustomCasterAugment augment, Entity caster, ItemStack casterStack, FocusWrapper focus, double strength) {
            ThaumicAugmentation.getLogger().warn("A null effect provider was invoked! This is probably a bug.\n{}", Arrays.toString(Thread.currentThread().getStackTrace()));
        }
    };
    
    public static void registerStrengthProvider(ResourceLocation id, IBuilderCasterStrengthProvider impl) {
        registerStrengthProvider(id, impl, (stack) -> {});
    }
    
    public static void registerStrengthProvider(ResourceLocation id, IBuilderCasterStrengthProvider impl, Consumer<ItemStack> create) {
        STRENGTH.put(id, new ProviderEntry<>(impl, create));
    }
    
    public static void registerEffectProvider(ResourceLocation id, IBuilderCasterEffectProvider impl) {
        registerEffectProvider(id, impl, (stack) -> {});
    }
    
    public static void registerEffectProvider(ResourceLocation id, IBuilderCasterEffectProvider impl, Consumer<ItemStack> create) {
        EFFECT.put(id, new ProviderEntry<>(impl, create));
    }
    
    public static boolean doesStrengthProviderExist(ResourceLocation id) {
        return STRENGTH.containsKey(id);
    }
    
    public static boolean doesEffectProviderExist(ResourceLocation id) {
        return EFFECT.containsKey(id);
    }
    
    public static ItemStack createStackForStrengthProvider(ResourceLocation id) {
        ItemStack stack = TAInternals.createCasterStrengthProviderStack(id);
        ProviderEntry<IBuilderCasterStrengthProvider> s = STRENGTH.get(id);
        if (s != null)
            s.stack.accept(stack);
        
        return stack;
    }
    
    public static ResourceLocation getStrengthProviderID(ItemStack stack) {
        return new ResourceLocation(getStrengthProviderIDString(stack));
    }
    
    public static String getStrengthProviderIDString(ItemStack stack) {
        return TAInternals.getCasterStrengthProviderID(stack);
    }
    
    public static ItemStack createStackForEffectProvider(ResourceLocation id) {
        ItemStack stack = TAInternals.createCasterEffectProviderStack(id);
        ProviderEntry<IBuilderCasterEffectProvider> e = EFFECT.get(id);
        if (e != null)
            e.stack.accept(stack);
        
        return stack;
    }
    
    public static ResourceLocation getEffectProviderID(ItemStack stack) {
        return new ResourceLocation(getEffectProviderIDString(stack));
    }
    
    public static String getEffectProviderIDString(ItemStack stack) {
        return TAInternals.getCasterEffectProviderID(stack);
    }
    
    public static IBuilderCasterStrengthProvider getStrengthProvider(ResourceLocation id) {
        ProviderEntry<IBuilderCasterStrengthProvider> s = STRENGTH.get(id);
        if (s != null)
            return s.provider;
        else
            return NULL_STRENGTH;
    }
    
    public static IBuilderCasterEffectProvider getEffectProvider(ResourceLocation id) {
        ProviderEntry<IBuilderCasterEffectProvider> e = EFFECT.get(id);
        if (e != null)
            return e.provider;
        else
            return NULL_EFFECT;
    }
    
    public static Set<ResourceLocation> getAllStrengthProviders() {
        return STRENGTH.keySet();
    }
    
    public static Set<ResourceLocation> getAllEffectProviders() {
        return EFFECT.keySet();
    }
    
}
