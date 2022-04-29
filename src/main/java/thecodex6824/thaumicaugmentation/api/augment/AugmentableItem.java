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

package thecodex6824.thaumicaugmentation.api.augment;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Arrays;

/*
* Default implementation of the Augmentable Item capability.
* @author TheCodex6824
* 
* @see thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem
*/
public class AugmentableItem implements IAugmentableItem, INBTSerializable<NBTTagCompound> {

    private ItemStack[] augments;
    
    public AugmentableItem(int slots) {
        augments = new ItemStack[slots];
        Arrays.fill(augments, 0, augments.length, ItemStack.EMPTY);
    }
    
    public AugmentableItem(ItemStack[] augs) {
        augments = Arrays.copyOf(augs, augs.length);
        for (int i = 0; i < augments.length; ++i) {
            if (augments[i] == null)
                augments[i] = ItemStack.EMPTY;
        }
    }
    
    @Override
    public void setAugment(ItemStack augment, int slot) {
        if (slot > -1 && slot < augments.length)
            augments[slot] = augment != null ? augment : ItemStack.EMPTY;
    }
    
    @Override
    public ItemStack[] setAllAugments(ItemStack[] augs) {
        ItemStack[] old = Arrays.copyOf(augments, augments.length);
        for (int i = 0; i < Math.min(augments.length, augs.length); ++i)
            augments[i] = augs[i] != null ? augs[i] : ItemStack.EMPTY;
        
        return old;
    }
    
    @Override
    public ItemStack[] getAllAugments() {
        ArrayList<ItemStack> stacks = new ArrayList<>(augments.length);
        for (ItemStack stack : augments) {
            if (!stack.isEmpty())
                stacks.add(stack);
        }
        
        return stacks.toArray(new ItemStack[stacks.size()]);
    }
    
    @Override
    public ItemStack getAugment(int slot) {
        if (slot > -1 && slot < augments.length)
            return augments[slot];
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public ItemStack removeAugment(int slot) {
        if (slot > -1 && slot < augments.length) {
            ItemStack old = augments[slot];
            augments[slot] = ItemStack.EMPTY;
            return old;
        }
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public int getUsedAugmentSlots() {
        for (int i = 0; i < augments.length; ++i) {
            if (augments[i].isEmpty())
                return i;
        }
        
        return getTotalAugmentSlots();
    }
    
    @Override
    public int getTotalAugmentSlots() {
        return augments.length;
    }
    
    @Override
    public boolean isAugmentAcceptable(ItemStack augment, int slot) {
        for (ItemStack aug : augments) {
            if (!aug.isEmpty() && !aug.getCapability(CapabilityAugment.AUGMENT, null).isCompatible(augment))
                return false;
        }
        
        return true;
    }
    
    @Override
    public int getNextAvailableSlot() {
        for (int i = 0; i < augments.length; ++i) {
            if (augments[i].isEmpty())
                return i;
        }
        
        return -1;
    }
    
    @Override
    public boolean isAugmented() {
        for (ItemStack stack : augments) {
            if (!stack.isEmpty())
                return true;
        }
        
        return false;
    }
    
    @Override
    public NBTTagCompound getSyncNBT() {
        return serializeNBT();
    }
    
    @Override
    public void readSyncNBT(NBTTagCompound tag) {
        deserializeNBT(tag);
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("slots", augments.length);
        for (int i = 0; i < augments.length; ++i) {
            ItemStack stack = augments[i];
            if (stack != null && !stack.isEmpty())
                data.setTag("slot" + i, stack.serializeNBT());
        }
        
        return data;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        int slots = nbt.getInteger("slots");
        if (slots < 256) {
            augments = new ItemStack[slots];
            for (int i = 0; i < augments.length; ++i) {
                if (nbt.hasKey("slot" + i, NBT.TAG_COMPOUND))
                    augments[i] = new ItemStack(nbt.getCompoundTag("slot" + i));
                else
                    augments[i] = ItemStack.EMPTY;
            }
        }
    }
    
}
