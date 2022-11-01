/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.client.renderer.entity;

import com.sasmaster.glelwjgl.java.CoreGLE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager;
import thecodex6824.thaumicaugmentation.client.shader.TAShaders;

import javax.annotation.Nullable;
import java.util.Arrays;

public class RenderFluxRiftOptimized extends Render<EntityFluxRift> {
    
    protected static final ResourceLocation TEXTURE = TATextures.RIFT;
    protected static final CoreGLE GLE = new CoreGLE();
    
    protected static double[][] POINT_BUFFER = new double[0][0];
    protected static float[][] COLOR_BUFFER = new float[0][0];
    protected static double[] RADIUS_BUFFER = new double[0];
    
    static {
        GLE.set_POLYCYL_TESS(6);
        GLE.gleSetJoinStyle(CoreGLE.TUBE_JN_ANGLE);
    }
    
    public RenderFluxRiftOptimized(RenderManager rm) {
        super(rm);
        shadowSize = 0.0F;
    }

    @Override
    public void doRender(EntityFluxRift rift, double x, double y, double z, float yaw, float pt) {
        if (Minecraft.getMinecraft().profiler.profilingEnabled)
            Minecraft.getMinecraft().profiler.startSection("fluxrift");
        if (rift.points.size() > 2) {
            Entity rv = Minecraft.getMinecraft().getRenderViewEntity();
            if (rv == null)
                rv = Minecraft.getMinecraft().player;
            
            boolean goggles = EntityUtils.hasGoggles(rv);
            GlStateManager.pushMatrix();
            bindTexture(TEXTURE);
            TAShaderManager.enableShader(TAShaders.FLUX_RIFT, TAShaders.SHADER_CALLBACK_GENERIC_SPHERE);
            float amp = 1.0F;
            float stab = MathHelper.clamp(1.0F - rift.getRiftStability() / 50.0F, 0.0F, 1.5F);
            GlStateManager.enableBlend();
            for (int layer = 0; layer < 4; ++layer) {
                if (layer < 3) {
                    GlStateManager.depthMask(false);
                    if (layer == 0 && goggles)
                        GlStateManager.disableDepth();
                }
                
                GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, layer != 3 ? DestFactor.ONE : DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.pushMatrix();
                if (rift.points.size() > POINT_BUFFER.length) {
                    POINT_BUFFER = new double[rift.points.size()][3];
                    COLOR_BUFFER = new float[rift.points.size()][4];
                    for (float[] arr : COLOR_BUFFER)
                        Arrays.fill(arr, 1.0F);
                    
                    RADIUS_BUFFER = new double[rift.points.size()];
                }
                
                for (int i = 0; i < rift.points.size(); ++i) {
                    float var = rift.ticksExisted + pt;
                    if (i > rift.points.size() / 2)
                        var -= i * 10;
                    else if (i < rift.points.size() / 2)
                        var += i * 10;
                    
                    POINT_BUFFER[i][0] = rift.points.get(i).x + x + MathHelper.sin(var / 50.0F * amp) * 0.10000000149011612 * stab;
                    POINT_BUFFER[i][1] = rift.points.get(i).y + y + MathHelper.sin(var / 60.0F * amp) * 0.10000000149011612 * stab;
                    POINT_BUFFER[i][2] = rift.points.get(i).z + z + MathHelper.sin(var / 70.0F * amp) * 0.10000000149011612 * stab;
                    double width = 1.0 - MathHelper.sin(var / 8.0F * amp) * 0.10000000149011612 * stab;
                    RADIUS_BUFFER[i] = rift.pointsWidth.get(i) * width * (layer != 3 ? 1.25F + 0.5F * layer : 1.0F);
                } 
                
                GLE.glePolyCone(rift.points.size(), POINT_BUFFER, COLOR_BUFFER, RADIUS_BUFFER, 1.0F, 0.0F);
                GlStateManager.popMatrix();
                if (layer < 3) {
                    GlStateManager.depthMask(true);
                    if (layer == 0 && goggles)
                        GlStateManager.enableDepth();
                } 
            } 
            
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableBlend();
            TAShaderManager.disableShader();
            GlStateManager.popMatrix();
        }
        
        if (Minecraft.getMinecraft().profiler.profilingEnabled)
            Minecraft.getMinecraft().profiler.endSection();
    }

    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityFluxRift entity) {
        return null;
    }
    
}
