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

import io.netty.buffer.ByteBuf;

/**
 * Config option class for int[] values.
 * @author TheCodex6824
 */
public class ConfigOptionStringList extends ConfigOption<String[]> {

    protected String[] value;

    public ConfigOptionStringList(boolean enforceServer, String[] defaultValue) {
        super(enforceServer);
        value = defaultValue;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(value.length);
        for (String i : value) {
            byte[] encoded = i.getBytes(StandardCharsets.UTF_8);
            buf.writeInt(encoded.length);
            buf.writeBytes(encoded);
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        int size = buf.readInt();
        value = new String[Math.min(size, 256)];
        for (int i = 0; i < value.length; ++i) {
            byte[] data = new byte[Math.min(buf.readInt(), Short.MAX_VALUE)];
            value[i] = new String(data, StandardCharsets.UTF_8);
        }
    }

    @Override
    public String[] getValue() {
        return value;
    }

    @Override
    public void setValue(String[] value) {
        this.value = value;
    }

}
