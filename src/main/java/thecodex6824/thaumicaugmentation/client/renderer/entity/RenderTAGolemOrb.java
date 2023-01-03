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

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAGolemOrb;

import javax.annotation.Nullable;

public class RenderTAGolemOrb extends Render<EntityTAGolemOrb> {

    public RenderTAGolemOrb(RenderManager manager) {
        super(manager);
        shadowSize = 0.0F;
    }
    
    @Override
    public void doRender(EntityTAGolemOrb entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        GlStateManager.depthMask(false);
        bindTexture(TATextures.TC_PARTICLES);
        double x1 = (1 + entity.ticksExisted % 6) / 32.0;
        double x2 = x1 + 0.03125;
        double y1 = entity.isRed() ? 0.1875 : 0.21875;
        double y2 = y1 + 0.03125;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.8F);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((renderManager.options.thirdPersonView == 2 ? -1.0F : 1.0F) * renderManager.playerViewX,
                1.0F, 0.0F, 0.0F);
        float scale = MathHelper.sin(entity.ticksExisted / 5.0F) * 0.2F + 0.2F;
        GlStateManager.scale(1.0F + scale, 1.0F + scale, 1.0F + scale);
        int sky = (220 >> 16) & 0xFFFF;
        int block = 220 & 0xFFFF;
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        buffer.pos(entity.width / 2.0, 0.0, 0.0).tex(x2, y2).lightmap(sky, block).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.pos(-entity.width / 2.0, 0.0, 0.0).tex(x1, y2).lightmap(sky, block).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.pos(-entity.width / 2.0, entity.height, 0.0).tex(x1, y1).lightmap(sky, block).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.pos(entity.width / 2.0, entity.height, 0.0).tex(x2, y1).lightmap(sky, block).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        t.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    
    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityTAGolemOrb entity) {
        return null;
    }
    
}
