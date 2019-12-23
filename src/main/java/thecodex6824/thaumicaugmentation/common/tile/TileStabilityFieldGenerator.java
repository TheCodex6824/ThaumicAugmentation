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

package thecodex6824.thaumicaugmentation.common.tile;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import thaumcraft.common.entities.EntityFluxRift;

public class TileStabilityFieldGenerator extends TileEntity {

    public TileStabilityFieldGenerator() {
        
    }
    
    protected double getDistForFace(EnumFacing face, Entity entity) {
        return getDistForFace(face, entity.getPositionVector());
    }
    
    protected double getDistForFace(EnumFacing face, Vec3d vec) {
        if (face.getAxis() == Axis.X)
            return Math.abs(pos.getX() - vec.x);
        else if (face.getAxis() == Axis.Y)
            return Math.abs(pos.getY() - vec.y);
        else
            return Math.abs(pos.getZ() - vec.z);
    }
    
    @Nullable
    protected EntityFluxRift findClosestRift(EnumFacing face) {
        BlockPos pos1 = pos.offset(face).add(1.0 - face.getXOffset(), 1.0 - face.getYOffset(), 1.0 - face.getZOffset());
        BlockPos pos2 = pos.offset(face, 10).add(1.0 + face.getXOffset(), 1.0 + face.getYOffset(), 1.0 + face.getZOffset());
        List<EntityFluxRift> rifts = world.getEntitiesWithinAABB(EntityFluxRift.class, 
                new AxisAlignedBB(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX() + 1, pos2.getY() + 1, pos2.getZ() + 1));
        if (!rifts.isEmpty()) {
            rifts.sort((rift1, rift2) -> Double.compare(getDistForFace(face, rift1), getDistForFace(face, rift2)));
            RayTraceResult trace = world.rayTraceBlocks(new Vec3d(pos.offset(face)), new Vec3d(pos.offset(face, 10)));
            EntityFluxRift chosenOne = rifts.get(0);
            if (trace == null || trace.hitVec == null || getDistForFace(face, chosenOne) < getDistForFace(face, trace.hitVec))
                return chosenOne;
        }
        
        return null;
    }
}
