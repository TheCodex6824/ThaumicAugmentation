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

package thecodex6824.thaumicaugmentation.common.world.biome.layer;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraft.world.gen.layer.IntCache;
import thecodex6824.thaumicaugmentation.api.world.TABiomes;

public class GenLayerEmptiness extends GenLayer {

	// multiple entries for easy weighting
	protected static final Biome[] ALLOWED_BIOMES = new Biome[] {TABiomes.EMPTINESS, TABiomes.TAINTED_LANDS,
			TABiomes.EMPTINESS, TABiomes.EMPTINESS};
	
	public GenLayerEmptiness(long seed) {
		super(seed);
	}
	
	@Override
	public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
		int[] ret = IntCache.getIntCache(areaWidth * areaHeight);
		
		for (int y = 0; y < areaHeight; ++y) {
			for (int x = 0; x < areaWidth; ++x) {
				initChunkSeed(areaX + x, areaY + y);
				ret[x + y * areaWidth] = Biome.getIdForBiome(ALLOWED_BIOMES[nextInt(ALLOWED_BIOMES.length)]);
			}
		}
		
		return ret;
	}
	
	// it's 2019 and java doesn't have tuples/pairs. fun...
	public static GenLayer[] createLayers(long seed) {
		
		GenLayer biome = new GenLayerEmptiness(14676);
		biome = new GenLayerZoom(10000, biome);
		biome = new GenLayerZoom(10001, biome);
		biome = new GenLayerZoom(10002, biome);
		biome = new GenLayerZoom(10003, biome);
		biome = new GenLayerZoom(10004, biome);
		biome = new GenLayerZoom(10005, biome);
		biome = new GenLayerZoom(10006, biome);
		
		GenLayer voronoiZoom = new GenLayerVoronoiZoom(10, biome);
		biome.initWorldGenSeed(seed);
		voronoiZoom.initWorldGenSeed(seed);
		
		return new GenLayer[] {biome, voronoiZoom};
	}
	
}
