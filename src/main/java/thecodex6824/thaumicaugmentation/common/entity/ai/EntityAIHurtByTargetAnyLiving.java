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

package thecodex6824.thaumicaugmentation.common.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

public class EntityAIHurtByTargetAnyLiving extends EntityAITargetAnyLiving {

    protected int revengeTimerOld;

    public EntityAIHurtByTargetAnyLiving(EntityLiving living, boolean checkSight) {
        super(living, checkSight);
    }

    @Override
    public boolean shouldExecute() {
        int timer = entity.getRevengeTimer();
        EntityLivingBase revenge = entity.getRevengeTarget();
        return timer != revengeTimerOld && revenge != null && isSuitableTarget(revenge, false);
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        entity.setAttackTarget(entity.getRevengeTarget());
        target = entity.getAttackTarget();
        revengeTimerOld = entity.getRevengeTimer();
    }
    
}
