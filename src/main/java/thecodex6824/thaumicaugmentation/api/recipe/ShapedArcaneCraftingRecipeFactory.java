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
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapedArcaneRecipe;

public class ShapedArcaneCraftingRecipeFactory implements IRecipeFactory {

    @Override
    @SuppressWarnings("null")
    public IRecipe parse(JsonContext context, JsonObject json) {
        String group = JsonUtils.getString(json, "group", "");
        String research = JsonUtils.getString(json, "research");
        int vis = JsonUtils.getInt(json, "vis");
        
        AspectList aspects = new AspectList();
        JsonObject aspectJson = JsonUtils.getJsonObject(json, "aspects", new JsonObject());
        for (Map.Entry<String, JsonElement> entry : aspectJson.entrySet()) {
            Aspect aspect = Aspect.getAspect(entry.getKey());
            if (aspect == null)
                throw new JsonSyntaxException("Invalid aspect: '" + entry.getKey() + "' is not a valid aspect name");
            else if (entry.getValue().isJsonNull())
                throw new JsonSyntaxException("Missing aspect count for aspect + '" + entry.getKey() + "'");
            
            aspects.add(aspect, entry.getValue().getAsInt());
        }
        
        Char2ObjectOpenHashMap<Ingredient> ing = new Char2ObjectOpenHashMap<>();
        for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(json, "key").entrySet()) {
            if (entry.getKey().length() != 1)
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            if (" ".equals(entry.getKey()))
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

            ing.put(entry.getKey().toCharArray()[0], CraftingHelper.getIngredient(entry.getValue(), context));
        }
        ing.put(' ', Ingredient.EMPTY);

        JsonArray patternJ = JsonUtils.getJsonArray(json, "pattern");

        if (patternJ.size() == 0)
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        if (patternJ.size() > 3)
            throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");

        String[] pattern = new String[patternJ.size()];
        for (int x = 0; x < pattern.length; ++x) {
            String line = JsonUtils.getString(patternJ.get(x), "pattern[" + x + "]");
            if (line.length() > 3)
                throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
            if (x > 0 && pattern[0].length() != line.length())
                throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            
            pattern[x] = line;
        }

        NonNullList<Ingredient> input = NonNullList.withSize(pattern[0].length() * pattern.length, Ingredient.EMPTY);
        Set<Character> keys = Sets.newHashSet(ing.keySet());
        keys.remove(' ');

        int x = 0;
        for (String line : pattern) {
            for (char chr : line.toCharArray()) {
                Ingredient i = ing.get(chr);
                if (i == null)
                    throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
                
                input.set(x++, i);
                keys.remove(chr);
            }
        }

        if (!keys.isEmpty())
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

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
        
        ShapedPrimer primer = new ShapedPrimer();
        primer.width = pattern[0].length();
        primer.height = pattern.length;
        primer.input = input;
        return new ShapedArcaneRecipe(new ResourceLocation(group), research, vis, aspects, output, primer);
    }
    
}
