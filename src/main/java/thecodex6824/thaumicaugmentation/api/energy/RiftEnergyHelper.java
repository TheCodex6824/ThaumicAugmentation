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

public class RiftEnergyHelper {

    private RiftEnergyHelper() {}
    
    public static final String ENERGY_NONE = "thaumicaugmentation.text.energy_none";
    public static final String ENERGY_MINIMAL = "thaumicaugmentation.text.energy_minimal";
    public static final String ENERGY_VERY_WEAK = "thaumicaugmentation.text.energy_very_weak";
    public static final String ENERGY_WEAK = "thaumicaugmentation.text.energy_weak";
    public static final String ENERGY_MEDIUM = "thaumicaugmentation.text.energy_medium";
    public static final String ENERGY_STRONG = "thaumicaugmentation.text.energy_strong";
    public static final String ENERGY_MAX = "thaumicaugmentation.text.energy_max";
    
    public static String getEnergyAmountDescriptor(IRiftEnergyStorage storage) {
        if (storage.getEnergyStored() <= 0)
            return ENERGY_NONE;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.1)
            return ENERGY_MINIMAL;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.25)
            return ENERGY_VERY_WEAK;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.5)
            return ENERGY_WEAK;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.75)
            return ENERGY_MEDIUM;
        else if (storage.getEnergyStored() < storage.getMaxEnergyStored())
            return ENERGY_STRONG;
        else
            return ENERGY_MAX;
    }
    
}
