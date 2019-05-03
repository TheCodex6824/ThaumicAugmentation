/**
 *	Thaumic Augmentation
 *	Copyright (c) 2019 TheCodex6824.
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

public class ConfigOptionInt extends ConfigOption<Integer> {

	protected int value;
	
	public ConfigOptionInt(boolean enforceServer, Integer defaultValue) {
		super(enforceServer);
		value = defaultValue;
	}
	
	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(value);
	}
	
	@Override
	public void deserialize(ByteBuf buf) {
		value = buf.readInt();
	};
	
	@Override
	public Integer getValue() {
		return value;
	}
	
	@Override
	public void setValue(Integer value) {
		this.value = value;
	}
	
}
