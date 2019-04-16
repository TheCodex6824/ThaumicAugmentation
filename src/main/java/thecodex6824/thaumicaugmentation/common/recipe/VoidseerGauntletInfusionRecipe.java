/**
 *	Thaumic Augmentation
 *	Copyright (c) 2019 TheCodex6824.
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
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.items.ItemsTC;
import thecodex6824.thaumicaugmentation.api.TAItems;

public class VoidseerGauntletInfusionRecipe extends InfusionRecipe {

	public VoidseerGauntletInfusionRecipe() {
		super("GAUNTLET_VOID", new ItemStack(TAItems.GAUNTLET, 1, 1), 6, 
				new AspectList().add(Aspect.ENERGY, 25).add(Aspect.ELDRITCH, 50).add(Aspect.VOID, 50), 
				new ItemStack(TAItems.GAUNTLET, 1, 0), new Object[] {
						new ItemStack(ItemsTC.charmVoidseer), "plateVoid", "plateVoid", "plateVoid", "plateVoid", new ItemStack(ItemsTC.salisMundus)
				}
		);
	}
	
	@Override
	public Object getRecipeOutput(EntityPlayer player, ItemStack input, List<ItemStack> comps) {
		if (input != null) {
			ItemStack newGauntlet = input.copy();
			newGauntlet.setItemDamage(1);
			return newGauntlet;
		}
		
		return null;
	}
	
}
