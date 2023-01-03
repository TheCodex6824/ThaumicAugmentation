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

import java.io.File;

import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.WorldCapabilityData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.api.world.capability.CapabilityTAWorldGenerationVersion;
import thecodex6824.thaumicaugmentation.api.world.capability.ITAWorldGenerationVersion;
import thecodex6824.thaumicaugmentation.api.world.capability.TAWorldGenerationVersion;
import thecodex6824.thaumicaugmentation.common.capability.provider.SimpleCapabilityProvider;
import thecodex6824.thaumicaugmentation.common.world.legacy.WorldProviderImplEmptinessLegacy;
import thecodex6824.thaumicaugmentation.common.world.v2.WorldProviderImplEmptinessV2;

public class WorldProviderEmptiness extends WorldProvider {
    
	protected WorldProvider impl;
	protected int stashedVersion;
	
    @Override
    protected void init() {
    	if (!world.isRemote) {
	    	ICapabilityProvider temp = new SimpleCapabilityProvider<>(new TAWorldGenerationVersion(), CapabilityTAWorldGenerationVersion.WORLDGEN_VERSION);
	        CapabilityDispatcher disp = ForgeEventFactory.gatherCapabilities(world, temp);
	        WorldCapabilityData data = (WorldCapabilityData) world.getPerWorldStorage().getOrLoadData(WorldCapabilityData.class, WorldCapabilityData.ID);
	        if (data != null) {
	            data.setCapabilities(this, disp);
	            ITAWorldGenerationVersion version = disp.getCapability(CapabilityTAWorldGenerationVersion.WORLDGEN_VERSION, null);
	            if (version != null) {
	            	stashedVersion = version.getVersion();
	            }
	            else {
	            	File saveDir = new File(world.getSaveHandler().getWorldDirectory(), "DIM" + getDimension());
	            	if (!saveDir.exists()) {
	            		stashedVersion = TADimensions.WORLDGEN_V2;
	            	}
	            }
	        }
	    	
	    	if (stashedVersion <= TADimensions.WORLDGEN_LEGACY) {
	    		impl = new WorldProviderImplEmptinessLegacy();
	    	}
	    	else if (stashedVersion == TADimensions.WORLDGEN_V2){
	    		impl = new WorldProviderImplEmptinessV2();
	    	}
	    	else {
	    		throw new IllegalArgumentException("Invalid world generation version");
	    	}
	    	
	    	impl.setDimension(getDimension());
	    	impl.setWorld(world);
	    	impl.setSpawnPoint(super.getSpawnPoint());
    	}
    	else {
    		impl = new WorldProviderImplEmptinessV2();
    		impl.setDimension(getDimension());
	    	impl.setWorld(world);
	    	impl.setSpawnPoint(super.getSpawnPoint());
    		stashedVersion = TADimensions.WORLDGEN_V2;
    	}
    }
    
    @Override
    public ICapabilityProvider initCapabilities() {
    	TAWorldGenerationVersion version = new TAWorldGenerationVersion();
    	version.setVersion(stashedVersion);
    	return new SimpleCapabilityProvider<>(version, CapabilityTAWorldGenerationVersion.WORLDGEN_VERSION);
    }
    
