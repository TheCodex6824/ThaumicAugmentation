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

import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import thaumcraft.common.golems.EntityThaumcraftGolem;

public class AIGotoHomeIfHurt extends AIGotoHomeImproved {

    public AIGotoHomeIfHurt(EntityThaumcraftGolem golem, double moveSpeed) {
        super(golem, moveSpeed);
    }
    
    @Override
    public boolean shouldExecute() {
        if (entity.getHealth() >= entity.getMaxHealth() * 0.33F)
            return false;

        double currentHomeDist = entity.getDistanceSqToCenter(entity.getHomePosition());
        if (currentHomeDist < 1.25)
            return false;
        else if (currentHomeDist > 1024.0) {
            Vec3d targetPos = RandomPositionGenerator.findRandomTargetBlockTowards(entity, 16, 7,
                    new Vec3d(entity.getHomePosition()));
            if (targetPos == null)
                return false;

            target = new BlockPos(targetPos);
        }
        else
            target = entity.getHomePosition();

        return true;
    }

    @Override
    public void updateTask() {
        if (entity.getDistanceSqToCenter(target) <= 1.25)
            entity.getNavigator().clearPath();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return entity.getHealth() < entity.getMaxHealth() * 0.75F;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }
    
}
