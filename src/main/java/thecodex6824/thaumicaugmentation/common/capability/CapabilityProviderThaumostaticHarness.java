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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import baubles.api.cap.BaubleItem;
import baubles.api.cap.BaublesCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import thecodex6824.thaumicaugmentation.api.augment.AugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;

public class CapabilityProviderThaumostaticHarness implements ICapabilitySerializable<NBTTagCompound> {

    private AugmentableItem augmentable;
    private BaubleItem bauble;
    
    public CapabilityProviderThaumostaticHarness(AugmentableItem a, BaubleItem b) {
        augmentable = a;
        bauble = b;
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityAugmentableItem.AUGMENTABLE_ITEM ||
                capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
    }
    
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityAugmentableItem.AUGMENTABLE_ITEM)
            return CapabilityAugmentableItem.AUGMENTABLE_ITEM.cast(augmentable);
        else if (capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE)
            return BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(bauble);
        else
            return null;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        augmentable.deserializeNBT(nbt);
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        return augmentable.serializeNBT();
    }
    
}
