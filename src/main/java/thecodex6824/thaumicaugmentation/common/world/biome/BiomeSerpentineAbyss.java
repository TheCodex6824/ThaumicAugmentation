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

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.BiomeDecorator;
import thaumcraft.common.blocks.world.ore.ShardType;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;

public class BiomeSerpentineAbyss extends BiomeEmptinessBase {
	
    public BiomeSerpentineAbyss() {
        super(new BiomeProperties("Serpentine Abyss").setBaseHeight(-64.0F).setHeightVariation(0.0F).setRainDisabled().setTemperature(
                0.25F).setWaterColor(0x5500AA), 0.25F, 0x9900bb);

        topBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_ANCIENT_BLUE);
    }

    @Override
    public boolean canRain() {
        return false;
    }
    
    @Override
    public List<ShardType> getCrystalTypesForWorldGen() {
    	return Collections.emptyList();
    }
    
    @Override
    public Vec3d getFogColor(Entity view, float angle, float partialTicks) {
    	double heightColor = MathHelper.clampedLerp(0.3, 0.7, (view.posY - 64) / 64.0);
    	return new Vec3d(0.2, heightColor - 0.3, heightColor);
    }

    @Override
    public BiomeDecorator createBiomeDecorator() {
        return new BiomeDecoratorEmptinessBase();
    }

}
