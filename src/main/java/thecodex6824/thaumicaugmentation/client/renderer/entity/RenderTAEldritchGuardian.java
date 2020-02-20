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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import thaumcraft.client.renderers.models.entity.ModelEldritchGuardian;
import thaumcraft.common.entities.monster.boss.EntityEldritchWarden;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAEldritchGuardian;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAEldritchWarden;

public class RenderTAEldritchGuardian extends RenderLiving<EntityLiving> {

    protected static final ResourceLocation GUARDIAN = new ResourceLocation("thaumcraft", "textures/entity/eldritch_guardian.png");
    protected static final ResourceLocation WARDEN = new ResourceLocation("thaumcraft", "textures/entity/eldritch_warden.png");
    
    public RenderTAEldritchGuardian(RenderManager manager, ModelEldritchGuardian model, float shadowSize) {
        super(manager, model, shadowSize);
    }
    
    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float entityYaw,
            float partialTicks) {
        
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        float alpha = 1.0F;
        Entity e = Minecraft.getMinecraft().getRenderViewEntity();
        if (e == null)
            e = Minecraft.getMinecraft().player;
        
        float maxDist = e.world.getDifficulty() == EnumDifficulty.HARD ? 576.0F : 1024.0F;
        float distThreshold = 256.0F;
        if (entity instanceof EntityTAEldritchGuardian && !((EntityTAEldritchGuardian) entity).isTransparent() ||
                entity instanceof EntityTAEldritchWarden && !((EntityTAEldritchWarden) entity).isTransparent()) {
            alpha = 1.0F;
        }
        else {
            double dist = entity.getDistanceSq(e);
            if (dist < 256.0)
                alpha = 0.6F;
            else
                alpha = (float) (1.0F - Math.min(maxDist - distThreshold, dist - distThreshold) / maxDist - distThreshold) * 0.6F;
        }
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        double ry = y;
        if (entity instanceof EntityEldritchWarden)
            ry -= entity.height * (((EntityEldritchWarden) entity).getSpawnTimer() / 150.0);
            
        super.doRender(entity, x, ry, z, entityYaw, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
    }
    
    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityLiving entity) {
        return entity instanceof EntityEldritchWarden ? WARDEN : GUARDIAN;
    }
    
}
