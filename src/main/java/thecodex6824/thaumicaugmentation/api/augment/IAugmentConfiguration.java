/**
 *  Thaumic Augmentation
 *  Copyright (c) 2022 KevoHoff.
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

package thecodex6824.thaumicaugmentation.api.augment;

import com.google.common.collect.ImmutableMap;

import net.minecraft.item.ItemStack;

/**
 * Interface for the AugmentConfiguration capability.
 */
public interface IAugmentConfiguration {

    /**
     * Returns a read-only view of the augments in this configuration.
     * @return The specific augments contained in the configuration
     */
    public ImmutableMap<Integer, ItemStack> getAugmentConfig();
    
    /**
     * Sets an augment in the augment configuration if the
     * augment is acceptable.
     * @param augment The augment to set in some slot
     * @param slot The slot the augment is being added to
     * @return The augment previously in this slot, or the empty ItemStack if it was empty
     */
    public ItemStack setAugment(ItemStack augment, int slot);

    /**
     * Removes an augment from the configuration. Pops augment
     * from configuration stack.
     * @param slot The slot the augment is being removed from
     * @return The removed augment if it was present, the empty ItemStack otherwise.
     */
    public ItemStack removeAugment(int slot);

    /**
     * Returns if the provided augment can be inserted into the configuration.
     * @param augment The augment to check
     * @param slot The slot the augment would go into
     * @return If the given augment can be inserted into the configuration
     */
    public boolean isAugmentAcceptable(ItemStack augment, int slot);
    
}
