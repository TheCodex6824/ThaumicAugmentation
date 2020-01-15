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

package thecodex6824.thaumicaugmentation.api.recipe;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.IngredientNBT;

public class IngredientNBTCapabilities extends IngredientNBT {

    public static final IngredientNBTCapabilities EMPTY = new IngredientNBTCapabilities(ItemStack.EMPTY);
    
    protected ItemStack stack;
    
    public IngredientNBTCapabilities(ItemStack s) {
        super(s);
        stack = s;
    }
    
    @Override
    public boolean apply(@Nullable ItemStack input) {
        return super.apply(input) && stack.areCapsCompatible(input);
    }
    
    @Override
    public ItemStack[] getMatchingStacks() {
        return new ItemStack[] {stack};
    }
    
}
