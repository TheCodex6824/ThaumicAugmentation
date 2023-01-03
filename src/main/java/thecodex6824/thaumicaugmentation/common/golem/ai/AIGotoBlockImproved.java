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

package thecodex6824.thaumicaugmentation.common.golem.ai;

import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.golems.tasks.TaskHandler;

import java.util.ArrayList;

public class AIGotoBlockImproved extends AIGotoImproved {

    public AIGotoBlockImproved(EntityThaumcraftGolem golem, double reachDist, double moveSpeed) {
        super(golem, reachDist, moveSpeed);
    }
    
    protected boolean canGolemReach(BlockPos pos) {
        if (entity.getDistanceSq(pos) < reach * reach)
            return true;
        else {
            Path path = entity.getNavigator().getPathToXYZ(pos.getX(), pos.getY(), pos.getZ());
            if (path == null)
                return false;
            
            PathPoint dest = path.getFinalPathPoint();
            if (dest == null)
                return false;
            
            return new Vec3d(dest.x, dest.y, dest.z).squareDistanceTo(new Vec3d(pos)) < reach * reach;
        }
    }
    
    @Override
    protected boolean getTask() {
        ArrayList<Task> blockTasks = TaskHandler.getBlockTasksSorted(entity.dimension,
                entity.getUniqueID(), entity);
        for (Task task : blockTasks) {
            if (isGolemValidForTask(task) && task.canGolemPerformTask(entity) &&
                    entity.isWithinHomeDistanceFromPosition(task.getPos())) {

                boolean reach = canGolemReach(task.getPos());
                if (!reach)
                    reach = acceptOrDeferTask(task);

                if (reach) {
                    entity.setTask(task);
                    entity.getTask().setReserved(true);
                    if (ModConfig.CONFIG_GRAPHICS.showGolemEmotes)
                        entity.getGolemWorld().setEntityState(entity, (byte) 5);

                    return true;
                }
            }
        }
        
        return false;
    }
    
}
