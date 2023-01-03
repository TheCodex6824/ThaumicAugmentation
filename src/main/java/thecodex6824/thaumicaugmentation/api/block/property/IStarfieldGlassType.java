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

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;
import thecodex6824.thaumicaugmentation.common.util.ShaderType;

import javax.annotation.Nullable;

public interface IStarfieldGlassType {

    enum GlassType implements IStringSerializable {
        
        GLASS_RIFT(0),
        GLASS_FRACTURE(1),
        GLASS_MIRROR(2);
        
        private final int meta;
        
        GlassType(int m) {
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
        public static GlassType fromMeta(int id) {
            for (GlassType type : values()) {
                if (type.getMeta() == id)
                    return type;
            }
            
            return null;
        }
        
        @Nullable
        public ShaderType getShaderType() {
            for (ShaderType type : ShaderType.values()) {
                if (type.getIndex() == meta)
                    return type;
            }
            
            return null;
        }
        
    }
    
    PropertyEnum<GlassType> GLASS_TYPE = PropertyEnum.create("ta_glass_type", GlassType.class);
    
}
