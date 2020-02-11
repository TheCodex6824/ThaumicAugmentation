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
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import thecodex6824.thaumicaugmentation.api.TABlocks;

public interface ITASlabType {

    public enum SlabType implements IStringSerializable {
        ANCIENT_TILE(0, () -> Material.ROCK, () -> SoundType.STONE, () -> new ItemStack(TABlocks.SLAB, 1, 0), MapColor.ADOBE);
        
        private int meta;
        private Supplier<Material> mat;
        private Supplier<SoundType> sound;
        private Supplier<ItemStack> drop;
        private MapColor color;
        
        private SlabType(int m, Supplier<Material> mt, Supplier<SoundType> s, Supplier<ItemStack> b, MapColor c) {
            meta = m;
            mat = mt;
            sound = s;
            drop = b;
            color = c;
        }
        
        public int getMeta() {
            return meta;
        }
        
        public Material getMaterial() {
            return mat.get();
        }
        
        public SoundType getSoundType() {
            return sound.get();
        }
        
        public ItemStack getDrop() {
            return drop.get();
        }
        
        public MapColor getMapColor() {
            return color;
        }
        
        @Override
        public String getName() {
            return name().toLowerCase();
        }
        
        @Nullable
        public static SlabType fromMeta(int id) {
            for (SlabType type : values()) {
                if (type.getMeta() == id)
                    return type;
            }
            
            return null;
        }
    }
    
    public static final PropertyEnum<SlabType> SLAB_TYPE = PropertyEnum.create("ta_slab_type", SlabType.class);
    public static final PropertyBool DOUBLE = PropertyBool.create("ta_slab_double");
    
}
