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

import java.lang.ref.WeakReference;

import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.api.energy.IRiftEnergyStorage;

public class RiftEnergyStorageFluxRiftImpl implements IRiftEnergyStorage {

    protected static final long MAX_ENERGY = 1000;
    
    private WeakReference<EntityFluxRift> rift;
    
    public RiftEnergyStorageFluxRiftImpl(EntityFluxRift rift) {
        this.rift = new WeakReference<>(rift);
    }
    
    @Override
    public boolean canExtract() {
        return rift.get() != null;
    }
    
    @Override
    public boolean canReceive() {
        return rift.get() != null;
    }
    
    @Override
    public long getEnergyStored() {
        EntityFluxRift r = rift.get();
        if (r != null)
            return r.getRiftSize() * 5;
        
        return 0;
    }
    
    @Override
    public long getMaxEnergyStored() {
        EntityFluxRift r = rift.get();
        if (r != null)
            return MAX_ENERGY;
        
        return 0;
    }
    
    @Override
    public long extractEnergy(long maxToExtract, boolean simulate) {
        EntityFluxRift r = rift.get();
        if (r != null) {
            long toExtract = Math.min(r.getRiftSize() * 5, Math.min(5, maxToExtract));
            if (!simulate) {
                r.setRiftSize((int) (r.getRiftSize() - toExtract / 5));
                r.setRiftStability(r.getRiftStability() - 1.0F);
            }
            
            return toExtract;
        }
        
        return 0;
    }
    
    @Override
    public long receiveEnergy(long maxToReceive, boolean simulate) {
        EntityFluxRift r = rift.get();
        if (r != null) {
            long toReceive = Math.min(Math.min(5, maxToReceive), MAX_ENERGY - r.getRiftSize() * 5);
            if (!simulate)
                r.setRiftSize((int) (r.getRiftSize() + toReceive / 5));
            
            return toReceive;
        }
        
        return 0;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {}
    
    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }
    
}
