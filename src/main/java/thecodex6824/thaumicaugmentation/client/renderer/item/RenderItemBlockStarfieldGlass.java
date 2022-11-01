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

package thecodex6824.thaumicaugmentation.client.renderer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.client.model.BuiltInRendererModel;
import thecodex6824.thaumicaugmentation.client.model.CachedBakedModel;
import thecodex6824.thaumicaugmentation.client.model.CachedBakedModelComplex;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager;
import thecodex6824.thaumicaugmentation.client.shader.TAShaders;

public class RenderItemBlockStarfieldGlass extends TileEntityItemStackRenderer {

    protected CachedBakedModel<IBakedModel> glassBlock;
    protected CachedBakedModelComplex<ItemStack, BuiltInRendererModel.BakedModel> glassItem;
    
    public RenderItemBlockStarfieldGlass() {
        glassBlock = new CachedBakedModel<>(() -> Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(TABlocks.STARFIELD_GLASS.getDefaultState()));
        glassItem = new CachedBakedModelComplex<>(stack -> {
            IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null);
            if (model instanceof BuiltInRendererModel.BakedModel)
                return (BuiltInRendererModel.BakedModel) model;
            else
                throw new IllegalArgumentException("Item model doesn't belong to us");
        });
    }
    
    @Override
    public void renderByItem(ItemStack stack) {
        GlStateManager.disableLighting();
        switch (stack.getMetadata()) {
            case 0: {
                if (TAShaderManager.shouldUseShaders())
                    TAShaderManager.enableShader(TAShaders.FLUX_RIFT, glassItem.get(stack).getLastTransformType() == TransformType.GUI ? 
                            TAShaders.SHADER_CALLBACK_CONSTANT_SPHERE_ZOOMED_5 : TAShaders.SHADER_CALLBACK_CONSTANT_SPHERE);
                else
                    GlStateManager.color(0.1F, 0.4F, 0.5F, 1.0F);
                
                Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.RIFT);
                break;
            }
            case 1: {
                if (TAShaderManager.shouldUseShaders())
                    TAShaderManager.enableShader(TAShaders.FRACTURE, glassItem.get(stack).getLastTransformType() == TransformType.GUI ? 
                            TAShaders.SHADER_CALLBACK_CONSTANT_SPHERE_ZOOMED_5 : TAShaders.SHADER_CALLBACK_CONSTANT_SPHERE);
                
                Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.EMPTINESS_SKY);
                break;
            }
            case 2: {
                if (TAShaderManager.shouldUseShaders())
                    TAShaderManager.enableShader(TAShaders.MIRROR, glassItem.get(stack).getLastTransformType() == TransformType.GUI ? 
                            TAShaders.SHADER_CALLBACK_CONSTANT_SPHERE_ZOOMED_5 : TAShaders.SHADER_CALLBACK_CONSTANT_SPHERE);
                
                Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.MIRROR);
                break;
            }
            default: {
                // should be unreachable
                Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.RIFT);
                break;
            }
        }
        
        if (glassItem.get(stack).getLastTransformType() == TransformType.FIXED)
            GlStateManager.disableCull();
        
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(0.001, 0.999, 0.999).tex(0, 0).endVertex();
        buffer.pos(0.999, 0.999, 0.999).tex(1, 0).endVertex();
        buffer.pos(0.001, 0.001, 0.999).tex(0, 1).endVertex();
        buffer.pos(0.999, 0.001, 0.999).tex(1, 1).endVertex();
        buffer.pos(0.999, 0.001, 0.001).tex(1, 0).endVertex();
        buffer.pos(0.999, 0.999, 0.999).tex(0, 1).endVertex();
        buffer.pos(0.999, 0.999, 0.001).tex(0, 0).endVertex();
        buffer.pos(0.001, 0.999, 0.999).tex(1, 1).endVertex();
        buffer.pos(0.001, 0.999, 0.001).tex(1, 0).endVertex();
        buffer.pos(0.001, 0.001, 0.999).tex(0, 1).endVertex();
        buffer.pos(0.001, 0.001, 0.001).tex(0, 0).endVertex();
        buffer.pos(0.999, 0.001, 0.001).tex(1, 0).endVertex();
        buffer.pos(0.001, 0.999, 0.001).tex(0, 1).endVertex();
        buffer.pos(0.999, 0.999, 0.001).tex(1, 1).endVertex();
        t.draw();
        
        if (glassItem.get(stack).getLastTransformType() == TransformType.FIXED)
            GlStateManager.enableCull();
   
        if (TAShaderManager.shouldUseShaders())
            TAShaderManager.disableShader();
        else
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5, 0.5, 0.5);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, glassBlock.get().getOverrides().handleItemState(glassBlock.get(), stack, null, null));
        GlStateManager.popMatrix();
    }
    
}
