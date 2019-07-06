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

package thecodex6824.thaumicaugmentation.common.world.biome;

import java.util.List;
import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import thecodex6824.thaumicaugmentation.api.world.TABiomes;
import thecodex6824.thaumicaugmentation.common.world.biome.layer.GenLayerEmptiness;

public class BiomeProviderEmptiness extends BiomeProvider {

    protected BiomeCache cache;
    protected GenLayer biomeGen;
    protected GenLayer indexGen;
    
    static {
        allowedBiomes.clear();
        allowedBiomes.add(TABiomes.EMPTINESS);
        allowedBiomes.add(TABiomes.TAINTED_LANDS);
        allowedBiomes.add(TABiomes.EMPTINESS_HIGHLANDS);
    }
    
    public BiomeProviderEmptiness() {
        cache = new BiomeCache(this);
    }
    
    public BiomeProviderEmptiness(World world) {
        this();
        GenLayer[] layers = GenLayerEmptiness.createLayers(world.getSeed());
        biomeGen = layers[0];
        indexGen = layers[1];
    }
    
    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return allowedBiomes;
    }
    
    @Override
    public Biome getBiome(BlockPos pos) {
        return getBiome(pos, null);
    }
    
    @Override
    public Biome getBiome(BlockPos pos, Biome defaultBiome) {
        return cache.getBiome(pos.getX(), pos.getZ(), defaultBiome);
    }
    
    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        IntCache.resetIntCache();
        
        if (biomes == null || biomes.length < width * height)
            biomes = new Biome[width * height];
        
        int[] ids = biomeGen.getInts(x, z, width, height);
        for (int i = 0; i < width * height; ++i) {
            if (ids[i] >= 0 && ids[i] <= Biome.REGISTRY.getKeys().size())
                biomes[i] = Biome.getBiome(ids[i]);
            else
                biomes[i] = TABiomes.EMPTINESS;
        }
        
        return biomes;
    }
    
    @Override
    public Biome[] getBiomes(Biome[] oldBiomeList, int x, int z, int width, int depth) {
        return getBiomes(oldBiomeList, x, z, width, depth, true);
    }
    
    @Override
    public Biome[] getBiomes(Biome[] biomes, int x, int z, int width, int height, boolean cacheFlag) {
        IntCache.resetIntCache();
        
        if (biomes == null || biomes.length < width * height)
            biomes = new Biome[width * height];
        
        if (cacheFlag && width == 16 && height == 16 && (x & 15) == 0 && (z & 15) == 0) {
            Biome[] cached = cache.getCachedBiomes(x, z);
            System.arraycopy(cached, 0, biomes, 0, width * height);
            return biomes;
        }
        else {
            int[] ids = indexGen.getInts(x, z, width, height);
            for (int i = 0; i < width * height; ++i) {
                if (ids[i] >= 0 && ids[i] <= Biome.REGISTRY.getKeys().size())
                    biomes[i] = Biome.getBiome(ids[i]);
                else
                    biomes[i] = TABiomes.EMPTINESS;
            }
            
            return biomes;
        }
    }
    
    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        IntCache.resetIntCache();
        
        int xMin = x - radius >> 2;
        int zMin = z - radius >> 2;
        int xMax = x + radius >> 2;
        int zMax = z + radius >> 2;
        
        int width = xMax - xMin + 1;
        int height = zMax - zMin + 1;
        int[] ids = biomeGen.getInts(xMin, zMin, width, height);
        for (int i = 0; i < width * height; ++i) {
            Biome biome = Biome.getBiome(ids[i]);
            if (!allowed.contains(biome))
                return false;
        }
        
        return true;
    }
    
    @Override
    public BlockPos findBiomePosition(int x, int z, int radius, List<Biome> biomes, Random random) {
        IntCache.resetIntCache();
        
        int xMin = x - radius >> 2;
        int zMin = z - radius >> 2;
        int xMax = x + radius >> 2;
        int zMax = z + radius >> 2;
        
        int width = xMax - xMin + 1;
        int height = zMax - zMin + 1;
        int[] ids = biomeGen.getInts(xMin, zMin, width, height);
        BlockPos ret = null;
        int counter = 0;
        for (int i = 0; i < width * height; ++i) {
            int bX = xMin + i % width << 2;
            int bZ = zMin + i / height << 2;
            Biome biome = Biome.getBiome(ids[i]);
            if (biomes.contains(biome) && (ret == null || random.nextInt(counter + 1) == 0)) {
                ret = new BlockPos(bX, 0, bZ);
                ++counter;
            }
        }
        
        return ret;
    }
    
    @Override
    public void cleanupCache() {
        cache.cleanupCache();
    }
    
}
