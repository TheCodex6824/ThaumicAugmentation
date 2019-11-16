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

package thecodex6824.thaumicaugmentation.api.world.capability;

import java.util.Collection;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

/**
 * Interface that marks an area as having a fracture in it.
 * @author TheCodex6824
 */
public interface IFractureLocations {

    /**
     * Sets the chunk the fracture(s) are located in.
     * @param c The new chunk
     */
    public void setChunk(Chunk c);
    
    /**
     * Returns if this chunk has a fracture in it.
     * @return If this chunk has a fracture in it
     */
    public boolean hasFracture();
    
    /**
     * Adds a stored location for a fracture in this chunk. This
     * does not actually move any fractures, it only stores the location.
     * @param pos The position of the fracture in this chunk
     */
    public void addFractureLocation(BlockPos pos);
    
    /**
     * Removes a stored location of a fracture in this chunk. This
     * does not actually move any fractures, it only stores the location.
     * @param pos The position of the fracture in this chunk
     */
    public void removeFractureLocation(BlockPos pos);
    
    /**
     * Returns the locations of the fracture(s) in this chunk.
     * @return The location of the fractures
     */
    public Collection<BlockPos> getFractureLocations();
    
}
