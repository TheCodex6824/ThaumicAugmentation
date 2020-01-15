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

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.CasterAugmentBuilder;

public class EffectProviderIngredientFactory implements IIngredientFactory {

    @Nonnull
    @Override
    @SuppressWarnings("null")
    public Ingredient parse(JsonContext context, JsonObject json) {
        IngredientNBTCapabilities i = new IngredientNBTCapabilities(CasterAugmentBuilder.createStackForEffectProvider(
                new ResourceLocation(JsonUtils.getString(json, "id"))));
        i = i != null ? i : IngredientNBTCapabilities.EMPTY;
        return i;
    }
    
}
