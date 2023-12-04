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

import java.util.ArrayList;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipesArmorDyes;
import net.minecraft.world.World;
import net.minecraftforge.oredict.DyeUtils;
import thecodex6824.thaumicaugmentation.api.item.IDyeableItem;

public class DyeableItemRecipe extends RecipesArmorDyes {

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean hasDyedItem = false;
        boolean hasDye = false;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof IDyeableItem) {
                    if (hasDyedItem)
                        return false;
                    else
                        hasDyedItem = true;
                }
                else if (DyeUtils.isDye(stack))
                    hasDye = true;
                else
                    return false;
            }
        }

        return hasDyedItem && hasDye;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack toDye = ItemStack.EMPTY;
        ArrayList<ItemStack> dyes = new ArrayList<>();
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof IDyeableItem) {
                    if (!toDye.isEmpty())
                        return ItemStack.EMPTY;
                    else
                        toDye = stack;
                }
                else if (DyeUtils.isDye(stack))
                    dyes.add(stack);
                else
                    return ItemStack.EMPTY;
            }
        }

        if (!toDye.isEmpty() && dyes.size() > 0) {
            ItemStack toOutput = toDye.copy();
            toOutput.setCount(Math.min(toOutput.getCount(), 1));
            int oldColor = ((IDyeableItem) toOutput.getItem()).getDyedColor(toOutput);
            float[] totalComponents = new float[] {(oldColor >> 16 & 0xFF) / 255.0F, (oldColor >> 8 & 0xFF) / 255.0F, (oldColor & 0xFF) / 255.0F};
            float totalMaximum = Math.max(totalComponents[0], Math.max(totalComponents[1], totalComponents[2]));
            for (ItemStack d : dyes) {
                float[] dyeColor = DyeUtils.colorFromStack(d).get().getColorComponentValues();
                totalComponents[0] += dyeColor[0];
                totalComponents[1] += dyeColor[1];
                totalComponents[2] += dyeColor[2];
                totalMaximum += Math.max(dyeColor[0], Math.max(dyeColor[1], dyeColor[2]));
            }

            totalComponents[0] /= dyes.size() + 1;
            totalComponents[1] /= dyes.size() + 1;
            totalComponents[2] /= dyes.size() + 1;
            totalMaximum /= dyes.size() + 1;

            float gainFactor = totalMaximum / Math.max(totalComponents[0], Math.max(totalComponents[1], totalComponents[2]));
            totalComponents[0] *= gainFactor;
            totalComponents[1] *= gainFactor;
            totalComponents[2] *= gainFactor;
            int newColor = ((int) (totalComponents[0] * 255) << 16) + ((int) (totalComponents[1] * 255) << 8) + (int) (totalComponents[2] * 255);
            ((IDyeableItem) toOutput.getItem()).setDyedColor(toOutput, newColor);
            return toOutput;
        }

        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean isDynamic() {
        return true;
    }

}
