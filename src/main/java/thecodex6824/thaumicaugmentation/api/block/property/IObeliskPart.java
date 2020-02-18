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

import javax.annotation.Nullable;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public interface IObeliskPart {

    public enum ObeliskPart implements IStringSerializable {
        
        MIDDLE(0),
        INNER(1),
        CAP(2);
        
        private int meta;
        
        private ObeliskPart(int m) {
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
        public static ObeliskPart fromMeta(int id) {
            for (ObeliskPart type : values()) {
                if (type.getMeta() == id)
                    return type;
            }
            
            return null;
        }
        
    }
    
    public static final PropertyEnum<ObeliskPart> OBELISK_PART = PropertyEnum.create("ta_obelisk_part", ObeliskPart.class);
    
}
