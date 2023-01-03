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

package thecodex6824.thaumicaugmentation.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.common.entity.EntityFocusShield;

import javax.annotation.Nullable;

public class RenderFocusShield extends Render<EntityFocusShield> {
    
    protected static final double[] ALPHA_STRETCH = new double[] {
            180.0 / (Math.PI / 2.0),
            120.0 / (Math.PI / 2.0),
            60.0 / (Math.PI / 2.0)
    };
    
    public RenderFocusShield(RenderManager manager) {
        super(manager);
    }
    
    protected float getAlpha(EntityFocusShield entity, int layer, float baseAlpha, float partialTicks) {
        if (layer == 0)
            return (float) (Math.sin((entity.ticksExisted + partialTicks) / (120.0 / (Math.PI / 2.0))) + 1.0F) / 2.0F;
        else if (layer == 1)
            return (float) (Math.sin((entity.ticksExisted + partialTicks) / (90.0 / (Math.PI / 2.0))) + 1.0F) / 2.0F;
        else
            return Math.max((float) (Math.sin((entity.ticksExisted + partialTicks) / (60.0 / (Math.PI / 2.0))) + 1.0F) / 2.0F, Math.min(baseAlpha + 0.1F, 1.0F));
    }
    
    protected Vec3d getEntityLookVector(float yaw, float pitch) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d(f1 * f2, f3, f * f2);
    }
    
    @Override
    public void doRender(EntityFocusShield entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Entity rv = Minecraft.getMinecraft().getRenderViewEntity();
        if (rv == null)
            rv = Minecraft.getMinecraft().player;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + rv.getEyeHeight() / entity.height, z);
        GlStateManager.rotate(entity.rotationYaw + 180.0F, 0.0F, -1.0F, 0.0F);
        GlStateManager.rotate(entity.rotationPitch, -1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-entity.width / 2.0, -(rv.getEyeHeight() / entity.height), 0.0);
        GlStateManager.scale(entity.width, entity.height, entity.width);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int color = entity.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0F, g = ((color >> 8) & 0xFF) / 255.0F, b = (color & 0xFF) / 255.0F;
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        float baseAlpha = 0.25F;
        if (entity.getTotalLifespan() > 0 && entity.getTotalLifespan() - entity.getTimeAlive() <= 100)
            baseAlpha = entity.getTimeAlive() % 8 < 4 ? 0.0F : baseAlpha; 
        if (baseAlpha > 0.000001F) {
            for (ResourceLocation loc : TATextures.BASE_LAYERS) {
                bindTexture(loc);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos(0.0, 0.0, 0.0).tex(0.0, 1.0).color(r, g, b, baseAlpha).endVertex();
                buffer.pos(0.0, 1.0, 0.0).tex(0.0, 0.0).color(r, g, b, baseAlpha).endVertex();
                buffer.pos(1.0, 1.0, 0.0).tex(1.0, 0.0).color(r, g, b, baseAlpha).endVertex();
                buffer.pos(1.0, 0.0, 0.0).tex(1.0, 1.0).color(r, g, b, baseAlpha).endVertex();
                t.draw();
            }
            for (int i = 0; i < TATextures.RUNE_LAYERS.length; ++i) {
                float alpha = getAlpha(entity, i, baseAlpha, partialTicks);
                bindTexture(TATextures.RUNE_LAYERS[i]);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos(0.0, 0.0, 0.0).tex(0.0, 1.0).color(r, g, b, alpha).endVertex();
                buffer.pos(0.0, 1.0, 0.0).tex(0.0, 0.0).color(r, g, b, alpha).endVertex();
                buffer.pos(1.0, 1.0, 0.0).tex(1.0, 0.0).color(r, g, b, alpha).endVertex();
                buffer.pos(1.0, 0.0, 0.0).tex(1.0, 1.0).color(r, g, b, alpha).endVertex();
                t.draw();
            }
        }
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
    
    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityFocusShield entity) {
        return null;
    }
    
}
