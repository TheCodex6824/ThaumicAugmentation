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

package thecodex6824.thaumicaugmentation.common.world.biome.layer;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraft.world.gen.layer.IntCache;
import thecodex6824.thaumicaugmentation.api.world.TABiomes;

public class GenLayerEmptiness extends GenLayer {

	protected static final ImmutableList<Integer> CDF_WEIGHTS = ImmutableList.of(
		15,
		25,
		30,
		35
	);
	protected static final ImmutableList<Biome> CDF_BIOMES = ImmutableList.of(
		TABiomes.EMPTINESS,
		TABiomes.MERCURIAL_PEAKS,
		TABiomes.SERPENTINE_ABYSS,
		TABiomes.TAINTED_SWAMP
	);
    
    public GenLayerEmptiness(long seed) {
        super(seed);
    }
    
    public GenLayerEmptiness(long seed, GenLayer layer) {
        super(seed);
        parent = layer;
    }
    
    private int binarySearchWeights(int n) {
        int left = 0;
        int right = CDF_WEIGHTS.size() - 1;
        while (left < right) {
            int check = left + (right - left) / 2;
            if (CDF_WEIGHTS.get(check) <= n && CDF_WEIGHTS.get(check + 1) > n)
                return check + 1;
            else if (CDF_WEIGHTS.get(check) <= n)
                left = check + 1;
            else if (CDF_WEIGHTS.get(check) > n)
                right = check;
        }
        
        if (CDF_WEIGHTS.get(0) > n)
            return 0;
        
        return -1;
    }
    
    @Override
    public int[] getInts(int areaX, int areaZ, int areaWidth, int areaHeight) {
        int[] ret = IntCache.getIntCache(areaWidth * areaHeight);
        for (int z = 0; z < areaHeight; ++z) {
            for (int x = 0; x < areaWidth; ++x) {
                initChunkSeed(areaX + x, areaZ + z);
                int biomeWeight = nextInt(CDF_WEIGHTS.get(CDF_WEIGHTS.size() - 1));
                ret[x + z * areaWidth] = Biome.getIdForBiome(CDF_BIOMES.get(binarySearchWeights(biomeWeight)));
            }
        }
        
        return ret;
    }
    
    public static GenLayer[] createLayers(long seed) {
        GenLayer biome = new GenLayerEmptiness(14676);
        biome = GenLayerZoom.magnify(10000, biome, 5);
        biome = new GenLayerNeighboringBiomes(20000, biome);
        
        GenLayer voronoiZoom = new GenLayerVoronoiZoom(10, biome);
        biome.initWorldGenSeed(seed);
        voronoiZoom.initWorldGenSeed(seed);
        
        return new GenLayer[] {biome, voronoiZoom};
    }
    
}
