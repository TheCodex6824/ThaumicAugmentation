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

package thecodex6824.thaumicaugmentation.common.capability.provider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import baubles.api.cap.BaubleItem;
import baubles.api.cap.BaublesCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class CapabilityProviderAugmentFocusBag implements ICapabilitySerializable<NBTTagCompound> {

	private final ItemStackHandler inventory;
    private final BaubleItem bauble;
    
    public CapabilityProviderAugmentFocusBag(ItemStackHandler inv, BaubleItem b) {
        inventory = inv;
        bauble = b;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("inventory", inventory.serializeNBT());
        return tag;
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
        		capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
    }
    
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        else if (capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE) {
            return BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(bauble);
        }
        
        return null;
    }
    
}
