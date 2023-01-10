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

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import thaumcraft.api.aura.AuraHelper;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeEmptinessBase;

public final class TAWorldGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider) {
        
        if (!TAConfig.disableEmptiness.getValue() && world.provider.getDimension() == TADimensions.EMPTINESS.getId()) {
        	BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
            Biome biome = world.getBiome(pos);
            if (biome instanceof BiomeEmptinessBase) {
                float flux = AuraHelper.getAuraBase(world, pos) * ((BiomeEmptinessBase) biome).getBaseFluxConcentration();
                AuraHelper.drainVis(world, pos, flux, false);
                AuraHelper.polluteAura(world, pos, flux, false);
            }
        }
    }

}
