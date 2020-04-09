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

package thecodex6824.thaumicaugmentation.api;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.util.EnumHelper;

/**
 * Holds materials for Thaumic Augmentation. Materials are used for things like blocks and armor.
 * @author TheCodex6824
 */
public final class TAMaterials {

    private TAMaterials() {}
    
    public static final ArmorMaterial VOID_BOOTS = EnumHelper.addArmorMaterial(ThaumicAugmentationAPI.MODID + ":VOID_BOOTS", 
            ThaumicAugmentationAPI.MODID + ":void_boots", 18, new int[] {4, 7, 9, 3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2.0F);

    public static final IRarity RARITY_MAGICAL = new IRarity() {
        @Override
        public String getName() {
            return "Magical";
        }
        
        @Override
        public TextFormatting getColor() {
            return TextFormatting.YELLOW;
        }
    };
    
    public static final IRarity RARITY_ARCANE = new IRarity() {
        @Override
        public String getName() {
            return "Arcane";
        }
        
        @Override
        public TextFormatting getColor() {
            return TextFormatting.AQUA;
        }
    };
    
    public static final IRarity RARITY_ELDRITCH = new IRarity() {
        @Override
        public String getName() {
            return "Eldritch";
        }
        
        @Override
        public TextFormatting getColor() {
            return TextFormatting.LIGHT_PURPLE;
        }
    };
    
}
