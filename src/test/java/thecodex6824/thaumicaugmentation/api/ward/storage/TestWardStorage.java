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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import thecodex6824.thaumicaugmentation.api.ward.storage.WardStorageServer.StorageManagersServer.IWardStorageManagerServer;
import thecodex6824.thaumicaugmentation.api.ward.storage.WardStorageServer.StorageManagersServer.StorageManager1Bit;
import thecodex6824.thaumicaugmentation.api.ward.storage.WardStorageServer.StorageManagersServer.StorageManager2Bits;
import thecodex6824.thaumicaugmentation.api.ward.storage.WardStorageServer.StorageManagersServer.StorageManager4Bits;
import thecodex6824.thaumicaugmentation.api.ward.storage.WardStorageServer.StorageManagersServer.StorageManagerByte;
import thecodex6824.thaumicaugmentation.api.ward.storage.WardStorageServer.StorageManagersServer.StorageManagerNull;
import thecodex6824.thaumicaugmentation.api.ward.storage.WardStorageServer.StorageManagersServer.StorageManagerShort;

public class TestWardStorage {

    /**
     * The default Java UUID factory uses SecureRandom,
     * which is nice and all but we aren't using these
     * for secure purposes
     */
    private UUID fastGenerateUUID() {
	long most = ThreadLocalRandom.current().nextLong();
	long least = ThreadLocalRandom.current().nextLong();

	most &= ~0xF000;
	most |= 0x4000;
	least &= 0x0FFFFFFFFFFFFFFFL;
	least |= 0x8000000000000000L;
	return new UUID(most, least);
    }

    private void sharedTest(IWardStorageManagerServer manager) {
	WardStorageServer storage = new WardStorageServer(manager);

	UUID[] players = new UUID[manager.getMaxAllowedOwners() + 1];
	players[0] = IWardStorageServer.NIL_UUID;
	for (int i = 1; i < players.length; ++i)
	    players[i] = fastGenerateUUID();

	HashMap<BlockPos, UUID> data = new HashMap<>(16 * 16 * 256);
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
	    assertEquals(entry.getValue(), storage.getWard(entry.getKey()), "UUID compare fail @ " + entry.getKey());

	NBTTagCompound serialized = storage.serializeNBT();
	storage.deserializeNBT(serialized);

	for (Map.Entry<BlockPos, UUID> entry : data.entrySet())
	    assertEquals(entry.getValue(), storage.getWard(entry.getKey()), "UUID compare fail @ " + entry.getKey());
    }

    @Test
    public void testNull() {
	sharedTest(new StorageManagerNull());
    }

    @Test
    public void test1Bit() {
	sharedTest(new StorageManager1Bit());
    }

    @Test
    public void test2Bits() {
	sharedTest(new StorageManager2Bits());
    }

    @Test
    public void test4Bits() {
	sharedTest(new StorageManager4Bits());
    }

    @Test
    public void testByte() {
	sharedTest(new StorageManagerByte());
    }

    @Test
    public void testShort() {
	sharedTest(new StorageManagerShort());
    }

    @Test
    public void testSizeIncrease() {
	WardStorageServer storage = new WardStorageServer(new StorageManager2Bits());

	UUID[] players = new UUID[123];
	players[0] = IWardStorageServer.NIL_UUID;
	for (int i = 1; i < players.length; ++i)
	    players[i] = fastGenerateUUID();

	HashMap<BlockPos, UUID> data = new HashMap<>(16 * 16 * 256);
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
	    assertEquals(entry.getValue(), storage.getWard(entry.getKey()), "UUID compare fail @ " + entry.getKey());

	NBTTagCompound serialized = storage.serializeNBT();
	storage.deserializeNBT(serialized);

	for (Map.Entry<BlockPos, UUID> entry : data.entrySet())
	    assertEquals(entry.getValue(), storage.getWard(entry.getKey()), "UUID compare fail @ " + entry.getKey());
    }

