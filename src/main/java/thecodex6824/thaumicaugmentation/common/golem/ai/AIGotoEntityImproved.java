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

package thecodex6824.thaumicaugmentation.common.golem.ai;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.golems.tasks.TaskHandler;

import java.util.ArrayList;

public class AIGotoEntityImproved extends AIGotoImproved {

    public AIGotoEntityImproved(EntityThaumcraftGolem golem, double reachDist, double moveSpeed) {
        super(golem, reachDist, moveSpeed);
    }
    
    protected boolean canGolemReach(Entity other) {
        double correctedReach = 4.0 + (other.width / 2.0) * (other.width / 2.0);
        if (entity.getDistanceSq(other) < correctedReach)
            return true;
        else {
            Path path = entity.getNavigator().getPathToEntityLiving(other);
            if (path == null)
                return false;
            
            PathPoint dest = path.getFinalPathPoint();
            if (dest == null)
                return false;

            return other.getDistanceSq(dest.x + 0.5, dest.y + 0.5, dest.z + 0.5) < correctedReach;
        }
    }
    
    @Override
    protected boolean getTask() {
        ArrayList<Task> entityTasks = TaskHandler.getEntityTasksSorted(entity.dimension,
                entity.getUniqueID(), entity);
        for (Task task : entityTasks) {
            if (isGolemValidForTask(task) && task.canGolemPerformTask(entity) &&
                    entity.isWithinHomeDistanceFromPosition(task.getEntity().getPosition())) {

                boolean reach = canGolemReach(task.getEntity());
                if (!reach)
                    reach = acceptOrDeferTask(task);

                if (reach) {
                    entity.setTask(task);
                    entity.getTask().setReserved(true);
                    reachDistSq = 4.0 + (task.getEntity().width / 2.0) * (task.getEntity().width / 2.0);
                    if (ModConfig.CONFIG_GRAPHICS.showGolemEmotes)
                        entity.getGolemWorld().setEntityState(entity, (byte) 5);

                    return true;
                }
            }
        }
        
        return false;
    }
    
}
