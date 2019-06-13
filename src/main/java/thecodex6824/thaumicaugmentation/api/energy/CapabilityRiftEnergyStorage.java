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

package thecodex6824.thaumicaugmentation.api.energy;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import thecodex6824.thaumicaugmentation.api.internal.TAInternals;

public final class CapabilityRiftEnergyStorage {

    private CapabilityRiftEnergyStorage() {}
    
    @CapabilityInject(IRiftEnergyStorage.class)
    public static final Capability<IRiftEnergyStorage> RIFT_ENERGY_STORAGE = null;
    
    public static IRiftEnergyStorage create(long capacity) {
        return TAInternals.createRiftEnergyStorage(capacity);
    }
    
    public static IRiftEnergyStorage create(long capacity, long maxTransfer) {
        return TAInternals.createRiftEnergyStorage(capacity, maxTransfer);
    }
    
    public static IRiftEnergyStorage create(long capacity, long maxReceive, long maxExtract) {
        return TAInternals.createRiftEnergyStorage(capacity, maxReceive, maxExtract);
    }
    
    public static IRiftEnergyStorage create(long capacity, long maxReceive, long maxExtract, long initial) {
        return TAInternals.createRiftEnergyStorage(capacity, maxReceive, maxExtract, initial);
    }
    
}
