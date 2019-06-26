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

import com.google.common.annotations.VisibleForTesting;

import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import scala.actors.threadpool.Arrays;

public class WardStorageServer implements IWardStorageServer {

    @VisibleForTesting
    static final class StorageManagers {
        
        public static final int CHUNK_X_SIZE = 16;
        public static final int CHUNK_Y_SIZE = 256;
        public static final int CHUNK_Z_SIZE = 16;
        public static final int CHUNK_DATA_SIZE = CHUNK_X_SIZE * CHUNK_Y_SIZE * CHUNK_Z_SIZE;
        
        private StorageManagers() {}
        
        public static interface IWardStorageManager {
            
            public byte getStorageID();
            
            public int getNumCurrentOwners();
            
            public int getMaxAllowedOwners();
            
            public void addOwner(UUID owner);
            
            public void removeOwner(UUID owner);
            
            public boolean isOwner(UUID owner);
            
            public UUID getOwner(BlockPos pos);
            
            public void setOwner(BlockPos pos, UUID owner);
            
            public UUID[] getOwners();
            
            public NBTTagCompound serialize();
            
            public void deserialize(NBTTagCompound tag);
            
        }
        
        public static class StorageManagerNull implements IWardStorageManager {
            
            public StorageManagerNull() {}
            
            public StorageManagerNull(IWardStorageManager other) {}
            
            @Override
            public byte getStorageID() {
                return 0;
            }
            
            @Override
            public int getNumCurrentOwners() {
                return 0;
            }
            
            @Override
            public void addOwner(UUID owner) {
                throw new IndexOutOfBoundsException("Attempted to exceed owner storage capacity");
            }
            
            @Override
            public boolean isOwner(UUID owner) {
                return false;
            }
            
            @Override
            public void removeOwner(UUID owner) {}
            
            @Override
            public int getMaxAllowedOwners() {
                return 0;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                return EMPTY_UUID;
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {}
            
            @Override
            public UUID[] getOwners() {
                return new UUID[0];
            }
            
            @Override
            public NBTTagCompound serialize() {
                return new NBTTagCompound();
            }
            
            @Override
            public void deserialize(NBTTagCompound tag) {}
            
        }
        
        public static class StorageManager1Bit implements IWardStorageManager {
            
            private byte[] data;
            private UUID owner;
            
            public StorageManager1Bit() {
                data = new byte[CHUNK_DATA_SIZE / 8];
                owner = EMPTY_UUID;
            }
            
            public StorageManager1Bit(IWardStorageManager other) {
                this();
                if (other.getMaxAllowedOwners() > 0) {
                    owner = other.getOwners()[0];
                    MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
                    for (int x = 0; x < CHUNK_X_SIZE; ++x) {
                        for (int z = 0; z < CHUNK_Z_SIZE; ++z) {
                            pos.setPos(x, 0, z);
                            for (int y = 0; y < CHUNK_Y_SIZE; ++y) {
                                pos.setY(y);
                                setOwner(pos, other.getOwner(pos));
                            }
                        }
                    }
                }
            }
            
            @Override
            public byte getStorageID() {
                return 1;
            }
            
            @Override
            public int getNumCurrentOwners() {
                return owner != null ? 1 : 0;
            }
            
            @Override
            public void addOwner(UUID owner) {
                if (this.owner.equals(EMPTY_UUID))
                    this.owner = owner;
                else
                    throw new IndexOutOfBoundsException("Attempted to exceed owner storage capacity");
            }
            
            @Override
            public boolean isOwner(UUID owner) {
                return owner.equals(this.owner);
            }
            
            @Override
            public void removeOwner(UUID owner) {
                if (this.owner.equals(owner))
                    this.owner = EMPTY_UUID;
                
                Arrays.fill(data, (byte) 0);
            }
            
