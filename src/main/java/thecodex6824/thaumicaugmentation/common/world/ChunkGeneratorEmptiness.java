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

package thecodex6824.thaumicaugmentation.common.world;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent.InitNoiseField;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.common.world.structure.MapGenEldritchSpire;

public class ChunkGeneratorEmptiness implements ITAChunkGenerator {

    protected World world;
    protected Random rand;

    protected NoiseGeneratorOctaves min;
    protected NoiseGeneratorOctaves max;
    protected NoiseGeneratorOctaves main;
    protected NoiseGeneratorOctaves scale;
    protected NoiseGeneratorOctaves depth;
    protected NoiseGeneratorPerlin gen4;
    
    protected double[] biomeWeights;
    
    protected MapGenEldritchSpire spireGenerator;

    public ChunkGeneratorEmptiness(World w) {
        world = w;
        rand = new Random(world.getSeed());
        min = new NoiseGeneratorOctaves(rand, 16);
        max = new NoiseGeneratorOctaves(rand, 16);
        main = new NoiseGeneratorOctaves(rand, 8);
        scale = new NoiseGeneratorOctaves(rand, 10);
        depth = new NoiseGeneratorOctaves(rand, 16);
        gen4 = new NoiseGeneratorPerlin(rand, 4);
        
        biomeWeights = new double[25];
        for (int x = -2; x <= 2; ++x) {
            for (int z = -2; z <= 2; ++z)
                biomeWeights[x + 2 + (z + 2) * 5] = 10.0F / MathHelper.sqrt(x * x + z * z + 0.2F);
        }
        
        InitNoiseGensEvent.Context ctx = new InitNoiseGensEvent.Context(min, max, main, scale, depth);
        ctx = TerrainGen.getModdedNoiseGenerators(world, rand, ctx);
        min = ctx.getLPerlin1();
        max = ctx.getLPerlin2();
        main = ctx.getPerlin();
        scale = ctx.getScale();
        depth = ctx.getDepth();
        
        spireGenerator = (MapGenEldritchSpire) TerrainGen.getModdedMapGen(new MapGenEldritchSpire(this), EventType.CUSTOM);
    }

