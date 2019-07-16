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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageServer.StorageManagersServer.IWardStorageManagerServer;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageServer.StorageManagersServer.StorageManager1Bit;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageServer.StorageManagersServer.StorageManager2Bits;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageServer.StorageManagersServer.StorageManager4Bits;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageServer.StorageManagersServer.StorageManagerByte;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageServer.StorageManagersServer.StorageManagerNull;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageServer.StorageManagersServer.StorageManagerShort;

public class TestWardStorage {
    
    private HashMap<BlockPos, UUID> data;
    private IWardStorageManagerServer manager;
    
    private void sharedTest(int numPlayers) {
        WardStorageServer storage = new WardStorageServer(manager);
        
        UUID[] players = new UUID[numPlayers + 1];
        players[0] = IWardStorageServer.NIL_UUID;
        for (int i = 1; i < players.length; ++i)
            players[i] = UUID.randomUUID();
        
        data = new HashMap<>(16 * 16 * 256);
        Random rand = new Random();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                for (int y = 0; y < 256; ++y)
                    data.put(new BlockPos(x, y, z), players[rand.nextInt(players.length)]);
            }
        }
        
        for (Map.Entry<BlockPos, UUID> entry : data.entrySet()) {
            if (!entry.getValue().equals(IWardStorageServer.NIL_UUID))
                storage.setWard(entry.getKey(), entry.getValue());
            else
                storage.clearWard(entry.getKey());
        }
        
        for (Map.Entry<BlockPos, UUID> entry : data.entrySet())
            assertEquals("UUID compare fail @ " + entry.getKey(), entry.getValue(), storage.getWard(entry.getKey()));
        
        NBTTagCompound serialized = storage.serializeNBT();
        storage.deserializeNBT(serialized);
        
        for (Map.Entry<BlockPos, UUID> entry : data.entrySet())
            assertEquals("UUID compare fail @ " + entry.getKey(), entry.getValue(), storage.getWard(entry.getKey()));
    }
    
    @Test
    public void testNull() {
        manager = new StorageManagerNull();
        sharedTest(manager.getMaxAllowedOwners());
    }
    
    @Test
    public void test1Bit() {
        manager = new StorageManager1Bit();
        sharedTest(manager.getMaxAllowedOwners());
    }
    
    @Test
    public void test2Bits() {
        manager = new StorageManager2Bits();
        sharedTest(manager.getMaxAllowedOwners());
    }
    
    @Test
    public void test4Bits() {
        manager = new StorageManager4Bits();
        sharedTest(manager.getMaxAllowedOwners());
    }
    
    @Test
    public void testByte() {
        manager = new StorageManagerByte();
        sharedTest(manager.getMaxAllowedOwners());
    }
    
    @Test
    public void testShort() {
        manager = new StorageManagerShort();
        sharedTest(manager.getMaxAllowedOwners());
    }
    
    @Test
    public void testSizeIncrease() {
        WardStorageServer storage = new WardStorageServer(new StorageManager2Bits());
        
        UUID[] players = new UUID[123];
        players[0] = IWardStorageServer.NIL_UUID;
        for (int i = 1; i < players.length; ++i)
            players[i] = UUID.randomUUID();
        
        data = new HashMap<>(16 * 16 * 256);
        Random rand = new Random();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                for (int y = 0; y < 256; ++y)
                    data.put(new BlockPos(x, y, z), players[rand.nextInt(players.length)]);
            }
        }
        
        for (Map.Entry<BlockPos, UUID> entry : data.entrySet()) {
            if (!entry.getValue().equals(IWardStorageServer.NIL_UUID))
                storage.setWard(entry.getKey(), entry.getValue());
            else
                storage.clearWard(entry.getKey());
        }
        
        assertEquals(StorageManagerByte.class, storage.manager.getClass());
        
        for (Map.Entry<BlockPos, UUID> entry : data.entrySet())
            assertEquals("UUID compare fail @ " + entry.getKey(), entry.getValue(), storage.getWard(entry.getKey()));
        
        NBTTagCompound serialized = storage.serializeNBT();
        storage.deserializeNBT(serialized);
        
        for (Map.Entry<BlockPos, UUID> entry : data.entrySet())
            assertEquals("UUID compare fail @ " + entry.getKey(), entry.getValue(), storage.getWard(entry.getKey()));
    }
    
    @Test
    public void testSizeReduction() {
        WardStorageServer storage = new WardStorageServer(new StorageManagerByte());
        
        UUID[] players = new UUID[13];
        players[0] = IWardStorageServer.NIL_UUID;
        for (int i = 1; i < players.length; ++i)
            players[i] = UUID.randomUUID();
        
        data = new HashMap<>(16 * 16 * 256);
        Random rand = new Random();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                for (int y = 0; y < 256; ++y)
                    data.put(new BlockPos(x, y, z), players[rand.nextInt(players.length)]);
            }
        }
        
        for (Map.Entry<BlockPos, UUID> entry : data.entrySet()) {
            if (!entry.getValue().equals(IWardStorageServer.NIL_UUID))
                storage.setWard(entry.getKey(), entry.getValue());
            else
                storage.clearWard(entry.getKey());
        }
        
        for (Map.Entry<BlockPos, UUID> entry : data.entrySet())
            assertEquals("UUID compare fail @ " + entry.getKey(), entry.getValue(), storage.getWard(entry.getKey()));
        
        NBTTagCompound serialized = storage.serializeNBT();
        storage.deserializeNBT(serialized);
        
        assertEquals(StorageManager4Bits.class, storage.manager.getClass());
        
        for (Map.Entry<BlockPos, UUID> entry : data.entrySet())
            assertEquals("UUID compare fail @ " + entry.getKey(), entry.getValue(), storage.getWard(entry.getKey()));
    }
    
}
