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

package thecodex6824.thaumicaugmentation.api.internal;

import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.energy.IRiftEnergyStorage;
import thecodex6824.thaumicaugmentation.api.warded.IWardedInventory;

public interface IInternalMethodProvider {

    public IAugmentableItem createAugmentableItemImpl(int slots);
    
    public IRiftEnergyStorage createRiftEnergyStorageImpl(long energy);
    public IRiftEnergyStorage createRiftEnergyStorageImpl(long energy, long maxTransfer);
    public IRiftEnergyStorage createRiftEnergyStorageImpl(long energy, long maxReceive, long maxExtract);
    public IRiftEnergyStorage createRiftEnergyStorageImpl(long energy, long maxReceive, long maxExtract, long initial);
    
    public IWardedInventory createWardedInventoryImpl(int slots);
    
    public void addConfigListener(Runnable listener);
    
    public boolean removeConfigListener(Runnable listener);
    
}
