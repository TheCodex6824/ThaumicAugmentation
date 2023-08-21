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

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import thecodex6824.thaumicaugmentation.api.world.TABiomes;

public class GenLayerNeighboringBiomes extends GenLayer {

	protected GenLayer parent;
	
	public GenLayerNeighboringBiomes(long seed, GenLayer parent) {
		super(seed);
		this.parent = parent;
	}
	
	@Override
	public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
		int[] biomesIn = parent.getInts(areaX - 8, areaY - 8, areaWidth + 16, areaHeight + 16);
        int[] biomesOut = IntCache.getIntCache(areaWidth * areaHeight);
        for (int y = 0; y < areaHeight; ++y) {
            for (int x = 0; x < areaWidth; ++x) {
                initChunkSeed(x + areaX, y + areaY);
                Biome biome = Biome.getBiome(biomesIn[x + 6 + (y + 6) * (areaWidth + 16)]);
                if (biome == TABiomes.TAINTED_SWAMP) {
                	for (int offsetY = -6; offsetY <= 6; ++offsetY) {
                		for (int offsetX = -6; offsetX <= 6; ++offsetX) {
                			Biome offsetBiome = Biome.getBiomeForId(biomesIn[x + offsetX + 6 + (y + offsetY + 6) * (areaWidth + 16)]);
    	            		if (offsetBiome != biome) {
    	            			biome = TABiomes.TAINTED_LANDS;
    	            			break;
    	            		}
                		}
                	}
                }
            	
                biomesOut[x + y * areaWidth] = Biome.getIdForBiome(biome);
            }
        }

        return biomesOut;
	}
	
}
