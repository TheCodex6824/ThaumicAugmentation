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

package thecodex6824.thaumicaugmentation.api.impetus;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/*
 * Default implementation of the ImpetusStorage capability.
 * @author TheCodex6824
 */
public class ImpetusStorage implements IImpetusStorage, INBTSerializable<NBTTagCompound> {

    protected long energy;
    protected long maxEnergy;
    protected long maxReceive;
    protected long maxExtract;
    
    public ImpetusStorage(long maxEnergy) {
        this(maxEnergy, maxEnergy, maxEnergy, 0);
    }
    
    public ImpetusStorage(long maxEnergy, long maxTransfer) {
        this(maxEnergy, maxTransfer, maxTransfer, 0);
    }
    
    public ImpetusStorage(long maxEnergy, long maxReceive, long maxExtract) {
        this(maxEnergy, maxReceive, maxExtract, 0);
    }
    
    public ImpetusStorage(long maxEnergy, long maxReceive, long maxExtract, long initialEnergy) {
        energy = Math.max(0, Math.min(maxEnergy, initialEnergy));
        this.maxEnergy = maxEnergy;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }
    
    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }
    
    @Override
    public boolean canReceive() {
        return maxReceive > 0;
    }
    
    @Override
    public long extractEnergy(long maxToExtract, boolean simulate) {
        if (canExtract()) {
            long amount = Math.min(energy, Math.min(maxExtract, maxToExtract));
            if (!simulate)
                energy -= amount;
            
            return amount;
        }
        
        return 0;
    }
    
    @Override
    public long receiveEnergy(long maxToReceive, boolean simulate) {
        if (canReceive()) {
            long amount = Math.min(Math.min(maxReceive, maxToReceive), maxEnergy - energy);
            if (!simulate)
                energy += amount;
            
            return amount;
        }
        
        return 0;
    }
    
    @Override
    public long getEnergyStored() {
        return energy;
    }
    
    @Override
    public long getMaxEnergyStored() {
        return maxEnergy;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        energy = nbt.getLong("energy");
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("energy", energy);
        return tag;
    }
    
}
