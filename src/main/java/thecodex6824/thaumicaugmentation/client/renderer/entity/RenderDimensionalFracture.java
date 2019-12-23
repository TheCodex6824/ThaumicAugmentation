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

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.entity.EntityDimensionalFracture;

public class RenderDimensionalFracture extends Render<EntityDimensionalFracture> {

    protected static final ResourceLocation FRACTURE_TEXTURE_CLOSED = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/environment/emptiness_sky.png");
    protected static final ResourceLocation FRACTURE_TEXTURE_OPEN = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/environment/emptiness_sky.png");

    public RenderDimensionalFracture(RenderManager manager) {
        super(manager);
        shadowSize = 0.0F;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDimensionalFracture entity) {
        return entity.isOpen() ? FRACTURE_TEXTURE_OPEN : FRACTURE_TEXTURE_CLOSED;
    }

    @Override
    public void doRender(EntityDimensionalFracture entity, double x, double y, double z, float yaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        ThaumicAugmentation.proxy.getRenderHelper().renderDimensionalFracture(entity.isOpen(),
                entity.getEntityWorld().getTotalWorldTime(), entity.getTimeOpened(), entity.getOpeningDuration(), partialTicks, 6, false, 1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

}
