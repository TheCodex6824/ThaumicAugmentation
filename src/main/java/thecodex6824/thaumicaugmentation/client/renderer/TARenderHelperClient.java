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

import com.sasmaster.glelwjgl.java.CoreGLE;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXFireMote;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.client.fx.FXArcCustom;
import thecodex6824.thaumicaugmentation.client.fx.FXGenericP2ECustomSpeed;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager;
import thecodex6824.thaumicaugmentation.client.shader.TAShaders;
import thecodex6824.thaumicaugmentation.common.block.trait.IRenderableSides;
import thecodex6824.thaumicaugmentation.common.tile.TileObelisk;
import thecodex6824.thaumicaugmentation.common.tile.TileRiftBarrier;
import thecodex6824.thaumicaugmentation.common.tile.TileStarfieldGlass;
import thecodex6824.thaumicaugmentation.common.util.ITARenderHelper;
import thecodex6824.thaumicaugmentation.common.util.ShaderType;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

public class TARenderHelperClient implements ITARenderHelper {

    protected static final ResourceLocation RIFT_TEXTURE = new ResourceLocation("minecraft", "textures/entity/end_portal.png");
    protected static final ResourceLocation BLANK = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/misc/white.png");
    protected static final CoreGLE GLE = new CoreGLE();
    
    protected static double[][] POINT_BUFFER = new double[0][0];
    protected static float[][] COLOR_BUFFER = new float[0][0];
    protected static double[] RADIUS_BUFFER = new double[0];
    
    protected static final FloatBuffer BUFFER_X = (FloatBuffer) GLAllocation.createDirectFloatBuffer(16).put(1.0F).put(0.0F).put(0.0F).put(0.0F).flip();
    protected static final FloatBuffer BUFFER_Y = (FloatBuffer) GLAllocation.createDirectFloatBuffer(16).put(0.0F).put(1.0F).put(0.0F).put(0.0F).flip();
    protected static final FloatBuffer BUFFER_Z = (FloatBuffer) GLAllocation.createDirectFloatBuffer(16).put(0.0F).put(0.0F).put(1.0F).put(0.0F).flip();
    protected static final FloatBuffer BUFFER_W = (FloatBuffer) GLAllocation.createDirectFloatBuffer(16).put(0.0F).put(0.0F).put(0.0F).put(1.0F).flip();
    
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
    
    protected static final double[][] FRACTURE_POINT_BUFFER = new double[FRACTURE_POINTS_CLOSED.length][3];
    protected static final float[][] FRACTURE_COLOR_BUFFER = new float[FRACTURE_POINTS_CLOSED.length][4];
    protected static final double[] FRACTURE_RADIUS_BUFFER = new double[FRACTURE_POINTS_CLOSED.length];
    
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
                GlStateManager.pushMatrix();
                if (rift.getPoints().length > POINT_BUFFER.length) {
                    POINT_BUFFER = new double[rift.getPoints().length][3];
                    COLOR_BUFFER = new float[rift.getPoints().length][4];
                    for (float[] arr : COLOR_BUFFER)
                        Arrays.fill(arr, 1.0F);
                    
                    RADIUS_BUFFER = new double[rift.getPoints().length];
                }
                
                for (int i = 0; i < rift.getPoints().length; ++i) {
                    float time = Minecraft.getMinecraft().player.ticksExisted + partialTicks;
                    if (i > rift.getPoints().length / 2)
                        time -= i * 10;
                    else if (i < rift.getPoints().length / 2)
                        time += i * 10;
                    
                    POINT_BUFFER[i][0] = rift.getPoints()[i].x + Math.sin(time / 50.0) * 0.10000000149011612 * stab;
                    POINT_BUFFER[i][1] = rift.getPoints()[i].y + Math.sin(time / 60.0) * 0.10000000149011612 * stab;
                    POINT_BUFFER[i][2] = rift.getPoints()[i].z + Math.sin(time / 70.0) * 0.10000000149011612 * stab;
                    RADIUS_BUFFER[i] = rift.getWidths()[i] * (1.0 - Math.sin(time / 8.0) * 0.10000000149011612 * stab) *
                            (layer < 3 ? 1.25 + 0.5 * layer : 1.0);
                }
                
