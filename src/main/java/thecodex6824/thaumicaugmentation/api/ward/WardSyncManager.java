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

package thecodex6824.thaumicaugmentation.api.ward;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorageServer;

/**
 * Handles syncing ward changes to clients.
 * @author TheCodex6824
 */
public final class WardSyncManager {

    private WardSyncManager() {}
    
    public static class DimensionalChunkPos {
        
        public final int dim;
        public final int x;
        public final int z;
        
        public DimensionalChunkPos(int dim, int x, int z) {
            this.dim = dim;
            this.x = x;
            this.z = z;
        }
        
    }
    
    public static class WardUpdateEntry {
        
        public final BlockPos pos;
        
        @Nullable
        public final UUID update;
        
        public WardUpdateEntry(BlockPos p, UUID data) {
            pos = p;
            update = data;
        }
        
    }
    
    private static Multimap<DimensionalChunkPos, WardUpdateEntry> entries = MultimapBuilder.hashKeys().arrayListValues(16).build();
    
    public static void markChunkForFullSync(World world, BlockPos pos) {
        markPosForNewOwner(world, pos, null);
    }
    
    public static void markPosForClear(World world, BlockPos pos) {
        markPosForNewOwner(world, pos, IWardStorageServer.NIL_UUID);
    }
    
    public static void markPosForNewOwner(World world, BlockPos pos, UUID newOwner) {
        entries.put(new DimensionalChunkPos(world.provider.getDimension(), pos.getX() >> 4, pos.getZ() >> 4), 
                new WardUpdateEntry(pos.toImmutable(), newOwner));
    }
    
    public static Collection<Entry<DimensionalChunkPos, WardUpdateEntry>> getEntries() {
        return entries.entries();
    }
    
    public static void clearEntries() {
        entries.clear();
    }
}