            @Override
            public int getMaxAllowedOwners() {
                return 1;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + pos.getY() * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                return (data[index / 8] & (1 << (index % 8))) != 0 ? owner : EMPTY_UUID;
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {
                int index = (pos.getX() & 15) + pos.getY() * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                data[index / 8] = owner.equals(this.owner) && !owner.equals(EMPTY_UUID) ? (byte) (data[index / 8] | (1 << (index % 8))) : 
                    (byte) (data[index / 8] & ~(1 << (index % 8)));
            }
            
            @Override
            public UUID[] getOwners() {
                return new UUID[] {owner};
            }
            
            @Override
            public NBTTagCompound serialize() {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setUniqueId("0", owner);
                tag.setByteArray("d", data);
                return tag;
            }
            
            @Override
            public void deserialize(NBTTagCompound tag) {
                owner = tag.getUniqueId("0");
                data = tag.getByteArray("d");
                if (data.length != CHUNK_DATA_SIZE / 8)
                    throw new RuntimeException("Invalid ward data length");
            }
            
        }
        
        public static class StorageManager2Bits implements IWardStorageManager {
            
            private byte[] data;
            private UUID[] owners;
            private Object2ByteOpenHashMap<UUID> reverseMap;
            
            public StorageManager2Bits() {
                data = new byte[CHUNK_DATA_SIZE / 4];
                owners = new UUID[getMaxAllowedOwners()];
                reverseMap = new Object2ByteOpenHashMap<>();
                for (int i = 0; i < owners.length; ++i)
                    owners[i] = EMPTY_UUID;
            }
            
            public StorageManager2Bits(IWardStorageManager other) {
                this();
                if (other.getMaxAllowedOwners() > 0) {
                    for (int i = 0; i < Math.min(owners.length, other.getOwners().length); ++i) {
                        owners[i] = other.getOwners()[i];
                        reverseMap.put(owners[i], (byte) i);
                    }
                    
                    MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
                    for (int x = 0; x < CHUNK_X_SIZE; ++x) {
                        for (int z = 0; z < CHUNK_Z_SIZE; ++z) {
                            pos.setPos(x, 0, z);
                            for (int y = 0; y < CHUNK_Y_SIZE; ++y) {
                                pos.setY(y);
                                setOwner(pos, other.getOwner(pos));
                            }
                        }
                    }
                }
            }
            
            @Override
            public byte getStorageID() {
                return 2;
            }
            
            @Override
            public int getNumCurrentOwners() {
                return reverseMap.size();
            }
            
            @Override
            public void addOwner(UUID owner) {
                for (int i = 0; i < owners.length; ++i) {
                    if (owners[i].equals(EMPTY_UUID)) {
                        owners[i] = owner;
                        reverseMap.put(owner, (byte) i);
                        return;
                    }
                }
                
                throw new IndexOutOfBoundsException("Attempted to exceed owner storage capacity");
            }
            
            @Override
            public boolean isOwner(UUID owner) {
                return reverseMap.containsKey(owner);
            }
            
            @Override
            public void removeOwner(UUID owner) {
                owners[reverseMap.getByte(owner)] = EMPTY_UUID;
                reverseMap.removeByte(owner);
            }
            
