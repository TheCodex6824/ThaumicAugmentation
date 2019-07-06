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

package thecodex6824.thaumicaugmentation.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thecodex6824.thaumicaugmentation.api.warded.IWardedTile;

/**
 * Interface for items that can allow players to access warded blocks
 * that belong to someone else. This isn't a capability as there would be
 * no sane default implementation, and it only makes sense for items - which completely kills
 * the advantages of capabilities.
 * @author TheCodex6824
 */
public interface IWardAuthenticator {

    /**
     * Returns if this item allows the user to access the provided warded tile.
     * @param tile The tile the player is trying to access
     * @param stack The ItemStack the player is using
     * @param user The player trying to interact
     * @return If this item should allow the player to interact with the warded tile
     */
    public boolean permitsUsage(IWardedTile tile, ItemStack stack, EntityPlayer user);

}
