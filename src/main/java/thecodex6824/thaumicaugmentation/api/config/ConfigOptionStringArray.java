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
import java.util.ArrayList;

import io.netty.buffer.ByteBuf;

public class ConfigOptionStringArray extends ConfigOption<String[]> {

    protected String[] value;

    public ConfigOptionStringArray(boolean enforceServer, String[] defaultValue) {
        super(enforceServer);
        value = new String[defaultValue.length];
        System.arraycopy(defaultValue, 0, value, 0, value.length);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(value.length);
        for (String s : value) {
            byte[] data = s.getBytes(StandardCharsets.UTF_8);
            buf.writeInt(data.length);
            buf.writeBytes(data);
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        ArrayList<String> list = new ArrayList<>();
        int entries = buf.readInt();
        for (int i = 0; i < entries; ++i) {
            byte[] str = new byte[Math.min(buf.readInt(), 2097152)];
            buf.readBytes(str);
            list.add(new String(str, StandardCharsets.UTF_8));
        }
        
        value = list.toArray(new String[list.size()]);
    }

    @Override
    public String[] getValue() {
        return value;
    }

    @Override
    public void setValue(String[] value) {
        this.value = new String[value.length];
        System.arraycopy(value, 0, this.value, 0, this.value.length);
    }

    
}