            @Override
            public int getMaxAllowedOwners() {
                return 3;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + pos.getY() * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int result = ((data[index / 4] & (3 << (index % 4 * 2)))) >>> (index % 4 * 2);
                return result == 0 ? EMPTY_UUID : owners[result - 1];
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {
                int index = (pos.getX() & 15) + pos.getY() * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int toSet = !owner.equals(EMPTY_UUID) ? reverseMap.getByte(owner) + 1 : 0;
                
                data[index / 4] = (toSet & 1) != 0 ? (byte) (data[index / 4] | (1 << (index % 4 * 2))) : 
                    (byte) (data[index / 4] & ~(1 << (index % 4 * 2)));
                data[index / 4] = (toSet & 2) != 0 ? (byte) (data[index / 4] | (2 << (index % 4 * 2))) : 
                    (byte) (data[index / 4] & ~(2 << (index % 4 * 2)));
            }
            
            @Override
            public UUID[] getOwners() {
                return owners;
            }
            
            @Override
            public NBTTagCompound serialize() {
                NBTTagCompound tag = new NBTTagCompound();
                int ownerCount = 0;
                for (int i = 0; i < owners.length; ++i) {
                    if (!owners[i].equals(EMPTY_UUID)) {
                        tag.setUniqueId(Integer.toString(i), owners[i]);
                        ++ownerCount;
                    }
                    else
                        break;
                }
                
                tag.setInteger("o", ownerCount);
                tag.setByteArray("d", data);
                return tag;
            }
            
            @Override
            public void deserialize(NBTTagCompound tag) {
                for (int i = 0; i < tag.getInteger("o"); ++i) {
                    owners[i] = tag.getUniqueId(Integer.toString(i));
                    reverseMap.put(owners[i], (byte) i);
                }
                
                data = tag.getByteArray("d");
                if (data.length != CHUNK_DATA_SIZE / 4)
                    throw new RuntimeException("Invalid ward data length");
            }
            
        }
        
        public static class StorageManager4Bits implements IWardStorageManager {
            
            private byte[] data;
            private UUID[] owners;
            private Object2ByteOpenHashMap<UUID> reverseMap;
            
            public StorageManager4Bits() {
                data = new byte[CHUNK_DATA_SIZE / 2];
                owners = new UUID[getMaxAllowedOwners()];
                reverseMap = new Object2ByteOpenHashMap<>();
                for (int i = 0; i < owners.length; ++i)
                    owners[i] = EMPTY_UUID;
            }
            
            public StorageManager4Bits(IWardStorageManager other) {
                this();
                if (other.getMaxAllowedOwners() > 0) {
                    for (int i = 0; i < Math.min(owners.length, other.getOwners().length); ++i) {
                        owners[i] = other.getOwners()[i];
                        reverseMap.put(owners[i], (byte) i);
                    }
                    
                    MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
                    for (int x = 0; x < CHUNK_X_SIZE; ++x) {
                        for (int z = 0; z < CHUNK_Z_SIZE; ++z) {
                            pos.setPos(x, 0, z);
                            for (int y = 0; y < CHUNK_Y_SIZE; ++y) {
                                pos.setY(y);
                                setOwner(pos, other.getOwner(pos));
                            }
                        }
                    }
                }
            }
            
            @Override
            public byte getStorageID() {
                return 3;
            }
            
            @Override
            public int getNumCurrentOwners() {
                return reverseMap.size();
            }
            
            @Override
            public void addOwner(UUID owner) {
                for (int i = 0; i < owners.length; ++i) {
                    if (owners[i].equals(EMPTY_UUID)) {
                        owners[i] = owner;
                        reverseMap.put(owner, (byte) i);
                        return;
                    }
                }
                
                throw new IndexOutOfBoundsException("Attempted to exceed owner storage capacity");
            }
            
            @Override
            public boolean isOwner(UUID owner) {
                return reverseMap.containsKey(owner);
            }
            
            @Override
            public void removeOwner(UUID owner) {
                owners[reverseMap.getByte(owner)] = EMPTY_UUID;
                reverseMap.removeByte(owner);
            }
            
