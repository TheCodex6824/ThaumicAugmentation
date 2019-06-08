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

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import thecodex6824.thaumicaugmentation.api.energy.IAnarumStorage;

public final class CapabilityAnarumStorageImpl {

    private CapabilityAnarumStorageImpl() {}
    
    public static void init() {
        CapabilityManager.INSTANCE.register(IAnarumStorage.class, new Capability.IStorage<IAnarumStorage>() {
            
            @Override
            public void readNBT(Capability<IAnarumStorage> capability, IAnarumStorage instance, EnumFacing side,
                    NBTBase nbt) {
                
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            public NBTBase writeNBT(Capability<IAnarumStorage> capability, IAnarumStorage instance, EnumFacing side) {
                return instance.serializeNBT();
            }
            
        }, () -> new DefaultImpl(1000));
    }
    
    public static class DefaultImpl implements IAnarumStorage {
        
        private long energy;
        private long maxEnergy;
        private long maxReceive;
        private long maxExtract;
        
        public DefaultImpl(long maxEnergy) {
            this(maxEnergy, maxEnergy, maxEnergy, 0);
        }
        
        public DefaultImpl(long maxEnergy, long maxTransfer) {
            this(maxEnergy, maxTransfer, maxTransfer, 0);
        }
        
        public DefaultImpl(long maxEnergy, long maxReceive, long maxExtract) {
            this(maxEnergy, maxReceive, maxExtract, 0);
        }
        
        public DefaultImpl(long maxEnergy, long maxReceive, long maxExtract, long initialEnergy) {
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
        public long extractEnergy(long maxEnergy, boolean simulate) {
            if (canExtract()) {
                long amount = Math.min(energy, Math.max(maxExtract, maxEnergy));
                if (!simulate)
                    energy -= amount;
                
                return amount;
            }
            
            return 0;
        }
        
        @Override
        public long receiveEnergy(long maxEnergy, boolean simulate) {
            if (canReceive()) {
                long amount = Math.min(Math.min(maxReceive, maxEnergy), maxEnergy - energy);
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
    
}