    protected double[] generateHeights(int posX, int posY, int posZ, int sizeX, int sizeY, int sizeZ, Biome[] biomes) {
        double[] output = new double[sizeX * sizeY * sizeZ];
        InitNoiseField noiseEvent = new InitNoiseField(this, output, posX, posY, posZ, sizeX, sizeY, sizeZ);
        MinecraftForge.EVENT_BUS.post(noiseEvent);
        if (noiseEvent.getResult() == Result.DENY)
            return noiseEvent.getNoisefield();
        
        double depthScaleX = 200.0;
        double depthScaleZ = 200.0;
        double coordScale = 684.412;
        double heightScale = 684.412;
        double[] depthNoise = depth.generateNoiseOctaves(null, posX, posZ, sizeX, sizeZ, depthScaleX, depthScaleZ, 0.5);
        double[] mainNoise = main.generateNoiseOctaves(null, posX, posY, posZ, sizeX, sizeY, sizeZ, coordScale / 80.0, heightScale / 160.0, coordScale / 80.0);
        double[] minNoise = min.generateNoiseOctaves(null, posX, posY, posZ, sizeX, sizeY, sizeZ, coordScale, heightScale, coordScale);
        double[] maxNoise = max.generateNoiseOctaves(null, posX, posY, posZ, sizeX, sizeY, sizeZ, coordScale, heightScale, coordScale);
        
        int noiseIndex = 0;
        int depthIndex = 0;
        for (int x = 0; x < sizeX; ++x) {
            for (int z = 0; z < sizeZ; ++z) {
                float f2 = 0.0F;
                float f3 = 0.0F;
                float f4 = 0.0F;
                Biome biome = biomes[x + 2 + (z + 2) * 10];

                for (int bX = -2; bX <= 2; ++bX) {
                    for (int bZ = -2; bZ <= 2; ++bZ) {
                        Biome biome1 = biomes[x + bX + 2 + (z + bZ + 2) * 10];
                        float f7 = (float) biomeWeights[bX + 2 + (bZ + 2) * 5] / (biome1.getBaseHeight() + 2.0F);

                        if (biome1.getBaseHeight() > biome.getBaseHeight())
                            f7 /= 2.0F;

                        f2 += biome1.getHeightVariation() * f7;
                        f3 += biome1.getBaseHeight() * f7;
                        f4 += f7;
                    }
                }

                f2 /= f4;
                f3 /= f4;
                f2 = f2 * 0.9F + 0.1F;
                f3 = (f3 * 4.0F - 1.0F) / 8.0F;
                double d7 = depthNoise[depthIndex++] / 8000.0;

                if (d7 < 0.0)
                    d7 = -d7 * 0.3;

                d7 = d7 * 3.0 - 2.0;

                if (d7 < 0.0D) {
                    d7 /= 2.0;

                    if (d7 < -1.0)
                        d7 = -1.0;

                    d7 /= 1.4;
                    d7 /= 2.0;
                }
                else {
                    if (d7 > 1.0)
                        d7 = 1.0;

                    d7 /= 8.0;
                }

                double depthBase = 0.5;
                double d8 = (f3 + d7 * 0.2) * (depthBase / 8.0);
                double d9 = f2;
                double d0 = depthBase + d8 * 4.0;

                for (int l1 = 0; l1 < 33; ++l1) {
                    double d1 = (l1 - d0) * 12.0 * 128.0 / 256.0 / d9;

                    if (d1 < 0.0D)
                        d1 *= 4.0D;

                    double d2 = minNoise[noiseIndex] / 512.0;
                    double d3 = maxNoise[noiseIndex] / 512.0;
                    double d4 = (mainNoise[noiseIndex] / 10.0 + 1.0) / 2.0;
                    double d5 = MathHelper.clampedLerp(d2, d3, d4) - d1;

                    if (l1 > 29) {
                        double d6 = (l1 - 29) / 3.0F;
                        d5 = d5 * (1.0 - d6) - 10.0 * d6;
                    }

                    output[noiseIndex++] = d5;
                }
            }
        }
        
        return output;
    }

    @Override
    public void populatePrimerWithHeightmap(int xPos, int zPos, ChunkPrimer primer) {
        Biome[] biomes = world.getBiomeProvider().getBiomesForGeneration(null, xPos * 4 - 2, zPos * 4 - 2, 10, 10);
        setBlocksInChunk(xPos, zPos, primer, biomes);
    }
    
