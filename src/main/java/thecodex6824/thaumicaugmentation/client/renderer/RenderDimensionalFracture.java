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

import java.util.function.Consumer;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

import com.sasmaster.glelwjgl.java.CoreGLE;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager.Shader;
import thecodex6824.thaumicaugmentation.client.shader.TAShaders;
import thecodex6824.thaumicaugmentation.common.entity.EntityDimensionalFracture;

public class RenderDimensionalFracture extends Render<EntityDimensionalFracture> {

    protected static final ResourceLocation TEXTURE_CLOSED = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/environment/emptiness_sky.png");
    protected static final ResourceLocation TEXTURE_OPEN = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/environment/emptiness_sky.png");
    protected static final Vec3d[] POINTS_CLOSED = new Vec3d[] {
            new Vec3d(0.85, 1.97, 0.39),
            new Vec3d(0.60, 1.77, 0.45),
            new Vec3d(0.48, 1.35, 0.53),
            new Vec3d(0.29, 1.10, 0.66),
            new Vec3d(0.15, 0.80, 0.78),
            new Vec3d(0.25, 0.45, 0.62),
            new Vec3d(0.45, 0.10, 0.52),
            new Vec3d(0.65, -0.25, 0.38),
            new Vec3d(0.60, -0.55, 0.25),
            new Vec3d(0.50, -0.85, 0.20)
    };
    protected static final Vec3d[] POINTS_OPEN = new Vec3d[] {
            new Vec3d(0.85, 1.97, 0.39),
            new Vec3d(0.60, 1.77, 0.50),
            new Vec3d(0.54, 1.35, 0.45),
            new Vec3d(0.36, 1.10, 0.60),
            new Vec3d(0.50, 0.80, 0.50),
            new Vec3d(0.50, 0.15, 0.50),
            new Vec3d(0.39, -0.10, 0.58),
            new Vec3d(0.58, -0.45, 0.42),
            new Vec3d(0.60, -0.75, 0.50),
            new Vec3d(0.50, -0.95, 0.20)
    };
    
    protected static final double[] WIDTHS_CLOSED = new double[] {
            0,
            0.0051,
            0.0056,
            0.008,
            0.009,
            0.009,
            0.0074,
            0.0056,
            0.0041,
            0
    };
    protected static final double[] WIDTHS_OPEN = new double[] {
            0,
            0.052,
            0.179,
            0.216,
            0.250,
            0.250,
            0.228,
            0.172,
            0.052,
            0
    };
    
    protected static final Consumer<Shader> SHADER_CALLBACK = shader -> {
        Minecraft mc = Minecraft.getMinecraft();
        
        int x = ARBShaderObjects.glGetUniformLocationARB(shader.getID(), "yaw");
        ARBShaderObjects.glUniform1fARB(x, (float) (mc.player.rotationYaw * 2.0F * Math.PI / 360.0));
        
        int z = ARBShaderObjects.glGetUniformLocationARB(shader.getID(), "pitch");
        ARBShaderObjects.glUniform1fARB(z, (float) (-mc.player.rotationPitch * 2.0F * Math.PI / 360.0));
    };

    protected CoreGLE gle;

    public RenderDimensionalFracture(RenderManager manager) {
        super(manager);
        gle = new CoreGLE();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDimensionalFracture entity) {
        return entity.isOpen() ? TEXTURE_OPEN : TEXTURE_CLOSED;
    }

    protected double lerp(double initial, double last, double currentTime, double timeOpened, double totalTime) {
        double factor = MathHelper.clamp((currentTime - timeOpened) / totalTime, 0, 1.0);
        return (1.0 - factor) * initial + factor * last;
    }
    
    @Override
    public void doRender(EntityDimensionalFracture entity, double x, double y, double z, float yaw, float partialTicks) {

        World world = entity.getEntityWorld();
        boolean isRevealing = EntityUtils.hasGoggles(Minecraft.getMinecraft().player);

        GL11.glPushMatrix();
        bindTexture(entity.isOpen() ? TEXTURE_OPEN : TEXTURE_CLOSED);
        TAShaderManager.enableShader(TAShaders.FRACTURE, SHADER_CALLBACK);
        GL11.glEnable(GL11.GL_BLEND);
        for (int layer = 0; layer < 4; ++layer) {
            if (layer != 3) {
                GlStateManager.depthMask(false);
                if (layer == 0 && isRevealing)
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
            }

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, layer != 3 ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);

            GL11.glPushMatrix();
            double[][] pointBuffer = new double[POINTS_CLOSED.length][3];
            float[][] colorBuffer = new float[POINTS_CLOSED.length][4];
            double[] radiusBuffer = new double[POINTS_CLOSED.length];
            for (int i = 0; i < POINTS_CLOSED.length; ++i) {
                double time = world.getTotalWorldTime() + partialTicks;
                if (time > POINTS_CLOSED.length / 2)
                    time -= i * 10;
                else if (time < POINTS_CLOSED.length / 2)
                    time += i * 10;

                Vec3d rotatedClosed = POINTS_CLOSED[i].add(-0.5, 1.0, -0.5).rotateYaw(yaw);
                Vec3d rotatedOpen = POINTS_OPEN[i].add(-0.5, 1.0, -0.5).rotateYaw(yaw);
                long now = world.getTotalWorldTime();
                long opened = entity.getTimeOpened();
                long totalTime = entity.getOpeningDuration();
                pointBuffer[i][0] = lerp(rotatedClosed.x, rotatedOpen.x, now, opened, totalTime) + x + Math.sin(time / 50) * 0.1;
                pointBuffer[i][1] = lerp(rotatedClosed.y, rotatedOpen.y, now, opened, totalTime) + y + Math.sin(time / 60) * 0.1;
                pointBuffer[i][2] = lerp(rotatedClosed.z, rotatedOpen.z, now, opened, totalTime) + z + Math.sin(time / 70) * 0.1;
                
                colorBuffer[i][0] = 1.0F;
                colorBuffer[i][1] = 1.0F;
                colorBuffer[i][2] = 1.0F;
                colorBuffer[i][3] = (float) lerp(0.25F, 1.0F, now, opened, totalTime);

                double widthMultiplier = 1.0 - Math.sin(time / 8) * 0.1;
                radiusBuffer[i] = lerp(WIDTHS_CLOSED[i], WIDTHS_OPEN[i], now, opened, totalTime) * widthMultiplier * (layer != 3 ? 1.25 + 0.5 * layer : 1.0);
            }

            gle.set_POLYCYL_TESS(CoreGLE.GLE_TEXTURE_NORMAL_SPH);
            gle.gleSetJoinStyle(CoreGLE.TUBE_JN_ANGLE);
            gle.glePolyCone(pointBuffer.length, pointBuffer, colorBuffer, radiusBuffer, 1.0F, 0.0F);

            GL11.glPopMatrix();
            if (layer != 3) {
                GlStateManager.depthMask(true);
                if (layer == 0 && isRevealing)
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_BLEND);
        TAShaderManager.disableShaders();
        GL11.glPopMatrix();
    }

}