                GLE.set_POLYCYL_TESS(tessLevel);
                GLE.gleSetJoinStyle(CoreGLE.TUBE_JN_ANGLE);
                GLE.glePolyCone(rift.getPoints().length, POINT_BUFFER, COLOR_BUFFER, RADIUS_BUFFER, 1.0F, 0.0F);
                GlStateManager.popMatrix();
            }
            
            if (layer < 3) {
                GlStateManager.depthMask(true);
                if (layer == 0 && goggles)
                    GlStateManager.enableDepth();
            }
        }
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    @Override
    public void renderFluxRift(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, boolean ignoreGoggles) {
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(RIFT_TEXTURE);
        TAShaderManager.enableShader(TAShaders.FLUX_RIFT, TAShaders.SHADER_CALLBACK_GENERIC_SPHERE);
        GlStateManager.enableBlend();
        renderFluxRiftShared(rift, stability, partialTicks, tessLevel, !ignoreGoggles && EntityUtils.hasGoggles(Minecraft.getMinecraft().player));
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        TAShaderManager.disableShader();
        GlStateManager.popMatrix();
    }
    
    protected void renderFluxRiftSingleLayer(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, boolean disableDepth, float r, float g, float b, float a, int joinType) {
        boolean colorMod = r < 1.0F || g < 1.0F || b < 1.0F || a < 1.0F;
        float stab = Math.max(Math.min(1.0F - stability / 50.0F, 1.5F), 0.0F);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        if (disableDepth) {
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
        }
        if (rift.getPoints().length > 2) {
            GlStateManager.pushMatrix();
            if (rift.getPoints().length > POINT_BUFFER.length) {
                POINT_BUFFER = new double[rift.getPoints().length][3];
                COLOR_BUFFER = new float[rift.getPoints().length][4];
                for (float[] arr : COLOR_BUFFER)
                    Arrays.fill(arr, 1.0F);
                
                RADIUS_BUFFER = new double[rift.getPoints().length];
            }
            for (int i = 0; i < rift.getPoints().length; ++i) {
                float time = Minecraft.getMinecraft().player.ticksExisted + partialTicks;
                if (i > rift.getPoints().length / 2)
                    time -= i * 10;
                else if (i < rift.getPoints().length / 2)
                    time += i * 10;
                
                POINT_BUFFER[i][0] = rift.getPoints()[i].x + Math.sin(time / 50.0) * 0.10000000149011612 * stab;
                POINT_BUFFER[i][1] = rift.getPoints()[i].y + Math.sin(time / 60.0) * 0.10000000149011612 * stab;
                POINT_BUFFER[i][2] = rift.getPoints()[i].z + Math.sin(time / 70.0) * 0.10000000149011612 * stab;
                if (colorMod) {
                    COLOR_BUFFER[i][0] = r;
                    COLOR_BUFFER[i][1] = g;
                    COLOR_BUFFER[i][2] = b;
                    COLOR_BUFFER[i][3] = a;
                }
                RADIUS_BUFFER[i] = rift.getWidths()[i] * (1.0 - Math.sin(time / 8.0) * 0.10000000149011612 * stab);
            }
            
            GLE.set_POLYCYL_TESS(tessLevel);
            GLE.gleSetJoinStyle(joinType);
            GLE.glePolyCone(rift.getPoints().length, POINT_BUFFER, COLOR_BUFFER, RADIUS_BUFFER, 1.0F, 0.0F);
            GlStateManager.popMatrix();
        }
        
        if (disableDepth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (colorMod) {
            for (float[] arr : COLOR_BUFFER)
                Arrays.fill(arr, 1.0F);
        }
    }
    
    @Override
    public void renderFluxRiftOutline(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType) {
        GlStateManager.pushMatrix();
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
        GlStateManager.popMatrix();
    }
    
    @Override
    public void renderFluxRiftSolidLayer(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType) {
        GlStateManager.pushMatrix();
        if (bindTexture)
            Minecraft.getMinecraft().renderEngine.bindTexture(BLANK);
        
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        renderFluxRiftSingleLayer(rift, stability, partialTicks, tessLevel, false, r, g, b, a, joinType);
        GlStateManager.enableLighting();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    
    protected double lerp(double initial, double last, double currentTime, double timeOpened, double totalTime) {
        double factor = MathHelper.clamp((currentTime - timeOpened) / totalTime, 0, 1.0);
        return (1.0 - factor) * initial + factor * last;
    }
    
    protected void renderDimensionalFractureSingleLayer(boolean open, long worldTime, long timeOpened, long openingDuration, float partialTicks, int tessLevel, float r, float g, float b, float a, int joinType) {
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
        if (TAShaderManager.shouldUseShaders())
            TAShaderManager.enableShader(TAShaders.FRACTURE, TAShaders.SHADER_CALLBACK_GENERIC_SPHERE);
        
        GlStateManager.enableBlend();
        for (int layer = 0; layer < 4; ++layer) {
            if (layer != 3) {
                GlStateManager.depthMask(false);
                if (layer == 0 && isRevealing)
                    GlStateManager.disableDepth();
            }

            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, layer != 3 ? DestFactor.ONE : DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.pushMatrix();
            
            for (int i = 0; i < FRACTURE_POINTS_CLOSED.length; ++i) {
                double time = worldTime + partialTicks;
                if (i > FRACTURE_POINTS_CLOSED.length / 2)
                    time -= i * 10;
                else if (i < FRACTURE_POINTS_CLOSED.length / 2)
                    time += i * 10;

                Vec3d rotatedClosed = FRACTURE_POINTS_CLOSED[i].add(-0.5, 1.0, -0.5);
                Vec3d rotatedOpen = FRACTURE_POINTS_OPEN[i].add(-0.5, 1.0, -0.5);
                FRACTURE_POINT_BUFFER[i][0] = lerp(rotatedClosed.x, rotatedOpen.x, worldTime, timeOpened, openingDuration) + Math.sin(time / 50) * lerp(0.1, 0.01, worldTime, timeOpened, openingDuration);
                FRACTURE_POINT_BUFFER[i][1] = lerp(rotatedClosed.y, rotatedOpen.y, worldTime, timeOpened, openingDuration) + Math.sin(time / 60) * lerp(0.1, 0.01, worldTime, timeOpened, openingDuration);
                FRACTURE_POINT_BUFFER[i][2] = lerp(rotatedClosed.z, rotatedOpen.z, worldTime, timeOpened, openingDuration) + Math.sin(time / 70) * lerp(0.1, 0.01, worldTime, timeOpened, openingDuration);
                
                FRACTURE_COLOR_BUFFER[i][0] = r;
                FRACTURE_COLOR_BUFFER[i][1] = g;
                FRACTURE_COLOR_BUFFER[i][2] = b;
                FRACTURE_COLOR_BUFFER[i][3] = (float) lerp(a / 4.0, a, worldTime, timeOpened, openingDuration);

                double widthMultiplier = 1.0 - Math.sin(time / 8) * 0.1;
                FRACTURE_RADIUS_BUFFER[i] = lerp(FRACTURE_WIDTHS_CLOSED[i], FRACTURE_WIDTHS_OPEN[i], worldTime, timeOpened, openingDuration) * widthMultiplier * (layer != 3 ? 1.25 + 0.5 * layer : 1.0);
            }

            GLE.set_POLYCYL_TESS(tessLevel);
            GLE.gleSetJoinStyle(CoreGLE.TUBE_JN_ANGLE);
            GLE.glePolyCone(FRACTURE_POINT_BUFFER.length, FRACTURE_POINT_BUFFER, FRACTURE_COLOR_BUFFER,
                    FRACTURE_RADIUS_BUFFER,1.0F, 0.0F);

            GlStateManager.popMatrix();
            if (layer != 3) {
                GlStateManager.depthMask(true);
                if (layer == 0 && isRevealing)
                    GlStateManager.enableDepth();
            }
        }

        // the GLE stuff above changes the color but MC still thinks it was not changed
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        if (TAShaderManager.shouldUseShaders())
            TAShaderManager.disableShader();
        
        GlStateManager.popMatrix();
    }
    
    @Override
    public void renderDimensionalFractureSolidLayer(boolean open, long worldTime, long timeOpened, long openingDuration, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType) {
        GlStateManager.pushMatrix();
        if (bindTexture)
            Minecraft.getMinecraft().renderEngine.bindTexture(BLANK);
        
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        renderDimensionalFractureSingleLayer(open, worldTime, timeOpened, openingDuration, partialTicks, tessLevel, r, g, b, a, joinType);
        GlStateManager.enableLighting();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
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
            int color) {
        
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
    public void renderObeliskParticles(World world, double x, double y, double z) {
        FXGeneric fx = new FXGeneric(world, x, y, z, 0.0, 0.0, 0.0);
        fx.setMaxAge(80 + world.rand.nextInt(20));
        fx.setRBGColorF(0.05F, 0.05F, 0.05F);
        fx.setAlphaF(0.0F, 0.75F, 0.0F);
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
    public void renderParticleTrail(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b) {
        for (int i = 0; i < 2 + world.rand.nextInt(4); ++i) {
            FXGeneric fx = new FXGeneric(world, x + (world.rand.nextFloat() - world.rand.nextFloat()) / 2.0, y + (world.rand.nextFloat() - world.rand.nextFloat()) / 2.0,
                    z + (world.rand.nextFloat() - world.rand.nextFloat()) / 2.0, vx, vy, vz);
            fx.setMaxAge(80 + world.rand.nextInt(20));
            fx.setRBGColorF(r, g, b);
            fx.setAlphaF(0.0F, 0.5F, 0.0F);
            fx.setGridSize(64);
            fx.setParticles(264, 8, 1);
            fx.setScale(2.0F, 1.0F);
            fx.setLayer(1);
            fx.setLoop(true);
            fx.setNoClip(false);
            fx.setRotationSpeed(world.rand.nextFloat(), world.rand.nextBoolean() ? 1.0F : -1.0F);
            ParticleEngine.addEffect(world, fx);
        }
    }
    
    @Override
    public void renderWisp(double x, double y, double z, Entity target) {
        FXDispatcher.INSTANCE.wispFXEG(x, y, z, target);
    }
    
    @Override
    public void renderVent(double x, double y, double z, double vx, double vy, double vz, int color, float scale) {
        FXDispatcher.INSTANCE.drawVentParticles(x, y, z, vx, vy, vz, color, scale);
    }
    
    @Override
    public void renderWispParticles(double x, double y, double z, double vx, double vy, double vz, int color, int delay) {
        FXDispatcher.INSTANCE.drawWispParticles(x, y, z, vx, vy, vz, color, delay);
    }
    
    @Override
    public void renderFollowingParticles(World world, double x, double y, double z, Entity toFollow, float r, float g, float b) {
        FXGenericP2ECustomSpeed orb = new FXGenericP2ECustomSpeed(world, x, y, z, toFollow, -0.2, 0.2);
        orb.setRBGColorF(r, g, b);
        orb.setMaxAge(200);
        orb.setAlphaF(0.75F, 1.0F, 0.0F);
        orb.setGridSize(64);
        orb.setParticles(264, 8, 1);
        orb.setScale(2.0F);
        orb.setLayer(1);
        orb.setLoop(true);
        orb.setNoClip(false);
        orb.setRotationSpeed(world.rand.nextFloat(), world.rand.nextBoolean() ? 1.0F : -1.0F);
        ParticleEngine.addEffect(world, orb);
    }
    
    @Nullable
    private static EnumHand findImpulseCannon(EntityLivingBase entity) {
        ItemStack stack = entity.getHeldItemMainhand();
        if (stack.getItem() == TAItems.IMPULSE_CANNON)
            return EnumHand.MAIN_HAND;
        
        stack = entity.getHeldItemOffhand();
        if (stack.getItem() == TAItems.IMPULSE_CANNON)
            return EnumHand.OFF_HAND;
        
        return null;
    }

    protected double getCloseZ(Vec3d tile, Vec3d cam) {
        double dist = tile.squareDistanceTo(cam);
        return Math.pow(2.0, MathHelper.clamp(MathHelper.floor(Math.log1p(dist) - 14.0), -14.0, -1.0));
    }

    @Override
    public void renderStarfieldGlass(ShaderType type, TileStarfieldGlass tile, double pX, double pY, double pZ) {
        BlockPos pos = tile.getPos();
        IBlockAccess world = tile.getWorld();
        IBlockState s = world.getBlockState(pos);
        s = s.getBlock().getExtendedState(s, world, pos);
        if (s instanceof IExtendedBlockState) {
            IExtendedBlockState state = (IExtendedBlockState) s;
            if (state.getUnlistedNames().contains(IRenderableSides.SIDES)) {
                List<EnumFacing> allSides = state.getValue(IRenderableSides.SIDES);
                if (allSides != null && !allSides.isEmpty()) {
                    double zCloseNeg = getCloseZ(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), new Vec3d(pX, pY, pZ));
                    double zClosePos = 1.0 - zCloseNeg;
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(pos.getX() - pX, pos.getY() - pY, pos.getZ() - pZ);
                    Tessellator t = Tessellator.getInstance();
                    BufferBuilder buffer = t.getBuffer();
                    for (EnumFacing face : allSides) {
                        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                        switch (face) {
                            case DOWN: {
                                buffer.pos(0.0, zCloseNeg, 0.0).tex(0, 0).endVertex();
                                buffer.pos(1.0, zCloseNeg, 0.0).tex(1, 0).endVertex();
                                buffer.pos(1.0, zCloseNeg, 1.0).tex(1, 1).endVertex();
                                buffer.pos(0.0, zCloseNeg, 1.0).tex(0, 1).endVertex();
                                break;
                            }
                            case UP: {
                                buffer.pos(0.0, zClosePos, 0.0).tex(0, 0).endVertex();
                                buffer.pos(0.0, zClosePos, 1.0).tex(1, 0).endVertex();
                                buffer.pos(1.0, zClosePos, 1.0).tex(1, 1).endVertex();
                                buffer.pos(1.0, zClosePos, 0.0).tex(0, 1).endVertex();
                                break;
                            }
                            case EAST: {
                                buffer.pos(zClosePos, 0.0, 0.0).tex(0, 0).endVertex();
                                buffer.pos(zClosePos, 1.0, 0.0).tex(1, 0).endVertex();
                                buffer.pos(zClosePos, 1.0, 1.0).tex(1, 1).endVertex();
                                buffer.pos(zClosePos, 0.0, 1.0).tex(0, 1).endVertex();
                                break;
                            }
                            case WEST: {
                                buffer.pos(zCloseNeg, 0.0, 0.0).tex(0, 0).endVertex();
                                buffer.pos(zCloseNeg, 0.0, 1.0).tex(0, 1).endVertex();
                                buffer.pos(zCloseNeg, 1.0, 1.0).tex(1, 1).endVertex();
                                buffer.pos(zCloseNeg, 1.0, 0.0).tex(1, 0).endVertex();
                                break;
                            }
                            case SOUTH: {
                                buffer.pos(0.0, 0.0, zClosePos).tex(0, 0).endVertex();
                                buffer.pos(1.0, 0.0, zClosePos).tex(1, 0).endVertex();
                                buffer.pos(1.0, 1.0, zClosePos).tex(1, 1).endVertex();
                                buffer.pos(0.0, 1.0, zClosePos).tex(0, 1).endVertex();
                                break;
                            }
                            case NORTH: {
                                buffer.pos(0.0, 0.0, zCloseNeg).tex(0, 0).endVertex();
                                buffer.pos(0.0, 1.0, zCloseNeg).tex(0, 1).endVertex();
                                buffer.pos(1.0, 1.0, zCloseNeg).tex(1, 1).endVertex();
                                buffer.pos(1.0, 0.0, zCloseNeg).tex(1, 0).endVertex();
                                break;
                            }
                            
                            default: break;
                        }
                        
                        t.draw();
                    }
            
                    GlStateManager.popMatrix();
                }
            }
        }
    }
    
    @Override
    public void renderObelisk(ShaderType type, TileObelisk tile, double pX, double pY, double pZ) {
        BlockPos pos = tile.getPos();
        IBlockState state = tile.getWorld().getBlockState(pos);
        double offset = Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().isSingleplayer() ? 0.0 :
            Minecraft.getMinecraft().getRenderPartialTicks();
        double bob = Math.sin((Minecraft.getMinecraft().player.ticksExisted + offset) / 20.0) / 4.0;
        double zCloseNeg = getCloseZ(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), new Vec3d(pX, pY + bob, pZ));
        double zClosePos = 1.0 - zCloseNeg;
        GlStateManager.pushMatrix();
        GlStateManager.translate(pos.getX() - pX, pos.getY() - pY + bob, pos.getZ() - pZ);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            if (state.shouldSideBeRendered(tile.getWorld(), pos, face) || state.shouldSideBeRendered(tile.getWorld(), pos.up(), face) ||
                    state.shouldSideBeRendered(tile.getWorld(), pos.down(), face)) {
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                switch (face) {
                    case EAST: {
                        buffer.pos(zClosePos, -1.0, 0.0).tex(0, 0).endVertex();
                        buffer.pos(zClosePos, 2.0, 0.0).tex(0, 3).endVertex();
                        buffer.pos(zClosePos, 2.0, 1.0).tex(1, 3).endVertex();
                        buffer.pos(zClosePos, -1.0, 1.0).tex(1, 0).endVertex();
                        break;
                    }
                    case WEST: {
                        buffer.pos(zCloseNeg, -1.0, 0.0).tex(0, 0).endVertex();
                        buffer.pos(zCloseNeg, -1.0, 1.0).tex(0, 1).endVertex();
                        buffer.pos(zCloseNeg, 2.0, 1.0).tex(3, 1).endVertex();
                        buffer.pos(zCloseNeg, 2.0, 0.0).tex(3, 0).endVertex();
                        break;
                    }
                    case SOUTH: {
                        buffer.pos(0.0, -1.0, zClosePos).tex(0, 0).endVertex();
                        buffer.pos(1.0, -1.0, zClosePos).tex(1, 0).endVertex();
                        buffer.pos(1.0, 2.0, zClosePos).tex(1, 3).endVertex();
                        buffer.pos(0.0, 2.0, zClosePos).tex(0, 3).endVertex();
                        break;
                    }
                    case NORTH: {
                        buffer.pos(0.0, -1.0, zCloseNeg).tex(0, 0).endVertex();
                        buffer.pos(0.0, 2.0, zCloseNeg).tex(0, 3).endVertex();
                        buffer.pos(1.0, 2.0, zCloseNeg).tex(1, 3).endVertex();
                        buffer.pos(1.0, -1.0, zCloseNeg).tex(1, 0).endVertex();
                        break;
                    }
                    
                    default: break;
                }
                
                t.draw();
            }
        }

        GlStateManager.popMatrix();
    }
    
    @Override
    public void renderRiftBarrier(ShaderType type, TileRiftBarrier tile, double pX, double pY, double pZ) {
        BlockPos pos = tile.getPos();
        IBlockState state = tile.getWorld().getBlockState(pos);
        EnumFacing face = state.getValue(IHorizontallyDirectionalBlock.DIRECTION);
        if (state.shouldSideBeRendered(tile.getWorld(), pos, face) || state.shouldSideBeRendered(tile.getWorld(), pos, face.getOpposite())) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(pos.getX() - pX, pos.getY() - pY, pos.getZ() - pZ);
            GlStateManager.disableCull();
            Tessellator t = Tessellator.getInstance();
            BufferBuilder buffer = t.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            switch (face) {
                case EAST: {
                    buffer.pos(0.5, 0.0, 0.0).tex(0, 0).endVertex();
                    buffer.pos(0.5, 1.0, 0.0).tex(1, 0).endVertex();
                    buffer.pos(0.5, 1.0, 1.0).tex(1, 1).endVertex();
                    buffer.pos(0.5, 0.0, 1.0).tex(0, 1).endVertex();
                    break;
                }
                case WEST: {
                    buffer.pos(0.5, 0.0, 0.0).tex(0, 0).endVertex();
                    buffer.pos(0.5, 0.0, 1.0).tex(0, 1).endVertex();
                    buffer.pos(0.5, 1.0, 1.0).tex(1, 1).endVertex();
                    buffer.pos(0.5, 1.0, 0.0).tex(1, 0).endVertex();
                    break;
                }
                case SOUTH: {
                    buffer.pos(0.0, 0.0, 0.5).tex(0, 0).endVertex();
                    buffer.pos(1.0, 0.0, 0.5).tex(1, 0).endVertex();
                    buffer.pos(1.0, 1.0, 0.5).tex(1, 1).endVertex();
                    buffer.pos(0.0, 1.0, 0.5).tex(0, 1).endVertex();
                    break;
                }
                case NORTH: {
                    buffer.pos(0.0, 0.0, 0.5).tex(0, 0).endVertex();
                    buffer.pos(0.0, 1.0, 0.5).tex(0, 1).endVertex();
                    buffer.pos(1.0, 1.0, 0.5).tex(1, 1).endVertex();
                    buffer.pos(1.0, 0.0, 0.5).tex(1, 0).endVertex();
                    break;
                }
                
                default: break;
            }
            
            t.draw();
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
        }
    }
    
    @Override
    public void drawCube() {
        drawCube(0.0, 1.0);
    }
    
    @Override
    public void drawCube(double min, double max) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(min, min, max).tex(0.0, 0.0).endVertex();
        buffer.pos(max, min, max).tex(1.0, 0.0).endVertex();
        buffer.pos(min, max, max).tex(0.0, 1.0).endVertex();
        buffer.pos(max, max, max).tex(1.0, 1.0).endVertex();
        buffer.pos(max, max, min).tex(1.0, 0.0).endVertex();
        buffer.pos(max, min, max).tex(0.0, 1.0).endVertex();
        buffer.pos(max, min, min).tex(0.0, 0.0).endVertex();
        buffer.pos(min, min, max).tex(1.0, 1.0).endVertex();
        buffer.pos(min, min, min).tex(1.0, 0.0).endVertex();
        buffer.pos(min, max, max).tex(0.0, 1.0).endVertex();
        buffer.pos(min, max, min).tex(0.0, 0.0).endVertex();
        buffer.pos(max, max, min).tex(1.0, 0.0).endVertex();
        buffer.pos(min, min, min).tex(0.0, 1.0).endVertex();
        buffer.pos(max, min, min).tex(1.0, 1.0).endVertex();
        t.draw();
    }
    
    @Override
    public Vec3d estimateImpulseCannonFiringPoint(EntityLivingBase entity, float partialTicks) {
        Vec3d origin = null;
        Entity rv = Minecraft.getMinecraft().getRenderViewEntity() != null ? Minecraft.getMinecraft().getRenderViewEntity() :
            Minecraft.getMinecraft().player;
        Render<? extends Entity> r = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);
        if (r instanceof RenderLivingBase<?>) {
            ModelBase model = ((RenderLivingBase<?>) r).getMainModel();
            if (model instanceof ModelBiped) {
                ModelBiped biped = (ModelBiped) model;
                EnumHand hand = findImpulseCannon(entity);
                EnumHandSide side = hand == EnumHand.MAIN_HAND ? entity.getPrimaryHand() : entity.getPrimaryHand().opposite();
                float armLength = 0.0F;
                float armHeight = 0.0F;
                float armHeightFromGround = 0.0F;
                float armWidth = 0.0F;
                boolean rightSide = side == EnumHandSide.RIGHT;
                boolean rotateHeight = true;
                if (entity.equals(rv) && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                    armLength = 0.3F;
                    armHeightFromGround = 1.525F;
                    if (entity.isSneaking())
                        armHeightFromGround -= 0.08F;
                        
                    armHeight = 0.0F;
                    armWidth = rightSide ? -0.125F : 0.125F;
                    rotateHeight = false;
                }
                else {
                    ModelRenderer arm = rightSide ? biped.bipedRightArm : biped.bipedLeftArm;
                    if (!arm.cubeList.isEmpty()) {
                        ModelBox box = arm.cubeList.get(0);
                        armLength = (box.posY2 - box.posY1) / 16.0F + 0.7F;
                        armHeightFromGround = arm.rotationPointY / 2.0F;
                        if (entity.isSneaking())
                            armHeightFromGround -= 0.3F;
                        
                        armHeight = (box.posZ2 - box.posZ1) / 16.0F + 0.1F;
                        armWidth = arm.rotationPointX / 16.0F;
                    }
                    else {
                        armLength = 0.7F;
                        armHeightFromGround = entity.isSneaking() ? 0.6F : 0.9F;
                        armHeight = 0.2F;
                        armWidth = 0.2F;
                    }
                }
                
                double lerpX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
                double lerpY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
                double lerpZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
                float lerpPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                float lerpYaw = entity.prevRotationYawHead + (entity.rotationYawHead - entity.prevRotationYawHead) * partialTicks;
                if (rotateHeight) {
                    origin = new Vec3d(0.0, armHeight, armLength).rotatePitch((float) -Math.toRadians(lerpPitch)).add(armWidth, 0.0, 0.0).rotateYaw(
                            (float) -Math.toRadians(lerpYaw)).add(lerpX, lerpY + armHeightFromGround, lerpZ);
                }
                else {
                    origin = new Vec3d(0.0, 0.0, armLength).rotatePitch((float) -Math.toRadians(lerpPitch)).add(armWidth, 0.0, 0.0).rotateYaw(
                            (float) -Math.toRadians(lerpYaw)).add(lerpX, lerpY + armHeightFromGround, lerpZ);
                }
            }
        }
        
        if (origin == null) {
            double lerpX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
            double lerpY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
            double lerpZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
            origin = new Vec3d(lerpX, lerpY, lerpZ).add(0.0, entity.height / 2.0F, 0.0);
        }
        
        return origin;
    }
    
    @Override
    public boolean shadersAvailable() {
        return TAShaderManager.shouldUseShaders();
    }
    
    @Override
    public boolean stencilAvailable() {
        return GL11.glGetInteger(GL11.GL_STENCIL_BITS) > 0;
    }
    
    @Override
    public boolean framebuffersAvailable() {
        return Minecraft.getMinecraft().gameSettings.fboEnable && !TAConfig.disableFramebuffers.getValue() &&
                !FMLClientHandler.instance().hasOptifine();
    }

}
