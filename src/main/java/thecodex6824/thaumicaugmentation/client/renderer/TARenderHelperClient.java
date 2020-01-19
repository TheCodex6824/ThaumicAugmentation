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

package thecodex6824.thaumicaugmentation.client.renderer;

import org.lwjgl.opengl.GL11;

import com.sasmaster.glelwjgl.java.CoreGLE;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXFireMote;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.client.fx.FXArcCustom;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager;
import thecodex6824.thaumicaugmentation.client.shader.TAShaders;
import thecodex6824.thaumicaugmentation.common.util.ITARenderHelper;

public class TARenderHelperClient implements ITARenderHelper {

    protected static final ResourceLocation RIFT_TEXTURE = new ResourceLocation("minecraft", "textures/entity/end_portal.png");
    protected static final ResourceLocation BLANK = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/misc/white.png");
    protected static final CoreGLE GLE = new CoreGLE();
    protected static final ResourceLocation FRACTURE_TEXTURE_CLOSED = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/environment/emptiness_sky.png");
    protected static final ResourceLocation FRACTURE_TEXTURE_OPEN = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/environment/emptiness_sky.png");
    protected static final Vec3d[] FRACTURE_POINTS_CLOSED = new Vec3d[] {
            new Vec3d(0.91, 1.99, 0.41),
            new Vec3d(0.85, 1.97, 0.39),
            new Vec3d(0.60, 1.77, 0.45),
            new Vec3d(0.48, 1.35, 0.53),
            new Vec3d(0.29, 1.10, 0.66),
            new Vec3d(0.15, 0.80, 0.78),
            new Vec3d(0.25, 0.45, 0.62),
            new Vec3d(0.45, 0.10, 0.52),
            new Vec3d(0.65, -0.25, 0.38),
            new Vec3d(0.60, -0.55, 0.25),
            new Vec3d(0.50, -0.85, 0.20),
            new Vec3d(0.55, -0.90, 0.23)
    };
    protected static final Vec3d[] FRACTURE_POINTS_OPEN = new Vec3d[] {
            new Vec3d(0.80, 1.99, 0.65),
            new Vec3d(0.75, 1.97, 0.49),
            new Vec3d(0.60, 1.77, 0.50),
            new Vec3d(0.54, 1.35, 0.55),
            new Vec3d(0.56, 1.10, 0.60),
            new Vec3d(0.50, 0.80, 0.50),
            new Vec3d(0.50, 0.15, 0.50),
            new Vec3d(0.59, -0.10, 0.58),
            new Vec3d(0.58, -0.45, 0.52),
            new Vec3d(0.60, -0.75, 0.50),
            new Vec3d(0.50, -0.95, 0.20),
            new Vec3d(0.27, -0.98, 0.16)
    };
    
    protected static final double[] FRACTURE_WIDTHS_CLOSED = new double[] {
            0,
            0.00052,
            0.0051,
            0.0056,
            0.008,
            0.009,
            0.009,
            0.0074,
            0.0056,
            0.0041,
            0.00042,
            0
    };
    protected static final double[] FRACTURE_WIDTHS_OPEN = new double[] {
            0,
            0.00052,
            0.152,
            0.279,
            0.316,
            0.350,
            0.350,
            0.328,
            0.272,
            0.152,
            0.00052,
            0
    };
    
