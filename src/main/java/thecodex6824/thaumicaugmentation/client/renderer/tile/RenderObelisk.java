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

package thecodex6824.thaumicaugmentation.client.renderer.tile;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import thecodex6824.thaumicaugmentation.client.event.RenderEventHandler;
import thecodex6824.thaumicaugmentation.common.tile.TileObelisk;
import thecodex6824.thaumicaugmentation.common.util.ShaderType;

public class RenderObelisk extends TileEntitySpecialRenderer<TileObelisk> {
    
    @Override
    public void render(TileObelisk te, double x, double y, double z, float partialTicks, int destroyStage,
            float alpha) {
        
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        BlockPos pos = te.getPos();
        IBlockAccess world = te.getWorld();
        IBlockState state = te.getWorld().getBlockState(pos).getActualState(world, pos);
        IBakedModel blockModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
        state = state.getBlock().getExtendedState(state, world, pos);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        double oldX = buffer.xOffset, oldY = buffer.yOffset, oldZ = buffer.zOffset;
        double offset = Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().isSingleplayer() ? 0.0 : partialTicks;
        buffer.setTranslation(x - pos.getX(), y - pos.getY() + Math.sin((Minecraft.getMinecraft().player.ticksExisted + offset) / 20.0) / 4.0,
                z - pos.getZ());
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world,
                blockModel, state, pos, buffer, true);
        t.draw();
        buffer.setTranslation(oldX, oldY, oldZ);
        RenderHelper.enableStandardItemLighting();
        RenderEventHandler.onRenderShaderTile(ShaderType.RIFT, te);
    }
    
}
