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

import thaumcraft.common.entities.EntityFluxRift;

public class FluxRiftImpetusStorage implements IImpetusStorage {
    
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
        size = Math.min(size, MAX_SIZE);
        long total = 0;
        for (int i = 1; i <= size; ++i)
            total += calcEnergyThisSize(size);
        
        return total;
    }
    
    protected long calcEnergyThisSize(int size) {
        /*
         * f(x) = (1/2) (e^(-(x-172)^2 / 11250) * x)
         */
        size = Math.min(size, MAX_SIZE);
        return (long) (0.5 * (Math.pow(Math.E, -Math.pow(size - 172, 2) / 11250.0) * size));
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
            return calcTotalRiftEnergy(MAX_SIZE);
        
        return 0;
    }
    
    @Override
    public long extractEnergy(long maxToExtract, boolean simulate) {
        EntityFluxRift r = rift.get();
        if (r != null && r.getRiftSize() > 0) {
            long toExtract = Math.min(calcEnergyThisSize(r.getRiftSize()), maxToExtract);
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
            long required = calcEnergyThisSize(r.getRiftSize() + 1);
            long toReceive = Math.min(maxToReceive, required);
            if (toReceive >= required) {
                if (!simulate)
                    r.setRiftSize(r.getRiftSize() + 1);
                
                return required;
            }
            else
                return 0;
        }
        
        return 0;
    }
    
}
