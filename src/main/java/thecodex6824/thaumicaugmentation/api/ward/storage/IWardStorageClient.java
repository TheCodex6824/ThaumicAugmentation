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

package thecodex6824.thaumicaugmentation.api.ward.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/*
 * The ward storage interface for the client side. Clients should not be able
 * to tell what exact players own wards, so they get limited information from the
 * server.
 * @author TheCodex6824
 */
public interface IWardStorageClient extends IWardStorage {

    /*
     * Removes the ward at the given position.
     * @param pos The position of the block
     */
    void clearWard(BlockPos pos);
    
    /*
     * Unconditionally clears <strong>all</strong> of the wards in this chunk.
     */
    void clearAllWards();
    
    /*
     * Sets the ward at the given position to the given logical ward owner.
     * @param pos The position of the block
     * @param id The effective client side owner of the ward
     */
    void setWard(BlockPos pos, ClientWardStorageValue id);
    
    /*
     * Returns the client side owner of the ward.
     * @param pos The position of the block
     * @return The effective client side owner of the ward
     */
    ClientWardStorageValue getWard(BlockPos pos);
    
    /*
     * Loads data sent from the server into the storage.
     * Note that this is not from INBTSerializable because the
     * client should never be saving ward data to disk.
     * @param tag The NBTTagCompound containing the ward data
     */
    void deserializeNBT(NBTTagCompound tag);
    
}
