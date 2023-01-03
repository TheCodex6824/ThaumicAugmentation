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

package thecodex6824.thaumicaugmentation.common.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskPart;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskPart.ObeliskPart;

public class TileObeliskVisual extends TileEntity {
    
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
    public boolean hasFastRenderer() {
        return true;
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos).grow(0.0, 1.0, 0.0);
    }
    
    @Override
    public boolean shouldRenderInPass(int pass) {
        IBlockState state = world.getBlockState(pos);
        if (state.getPropertyKeys().contains(IObeliskPart.OBELISK_PART) &&
                state.getValue(IObeliskPart.OBELISK_PART) != ObeliskPart.CAP)
            return pass == 1;
        else
            return pass == 0;
    }
    
}
