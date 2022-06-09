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

import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;

public class InfiniteImpetusStorage implements IImpetusStorage {

    protected boolean source;
    protected boolean sink;
    
    public InfiniteImpetusStorage(boolean source, boolean sink) {
        this.source = source;
        this.sink = sink;
    }
    
    @Override
    public boolean canExtract() {
        return source;
    }
    
    @Override
    public boolean canReceive() {
        return sink;
    }
    
    @Override
    public long extractEnergy(long maxEnergy, boolean simulate) {
        return source ? maxEnergy : 0;
    }
    
    @Override
    public long getEnergyStored() {
        return getMaxEnergyStored();
    }
    
    @Override
    public long getMaxEnergyStored() {
        return source ? Long.MAX_VALUE : 0;
    }
    
    @Override
    public long receiveEnergy(long maxEnergy, boolean simulate) {
        return sink ? maxEnergy : 0;
    }
    
}
