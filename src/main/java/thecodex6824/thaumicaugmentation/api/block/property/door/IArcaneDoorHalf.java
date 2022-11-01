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

package thecodex6824.thaumicaugmentation.api.block.property.door;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

/*
 * Property interface marking the upper and lower half of the Arcane Door.
 * @author TheCodex6824
 */
public interface IArcaneDoorHalf {

    enum ArcaneDoorHalf implements IStringSerializable {
        
        UPPER,
        LOWER;
        
        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }
    
    PropertyEnum<ArcaneDoorHalf> DOOR_HALF = PropertyEnum.create(
            "ta_door_half", ArcaneDoorHalf.class);
    
}
