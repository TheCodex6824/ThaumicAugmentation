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

package thecodex6824.thaumicaugmentation.common.util;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public final class MorphicArmorHelper {

    private MorphicArmorHelper() {}
    
    private static final NonNullList<ItemStack> EMPTY_ARMOR = NonNullList.withSize(4, ItemStack.EMPTY);
    
    public static NonNullList<ItemStack> getArmorInventory(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer)
            return ((EntityPlayer) entity).inventory.armorInventory;
        else if (entity instanceof EntityLiving)
            return ((EntityLiving) entity).inventoryArmor;
        else
            return EMPTY_ARMOR;
    }
    
}
