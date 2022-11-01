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

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import thaumcraft.client.lib.obj.AdvancedModelLoader;
import thaumcraft.client.lib.obj.IModelCustom;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.common.entity.EntityCelestialObserver;

import javax.annotation.Nullable;

public class RenderCelestialObserver extends Render<EntityCelestialObserver> {

    protected static final ResourceLocation MODEL = new ResourceLocation(ThaumicAugmentationAPI.MODID, "models/entity/celestial_observer.obj");
    
    protected IModelCustom model;
    
    public RenderCelestialObserver(RenderManager manager) {
        super(manager);
        model = AdvancedModelLoader.loadModel(MODEL);
    }
    
    @Override
    public boolean isMultipass() {
        return true;
    }
    
    protected void renderSolid(EntityCelestialObserver entity, double x, double y, double z, float entityYaw, float partialTicks) {
        bindTexture(getEntityTexture(entity));
        GlStateManager.pushMatrix();
        model.renderPart("legs");
        GlStateManager.translate(entity.width / 2.0F, 0.95F, entity.width / 2.0F);
        GlStateManager.rotate(entity.prevRotationYawHead + (entity.rotationYawHead - entity.prevRotationYawHead) * partialTicks,
                0.0F, -1.0F, 0.0F);
        GlStateManager.translate(-entity.width / 2.0F, -0.95F, -entity.width / 2.0F);
        model.renderPart("pivot");
        GlStateManager.translate(entity.width / 2.0F, 0.875F, entity.width / 2.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks,
                1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-entity.width / 2.0F, -0.875F, -entity.width / 2.0F);
        model.renderPart("body");
        bindTexture(TATextures.THAUMOMETER);
        model.renderPart("thaumometer_solid");
        GlStateManager.popMatrix();
    }
    
    protected void renderTransparent(EntityCelestialObserver entity, double x, double y, double z, float entityYaw, float partialTicks) {
        bindTexture(TATextures.THAUMOMETER_GLASS);
        GlStateManager.pushMatrix();
        GlStateManager.translate(entity.width / 2.0F, 0.875F, entity.width / 2.0F);
        GlStateManager.rotate(entity.prevRotationYawHead + (entity.rotationYawHead - entity.prevRotationYawHead) * partialTicks,
                0.0F, -1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks,
                1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-entity.width / 2.0F, -0.875F, -entity.width / 2.0F);
        model.renderPart("thaumometer_transparent");
        GlStateManager.popMatrix();
    }
    
    @Override
    public void doRender(EntityCelestialObserver entity, double x, double y, double z, float entityYaw,
            float partialTicks) {
        
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        if (partialTicks == 1.0F) { // detect TOP hud rendering
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            GlStateManager.translate(x - 0.5, y, z - 0.5);
            if (entity.hurtTime > 0) {
                GlStateManager.color(1.0F, 0.5F, 0.5F, 1.0F);
                float shake = entity.hurtTime / 2048.0F;
                GlStateManager.translate(entity.getRNG().nextGaussian() * shake, entity.getRNG().nextGaussian() * shake,
                        entity.getRNG().nextGaussian() * shake);
            }
            
            renderSolid(entity, x, y, z, entityYaw, partialTicks);
            renderTransparent(entity, x, y, z, entityYaw, partialTicks);
            GlStateManager.popMatrix();
        }
    }
    
    @Override
    public void renderMultipass(EntityCelestialObserver entity, double x, double y, double z, float entityYaw,
            float partialTicks) {
        
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.translate(x - 0.5, y, z - 0.5);
        if (entity.hurtTime > 0) {
            GlStateManager.color(1.0F, 0.5F, 0.5F, 1.0F);
            float shake = entity.hurtTime / 2048.0F;
            GlStateManager.translate(entity.getRNG().nextGaussian() * shake, entity.getRNG().nextGaussian() * shake,
                    entity.getRNG().nextGaussian() * shake);
        }
        
        if (MinecraftForgeClient.getRenderPass() == 0)
            renderSolid(entity, x, y, z, entityYaw, partialTicks);
        else {
            GlStateManager.enableBlend();
            renderTransparent(entity, x, y, z, entityYaw, partialTicks);
        }
        
        GlStateManager.popMatrix();
    }
    
    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityCelestialObserver entity) {
        return TATextures.CELESTIAL_OBSERVER;
    }
    
}
