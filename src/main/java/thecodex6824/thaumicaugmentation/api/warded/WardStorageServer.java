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
import net.minecraftforge.common.MinecraftForge;
import scala.actors.threadpool.Arrays;
import thecodex6824.thaumicaugmentation.api.event.BlockWardEvent;

/**
 * Default implementation of {@link IWardStorage} for servers.
 * @author TheCodex6824
 */
public class WardStorageServer implements IWardStorageServer {

    @VisibleForTesting
    static final class StorageManagersServer {
        
        public static final int CHUNK_X_SIZE = 16;
        public static final int CHUNK_Y_SIZE = 256;
        public static final int CHUNK_Z_SIZE = 16;
        public static final int CHUNK_DATA_SIZE = CHUNK_X_SIZE * CHUNK_Y_SIZE * CHUNK_Z_SIZE;
        
        private StorageManagersServer() {}
        
        public static interface IWardStorageManagerServer {
            
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
        
        public static class StorageManagerNull implements IWardStorageManagerServer {
            
            public StorageManagerNull() {}
            
            public StorageManagerNull(IWardStorageManagerServer other) {}
            
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
                return NIL_UUID;
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
        
        public static class StorageManager1Bit implements IWardStorageManagerServer {
            
            private byte[] data;
            private UUID owner;
            
            public StorageManager1Bit() {
                data = new byte[CHUNK_DATA_SIZE / 8];
                owner = NIL_UUID;
            }
            
            public StorageManager1Bit(IWardStorageManagerServer other) {
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
                if (this.owner.equals(NIL_UUID))
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
                    this.owner = NIL_UUID;
                
                Arrays.fill(data, (byte) 0);
            }
            
