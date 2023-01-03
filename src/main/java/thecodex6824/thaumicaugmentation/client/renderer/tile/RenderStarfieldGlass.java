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

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.FastTESR;
import thecodex6824.thaumicaugmentation.api.block.property.IStarfieldGlassType;
import thecodex6824.thaumicaugmentation.client.event.RenderEventHandler;
import thecodex6824.thaumicaugmentation.common.tile.TileStarfieldGlass;

public class RenderStarfieldGlass extends FastTESR<TileStarfieldGlass> {
    
    @Override
    public void renderTileEntityFast(TileStarfieldGlass te, double x, double y, double z, float partialTicks,
            int destroyStage, float partial, BufferBuilder buffer) {
        
        World world = te.getWorld();
        IBlockState state = world.getBlockState(te.getPos());
        if (state.getPropertyKeys().contains(IStarfieldGlassType.GLASS_TYPE))
            RenderEventHandler.onRenderShaderTile(state.getValue(IStarfieldGlassType.GLASS_TYPE).getShaderType(), te);
    }
    
}
