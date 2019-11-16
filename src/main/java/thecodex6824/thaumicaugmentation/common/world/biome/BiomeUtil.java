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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationHandler;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationJEID;
import thecodex6824.thaumicaugmentation.common.network.PacketBiomeUpdate;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.util.TriConsumer;

public final class BiomeUtil {

    private BiomeUtil() {}
    
    private static void invoke(TriConsumer<World, BlockPos, Biome> c, World world, BlockPos pos, Biome biome) {
        c.accept(world, pos, biome);
    }
    
    private static final TriConsumer<World, BlockPos, Biome> JEID_SET_BIOME = 
        (world, pos, biome) -> {
            invoke((w, p, b) -> {
                ((IntegrationJEID) IntegrationHandler.getIntegration(IntegrationHandler.JEID_MOD_ID)).setBiomeJEID(w, pos, b);
            }, 
            world, pos, biome);
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
        Biome[] biomeArray = world.getBiomeProvider().getBiomesForGeneration(null, pos.getX(), pos.getZ(), 1, 1);
        if (biomeArray != null && biomeArray.length > 0) {
            Biome biome = biomeArray[0];
            if (biome != null)
                setBiome(world, pos, biome);
        }
    }
    
    public static Biome getNaturalBiome(World world, BlockPos pos, Biome fallback) {
        Biome[] biomeArray = world.getBiomeProvider().getBiomesForGeneration(null, pos.getX(), pos.getZ(), 1, 1);
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
        Biome[] biomeArray = world.getBiomeProvider().getBiomesForGeneration(null, pos.getX(), pos.getZ(), 1, 1);
        if (biomeArray != null && biomeArray.length > 0)
            return biomeArray[0] == world.getBiome(pos);
        else
            return false;
    }
    
}
