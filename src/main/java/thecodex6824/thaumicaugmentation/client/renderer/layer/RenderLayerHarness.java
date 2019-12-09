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

package thecodex6824.thaumicaugmentation.client.renderer.layer;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.lib.obj.AdvancedModelLoader;
import thaumcraft.client.lib.obj.IModelCustom;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

public class RenderLayerHarness implements LayerRenderer<EntityPlayer> {

    protected static final ResourceLocation BASE_TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/models/harness/harness_base.png");
    protected static final ResourceLocation THAUMOSTATIC_TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/models/harness/thaumostatic_module.png"); 
    protected static final ResourceLocation THAUMOSTATIC_MODEL = new ResourceLocation(ThaumicAugmentationAPI.MODID, "models/entity/thaumostatic_module.obj");
    
    protected RenderPlayer render;
    protected ModelBiped base;
    protected IModelCustom thaumostatic;
    
    public RenderLayerHarness(RenderPlayer renderer) {
        render = renderer;
        base = new ModelBiped(1.0F);
        thaumostatic = AdvancedModelLoader.loadModel(THAUMOSTATIC_MODEL);
    }
    
    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        
        ItemStack harness = ItemStack.EMPTY;
        IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
        if (baubles != null) {
            ItemStack stack = baubles.getStackInSlot(BaubleType.BODY.getValidSlots()[0]);
            if (stack.getItem() == TAItems.THAUMOSTATIC_HARNESS)
                harness = stack;
        }
        
        if (!harness.isEmpty()) {
            base.setModelAttributes(render.getMainModel());
            base.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTicks);
            render.bindTexture(BASE_TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            base.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            if (harness.getItem() == TAItems.THAUMOSTATIC_HARNESS) {
                render.bindTexture(THAUMOSTATIC_TEXTURE);
                GlStateManager.pushMatrix();
                GlStateManager.enableRescaleNormal();
                GlStateManager.scale(0.1F, 0.1F, 0.1F);
                GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.33F, -3.7F);
                thaumostatic.renderAll();
                GlStateManager.disableRescaleNormal();
                GlStateManager.popMatrix();
            }
        }
    }
    
    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
    
}
