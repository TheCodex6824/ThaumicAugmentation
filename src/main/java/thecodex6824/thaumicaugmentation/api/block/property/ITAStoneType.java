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

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.util.QuadConsumer;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Supplier;

/*
 * Property interface for the type of stone in Thaumic Augmentation's stone.
 * @author TheCodex6824
 * 
 * @see thecodex6824.thaumicaugmentation.api.TABlocks#STONE
 */
public interface ITAStoneType {
    
    enum StoneType implements IStringSerializable {
        
        STONE_VOID(0, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.OBSIDIAN, 0),
        STONE_TAINT_NODECAY(1, () -> ThaumcraftMaterials.MATERIAL_TAINT, () -> SoundsTC.GORE, (w, b, s, r) -> {}, MapColor.OBSIDIAN, 0),
        SOIL_STONE_TAINT_NODECAY(2, () -> ThaumcraftMaterials.MATERIAL_TAINT, () -> SoundsTC.GORE, (w, b, s, r) -> {
            if (w.getBlockState(b.up()).getLightOpacity(w, b.up()) > 2)
                w.setBlockState(b, s.withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_TAINT_NODECAY));
        }, MapColor.PURPLE, 0),
        ANCIENT_RUNES(3, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.BROWN, 0),
        ANCIENT_GLYPHS(4, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.BROWN, 0),
        ANCIENT_BRICKS(5, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.BROWN, 0),
        STONE_CRUSTED(6, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.ADOBE, 0),
        STONE_CRUSTED_GLOWING(7, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.ADOBE, 15),
        ANCIENT_PILLAR(8, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.ADOBE, 0),
        CHISELED_ANCIENT_BRICKS(9, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.BROWN, 0),
        ANCIENT_COBBLESTONE(10, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.BLUE, 0),
        ANCIENT_LIGHT(11, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.BROWN, 15),
        STONE_ANCIENT_BLUE(12, () -> Material.ROCK, () -> SoundType.STONE, (w, b, s, r) -> {}, MapColor.BLUE, 0);
        
        private final int meta;
        private final Supplier<Material> mat;
        private final Supplier<SoundType> sound;
        private final QuadConsumer<World, BlockPos, IBlockState, Random> randomTickFunc;
        private final MapColor color;
        private final int light;
        
        StoneType(int m, Supplier<Material> mt, Supplier<SoundType> s, QuadConsumer<World, BlockPos, IBlockState, Random> func, MapColor c, int l) {
            meta = m;
            mat = mt;
            sound = s;
            randomTickFunc = func;
            color = c;
            light = l;
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
        
        public MapColor getMapColor() {
            return color;
        }
        
        public int getLightLevel() {
            return light;
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
    
    PropertyEnum<StoneType> STONE_TYPE = PropertyEnum.create("ta_stone_type", StoneType.class);
    
}
