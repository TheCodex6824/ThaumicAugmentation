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

package thecodex6824.thaumicaugmentation.client.renderer.item;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.client.model.CachedBakedModel;

public class RenderItemBlockRiftJar extends TileEntityItemStackRenderer {

    protected Cache<ItemStack, FluxRiftReconstructor> rifts;
    protected CachedBakedModel<IBakedModel> jar;
    
    public RenderItemBlockRiftJar() {
        rifts = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterAccess(
                3000, TimeUnit.MILLISECONDS).maximumSize(250).build();
        jar = new CachedBakedModel<>(() -> Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(TABlocks.RIFT_JAR.getDefaultState()));
    }
    
    @Override
    public void renderByItem(ItemStack stack) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.25F, 0.5F);
        if (stack.hasTagCompound()) {
            FluxRiftReconstructor rift = null;
            try {
                rift = rifts.get(stack, () -> new FluxRiftReconstructor(stack.getTagCompound().getInteger("seed"),
                        stack.getTagCompound().getInteger("size")));
            }
            catch (ExecutionException ex) {
                ThaumicAugmentation.getLogger().error("FluxRiftReconstructor somehow had an error: " + ex.getMessage());
                return;
            }
            
            int tess = 6;
            if (stack.getItemFrame() != null) {
                Entity rv = Minecraft.getMinecraft().getRenderViewEntity() != null ? Minecraft.getMinecraft().getRenderViewEntity() :
                    Minecraft.getMinecraft().player;
                double dist = rv.getPositionEyes(Minecraft.getMinecraft().getRenderPartialTicks()).squareDistanceTo(stack.getItemFrame().getPositionVector());
                if (dist <= 64 * 64) {
                    if (dist > 16 * 16)
                        tess = 2;
                    else if (dist > 8 * 8)
                        tess = 3;
                }
                else
                    tess = 0;
            }
            
            if (tess > 0) {
                GlStateManager.pushMatrix();
                AxisAlignedBB box = rift.getBoundingBox();
                GlStateManager.scale(0.3125 / (Math.max(Math.abs(box.minX), Math.abs(box.maxX)) + 0.075), 0.375 / (Math.max(Math.abs(box.minY), Math.abs(box.maxY)) + 0.075),
                        0.3125 / (Math.max(Math.abs(box.minZ), Math.abs(box.maxZ)) + 0.075));
                ThaumicAugmentation.proxy.getRenderHelper().renderFluxRift(rift, 0, Minecraft.getMinecraft().getRenderPartialTicks(), tess, true);
                GlStateManager.popMatrix();
            }
        }
        
        GlStateManager.translate(0.0F, 0.375F, 0.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, jar.get());
        GlStateManager.popMatrix();
    }
    
}
