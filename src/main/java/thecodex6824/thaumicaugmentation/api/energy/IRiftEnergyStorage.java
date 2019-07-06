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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Interface for the Rift Energy Storage capability. This represents an object capable of giving and/or receiving energy.
 * Transmission of the energy is left to individual components, this capability only handles storing a quantity. The API was
 * based on the Forge Energy / RF API, only using longs instead of ints to allow crazy energy values.
 * @author TheCodex6824
 */
public interface IRiftEnergyStorage extends INBTSerializable<NBTTagCompound> {

    /**
     * Attempts to insert energy into this object, optionally just simulating the result.
     * @param maxEnergy The maximum amount of energy to insert
     * @param simulate Whether the energy value of this item should be changed
     * @return The actual amount of energy inserted
     */
    long receiveEnergy(long maxEnergy, boolean simulate);
    
    /**
     * Attempts to remove energy from this object, optionally just simulating the result.
     * @param maxEnergy The maximum amount of energy to extract
     * @param simulate Whether the energy value of this item should be changed
     * @return The actual amount of energy removed
     */
    long extractEnergy(long maxEnergy, boolean simulate);
    
    /**
     * Returns the amount of energy currently stored.
     * @return The amount of energy stored
     */
    long getEnergyStored();
    
    /**
     * Returns the maximum amount of energy that can be stored in this object.
     * @return The maximum amount of energy that can be stored
     */
    long getMaxEnergyStored();
    
    /**
     * Returns whether this object is capable of receiving at all. If this object is not, then
     * {@link #receiveEnergy} will always return 0.
     * @return If this object can receive energy
     */
    boolean canReceive();
    
    /**
     * Returns whether this object is capable of having energy be extracted at all. If this object is not, then
     * {@link #extractEnergy} will always return 0.
     * @return If this object can have energy extracted
     */
    boolean canExtract();
    
}
