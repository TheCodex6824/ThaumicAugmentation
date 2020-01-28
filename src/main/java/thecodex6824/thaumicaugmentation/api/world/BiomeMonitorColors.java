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

import net.minecraft.world.biome.Biome;

public final class BiomeMonitorColors {

    private BiomeMonitorColors() {}
    
    public static class MonitorColors {
        
        private int grass;
        private int plants;
        private int water;
        
        public MonitorColors(int g, int p, int w) {
            grass = g;
            plants = p;
            water = w;
        }
        
        public int getGrassColor() {
            return grass;
        }
        
        public int getPlantColor() {
            return plants;
        }
        
        public int getWaterColor() {
            return water;
        }
        
    }
    
    private static final MonitorColors NULL_ENTRY = new MonitorColors(-1, -1, -1);
    private static HashMap<String, MonitorColors> overrides = new HashMap<>();
    
    public static void registerMonitorColorOverride(Biome biome, int grassColor, int plantColor, int waterColor) {
        overrides.put(biome.getRegistryName().toString(), new MonitorColors(grassColor, plantColor, waterColor));
    }
    
    public static MonitorColors getMonitorColors(Biome biome) {
        return overrides.getOrDefault(biome.getRegistryName().toString(), NULL_ENTRY);
    }
    
    public static void init() {}
    
}
