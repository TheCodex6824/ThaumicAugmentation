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

import io.netty.buffer.ByteBuf;

/*
 * Config option class for Boolean values.
 * @author TheCodex6824
 */
public class ConfigOptionBoolean extends ConfigOption<Boolean> {

    protected boolean value;

    public ConfigOptionBoolean(boolean enforceServer, boolean defaultValue) {
        super(enforceServer);
        value = defaultValue;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(value);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        value = buf.readBoolean();
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

}
