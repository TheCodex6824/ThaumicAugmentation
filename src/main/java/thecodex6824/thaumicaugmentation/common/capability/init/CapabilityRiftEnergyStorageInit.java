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

package thecodex6824.thaumicaugmentation.common.capability.init;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import thecodex6824.thaumicaugmentation.api.energy.IRiftEnergyStorage;
import thecodex6824.thaumicaugmentation.api.energy.RiftEnergyStorage;

public final class CapabilityRiftEnergyStorageInit {

    private CapabilityRiftEnergyStorageInit() {}
    
    public static void init() {
        CapabilityManager.INSTANCE.register(IRiftEnergyStorage.class, new Capability.IStorage<IRiftEnergyStorage>() {
            
            @Override
            public void readNBT(Capability<IRiftEnergyStorage> capability, IRiftEnergyStorage instance, EnumFacing side,
                    NBTBase nbt) {
                
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            public NBTBase writeNBT(Capability<IRiftEnergyStorage> capability, IRiftEnergyStorage instance, EnumFacing side) {
                return instance.serializeNBT();
            }
            
        }, () -> new RiftEnergyStorage(1000));
    }
    
}
