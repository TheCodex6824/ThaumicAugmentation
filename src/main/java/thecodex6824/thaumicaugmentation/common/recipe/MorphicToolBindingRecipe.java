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

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.items.ItemsTC;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;

public class MorphicToolBindingRecipe extends InfusionRecipe {

    public MorphicToolBindingRecipe() {
        super("MORPHIC_TOOL", ItemStack.EMPTY, 5, new AspectList().add(Aspect.VOID, 15),
                ItemStack.EMPTY, new Object[] {});
    }
    
    @Override
    @SuppressWarnings("null")
    public boolean matches(List<ItemStack> input, ItemStack central, World world, EntityPlayer player) {
        if (input.size() != 3 || central.getItem() == TAItems.MORPHIC_TOOL || central.getItem() == ItemsTC.primordialPearl ||
                central.getItem() instanceof ItemArmor || !ThaumcraftCapabilities.knowsResearch(player, research))
            return false;

        boolean morphicFound = false;
        boolean quicksilverFound = false;
        for (ItemStack stack : input) {
            if (stack.getItem() == ItemsTC.primordialPearl) {
                if (morphicFound)
                    return false;
                else
                    morphicFound = true;
            }
            else if (stack.getItem() == ItemsTC.quicksilver) {
                if (quicksilverFound)
                    return false;
                else
                    quicksilverFound = true;
            }
            
            // Thaumcraft tries to leave container items behind, which normally is correct but presents
            // an issue with morphic tools as it is possible to get the input items back
            // Without any checks it would allow a partial dupe, letting the player keep both the normal item and
            // container item, at the cost of doing the infusion.
            // There is a coremod mitigation for this, but if it's disabled just stop it from being a problem
            if (!ThaumicAugmentationAPI.isCoremodAvailable() && stack.getItem().hasContainerItem(stack))
                return false;
            else if (stack.getItem() instanceof ItemArmor)
                return false;
        }

        return morphicFound && quicksilverFound;
    }

    @Override
    public Object getRecipeOutput(EntityPlayer player, ItemStack input, List<ItemStack> comps) {
        ItemStack toReturn = new ItemStack(TAItems.MORPHIC_TOOL);
        toReturn.setCount(Math.min(toReturn.getCount(), 1));
        if (input.hasTagCompound())
            toReturn.setTagCompound(input.getTagCompound().copy());
        
        IMorphicTool tool = toReturn.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null);
        tool.setFunctionalStack(input);
        for (ItemStack stack : comps) {
            if (stack.getItem() != ItemsTC.primordialPearl && stack.getItem() != ItemsTC.quicksilver) {
                tool.setDisplayStack(stack);
                return toReturn;
            }
        }
        
        return toReturn;
    }
    
}
