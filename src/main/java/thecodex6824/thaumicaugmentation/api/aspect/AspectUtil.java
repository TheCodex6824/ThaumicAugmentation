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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.text.TextFormatting;
import thaumcraft.api.aspects.Aspect;

public final class AspectUtil {

    private AspectUtil() {}
    
    private static final HashMap<TextFormatting, Integer> CHAT_COLORS;
    
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
    
    public static String getChatColorForAspect(Aspect aspect) {
        return getChatColorForAspect(aspect, Collections.emptySet());
    }
    
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
    
}
