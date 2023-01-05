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

package thecodex6824.thaumicaugmentation.common.world;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

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

	protected static final double DEPTH_SCALE_X = 200.0;
	protected static final double DEPTH_SCALE_Z = 200.0;
	protected static final double DEPTH_SCALE_EXPONENT = 0.5;
	protected static final double HORIZONTAL_COORD_SCALE = 684.412;
	protected static final double HEIGHT_COORD_SCALE = 684.412;
	protected static final double DEPTH_SCALE = 0.5;
	protected static final double MIN_NOISE_SCALE = 1.0 / 512.0;
	protected static final double MAX_NOISE_SCALE = 1.0 / 512.0;
	protected static final int HEIGHT_SCALE_X = 5;
	protected static final int HEIGHT_SCALE_Y = 33;
	protected static final int HEIGHT_SCALE_Z = 5;
	
    protected World world;
    protected Random rand;

    protected NoiseGeneratorOctaves min;
    protected NoiseGeneratorOctaves max;
    protected NoiseGeneratorOctaves main;
    protected NoiseGeneratorOctaves scale;
    protected NoiseGeneratorOctaves depth;
    protected NoiseGeneratorPerlin surfaceNoise;
    
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
        surfaceNoise = new NoiseGeneratorPerlin(rand, 4);
        
        biomeWeights = new double[25];
        for (int x = -2; x <= 2; ++x) {
            for (int z = -2; z <= 2; ++z) {
                biomeWeights[x + 2 + (z + 2) * 5] = 10.0F / MathHelper.sqrt(x * x + z * z + 0.2F);
            }
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
        if (noiseEvent.getResult() == Result.DENY) {
            return noiseEvent.getNoisefield();
        }
        
        double[] depthNoise = depth.generateNoiseOctaves(null, posX, posZ, sizeX, sizeZ, DEPTH_SCALE_X, DEPTH_SCALE_Z, DEPTH_SCALE_EXPONENT);
        double[] mainNoise = main.generateNoiseOctaves(null, posX, posY, posZ, sizeX, sizeY, sizeZ, HORIZONTAL_COORD_SCALE / 80.0, HEIGHT_COORD_SCALE / 160.0, HORIZONTAL_COORD_SCALE / 80.0);
        double[] minNoise = min.generateNoiseOctaves(null, posX, posY, posZ, sizeX, sizeY, sizeZ, HORIZONTAL_COORD_SCALE, HEIGHT_COORD_SCALE, HORIZONTAL_COORD_SCALE);
        double[] maxNoise = max.generateNoiseOctaves(null, posX, posY, posZ, sizeX, sizeY, sizeZ, HORIZONTAL_COORD_SCALE, HEIGHT_COORD_SCALE, HORIZONTAL_COORD_SCALE);
        
        int noiseIndex = 0;
        int depthIndex = 0;
        for (int x = 0; x < sizeX; ++x) {
            for (int z = 0; z < sizeZ; ++z) {
                float totalHeightVariation = 0.0F;
                float totalBaseHeight = 0.0F;
                float totalHeightBlend = 0.0F;
                Biome biome = biomes[x + 2 + (z + 2) * 10];
                for (int bX = -2; bX <= 2; ++bX) {
                    for (int bZ = -2; bZ <= 2; ++bZ) {
                        Biome surroundingBiome = biomes[x + bX + 2 + (z + bZ + 2) * 10];
                        float heightBlend = (float) biomeWeights[bX + 2 + (bZ + 2) * 5] / (surroundingBiome.getBaseHeight() + 2.0F);

                        if (surroundingBiome.getBaseHeight() > biome.getBaseHeight()) {
                        	heightBlend /= 2.0F;
                        }

                        totalHeightVariation += surroundingBiome.getHeightVariation() * heightBlend;
                        totalBaseHeight += surroundingBiome.getBaseHeight() * heightBlend;
                        totalHeightBlend += heightBlend;
                    }
                }

                totalHeightVariation /= totalHeightBlend;
                totalBaseHeight /= totalHeightBlend;
                totalHeightVariation = totalHeightVariation * 0.9F + 0.1F;
                totalBaseHeight = (totalBaseHeight * 4.0F - 1.0F) / 8.0F;
                double heightValue = depthNoise[depthIndex++] / 8000.0;
                if (heightValue < 0.0) {
                	heightValue = -heightValue * 0.3;
                }
                heightValue = heightValue * 3.0 - 2.0;
                if (heightValue < 0.0) {
                	heightValue /= 2.0;

                    if (heightValue < -1.0) {
                    	heightValue = -1.0;
                    }

                    heightValue /= 1.4;
                    heightValue /= 2.0;
                }
                else {
                    if (heightValue > 1.0) {
                    	heightValue = 1.0;
                    }

                    heightValue /= 8.0;
                }

                for (int y = 0; y < sizeY; ++y) {
                    double heightOffset = (y - (DEPTH_SCALE + (totalBaseHeight + heightValue * 0.2) * (DEPTH_SCALE / 8.0) * 4.0)) * 12.0 * 128.0 / 256.0 / totalHeightVariation;
                    if (heightOffset < 0.0) {
                    	heightOffset *= 4.0;
                    }

                    double min = minNoise[noiseIndex] * MIN_NOISE_SCALE;
                    double max = maxNoise[noiseIndex] * MAX_NOISE_SCALE;
                    double main = (mainNoise[noiseIndex] / 10.0 + 1.0) / 2.0;
                    double outputNoise = MathHelper.clampedLerp(min, max, main) - heightOffset;
                    if (y > 29) {
                        double tallBonus = (y - 29) / 3.0F;
                        outputNoise = outputNoise * (1.0 - tallBonus) - 10.0 * tallBonus;
                    }

                    output[noiseIndex++] = outputNoise;
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
        double[] heights = generateHeights(xPos * 4, 0, zPos * 4, HEIGHT_SCALE_X, HEIGHT_SCALE_Y, HEIGHT_SCALE_Z, biomes);
        for (int x = 0; x < HEIGHT_SCALE_X - 1; ++x) {
            for (int z = 0; z < HEIGHT_SCALE_Z - 1; ++z) {
                int cornerMinMin = (x * HEIGHT_SCALE_Z + z) * HEIGHT_SCALE_Y;
                int cornerMinMax = (x * HEIGHT_SCALE_Z + z + 1) * HEIGHT_SCALE_Y;
                int cornerMaxMin = ((x + 1) * HEIGHT_SCALE_Z + z) * HEIGHT_SCALE_Y;
                int cornerMaxMax = ((x + 1) * HEIGHT_SCALE_Z + z + 1) * HEIGHT_SCALE_Y;
                for (int y = 0; y < HEIGHT_SCALE_Y - 1; ++y) {
                    double base = heights[cornerMinMin + y];
                    double bias = heights[cornerMinMax + y];
                    double baseMod = heights[cornerMaxMin + y];
                    double biasMod = heights[cornerMaxMax + y];
                    double basePerStep = (heights[cornerMinMin + y + 1] - base) * 0.125;
                    double biasPerStep = (heights[cornerMinMax + y + 1] - bias) * 0.125;
                    double baseModPerStep = (heights[cornerMaxMin + y + 1] - baseMod) * 0.125;
                    double biasModPerStep = (heights[cornerMaxMax + y + 1] - biasMod) * 0.125;
                    int regionHeight = (HEIGHT_SCALE_Y - 1) / 4;
                    for (int y2 = 0; y2 < regionHeight; ++y2) {
                        double densityValue = base;
                        double densityBiasStart = bias;
                        double densityValueMod = (baseMod - base) * 0.25;
                        double densityBiasMod = (biasMod - bias) * 0.25;
                        for (int x2 = 0; x2 < HEIGHT_SCALE_X - 1; ++x2) {
                            double densityHeightBias = (densityBiasStart - densityValue) * 0.25;
                            double density = densityValue;
                            for (int z2 = 0; z2 < HEIGHT_SCALE_Z - 1; ++z2) {
                                if (density > 0.0 && y * regionHeight + y2 >= 0) {
                                    primer.setBlockState(x * (HEIGHT_SCALE_X - 1) + x2, y * regionHeight + y2, z * (HEIGHT_SCALE_Z - 1) + z2, filler);
                                }
                                
                                density += densityHeightBias;
                            }

                            densityValue += densityValueMod;
                            densityBiasStart += densityBiasMod;
                        }

                        base += basePerStep;
                        bias += biasPerStep;
                        baseMod += baseModPerStep;
                        biasMod += biasModPerStep;
                    }
                }
            }
        }
    }

    protected void replaceBlocksForBiome(int x, int z, ChunkPrimer primer, Biome[] biomes) {
        if (!ForgeEventFactory.onReplaceBiomeBlocks(this, x, z, primer, world))
            return;
        
        double[] noise = surfaceNoise.getRegion(null, x * 16, z * 16, 16, 16, 0.0625, 0.0625, 1.0);
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
    
    @Nullable
    public MapGenEldritchSpire.Start getSpireStart(BlockPos pos) {
        return (MapGenEldritchSpire.Start) spireGenerator.getStructureAt(pos);
    }

}
