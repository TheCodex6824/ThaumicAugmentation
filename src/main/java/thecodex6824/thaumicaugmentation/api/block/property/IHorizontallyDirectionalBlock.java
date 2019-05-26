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

package thecodex6824.thaumicaugmentation.api.block.property;

import java.util.Arrays;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.util.EnumFacing;

/**
 * Property interface for blocks that can be oriented in a horizontal direction,
 * such as chests and doors.
 * @author TheCodex6824
 *
 */
public interface IHorizontallyDirectionalBlock {

	public static final PropertyDirection DIRECTION = PropertyDirection.create("ta_direction_h", 
			Arrays.asList(EnumFacing.HORIZONTALS));

}
