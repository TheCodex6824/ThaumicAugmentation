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

package thecodex6824.thaumicaugmentation.common.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.common.util.IShaderRenderingCallback;
import thecodex6824.thaumicaugmentation.common.util.ShaderType;

public class TileStarfieldGlass extends TileEntity implements IShaderRenderingCallback {
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public double getMaxRenderDistanceSquared() {
        int dist = TAConfig.bulkRenderDistance.getValue();
        return dist * dist;
    }
    
    @Override
    public void renderWithShader(ShaderType type, double pX, double pY, double pZ) {
        ThaumicAugmentation.proxy.getRenderHelper().renderStarfieldGlass(type, this, pX, pY, pZ);
    }
    
    @Override
    public boolean hasFastRenderer() {
        return true;
    }
    
}
