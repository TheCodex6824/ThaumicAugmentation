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

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.lib.obj.AdvancedModelLoader;
import thaumcraft.client.lib.obj.IModelCustom;
import thaumcraft.common.items.casters.ItemFocus;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.entity.EntityAutocasterBase;

public class RenderAutocaster<T extends EntityAutocasterBase> extends Render<T> {

    protected static final ResourceLocation MODEL = new ResourceLocation(ThaumicAugmentationAPI.MODID, "models/entity/autocaster.obj");
    protected static final ResourceLocation TEXTURE_NORMAL = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/entities/autocaster.png");
    protected static final ResourceLocation TEXTURE_ELDRITCH = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/entities/autocaster_eldritch.png");
    
    protected boolean eldritch;
    protected IModelCustom model;
    
    public RenderAutocaster(RenderManager manager, boolean eldritchTexture) {
        super(manager);
        eldritch = eldritchTexture;
        model = AdvancedModelLoader.loadModel(MODEL);
    }
    
    @Override
    public void doRender(EntityAutocasterBase entity, double x, double y, double z, float entityYaw, float partialTicks) {
        bindTexture(eldritch ? TEXTURE_ELDRITCH : TEXTURE_NORMAL);
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.translate(x, y, z);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.5, 0);
        EnumFacing facing = entity.getFacing();
        switch (facing) {
            case DOWN: {
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
                break;
            }
            case EAST: {
                GlStateManager.rotate(270.0F, 0.0F, 0.0F, 1.0F);
                break;
            }
            case NORTH: {
                GlStateManager.rotate(270.0F, 1.0F, 0.0F, 0.0F);
                break;
            }
            case SOUTH: {
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            }
            case WEST: {
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                break;
            }
            case UP:
            default:
                break;
        }
        GlStateManager.translate(0, -0.5, 0);
        model.renderPart("base");
        GlStateManager.popMatrix();
        GlStateManager.translate(0, 0.5, 0);
        GlStateManager.rotate(entity.prevRotationYawHead + (entity.rotationYawHead - entity.prevRotationYawHead) * partialTicks,
                0.0F, -1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks,
                1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0, -0.5, 0);
        model.renderPart("sphere");
        ItemStack focus = entity.getHeldItemMainhand();
        if (focus != null && focus.getItem() instanceof ItemFocus) {
            int color = ((ItemFocus) focus.getItem()).getFocusColor(focus);
            GlStateManager.color(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, 1.0F);
            model.renderPart("focus");
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }
    
    @Override
    @Nullable
    protected ResourceLocation getEntityTexture(EntityAutocasterBase entity) {
        return eldritch ? TEXTURE_ELDRITCH : TEXTURE_NORMAL;
    }
    
}
