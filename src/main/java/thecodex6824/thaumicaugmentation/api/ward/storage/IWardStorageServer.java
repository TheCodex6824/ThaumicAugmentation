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

package thecodex6824.thaumicaugmentation.api.ward.storage;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * The ward storage interface for the server side. Servers have access to all information
 * for each warded block, so they can get the player/UUID associated with each ward.
 * Implementations should try to be efficient when it comes to server resources.
 * @author TheCodex6824
 */
public interface IWardStorageServer extends IWardStorage {

    /**
     * A constant UUID that should be used for blocks with
     * no ward - null should not be returned. This UUID is not
     * a valid UUID, as the variant and version fields are 0.
     * This makes the chance of a collision with a real UUID very hard to achieve.
     */
    public static final UUID NIL_UUID = new UUID(0, 0);
    
    /**
     * Returns the total amount of things that own any amount of wards in this
     * area.
     * @return The total amount of owners of wards
     */
    public int getTotalWardOwners();
    
    /**
     * Returns if the given UUID is the owner of a ward.
     * @param id The UUID to check
     * @return If the passed UUID is an owner
     */
    public boolean isWardOwner(UUID id);
    
    /**
     * Sets the owner of a ward of a block to the passed new owner. The caller is
     * responsible for syncing or firing events as needed.
     * @param pos The position of the block to change the owner of
     * @param owner The UUID of the new owner
     */
    public void setWard(BlockPos pos, UUID owner);
    
    /**
     * Sets the owner of a ward of a block to the passed new owner, and handles
     * syncing the change to clients and firing the appropiate events.
     * @param syncTo The world this storage exists in
     * @param pos The position of the block to change the owner of
     * @param owner The UUID of the new owner
     */
    public void setWard(BlockPos pos, UUID owner, World syncTo);
    
    /**
     * Removes the owner of the ward of a block. The caller is
     * responsible for syncing or firing events as needed.
     * @param pos The position of the block to clear
     */
    public void clearWard(BlockPos pos);
    
    /**
     * Removes the owner of the ward of a block, and handles
     * syncing the change to clients and firing the appropiate events.
     * @param syncTo The world this storage exists in
     * @param pos The position of the block to clear
     */
    public void clearWard(BlockPos pos, World syncTo);
    
    /**
     * Unconditionally clears <strong>all</strong> of the wards in this chunk. The caller is
     * responsible for syncing or firing events as needed.
     */
    public void clearAllWards();
    
    /**
     * Unconditionally clears <strong>all</strong> of the wards in this chunk, and handles
     * syncing the change to clients and firing the appropiate events.
     * @param syncTo The world this storage exists in
     * @param inside A BlockPos that exists inside this chunk
     */
    public void clearAllWards(World syncTo, BlockPos inside);
    
    /**
     * Returns the owner of the ward of a block, or the {@link #NIL_UUID} if
     * there is no ward.
     * @param pos The position of the block
     * @return The owner of the ward or {@link #NIL_UUID} if there is no ward
     */
    public UUID getWard(BlockPos pos);
    
    /**
     * Removes the provided owner from the list of ward owners, <strong>and removes
     * all wards owned by this owner. This WILL NOT fire any events, as the amount of events to
     * fire could be extreme.</strong> The caller should then send a full sync to
     * the client(s) - they are not queued automatically as there can potentially be
     * up to 65536 locations to sync. This method is almost never what a caller would actually want - 
     * it is intended for purging a user's wards without knowing their locations.
     * @param owner The owner that will be completely removed
     */
    public void removeOwner(UUID owner);
    
    /**
     * Creates a NBTTagCompound suitable for a full sync to the client. A full
     * sync means that it should include all blocks needed in a storage instance such that
     * the client will have an up-to-date view of the wards in this area, no matter
     * what data they already have. If this storage has no meaningful data,
     * null can be returned to not do a sync.
     * @param chunk The chunk that is to be synced
     * @param player The player that needs to be synced to
     * @return An NBTTagCompound to send to the client for sync purposes, or null if no sync is needed
     */
    public @Nullable NBTTagCompound fullSyncToClient(Chunk chunk, UUID player);
    
    /**
     * Creates a NBTTagCompound suitable for a full sync to the client. A full
     * sync means that it should include all blocks needed in a storage instance such that
     * the client will have an up-to-date view of the wards in this area, no matter
     * what data they already have. If this storage has no meaningful data,
     * null can be returned to not do a sync unless force is true, in which case it will still return data.
     * @param chunk The chunk that is to be synced
     * @param player The player that needs to be synced to
     * @param force To force the storage to return a tag compound, even if it has no meaningful data
     * @return An NBTTagCompound to send to the client for sync purposes, or null if no sync is needed
     */
    public @Nullable NBTTagCompound fullSyncToClient(Chunk chunk, UUID player, boolean force);
    
}
