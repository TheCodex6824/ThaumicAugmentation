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

import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import thaumcraft.common.entities.monster.boss.EntityTaintacleGiant;
import thaumcraft.common.entities.monster.tainted.EntityTaintSeed;
import thaumcraft.common.entities.monster.tainted.EntityTaintSeedPrime;

public class BiomeDecoratorTaintedLands extends BiomeDecorator {
    
	private void generateEntities(World world, Random rand, Biome biome, BlockPos pos) {
		BlockPos p = world.getTopSolidOrLiquidBlock(pos.add(8 + rand.nextInt(16), 0, 8 + rand.nextInt(16)));
        if (!world.isAirBlock(p.down()) && world.getBlockState(p.down()).isNormalCube()) {
            EntityLiving thingToSpawn = null;
            int result = rand.nextInt(500);
            if (result == 0)
                thingToSpawn = new EntityTaintacleGiant(world);
            else if (result < 51) {
                thingToSpawn = new EntityTaintSeedPrime(world);
                ((EntityTaintSeedPrime) thingToSpawn).boost = 1200;
            }
            else if (result < 151) {
                thingToSpawn = new EntityTaintSeed(world);
                ((EntityTaintSeed) thingToSpawn).boost = 200;
            }
            
            if (thingToSpawn != null) {
                thingToSpawn.setLocationAndAngles(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, rand.nextInt(360), 0);
                if (thingToSpawn.getCanSpawnHere() && thingToSpawn.isNotColliding()) {
                    thingToSpawn.enablePersistence();
                    world.spawnEntity(thingToSpawn);
                }
            }
        }
	}
	
	private void generateFeatures(World world, Random rand, Biome biome, BlockPos pos) {
		for (int i = 0; i < 2; ++i) {
            int x = rand.nextInt(16) + 8;
            int z = rand.nextInt(16) + 8;
            BlockPos gen = world.getTopSolidOrLiquidBlock(pos.add(x, 0, z));
            if (!world.isAirBlock(gen.down()) && world.isAirBlock(gen))
            	biome.getRandomWorldGenForGrass(rand).generate(world, rand, gen);
        }
	}
	
    @Override
    public void decorate(World world, Random rand, Biome biome, BlockPos pos) {
        generateFeatures(world, rand, biome, pos);
        generateEntities(world, rand, biome, pos);
    }
    
}