    @Test
    public void testSizeReduction() {
	WardStorageServer storage = new WardStorageServer(new StorageManagerByte());

	UUID[] players = new UUID[13];
	players[0] = IWardStorageServer.NIL_UUID;
	for (int i = 1; i < players.length; ++i)
	    players[i] = fastGenerateUUID();

	HashMap<BlockPos, UUID> data = new HashMap<>(16 * 16 * 256);
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
	    assertEquals(entry.getValue(), storage.getWard(entry.getKey()), "UUID compare fail @ " + entry.getKey());

	NBTTagCompound serialized = storage.serializeNBT();
	storage.deserializeNBT(serialized);

	assertEquals(StorageManager4Bits.class, storage.manager.getClass());

	for (Map.Entry<BlockPos, UUID> entry : data.entrySet())
	    assertEquals(entry.getValue(), storage.getWard(entry.getKey()), "UUID compare fail @ " + entry.getKey());
    }

    @Test
    public void testOwnerManagement1Bit() {
	WardStorageServer storage = new WardStorageServer(new StorageManager1Bit());

	UUID[] players = new UUID[storage.manager.getMaxAllowedOwners() + 1];
	players[0] = IWardStorageServer.NIL_UUID;
	for (int i = 1; i < players.length; ++i)
	    players[i] = fastGenerateUUID();

	HashMap<BlockPos, UUID> data = new HashMap<>(16 * 16 * 256);
	Random rand = new Random();
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		for (int y = 0; y < 256; ++y)
		    data.put(new BlockPos(x, y, z), players[rand.nextInt(players.length)]);
	    }
	}

	int count = 0;
	for (Map.Entry<BlockPos, UUID> entry : data.entrySet()) {
	    if (!entry.getValue().equals(IWardStorageServer.NIL_UUID)) {
		storage.setWard(entry.getKey(), entry.getValue());
		++count;
	    }
	    else
		storage.clearWard(entry.getKey());
	}

	assertEquals((long) ((StorageManager1Bit) storage.manager).count, count);

	MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    if (storage.getWard(pos).equals(players[1]))
			storage.clearWard(pos);
		}
	    }
	}

	assertFalse(storage.isWardOwner(players[1]));

	storage.setWard(new BlockPos(rand.nextInt(16), rand.nextInt(256), rand.nextInt(16)), players[1]);
	NBTTagCompound serialized = storage.serializeNBT();
	storage.deserializeNBT(serialized);

	count = 0;
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    UUID ward = storage.getWard(pos);
		    if (!ward.equals(IWardStorageServer.NIL_UUID))
			++count;
		}
	    }
	}

	assertEquals((long) ((StorageManager1Bit) storage.manager).count, count);

	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    if (storage.getWard(pos).equals(players[1]))
			storage.clearWard(pos);
		}
	    }
	}

	assertFalse(storage.isWardOwner(players[1]));
    }

    @Test
    public void testOwnerManagement2Bits() {
	WardStorageServer storage = new WardStorageServer(new StorageManager2Bits());

	UUID[] players = new UUID[storage.manager.getMaxAllowedOwners() + 1];
	players[0] = IWardStorageServer.NIL_UUID;
	for (int i = 1; i < players.length; ++i)
	    players[i] = fastGenerateUUID();

	HashMap<BlockPos, UUID> data = new HashMap<>(16 * 16 * 256);
	Random rand = new Random();
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		for (int y = 0; y < 256; ++y)
		    data.put(new BlockPos(x, y, z), players[rand.nextInt(players.length)]);
	    }
	}

	HashMap<UUID, Integer> counts = new HashMap<>();
	for (int i = 1; i < players.length; ++i)
	    counts.put(players[i], 0);

	for (Map.Entry<BlockPos, UUID> entry : data.entrySet()) {
	    if (!entry.getValue().equals(IWardStorageServer.NIL_UUID)) {
		storage.setWard(entry.getKey(), entry.getValue());
		counts.put(entry.getValue(), counts.get(entry.getValue()) + 1);
	    }
	    else
		storage.clearWard(entry.getKey());
	}

	for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
	    if (storage.isWardOwner(entry.getKey())) {
		int internalID = ((StorageManager2Bits) storage.manager).reverseMap.getByte(entry.getKey());
		assertEquals((long) ((StorageManager2Bits) storage.manager).counts[internalID], (long) entry.getValue());
	    }
	}

	HashSet<UUID> snapped = new HashSet<>();
	for (int i = 1; i < players.length; ++i) {
	    if (i == 1 || (rand.nextInt(5) == 0 && snapped.size() < 1))
		snapped.add(players[i]);
	}

	MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    if (snapped.contains(storage.getWard(pos)))
			storage.clearWard(pos);
		}
	    }
	}

	for (UUID id : snapped)
	    assertFalse(storage.isWardOwner(id));

	NBTTagCompound serialized = storage.serializeNBT();
	storage.deserializeNBT(serialized);

	counts.clear();
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    UUID ward = storage.getWard(pos);
		    if (!ward.equals(IWardStorageServer.NIL_UUID))
			counts.put(ward, counts.getOrDefault(ward, 0) + 1);
		}
	    }
	}

	for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
	    int internalID = ((StorageManager2Bits) storage.manager).reverseMap.getByte(entry.getKey());
	    assertEquals((long) ((StorageManager2Bits) storage.manager).counts[internalID], (long) entry.getValue());
	}

	snapped.clear();
	for (int i = 1; i < players.length; ++i) {
	    if (i == 1 || rand.nextInt(5) == 0)
		snapped.add(players[i]);
	}

	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    if (snapped.contains(storage.getWard(pos)))
			storage.clearWard(pos);
		}
	    }
	}

	for (UUID id : snapped)
	    assertFalse(storage.isWardOwner(id));
    }

    @Test
    public void testOwnerManagement4Bits() {
	WardStorageServer storage = new WardStorageServer(new StorageManager4Bits());

	UUID[] players = new UUID[storage.manager.getMaxAllowedOwners() + 1];
	players[0] = IWardStorageServer.NIL_UUID;
	for (int i = 1; i < players.length; ++i)
	    players[i] = fastGenerateUUID();

	HashMap<BlockPos, UUID> data = new HashMap<>(16 * 16 * 256);
	Random rand = new Random();
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		for (int y = 0; y < 256; ++y)
		    data.put(new BlockPos(x, y, z), players[rand.nextInt(players.length)]);
	    }
	}

	HashMap<UUID, Integer> counts = new HashMap<>();
	for (int i = 1; i < players.length; ++i)
	    counts.put(players[i], 0);

	for (Map.Entry<BlockPos, UUID> entry : data.entrySet()) {
	    if (!entry.getValue().equals(IWardStorageServer.NIL_UUID)) {
		storage.setWard(entry.getKey(), entry.getValue());
		counts.put(entry.getValue(), counts.get(entry.getValue()) + 1);
	    }
	    else
		storage.clearWard(entry.getKey());
	}

	for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
	    if (storage.isWardOwner(entry.getKey())) {
		int internalID = ((StorageManager4Bits) storage.manager).reverseMap.getByte(entry.getKey());
		assertEquals((long) ((StorageManager4Bits) storage.manager).counts[internalID], (long) entry.getValue());
	    }
	}

	HashSet<UUID> snapped = new HashSet<>();
	for (int i = 1; i < players.length; ++i) {
	    if (i == 1 || (rand.nextInt(5) == 0 && snapped.size() < 11))
		snapped.add(players[i]);
	}

	MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    if (snapped.contains(storage.getWard(pos)))
			storage.clearWard(pos);
		}
	    }
	}

	for (UUID id : snapped)
	    assertFalse(storage.isWardOwner(id));

	NBTTagCompound serialized = storage.serializeNBT();
	storage.deserializeNBT(serialized);

	counts.clear();
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    UUID ward = storage.getWard(pos);
		    if (!ward.equals(IWardStorageServer.NIL_UUID))
			counts.put(ward, counts.getOrDefault(ward, 0) + 1);
		}
	    }
	}

	for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
	    int internalID = ((StorageManager4Bits) storage.manager).reverseMap.getByte(entry.getKey());
	    assertEquals((long) ((StorageManager4Bits) storage.manager).counts[internalID], (long) entry.getValue());
	}

	snapped.clear();
	for (int i = 1; i < players.length; ++i) {
	    if (i == 1 || rand.nextInt(5) == 0)
		snapped.add(players[i]);
	}

	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    if (snapped.contains(storage.getWard(pos)))
			storage.clearWard(pos);
		}
	    }
	}

	for (UUID id : snapped)
	    assertFalse(storage.isWardOwner(id));
    }

    @Test
    public void testOwnerManagementByte() {
	WardStorageServer storage = new WardStorageServer(new StorageManagerByte());

	UUID[] players = new UUID[storage.manager.getMaxAllowedOwners() + 1];
	players[0] = IWardStorageServer.NIL_UUID;
	for (int i = 1; i < players.length; ++i)
	    players[i] = fastGenerateUUID();

	HashMap<BlockPos, UUID> data = new HashMap<>(16 * 16 * 256);
	int forcedIndex = 1;
	Random rand = new Random();
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		for (int y = 0; y < 256; ++y) {
		    data.put(new BlockPos(x, y, z), forcedIndex < 256 ? players[forcedIndex++] :
			players[rand.nextInt(players.length)]);
		}
	    }
	}

	HashMap<UUID, Integer> counts = new HashMap<>();
	for (int i = 1; i < players.length; ++i)
	    counts.put(players[i], 0);

	for (Map.Entry<BlockPos, UUID> entry : data.entrySet()) {
	    if (!entry.getValue().equals(IWardStorageServer.NIL_UUID)) {
		storage.setWard(entry.getKey(), entry.getValue());
		counts.put(entry.getValue(), counts.get(entry.getValue()) + 1);
	    }
	    else
		storage.clearWard(entry.getKey());
	}

	for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
	    if (storage.isWardOwner(entry.getKey())) {
		int internalID = ((StorageManagerByte) storage.manager).reverseMap.getByte(entry.getKey()) + 128;
		assertEquals((long) ((StorageManagerByte) storage.manager).counts[internalID], (long) entry.getValue());
	    }
	}

	HashSet<UUID> snapped = new HashSet<>();
	for (int i = 1; i < players.length; ++i) {
	    if (i == 1 || (rand.nextInt(5) == 0 && snapped.size() < 239))
		snapped.add(players[i]);
	}

	MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    if (snapped.contains(storage.getWard(pos)))
			storage.clearWard(pos);
		}
	    }
	}

	for (UUID id : snapped)
	    assertFalse(storage.isWardOwner(id));

	NBTTagCompound serialized = storage.serializeNBT();
	storage.deserializeNBT(serialized);

	counts.clear();
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    UUID ward = storage.getWard(pos);
		    if (!ward.equals(IWardStorageServer.NIL_UUID))
			counts.put(ward, counts.getOrDefault(ward, 0) + 1);
		}
	    }
	}

	for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
	    int internalID = ((StorageManagerByte) storage.manager).reverseMap.getByte(entry.getKey()) + 128;
	    assertEquals((long) ((StorageManagerByte) storage.manager).counts[internalID], (long) entry.getValue());
	}

	snapped.clear();
	for (int i = 1; i < players.length; ++i) {
	    if (i == 1 || rand.nextInt(5) == 0)
		snapped.add(players[i]);
	}

	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    if (snapped.contains(storage.getWard(pos)))
			storage.clearWard(pos);
		}
	    }
	}

	for (UUID id : snapped)
	    assertFalse(storage.isWardOwner(id));
    }

    @Test
    public void testOwnerManagementShort() {
	WardStorageServer storage = new WardStorageServer(new StorageManagerShort());

	UUID[] players = new UUID[storage.manager.getMaxAllowedOwners() + 1];
	players[0] = IWardStorageServer.NIL_UUID;
	for (int i = 1; i < players.length; ++i)
	    players[i] = fastGenerateUUID();

	HashMap<BlockPos, UUID> data = new HashMap<>(16 * 16 * 256);
	Random rand = new Random();
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		for (int y = 0; y < 256; ++y)
		    data.put(new BlockPos(x, y, z), players[rand.nextInt(players.length)]);
	    }
	}

	HashMap<UUID, Integer> counts = new HashMap<>();
	for (int i = 1; i < players.length; ++i)
	    counts.put(players[i], 0);

	for (Map.Entry<BlockPos, UUID> entry : data.entrySet()) {
	    if (!entry.getValue().equals(IWardStorageServer.NIL_UUID)) {
		storage.setWard(entry.getKey(), entry.getValue());
		counts.put(entry.getValue(), counts.getOrDefault(entry.getValue(), 0) + 1);
	    }
	    else
		storage.clearWard(entry.getKey());
	}

	for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
	    if (storage.isWardOwner(entry.getKey())) {
		int internalID = ((StorageManagerShort) storage.manager).reverseMap.getShort(entry.getKey()) + 32768;
		assertEquals((long) ((StorageManagerShort) storage.manager).counts[internalID], (long) entry.getValue());
	    }
	}

	HashSet<UUID> snapped = new HashSet<>();
	for (int i = 1; i < players.length; ++i) {
	    if (i == 1 || (rand.nextInt(5) == 0 && snapped.size() < 65279))
		snapped.add(players[i]);
	}

	MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    if (snapped.contains(storage.getWard(pos)))
			storage.clearWard(pos);
		}
	    }
	}

	for (UUID id : snapped)
	    assertFalse(storage.isWardOwner(id));

	NBTTagCompound serialized = storage.serializeNBT();
	storage.deserializeNBT(serialized);

	counts.clear();
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    UUID ward = storage.getWard(pos);
		    if (!ward.equals(IWardStorageServer.NIL_UUID))
			counts.put(ward, counts.getOrDefault(ward, 0) + 1);
		}
	    }
	}

	for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
	    int internalID = ((StorageManagerShort) storage.manager).reverseMap.getShort(entry.getKey()) + 32768;
	    assertEquals((long) ((StorageManagerShort) storage.manager).counts[internalID], (long) entry.getValue());
	}

	snapped.clear();
	for (int i = 1; i < players.length; ++i) {
	    if (i == 1 || rand.nextInt(5) == 0)
		snapped.add(players[i]);
	}

	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		pos.setPos(x, 0, z);
		for (int y = 0; y < 256; ++y) {
		    pos.setY(y);
		    if (snapped.contains(storage.getWard(pos)))
			storage.clearWard(pos);
		}
	    }
	}

	for (UUID id : snapped)
	    assertFalse(storage.isWardOwner(id));
    }

    @Test
    public void testClearAll() {
	WardStorageServer storage = new WardStorageServer(new StorageManager2Bits());

	UUID[] players = new UUID[storage.manager.getMaxAllowedOwners() + 1];
	players[0] = IWardStorageServer.NIL_UUID;
	for (int i = 1; i < players.length; ++i)
	    players[i] = fastGenerateUUID();

	HashMap<BlockPos, UUID> data = new HashMap<>(16 * 16 * 256);
	Random rand = new Random();
	for (int x = 0; x < 16; ++x) {
	    for (int z = 0; z < 16; ++z) {
		for (int y = 0; y < 256; ++y)
		    data.put(new BlockPos(x, y, z), players[rand.nextInt(players.length)]);
	    }
	}

	HashMap<UUID, Integer> counts = new HashMap<>();
	for (int i = 1; i < players.length; ++i)
	    counts.put(players[i], 0);

	for (Map.Entry<BlockPos, UUID> entry : data.entrySet()) {
	    if (!entry.getValue().equals(IWardStorageServer.NIL_UUID)) {
		storage.setWard(entry.getKey(), entry.getValue());
		counts.put(entry.getValue(), counts.get(entry.getValue()) + 1);
	    }
	    else
		storage.clearWard(entry.getKey());
	}

	for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
	    if (storage.isWardOwner(entry.getKey())) {
		int internalID = ((StorageManager2Bits) storage.manager).reverseMap.getByte(entry.getKey());
		assertEquals((long) ((StorageManager2Bits) storage.manager).counts[internalID], (long) entry.getValue());
	    }
	}

	storage.manager.clearAllOwnersAndWards();

	for (UUID id : players)
	    assertFalse(storage.isWardOwner(id));
    }

}
