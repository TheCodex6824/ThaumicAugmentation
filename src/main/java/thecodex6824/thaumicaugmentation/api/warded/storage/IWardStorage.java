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

package thecodex6824.thaumicaugmentation.api.warded.storage;

import net.minecraft.util.math.BlockPos;

/**
 * Base ward (focus) storage interface. There is only a single method for determining if
 * a block is warded, as the representation held by the client and server is different.
 * @author TheCodex6824
 */
public interface IWardStorage {
    
    /**
     * Returns if the block at the given position has a ward with any owner.
     * @param pos The position of the block to check
     * @return If the block at the position is warded
     */
    public boolean hasWard(BlockPos pos);
    
}
