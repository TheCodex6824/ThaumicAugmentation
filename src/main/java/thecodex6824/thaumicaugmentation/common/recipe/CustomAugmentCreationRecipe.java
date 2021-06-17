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
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.CasterAugmentBuilder;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.ICustomCasterAugment;

public class CustomAugmentCreationRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
    
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack strength = ItemStack.EMPTY;
        ItemStack effect = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (!stack.hasTagCompound())
                    return false;
                else if (CasterAugmentBuilder.doesStrengthProviderExist(new ResourceLocation(stack.getTagCompound().getString("id")))) {
                    if (strength.isEmpty())
                        strength = stack;
                    else
                        return false;
                }
                else if (CasterAugmentBuilder.doesEffectProviderExist(new ResourceLocation(stack.getTagCompound().getString("id")))) {
                    if (effect.isEmpty())
                        effect = stack;
                    else
                        return false;
                }
                else
                    return false;
            }
        }

        return !strength.isEmpty() && !effect.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack strength = ItemStack.EMPTY;
        ItemStack effect = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (!stack.hasTagCompound())
                    return ItemStack.EMPTY;
                else if (CasterAugmentBuilder.doesStrengthProviderExist(new ResourceLocation(stack.getTagCompound().getString("id")))) {
                    if (strength.isEmpty())
                        strength = stack;
                    else
                        return ItemStack.EMPTY;
                }
                else if (CasterAugmentBuilder.doesEffectProviderExist(new ResourceLocation(stack.getTagCompound().getString("id")))) {
                    if (effect.isEmpty())
                        effect = stack;
                    else
                        return ItemStack.EMPTY;
                }
                else
                    return ItemStack.EMPTY;
            }
        }
        
        ItemStack output = new ItemStack(TAItems.AUGMENT_CUSTOM);
        ICustomCasterAugment augment = (ICustomCasterAugment) output.getCapability(CapabilityAugment.AUGMENT, null);
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
