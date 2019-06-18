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
import net.minecraftforge.registries.IForgeRegistryEntry;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;

public class AugmentAdditionRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
    
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack augmentable = ItemStack.EMPTY;
        ItemStack augment = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.hasCapability(CapabilityAugment.AUGMENT, null)) {
                    if (augment.isEmpty())
                        augment = stack;
                    else
                        return false;
                }
                else if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                    if (augmentable.isEmpty())
                        augmentable = stack;
                    else
                        return false;
                }
                else
                    return false;
            }
        }

        return !augmentable.isEmpty() && !augment.isEmpty() && augmentable.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).getNextAvailableSlot() != -1 &&
                augment.getCapability(CapabilityAugment.AUGMENT, null).canBeAppliedToItem(augmentable) &&
                augmentable.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).isAugmentAcceptable(
                augment, augmentable.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).getNextAvailableSlot());
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack augmentable = ItemStack.EMPTY;
        ItemStack augment = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.hasCapability(CapabilityAugment.AUGMENT, null)) {
                    if (augment.isEmpty())
                        augment = stack;
                    else
                        return ItemStack.EMPTY;
                }
                else if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                    if (augmentable.isEmpty())
                        augmentable = stack;
                    else
                        return ItemStack.EMPTY;
                }
                else
                    return ItemStack.EMPTY;
            }
        }
        
        ItemStack copy = augmentable.copy();
        IAugmentableItem cap = copy.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        cap.setAugment(augment.copy(), cap.getNextAvailableSlot());
        return copy;
    }
    
    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
    
}
