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

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.entity.ModelEldritchGolem;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAEldritchGolem;

public class RenderTAEldritchGolem extends RenderLiving<EntityTAEldritchGolem> {

    protected static ResourceLocation TEXTURE = new ResourceLocation("thaumcraft", "textures/entity/eldritch_golem.png");
    
    public RenderTAEldritchGolem(RenderManager manager, ModelEldritchGolem golemModel, float shadowSize) {
        super(manager, golemModel, shadowSize);
    }
    
    @Override
    protected void preRenderCallback(EntityTAEldritchGolem entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(1.7F, 1.7F, 1.7F);
    }
    
    @Override
    public void doRender(EntityTAEldritchGolem entity, double x, double y, double z, float entityYaw,
            float partialTicks) {
        
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableBlend();
    }
    
    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityTAEldritchGolem entity) {
        return TEXTURE;
    }
    
}
