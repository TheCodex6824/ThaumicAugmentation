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

package thecodex6824.thaumicaugmentation.common.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.impl.custom.CustomAugmentBuilder;
import thecodex6824.thaumicaugmentation.api.augment.impl.custom.IBuilderEffectProvider;
import thecodex6824.thaumicaugmentation.api.augment.impl.custom.IBuilderStrengthProvider;
import thecodex6824.thaumicaugmentation.api.augment.impl.custom.ICustomAugment;

public class CustomAugmentCreationRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
    
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack strength = ItemStack.EMPTY;
        IBuilderStrengthProvider strengthProvider = null;
        ItemStack effect = ItemStack.EMPTY;
        IBuilderEffectProvider effectProvider = null;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.hasTagCompound()) {
                    assert stack.getTagCompound() != null;
                    ResourceLocation loc = new ResourceLocation(stack.getTagCompound().getString("id"));
                    if (CustomAugmentBuilder.doesStrengthProviderExist(loc)) {
                        if (strength.isEmpty()) {
                            strength = stack;
                            strengthProvider = CustomAugmentBuilder.getStrengthProvider(loc);
                            continue;
                        }
                    } else if (CustomAugmentBuilder.doesEffectProviderExist(loc)) {
                        if (effect.isEmpty()) {
                            effect = stack;
                            effectProvider = CustomAugmentBuilder.getEffectProvider(loc);
                            continue;
                        }
                    }
                }
                return false;
            }
        }
        return !strength.isEmpty() && !effect.isEmpty() && effectProvider.compatibleWith(strengthProvider);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack strength = ItemStack.EMPTY;
        IBuilderStrengthProvider strengthProvider = null;
        ItemStack effect = ItemStack.EMPTY;
        IBuilderEffectProvider effectProvider = null;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.hasTagCompound()) {
                    assert stack.getTagCompound() != null;
                    ResourceLocation loc = new ResourceLocation(stack.getTagCompound().getString("id"));
                    if (CustomAugmentBuilder.doesStrengthProviderExist(loc)) {
                        if (strength.isEmpty()) {
                            strength = stack;
                            strengthProvider = CustomAugmentBuilder.getStrengthProvider(loc);
                            continue;
                        }
                    } else if (CustomAugmentBuilder.doesEffectProviderExist(loc)) {
                        if (effect.isEmpty()) {
                            effect = stack;
                            effectProvider = CustomAugmentBuilder.getEffectProvider(loc);
                            continue;
                        }
                    }
                }
                return ItemStack.EMPTY;
            }
        }
        if (strengthProvider == null || effectProvider == null ||
                !effectProvider.compatibleWith(strengthProvider)) return ItemStack.EMPTY;
        ItemStack output = new ItemStack(TAItems.AUGMENT_CUSTOM);
        ICustomAugment augment = (ICustomAugment) output.getCapability(CapabilityAugment.AUGMENT, null);
        augment.setStrengthProvider(strength);
        augment.setEffectProvider(effect);
        return output;
    }
    
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean isDynamic() {
        return true;
    }
    
}
