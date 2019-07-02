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

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public interface IWardStorageServer extends IWardStorage {

    public static final UUID EMPTY_UUID = new UUID(0, 0);
    
    public int getTotalWardOwners();
    
    public boolean isWardOwner(UUID id);
    
    public void setWard(World syncTo, BlockPos pos, UUID owner);
    
    public void clearWard(World syncTo, BlockPos pos);
    
    public UUID getWard(BlockPos pos);
    
    public NBTTagCompound fullSyncToClient(Chunk chunk, UUID player);
    
}
