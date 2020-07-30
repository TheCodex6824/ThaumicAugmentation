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

package thecodex6824.thaumicaugmentation.api.config;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import io.netty.buffer.ByteBuf;

/**
 * Config option class for Map{@literal <}String, Integer{@literal >} values.
 * @author TheCodex6824
 * @deprecated Forge config maps seem to be a hack using categories, and they have issues with removing
 * default entries. Avoid if possible.
 */
public class ConfigOptionStringToIntMap extends ConfigOption<Map<String, Integer>>{

    protected ImmutableMap<String, Integer> value;
    
    public ConfigOptionStringToIntMap(boolean enforceServer, Map<String, Integer> map) {
        super(enforceServer);
        value = ImmutableMap.copyOf(map);
    }
    
    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(value.size());
        value.forEach((String k, Integer v) -> {
            byte[] data = k.getBytes(StandardCharsets.UTF_8);
            buf.writeInt(data.length);
            buf.writeBytes(data);
            buf.writeInt(v);
        });
    }

    @Override
    public void deserialize(ByteBuf buf) {
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        int entries = buf.readInt();
        for (int i = 0; i < entries; ++i) {
            byte[] name = new byte[Math.min(buf.readInt(), 2097152)];
            buf.readBytes(name);
            builder.put(new String(name, StandardCharsets.UTF_8), buf.readInt());
        }
        
        value = builder.build();
    }

    @Override
    public Map<String, Integer> getValue() {
        return value;
    }

    @Override
    public void setValue(Map<String, Integer> value) {
        this.value = ImmutableMap.copyOf(value);
    }
    
}
