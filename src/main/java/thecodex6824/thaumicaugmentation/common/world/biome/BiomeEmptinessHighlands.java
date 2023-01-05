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

import java.util.Random;

import net.minecraft.block.BlockFlower.EnumFlowerType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.chunk.ChunkPrimer;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.api.world.IPurgeBiomeSpawns;

public class BiomeEmptinessHighlands extends Biome implements IPurgeBiomeSpawns, IFluxBiome, IBiomeSpecificSpikeBlockProvider {

	protected IBlockState blueStone;
	
    public BiomeEmptinessHighlands() {
        super(new BiomeProperties("Emptiness Highlands").setBaseHeight(1.0F).setHeightVariation(1.0F).setRainDisabled().setTemperature(
                0.25F).setWaterColor(0x5500AA));

        purgeSpawns();
        flowers.clear();
        topBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_VOID);
        fillerBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_VOID);
        blueStone = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_ANCIENT_BLUE);
    }
    
    @Override
    public void purgeSpawns() {
        spawnableCreatureList.clear();
        spawnableMonsterList.clear();
        spawnableWaterCreatureList.clear();
        spawnableCaveCreatureList.clear();
    }
    
    @Override
    public IBlockState getSpikeState(World world, BlockPos pos) {
        return fillerBlock;
    }
    
    @Override
    public float getBaseFluxConcentration() {
        return 0.25F;
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
    public void genTerrainBlocks(World world, Random rand, ChunkPrimer chunkPrimer, int x, int z, double noiseVal) {
    	int ground = BiomeUtil.getHeightFromPrimer(world, chunkPrimer, x, z);
    	if (ground >= world.provider.getAverageGroundLevel() * 1.5 - rand.nextInt(5)) {
	    	int numBlue = (int) (noiseVal + 5.0);
			for (int y = 0; y < numBlue; ++y) {
				if (ground - y >= 0 && ground - y < world.provider.getActualHeight()) {
					BiomeUtil.setBlockStateInPrimer(chunkPrimer, x, ground - y, z, blueStone);
				}
				else if (ground - y < 0) {
					break;
				}
			}
    	}
    }

    @Override
    public BiomeDecorator createBiomeDecorator() {
        return new BiomeDecoratorEmptiness();
    }

    @Override
    public EnumFlowerType pickRandomFlower(Random rand, BlockPos pos) {
        return EnumFlowerType.ALLIUM;
    }

}
