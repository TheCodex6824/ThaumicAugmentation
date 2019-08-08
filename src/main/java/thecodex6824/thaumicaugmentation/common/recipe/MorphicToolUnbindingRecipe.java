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
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;
import thecodex6824.thaumicaugmentation.common.item.ItemMorphicTool;

public class MorphicToolUnbindingRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }
    
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean itemFound = false;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof ItemMorphicTool) {
                    if (!itemFound)
                        itemFound = true;
                    else
                        return false;
                }
                else
                    return false;
            }
        }

        return itemFound;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack item = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof ItemMorphicTool) {
                    if (item.isEmpty())
                        item = stack;
                    else
                        return ItemStack.EMPTY;
                }
                else
                    return ItemStack.EMPTY;
            }
        }
        
        IMorphicTool tool = item.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null);
        if (!tool.getFunctionalStack().isEmpty())
            return tool.getFunctionalStack().copy();
        else
            return tool.getDisplayStack().copy();
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        ItemStack item = ItemStack.EMPTY;
        int itemIndex = 0;
        NonNullList<ItemStack> result = NonNullList.withSize(Math.min(inv.getSizeInventory(), 9), ItemStack.EMPTY);
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof ItemMorphicTool) {
                    if (item.isEmpty()) {
                        item = stack;
                        itemIndex = i;
                    }
                    else
                        return NonNullList.create();
                }
                else
                    return NonNullList.create();
            }
        }
        
        IMorphicTool tool = item.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null);
        if (!tool.getFunctionalStack().isEmpty())
            result.set(itemIndex, item.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getDisplayStack().copy());
        
        return result;
    }
    
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
    
}
