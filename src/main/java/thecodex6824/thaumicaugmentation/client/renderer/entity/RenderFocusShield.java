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

package thecodex6824.thaumicaugmentation.client.renderer.entity;

import javax.annotation.Nullable;

import org.lwjgl.util.glu.Disk;
import org.lwjgl.util.glu.GLU;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.common.entity.EntityFocusShield;

public class RenderFocusShield extends Render<EntityFocusShield> {

    protected static final Disk THE_DISK = new Disk();
    
    public RenderFocusShield(RenderManager manager) {
        super(manager);
        THE_DISK.setDrawStyle(GLU.GLU_FILL);
        THE_DISK.setNormals(GLU.GLU_NONE);
    }
    
    @Override
    public void doRender(EntityFocusShield entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Entity rv = Minecraft.getMinecraft().getRenderViewEntity();
        if (rv == null)
            rv = Minecraft.getMinecraft().player;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + rv.getEyeHeight() / entity.height + 0.15, z);
        
        GlStateManager.rotate(entity.rotationYaw + 180.0F, 0.0F, -1.0F, 0.0F);
        GlStateManager.rotate(entity.rotationPitch, -1.0F, 0.0F, 0.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        int color = Aspect.PROTECT.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0F, g = ((color >> 8) & 0xFF) / 255.0F, b = (color & 0xFF) / 255.0F, a = 0.25F;
        GlStateManager.color(r, g, b, a);
        THE_DISK.draw(entity.height / 2.0F, 0, 6, 6);
        GlStateManager.color(0.0F, 0.0F, 0.0F, a * 2);
        THE_DISK.draw(entity.height / 2.0F + 0.01F, entity.height / 2.0F, 6, 6);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
    
    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityFocusShield entity) {
        return null;
    }
    
}
