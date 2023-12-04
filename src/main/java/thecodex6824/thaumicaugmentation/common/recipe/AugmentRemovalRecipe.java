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
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;

public class AugmentRemovalRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }
    
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack augmentable = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                    if (augmentable.isEmpty())
                        augmentable = stack;
                    else
                        return false;
                }
                else
                    return false;
            }
        }

        if (!augmentable.isEmpty()) {
            IAugmentableItem augmentableCap = augmentable.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
            int toRemove = -1;
            for (int i = augmentableCap.getUsedAugmentSlots() - 1; i >= 0; --i) {
                IAugment aug = augmentableCap.getAugment(i).getCapability(CapabilityAugment.AUGMENT, null);
                if (aug.shouldAllowDefaultRemoval()) {
                    toRemove = i;
                    break;
                }
            }
            
            return toRemove != -1;
        }
        
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack augmentable = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                    if (augmentable.isEmpty())
                        augmentable = stack;
                    else
                        return ItemStack.EMPTY;
                }
                else
                    return ItemStack.EMPTY;
            }
        }
        
        // already verified if recipe is allowed in matches, but still need to find augment
        IAugmentableItem cap = augmentable.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        int toRemove = -1;
        for (int i = cap.getUsedAugmentSlots() - 1; i >= 0; --i) {
            IAugment aug = cap.getAugment(i).getCapability(CapabilityAugment.AUGMENT, null);
            if (aug.shouldAllowDefaultRemoval()) {
                toRemove = i;
                break;
            }
        }
        
        if (toRemove != -1)
            return cap.getAugment(toRemove).copy();
        else
            return ItemStack.EMPTY;
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < ret.size(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (i < 9 && !stack.isEmpty() && stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                ItemStack copy = stack.copy();
                copy.setCount(Math.min(copy.getCount(), 1));
                IAugmentableItem cap = copy.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                cap.removeAugment(cap.getNextAvailableSlot() == -1 ? cap.getTotalAugmentSlots() - 1 : cap.getNextAvailableSlot() - 1);
                ret.set(i, copy);
                break;
            }
        }
        
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
