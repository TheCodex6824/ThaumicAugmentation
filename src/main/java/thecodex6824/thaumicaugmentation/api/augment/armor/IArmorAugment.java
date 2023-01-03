/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.api.augment.armor;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;

public interface IArmorAugment extends IAugment {

    /*
     * Called when the augmentable armor item has ISpecialArmor#getProperties called.
     * Note that this requires the armor item to fire this hook.
     * @param wearer The entity wearing the armor
     * @param worn The stack of the augmentable item worn
     * @param source The source of the damage
     * @param input The default ArmorProperties the armor is using
     * @return The ArmorProperties to use
     */
    default ArmorProperties onArmorCalc(Entity wearer, ItemStack worn, DamageSource source, ArmorProperties input) {
        return input;
    }
    
    /*
     * Called when the augmentable armor item has ISpecialArmor#getArmorDisplay called.
     * Note that this requires the armor item to fire this hook, and this value is added
     * to whatever vanilla would normally show for the armor.
     * @param wearer The entity wearing the armor
     * @param worn The stack of the augmentable item worn
     * @param input The default display value the armor is using
     * @return The armor value to use
     */
    default int onArmorDisplay(Entity wearer, ItemStack worn, int input) {
        return input;
    }
    
    @Override
    default boolean canBeAppliedToItem(ItemStack augmentable) {
        return augmentable.getItem() instanceof ItemArmor;
    }
    
}
