/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;

/*
 * Holds the possible values for a block's ward state for the client side.
 * An enum instead of UUID is used because the client should not know who exactly
 * owns a ward if it's not them - just that it is warded.
 * @author TheCodex6824
 */
public enum ClientWardStorageValue {
    
    EMPTY((byte) 0),
    OWNED_SELF((byte) 1),
    OWNED_OTHER((byte) 2);
    
    private static final Byte2ObjectOpenHashMap<ClientWardStorageValue> LOOKUP = new Byte2ObjectOpenHashMap<>();
    
    static {
        LOOKUP.put(EMPTY.getID(), EMPTY);
        LOOKUP.put(OWNED_SELF.getID(), OWNED_SELF);
        LOOKUP.put(OWNED_OTHER.getID(), OWNED_OTHER);
    }
    
    private final byte id;
    
    ClientWardStorageValue(byte val) {
        id = val;
    }
    
    public byte getID() {
        return id;
    }
    
    public static ClientWardStorageValue fromID(byte id) {
        return LOOKUP.get(id);
    }
    
}
