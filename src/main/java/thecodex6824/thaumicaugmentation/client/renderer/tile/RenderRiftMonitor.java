/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sasmaster.glelwjgl.java.CoreGLE;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.biome.Biome;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.obj.AdvancedModelLoader;
import thaumcraft.client.lib.obj.IModelCustom;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.entity.IDimensionalFracture;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.api.world.BiomeMonitorColors;
import thecodex6824.thaumicaugmentation.api.world.BiomeMonitorColors.MonitorColors;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.common.tile.TileRiftMonitor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RenderRiftMonitor extends TileEntitySpecialRenderer<TileRiftMonitor> {

    protected static final Cache<Integer, FluxRiftReconstructor> RIFTS = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterAccess(5, TimeUnit.SECONDS).build();
  
    protected IModelCustom meter;
    
    public RenderRiftMonitor() {
        meter = AdvancedModelLoader.loadModel(new ResourceLocation("thaumcraft", "models/item/scanner.obj"));
    }
    
    @Override
    public void render(TileRiftMonitor te, double x, double y, double z, float partialTicks, int destroyStage,
            float alpha) {
        
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        Entity target = te.getTarget();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.pushMatrix();
        if (target != null) {
            GlStateManager.translate(0.5, 0.875 + Math.sin((te.getWorld().getTotalWorldTime() + partialTicks) / 40.0) / 15.0, 0.5);
            GlStateManager.scale(0.25, 0.25, 0.25);
            GlStateManager.rotate(te.getWorld().getTotalWorldTime() % 360 + partialTicks, 0.0F, 1.0F, 0.0F);
        }
        else {
            GlStateManager.translate(0.5, 0.875, 0.5);
            GlStateManager.scale(0.25, 0.25, 0.25);
        }
        bindTexture(TATextures.MONITOR_BASE_TEXTURE);
        meter.renderPart("scanner");
        bindTexture(TATextures.MONITOR_GLASS_TEXTURE);
        meter.renderOnly("screen", "screen_back");
        GlStateManager.popMatrix();
        GlStateManager.translate(0.0, 1.0, 0.0);
        GlStateManager.disableLighting();
        float r = 1.0F, g = 1.0F, b = 1.0F, a = 1.0F;
        if (!te.getMode()) {
            r = 0.5F;
            g = 0.5F;
            b = 1.0F;
            a = 1.0F;
        }
        else {
            r = 0.55F;
            g = 0.75F;
            b = 0.55F;
            a = 1.0F;
        }
        
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        GlStateManager.color(r, g, b, a);
        GlStateManager.disableCull();
        bindTexture(TATextures.SIDE);
        for (int i = 0; i < 4; ++i) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.rotate(90.0F * i, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5, -0.5, -0.5);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(0.0, 1.0, 0.0).tex(0.0, 1.0).endVertex();
            buffer.pos(1.0, 1.0, 0.0).tex(1.0, 1.0).endVertex();
            buffer.pos(1.0, 0.0, 0.0).tex(1.0, 0.0).endVertex();
            buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).endVertex();
            t.draw();
            GlStateManager.popMatrix();
        }
        
        bindTexture(TATextures.GRID);
        GlStateManager.color(r / 2.0F, g / 2.0F, b / 2.0F, a);
        GlStateManager.depthMask(false);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(0.0, 0.0, 1.0).tex(0.0, 16.0).endVertex();
        buffer.pos(1.0, 0.0, 1.0).tex(16.0, 16.0).endVertex();
        buffer.pos(1.0, 0.0, 0.0).tex(16.0, 0.0).endVertex();
        buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).endVertex();
        t.draw();
        GlStateManager.depthMask(true);
        GlStateManager.color(r, g, b, a);
        GlStateManager.enableCull();
        if (target instanceof EntityFluxRift && ((EntityFluxRift) target).getRiftSize() > 0) {
            FluxRiftReconstructor rift = null;
            try {
                rift = RIFTS.get(target.getEntityId(), () -> new FluxRiftReconstructor(((EntityFluxRift) target).getRiftSeed(), ((EntityFluxRift) target).getRiftSize()));
            }
            catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            }
   
            if (!te.getMode()) {
                GlStateManager.translate(0.5, 0.4375, 0.5);
                GlStateManager.scale(0.0625, 0.0625, 0.0625);
                for (int i = 0; i < 2; ++i) {
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(180.0F * i, 0.0F, 1.0F, 0.0F);
                    for (int offset = 0; offset < 2; ++offset) {
                        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
                        buffer.pos(4.0, -6.0 - offset, 5.0).tex(0.0, 0.0).endVertex();
                        buffer.pos(5.0, -6.0 - offset, 5.0).tex(1.0, 0.0).endVertex();
                        buffer.pos(4.0, -5.0 - offset, 5.0).tex(0.0, 1.0).endVertex();
                        buffer.pos(5.0, -5.0 - offset, 5.0).tex(1.0, 1.0).endVertex();
                        buffer.pos(5.0, -5.0 - offset, 4.0).tex(1.0, 0.0).endVertex();
                        buffer.pos(5.0, -6.0 - offset, 5.0).tex(0.0, 1.0).endVertex();
                        buffer.pos(5.0, -6.0 - offset, 4.0).tex(0.0, 0.0).endVertex();
                        buffer.pos(4.0, -6.0 - offset, 5.0).tex(1.0, 1.0).endVertex();
                        buffer.pos(4.0, -6.0 - offset, 4.0).tex(1.0, 0.0).endVertex();
                        buffer.pos(4.0, -5.0 - offset, 5.0).tex(0.0, 1.0).endVertex();
                        buffer.pos(4.0, -5.0 - offset, 4.0).tex(0.0, 0.0).endVertex();
                        buffer.pos(5.0, -5.0 - offset, 4.0).tex(1.0, 0.0).endVertex();
                        buffer.pos(4.0, -6.0 - offset, 4.0).tex(0.0, 1.0).endVertex();
                        buffer.pos(5.0, -6.0 - offset, 4.0).tex(1.0, 1.0).endVertex();
                        t.draw();
                    }
                    GlStateManager.popMatrix();
                }
            }
            else {
                rift = rift.onlyCenterPoints(Math.min(rift.getPoints().length, 24));
                AxisAlignedBB box = rift.getBoundingBox();
                GlStateManager.translate(0.5, 0.5, 0.5);
                double largestFactor = Math.max(Math.max(Math.abs(box.minX), Math.abs(box.maxX)),
                        Math.max(Math.max(Math.abs(box.minY), Math.abs(box.maxY)), Math.max(Math.abs(box.minZ), Math.abs(box.maxZ))));
                largestFactor = Math.max(largestFactor, Math.max(box.maxX - box.minX,
                        Math.max(box.maxY - box.minY, box.maxZ - box.minZ)) + 1.0);
                GlStateManager.scale(1.0 / largestFactor, 1.0 / largestFactor, 1.0 / largestFactor);
                GlStateManager.translate(-(box.maxX - Math.abs(box.minX)) / 2.0, -(box.maxY - Math.abs(box.minY)) / 2.0, -(box.maxZ - Math.abs(box.minZ)) / 2.0);
                GlStateManager.disableCull();
            }
            
            ThaumicAugmentation.proxy.getRenderHelper().renderFluxRiftSolidLayer(rift, (int) ((EntityFluxRift) target).getRiftStability(), partialTicks, 6, r, g, b, a, false, CoreGLE.TUBE_JN_ANGLE);
        
            if (te.getMode())
                GlStateManager.enableCull();
        }
        else if (target instanceof IDimensionalFracture) {
            if (!te.getMode()) {
                GlStateManager.translate(0.5, 0.05, 0.5);
                Biome biome = ((IDimensionalFracture) target).getDestinationBiome();
                if (biome != null) {
                    MonitorColors colors = BiomeMonitorColors.getMonitorColors(biome);
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.0625, 0.0625, 0.0625);
                    MutableBlockPos pos = new MutableBlockPos();
                    for (int cubeZ = -3; cubeZ < 4; ++cubeZ) {
                        for (int cubeX = -3; cubeX < 4; ++cubeX) {
                            pos.setPos(cubeX, (int) y, cubeZ);
                            int color = 0;
                            if (Math.abs(cubeX) < 2 && Math.abs(cubeZ) < 2)
                                color = (colors.getGrassColor() & 0xFF000000) != 0 ? biome.getGrassColorAtPos(pos) : colors.getGrassColor();
                            else if (Math.abs(cubeX) < 3 && Math.abs(cubeZ) < 3)
                                color = (colors.getPlantColor() & 0xFF000000) != 0 ? biome.getFoliageColorAtPos(pos) : colors.getPlantColor();
                            else
                                color = (colors.getWaterColor() & 0xFF000000) != 0 ? (0x3F76E4 & biome.getWaterColor()) : colors.getWaterColor();
                            
                            float cR = ((color >> 16) & 0xFF) / 255.0F, cG = ((color >> 8) & 0xFF) / 255.0F, cB = (color & 0xFF) / 255.0F;
                            GlStateManager.color(cR, cG, cB, a);
                            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
                            buffer.pos(cubeX - 0.5, 0.0, cubeZ + 0.5).tex(0.0, 0.0).endVertex();
                            buffer.pos(cubeX + 0.5, 0.0, cubeZ + 0.5).tex(1.0, 0.0).endVertex();
                            buffer.pos(cubeX - 0.5, 1.0, cubeZ + 0.5).tex(0.0, 1.0).endVertex();
                            buffer.pos(cubeX + 0.5, 1.0, cubeZ + 0.5).tex(1.0, 1.0).endVertex();
                            buffer.pos(cubeX + 0.5, 1.0, cubeZ - 0.5).tex(1.0, 0.0).endVertex();
                            buffer.pos(cubeX + 0.5, 0.0, cubeZ + 0.5).tex(0.0, 1.0).endVertex();
                            buffer.pos(cubeX + 0.5, 0.0, cubeZ - 0.5).tex(0.0, 0.0).endVertex();
                            buffer.pos(cubeX - 0.5, 0.0, cubeZ + 0.5).tex(1.0, 1.0).endVertex();
                            buffer.pos(cubeX - 0.5, 0.0, cubeZ - 0.5).tex(1.0, 0.0).endVertex();
                            buffer.pos(cubeX - 0.5, 1.0, cubeZ + 0.5).tex(0.0, 1.0).endVertex();
                            buffer.pos(cubeX - 0.5, 1.0, cubeZ - 0.5).tex(0.0, 0.0).endVertex();
                            buffer.pos(cubeX + 0.5, 1.0, cubeZ - 0.5).tex(1.0, 0.0).endVertex();
                            buffer.pos(cubeX - 0.5, 0.0, cubeZ - 0.5).tex(0.0, 1.0).endVertex();
                            buffer.pos(cubeX + 0.5, 0.0, cubeZ - 0.5).tex(1.0, 1.0).endVertex();
                            t.draw();
                        }
                    }
                    GlStateManager.popMatrix();
                }
                GlStateManager.translate(0.0, 0.1, 0.0);
                GlStateManager.scale(0.25, 0.25, 0.25);
            }
            else {
                AxisAlignedBB box = target.getEntityBoundingBox().offset(target.getPositionVector().scale(-1.0));
                GlStateManager.translate(0.5, 0.0, 0.5);
                double largestFactor = Math.max(Math.max(Math.abs(box.minX), Math.abs(box.maxX)),
                        Math.max(Math.max(Math.abs(box.minY), Math.abs(box.maxY)), Math.max(Math.abs(box.minZ), Math.abs(box.maxZ))));
                GlStateManager.scale(1.0 / largestFactor, 1.0 / largestFactor, 1.0 / largestFactor);
            }
            
            IDimensionalFracture frac = (IDimensionalFracture) target;
            ThaumicAugmentation.proxy.getRenderHelper().renderDimensionalFractureSolidLayer(frac.isOpen(), target.getEntityWorld().getTotalWorldTime(), frac.getTimeOpened(), frac.getOpeningDuration(),
                    partialTicks, 6, r, g, b, a, false, CoreGLE.TUBE_JN_ANGLE);
        }
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
    
}
