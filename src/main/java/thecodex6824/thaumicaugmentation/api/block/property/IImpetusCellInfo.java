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

package thecodex6824.thaumicaugmentation.api.block.property;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.EnumFacing;

public interface IImpetusCellInfo {

    public static final PropertyInteger CELL_INFO = PropertyInteger.create("ta_cell_info", 0, 15);
    
    public static boolean isCellPresent(int prop, EnumFacing side) {
        if (side.getHorizontalIndex() > -1)
            return ((prop >> side.getHorizontalIndex()) & 1) == 1;
        else
            return false;
    }
    
    public static Set<EnumFacing> getCellDirections(int prop) {
        EnumSet<EnumFacing> ret = EnumSet.noneOf(EnumFacing.class);
        for (int i = 0; i < EnumFacing.HORIZONTALS.length; ++i) {
            if (((prop >> i) & 1) == 1)
                ret.add(EnumFacing.byHorizontalIndex(i));
        }
        
        return ret;
    }
    
    public static int getNumberOfCells(int prop) {
        return Integer.bitCount(prop & 15);
    }
    
    public static int setCellPresent(int prop, EnumFacing side) {
        if (side.getHorizontalIndex() > -1)
            return prop | (1 << side.getHorizontalIndex());
        else
            return prop;
    }
    
}
