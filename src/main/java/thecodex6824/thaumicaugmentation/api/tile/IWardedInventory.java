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

package thecodex6824.thaumicaugmentation.api.tile;

import net.minecraftforge.items.IItemHandler;

/**
 * Interface for warded blocks that have a private internal inventory. The inventory is exposed
 * here instead of in a capability so that other things can't (inadvertently) bypass the ward
 * permission checks.
 * @author TheCodex6824
 *
 */
public interface IWardedInventory {

    /**
     * Returns the inventory associated with this warded block. It is assumed that
     * users of this method have done the proper permission checks to permit
     * access.
     * @return The inventory stored in this warded block
     */
    public IItemHandler getInventory();

}
