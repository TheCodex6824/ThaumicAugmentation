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

package thecodex6824.thaumicaugmentation.api.aspect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.text.TextFormatting;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ModConfig;

/**
 * Contains random utilities for working with Thaumcraft's aspects.
 * @author TheCodex6824
 */
public final class AspectUtil {

    private AspectUtil() {}
    
    private static final HashMap<TextFormatting, Integer> CHAT_COLORS;
    private static ArrayList<String> ASPECT_KEYS;
    private static Object2IntOpenHashMap<Aspect> ASPECT_TO_ID;
    
    static {
        CHAT_COLORS = new HashMap<>();
        CHAT_COLORS.put(TextFormatting.BLACK, 0x000000);
        CHAT_COLORS.put(TextFormatting.DARK_BLUE, 0x0000AA);
        CHAT_COLORS.put(TextFormatting.DARK_GREEN, 0x00AA00);
        CHAT_COLORS.put(TextFormatting.DARK_AQUA, 0x00AAAA);
        CHAT_COLORS.put(TextFormatting.DARK_RED, 0xAA0000);
        CHAT_COLORS.put(TextFormatting.DARK_PURPLE, 0xAA00AA);
        CHAT_COLORS.put(TextFormatting.GOLD, 0xFFAA00);
        CHAT_COLORS.put(TextFormatting.GRAY, 0xAAAAAA);
        CHAT_COLORS.put(TextFormatting.DARK_GRAY, 0x555555);
        CHAT_COLORS.put(TextFormatting.BLUE, 0x5555FF);
        CHAT_COLORS.put(TextFormatting.GREEN, 0x55FF55);
        CHAT_COLORS.put(TextFormatting.AQUA, 0x55FFFF);
        CHAT_COLORS.put(TextFormatting.RED, 0xFF5555);
        CHAT_COLORS.put(TextFormatting.LIGHT_PURPLE, 0xFF55FF);
        CHAT_COLORS.put(TextFormatting.YELLOW, 0xFFFF55);
        CHAT_COLORS.put(TextFormatting.WHITE, 0xFFFFFF);
    }
    
    /**
     * Returns the closest chat color for the given aspect. This will check if the aspect already has
     * an associated chat color, and return that color if it does. Otherwise, it will compute the closest chat color
     * based on the aspect's normal color.
     * @param aspect The aspect to get the color of
     * @return The closest chat color of the aspect
     */
    public static String getChatColorForAspect(Aspect aspect) {
        return getChatColorForAspect(aspect, Collections.emptySet());
    }
    
    /**
     * Returns the closest chat color for the given aspect, not including any colors passed. This will check if the aspect
     * already has an associated chat color, and return that color if it does. Otherwise, it will compute the closest chat color
     * based on the aspect's normal color.
     * @param aspect The aspect to get the color of
     * @param forbiddenColors The colors that are not allowed to be returned
     * @return The closest chat color of the aspect, not including any aspect passed in forbiddenColors
     */
    public static String getChatColorForAspect(Aspect aspect, Set<TextFormatting> forbiddenColors) {
        if (aspect.getChatcolor() != null)
            return '\u00a7' + aspect.getChatcolor();
        else {
            int smallestDistance = Integer.MAX_VALUE;
            String color = "";
            int aspectColor = aspect.getColor();
            int r = (aspectColor >> 16) & 0xFF, g = (aspectColor >> 8) & 0xFF, b = aspectColor & 0xFF;
            for (Map.Entry<TextFormatting, Integer> entry : CHAT_COLORS.entrySet()) {
                if (!forbiddenColors.contains(entry.getKey())) {
                    int number = entry.getValue();
                    int red = (number >> 16) & 0xFF, green = (number >> 8) & 0xFF, blue = number & 0xFF;
                    int distance = (int) (Math.pow(red - r, 2) + Math.pow(green - g, 2) + Math.pow(blue - b, 2));
                    if (distance < smallestDistance) {
                        smallestDistance = distance;
                        color = entry.getKey().toString();
                    }
                }
            }
            
            return color;
        }
    }
    
    public static Aspect getRandomAspect(Random rand) {
        if (ASPECT_KEYS == null)
            ASPECT_KEYS = new ArrayList<>(Aspect.aspects.keySet());
        
        if (!ASPECT_KEYS.isEmpty())
            return Aspect.getAspect(ASPECT_KEYS.get(rand.nextInt(ASPECT_KEYS.size())));
        else
            return Aspect.ORDER;
    }
    
    public static int getAspectID(Aspect aspect) {
        if (ASPECT_TO_ID == null) {
            ASPECT_TO_ID = new Object2IntOpenHashMap<>();
            ASPECT_TO_ID.defaultReturnValue(-1);
            for (int i = 0; i < ModConfig.aspectOrder.size(); ++i)
                ASPECT_TO_ID.put(ModConfig.aspectOrder.get(i), i);
        }
        
        return ASPECT_TO_ID.getInt(aspect);
    }
    
}
