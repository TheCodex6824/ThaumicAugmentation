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
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.items.ItemsTC;
import thecodex6824.thaumicaugmentation.api.item.IAssociatedAspect;

public class ElementChangeRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
    
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack crystal = ItemStack.EMPTY;
        ItemStack augment = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof IAssociatedAspect) {
                    if (augment.isEmpty())
                        augment = stack;
                    else
                        return false;
                }
                else if (stack.getItem() == ItemsTC.crystalEssence) {
                    if (crystal.isEmpty())
                        crystal = stack;
                    else
                        return false;
                }
                else
                    return false;
            }
        }

        return !crystal.isEmpty() && !augment.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack crystal = ItemStack.EMPTY;
        ItemStack augment = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof IAssociatedAspect) {
                    if (augment.isEmpty())
                        augment = stack;
                    else
                        return ItemStack.EMPTY;
                }
                else if (stack.getItem() == ItemsTC.crystalEssence) {
                    if (crystal.isEmpty())
                        crystal = stack;
                    else
                        return ItemStack.EMPTY;
                }
                else
                    return ItemStack.EMPTY;
            }
        }
        
        ItemStack newAugment = augment.copy();
        ((IAssociatedAspect) newAugment.getItem()).setAspect(newAugment, ((IEssentiaContainerItem) crystal.getItem()).getAspects(crystal).getAspects()[0]);
        return newAugment;
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        ItemStack augment = ItemStack.EMPTY;
        int crystalIndex = 0;
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (i < 9 && stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof IAssociatedAspect)
                    augment = stack;
                else if (stack.getItem() == ItemsTC.crystalEssence)
                    crystalIndex = i;
            }
        }
        
        Aspect oldAspect = ((IAssociatedAspect) augment.getItem()).getAspect(augment);
        ret.set(crystalIndex, ThaumcraftApiHelper.makeCrystal(oldAspect));
        return ret;
    }
    
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
    
}
