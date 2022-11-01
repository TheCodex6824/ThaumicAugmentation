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

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.common.entity.EntityPrimalWisp;

import javax.annotation.Nullable;

public class RenderPrimalWisp extends Render<EntityPrimalWisp> {

    // rotation origin offset and rotation axes
    protected static final float[][] ASPECT_ROTATIONS = new float[][] {
        {0.0F, 0.0F, 0.75F, 1.0F, 0.0F, 0.0F},
        {0.75F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F},
        {0.5F, 0.0F, 0.5F, 0.0F, 1.0F, 0.0F},
        {-0.5F, 0.75F, 0.0F, 1.0F, 0.0F, 1.0F},
        {0.0F, 0.5F, -0.75F, 1.0F, 1.0F, 0.0F},
        {-0.75F, -0.5F, 0.0F, 0.0F, 1.0F, 1.0F},
        {0.25F, 0.25F, 0.25F, 1.0F, 1.0F, 1.0F}
    };
    
    public RenderPrimalWisp(RenderManager m) {
        super(m);
        shadowSize = 0.0F;
    }
    
    @Override
    public void doRender(EntityPrimalWisp entity, double x, double y, double z, float entityYaw, float partialTicks) {
        bindTexture(TATextures.TC_PARTICLES);
        GlStateManager.pushMatrix();
        // force sync the gl state because TC breaks it by not using GlStateManager (again...)
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0145F);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0145F);
        GlStateManager.depthMask(false);
        GL11.glDepthMask(false);
        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.translate(x, y + entity.height / 2.0, z);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((renderManager.options.thirdPersonView == 2 ? -1.0F : 1.0F) * renderManager.playerViewX,
                1.0F, 0.0F, 0.0F);
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.disableLighting();
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        double tX = (entity.ticksExisted % 16 + 512.0) % 64.0 / 64.0;
        double tY = 0.1255;
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(1.125, -1.125, 0.0).tex(tX + 0.015625, tY + 0.015625).endVertex();
        buffer.pos(-1.125, -1.125, 0.0).tex(tX, tY + 0.015625).endVertex();
        buffer.pos(-1.125, 1.125, 0.0).tex(tX, tY).endVertex();
        buffer.pos(1.125, 1.125, 0.0).tex(tX + 0.015625, tY).endVertex();
        t.draw();
        
        tX = (entity.ticksExisted % 16 + 320.0) % 64.0 / 64.0;
        tY = 0.078125;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.25F);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(1.6875, -1.6875, 0.0).tex(tX + 0.015625, tY + 0.015625).endVertex();
        buffer.pos(-1.6875, -1.6875, 0.0).tex(tX, tY + 0.015625).endVertex();
        buffer.pos(-1.6875, 1.6875, 0.0).tex(tX, tY).endVertex();
        buffer.pos(1.6875, 1.6875, 0.0).tex(tX + 0.015625, tY).endVertex();
        t.draw();
        
        bindTexture(TATextures.NODE);
        tX = (entity.ticksExisted % 16.0 + 800.0) % 32.0 / 32.0;
        tY = 0.78125;
        GlStateManager.color(1.0F, 1.0F, 0.8F, 0.75F);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(1.125, -1.125, 0.0).tex(tX + 0.03125, tY + 0.03125).endVertex();
        buffer.pos(-1.125, -1.125, 0.0).tex(tX, tY + 0.03125).endVertex();
        buffer.pos(-1.125, 1.125, 0.0).tex(tX, tY).endVertex();
        buffer.pos(1.125, 1.125, 0.0).tex(tX + 0.03125, tY).endVertex();
        t.draw();
        
        GlStateManager.popMatrix();
        for (int i = 0; i < Aspect.getPrimalAspects().size(); ++i) {
            Aspect aspect = Aspect.getPrimalAspects().get(i);
            float[] rot = i < ASPECT_ROTATIONS.length ? ASPECT_ROTATIONS[i] :
                ASPECT_ROTATIONS[ASPECT_ROTATIONS.length - 1];
            GlStateManager.pushMatrix();
            float rotateAmount = (entity.ticksExisted + partialTicks) % 40 / 40.0F * 360.0F;
            GlStateManager.rotate(rotateAmount, rot[3], rot[4], rot[5]);
            GlStateManager.translate(rot[0], rot[1], rot[2]);
            GlStateManager.rotate(-rotateAmount, rot[3], rot[4], rot[5]);
            GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((renderManager.options.thirdPersonView == 2 ? -1.0F : 1.0F) * renderManager.playerViewX,
                    1.0F, 0.0F, 0.0F);
            int color = aspect.getColor();
            GlStateManager.color(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F, 0.55F);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(0.5625, -0.5625, 0.0).tex(tX + 0.03125, tY + 0.03125).endVertex();
            buffer.pos(-0.5625, -0.5625, 0.0).tex(tX, tY + 0.03125).endVertex();
            buffer.pos(-0.5625, 0.5625, 0.0).tex(tX, tY).endVertex();
            buffer.pos(0.5625, 0.5625, 0.0).tex(tX + 0.03125, tY).endVertex();
            t.draw();
            GlStateManager.popMatrix();
        }
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableLighting();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.popMatrix();
    }
    
    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityPrimalWisp entity) {
        return null;
    }
    
}