            @Override
            public int getMaxAllowedOwners() {
                return 1;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                return (data[index / 8] & (1 << (index % 8))) != 0 ? owner : NIL_UUID;
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                data[index / 8] = owner.equals(this.owner) && !owner.equals(NIL_UUID) ? (byte) (data[index / 8] | (1 << (index % 8))) : 
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
        
        public static class StorageManager2Bits implements IWardStorageManagerServer {
            
            private byte[] data;
            private UUID[] owners;
            private Object2ByteOpenHashMap<UUID> reverseMap;
            
            public StorageManager2Bits() {
                data = new byte[CHUNK_DATA_SIZE / 4];
                owners = new UUID[getMaxAllowedOwners()];
                reverseMap = new Object2ByteOpenHashMap<>();
                for (int i = 0; i < owners.length; ++i)
                    owners[i] = NIL_UUID;
            }
            
            public StorageManager2Bits(IWardStorageManagerServer other) {
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
                    if (owners[i].equals(NIL_UUID)) {
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
                owners[reverseMap.getByte(owner)] = NIL_UUID;
                reverseMap.removeByte(owner);
            }
            
            @Override
            public int getMaxAllowedOwners() {
                return 3;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int result = ((data[index / 4] & (3 << (index % 4 * 2)))) >>> (index % 4 * 2);
                return result == 0 ? NIL_UUID : owners[result - 1];
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int toSet = !owner.equals(NIL_UUID) ? reverseMap.getByte(owner) + 1 : 0;
                
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
                    if (!owners[i].equals(NIL_UUID)) {
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
        
        public static class StorageManager4Bits implements IWardStorageManagerServer {
            
            private byte[] data;
            private UUID[] owners;
            private Object2ByteOpenHashMap<UUID> reverseMap;
            
            public StorageManager4Bits() {
                data = new byte[CHUNK_DATA_SIZE / 2];
                owners = new UUID[getMaxAllowedOwners()];
                reverseMap = new Object2ByteOpenHashMap<>();
                for (int i = 0; i < owners.length; ++i)
                    owners[i] = NIL_UUID;
            }
            
            public StorageManager4Bits(IWardStorageManagerServer other) {
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
                    if (owners[i].equals(NIL_UUID)) {
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
                owners[reverseMap.getByte(owner)] = NIL_UUID;
                reverseMap.removeByte(owner);
            }
            
            @Override
            public int getMaxAllowedOwners() {
                return 15;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int result = ((data[index / 2] & (15 << (index % 2 * 4)))) >>> (index % 2 * 4);
                return result == 0 ? NIL_UUID : owners[result - 1];
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int toSet = !owner.equals(NIL_UUID) ? reverseMap.getByte(owner) + 1 : 0;
                
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
                    if (!owners[i].equals(NIL_UUID)) {
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
        
        public static class StorageManagerByte implements IWardStorageManagerServer {
            
            private byte[] data;
            private UUID[] owners;
            private Object2ByteOpenHashMap<UUID> reverseMap;
            
            public StorageManagerByte() {
                data = new byte[CHUNK_DATA_SIZE];
                owners = new UUID[getMaxAllowedOwners()];
                reverseMap = new Object2ByteOpenHashMap<>();
                for (int i = 0; i < owners.length; ++i)
                    owners[i] = NIL_UUID;
            }
            
            public StorageManagerByte(IWardStorageManagerServer other) {
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
                    if (owners[i].equals(NIL_UUID)) {
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
                owners[reverseMap.getByte(owner)] = NIL_UUID;
                reverseMap.removeByte(owner);
            }
            
            @Override
            public int getMaxAllowedOwners() {
                return 255;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int result = data[index] + 128;
                return result == 0 ? NIL_UUID : owners[result - 1];
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                data[index] = (byte) ((!owner.equals(NIL_UUID) ? (byte) (reverseMap.getByte(owner) + 1) : 0) - 128);
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
                    if (!owners[i].equals(NIL_UUID)) {
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
        
        public static class StorageManagerShort implements IWardStorageManagerServer {
            
            private short[] data;
            private UUID[] owners;
            private Object2ShortOpenHashMap<UUID> reverseMap;
            
            public StorageManagerShort() {
                data = new short[CHUNK_DATA_SIZE];
                owners = new UUID[getMaxAllowedOwners()];
                reverseMap = new Object2ShortOpenHashMap<>();
                for (int i = 0; i < owners.length; ++i)
                    owners[i] = NIL_UUID;
            }
            
            public StorageManagerShort(IWardStorageManagerServer other) {
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
                    if (owners[i].equals(NIL_UUID)) {
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
                owners[reverseMap.getShort(owner)] = NIL_UUID;
                reverseMap.removeShort(owner);
            }
            
            @Override
            public int getMaxAllowedOwners() {
                return 65535;
            }
            
            @Override
            public UUID getOwner(BlockPos pos) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                int result = data[index] + 32768;
                return result == 0 ? NIL_UUID : owners[result - 1];
            }
            
            @Override
            public void setOwner(BlockPos pos, UUID owner) {
                int index = (pos.getX() & 15) + (pos.getY() & 255) * CHUNK_X_SIZE + (pos.getZ() & 15) * CHUNK_X_SIZE * CHUNK_Y_SIZE;
                data[index] = (short) ((!owner.equals(NIL_UUID) ? (reverseMap.getShort(owner) + 1) : 0) - 32768);
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
                    if (!owners[i].equals(NIL_UUID)) {
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
    
    protected StorageManagersServer.IWardStorageManagerServer manager;
    
    public WardStorageServer() {
        manager = new StorageManagersServer.StorageManagerNull();
    }
    
    @VisibleForTesting
    WardStorageServer(StorageManagersServer.IWardStorageManagerServer toSet) {
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
        return !getWard(pos).equals(NIL_UUID);
    }
    
    @VisibleForTesting
    void clearWard(BlockPos pos) {
        manager.setOwner(pos, NIL_UUID);
    }
    
    @Override
    public void clearWard(World syncTo, BlockPos pos) {
        BlockWardEvent.DewardedServer event = new BlockWardEvent.DewardedServer.Pre(syncTo, pos);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            manager.setOwner(pos, NIL_UUID);
            WardSyncManager.markPosForClear(syncTo, pos);
            MinecraftForge.EVENT_BUS.post(new BlockWardEvent.DewardedServer.Post(syncTo, pos));
        }
    }
    
    protected StorageManagersServer.IWardStorageManagerServer createIncreasedSizeManager() {
        switch (manager.getStorageID()) {
            case 0: return new StorageManagersServer.StorageManager1Bit(manager);
            case 1: return new StorageManagersServer.StorageManager2Bits(manager);
            case 2: return new StorageManagersServer.StorageManager4Bits(manager);
            case 3: return new StorageManagersServer.StorageManagerByte(manager);
            case 4: return new StorageManagersServer.StorageManagerShort(manager);
            default: throw new RuntimeException("Invalid ward storage manager growth (other mod interacting?)");
        }
    }
    
    protected void evaluateManagerSizeDecrease() {
        if (manager.getMaxAllowedOwners() != 0 && manager.getNumCurrentOwners() == 0)
            manager = new StorageManagersServer.StorageManagerNull(manager);
        else if (manager.getMaxAllowedOwners() > 1 && manager.getNumCurrentOwners() <= 1)
            manager = new StorageManagersServer.StorageManager1Bit(manager);
        else if (manager.getMaxAllowedOwners() > 3 && manager.getNumCurrentOwners() <= 3)
            manager = new StorageManagersServer.StorageManager2Bits(manager);
        else if (manager.getMaxAllowedOwners() > 15 && manager.getNumCurrentOwners() <= 15)
            manager = new StorageManagersServer.StorageManager4Bits(manager);
        else if (manager.getMaxAllowedOwners() > 255 && manager.getNumCurrentOwners() <= 255)
            manager = new StorageManagersServer.StorageManagerByte(manager);
    }
    
    @VisibleForTesting
    void setWard(BlockPos pos, UUID owner) {
        if (!manager.isOwner(owner)) {
            if (manager.getNumCurrentOwners() == manager.getMaxAllowedOwners())
                manager = createIncreasedSizeManager();
            
            manager.addOwner(owner);
        }
        
        manager.setOwner(pos, owner);
    }
    
    @Override
    public void setWard(World syncTo, BlockPos pos, UUID owner) {
        BlockWardEvent.WardedServer event = new BlockWardEvent.WardedServer.Pre(syncTo, pos, owner);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            if (!manager.isOwner(owner)) {
                if (manager.getNumCurrentOwners() == manager.getMaxAllowedOwners())
                    manager = createIncreasedSizeManager();
                
                manager.addOwner(owner);
            }
            
            manager.setOwner(pos, owner);
            WardSyncManager.markPosForNewOwner(syncTo, pos, owner);
            MinecraftForge.EVENT_BUS.post(new BlockWardEvent.WardedServer.Post(syncTo, pos, owner));
        }
    }
    
    @Override
    public NBTTagCompound fullSyncToClient(Chunk chunk, UUID player) {
        if (manager.getNumCurrentOwners() > 0) {
            NBTTagCompound tag = new NBTTagCompound();
            byte[] data = new byte[StorageManagersServer.CHUNK_DATA_SIZE / 4];
            MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
            for (int x = 0; x < StorageManagersServer.CHUNK_X_SIZE; ++x) {
                for (int z = 0; z < StorageManagersServer.CHUNK_Z_SIZE; ++z) {
                    pos.setPos(x, 0, z);
                    for (int y = 0; y < StorageManagersServer.CHUNK_Y_SIZE; ++y) {
                        pos.setY(y);
                        byte toSet = 0;
                        UUID owner = manager.getOwner(pos);
                        if (owner.equals(player))
                            toSet = 1;
                        else if (!owner.equals(IWardStorageServer.NIL_UUID))
                            toSet = 2;
                        
                        int index = (pos.getX() & 15) + (pos.getY() & 255) * StorageManagersServer.CHUNK_X_SIZE + (pos.getZ() & 15) * 
                                StorageManagersServer.CHUNK_X_SIZE * StorageManagersServer.CHUNK_Y_SIZE;
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
            return tag;
        }
        
        return null;
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
                manager = new StorageManagersServer.StorageManagerNull();
                break;
            }
            case 1: {
                manager = new StorageManagersServer.StorageManager1Bit();
                break;
            }
            case 2: {
                manager = new StorageManagersServer.StorageManager2Bits();
                break;
            }
            case 3: {
                manager = new StorageManagersServer.StorageManager4Bits();
                break;
            }
            case 4: {
                manager = new StorageManagersServer.StorageManagerByte();
                break;
            }
            case 5: {
                manager = new StorageManagersServer.StorageManagerShort();
                break;
            }
            default: throw new RuntimeException("Invalid chunk ward storage manager ID");
        }
        
        manager.deserialize(nbt.getCompoundTag("d"));
    }
    
}
