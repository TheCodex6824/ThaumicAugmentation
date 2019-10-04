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
import thaumcraft.common.entities.EntityFluxRift;

public class FluxRiftImpetusStorage implements IImpetusStorage {
    
    protected static final long MAX_ENERGY = 5464;
    protected static final int MAX_SIZE = 200;
    
    protected WeakReference<EntityFluxRift> rift;
    
    public FluxRiftImpetusStorage() {
        rift = new WeakReference<>(null);
    }
    
    public FluxRiftImpetusStorage(EntityFluxRift rift) {
        this.rift = new WeakReference<>(rift);
    }
    
    public void bindToRift(EntityFluxRift rift) {
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
    
    protected long calcTotalRiftEnergy(int size) {
        /*
         * f(x) = { 0 < x <= 100: 2^(x / 20),
         *          100 < x <= 200: 2^((x - 85) / 20) + 31 }
         */
        size = size % MAX_SIZE;
        long energy = 0;
        if (size > 100) {
             energy += (long) (5 * Math.pow(2, (size - 85) / 20.0 + 2) / Math.log(2) + 31 * size) -
                     (long) ((5 * Math.pow(2, 2.75)) / Math.log(2) + 3131);
        }
        
        size = Math.min(size, 100);
        energy += (long) (5 * Math.pow(2, size / 20.0 + 2) / Math.log(2)) -
                (long) (5 * Math.pow(2, 2.05) / Math.log(2));
        
        return energy;
    }
    
    protected long calcEnergyThisSize(int size) {
        if (size > MAX_SIZE)
            return 84;
        else if (size > 100)
            return (long) Math.pow(2, (size - 85) / 20.0) + 31;
        else
            return (long) Math.pow(2, size / 20.0);
    }
    
    @Override
    public long getEnergyStored() {
        EntityFluxRift r = rift.get();
        if (r != null)
            return calcTotalRiftEnergy(r.getRiftSize());
        
        return 0;
    }
    
    @Override
    public long getMaxEnergyStored() {
        if (rift.get() != null)
            return MAX_ENERGY;
        
        return 0;
    }
    
    @Override
    public long extractEnergy(long maxToExtract, boolean simulate) {
        EntityFluxRift r = rift.get();
        if (r != null && r.getRiftSize() > 0) {
            long toExtract = Math.min(calcEnergyThisSize(r.getRiftSize()), Math.min(5, maxToExtract));
            if (!simulate) {
                r.setRiftSize(r.getRiftSize() - 1);
                r.setRiftStability(r.getRiftStability() - 1.0F);
            }
            
            return toExtract;
        }
        
        return 0;
    }
    
    @Override
    public long receiveEnergy(long maxToReceive, boolean simulate) {
        EntityFluxRift r = rift.get();
        if (r != null && r.getRiftSize() < MAX_SIZE) {
            long toReceive = Math.min(maxToReceive, calcEnergyThisSize(r.getRiftSize() + 1));
            if (!simulate)
                r.setRiftSize(r.getRiftSize() + 1);
            
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
