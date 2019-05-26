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

import net.minecraft.block.BlockFlower.EnumFlowerType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.chunk.ChunkPrimer;
import thaumcraft.common.entities.monster.tainted.EntityTaintCrawler;
import thaumcraft.common.entities.monster.tainted.EntityTaintacle;
import thaumcraft.common.entities.monster.tainted.EntityTaintacleSmall;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.api.world.IPurgeBiomeSpawns;

public class BiomeTaintedLands extends Biome implements IPurgeBiomeSpawns, IFluxBiome {

	public BiomeTaintedLands() {
		super(new BiomeProperties("Tainted Lands").setBaseHeight(-1.8F).setHeightVariation(0.15F).setRainDisabled().setTemperature(
				0xFF00FF).setWaterColor(0xFF00FF));

		purgeSpawns();
		flowers.clear();
		topBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.SOIL_STONE_TAINT_NODECAY);
		fillerBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_TAINT_NODECAY);
	}
	
	@Override
	public void purgeSpawns() {
		spawnableCreatureList.clear();
		spawnableMonsterList.clear();
		spawnableMonsterList.add(new SpawnListEntry(EntityTaintCrawler.class, 100, 3, 5));
		spawnableMonsterList.add(new SpawnListEntry(EntityTaintacleSmall.class, 75, 1, 2));
		spawnableMonsterList.add(new SpawnListEntry(EntityTaintacle.class, 50, 1, 1));
		
		spawnableWaterCreatureList.clear();
		spawnableCaveCreatureList.clear();
	}
	
	@Override
	public float getBaseFluxConcentration() {
		return 0.65F;
	}

	@Override
	public boolean canRain() {
		return false;
	}

	@Override
	public int getSkyColorByTemp(float currentTemperature) {
		return 0;
	}

	@Override
	public void genTerrainBlocks(World world, Random rand, ChunkPrimer primer, int x, int z, double noiseVal) {
		int cX = x & 15;
		int cZ = z & 15;
		for (int y = world.getActualHeight(); y >= 0; --y) {
			IBlockState current = primer.getBlockState(cX, y, cZ);
			if (!current.getBlock().isAir(current, world, new BlockPos(x, y, z)))
				primer.setBlockState(cX, y, cZ, primer.getBlockState(cX, y + 1, cZ).isNormalCube() ? fillerBlock : topBlock);
		}
	}

	@Override
	public BiomeDecorator createBiomeDecorator() {
		return new BiomeDecoratorTaintedLands();
	}

	@Override
	public EnumFlowerType pickRandomFlower(Random rand, BlockPos pos) {
		return EnumFlowerType.ALLIUM;
	}
	
}
