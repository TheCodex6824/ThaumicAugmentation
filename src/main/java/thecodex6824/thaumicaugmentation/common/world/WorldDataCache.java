/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProvider;

import java.util.HashMap;
import java.util.Set;

public final class WorldDataCache {

    private WorldDataCache() {}
    
    private static boolean init = false;
    private static final HashMap<Integer, WorldData> PROVIDERS = new HashMap<>();
    
    public static class WorldData {
        
        private final int id;
        private final long seed;
        private final BiomeProvider biome;
        private final double factor;
        
        public WorldData(WorldProvider provider) {
            this(provider.getDimension(), provider.getSeed(), provider.getBiomeProvider(), provider.getMovementFactor());
        }
        
        public WorldData(int dimID, long worldSeed, BiomeProvider biomeProvider, double moveFactor) {
            id = dimID;
            seed = worldSeed;
            biome = biomeProvider;
            factor = moveFactor;
        }
        
        public int getDimensionID() {
            return id;
        }
        
        public long getWorldSeed() {
            return seed;
        }
        
        public BiomeProvider getBiomeProvider() {
            return biome;
        }
        
        public double getMovementFactor() {
            return factor;
        }
        
    }

    public static void addOrUpdateData(World world) {
        PROVIDERS.put(world.provider.getDimension(), new WorldData(world.provider));
    }

    public static WorldData getData(int dim) {
        return PROVIDERS.get(dim);
    }

    public static Set<Integer> listAllDimensions() {
        return PROVIDERS.keySet();
    }
    
    public static void setInitialized() {
        init = true;
    }
    
    public static boolean isInitialized() {
        return init;
    }

}
