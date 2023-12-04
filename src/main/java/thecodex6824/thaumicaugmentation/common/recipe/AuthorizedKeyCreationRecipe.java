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
import thecodex6824.thaumicaugmentation.common.item.ItemKey;

public class AuthorizedKeyCreationRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean hasIronKey = false;
        boolean hasBrassKey = false;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof ItemKey) {
                    if (stack.getMetadata() == 0 && !hasIronKey) {
                        IWardAuthenticator key = stack.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null);
                        if (key instanceof WardAuthenticatorKey && !((WardAuthenticatorKey) key).hasOwner())
                            hasIronKey = true;
                    }
                    else if (stack.getMetadata() == 1 && !hasBrassKey) {
                        IWardAuthenticator key = stack.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null);
                        if (key instanceof WardAuthenticatorKey && ((WardAuthenticatorKey) key).hasOwner())
                            hasBrassKey = true;
                    }
                    else
                        return false;
                }
                else
                    return false;
            }
        }

        return hasIronKey && hasBrassKey;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack ironKey = ItemStack.EMPTY;
        ItemStack brassKey = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getItem() instanceof ItemKey) {
                    if (stack.getMetadata() == 0 && ironKey.isEmpty()) {
                        IWardAuthenticator key = stack.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null);
                        if (key instanceof WardAuthenticatorKey && !((WardAuthenticatorKey) key).hasOwner())
                            ironKey = stack;
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

        if (!ironKey.isEmpty() && !brassKey.isEmpty()) {
            ItemStack output = brassKey.copy();
            output.setCount(Math.min(output.getCount(), 1));
            output.setItemDamage(0);

            return output;
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
