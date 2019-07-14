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

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class FXBlockWardFixed extends Particle {
    
    protected ResourceLocation[] textures = new ResourceLocation[15];
    protected EnumFacing side;
    protected int rotation = 0;
    protected float sx = 0.0F;
    protected float sy = 0.0F;
    protected float sz = 0.0F;
    
    public FXBlockWardFixed(World world, double x, double y, double z, EnumFacing side, float hX, float hY, float hZ) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.side = side;
        
        motionX = 0.0;
        motionY = 0.0;
        motionZ = 0.0;
        particleGravity = 0.0F;
        particleMaxAge = 12 + rand.nextInt(5);
      
        setSize(0.01F, 0.01F);
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
      
        particleScale = ((float)(1.4D + rand.nextGaussian() * 0.3));
        rotation = rand.nextInt(360);
        sx = (float) MathHelper.clamp(hX - 0.6F + rand.nextFloat() * 0.2F, -0.4F, 0.4F);
        sy = (float) MathHelper.clamp(hY - 0.6F + rand.nextFloat() * 0.2F, -0.4F, 0.4F);
        sz = (float) MathHelper.clamp(hZ - 0.6F + rand.nextFloat() * 0.2F, -0.4F, 0.4F);
        if (side.getXOffset() != 0)
            sx = 0.0F;
        if (side.getYOffset() != 0)
            sy = 0.0F;
        if (side.getZOffset() != 0)
            sz = 0.0F;
     
        for (int i = 0; i < 15; ++i)
            textures[i] = new ResourceLocation("thaumcraft", "textures/models/hemis" + (i + 1) + ".png");
     
    }
    
    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {
        Tessellator.getInstance().draw();
        GL11.glPushMatrix();
        float fade = (particleAge + partialTicks) / particleMaxAge;
        int frame = (int) Math.min(textures.length - 1, Math.max(0, (textures.length - 1) * fade));
        Minecraft.getMinecraft().renderEngine.bindTexture(textures[frame]);
      
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
      
        GL11.glColor4f(1.0F, 1.0F, 1.0F, particleAlpha / 2.0F);
      
        float drawX = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
        float drawY = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
        float drawZ = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);
      
        GL11.glTranslated(drawX + sx, drawY + sy, drawZ + sz);
      
        GL11.glRotatef(90.0F, side.getYOffset(), -side.getXOffset(), side.getZOffset());
        GL11.glRotatef(rotation, 0.0F, 0.0F, 1.0F);
        if (side.getZOffset() > 0) {
            GL11.glTranslated(0.0D, 0.0D, 0.501);
            GL11.glRotatef(180.0F, 0.0F, -1.0F, 0.0F);
        }
        else
            GL11.glTranslated(0.0D, 0.0D, -0.501);
      
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        int s = 240 >> 16 & 0xFFFF;
        int b = 240 & 0xFFFF;
        buffer.pos(-0.5 * particleScale, 0.5 * particleScale, 0.0).tex(0.0, 1.0).color(particleRed, particleGreen, particleBlue, particleAlpha / 2.0F).lightmap(s, b).endVertex();
        buffer.pos(0.5 * particleScale, 0.5 * particleScale, 0.0).tex(1.0, 1.0).color(particleRed, particleGreen, particleBlue, particleAlpha / 2.0F).lightmap(s, b).endVertex();
        buffer.pos(0.5 * particleScale, -0.5 * particleScale, 0.0).tex(1.0, 0.0).color(particleRed, particleGreen, particleBlue, particleAlpha / 2.0F).lightmap(s, b).endVertex();
        buffer.pos(-0.5 * particleScale, -0.5 * particleScale, 0.0).tex(0.0, 0.0).color(particleRed, particleGreen, particleBlue, particleAlpha / 2.0F).lightmap(s, b).endVertex();
        Tessellator.getInstance().draw();
      
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
      
        GL11.glPopMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(ParticleManager.PARTICLE_TEXTURES);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
    }
    
    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
          
        float threshold = particleMaxAge / 5.0F;
        if (particleAge <= threshold)
            particleAlpha = particleAge / threshold;
        else
            particleAlpha = (float) (particleMaxAge - particleAge) / particleMaxAge;
          
        if (particleAge++ >= particleMaxAge)
            setExpired();
          
        motionY -= 0.04 * particleGravity;
          
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
    }
    
    public void setGravity(float value) {
        particleGravity = value;
    }
}
