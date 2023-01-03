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

package thecodex6824.thaumicaugmentation.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.fx.ParticleEngine;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;

import java.util.function.Function;

public class FXImpulseBeam extends Particle {

    protected static final ResourceLocation BEAM = new ResourceLocation("thaumcraft", "textures/misc/beam1.png");
    
    protected EntityLivingBase source;
    protected double offset;
    protected float length;
    protected float rotYaw;
    protected float rotPitch;
    protected float prevYaw;
    protected float prevPitch;
    protected double tXPrev;
    protected double tYPrev;
    protected double tZPrev;
    protected double tX;
    protected double tY;
    protected double tZ;
    protected float endMod;
    protected boolean reverse;
    protected boolean pulse;
    protected int rotationSpeed;
    protected float size;
    protected float prevSize;
    protected int impact;
    protected boolean follow;
    protected boolean posSet;
    protected Function<FXImpulseBeam, Float> alphaFunc;
    protected boolean endsLoop;
    
    public FXImpulseBeam(World world, EntityLivingBase entity, double tx, double ty, double tz, float r, float g, float b, int maxAge) {
        super(world, entity.posX, entity.posY, entity.posZ, 0.0, 0.0, 0.0);
        endMod = 1.0F;
        pulse = true;
        offset = (entity.height / 2.0F) + 0.25;
        particleRed = r;
        particleGreen = g;
        particleBlue = b;
        source = entity;
        size = 1.0F;
        setSize(0.02F, 0.02F);
        tX = tx;
        tY = ty;
        tZ = tz;
        tXPrev = tX;
        tYPrev = tY;
        tZPrev = tZ;
        alphaFunc = obj -> 0.4F;
        Vec3d pos = ThaumicAugmentation.proxy.getRenderHelper().estimateImpulseCannonFiringPoint(source, 1.0F);
        posX = pos.x;
        posY = pos.y;
        posZ = pos.z;
        double dx = posX - tX;
        double dy = posY - tY;
        double dz = posZ - tZ;
        length = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        double y = MathHelper.sqrt(dx * dx + dz * dz);
        rotYaw = (float) (Math.toDegrees(Math.atan2(dx, dz)));
        rotPitch = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dy, y)));
        prevYaw = rotYaw;
        prevPitch = rotPitch;
        particleMaxAge = maxAge;
    }
    
    public void setAlphaFunc(Function<FXImpulseBeam, Float> func) {
        alphaFunc = func;
    }
    
    public Function<FXImpulseBeam, Float> getAlphaFunc() {
        return alphaFunc;
    }
    
    public void updateBeamTarget(double tx, double ty, double tz) {
        tX = tx;
        tY = ty;
        tZ = tz;
    }
    
    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        tXPrev = tX;
        tYPrev = tY;
        tZPrev = tZ;
        prevYaw = rotYaw;
        prevPitch = rotPitch;
        
        if (impact > 0)
            --impact;
        
        if (particleAge++ >= particleMaxAge)
          setExpired();
    }
    
    public void setEndMod(float mod) {
        endMod = mod;
    }
    
    public void setReverse(boolean rev) {
        reverse = rev;
    }
    
    public void setPulse(boolean p) {
        pulse = p;
    }
    
    public void setRotationspeed(int speed) {
        rotationSpeed = speed;
    }
    
    public void setFollowOwner(boolean f) {
        follow = f;
    }
    
    public void setImpactTicks(int ticks) {
        impact = ticks;
    }
    
    public void setSize(float newSize) {
        size = newSize;
    }
    
    public int getAge() {
        return particleAge;
    }
    
    public int getMaxAge() {
        return particleMaxAge;
    }
    
    public void setEndsLoop(boolean loop) {
        endsLoop = loop;
    }
    
    public void renderStartAndEnd(BufferBuilder buffer, float partialTicks, float f1, float f2, float f3, float f4, float f5) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        Minecraft.getMinecraft().renderEngine.bindTexture(ParticleEngine.particleTexture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        int maxParticles = TAConfig.reducedEffects.getValue() ? 2 : 3;
        for (int i = 0; i < maxParticles; ++i) {
            int part = endsLoop ? (particleAge + i * 2) % 16 : Math.min(particleAge + i * 2, 16);
            float var8 = part / 16.0F;
            float var9 = var8 + 0.0624375F;
            float var10 = 0.8126875F;
            float var11 = var10 + 0.0624375F;
            float var12 = endMod / 2.0F;
            float var13 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
            float var14 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
            float var15 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);
            int j = 200 >> 16 & 0xFFFF;
            int k = 200 & 0xFFFF;
            float op = alphaFunc.apply(this);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
            buffer.pos((var13 - f1 * var12 - f4 * var12), (var14 - f2 * var12), (var15 - f3 * var12 - f5 * var12)).tex(var9, var11).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
            buffer.pos((var13 - f1 * var12 + f4 * var12), (var14 + f2 * var12), (var15 - f3 * var12 + f5 * var12)).tex(var9, var10).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
            buffer.pos((var13 + f1 * var12 + f4 * var12), (var14 + f2 * var12), (var15 + f3 * var12 + f5 * var12)).tex(var8, var10).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
            buffer.pos((var13 + f1 * var12 - f4 * var12), (var14 - f2 * var12), (var15 + f3 * var12 - f5 * var12)).tex(var8, var11).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
            Tessellator.getInstance().draw();
        }
        
        if (impact > 0) {
            for (int i = 0; i < maxParticles; ++i) {
                int part = endsLoop ? (particleAge + i * 2) % 16 : Math.min(particleAge + i * 2, 16);
                float var8 = part / 16.0F;
                float var9 = var8 + 0.0624375F;
                float var10 = 0.8126875F;
                float var11 = var10 + 0.0624375F;
                float var12 = endMod / 2.0F;
                float var13 = (float) (tXPrev + (tX - tXPrev) * partialTicks - interpPosX);
                float var14 = (float) (tYPrev + (tY - tYPrev) * partialTicks - interpPosY);
                float var15 = (float) (tZPrev + (tZ - tZPrev) * partialTicks - interpPosZ);
                int j = 200 >> 16 & 0xFFFF;
                int k = 200 & 0xFFFF;
                float op = alphaFunc.apply(this);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                buffer.pos((var13 - f1 * var12 - f4 * var12), (var14 - f2 * var12), (var15 - f3 * var12 - f5 * var12)).tex(var9, var11).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
                buffer.pos((var13 - f1 * var12 + f4 * var12), (var14 + f2 * var12), (var15 - f3 * var12 + f5 * var12)).tex(var9, var10).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
                buffer.pos((var13 + f1 * var12 + f4 * var12), (var14 + f2 * var12), (var15 + f3 * var12 + f5 * var12)).tex(var8, var10).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
                buffer.pos((var13 + f1 * var12 - f4 * var12), (var14 - f2 * var12), (var15 + f3 * var12 - f5 * var12)).tex(var8, var11).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
                Tessellator.getInstance().draw();
            }
        }
        
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }
    
    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX,
            float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        
        Tessellator.getInstance().draw();
        GlStateManager.pushMatrix();
        float slide = Minecraft.getMinecraft().player.ticksExisted;
        float rot = 0.0F;
        if (rotationSpeed > 0.0F)
            rot = (float) (world.provider.getWorldTime() % (360 / rotationSpeed) * rotationSpeed) + rotationSpeed * partialTicks;
        
        float cSize = size;
        if (pulse) {
            cSize = Math.min(particleAge / 4.0F, 1.0F);
            cSize = prevSize + (cSize - prevSize) * partialTicks;
        } 
        
        float op = alphaFunc.apply(this);
        Minecraft.getMinecraft().renderEngine.bindTexture(BEAM);
        GlStateManager.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
        GlStateManager.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
        GlStateManager.disableCull();
        float var11 = slide + partialTicks;
        if (reverse)
            var11 *= -1.0F; 
        
        float var12 = -var11 * 0.2F - MathHelper.floor(-var11 * 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        GlStateManager.depthMask(false);
        
        if (follow || !posSet) {
            Vec3d pos = ThaumicAugmentation.proxy.getRenderHelper().estimateImpulseCannonFiringPoint(source, 1.0F);
            posX = pos.x;
            posY = pos.y;
            posZ = pos.z;
            posSet = true;
        }
        
        double dx = posX - tX;
        double dy = posY - tY;
        double dz = posZ - tZ;
        length = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        double y = MathHelper.sqrt(dx * dx + dz * dz);
        rotYaw = (float) (Math.toDegrees(Math.atan2(dx, dz)));
        rotPitch = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dy, y)));
        
        GlStateManager.translate(prevPosX + (posX - prevPosX) * partialTicks - interpPosX,
                prevPosY + (posY - prevPosY) * partialTicks - interpPosY,
                prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);
        float ry = prevYaw + (rotYaw - prevYaw) * partialTicks;
        float rp = prevPitch + (rotPitch - prevPitch) * partialTicks;
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F + ry, 0.0F, 0.0F, -1.0F);
        GlStateManager.rotate(rp, 1.0F, 0.0F, 0.0F);
        double var44 = -0.15 * cSize;
        double var17 = 0.15 * cSize;
        double var44b = -0.15 * cSize * endMod;
        double var17b = 0.15 * cSize * endMod;
        int i = 200;
        int j = i >> 16 & 0xFFFF;
        int k = i & 0xFFFF;
        GlStateManager.rotate(rot, 0.0F, 1.0F, 0.0F);
        for (int t = 0; t < 3; t++) {
            double var29 = length * cSize;
            double var31 = 0.0;
            double var33 = 1.0;
            double var35 = -1.0F + var12 + t / 3.0F;
            double var37 = length * cSize + var35;
            GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
            buffer.pos(var44b, var29, 0.0).tex(var33, var37).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
            buffer.pos(var44, 0.0, 0.0).tex(var33, var35).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
            buffer.pos(var17, 0.0, 0.0).tex(var31, var35).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
            buffer.pos(var17b, var29, 0.0).tex(var31, var37).color(particleRed, particleGreen, particleBlue, op).lightmap(j, k).endVertex();
            Tessellator.getInstance().draw();
        } 
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        renderStartAndEnd(buffer, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
        
        Minecraft.getMinecraft().renderEngine.bindTexture(ParticleManager.PARTICLE_TEXTURES);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        prevSize = cSize;
    }
    
}
