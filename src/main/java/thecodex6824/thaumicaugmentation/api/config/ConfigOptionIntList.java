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

package thecodex6824.thaumicaugmentation.api.config;

import io.netty.buffer.ByteBuf;

/*
 * Config option class for int[] values.
 * @author TheCodex6824
 */
public class ConfigOptionIntList extends ConfigOption<int[]> {

    protected int[] value;

    public ConfigOptionIntList(boolean enforceServer, int[] defaultValue) {
        super(enforceServer);
        value = defaultValue;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(value.length);
        for (int i : value)
            buf.writeInt(i);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        int size = buf.readInt();
        value = new int[Math.min(size, 256)];
        for (int i = 0; i < value.length; ++i)
            value[i] = buf.readInt();
    }

    @Override
    public int[] getValue() {
        return value;
    }

    @Override
    public void setValue(int[] value) {
        this.value = value;
    }

}
