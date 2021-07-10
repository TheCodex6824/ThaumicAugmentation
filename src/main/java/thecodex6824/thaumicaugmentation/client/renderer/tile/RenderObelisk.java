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

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.animation.FastTESR;
import thecodex6824.thaumicaugmentation.client.event.RenderEventHandler;
import thecodex6824.thaumicaugmentation.common.tile.TileObelisk;
import thecodex6824.thaumicaugmentation.common.util.ShaderType;

public class RenderObelisk extends FastTESR<TileObelisk> {
    
    @Override
    public void renderTileEntityFast(TileObelisk te, double x, double y, double z, float partialTicks, int destroyStage,
            float partial, BufferBuilder buffer) {
        
        BlockPos pos = te.getPos();
        IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
        IBlockState state = world.getBlockState(pos).getActualState(world, pos);
        IBakedModel blockModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
        state = state.getBlock().getExtendedState(state, world, pos);
        double oldX = buffer.xOffset, oldY = buffer.yOffset, oldZ = buffer.zOffset;
        double offset = Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().isSingleplayer() ? 0.0 : partialTicks;
        buffer.setTranslation(x - pos.getX(), y - pos.getY() + Math.sin((Minecraft.getMinecraft().player.ticksExisted + offset) / 20.0) / 4.0,
                z - pos.getZ());
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world,
                blockModel, state, pos, buffer, true);
        buffer.setTranslation(oldX, oldY, oldZ);
        
        // safe as it just queues some rendering for later
        RenderEventHandler.onRenderShaderTile(ShaderType.MIRROR, te);
    }
    
}
