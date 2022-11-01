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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RenderItemImportant extends RenderEntityItem {

    public RenderItemImportant(RenderManager manager, RenderItem render) {
        super(manager, render);
    }
    
    @Override
    public boolean isMultipass() {
        return true;
    }
    
    @Override
    public void doRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks) {}
    
    @Override
    public void renderMultipass(EntityItem entity, double x, double y, double z, float entityYaw,
            float partialTicks) {
        
        if (MinecraftForgeClient.getRenderPass() == 0)
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        else
            renderBeams(entity, x, y, z, entityYaw, partialTicks);
    }
    
    protected void renderBeams(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float bob = shouldBob() ? MathHelper.sin((entity.getAge() + partialTicks) / 10.0F + entity.hoverStart) * 0.1F + 0.1F : 0.0F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + bob + 0.35F, z);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        GlStateManager.enableCull();
        GlStateManager.depthMask(false);
        Random rng = new Random(1337);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        int beams = Minecraft.getMinecraft().gameSettings.fancyGraphics ? 10 : 5;
        for (int i = 0; i < beams; ++i) {
            GlStateManager.rotate(rng.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(rng.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(rng.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(rng.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(rng.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(rng.nextFloat() * 360.0F + entity.getAge() / 500.0F * 360.0F, 0.0F, 0.0F, 1.0F);
            float bH = rng.nextFloat() * 0.5F;
            float bY = rng.nextFloat() * 0.5F + 1.0F;           
            buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(0.0, 0.0, 0.0).color(1.0F, 1.0F, 1.0F, 0.75F).endVertex();
            buffer.pos(-0.866 * bH, bY, -0.5F * bH).color(1.0F, 0.0F, 1.0F, 0.0F).endVertex();
            buffer.pos(0.866 * bH, bY, -0.5F * bH).color(1.0F, 0.0F, 1.0F, 0.0F).endVertex();
            buffer.pos(0.0, bY, bH).color(1.0F, 0.0F, 1.0F, 0.0F).endVertex();
            buffer.pos(-0.866 * bH, bY, -0.5F * bH).color(1.0F, 0.0F, 1.0F, 0.0F).endVertex();
            t.draw();
        }
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.disableCull();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
    
}
