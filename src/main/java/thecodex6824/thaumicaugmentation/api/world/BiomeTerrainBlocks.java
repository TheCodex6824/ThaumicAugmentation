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
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

/**
 * Stores blocks that are associated with the surfaces of biomes. Used by dimensional
 * fractures to generate an area around them corresponding to the area they lead to.
 * @author TheCodex6824
 */
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
    private static HashMap<Block, Function<IBlockState, IBlockState>> blockReplacements = new HashMap<>();

    public static void init() {
        blockReplacements.put(Blocks.SAND, (state) -> {
            if (state.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND)
                return Blocks.RED_SANDSTONE.getDefaultState();
            else
                return Blocks.SANDSTONE.getDefaultState();
        });
        blockReplacements.put(Blocks.GRAVEL, (state) -> {
            return Blocks.COBBLESTONE.getDefaultState();
        });
        
        Biome.REGISTRY.forEach(biome -> {
            registerBiomeOverride(biome, biome.topBlock, biome.fillerBlock);
        });
    }

    private static IBlockState handleReplacements(IBlockState in) {
        if (blockReplacements.containsKey(in.getBlock()))
            return blockReplacements.get(in.getBlock()).apply(in);
        else
            return in;
    }
    
    public static void registerBiomeOverride(Biome biome, IBlockState top, IBlockState filler) {
        top = handleReplacements(top);
        filler = handleReplacements(filler);
        terrain.put(biome.getRegistryName().toString(), new TerrainBlocks(top, filler));
    }

    public static TerrainBlocks getTerrainBlocksForBiome(Biome biome) {
        return terrain.get(biome.getRegistryName().toString());
    }
    
    private static void updateReplacements() {
        for (TerrainBlocks blocks : terrain.values()) {
            boolean noMoreReplacements = true;
            do {
                noMoreReplacements = true;
                IBlockState newTop = handleReplacements(blocks.top);
                if (newTop != blocks.top) {
                    blocks.top = newTop;
                    noMoreReplacements = false;
                }
                
                IBlockState newFiller = handleReplacements(blocks.filler);
                if (newTop != blocks.filler) {
                    blocks.filler = newFiller;
                    noMoreReplacements = false;
                }
            } while (!noMoreReplacements);
        }
    }
    
    public static void registerBlockReplacement(Block toReplace, Function<IBlockState, IBlockState> replacer) {
        blockReplacements.put(toReplace, replacer);
        updateReplacements();
    }

}
