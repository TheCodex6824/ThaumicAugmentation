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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import thecodex6824.thaumicaugmentation.api.event.BlockWardEvent;

/**
 * Default implementation of {@link IWardStorage} for clients.
 * @author TheCodex6824
 */
public class WardStorageClient implements IWardStorageClient {

    private static final class StorageManagersClient {
        
        public static final int CHUNK_X_SIZE = 16;
        public static final int CHUNK_Y_SIZE = 256;
        public static final int CHUNK_Z_SIZE = 16;
        public static final int CHUNK_DATA_SIZE = CHUNK_X_SIZE * CHUNK_Y_SIZE * CHUNK_Z_SIZE;
        
        private StorageManagersClient() {}
        
        public static interface IWardStorageManagerClient {
            
            public ClientWardStorageValue getOwner(BlockPos pos);
            
            public void setOwner(BlockPos pos, ClientWardStorageValue owner);
            
            public boolean isNullStorage();
            
        }
        
        public static class StorageManagerNull implements IWardStorageManagerClient {
            
            public StorageManagerNull() {}
            
            @Override
            public void setOwner(BlockPos pos, ClientWardStorageValue owner) {}
            
            @Override
            public ClientWardStorageValue getOwner(BlockPos pos) {
                return ClientWardStorageValue.EMPTY;
            }
            
            @Override
            public boolean isNullStorage() {
                return true;
            }
            
        }
        
        public static class StorageManager2Bits implements IWardStorageManagerClient {
            
            private byte[] data;
            
            public StorageManager2Bits() {
                data = new byte[CHUNK_DATA_SIZE / 4];
            }
            
            public StorageManager2Bits(byte[] input) {
                data = input;
                if (data.length != CHUNK_DATA_SIZE / 4)
                    throw new RuntimeException("Invalid ward storage size");
            }
            
            @Override
            public ClientWardStorageValue getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int result = ((data[index / 4] & (3 << (index % 4 * 2)))) >>> (index % 4 * 2);
                return ClientWardStorageValue.fromID((byte) result);
            }
            
            @Override
            public void setOwner(BlockPos pos, ClientWardStorageValue owner) {
                byte id = owner.getID();
                int index = (pos.getX() & 15) + (pos.getY() & 255) * 16 + (pos.getZ() & 15) * 16 * 256;
                
                data[index / 4] = (id & 1) != 0 ? (byte) (data[index / 4] | (1 << (index % 4 * 2))) : 
                    (byte) (data[index / 4] & ~(1 << (index % 4 * 2)));
                data[index / 4] = (id & 2) != 0 ? (byte) (data[index / 4] | (2 << (index % 4 * 2))) : 
                    (byte) (data[index / 4] & ~(2 << (index % 4 * 2)));
            }
            
            @Override
            public boolean isNullStorage() {
                return false;
            }
            
        }
    }
    
    protected StorageManagersClient.IWardStorageManagerClient manager;
    
    public WardStorageClient() {
        manager = new StorageManagersClient.StorageManagerNull();
    }
    
    @Override
    public void setWard(BlockPos pos, ClientWardStorageValue val) {
        BlockWardEvent.WardedClient event = new BlockWardEvent.WardedClient.Pre(FMLClientHandler.instance().getClient().world, pos, val);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            if (manager.isNullStorage())
                manager = new StorageManagersClient.StorageManager2Bits();
            
            manager.setOwner(pos, val);
            MinecraftForge.EVENT_BUS.post(new BlockWardEvent.WardedClient.Post(FMLClientHandler.instance().getClient().world, pos, val));
        }
    }
    
    @Override
    public void clearWard(BlockPos pos) {
        BlockWardEvent.DewardedClient event = new BlockWardEvent.DewardedClient.Pre(FMLClientHandler.instance().getClient().world, pos);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            manager.setOwner(pos, ClientWardStorageValue.EMPTY);
            MinecraftForge.EVENT_BUS.post(new BlockWardEvent.DewardedClient.Post(FMLClientHandler.instance().getClient().world, pos));
        }
    }
    
    @Override
    public ClientWardStorageValue getWard(BlockPos pos) {
        return manager.getOwner(pos);
    }
    
    @Override
    public boolean hasWard(BlockPos pos) {
        return getWard(pos) != ClientWardStorageValue.EMPTY;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.getBoolean("o"))
            manager = new StorageManagersClient.StorageManager2Bits(nbt.getByteArray("d"));
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }
    
}
