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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import thecodex6824.thaumicaugmentation.common.world.feature.WorldGenVoidStoneSpike;

import java.util.Random;

public abstract class BiomeDecoratorEmptinessBase extends BiomeDecorator {

    protected WorldGenVoidStoneSpike spikeGen;
    
    public BiomeDecoratorEmptinessBase() {
       spikeGen = new WorldGenVoidStoneSpike();
    }
    
    @Override
    public void decorate(World world, Random random, Biome biome, BlockPos pos) {
        if (random.nextBoolean())
            spikeGen.generate(world, random, world.getHeight(pos.add(8 + random.nextInt(8), 0, 8 + random.nextInt(8))));
    }
    
}
