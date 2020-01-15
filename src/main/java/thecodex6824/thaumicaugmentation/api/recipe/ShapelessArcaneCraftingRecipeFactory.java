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

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;

public class ShapelessArcaneCraftingRecipeFactory implements IRecipeFactory {

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        String group = JsonUtils.getString(json, "group", "");
        String research = JsonUtils.getString(json, "research");
        int vis = JsonUtils.getInt(json, "vis");
        
        AspectList aspects = new AspectList();
        JsonObject aspectJson = JsonUtils.getJsonObject(json, "aspects");
        for (Map.Entry<String, JsonElement> entry : aspectJson.entrySet()) {
            Aspect aspect = Aspect.getAspect(entry.getKey());
            if (aspect == null)
                throw new JsonSyntaxException("Invalid aspect: '" + entry.getKey() + "' is not a valid aspect name");
            else if (entry.getValue().isJsonNull())
                throw new JsonSyntaxException("Missing aspect count for aspect + '" + entry.getKey() + "'");
            
            aspects.add(aspect, entry.getValue().getAsInt());
        }
        
        NonNullList<Ingredient> ings = NonNullList.create();
        for (JsonElement ele : JsonUtils.getJsonArray(json, "ingredients"))
            ings.add(CraftingHelper.getIngredient(ele, context));

        if (ings.isEmpty())
            throw new JsonParseException("No ingredients for shapeless recipe");
        if (ings.size() > 9)
            throw new JsonParseException("Too many ingredients for shapeless recipe");

        ItemStack output = null;
        try {
            output = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
        }
        catch (JsonParseException ex) {
            try {
                output = CraftingHelper.getIngredient(JsonUtils.getJsonObject(json, "result"), context).getMatchingStacks()[0];
            }
            catch (JsonParseException oof) {
                throw new JsonParseException("Failed to parse recipe output as itemstack or ingredient");
            }
        }
        return new ShapelessArcaneRecipe(new ResourceLocation(group), research, vis, aspects, output, ings.toArray(new Ingredient[0]));
    }
    
}