    protected void setBlocksInChunk(int xPos, int zPos, ChunkPrimer primer, Biome[] biomes) {
        IBlockState filler = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_VOID);
        int scaleX = 5, scaleY = 33, scaleZ = 5;
        double[] heights = generateHeights(xPos * 4, 0, zPos * 4, scaleX, scaleY, scaleZ, biomes);
        for (int i = 0; i < 4; ++i) {
            int j = i * 5;
            int k = (i + 1) * 5;

            for (int l = 0; l < 4; ++l) {
                int i1 = (j + l) * 33;
                int j1 = (j + l + 1) * 33;
                int k1 = (k + l) * 33;
                int l1 = (k + l + 1) * 33;

                for (int i2 = 0; i2 < 32; ++i2) {
                    double d1 = heights[i1 + i2];
                    double d2 = heights[j1 + i2];
                    double d3 = heights[k1 + i2];
                    double d4 = heights[l1 + i2];
                    double d5 = (heights[i1 + i2 + 1] - d1) * 0.125D;
                    double d6 = (heights[j1 + i2 + 1] - d2) * 0.125D;
                    double d7 = (heights[k1 + i2 + 1] - d3) * 0.125D;
                    double d8 = (heights[l1 + i2 + 1] - d4) * 0.125D;

                    for (int j2 = 0; j2 < 8; ++j2) {
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.25D;
                        double d13 = (d4 - d2) * 0.25D;

                        for (int k2 = 0; k2 < 4; ++k2) {
                            double d16 = (d11 - d10) * 0.25D;
                            double lvt_45_1_ = d10 - d16;

                            for (int l2 = 0; l2 < 4; ++l2) {
                                if ((lvt_45_1_ += d16) > 0.0D && i2 * 8 + j2 >= 0)
                                    primer.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, filler);
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    protected void replaceBlocksForBiome(int x, int z, ChunkPrimer primer, Biome[] biomes) {
        if (!ForgeEventFactory.onReplaceBiomeBlocks(this, x, z, primer, world))
            return;
        
        double[] noise = gen4.getRegion(null, x * 16, z * 16, 16, 16, 0.0625, 0.0625, 1.0);
        for (int cX = 0; cX < 16; ++cX) {
            for (int cZ = 0; cZ < 16; ++cZ) {
                Biome biome = biomes[cZ + cX * 16];
                biome.genTerrainBlocks(world, rand, primer, x * 16 + cX, z * 16 + cZ, noise[cZ + cX * 16]);
            }
        }
    }
    
    @Override
    public Chunk generateChunk(int x, int z) {
        rand.setSeed(x * 341873128712L + z * 132897987541L);
        ChunkPrimer primer = new ChunkPrimer();
        Biome[] biomes = world.getBiomeProvider().getBiomesForGeneration(null, x * 4 - 2, z * 4 - 2, 10, 10);
        setBlocksInChunk(x, z, primer, biomes);
        biomes = world.getBiomeProvider().getBiomes(biomes, x * 16, z * 16, 16, 16);
        replaceBlocksForBiome(x, z, primer, biomes);
        
        if (world.getWorldInfo().isMapFeaturesEnabled() && TAConfig.generateSpires.getValue())
            spireGenerator.generate(world, x, z, primer);
        
        Chunk chunk = new Chunk(world, primer, x, z);
        for (int i = 0; i < chunk.getBiomeArray().length; ++i)
            chunk.getBiomeArray()[i] = (byte) Biome.getIdForBiome(biomes[i]);
        
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int x, int z) {
        BlockFalling.fallInstantly = true;

        BlockPos pos = new BlockPos(x * 16, 0, z * 16);
        Biome biome = world.getBiome(pos.add(16, 0, 16));
        rand.setSeed(world.getSeed());
        long xSeed = rand.nextLong() + 1;
        long zSeed = rand.nextLong() + 1;
        rand.setSeed(x * xSeed + z * zSeed ^ world.getSeed());
        
        ForgeEventFactory.onChunkPopulate(true, this, world, rand, x, z, false);
        if (world.getWorldInfo().isMapFeaturesEnabled() && TAConfig.generateSpires.getValue()) {
            spireGenerator.generateStructure(world, rand, new ChunkPos(x, z));
            world.getChunk(x, z).resetRelightChecks();
        }
        
        biome.decorate(world, rand, pos);

        ForgeEventFactory.onChunkPopulate(false, this, world, rand, x, z, false);
        BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position,
            boolean findUnexplored) {

        if (world.getWorldInfo().isMapFeaturesEnabled() && TAConfig.generateSpires.getValue() &&
                spireGenerator.getStructureName().equals(structureName)) {
            
            return spireGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        
        return null;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (world.getWorldInfo().isMapFeaturesEnabled() && TAConfig.generateSpires.getValue()) {
            if (spireGenerator.isInsideStructure(pos))
                return spireGenerator.getSpawnableCreatures(creatureType, pos);
        }
        
        return world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        if (world.getWorldInfo().isMapFeaturesEnabled() && TAConfig.generateSpires.getValue()) {
            if (spireGenerator != null && spireGenerator.getStructureName().equals(structureName))
                return spireGenerator.isInsideStructure(pos);
        }
        
        return false;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        if (world.getWorldInfo().isMapFeaturesEnabled() && TAConfig.generateSpires.getValue())
            spireGenerator.generate(world, x, z, null);
    }

}
