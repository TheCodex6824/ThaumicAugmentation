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

import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.common.util.QuadConsumer;

public interface ITAStoneType {

    public enum StoneType implements IStringSerializable {
        STONE_VOID(0, () -> ThaumcraftMaterials.MATERIAL_TAINT, () -> SoundType.STONE, (w, b, s, r) -> {}),
        STONE_TAINT_NODECAY(1, () -> ThaumcraftMaterials.MATERIAL_TAINT, () -> SoundsTC.GORE, (w, b, s, r) -> {}),
        SOIL_STONE_TAINT_NODECAY(2, () -> ThaumcraftMaterials.MATERIAL_TAINT, () -> SoundsTC.GORE, (w, b, s, r) -> {
            if (w.getBlockState(b.up()).getLightOpacity(w, b.up()) > 2)
                w.setBlockState(b, s.withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_TAINT_NODECAY));
        });
        
        private int meta;
        private Supplier<Material> mat;
        private Supplier<SoundType> sound;
        private QuadConsumer<World, BlockPos, IBlockState, Random> randomTickFunc;
        
        private StoneType(int m, Supplier<Material> mt, Supplier<SoundType> s, QuadConsumer<World, BlockPos, IBlockState, Random> func) {
            meta = m;
            mat = mt;
            sound = s;
            randomTickFunc = func;
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
        
        public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
            randomTickFunc.accept(world, pos, state, random);
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
