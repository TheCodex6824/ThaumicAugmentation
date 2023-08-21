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

package thecodex6824.thaumicaugmentation.common.world.biome;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.BiomeDecorator;

public class BiomeEmptiness extends BiomeEmptinessBase {

    public BiomeEmptiness() {
        super(new BiomeProperties("Emptiness").setBaseHeight(0.15F).setHeightVariation(0.15F).setRainDisabled().setTemperature(
                0.25F).setWaterColor(0xAA00AA), 0.15F, 0x990099);
    }

    @Override
    public boolean canRain() {
        return false;
    }
    
    @Override
    public Vec3d getFogColor(Entity view, float angle, float partialTicks) {
    	return new Vec3d(0.2, 0.0, 0.3);
    }

    @Override
    public BiomeDecorator createBiomeDecorator() {
        return new BiomeDecoratorEmptinessBase();
    }

}
