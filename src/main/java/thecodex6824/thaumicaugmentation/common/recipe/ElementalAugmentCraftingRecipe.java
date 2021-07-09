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

import javax.annotation.Nonnull;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.items.ItemsTC;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.CasterAugmentBuilder;

public class ElementalAugmentCraftingRecipe extends ShapelessArcaneRecipe {

    public ElementalAugmentCraftingRecipe() {
        super(new ResourceLocation(""), "GAUNTLET_AUGMENTATION@2", 25,
                new AspectList().add(Aspect.AIR, 1).add(Aspect.EARTH, 1).add(Aspect.FIRE, 1).add(Aspect.ENTROPY, 1).add(
                Aspect.ORDER, 1).add(Aspect.WATER, 1), CasterAugmentBuilder.createStackForStrengthProvider(
                        new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_elemental")), new Object[] {
                        new ItemStack(ItemsTC.plate, 1, 2), ItemsTC.crystalEssence, ItemsTC.visResonator
                }
        );
    }
    
    @Override
    @Nonnull
    @SuppressWarnings("null")
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack crystal = ItemStack.EMPTY;
        for (int i = 0; i < Math.min(inv.getSizeInventory(), 9); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() == ItemsTC.crystalEssence) {
                crystal = stack;
                break;
            }
        }
        
        if (!crystal.isEmpty()) {
            ItemStack output = CasterAugmentBuilder.createStackForStrengthProvider(
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_elemental"));
            output.getTagCompound().setString("aspect", ((IEssentiaContainerItem) crystal.getItem()).getAspects(crystal).getAspects()[0].getTag());
            return output;
        }
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean isDynamic() {
        return true;
    }
    
}
