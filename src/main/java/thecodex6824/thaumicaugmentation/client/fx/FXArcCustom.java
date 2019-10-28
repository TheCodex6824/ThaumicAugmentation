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

package thecodex6824.thaumicaugmentation.client.fx;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.common.lib.utils.Utils;

public class FXArcCustom extends Particle {

    protected static final ResourceLocation BEAM = new ResourceLocation("thaumcraft", "textures/misc/beamh.png");
    
    protected double tx, ty, tz;
    protected ArrayList<Vec3d> points;
    protected float length, width;
    
    public FXArcCustom(World world, double x1, double y1, double z1, double x2, double y2, double z2, float red,
            float green, float blue, double upwardsVelocity, double gravity, double noise, float thickness) {
        
        super(world, x1, y1, z1, 0.0, 0.0, 0.0);
        points = new ArrayList<>();
        particleRed = red;
        particleGreen = green;
        particleBlue = blue;
        setSize(0.2F, 0.2F);
        tx = x2 - x1;
        ty = y2 - y1;
        tz = z2 - z1;
        width = thickness;
        particleMaxAge = 3;
        Vec3d origin = Vec3d.ZERO;
        Vec3d target = new Vec3d(tx, ty, tz);
        Vec3d current = origin;
        length = (float) target.length();
        Vec3d velocity = Utils.calculateVelocity(origin, target, upwardsVelocity, gravity);
        double l = Utils.distanceSquared3d(origin, velocity);
        points.add(origin);
        int vertexCount = 0;
        while (Utils.distanceSquared3d(target, current) > l && vertexCount < 50) {
            current = current.add(velocity.x, velocity.y, velocity.z);
            if (noise > 0) {
                current = current.add((rand.nextDouble() - rand.nextDouble()) * noise, (rand.nextDouble() - rand.nextDouble()) * noise,
                        (rand.nextDouble() - rand.nextDouble()) * noise);
            }
            points.add(current);
            velocity = velocity.subtract(0.0, gravity / 1.9, 0.0);
            ++vertexCount;
        }
        points.add(target);
    }
    
    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge)
            setExpired();
    }
    
    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX,
            float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        
        Tessellator t = Tessellator.getInstance();
        t.draw();
        
        GlStateManager.pushMatrix();
        double lerpX = prevPosX + (posX - prevPosX) * partialTicks - interpPosX;
        double lerpY = prevPosY + (posY - prevPosY) * partialTicks - interpPosY;
        double lerpZ = prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ;
        GlStateManager.translate(lerpX, lerpY, lerpZ);
        Minecraft.getMinecraft().renderEngine.bindTexture(BEAM);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        GlStateManager.disableCull();
        float alpha = 1.0F - (particleAge + partialTicks) / particleMaxAge;
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);
        for (int i = 0; i < points.size(); ++i) {
            Vec3d v = points.get(i);
            float pos = i / length;
            buffer.pos(v.x, v.y - width, v.z).tex(pos, 1.0F).color(particleRed, particleGreen, particleBlue, alpha).endVertex();
            buffer.pos(v.x, v.y + width, v.z).tex(pos, 0.0F).color(particleRed, particleGreen, particleBlue, alpha).endVertex();
        }
        t.draw();
        
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);
        for (int i = 0; i < points.size(); ++i) {
            Vec3d v = points.get(i);
            float pos = i / length;
            buffer.pos(v.x - width, v.y, v.z - width).tex(pos, 1.0F).color(particleRed, particleGreen, particleBlue, alpha).endVertex();
            buffer.pos(v.x + width, v.y, v.z + width).tex(pos, 0.0F).color(particleRed, particleGreen, particleBlue, alpha).endVertex();
        }
        t.draw();
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableCull();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(ParticleManager.PARTICLE_TEXTURES);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
    }
    
}