    @Override
    public void calculateInitialWeather() {
    	impl.calculateInitialWeather();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
    	return impl.calcSunriseSunsetColors(celestialAngle, partialTicks);
    }
    
    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
    	return impl.calculateCelestialAngle(worldTime, partialTicks);
    }
    
    @Override
    public boolean canBlockFreeze(BlockPos pos, boolean byWater) {
    	return impl.canBlockFreeze(pos, byWater);
    }
    
    @Override
    public DimensionType getDimensionType() {
    	return impl.getDimensionType();
    }
    
    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
    	return impl.canCoordinateBeSpawn(x, z);
    }
    
    @Override
    public boolean canDoLightning(Chunk chunk) {
    	return impl.canDoLightning(chunk);
    }
    
    @Override
    public boolean canDoRainSnowIce(Chunk chunk) {
    	return impl.canDoRainSnowIce(chunk);
    }
    
    @Override
    public boolean canDropChunk(int x, int z) {
    	return impl.canDropChunk(x, z);
    }
    
    @Override
    public boolean canMineBlock(EntityPlayer player, BlockPos pos) {
    	return impl.canMineBlock(player, pos);
    }
    
    @Override
    public boolean canRespawnHere() {
    	return impl.canRespawnHere();
    }
    
    @Override
    public WorldSleepResult canSleepAt(EntityPlayer player, BlockPos pos) {
    	return impl.canSleepAt(player, pos);
    }
    
    @Override
    public boolean canSnowAt(BlockPos pos, boolean checkLight) {
    	return impl.canSnowAt(pos, checkLight);
    }
    
    @Override
    public IChunkGenerator createChunkGenerator() {
    	return impl.createChunkGenerator();
    }
    
    @Override
    public WorldBorder createWorldBorder() {
    	return super.createWorldBorder();
    }
    
    @Override
    public boolean doesWaterVaporize() {
    	return impl.doesWaterVaporize();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean doesXZShowFog(int x, int z) {
    	return impl.doesXZShowFog(x, z);
    }
    
    @Override
    public int getActualHeight() {
    	return impl.getActualHeight();
    }
    
    @Override
    public int getAverageGroundLevel() {
    	return impl.getAverageGroundLevel();
    }
    
    @Override
    public Biome getBiomeForCoords(BlockPos pos) {
    	return impl.getBiomeForCoords(pos);
    }
    
    @Override
    public BiomeProvider getBiomeProvider() {
    	return impl.getBiomeProvider();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColor(float partialTicks) {
    	return impl.getCloudColor(partialTicks);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public float getCloudHeight() {
    	return impl.getCloudHeight();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public IRenderHandler getCloudRenderer() {
    	return impl.getCloudRenderer();
    }
    
    @Override
    public float getCurrentMoonPhaseFactor() {
    	return impl.getCurrentMoonPhaseFactor();
    }
    
    @Override
    public int getDimension() {
    	// forge checks the dim id for consistency, and the impl might not be ready yet
    	return super.getDimension();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float x, float z) {
    	return impl.getFogColor(x, z);
    }
    
    @Override
    public int getHeight() {
    	return impl.getHeight();
    }
    
    @Override
    public double getHorizon() {
    	return impl.getHorizon();
    }
    
    @Override
    public float[] getLightBrightnessTable() {
    	return impl.getLightBrightnessTable();
    }
    
    @Override
    public void getLightmapColors(float partialTicks, float sunBrightness, float skyLight, float blockLight,
    		float[] colors) {
    	
    	impl.getLightmapColors(partialTicks, sunBrightness, skyLight, blockLight, colors);
    }
    
    @Override
    public int getMoonPhase(long worldTime) {
    	return impl.getMoonPhase(worldTime);
    }
    
    @Override
    public double getMovementFactor() {
    	return impl.getMovementFactor();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public MusicType getMusicType() {
    	return impl.getMusicType();
    }
    
    @Override
    public BlockPos getRandomizedSpawnPoint() {
    	return impl.getRandomizedSpawnPoint();
    }
    
    @Override
    public int getRespawnDimension(EntityPlayerMP player) {
    	return impl.getRespawnDimension(player);
    }
    
    @Override
    public String getSaveFolder() {
    	return impl.getSaveFolder();
    }
    
    @Override
    public long getSeed() {
    	return impl.getSeed();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
    	return impl.getSkyColor(cameraEntity, partialTicks);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public IRenderHandler getSkyRenderer() {
    	return impl.getSkyRenderer();
    }
    
    @Override
    public BlockPos getSpawnCoordinate() {
    	return impl.getSpawnCoordinate();
    }
    
    @Override
    public BlockPos getSpawnPoint() {
    	return impl.getSpawnPoint();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float partialTicks) {
    	return impl.getStarBrightness(partialTicks);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float partialTicks) {
    	return impl.getSunBrightness(partialTicks);
    }
    
    @Override
    public float getSunBrightnessFactor(float lightAmount) {
    	return impl.getSunBrightnessFactor(lightAmount);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public double getVoidFogYFactor() {
    	return impl.getVoidFogYFactor();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public IRenderHandler getWeatherRenderer() {
    	return impl.getWeatherRenderer();
    }
    
    @Override
    public long getWorldTime() {
    	return impl.getWorldTime();
    }
    
    @Override
    public boolean hasSkyLight() {
    	return impl.hasSkyLight();
    }
    
    @Override
    public boolean isBlockHighHumidity(BlockPos pos) {
    	return impl.isBlockHighHumidity(pos);
    }
    
    @Override
    public boolean isDaytime() {
    	return impl.isDaytime();
    }
    
    @Override
    public boolean isNether() {
    	return impl.isNether();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean isSkyColored() {
    	return impl.isSkyColored();
    }
    
    @Override
    public boolean isSurfaceWorld() {
    	return impl.isSurfaceWorld();
    }
    
    @Override
    public void onPlayerAdded(EntityPlayerMP player) {
    	impl.onPlayerAdded(player);
    }
    
    @Override
    public void onPlayerRemoved(EntityPlayerMP player) {
    	impl.onPlayerRemoved(player);
    }
    
    @Override
    public void onWorldSave() {
    	impl.onWorldSave();
    }
    
    @Override
    public void onWorldUpdateEntities() {
    	impl.onWorldUpdateEntities();
    }
    
    @Override
    public void resetRainAndThunder() {
    	impl.resetRainAndThunder();
    }
    
    @Override
    public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful) {
    	impl.setAllowedSpawnTypes(allowHostile, allowPeaceful);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void setCloudRenderer(IRenderHandler renderer) {
    	impl.setCloudRenderer(renderer);
    }
    
    @Override
    public void setDimension(int dim) {
    	super.setDimension(dim);
    	if (impl != null) {
    		impl.setDimension(dim);
    	}
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void setSkyRenderer(IRenderHandler skyRenderer) {
    	impl.setSkyRenderer(skyRenderer);
    }
    
    @Override
    public void setSpawnPoint(BlockPos pos) {
    	super.setSpawnPoint(pos);
    	if (impl != null) {
    		impl.setSpawnPoint(pos);
    	}
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void setWeatherRenderer(IRenderHandler renderer) {
    	impl.setWeatherRenderer(renderer);
    }
    
    @Override
    public void setWorldTime(long time) {
    	impl.setWorldTime(time);
    }
    
    @Override
    public boolean shouldClientCheckLighting() {
    	return impl.shouldClientCheckLighting();
    }
    
    @Override
    public boolean shouldMapSpin(String entity, double x, double z, double rotation) {
    	return impl.shouldMapSpin(entity, x, z, rotation);
    }
    
    @Override
    public void updateWeather() {
    	impl.updateWeather();
    }

}
