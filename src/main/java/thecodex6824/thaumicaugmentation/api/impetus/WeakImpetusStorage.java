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

package thecodex6824.thaumicaugmentation.api.impetus;

import java.lang.ref.WeakReference;

import net.minecraft.nbt.NBTTagCompound;

public class WeakImpetusStorage implements IImpetusStorage {

    protected WeakReference<IImpetusStorage> wrapped;
    
    public WeakImpetusStorage() {
        wrapped = new WeakReference<>(null);
    }
    
    public WeakImpetusStorage(IImpetusStorage storage) {
        bind(storage);
    }
    
    public void bind(IImpetusStorage storage) {
        wrapped = new WeakReference<>(storage);
    }
    
    public boolean isValid() {
        return wrapped.get() != null;
    }
    
    @Override
    public boolean canExtract() {
        IImpetusStorage s = wrapped.get();
        return s != null && s.canExtract();
    }
    
    @Override
    public boolean canReceive() {
        IImpetusStorage s = wrapped.get();
        return s != null && s.canReceive();
    }
    
    @Override
    public long extractEnergy(long maxToExtract, boolean simulate) {
        IImpetusStorage s = wrapped.get();
        return s != null ? s.extractEnergy(maxToExtract, simulate) : 0;
    }
    
    @Override
    public long receiveEnergy(long maxToReceive, boolean simulate) {
        IImpetusStorage s = wrapped.get();
        return s != null ? s.extractEnergy(maxToReceive, simulate) : 0;
    }
    
    @Override
    public long getEnergyStored() {
        IImpetusStorage s = wrapped.get();
        return s != null ? s.getEnergyStored() : 0;
    }
    
    @Override
    public long getMaxEnergyStored() {
        IImpetusStorage s = wrapped.get();
        return s != null ? s.getMaxEnergyStored() : 0;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {}
    
    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }
    
}
