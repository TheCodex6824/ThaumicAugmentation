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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
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
    protected static final CoreGLE RIFT_RENDERER = new CoreGLE();
    
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
                
                RIFT_RENDERER.set_POLYCYL_TESS(tessLevel);
                RIFT_RENDERER.gleSetJoinStyle(CoreGLE.TUBE_JN_ANGLE);
                RIFT_RENDERER.glePolyCone(points.length, points, colors, widths, 1.0F, 0.0F);
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
    
    protected void renderFluxRiftSingleLayer(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean disableDepth) {
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
            
            RIFT_RENDERER.set_POLYCYL_TESS(tessLevel);
            RIFT_RENDERER.gleSetJoinStyle(CoreGLE.TUBE_JN_ANGLE);
            RIFT_RENDERER.glePolyCone(points.length, points, colors, widths, 1.0F, 0.0F);
            GL11.glPopMatrix();
        }
        
        if (disableDepth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    @Override
    public void renderFluxRiftOutline(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(RIFT_TEXTURE);
        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        renderFluxRiftSingleLayer(rift, stability, partialTicks, tessLevel, 1.0F, 1.0F, 1.0F, 1.0F, false);
        GlStateManager.enableCull();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glPopMatrix();
    }
    
    @Override
    public void renderFluxRiftSolidLayer(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(BLANK);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        renderFluxRiftSingleLayer(rift, stability, partialTicks, tessLevel, r, g, b, a, false);
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

}