            @Override
            public int getMaxAllowedOwners() {
                return 15;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + pos.getY() * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int result = ((data[index / 2] & (15 << (index % 2 * 4)))) >>> (index % 2 * 4);
                return result == 0 ? EMPTY_UUID : owners[result - 1];
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {
                int index = (pos.getX() & 15) + pos.getY() * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int toSet = !owner.equals(EMPTY_UUID) ? reverseMap.getByte(owner) + 1 : 0;
                
                data[index / 2] = (toSet & 1) != 0 ? (byte) (data[index / 2] | (1 << (index % 2 * 4))) : 
                    (byte) (data[index / 2] & ~(1 << (index % 2 * 4)));
                data[index / 2] = (toSet & 2) != 0 ? (byte) (data[index / 2] | (2 << (index % 2 * 4))) : 
                    (byte) (data[index / 2] & ~(2 << (index % 2 * 4)));
                data[index / 2] = (toSet & 4) != 0 ? (byte) (data[index / 2] | (4 << (index % 2 * 4))) : 
                    (byte) (data[index / 2] & ~(4 << (index % 2 * 4)));
                data[index / 2] = (toSet & 8) != 0 ? (byte) (data[index / 2] | (8 << (index % 2 * 4))) : 
                    (byte) (data[index / 2] & ~(8 << (index % 2 * 4)));
            }
            
            @Override
            public UUID[] getOwners() {
                return owners;
            }
            
            @Override
            public NBTTagCompound serialize() {
                NBTTagCompound tag = new NBTTagCompound();
                int ownerCount = 0;
                for (int i = 0; i < owners.length; ++i) {
                    if (!owners[i].equals(EMPTY_UUID)) {
                        tag.setUniqueId(Integer.toString(i), owners[i]);
                        ++ownerCount;
                    }
                    else
                        break;
                }
                
                tag.setInteger("o", ownerCount);
                tag.setByteArray("d", data);
                return tag;
            }
            
            @Override
            public void deserialize(NBTTagCompound tag) {
                for (int i = 0; i < tag.getInteger("o"); ++i) {
                    owners[i] = tag.getUniqueId(Integer.toString(i));
                    reverseMap.put(owners[i], (byte) i);
                }
                
                data = tag.getByteArray("d");
                if (data.length != CHUNK_DATA_SIZE / 2)
                    throw new RuntimeException("Invalid ward data length");
            }
            
        }
        
        public static class StorageManagerByte implements IWardStorageManager {
            
            private byte[] data;
            private UUID[] owners;
            private Object2ByteOpenHashMap<UUID> reverseMap;
            
            public StorageManagerByte() {
                data = new byte[CHUNK_DATA_SIZE];
                owners = new UUID[getMaxAllowedOwners()];
                reverseMap = new Object2ByteOpenHashMap<>();
                for (int i = 0; i < owners.length; ++i)
                    owners[i] = EMPTY_UUID;
            }
            
            public StorageManagerByte(IWardStorageManager other) {
                this();
                if (other.getMaxAllowedOwners() > 0) {
                    for (int i = 0; i < Math.min(owners.length, other.getOwners().length); ++i) {
                        owners[i] = other.getOwners()[i];
                        reverseMap.put(owners[i], (byte) i);
                    }
                    
                    MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
                    for (int x = 0; x < CHUNK_X_SIZE; ++x) {
                        for (int z = 0; z < CHUNK_Z_SIZE; ++z) {
                            pos.setPos(x, 0, z);
                            for (int y = 0; y < CHUNK_Y_SIZE; ++y) {
                                pos.setY(y);
                                setOwner(pos, other.getOwner(pos));
                            }
                        }
                    }
                }
            }
            
            @Override
            public byte getStorageID() {
                return 4;
            }
            
            @Override
            public int getNumCurrentOwners() {
                return reverseMap.size();
            }
            
            @Override
            public void addOwner(UUID owner) {
                for (int i = 0; i < owners.length; ++i) {
                    if (owners[i].equals(EMPTY_UUID)) {
                        owners[i] = owner;
                        reverseMap.put(owner, (byte) i);
                        return;
                    }
                }
                
                throw new IndexOutOfBoundsException("Attempted to exceed owner storage capacity");
            }
            
            @Override
            public boolean isOwner(UUID owner) {
                return reverseMap.containsKey(owner);
            }
            
            @Override
            public void removeOwner(UUID owner) {
                owners[reverseMap.getByte(owner)] = EMPTY_UUID;
                reverseMap.removeByte(owner);
            }
            
