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

package thecodex6824.thaumicaugmentation.client.renderer.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.animation.FastTESR;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.common.block.BlockGlassTube;
import thecodex6824.thaumicaugmentation.common.block.BlockGlassTube.ConnectionType;
import thecodex6824.thaumicaugmentation.common.tile.TileGlassTube;

public class RenderGlassTube extends FastTESR<TileGlassTube> {

    protected void drawRectPrism(BufferBuilder buffer, TextureAtlasSprite texture, Vec3d p1, Vec3d p2,
            float r, float g, float b) {
        
        buffer.pos(p1.x, p1.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p1.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p1.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        buffer.pos(p1.x, p1.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        
        buffer.pos(p1.x, p1.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p1.x, p2.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p1.x, p2.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        buffer.pos(p1.x, p1.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        
        buffer.pos(p1.x, p2.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p2.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p2.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        buffer.pos(p1.x, p2.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        
        buffer.pos(p2.x, p2.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p1.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p1.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p2.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        
        buffer.pos(p1.x, p1.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p1.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p2.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        buffer.pos(p1.x, p2.y, p1.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        
        buffer.pos(p1.x, p1.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p1.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMinV()).lightmap(240, 240).endVertex();
        buffer.pos(p2.x, p2.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMaxU(), texture.getMaxV()).lightmap(240, 240).endVertex();
        buffer.pos(p1.x, p2.y, p2.z).color(r, g, b, 1.0F).tex(texture.getMinU(), texture.getMaxV()).lightmap(240, 240).endVertex();
    }
    
    @Override
    public void renderTileEntityFast(TileGlassTube te, double x, double y, double z, float partialTicks,
            int destroyStage, float partial, BufferBuilder buffer) {
        
        buffer.setTranslation(0.0, 0.0, 0.0);
        int fluid = te.getFluidStartTicks();
        if (fluid > 0) {
            Aspect aspect = te.getEssentiaType(EnumFacing.UP);
            if (aspect == null || te.getEssentiaAmount(EnumFacing.UP) == 0)
                aspect = te.getLastFluid();
            
            if (aspect != null) {
                float levelMod = fluid / 20.0F;
                int color = aspect.getColor();
                float r = ((color >> 16) & 0xFF) / 255.0F;
                float g = ((color >> 8) & 0xFF) / 255.0F;
                float b = (color & 0xFF) / 255.0F;
                TextureAtlasSprite texture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TATextures.ESSENTIA.toString());
                IBlockState state = te.getWorld().getBlockState(te.getPos());
                state = state.getActualState(te.getWorld(), te.getPos());
                boolean sNeg = state.getValue(BlockGlassTube.NORTH) != ConnectionType.NONE,
                        sPos = state.getValue(BlockGlassTube.SOUTH) != ConnectionType.NONE;
                if (sNeg || sPos) {
                    Vec3d p1 = new Vec3d(x + 0.45, y + 0.45, z + (sNeg ? 0.0 : 0.45));
                    Vec3d p2 = new Vec3d(x + 0.55, y + 0.45 + 0.1 * levelMod, z + (sPos ? 1.0 : 0.55));
                    drawRectPrism(buffer, texture, p1, p2, r, g, b);
                }
                
                sNeg = state.getValue(BlockGlassTube.WEST) != ConnectionType.NONE;
                sPos = state.getValue(BlockGlassTube.EAST) != ConnectionType.NONE;
                if (sNeg || sPos) {
                    Vec3d p1 = new Vec3d(x + (sNeg ? 0.0 : 0.45), y + 0.45, z + 0.45);
                    Vec3d p2 = new Vec3d(x + (sPos ? 1.0 : 0.55), y + 0.45 + 0.1 * levelMod, z + 0.55);
                    drawRectPrism(buffer, texture, p1, p2, r, g, b);
                }
                
                sNeg = state.getValue(BlockGlassTube.DOWN) != ConnectionType.NONE;
                sPos = state.getValue(BlockGlassTube.UP) != ConnectionType.NONE;
                if (sNeg || sPos) {
                    double width = 0.1 * levelMod;
                    Vec3d p1 = new Vec3d(x + 0.45 + (0.1 - width), y + (sNeg ? 0.0 : 0.45), z + 0.45 + (0.1 - width));
                    Vec3d p2 = new Vec3d(x + 0.45 + width, y + (sPos ? 1.0 : 0.55), z + 0.45 + width);
                    drawRectPrism(buffer, texture, p1, p2, r, g, b);
                }
            }
        }
    }
   
}
