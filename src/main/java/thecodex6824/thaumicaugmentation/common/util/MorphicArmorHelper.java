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

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/*
 * This is intentionally implemented as raw NBT tags rather than a capability.
 * Forge never really addressed how to sync ItemStack capabilities properly.
 * They say to use the share tag on the item, but you can't exactly do that if it's not your item.
 * Go ahead and search "capability sync" on the Forge github if you want to see a graveyard of PRs and
 * issues closed by their stale bot that were for addressing this. Would have been nice to do this the
 * right way, but it just isn't feasible to independently keep track of every itemstack and its capabilities.
 */
public final class MorphicArmorHelper {

    private MorphicArmorHelper() {}
    
    private static final List<ItemStack> EMPTY_ARMOR = ImmutableList.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    
    public static final String MORPHIC_ARMOR_KEY = "ta_morphic_armor";
    
    public static List<ItemStack> getArmorInventory(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer)
            return ((EntityPlayer) entity).inventory.armorInventory;
        else if (entity instanceof EntityLiving)
            return ((EntityLiving) entity).inventoryArmor;
        else
            return EMPTY_ARMOR;
    }
    
    // unlike capabilities, this is actually worth using regularly
    // if false, the itemstack won't be deserialized from nbt and then thrown away
    public static boolean hasMorphicArmor(ItemStack stack) {
        return stack.getSubCompound(MORPHIC_ARMOR_KEY) != null;
    }
    
    public static ItemStack getMorphicArmor(ItemStack stack) {
        NBTTagCompound item = stack.getSubCompound(MORPHIC_ARMOR_KEY);
        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }
    
    public static void setMorphicArmor(ItemStack stack, ItemStack display) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        if (display.isEmpty())
            stack.getTagCompound().removeTag(MORPHIC_ARMOR_KEY);
        else
            stack.getTagCompound().setTag(MORPHIC_ARMOR_KEY, display.serializeNBT());
    }
    
}
