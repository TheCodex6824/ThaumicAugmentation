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
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.IForgeRegistryEntry;
import thecodex6824.thaumicaugmentation.common.item.ItemKey;

public class ThaumiumKeyCopyRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 3;
    }

    protected boolean isThaumiumKeyValid(ItemStack key) {
        return key.hasTagCompound() && key.getTagCompound().hasKey("boundTo", NBT.TAG_STRING) && 
                key.getTagCompound().hasKey("boundType", NBT.TAG_STRING) &&
                key.getTagCompound().hasKey("boundBlockPos", NBT.TAG_INT_ARRAY) &&
                key.getTagCompound().getIntArray("boundBlockPos").length == 3;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean hasEmptyThaumiumKey = false;
        ItemStack thaumiumKey = null;
        ItemStack brassKey = null;
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof ItemKey) {
                    if (stack.getMetadata() == 2) {
                        if (!stack.hasTagCompound() && !hasEmptyThaumiumKey)
                            hasEmptyThaumiumKey = true;
                        else if (isThaumiumKeyValid(stack) && thaumiumKey == null)
                            thaumiumKey = stack;
                        else
                            return false;
                    }
                    else if (stack.getMetadata() == 1 && brassKey == null && stack.hasTagCompound() && 
                            stack.getTagCompound().hasKey("boundTo", NBT.TAG_STRING))
                        brassKey = stack;
                    else
                        return false;
                }
                else
                    return false;
            }
        }

        return hasEmptyThaumiumKey && thaumiumKey != null && brassKey != null && 
                thaumiumKey.getTagCompound().getString("boundTo").equals(brassKey.getTagCompound().getString("boundTo"));
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack emptyThaumiumKey = null;
        ItemStack thaumiumKey = null;
        ItemStack brassKey = null;
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof ItemKey) {
                    if (stack.getMetadata() == 2) {
                        if (!stack.hasTagCompound() && emptyThaumiumKey == null)
                            emptyThaumiumKey = stack;
                        else if (isThaumiumKeyValid(stack) && thaumiumKey == null)
                            thaumiumKey = stack;
                        else
                            return ItemStack.EMPTY;
                    }
                    else if (stack.getMetadata() == 1 && brassKey == null && stack.hasTagCompound() && 
                            stack.getTagCompound().hasKey("boundTo", NBT.TAG_STRING))
                        brassKey = stack;
                    else
                        return ItemStack.EMPTY;
                }
                else
                    return ItemStack.EMPTY;
            }
        }

        if (emptyThaumiumKey != null && thaumiumKey != null && brassKey != null && 
                thaumiumKey.getTagCompound().getString("boundTo").equals(brassKey.getTagCompound().getString("boundTo")))
            return thaumiumKey.copy();
        else
            return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

}
