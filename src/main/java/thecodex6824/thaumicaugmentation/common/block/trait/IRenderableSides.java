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

package thecodex6824.thaumicaugmentation.common.block.trait;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.List;

public interface IRenderableSides {

    IUnlistedProperty<List<EnumFacing>> SIDES = new IUnlistedProperty<List<EnumFacing>>() {
        
        @Override
        public String getName() {
            return "ta_renderable_sides";
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public Class<List<EnumFacing>> getType() {
            return (Class<List<EnumFacing>>) (Class<?>) List.class;
        }
        
        @Override
        public boolean isValid(List<EnumFacing> value) {
            return value != null;
        }
        
        @Override
        public String valueToString(List<EnumFacing> value) {
            return value.toString();
        }
        
    };
    
}
