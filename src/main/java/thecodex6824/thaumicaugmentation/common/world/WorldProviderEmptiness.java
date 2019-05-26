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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.client.renderer.RenderHandlerEmptinessSky;
import thecodex6824.thaumicaugmentation.client.renderer.RenderHandlerNoop;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeProviderEmptiness;

public class WorldProviderEmptiness extends WorldProvider {
	
	@Override
	protected void init() {
		hasSkyLight = false;
		biomeProvider = new BiomeProviderEmptiness(world);
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			setCloudRenderer(new RenderHandlerNoop());
			setWeatherRenderer(new RenderHandlerNoop());
			setSkyRenderer(new RenderHandlerEmptinessSky());
		}
	}
	
	@Override
	public int getAverageGroundLevel() {
		return 8;
	}

	@Override
	public double getMovementFactor() {
		return TAConfig.emptinessMoveFactor.getValue();
	}

	@Override
	public DimensionType getDimensionType() {
		return TADimensions.EMPTINESS;
	}

	@Override
	public BiomeProvider getBiomeProvider() {
		return biomeProvider;
	}

	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkGeneratorEmptiness(world);
	}

	@Override
	public boolean canDoLightning(Chunk chunk) {
		return false;
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		return false;
	}

	@Override
	public float getSunBrightnessFactor(float par1) {
		return 0.0F;
	}

	@Override
	public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
		return null;
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		return 0.0F;
	}

	@Override
	public boolean doesXZShowFog(int x, int z) {
		return false;
	}

	@Override
	public Vec3d getFogColor(float x, float z) {
		return new Vec3d(0, 0, 0);
	}

	@Override
	public float getSunBrightness(float par1) {
		return 0.0F;
	}

	@Override
	public float getStarBrightness(float par1) {
		return 0.0F;
	}

	@Override
	public boolean isSurfaceWorld() {
		return false;
	}

	@Override
	public boolean canRespawnHere() {
		return false;
	}

	@Override
	public WorldSleepResult canSleepAt(EntityPlayer player, BlockPos pos) {
		return WorldSleepResult.DENY;
	}

	@Override
	public boolean shouldMapSpin(String entity, double x, double z, double rotation) {
		return true;
	}

}
