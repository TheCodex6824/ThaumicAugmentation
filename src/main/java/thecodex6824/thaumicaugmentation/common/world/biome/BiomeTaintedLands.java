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

import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.feature.WorldGenerator;
import thaumcraft.common.blocks.world.ore.ShardType;
import thaumcraft.common.entities.monster.tainted.EntityTaintCrawler;
import thaumcraft.common.entities.monster.tainted.EntityTaintacle;
import thaumcraft.common.entities.monster.tainted.EntityTaintacleSmall;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.common.world.feature.WorldGenTaintFlower;

public class BiomeTaintedLands extends BiomeEmptinessBase {

    protected static final WorldGenTaintFlower FLOWER_GENERATOR = new WorldGenTaintFlower();
    
    public BiomeTaintedLands() {
        super(new BiomeProperties("Tainted Lands").setBaseHeight(0.125F).setHeightVariation(0.05F).setRainDisabled().setTemperature(
                0.25F).setWaterColor(0xFF00FF), null, 0.5F, 0x660066);

        topBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.SOIL_STONE_TAINT_NODECAY);
        spawnableMonsterList.add(new SpawnListEntry(EntityTaintCrawler.class, 100, 3, 5));
        spawnableMonsterList.add(new SpawnListEntry(EntityTaintacleSmall.class, 75, 1, 2));
        spawnableMonsterList.add(new SpawnListEntry(EntityTaintacle.class, 50, 1, 1));
        spawnableMonsterList.add(new SpawnListEntry(EntityEnderman.class, 1, 2, 2));
    }
    
    @Override
    public List<ShardType> getCrystalTypesForWorldGen() {
    	return CRYSTAL_FLUX;
    }
    
    @Override
    public Vec3d getFogColor(Entity view, float angle, float partialTicks) {
    	return new Vec3d(0.7, 0.0, 0.7);
    }
    
    @Override
    public WorldGenerator getRandomWorldGenForGrass(Random rand) {
        return FLOWER_GENERATOR;
    }

    @Override
    public BiomeDecorator createBiomeDecorator() {
        return new BiomeDecoratorTaintedLands();
    }
    
}
