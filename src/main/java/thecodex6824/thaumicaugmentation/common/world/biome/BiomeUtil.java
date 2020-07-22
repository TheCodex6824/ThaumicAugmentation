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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.world.aura.AuraHandler;
import thaumcraft.common.world.biomes.BiomeHandler;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationAuraControl;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationHandler;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationJEID;
import thecodex6824.thaumicaugmentation.common.network.PacketBiomeUpdate;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.util.TriConsumer;

public final class BiomeUtil {

    private BiomeUtil() {}
    
    private static <A, B, C> void invoke(TriConsumer<A, B, C> consumer, A a, B b, C c) {
        consumer.accept(a, b, c);
    }
    
    private static final TriConsumer<World, BlockPos, Biome> JEID_SET_BIOME = 
        (world, pos, biome) -> {
            invoke((w, p, b) -> {
                ((IntegrationJEID) IntegrationHandler.getIntegration(IntegrationHandler.JEID_MOD_ID)).setBiomeJEID(w, p, b);
            }, 
            world, pos, biome);
        };
        
    private static final TriConsumer<World, Integer, Integer> AURACONTROL_HANDLE_AURA = 
        (world, chunkX, chunkZ) -> {
            invoke((w, x, z) -> {
                ((IntegrationAuraControl) IntegrationHandler.getIntegration(IntegrationHandler.AURACONTROL_MOD_ID)).handleAura(w, x, z);
            }, 
            world, chunkX, chunkZ);
        };
    
    public static void setBiome(World world, BlockPos pos, Biome newBiome) {
        if (IntegrationHandler.isIntegrationPresent(IntegrationHandler.JEID_MOD_ID))
            JEID_SET_BIOME.accept(world, pos, newBiome);
        else {
            Chunk chunk = world.getChunk(pos);
            byte[] array = chunk.getBiomeArray();
            array[(pos.getZ() & 15) << 4 | (pos.getX() & 15)] = (byte) Biome.getIdForBiome(newBiome);
            if (!world.isRemote) {
                world.markChunkDirty(pos, null);
                TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64.0);
                TANetwork.INSTANCE.sendToAllTracking(new PacketBiomeUpdate(pos.getX(), pos.getZ(), Biome.getIdForBiome(newBiome)), point);
            }
            else
                world.markBlocksDirtyVertical(pos.getX(), pos.getZ(), 0, 255);
        }
    }
    
    public static void resetBiome(World world, BlockPos pos) {
        Biome[] biomeArray = world.getBiomeProvider().getBiomesForGeneration(null, (pos.getX() >> 2) - 2, (pos.getZ() >> 2) - 2, 1, 1);
        if (biomeArray != null && biomeArray.length > 0) {
            Biome biome = biomeArray[0];
            if (biome != null)
                setBiome(world, pos, biome);
        }
    }
    
    public static Biome getNaturalBiome(World world, BlockPos pos, @Nullable Biome fallback) {
        Biome[] biomeArray = world.getBiomeProvider().getBiomesForGeneration(null, (pos.getX() >> 2) - 2, (pos.getZ() >> 2) - 2, 1, 1);
        if (biomeArray != null && biomeArray.length > 0) {
            Biome biome = biomeArray[0];
            if (biome != null)
                return biome;
        }
        
        return fallback;
    }
    
    public static boolean areBiomesSame(World world, BlockPos pos, Biome first) {
        return world.getBiome(pos) == first;
    }
    
    public static boolean isNaturalBiomePresent(World world, BlockPos pos) {
        Biome natural = getNaturalBiome(world, pos, null);
        return natural != null && natural == world.getBiome(pos);
    }
    
    private static Random copyRand(Random rand) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(rand);
            oos.close();
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()))) {
                return (Random) in.readObject();
            }
        }
        catch (Exception ex) {
            ThaumicAugmentation.getLogger().error("Failed to copy random for generateNewAura");
            throw new RuntimeException(ex);
        }
    }
    
    public static boolean generateNewAura(World world, BlockPos pos, boolean preserveFlux) {
        Biome biome = world.getBiome(pos);
        if (BiomeHandler.getBiomeBlacklist(Biome.getIdForBiome(biome)) != -1)
            return false;
        float life = BiomeHandler.getBiomeAuraModifier(biome);
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            Biome b = world.getBiome(pos.offset(face, 16));
            life += BiomeHandler.getBiomeAuraModifier(b);
        }
        
        life /= 5.0F;
        int target = AuraHelper.getAuraBase(world, pos);
        float flux = AuraHelper.getFlux(world, pos);
        float vis = AuraHelper.getVis(world, pos);
        
        Random chunkRandom = new Random(world.getSeed());
        long xSeed = chunkRandom.nextLong() >> 2 + 1;
        long zSeed = chunkRandom.nextLong() >> 2 + 1;
        chunkRandom.setSeed((xSeed * (pos.getX() >> 16) + zSeed * (pos.getZ() >> 16) ^ world.getSeed()));
        // highest safe number of ints that can be skipped
        for (int i = 0; i < 8; ++i)
            chunkRandom.nextInt();
        
        Random copy = copyRand(chunkRandom);
        boolean solutionFound = false;
        for (int i = 0; i < 1024 * 32; ++i) {
            double g = chunkRandom.nextGaussian();
            // the clamp means this may not be 100% accurate for auras outside that range, but it's better than nothing
            if (MathHelper.clamp((short) ((float) (1.0 + g * 0.10000000149011612) * life * 500.0F), 0, 500) == target) {
                solutionFound = true;
                break;
            }
            else {
                chunkRandom = copy;
                chunkRandom.nextBoolean();
                copy = copyRand(chunkRandom);
            }
        }
        
        Chunk chunk = world.getChunk(pos);
        AuraHandler.generateAura(chunk, copy);
        chunk.markDirty();
        if (IntegrationHandler.isIntegrationPresent(IntegrationHandler.AURACONTROL_MOD_ID)) {
            AURACONTROL_HANDLE_AURA.accept(world, pos.getX() >> 4, pos.getZ() >> 4);
            solutionFound = true;
        }
        
        float applyVis = Math.min(Math.min(vis, AuraHelper.getVis(world, pos)), AuraHelper.getAuraBase(world, pos));
        AuraHelper.drainVis(world, pos, AuraHelper.getVis(world, pos) - applyVis, false);
        if (preserveFlux)
            AuraHelper.polluteAura(world, pos, flux, false);
        
        return solutionFound;
    }
    
    @SuppressWarnings("rawtypes")
    public static Aspect getAspectForType(BiomeDictionary.Type type, Aspect fallback) {
        // yes, TC actually uses a raw list
        List stuff = BiomeHandler.biomeInfo.get(type);
        if (stuff != null && stuff.size() >= 2) {
            Object thing = stuff.get(1);
            if (thing instanceof Aspect)
                return (Aspect) thing;
        }
        
        return fallback;
    }
    
}
