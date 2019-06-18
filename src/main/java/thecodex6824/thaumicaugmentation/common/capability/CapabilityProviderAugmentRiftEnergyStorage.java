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

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.energy.CapabilityRiftEnergyStorage;
import thecodex6824.thaumicaugmentation.api.energy.IRiftEnergyStorage;

public class CapabilityProviderAugmentRiftEnergyStorage implements ICapabilitySerializable<NBTTagCompound> {

    private IAugment augment;
    private IRiftEnergyStorage energy;
    
    public CapabilityProviderAugmentRiftEnergyStorage(IAugment aug, IRiftEnergyStorage e) {
        augment = aug;
        energy = e;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        CapabilityAugment.AUGMENT.readNBT(augment, null, nbt.getCompoundTag("augment"));
        CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE.readNBT(energy, null, nbt.getCompoundTag("energy"));
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("augment", CapabilityAugment.AUGMENT.writeNBT(augment, null));
        tag.setTag("energy", CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE.writeNBT(energy, null));
        return tag;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityAugment.AUGMENT || capability == CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE;
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityAugment.AUGMENT)
            return CapabilityAugment.AUGMENT.cast(augment);
        else if (capability == CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE)
            return CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE.cast(energy);
        
        return null;
    }
    
}
