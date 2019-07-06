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

package thecodex6824.thaumicaugmentation.common.entity;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public class EntityUtil {
    
    public static final DataSerializer<Long> SERIALIZER_LONG = new DataSerializer<Long>() {

        @Override
        public Long copyValue(Long value) {
            return value;
        }
        
        @Override
        public DataParameter<Long> createKey(int id) {
            return new DataParameter<>(id, this);
        }
        
        @Override
        public Long read(PacketBuffer buf) throws IOException {
            return buf.readLong();
        }
        
        @Override
        public void write(PacketBuffer buf, Long value) {
            buf.writeLong(value);
        }
        
    };
}
