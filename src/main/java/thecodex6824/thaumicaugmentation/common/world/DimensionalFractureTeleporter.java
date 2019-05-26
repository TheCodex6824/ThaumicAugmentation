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

package thecodex6824.thaumicaugmentation.common.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class DimensionalFractureTeleporter implements ITeleporter {

    protected BlockPos pos;

    public DimensionalFractureTeleporter(BlockPos destination) {
        pos = destination;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        // we need to set the blockpos ourselves because the teleport code
        // sets it equal to the scaled position, while the fracture is at a
        // scaled *chunk* position with an offset
        if (entity instanceof EntityPlayerMP) {
            ((EntityPlayerMP) entity).connection.setPlayerLocation(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 
                    yaw, entity.rotationPitch);
        }
        else {
            entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 
                    yaw, entity.rotationPitch);
        }
    }

}
