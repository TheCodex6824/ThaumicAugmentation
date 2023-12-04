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
import thecodex6824.thaumicaugmentation.api.item.CapabilityWardAuthenticator;
import thecodex6824.thaumicaugmentation.api.item.IWardAuthenticator;
import thecodex6824.thaumicaugmentation.common.capability.WardAuthenticatorKey;
import thecodex6824.thaumicaugmentation.common.capability.WardAuthenticatorThaumiumKey;
import thecodex6824.thaumicaugmentation.common.item.ItemKey;

public class ThaumiumKeyCopyRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 3;
    }

    protected boolean isThaumiumKeyValid(ItemStack key) {
        return key.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null) instanceof WardAuthenticatorThaumiumKey;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean hasEmptyThaumiumKey = false;
        ItemStack thaumiumKey = ItemStack.EMPTY;
        ItemStack brassKey = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof ItemKey) {
                    if (stack.getMetadata() == 2) {
                        if (!stack.hasTagCompound() && !hasEmptyThaumiumKey)
                            hasEmptyThaumiumKey = true;
                        else if (isThaumiumKeyValid(stack) && thaumiumKey.isEmpty())
                            thaumiumKey = stack;
                        else
                            return false;
                    }
                    else if (stack.getMetadata() == 1 && brassKey.isEmpty()) {
                        IWardAuthenticator key = stack.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null);
                        if (key instanceof WardAuthenticatorKey && ((WardAuthenticatorKey) key).hasOwner())
                            brassKey = stack;
                    }
                    else
                        return false;
                }
                else
                    return false;
            }
        }

        if (hasEmptyThaumiumKey && !thaumiumKey.isEmpty() && !brassKey.isEmpty()) {
            WardAuthenticatorKey brass = (WardAuthenticatorKey) brassKey.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null);
            WardAuthenticatorKey thaum = (WardAuthenticatorKey) thaumiumKey.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null);
            return brass.getOwner().equals(thaum.getOwner());
        }
        else
            return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack emptyThaumiumKey = ItemStack.EMPTY;
        ItemStack thaumiumKey = ItemStack.EMPTY;
        ItemStack brassKey = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof ItemKey) {
                    if (stack.getMetadata() == 2) {
                        if (!stack.hasTagCompound() && emptyThaumiumKey.isEmpty())
                            emptyThaumiumKey = stack;
                        else if (isThaumiumKeyValid(stack) && thaumiumKey.isEmpty())
                            thaumiumKey = stack;
                        else
                            return ItemStack.EMPTY;
                    }
                    else if (stack.getMetadata() == 1 && brassKey.isEmpty()) {
                        IWardAuthenticator key = stack.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null);
                        if (key instanceof WardAuthenticatorKey && ((WardAuthenticatorKey) key).hasOwner())
                            brassKey = stack;
                    }
                    else
                        return ItemStack.EMPTY;
                }
                else
                    return ItemStack.EMPTY;
            }
        }

        if (!emptyThaumiumKey.isEmpty() && !thaumiumKey.isEmpty() && !brassKey.isEmpty())
        {
        	ItemStack ret = thaumiumKey.copy();
        	ret.setCount(Math.min(ret.getCount(), 1));
            return ret;
        }
        else
            return ItemStack.EMPTY;
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