            @Override
            public int getMaxAllowedOwners() {
                return 255;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + pos.getY() * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int result = data[index] + 128;
                return result == 0 ? EMPTY_UUID : owners[result - 1];
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {
                int index = (pos.getX() & 15) + pos.getY() * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                data[index] = (byte) ((!owner.equals(EMPTY_UUID) ? (byte) (reverseMap.getByte(owner) + 1) : 0) - 128);
            }
            
            @Override
            public UUID[] getOwners() {
                return owners;
            }
            
            @Override
            public NBTTagCompound serialize() {
                NBTTagCompound tag = new NBTTagCompound();
                int ownerCount = 0;
                for (int i = 0; i < owners.length; ++i) {
                    if (!owners[i].equals(EMPTY_UUID)) {
                        tag.setUniqueId(Integer.toString(i), owners[i]);
                        ++ownerCount;
                    }
                    else
                        break;
                }
                
                tag.setInteger("o", ownerCount);
                tag.setByteArray("d", data);
                return tag;
            }
            
            @Override
            public void deserialize(NBTTagCompound tag) {
                for (int i = 0; i < tag.getInteger("o"); ++i) {
                    owners[i] = tag.getUniqueId(Integer.toString(i));
                    reverseMap.put(owners[i], (byte) i);
                }
                
                data = tag.getByteArray("d");
                if (data.length != CHUNK_DATA_SIZE)
                    throw new RuntimeException("Invalid ward data length");
            }
            
        }
        
        public static class StorageManagerShort implements IWardStorageManager {
            
            private short[] data;
            private UUID[] owners;
            private Object2ShortOpenHashMap<UUID> reverseMap;
            
            public StorageManagerShort() {
                data = new short[CHUNK_DATA_SIZE];
                owners = new UUID[getMaxAllowedOwners()];
                reverseMap = new Object2ShortOpenHashMap<>();
                for (int i = 0; i < owners.length; ++i)
                    owners[i] = EMPTY_UUID;
            }
            
            public StorageManagerShort(IWardStorageManager other) {
                this();
                if (other.getMaxAllowedOwners() > 0) {
                    for (int i = 0; i < Math.min(owners.length, other.getOwners().length); ++i) {
                        owners[i] = other.getOwners()[i];
                        reverseMap.put(owners[i], (short) i);
                    }
                    
                    MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
                    for (int x = 0; x < CHUNK_X_SIZE; ++x) {
                        for (int z = 0; z < CHUNK_Z_SIZE; ++z) {
                            pos.setPos(x, 0, z);
                            for (int y = 0; y < CHUNK_Y_SIZE; ++y) {
                                pos.setY(y);
                                setOwner(pos, other.getOwner(pos));
                            }
                        }
                    }
                }
            }
            
            @Override
            public byte getStorageID() {
                return 5;
            }
            
            @Override
            public int getNumCurrentOwners() {
                return reverseMap.size();
            }
            
            @Override
            public void addOwner(UUID owner) {
                for (int i = 0; i < owners.length; ++i) {
                    if (owners[i].equals(EMPTY_UUID)) {
                        owners[i] = owner;
                        reverseMap.put(owner, (short) i);
                        return;
                    }
                }
                
                throw new IndexOutOfBoundsException("Attempted to exceed owner storage capacity");
            }
            
            @Override
            public boolean isOwner(UUID owner) {
                return reverseMap.containsKey(owner);
            }
            
            @Override
            public void removeOwner(UUID owner) {
                owners[reverseMap.getShort(owner)] = EMPTY_UUID;
                reverseMap.removeShort(owner);
            }
            
