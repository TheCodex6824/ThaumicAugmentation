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

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.FastTESR;
import thecodex6824.thaumicaugmentation.client.model.MiscModels;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusGate;

public class RenderImpetusGate extends FastTESR<TileImpetusGate> {

    protected static BlockModelRenderer renderer;
    
    @Override
    public void renderTileEntityFast(TileImpetusGate te, double x, double y, double z, float partialTicks,
            int destroyStage, float partial, BufferBuilder buffer) {
        
        BlockPos pos = te.getPos();
        if (te.getWorld().getRedstonePowerFromNeighbors(pos) > 0) {
            if (renderer == null)
                renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
            
            double oldX = buffer.xOffset, oldY = buffer.yOffset, oldZ = buffer.zOffset;
            buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
            renderer.renderModel(te.getWorld(), MiscModels.getImpetusGateShieldModel(), te.getWorld().getBlockState(pos),
                    pos, buffer, false, ThreadLocalRandom.current().nextLong());
            buffer.setTranslation(oldX, oldY, oldZ);
        }
    }
    
}
