/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public final class ItemHelper {

    private ItemHelper() {}
    
    public static Entity makeItemEntity(World world, double x, double y, double z, ItemStack stack) {
        Entity item = new EntityItem(world, x, y, z, stack);
        if (stack.getItem().hasCustomEntity(stack)) {
            Entity newItem = stack.getItem().createEntity(world, item, stack);
            item = newItem != null ? newItem : item;
        }
        
        return item;
    }

    @Nullable
    public static <T> NBTTagCompound tryMakeCapabilityTag(ItemStack stack, Capability<T> cap) {
        T obj = stack.getCapability(cap, null);
        if (obj instanceof INBTSerializable<?>) {
            NBTBase tag = ((INBTSerializable<?>) obj).serializeNBT();
            if (tag instanceof NBTTagCompound)
                return (NBTTagCompound) tag;
        }

        return null;
    }
    
}
