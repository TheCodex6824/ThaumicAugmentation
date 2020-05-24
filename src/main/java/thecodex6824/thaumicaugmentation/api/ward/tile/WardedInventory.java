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

package thecodex6824.thaumicaugmentation.api.ward.tile;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Default implementation of {@link IWardedInventory}.
 * @author TheCodex6824
 */
public class WardedInventory implements IWardedInventory, INBTSerializable<NBTTagCompound> {

    private ItemStackHandler wrapped;
    
    public WardedInventory(int slots) {
        wrapped = new ItemStackHandler(slots);
    }
    
    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return wrapped.extractItem(slot, amount, simulate);
    }
    
    @Override
    public int getSlotLimit(int slot) {
        return wrapped.getSlotLimit(slot);
    }
    
    @Override
    public int getSlots() {
        return wrapped.getSlots();
    }
    
    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return wrapped.getStackInSlot(slot);
    }
    
    @Override
    @SuppressWarnings("null")
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return wrapped.insertItem(slot, stack, simulate);
    }
    
    @Override
    public IItemHandler getItemHandler() {
        return wrapped;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        wrapped.deserializeNBT(nbt);
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        return wrapped.serializeNBT();
    }
    
}
