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

package thecodex6824.thaumicaugmentation.client.renderer.tile;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import thaumcraft.common.items.tools.ItemThaumometer;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.common.tile.TileAltar;

public class RenderAltar extends TileEntitySpecialRenderer<TileAltar> {

    protected boolean hasThaumometer(Entity e) {
        if (e instanceof EntityLivingBase) {
            ItemStack held = ((EntityLivingBase) e).getHeldItemMainhand();
            if (held.getItem() instanceof ItemThaumometer)
                return true;
            
            held = ((EntityLivingBase) e).getHeldItemOffhand();
            if (held.getItem() instanceof ItemThaumometer)
                return true;
        }
        
        return false;
    }
    
    @Override
    public void render(TileAltar te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        Entity rv = Minecraft.getMinecraft().getRenderViewEntity();
        if (rv == null)
            rv = Minecraft.getMinecraft().player;
        
        if (te.isOpen() || EntityUtils.hasRevealer(rv) || hasThaumometer(rv)) {
            int open = !te.isOpen() ? 0 : Math.min(280 - te.getOpenTicks(), 100);
            double jitter = !te.isOpen() || te.getOpenTicks() > 180 ? 0.0 :
                (Math.sin(rv.ticksExisted + partialTicks) / 4.0) * Math.min(180 - te.getOpenTicks(), 100) / 100.0;
            double scaleX = (int) Math.min(30.0, open + partialTicks) / 30.0 + 0.1 + jitter;
            double scaleY = (int) Math.min(5.0, open + partialTicks) / 5.0 + 0.075 + jitter;
            bindTexture(TATextures.ELDRITCH_PORTAL);
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
            GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            Tessellator t = Tessellator.getInstance();
            BufferBuilder buffer = t.getBuffer();
            float ticks = rv.ticksExisted % 16 / 16.0F;
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(scaleX, -scaleY, 0.0).tex(ticks, 1.0).endVertex();
            buffer.pos(-scaleX, -scaleY, 0.0).tex(ticks + 0.0625, 1.0).endVertex();
            buffer.pos(-scaleX, scaleY, 0.0).tex(ticks + 0.0625, 0.0).endVertex();
            buffer.pos(scaleX, scaleY, 0.0).tex(ticks, 0.0).endVertex();
            t.draw();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }
    
}
