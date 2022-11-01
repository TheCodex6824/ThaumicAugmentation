/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;

import java.util.Set;

/*
 * Config option class for Integer Set values.
 * @author TheCodex6824
 */
public class ConfigOptionIntSet extends ConfigOption<Set<Integer>> {

    protected Set<Integer> value;

    public ConfigOptionIntSet(boolean enforceServer, Set<Integer> defaultValue) {
        super(enforceServer);
        value = defaultValue;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(value.size());
        for (int i : value)
            buf.writeInt(i);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        int size = buf.readInt();
        ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
        for (int i = 0; i < size; ++i)
            builder.add(buf.readInt());
        
        value = builder.build();
    }

    @Override
    public Set<Integer> getValue() {
        return value;
    }

    @Override
    public void setValue(Set<Integer> value) {
        this.value = value;
    }

}
