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
import thaumcraft.api.items.ItemsTC;
import thecodex6824.thaumicaugmentation.api.item.CapabilityBiomeSelector;
import thecodex6824.thaumicaugmentation.api.item.IBiomeSelector;

public class BiomeSelectorSpecialResetRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
    
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean pearl = false;
        boolean biome = false;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                IBiomeSelector b = stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null);
                if (b != null && b.getBiomeID().equals(IBiomeSelector.EMPTY)) {
                    if (!biome)
                        biome = true;
                    else
                        return false;
                }
                else if (stack.getItem() == ItemsTC.primordialPearl) {
                    if (!pearl)
                        pearl = true;
                    else
                        return false;
                }
                else
                    return false;
            }
        }

        return pearl && biome;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        boolean pearl = false;
        ItemStack biome = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                IBiomeSelector b = stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null);
                if (b != null && b.getBiomeID().equals(IBiomeSelector.EMPTY)) {
                    if (biome.isEmpty())
                        biome = stack;
                    else
                        return ItemStack.EMPTY;
                }
                else if (stack.getItem() == ItemsTC.primordialPearl) {
                    if (!pearl)
                        pearl = true;
                    else
                        return ItemStack.EMPTY;
                }
                else
                    return ItemStack.EMPTY;
            }
        }

        if (pearl && !biome.isEmpty()) {
            ItemStack output = biome.copy();
            output.setCount(Math.min(output.getCount(), 1));
            output.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null).setBiomeID(IBiomeSelector.RESET);
            return output;
        }
        else
            return ItemStack.EMPTY;
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> ret = IRecipe.super.getRemainingItems(inv);
        boolean foundPearl = false;
        for (ItemStack s : ret) {
            if (s.getItem() == ItemsTC.primordialPearl) {
                s.setItemDamage(s.getItemDamage() - 1);
                foundPearl = true;
                break;
            }
        }
        
        if (!foundPearl)
            ret.set(ret.indexOf(ItemStack.EMPTY), new ItemStack(ItemsTC.primordialPearl, 1, 7));
        
        return ret;
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