    @Override
    public void renderGlowingSphere(World world, double x, double y, double z, int color) {
        FXFireMote sphere = new FXFireMote(world, x, y, z, 0, 0, 0,
                ((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F,
                3.0F, 0);
        sphere.setMaxAge(48);
        ParticleEngine.addEffect(world, sphere);
    }
    
    @Override
    public void renderBurst(World world, double x, double y, double z, float size, int color) {
        FXGeneric fx = new FXGeneric(world, x, y, z, 0, 0, 0);
        fx.setMaxAge(31);
        fx.setGridSize(16);
        fx.setParticles(208, 31, 1);
        fx.setScale(size);
        fx.setRBGColorF(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
        ParticleEngine.addEffect(world, fx);
    }
    
    @Override
    public void renderSpark(World world, double x, double y, double z, float size, int color, boolean colorAlpha) {
        FXDispatcher.INSTANCE.spark(x, y, z, size, ((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F, colorAlpha ? ((color >> 24) & 0xFF) / 255.0F : 1.0F);
    }
    
    @Override
    public void renderArc(World world, double x, double y, double z, double dx, double dy, double dz, int color, double height) {
        FXArcCustom arc = new FXArcCustom(world, x, y, z, dx, dy, dz, ((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F, height, 0.2, 0.0625, 0.125F);
        arc.setMaxAge(5 + world.rand.nextInt(5));
        ParticleEngine.addEffect(world, arc);
    }
    
    protected void renderFluxRiftShared(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, boolean goggles) {
        float stab = Math.max(Math.min(1.0F - stability / 50.0F, 1.5F), 0.0F);
        for (int layer = 0; layer < 4; ++layer) {
            if (layer < 3) {
                GlStateManager.depthMask(false);
                if (layer == 0 && goggles)
                    GlStateManager.disableDepth();
            }
            
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, layer != 3 ? DestFactor.ONE : DestFactor.ONE_MINUS_SRC_ALPHA);
            if (rift.getPoints().length > 2) {
                GL11.glPushMatrix();
                double[][] points = new double[rift.getPoints().length][3];
                float[][] colors = new float[rift.getPoints().length][4];
                double[] widths = new double[rift.getPoints().length];
                for (int i = 0; i < rift.getPoints().length; ++i) {
                    float time = Minecraft.getMinecraft().player.ticksExisted + partialTicks;
                    if (i > rift.getPoints().length / 2)
                        time -= i * 10;
                    else if (i < rift.getPoints().length / 2)
                        time += i * 10;
                    
                    points[i][0] = rift.getPoints()[i].x + Math.sin(time / 50.0) * 0.10000000149011612 * stab;
                    points[i][1] = rift.getPoints()[i].y + Math.sin(time / 60.0) * 0.10000000149011612 * stab;
                    points[i][2] = rift.getPoints()[i].z + Math.sin(time / 70.0) * 0.10000000149011612 * stab;
                    
                    colors[i][0] = 1.0F;
                    colors[i][1] = 1.0F;
                    colors[i][2] = 1.0F;
                    colors[i][3] = 1.0F;
                    
                    widths[i] = rift.getWidths()[i] * (1.0 - Math.sin(time / 8.0) * 0.10000000149011612 * stab) *
                            (layer < 3 ? 1.25 + 0.5 * layer : 1.0);
                }
                
                GLE.set_POLYCYL_TESS(tessLevel);
                GLE.gleSetJoinStyle(CoreGLE.TUBE_JN_ANGLE);
                GLE.glePolyCone(points.length, points, colors, widths, 1.0F, 0.0F);
                GL11.glPopMatrix();
            }
            
            if (layer < 3) {
                GlStateManager.depthMask(true);
                if (layer == 0 && goggles)
                    GlStateManager.enableDepth();
            }
        }
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    @Override
    public void renderFluxRift(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, boolean ignoreGoggles) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(RIFT_TEXTURE);
        TAShaderManager.enableShader(TAShaders.FLUX_RIFT, TAShaders.FLUX_RIFT_SHADER_CALLBACK);
        GlStateManager.enableBlend();
        renderFluxRiftShared(rift, stability, partialTicks, tessLevel, !ignoreGoggles && EntityUtils.hasGoggles(Minecraft.getMinecraft().player));
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        TAShaderManager.disableShader();
        GL11.glPopMatrix();
    }
    
    protected void renderFluxRiftSingleLayer(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, boolean disableDepth, float r, float g, float b, float a, int joinType) {
        float stab = Math.max(Math.min(1.0F - stability / 50.0F, 1.5F), 0.0F);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        if (disableDepth) {
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
        }
        if (rift.getPoints().length > 2) {
            GL11.glPushMatrix();
            double[][] points = new double[rift.getPoints().length][3];
            float[][] colors = new float[rift.getPoints().length][4];
            double[] widths = new double[rift.getPoints().length];
            for (int i = 0; i < rift.getPoints().length; ++i) {
                float time = Minecraft.getMinecraft().player.ticksExisted + partialTicks;
                if (i > rift.getPoints().length / 2)
                    time -= i * 10;
                else if (i < rift.getPoints().length / 2)
                    time += i * 10;
                
                points[i][0] = rift.getPoints()[i].x + Math.sin(time / 50.0) * 0.10000000149011612 * stab;
                points[i][1] = rift.getPoints()[i].y + Math.sin(time / 60.0) * 0.10000000149011612 * stab;
                points[i][2] = rift.getPoints()[i].z + Math.sin(time / 70.0) * 0.10000000149011612 * stab;
                
                colors[i][0] = r;
                colors[i][1] = g;
                colors[i][2] = b;
                colors[i][3] = a;
                
                widths[i] = rift.getWidths()[i] * (1.0 - Math.sin(time / 8.0) * 0.10000000149011612 * stab);
            }
            
            GLE.set_POLYCYL_TESS(tessLevel);
            GLE.gleSetJoinStyle(joinType);
            GLE.glePolyCone(points.length, points, colors, widths, 1.0F, 0.0F);
            GL11.glPopMatrix();
        }
        
        if (disableDepth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    @Override
    public void renderFluxRiftOutline(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType) {
        GL11.glPushMatrix();
        if (bindTexture)
            Minecraft.getMinecraft().renderEngine.bindTexture(RIFT_TEXTURE);
        
        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        renderFluxRiftSingleLayer(rift, stability, partialTicks, tessLevel, false, r, g, b, a, joinType);
        GlStateManager.enableCull();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glPopMatrix();
    }
    
    @Override
    public void renderFluxRiftSolidLayer(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType) {
        GL11.glPushMatrix();
        if (bindTexture)
            Minecraft.getMinecraft().renderEngine.bindTexture(BLANK);
        
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        renderFluxRiftSingleLayer(rift, stability, partialTicks, tessLevel, false, r, g, b, a, joinType);
        GlStateManager.enableLighting();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }
    
    protected double lerp(double initial, double last, double currentTime, double timeOpened, double totalTime) {
        double factor = MathHelper.clamp((currentTime - timeOpened) / totalTime, 0, 1.0);
        return (1.0 - factor) * initial + factor * last;
    }
    
    protected void renderDimensionalFractureSingleLayer(boolean open, long worldTime, long timeOpened, long openingDuration, float partialTicks, int tessLevel, float r, float g, float b, float a, int joinType) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.pushMatrix();
        double[][] pointBuffer = new double[FRACTURE_POINTS_CLOSED.length][3];
        float[][] colorBuffer = new float[FRACTURE_POINTS_CLOSED.length][4];
        double[] radiusBuffer = new double[FRACTURE_POINTS_CLOSED.length];
        for (int i = 0; i < FRACTURE_POINTS_CLOSED.length; ++i) {
            double time = worldTime + partialTicks;
            if (i > FRACTURE_POINTS_CLOSED.length / 2)
                time -= i * 10;
            else if (i < FRACTURE_POINTS_CLOSED.length / 2)
                time += i * 10;

            Vec3d rotatedClosed = FRACTURE_POINTS_CLOSED[i].add(-0.5, 1.0, -0.5);
            Vec3d rotatedOpen = FRACTURE_POINTS_OPEN[i].add(-0.5, 1.0, -0.5);
            pointBuffer[i][0] = lerp(rotatedClosed.x, rotatedOpen.x, worldTime, timeOpened, openingDuration) + Math.sin(time / 50) * lerp(0.1, 0.01, worldTime, timeOpened, openingDuration);
            pointBuffer[i][1] = lerp(rotatedClosed.y, rotatedOpen.y, worldTime, timeOpened, openingDuration) + Math.sin(time / 60) * lerp(0.1, 0.01, worldTime, timeOpened, openingDuration);
            pointBuffer[i][2] = lerp(rotatedClosed.z, rotatedOpen.z, worldTime, timeOpened, openingDuration) + Math.sin(time / 70) * lerp(0.1, 0.01, worldTime, timeOpened, openingDuration);
            
            colorBuffer[i][0] = r;
            colorBuffer[i][1] = g;
            colorBuffer[i][2] = b;
            colorBuffer[i][3] = (float) lerp(a / 4.0, a, worldTime, timeOpened, openingDuration);

            double widthMultiplier = 1.0 - Math.sin(time / 8) * 0.1;
            radiusBuffer[i] = lerp(FRACTURE_WIDTHS_CLOSED[i], FRACTURE_WIDTHS_OPEN[i], worldTime, timeOpened, openingDuration) * widthMultiplier;
        }

        GLE.set_POLYCYL_TESS(tessLevel);
        GLE.gleSetJoinStyle(joinType);
        GLE.glePolyCone(pointBuffer.length, pointBuffer, colorBuffer, radiusBuffer, 1.0F, 0.0F);

        GlStateManager.popMatrix();
    }
    
    @Override
    public void renderDimensionalFracture(boolean open, long worldTime, long timeOpened, long openingDuration, float partialTicks, int tessLevel, boolean ignoreGoggles, float r, float g, float b, float a) {
        GlStateManager.pushMatrix();
        boolean isRevealing = !ignoreGoggles && EntityUtils.hasGoggles(Minecraft.getMinecraft().getRenderViewEntity() != null ?
                Minecraft.getMinecraft().getRenderViewEntity() : Minecraft.getMinecraft().player);
        Minecraft.getMinecraft().renderEngine.bindTexture(open ? FRACTURE_TEXTURE_OPEN : FRACTURE_TEXTURE_CLOSED);
        TAShaderManager.enableShader(TAShaders.FRACTURE, TAShaders.FRACTURE_SHADER_CALLBACK);
        GlStateManager.enableBlend();
        for (int layer = 0; layer < 4; ++layer) {
            if (layer != 3) {
                GlStateManager.depthMask(false);
                if (layer == 0 && isRevealing)
                    GlStateManager.disableDepth();
            }

            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, layer != 3 ? DestFactor.ONE : DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.pushMatrix();
            double[][] pointBuffer = new double[FRACTURE_POINTS_CLOSED.length][3];
            float[][] colorBuffer = new float[FRACTURE_POINTS_CLOSED.length][4];
            double[] radiusBuffer = new double[FRACTURE_POINTS_CLOSED.length];
            for (int i = 0; i < FRACTURE_POINTS_CLOSED.length; ++i) {
                double time = worldTime + partialTicks;
                if (i > FRACTURE_POINTS_CLOSED.length / 2)
                    time -= i * 10;
                else if (i < FRACTURE_POINTS_CLOSED.length / 2)
                    time += i * 10;

                Vec3d rotatedClosed = FRACTURE_POINTS_CLOSED[i].add(-0.5, 1.0, -0.5);
                Vec3d rotatedOpen = FRACTURE_POINTS_OPEN[i].add(-0.5, 1.0, -0.5);
                pointBuffer[i][0] = lerp(rotatedClosed.x, rotatedOpen.x, worldTime, timeOpened, openingDuration) + Math.sin(time / 50) * lerp(0.1, 0.01, worldTime, timeOpened, openingDuration);
                pointBuffer[i][1] = lerp(rotatedClosed.y, rotatedOpen.y, worldTime, timeOpened, openingDuration) + Math.sin(time / 60) * lerp(0.1, 0.01, worldTime, timeOpened, openingDuration);
                pointBuffer[i][2] = lerp(rotatedClosed.z, rotatedOpen.z, worldTime, timeOpened, openingDuration) + Math.sin(time / 70) * lerp(0.1, 0.01, worldTime, timeOpened, openingDuration);
                
                colorBuffer[i][0] = r;
                colorBuffer[i][1] = g;
                colorBuffer[i][2] = b;
                colorBuffer[i][3] = (float) lerp(a / 4.0, a, worldTime, timeOpened, openingDuration);

                double widthMultiplier = 1.0 - Math.sin(time / 8) * 0.1;
                radiusBuffer[i] = lerp(FRACTURE_WIDTHS_CLOSED[i], FRACTURE_WIDTHS_OPEN[i], worldTime, timeOpened, openingDuration) * widthMultiplier * (layer != 3 ? 1.25 + 0.5 * layer : 1.0);
            }

            GLE.set_POLYCYL_TESS(tessLevel);
            GLE.gleSetJoinStyle(CoreGLE.TUBE_JN_ANGLE);
            GLE.glePolyCone(pointBuffer.length, pointBuffer, colorBuffer, radiusBuffer, 1.0F, 0.0F);

            GlStateManager.popMatrix();
            if (layer != 3) {
                GlStateManager.depthMask(true);
                if (layer == 0 && isRevealing)
                    GlStateManager.enableDepth();
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        TAShaderManager.disableShader();
        GlStateManager.popMatrix();
    }
    
    @Override
    public void renderDimensionalFractureSolidLayer(boolean open, long worldTime, long timeOpened, long openingDuration, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType) {
        GL11.glPushMatrix();
        if (bindTexture)
            Minecraft.getMinecraft().renderEngine.bindTexture(BLANK);
        
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        renderDimensionalFractureSingleLayer(open, worldTime, timeOpened, openingDuration, partialTicks, tessLevel, r, g, b, a, joinType);
        GlStateManager.enableLighting();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }
    
    @Override
    public void renderWispyMotes(World world, double x, double y, double z, double dx, double dy, double dz, int age,
            float r, float g, float b, float gravity) {
        
        FXDispatcher.INSTANCE.drawWispyMotes(x, y, z, dx, dy, dz, age, r, g, b, gravity);
    }
    
    @Override
    public void renderFireMote(World world, float x, float y, float z, float vx, float vy, float vz, float r, float g,
            float b, float a, float scale) {
        
        FXDispatcher.INSTANCE.drawFireMote(x, y, z, vx, vy, vz, r, g, b, a, scale);
    }
    
    @Override
    public void renderSmokeSpiral(World world, double x, double y, double z, float rad, int start, int minY,
            int color) {
        
        FXDispatcher.INSTANCE.smokeSpiral(x, y, z, rad, start, minY, color);
    }
    
    @Override
    public void renderTerraformerParticle(World world, double x, double y, double z, double vx, double vy, double vz,
            BlockPos pos, Biome biome) {
        
        int color = world.rand.nextInt(3);
        if (color == 0)
            color = biome.getGrassColorAtPos(pos);
        else if (color == 1)
            color = biome.getFoliageColorAtPos(pos);
        else {
            if (biome == Biomes.HELL)
                color = 0xFF4500;
            else
                color = biome.getWaterColor() & 0x3F76E4;
        }
        
        FXGeneric fx = new FXGeneric(world, x, y, z, vx, vy, vz);
        fx.setMaxAge(30 + world.rand.nextInt(12));
        fx.setRBGColorF(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
        fx.setAlphaF(0.9F, 0.0F);
        fx.setGridSize(16);
        fx.setParticles(56, 1, 1);
        fx.setScale(4.0F);
        fx.setLayer(1);
        fx.setLoop(true);
        fx.setNoClip(false); // this is REALLY poorly named, it actually should be "setCollides", as that's what it does
        fx.setRotationSpeed(world.rand.nextFloat(), world.rand.nextBoolean() ? 1.0F : -1.0F);
        ParticleEngine.addEffect(world, fx);
    }
    
    @Override
    public void renderRiftMoverParticle(World world, double x, double y, double z, double vx, double vy,
            double vz) {
        
        FXGeneric fx = new FXGeneric(world, x, y, z, vx, vy, vz);
        fx.setMaxAge(12 + world.rand.nextInt(6));
        fx.setRBGColorF(0.044F, 0.036F, 0.063F);
        fx.setAlphaF(0.75F);
        fx.setGridSize(64);
        fx.setParticles(264, 8, 1);
        fx.setScale(2.0F);
        fx.setLayer(1);
        fx.setLoop(true);
        fx.setNoClip(false);
        fx.setRotationSpeed(world.rand.nextFloat(), world.rand.nextBoolean() ? 1.0F : -1.0F);
        ParticleEngine.addEffect(world, fx);
    }
    
    @Override
    public boolean shadersAvailable() {
        return TAShaderManager.shouldUseShaders();
    }
    
    @Override
    public boolean stencilAvailable() {
        return GL11.glGetInteger(GL11.GL_STENCIL_BITS) > 0;
    }

}
