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

package thecodex6824.thaumicaugmentation.api.world;

import java.util.HashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;

public final class BiomeTerrainBlocks {

    private BiomeTerrainBlocks() {}
    
    public static class TerrainBlocks {

        private IBlockState top;
        private IBlockState filler;

        public TerrainBlocks(IBlockState t, IBlockState f) {
            top = t;
            filler = f;
        }

        public IBlockState getTopState() {
            return top;
        }

        public IBlockState getFillerState() {
            return filler;
        }

    }

    private static HashMap<String, TerrainBlocks> terrain = new HashMap<>();

    public static void init() {
        Biome.REGISTRY.forEach(biome -> {
            terrain.put(biome.getRegistryName().toString(), new TerrainBlocks(biome.topBlock, 
                    biome.fillerBlock));
        });
    }

    public static void registerBiomeOverride(Biome biome, IBlockState top, IBlockState filler) {
        terrain.put(biome.getRegistryName().toString(), new TerrainBlocks(top, filler));
    }

    public static TerrainBlocks getTerrainBlocksForBiome(Biome biome) {
        return terrain.get(biome.getRegistryName().toString());
    }

}
