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

package thecodex6824.thaumicaugmentation.api.block.property;

import javax.annotation.Nullable;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

/*
 * Property interface for the type of sand in Thaumic Augmentation's sand.
 * @author TheCodex6824
 * 
 * @see thecodex6824.thaumicaugmentation.api.TABlocks#SAND
 */
public interface ITASandType {
    
    enum SandType implements IStringSerializable {
        
        SAND_MERCURIAL(0, MapColor.RED, 0xffff0000);
        
        private final int meta;
        private final MapColor color;
        private final int dustColor;
        
        SandType(int m, MapColor c, int d) {
            meta = m;
            color = c;
            dustColor = d;
        }
        
        public int getMeta() {
            return meta;
        }
        
        public MapColor getMapColor() {
            return color;
        }
        
        public int getDustColor() {
        	return dustColor;
        }
        
        @Override
        public String getName() {
            return name().toLowerCase();
        }
        
        @Nullable
        public static SandType fromMeta(int id) {
            for (SandType type : values()) {
                if (type.getMeta() == id)
                    return type;
            }
            
            return null;
        }
    }
    
    PropertyEnum<SandType> SAND_TYPE = PropertyEnum.create("ta_sand_type", SandType.class);
    
}