            @Override
            public int getMaxAllowedOwners() {
                return 65535;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + pos.getY() * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int result = data[index] + 32768;
                return result == 0 ? EMPTY_UUID : owners[result - 1];
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {
                int index = (pos.getX() & 15) + pos.getY() * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                data[index] = (short) ((!owner.equals(EMPTY_UUID) ? (reverseMap.getShort(owner) + 1) : 0) - 32768);
            }
            
            @Override
            public UUID[] getOwners() {
                return owners;
            }
            
            private byte[] createByteArray() {
                byte[] dest = new byte[data.length * 2];
                for (int i = 0; i < data.length; ++i) {
                    dest[i * 2] = (byte) (data[i] >>> 8);
                    dest[i * 2 + 1] = (byte) data[i];
                }
                
                return dest;
            }
            
            @Override
            public NBTTagCompound serialize() {
                NBTTagCompound tag = new NBTTagCompound();
                int ownerCount = 0;
                for (int i = 0; i < owners.length; ++i) {
                    if (!owners[i].equals(EMPTY_UUID)) {
                        tag.setUniqueId(Integer.toString(i), owners[i]);
                        ++ownerCount;
                    }
                    else
                        break;
                }
                
                tag.setInteger("o", ownerCount);
                tag.setByteArray("d", createByteArray());
                return tag;
            }
            
            private void loadCharArray(byte[] src) {
                data = new short[CHUNK_DATA_SIZE];
                for (int i = 0; i < data.length; ++i)
                    data[i] = (short) ((src[i * 2] << 8) + (src[i * 2 + 1] & 0xFF));
            }
            
            @Override
            public void deserialize(NBTTagCompound tag) {
                for (int i = 0; i < tag.getInteger("o"); ++i) {
                    owners[i] = tag.getUniqueId(Integer.toString(i));
                    reverseMap.put(owners[i], (short) i);
                }
                
                loadCharArray(tag.getByteArray("d"));
                if (data.length != CHUNK_DATA_SIZE)
                    throw new RuntimeException("Invalid ward data length");
            }
            
        }
        
    }
    
    protected StorageManagers.IWardStorageManager manager;
    
    public WardStorageServer() {
        manager = new StorageManagers.StorageManager1Bit();
    }
    
    @VisibleForTesting
    WardStorageServer(StorageManagers.IWardStorageManager toSet) {
        manager = toSet;
    }
    
    @Override
    public int getTotalWardOwners() {
        return manager.getNumCurrentOwners();
    }
    
    @Override
    public UUID getWard(BlockPos pos) {
        return manager.getOwner(pos);
    }
    
    @Override
    public boolean isWardOwner(UUID id) {
        return manager.isOwner(id);
    }
    
    @Override
    public boolean hasWard(BlockPos pos) {
        return !getWard(pos).equals(EMPTY_UUID);
    }
    
    @Override
    public void clearWard(BlockPos pos) {
        clearWard(null, pos);
    }
    
    @Override
    public void clearWard(World syncTo, BlockPos pos) {
        manager.setOwner(pos, EMPTY_UUID);
        if (syncTo != null)
            WardSyncManager.markPosForClear(syncTo, pos);
    }
    
    protected StorageManagers.IWardStorageManager createIncreasedSizeManager() {
        switch (manager.getStorageID()) {
            case 0: return new StorageManagers.StorageManager1Bit(manager);
            case 1: return new StorageManagers.StorageManager2Bits(manager);
            case 2: return new StorageManagers.StorageManager4Bits(manager);
            case 3: return new StorageManagers.StorageManagerByte(manager);
            case 4: return new StorageManagers.StorageManagerShort(manager);
            default: throw new RuntimeException("Invalid ward storage manager growth (other mod interacting?)");
        }
    }
    
