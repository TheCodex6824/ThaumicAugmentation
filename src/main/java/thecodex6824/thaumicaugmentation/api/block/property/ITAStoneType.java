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

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.common.lib.SoundsTC;

public interface ITAStoneType {

    public enum StoneType implements IStringSerializable {
        STONE_VOID(0, Material.ROCK, () -> SoundType.STONE),
        STONE_TAINT_NODECAY(1, ThaumcraftMaterials.MATERIAL_TAINT, () -> SoundsTC.GORE),
        SOIL_STONE_TAINT_NODECAY(2, ThaumcraftMaterials.MATERIAL_TAINT, () -> SoundsTC.GORE);
        
        private int meta;
        private Material mat;
        private Supplier<SoundType> sound;
        
        private StoneType(int m, Material mt, Supplier<SoundType> s) {
            meta = m;
            mat = mt;
            sound = s;
        }
        
        public int getMeta() {
            return meta;
        }
        
        public Material getMaterial() {
            return mat;
        }
        
        public SoundType getSoundType() {
            return sound.get();
        }
        
        @Override
        public String getName() {
            return name().toLowerCase();
        }
        
        @Nullable
        public static StoneType fromMeta(int id) {
            for (StoneType type : values()) {
                if (type.getMeta() == id)
                    return type;
            }
            
            return null;
        }
    }
    
    public static PropertyEnum<StoneType> STONE_TYPE = PropertyEnum.create("ta_stone_type", StoneType.class);
    
}
