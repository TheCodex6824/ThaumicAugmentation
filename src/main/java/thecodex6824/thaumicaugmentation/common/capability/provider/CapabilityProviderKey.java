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
import thecodex6824.thaumicaugmentation.api.item.CapabilityWardAuthenticator;
import thecodex6824.thaumicaugmentation.common.capability.WardAuthenticatorKey;

public class CapabilityProviderKey implements ICapabilitySerializable<NBTTagCompound> {

    protected WardAuthenticatorKey cap;
    
    public CapabilityProviderKey(WardAuthenticatorKey k) {
        cap = k;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        cap.deserializeNBT(nbt);
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        return cap.serializeNBT();
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityWardAuthenticator.WARD_AUTHENTICATOR;
    }
    
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityWardAuthenticator.WARD_AUTHENTICATOR ?
                CapabilityWardAuthenticator.WARD_AUTHENTICATOR.cast(cap) : null;
    }
    
}
