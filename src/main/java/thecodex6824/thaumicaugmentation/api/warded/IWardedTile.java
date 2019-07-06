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

package thecodex6824.thaumicaugmentation.api.warded;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Interface for warded tile entities.
 * @author TheCodex6824
 */
public interface IWardedTile extends INBTSerializable<NBTTagCompound> {

    /**
     * Returns the block position of this warded tile. If for some reason it would
     * not have a block position, then return a position referring to (0, 0, 0)
     * instead - do not return null.
     * @return The BlockPos of this tile, or (0, 0, 0) if it does not have one
     */
    public BlockPos getPosition();

    /**
     * Returns a unique type ID for this warded tile. All instances of this tile should
     * share this ID. It does not matter what it is, as long as it is unique. This is used
     * by objects like the Thaumium key to restrict usable tiles. It should not change.
     * @return A unique type ID for this warded tile
     */
    public String getUniqueTypeID();

    /**
     * Sets the owner of this warded tile. For players, this should be their UUID, as
     * usernames can change.
     * @param uuid The UUID (or just string for NPCs) of the owner
     */
    public void setOwner(String uuid);

    /**
     * Returns the owner of this warded tile.
     * @return The owner
     */
    public String getOwner();

    /**
     * Method called when a player tries to interact with a warded tile. This should fire
     * a {@link thecodex6824.thaumicaugmentation.api.event.WardedTilePermissionEvent WardedBlockPermissionEvent}, 
     * and should react to its result and/or cancellation, unless the player is a server operator and 
     * {@link thecodex6824.thaumicaugmentation.api.TAConfig#opWardOverride opWardOverride} is true, 
     * in which case they should be granted access without firing an event. The default check can be defined by 
     * the implementation, but should generally check if the player is the owner and/or the player has an 
     * {@link thecodex6824.thaumicaugmentation.api.item.IWardAuthenticator IWardAuthenticator} that allows access.
     * @param player The player trying to access this warded tile.
     * @return If the player has permission, and the interaction should continue
     */
    public boolean hasPermission(EntityPlayer player);

}
