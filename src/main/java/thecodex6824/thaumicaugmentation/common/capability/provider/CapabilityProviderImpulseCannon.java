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

package thecodex6824.thaumicaugmentation.common.capability.provider;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import thecodex6824.thaumicaugmentation.api.augment.AugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderImpulseCannon implements ICapabilitySerializable<NBTTagCompound> {

    private final AugmentableItem augmentable;
    private final ImpetusStorage energy;
    
    public CapabilityProviderImpulseCannon(AugmentableItem aug, ImpetusStorage e) {
        augmentable = aug;
        energy = e;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        augmentable.deserializeNBT(nbt.getCompoundTag("augments"));
        energy.deserializeNBT(nbt.getCompoundTag("energy"));
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("augments", augmentable.serializeNBT());
        tag.setTag("energy", energy.serializeNBT());
        return tag;
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityAugmentableItem.AUGMENTABLE_ITEM || capability == CapabilityImpetusStorage.IMPETUS_STORAGE;
    }
    
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityAugmentableItem.AUGMENTABLE_ITEM)
            return CapabilityAugmentableItem.AUGMENTABLE_ITEM.cast(augmentable);
        else if (capability == CapabilityImpetusStorage.IMPETUS_STORAGE)
            return CapabilityImpetusStorage.IMPETUS_STORAGE.cast(energy);
        
        return null;
    }
    
}
