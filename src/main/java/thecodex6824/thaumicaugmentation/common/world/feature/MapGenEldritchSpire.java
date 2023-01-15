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

package thecodex6824.thaumicaugmentation.common.world.feature;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.structure.*;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ward.WardHelper;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorageServer;
import thecodex6824.thaumicaugmentation.api.world.TABiomes;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAEldritchGuardian;
import thecodex6824.thaumicaugmentation.common.world.ITAChunkGenerator;
import thecodex6824.thaumicaugmentation.common.world.structure.EldritchSpireComponent;
import thecodex6824.thaumicaugmentation.common.world.structure.EldritchSpireComponentPlacer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MapGenEldritchSpire extends MapGenStructure {

    protected static final ImmutableList<SpawnListEntry> MONSTER_SPAWNS = ImmutableList.of(
            new SpawnListEntry(EntityTAEldritchGuardian.class, 1, 1, 2));
    
    protected static final ImmutableList<Biome> BIOMES = ImmutableList.of(
            TABiomes.EMPTINESS, TABiomes.EMPTINESS_HIGHLANDS);
    
    static {
        MapGenStructureIO.registerStructure(Start.class, "EldritchSpire");
        EldritchSpireComponentPlacer.register();
    }
    
    protected ITAChunkGenerator generator;
    
    public MapGenEldritchSpire(ITAChunkGenerator chunkGenerator) {
        generator = chunkGenerator;
    }
    
    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int dist = TAConfig.spireSpacing.getValue();
        int x = chunkX;
        int z = chunkZ;
        
        if (x < 0)
            x -= dist;
        
        if (z < 0)
            z -= dist;
        
        int randX = x / dist;
        int randZ = z / dist;
        Random random = world.setRandomSeed(randX, randZ, 10387319);
        randX *= dist;
        randZ *= dist;
        randX += (random.nextInt(dist / 2 - 1) + random.nextInt(dist / 2 - 1)) / 2;
        randZ += (random.nextInt(dist / 2 - 1) + random.nextInt(dist / 2 - 1)) / 2;
        if (chunkX == randX && chunkZ == randZ)
            return world.provider.getBiomeProvider().areBiomesViable(chunkX * 16 + 8, chunkZ * 16 + 8, 32, BIOMES);
        
        return false;
    }
    
    @Override
    @Nullable
    public BlockPos getNearestStructurePos(World world, BlockPos pos, boolean findUnexplored) {
        this.world = world;
        BiomeProvider p = world.getBiomeProvider();
        if (p.isFixedBiome() && !BIOMES.contains(p.getFixedBiome()))
            return null;
        else {
            return findNearestStructurePosBySpacing(world, this, pos, TAConfig.spireSpacing.getValue(),
                    TAConfig.spireMinDist.getValue(), 10387319, true, 100, findUnexplored);
        }
    }
    
    @Override
    public String getStructureName() {
        return "EldritchSpire";
    }
    
    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new Start(world, generator, rand, chunkX, chunkZ);
    }
    
    @Override
    @Nullable
    public StructureStart getStructureAt(BlockPos pos) {
        return super.getStructureAt(pos);
    }
    
    public List<SpawnListEntry> getSpawnableCreatures(EnumCreatureType type, BlockPos pos) {
        // random chance to lessen mob spawn spam
        if (type == EnumCreatureType.MONSTER && ThreadLocalRandom.current().nextBoolean())
            return MONSTER_SPAWNS;
        else
            return ImmutableList.of();
    }
    
    public static class Start extends StructureStart {
        
        protected boolean valid;
        protected UUID ward;
        
        public Start() {
            super();
            ward = IWardStorageServer.NIL_UUID;
        }
        
        public Start(World world, ITAChunkGenerator generator, Random random, int chunkX, int chunkZ) {
            super(chunkX, chunkZ);
            ward = WardHelper.generateSafeUUID(random);
            Rotation rot = Rotation.values()[random.nextInt(Rotation.values().length)];
            ChunkPrimer primer = new ChunkPrimer();
            generator.populatePrimerWithHeightmap(chunkX, chunkZ, primer);
            
            int x = 5, z = 5;
            if (rot == Rotation.CLOCKWISE_90)
                x = -5;
            else if (rot == Rotation.CLOCKWISE_180) { 
                x = -5;
                z = -5;
            }
            else if (rot == Rotation.COUNTERCLOCKWISE_90)
                z = -5;
            
            int height1 = primer.findGroundBlockIdx(7, 7);
            int height2 = primer.findGroundBlockIdx(7, 7 + z);
            int height3 = primer.findGroundBlockIdx(7 + x, 7);
            int height4 = primer.findGroundBlockIdx(7 + x, 7 + z);
            int minHeight = Math.min(Math.min(height1, height2), Math.min(height3, height4));
            if (minHeight >= 2) {
                BlockPos pos = new BlockPos(chunkX * 16 + 8, minHeight + 1, chunkZ * 16 + 8);
                List<EldritchSpireComponent> pieces = new ArrayList<>();
                EldritchSpireComponentPlacer.generate(world, generator, world.getSaveHandler().getStructureTemplateManager(),
                        pos, rot, random, pieces, ward);
                components.addAll(pieces);
                updateBoundingBox();
                valid = true;
            }
        }
        
        @Override
        public void generateStructure(World world, Random rand, StructureBoundingBox structurebb) {
            // actually generate structure
            super.generateStructure(world, rand, structurebb);
            
            // do post-gen stuff (including filling below when needed)
            for (StructureComponent component : components) {
                if (component instanceof EldritchSpireComponent)
                    ((EldritchSpireComponent) component).onPostGeneration(world, structurebb);
            }
        }
        
        @Override
        public boolean isSizeableStructure() {
            return valid;
        }
        
        public UUID getWard() {
            return ward;
        }
        
        @Override
        public void writeToNBT(NBTTagCompound tagCompound) {
            super.writeToNBT(tagCompound);
            tagCompound.setBoolean("valid", valid);
            tagCompound.setUniqueId("ward", ward);
        }
        
        @Override
        public void readFromNBT(NBTTagCompound tagCompound) {
            super.readFromNBT(tagCompound);
            valid = tagCompound.getBoolean("valid");
            ward = tagCompound.getUniqueId("ward");
            if (ward == null)
                ward = IWardStorageServer.NIL_UUID;
        }
        
    }
    
}
