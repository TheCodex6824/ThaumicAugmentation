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

package thecodex6824.thaumicaugmentation.api.augment;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IAugmentableItem extends INBTSerializable<NBTTagCompound> {

    public int getUsedAugmentSlots();
    
    public int getTotalAugmentSlots();
    
    public boolean isAugmentAcceptable(ItemStack augment, int slot);
    
    public void setAugment(ItemStack augment, int slot);
    
    public ItemStack getAugment(int slot);
    
    public ItemStack[] getAllAugments();
    
    public ItemStack[] setAllAugments(ItemStack[] augs);
    
    public ItemStack removeAugment(int slot);
    
    public int getNextAvailableSlot();
    
    public boolean isAugmented();
    
}
