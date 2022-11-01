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

package thecodex6824.thaumicaugmentation.api.block.property;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nullable;

public interface IUrnType {

    enum UrnType implements IStringSerializable {
        
        URN_COMMON(0),
        URN_UNCOMMON(1),
        URN_RARE(2);
        
        private final int meta;
        
        UrnType(int m) {
            meta = m;
        }
        
        public int getMeta() {
            return meta;
        }
        
        @Override
        public String getName() {
            return name().toLowerCase();
        }
        
        @Nullable
        public static UrnType fromMeta(int id) {
            for (UrnType type : values()) {
                if (type.getMeta() == id)
                    return type;
            }
            
            return null;
        }
        
    }
    
    PropertyEnum<UrnType> URN_TYPE = PropertyEnum.create("ta_urn_type", UrnType.class);
    
}
