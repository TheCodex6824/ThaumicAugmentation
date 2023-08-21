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

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.client.renderer.RenderHandlerNoop;
import thecodex6824.thaumicaugmentation.client.sound.ClientSoundHandler;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeEmptinessBase;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeProviderEmptiness;

public class WorldProviderEmptiness extends WorldProvider {
	
    @Override
    protected void init() {
        hasSkyLight = false;
        biomeProvider = new BiomeProviderEmptiness(world);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            setCloudRenderer(new RenderHandlerNoop());
            setWeatherRenderer(new RenderHandlerNoop());
            //setSkyRenderer(new RenderHandlerEmptinessSky());
        }
    }
    
    @Override
    protected void generateLightBrightnessTable() {
        // slightly boost ambient lighting
        for (int i = 0; i < 16; ++i) {
            float b = 1.0F - i / 15.0F;
            lightBrightnessTable[i] = (1.0F - b) / (b * 3.0F + 1.0F) * 0.95F + 0.05F;
        }
    }
    
    @Override
    public int getAverageGroundLevel() {
        return 128;
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
    @Nullable
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        return null;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0.25F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean doesXZShowFog(int x, int z) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float angle, float partialTicks) {
    	Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
    	double r = 0.0, g = 0.0, b = 0.0;
    	BlockPos start = renderViewEntity.getPosition();
    	MutableBlockPos check = new MutableBlockPos(start);
    	for (int x = -4; x <= 4; ++x) {
    		for (int z = -4; z <= 4; ++z) {
    			check.setPos(start.getX() + x, MathHelper.clamp(start.getY(), 0, 255), start.getZ() + z);
    			Vec3d color = new Vec3d(1.0, 1.0, 1.0);
    			Biome biome = getBiomeForCoords(check);
    			if (biome instanceof BiomeEmptinessBase) {
    	    		color = ((BiomeEmptinessBase) biome).getFogColor(renderViewEntity, angle, partialTicks);
    	    	}
    			
    			r += color.x * color.x;
    			g += color.y * color.y;
    			b += color.z * color.z;
    		}
    	}
    	
    	double voidMod = renderViewEntity.posY >= 0.0 ? 1.0 : (64.0 + renderViewEntity.posY) / 64.0;
    	return new Vec3d(Math.sqrt(r / 100.0) * voidMod, Math.sqrt(g / 100.0) * voidMod, Math.sqrt(b / 100.0) * voidMod);
    }
    
    @Override
    public double getVoidFogYFactor() {
    	Minecraft mc = Minecraft.getMinecraft();
    	Entity renderView = mc.getRenderViewEntity();
    	double annoyingThing = (renderView.lastTickPosY + (renderView.posY - renderView.lastTickPosY) * mc.getRenderPartialTicks());
    	return 1.0 / annoyingThing;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float par1) {
        return 0.0F;
    }

    @Override
    @SideOnly(Side.CLIENT)
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
    
    @Override
    @SideOnly(Side.CLIENT)
    public MusicType getMusicType() {
        return ClientSoundHandler.EMPTINESS_MUSIC_NOOP;
    }

}
