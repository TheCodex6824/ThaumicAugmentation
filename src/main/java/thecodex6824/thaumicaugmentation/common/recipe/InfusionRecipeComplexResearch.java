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
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;
import net.minecraftforge.common.util.RecipeMatcher;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.crafting.InfusionRecipe;

public class InfusionRecipeComplexResearch extends InfusionRecipe {

    public InfusionRecipeComplexResearch(String research, Object outputResult, int inst, AspectList aspects, Object centralItem, Object... recipe) {
        super(research, outputResult, inst, aspects, centralItem, recipe);
    }
    
    @Override
    @SuppressWarnings("null")
    public boolean matches(List<ItemStack> input, ItemStack central, World world, EntityPlayer player) {
        if (getRecipeInput() == null || !ThaumcraftCapabilities.knowsResearchStrict(player, research))
            return false;
        
        return (getRecipeInput() == Ingredient.EMPTY || getRecipeInput().apply(central)) &&
                RecipeMatcher.findMatches(input, getComponents()) != null;
    }
    
}