    protected void evaluateManagerSizeDecrease() {
        if (manager.getMaxAllowedOwners() != 0 && manager.getNumCurrentOwners() == 0)
            manager = new StorageManagers.StorageManagerNull(manager);
        else if (manager.getMaxAllowedOwners() > 1 && manager.getNumCurrentOwners() <= 1)
            manager = new StorageManagers.StorageManager1Bit(manager);
        else if (manager.getMaxAllowedOwners() > 3 && manager.getNumCurrentOwners() <= 3)
            manager = new StorageManagers.StorageManager2Bits(manager);
        else if (manager.getMaxAllowedOwners() > 15 && manager.getNumCurrentOwners() <= 15)
            manager = new StorageManagers.StorageManager4Bits(manager);
        else if (manager.getMaxAllowedOwners() > 255 && manager.getNumCurrentOwners() <= 255)
            manager = new StorageManagers.StorageManagerByte(manager);
    }
    
    @Override
    public void setWard(BlockPos pos, UUID owner) {
        setWard(null, pos, owner);
    }
    
    @Override
    public void setWard(World syncTo, BlockPos pos, UUID owner) {
        if (!manager.isOwner(owner)) {
            if (manager.getNumCurrentOwners() == manager.getMaxAllowedOwners())
                manager = createIncreasedSizeManager();
            
            manager.addOwner(owner);
        }
        
        manager.setOwner(pos, owner);
        if (syncTo != null)
            WardSyncManager.markPosForNewOwner(syncTo, pos, owner);
    }
    
    @Override
    public NBTTagCompound fullSyncToClient(Chunk chunk, UUID player) {
        NBTTagCompound tag = new NBTTagCompound();
        boolean hasOwners = manager.getMaxAllowedOwners() != 0;
        tag.setBoolean("o", hasOwners);
        if (hasOwners) {
            byte[] data = new byte[StorageManagers.CHUNK_DATA_SIZE / 4];
            MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
            for (int x = 0; x < StorageManagers.CHUNK_X_SIZE; ++x) {
                for (int z = 0; z < StorageManagers.CHUNK_Z_SIZE; ++z) {
                    pos.setPos(x, 0, z);
                    for (int y = 0; y < StorageManagers.CHUNK_Y_SIZE; ++y) {
                        pos.setY(y);
                        byte toSet = 0;
                        UUID owner = manager.getOwner(pos);
                        if (owner.equals(player))
                            toSet = 1;
                        else if (!owner.equals(IWardStorageServer.EMPTY_UUID))
                            toSet = 2;
                        
                        int index = (pos.getX() & 15) + pos.getY() * StorageManagers.CHUNK_X_SIZE + (pos.getZ() & 15) * 
                                StorageManagers.CHUNK_X_SIZE * StorageManagers.CHUNK_Y_SIZE;
                        data[index / 4] = (toSet & 1) != 0 ? (byte) (data[index / 4] | (1 << (index % 4 * 2))) : 
                            (byte) (data[index / 4] & ~(1 << (index % 4 * 2)));
                        data[index / 4] = (toSet & 2) != 0 ? (byte) (data[index / 4] | (2 << (index % 4 * 2))) : 
                            (byte) (data[index / 4] & ~(2 << (index % 4 * 2)));
                    }
                }
            }
            
            tag.setByteArray("d", data);
            tag.setInteger("x", chunk.x);
            tag.setInteger("z", chunk.z);
        }
        
        return tag;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        evaluateManagerSizeDecrease();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("s", manager.getStorageID());
        tag.setTag("d", manager.serialize());
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        byte id = nbt.getByte("s");
        switch (id) {
            case 0: {
                manager = new StorageManagers.StorageManagerNull();
                break;
            }
            case 1: {
                manager = new StorageManagers.StorageManager1Bit();
                break;
            }
            case 2: {
                manager = new StorageManagers.StorageManager2Bits();
                break;
            }
            case 3: {
                manager = new StorageManagers.StorageManager4Bits();
                break;
            }
            case 4: {
                manager = new StorageManagers.StorageManagerByte();
                break;
            }
            case 5: {
                manager = new StorageManagers.StorageManagerShort();
                break;
            }
            default: throw new RuntimeException("Invalid chunk ward storage manager ID");
        }
        
        manager.deserialize(nbt.getCompoundTag("d"));
    }
    
}
