/*
 *  Thaumic Augmentation
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

package thecodex6824.thaumicaugmentation.api.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

/*
 * Interface implemented by Dimensional Fractures.
 * @author TheCodex6824
 */
public interface IDimensionalFracture {

    /*
     * Sets the position the fracture will teleport things to.
     * @param pos The new position to link to
     */
    void setLinkedPosition(BlockPos pos);

    /*
     * Returns this fracture's linked position.
     * @return The linked position
     */
    BlockPos getLinkedPosition();

    /*
     * Sets the dimension this fracture will teleport things to. The fracture will
     * not attempt to validate the dimension until it attempts to teleport something.
     * @param dim The new dimension to link to
     */
    void setLinkedDimension(int dim);

    /*
     * Returns this fracture's linked dimension. This may not be a valid dimension, as
     * the fracture will not attempt to validate the dimension until it attempts to teleport something.
     * @return The dimension ID of the linked dimension
     */
    int getLinkedDimension();
    
    /*
     * Sets a flag that tells the fracture to not search for another fracture at the destination.
     */
    void setLinkLocated();
    
    /*
     * Sets or unsets a flag that tells the fracture to not search for another fracture at the destination.
     * @param located If the destination was located
     */
    void setLinkLocated(boolean located);

    /*
     * Returns if the fracture found another fracture at its destination.
     * @return If the link was located
     */
    boolean wasLinkLocated();

    /*
     * Sets a flag that tells the fracture to not attempt to teleport things, as the destination is not valid.
     */
    void setLinkInvalid();
    
    /*
     * Sets or unsets a flag that tells the fracture to not attempt to teleport things, as the destination is not valid.
     * @param invalid If the link is invalid
     */
    void setLinkInvalid(boolean invalid);

    /*
     * Returns if the fracture's link is valid.
     * @return If the link is valid
     */
    boolean isLinkInvalid();

    /*
     * Opens the fracture, starting its opening transition. It will not be usable until the opening duration
     * elapses.
     * 
     * @see #getOpeningDuration()
     */
    void open();
    
    /*
     * Opens the fracture, optionally starting its opening transition.
     * @param skipTransition If the fracture should immediately be fully open
     */
    void open(boolean skipTransition);
    
    /*
     * Returns the time needed for the fracture to completely open after starting to open.
     * @return The opening duration
     */
    int getOpeningDuration();
    
    /*
     * Instantly closes this fracture.
     */
    void close();

    /*
     * Returns if the fracture is in the transition period before being fully open.
     * @return If this fracture is opening
     */
    boolean isOpening();
    
    /*
     * Returns if this fracture is fully opened.
     * @return If this fracture is fully opened
     */
    boolean isOpen();
    
    /*
     * Returns the world time that the fracture was opened at.
     * @return The world time the fracture was opened at
     */
    long getTimeOpened();
    
    @Nullable
    Biome getDestinationBiome();
    
    void setDestinationBiome(@Nullable Biome biome);
    
}
