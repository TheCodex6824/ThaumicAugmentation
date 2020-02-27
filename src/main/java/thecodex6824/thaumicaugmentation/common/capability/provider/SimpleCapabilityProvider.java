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

package thecodex6824.thaumicaugmentation.common.capability.provider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class SimpleCapabilityProvider<C> implements ICapabilitySerializable<NBTTagCompound> {

    protected C instance;
    protected Capability<C> cap;
    
    public SimpleCapabilityProvider(C inst, Capability<C> c) {
        cap = c;
        instance = inst;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        cap.readNBT(instance, null, nbt);
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) cap.writeNBT(instance, null);
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == cap;
    }
    
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        // in case some mod decides to query caps before they were set up
        if (cap != null && capability == cap)
            return cap.cast(instance);
        
        return null;
    }
    
}
