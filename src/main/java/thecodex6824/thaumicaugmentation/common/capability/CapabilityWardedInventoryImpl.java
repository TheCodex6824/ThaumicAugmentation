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

package thecodex6824.thaumicaugmentation.common.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import thecodex6824.thaumicaugmentation.api.warded.IWardedInventory;

public final class CapabilityWardedInventoryImpl {

    private CapabilityWardedInventoryImpl() {}
    
    public static void init() {
        CapabilityManager.INSTANCE.register(IWardedInventory.class, new Capability.IStorage<IWardedInventory>() {
            
            @Override
            public void readNBT(Capability<IWardedInventory> capability, IWardedInventory instance, EnumFacing side, NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            public NBTBase writeNBT(Capability<IWardedInventory> capability, IWardedInventory instance, EnumFacing side) {
                return instance.serializeNBT();
            }
            
        }, () -> new DefaultImpl(1));
    }
    
    public static class DefaultImpl implements IWardedInventory {
        
        private ItemStackHandler wrapped;
        
        public DefaultImpl(int slots) {
            wrapped = new ItemStackHandler(slots);
        }
        
        @Override
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
        public ItemStack getStackInSlot(int slot) {
            return wrapped.getStackInSlot(slot);
        }
        
        @Override
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
    
}
