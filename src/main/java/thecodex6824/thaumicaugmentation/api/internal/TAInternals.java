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

public final class TAInternals {

    private static IInternalMethodProvider provider;
    
    private TAInternals() {}
    
    public static void setInternalMethodProvider(IInternalMethodProvider provider) {
        TAInternals.provider = provider;
    }
    
    public static IAugmentableItem createAugmentableItem(int slots) {
        return provider.createAugmentableItemImpl(slots);
    }
    
    public static IRiftEnergyStorage createRiftEnergyStorage(long capacity) {
        return provider.createRiftEnergyStorageImpl(capacity);
    }
    
    public static IRiftEnergyStorage createRiftEnergyStorage(long capacity, long maxTransfer) {
        return provider.createRiftEnergyStorageImpl(capacity, maxTransfer);
    }
    
    public static IRiftEnergyStorage createRiftEnergyStorage(long capacity, long maxReceive, long maxExtract) {
        return provider.createRiftEnergyStorageImpl(capacity, maxReceive, maxExtract);
    }
    
    public static IRiftEnergyStorage createRiftEnergyStorage(long capacity, long maxReceive, long maxExtract, long initial) {
        return provider.createRiftEnergyStorageImpl(capacity, maxReceive, maxExtract, initial);
    }
    
    public static IWardedInventory createWardedInventory(int slots) {
        return provider.createWardedInventoryImpl(slots);
    }
    
    public static void addConfigListener(Runnable listener) {
        provider.addConfigListener(listener);
    }
    
    public static boolean removeConfigListener(Runnable listener) {
        return provider.removeConfigListener(listener);
    }
    
}
