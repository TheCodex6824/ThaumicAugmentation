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

package thecodex6824.thaumicaugmentation.api.block.property.door;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

/*
 * Property interface marking the type/meta of the Arcane Door.
 * @author TheCodex6824
 * @deprecated This was used in the old implementation of Arcane Doors, where they all shared a single block type.
 * Now that each door type is its own block, this should no longer be used.
 */
@Deprecated
public interface IArcaneDoorType {

    enum ArcaneDoorType implements IStringSerializable {

        WOOD,
        METAL;

        @Override
        public String getName() {
            return name().toLowerCase();
        }

    }

    PropertyEnum<ArcaneDoorType> TYPE =
            PropertyEnum.create("ta_doortype", ArcaneDoorType.class);

}
