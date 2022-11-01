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

package thecodex6824.thaumicaugmentation.api.ward.tile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/*
 * Interface for warded tile entities.
 * @author TheCodex6824
 */
public interface IWardedTile {

    /*
     * Returns the block position of this warded tile. If for some reason it would
     * not have a block position, then return a position referring to (0, 0, 0)
     * instead - do not return null.
     * @return The BlockPos of this tile, or (0, 0, 0) if it does not have one
     */
    BlockPos getPosition();

    /*
     * Returns a unique type ID for this warded tile. All instances of this tile should
     * share this ID. It does not matter what it is, as long as it is unique. This is used
     * by objects like the Thaumium key to restrict usable tiles. It should not change.
     * @return A unique type ID for this warded tile
     */
    String getUniqueTypeID();

    /*
     * Sets the owner of this warded tile.
     * @param uuid The UUID of the owner
     */
    void setOwner(UUID uuid);

    /*
     * Returns the owner of this warded tile.
     * @return The owner
     */
    UUID getOwner();

    /*
     * Method called when an entity tries to interact with a warded tile. This should fire
     * a {@link thecodex6824.thaumicaugmentation.api.event.WardedTilePermissionEvent WardedBlockPermissionEvent}, 
     * and should react to its result and/or cancellation. The default check can be defined by 
     * the implementation, but should generally check if the player is the owner and/or the player has an 
     * {@link thecodex6824.thaumicaugmentation.api.item.IWardAuthenticator IWardAuthenticator} that allows access.
     * @param living The entity trying to access this warded tile.
     * @return If the entity has permission, and the interaction should continue
     */
    boolean hasPermission(EntityLivingBase living);

}
