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
import net.minecraftforge.fml.relauncher.Side;

public abstract class ConfigOption<T> {

	protected T value;
	protected boolean enforceServer;

	public ConfigOption(boolean enforceServer) {
		this.enforceServer = enforceServer;
	}

	public boolean shouldSyncValue(Side s) {
		if (s == Side.CLIENT)
			return enforceServer;

		return false;
	}
	
	public abstract void serialize(ByteBuf buf);

	public abstract void deserialize(ByteBuf buf);

	public abstract T getValue();

	public abstract void setValue(T value);

	public void setValue(T value, Side logicalSide) {
		if (!shouldSyncValue(logicalSide))
			setValue(value);
	}

}
