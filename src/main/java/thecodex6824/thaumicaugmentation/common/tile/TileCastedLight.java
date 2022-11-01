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
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;

import java.util.concurrent.ThreadLocalRandom;

public class TileCastedLight extends TileEntity implements ITickable {

    protected boolean lastRenderState;
    protected int ticks;

    public TileCastedLight() {
        super();
        lastRenderState = TAConfig.reducedEffects.getValue();
        ticks = ThreadLocalRandom.current().nextInt(20);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void update() {
        if (world.isRemote && ticks++ % 5 == 0) {
            if (lastRenderState != TAConfig.reducedEffects.getValue()) {
                lastRenderState = !lastRenderState;
                world.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
            }
            if (!lastRenderState) {
                ThaumicAugmentation.proxy.getRenderHelper().renderGlowingSphere(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 
                        Aspect.LIGHT.getColor());
            }
        }
    }

}
