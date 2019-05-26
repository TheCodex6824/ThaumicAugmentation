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
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.items.ItemsTC;
import thecodex6824.thaumicaugmentation.api.TAItems;

public class RiftSeedFluxGrowthRecipe extends InfusionRecipe {

    private static final ItemStack ALLOWED_STACK = new ItemStack(TAItems.RIFT_SEED, 1, 0);

    public RiftSeedFluxGrowthRecipe() {
        super("RIFT_STUDIES", new ItemStack(TAItems.RIFT_SEED, 1, 0), 6, new AspectList().add(Aspect.FLUX, 25),
                new ItemStack(TAItems.RIFT_SEED, 1, 0), new Object[] {});
    }

    @Override
    public int getInstability(EntityPlayer player, ItemStack input, List<ItemStack> comps) {
        return comps.size();
    }

    @Override
    public boolean matches(List<ItemStack> input, ItemStack central, World world, EntityPlayer player) {
        if (input.size() > 9 || input.isEmpty())
            return false;

        if (!(central.isItemEqual(ALLOWED_STACK) && ThaumcraftCapabilities.knowsResearch(player, research) &&
                central.hasTagCompound() && central.getTagCompound().getInteger("riftSize") < 100 && 
                !central.getTagCompound().getBoolean("grown")))
            return false;

        for (ItemStack stack : input) {
            if (stack.getItem() != ItemsTC.voidSeed)
                return false;
        }

        return true;
    }

    @Override
    public AspectList getAspects(EntityPlayer player, ItemStack input, List<ItemStack> comps) {
        return new AspectList().add(Aspect.FLUX, comps.size() * 25);
    }

    @Override
    public Object getRecipeOutput(EntityPlayer player, ItemStack input, List<ItemStack> comps) {
        ItemStack toReturn = input.copy();
        toReturn.getTagCompound().setInteger("riftSize", comps.size() * 10 + 10);
        toReturn.getTagCompound().setBoolean("grown", true);
        return toReturn;
    }

}
