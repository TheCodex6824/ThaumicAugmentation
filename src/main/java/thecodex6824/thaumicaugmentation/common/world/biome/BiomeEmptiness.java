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
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.api.world.IPurgeBiomeSpawns;

public class BiomeEmptiness extends Biome implements IPurgeBiomeSpawns, IFluxBiome, IBiomeSpecificSpikeBlockProvider {

    public BiomeEmptiness() {
        super(new BiomeProperties("Emptiness").setBaseHeight(-1.8F).setHeightVariation(0.15F).setRainDisabled().setTemperature(
                0xAA00AA).setWaterColor(0xAA00AA));

        purgeSpawns();
        flowers.clear();
        topBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_VOID);
        fillerBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_VOID);
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
        return 0.15F;
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
        // so nothing to do here
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
