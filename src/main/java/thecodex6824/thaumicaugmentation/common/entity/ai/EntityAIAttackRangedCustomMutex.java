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

package thecodex6824.thaumicaugmentation.common.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;

public class EntityAIAttackRangedCustomMutex<T extends EntityLiving & IRangedAttackMob> extends EntityAIBase {

    protected T entity;
    protected EntityLivingBase target;
    protected int rangedAttackTime;
    protected int seeTime;
    protected int attackIntervalMin;
    protected int maxRangedAttackTime;
    protected float attackRadius;
    protected float maxAttackDistance;

    public EntityAIAttackRangedCustomMutex(T attacker, int maxAttackTime, float maxAttackDistanceIn, int mutex) {
        this(attacker, maxAttackTime, maxAttackTime, maxAttackDistanceIn, mutex);
    }

    public EntityAIAttackRangedCustomMutex(T attacker, int intervalMin, int maxAttackTime, float maxAttackDistanceIn, int mutex) {
        entity = attacker;
        rangedAttackTime = -1;
        attackIntervalMin = intervalMin;
        maxRangedAttackTime = maxAttackTime;
        attackRadius = maxAttackDistanceIn;
        maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
        setMutexBits(mutex);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase check = entity.getAttackTarget();
        if (check == null)
            return false;
        else {
            target = check;
            return true;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }

    @Override
    public void resetTask() {
        target = null;
        seeTime = 0;
        rangedAttackTime = -1;
    }

    @Override
    public void updateTask() {
        double dist = entity.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
        boolean sight = entity.getEntitySenses().canSee(target);

        if (sight)
            ++seeTime;
        else
            seeTime = 0;

        if (--rangedAttackTime == 0) {
            if (sight) {
                float ratio = MathHelper.sqrt(dist) / this.attackRadius;
                float clampedRatio = MathHelper.clamp(ratio, 0.1F, 1.0F);
                entity.attackEntityWithRangedAttack(target, clampedRatio);
                rangedAttackTime = MathHelper.floor(ratio * (maxRangedAttackTime - attackIntervalMin) + attackIntervalMin);
            }
        }
        else if (rangedAttackTime < 0) {
            float ratio = MathHelper.sqrt(dist) / this.attackRadius;
            rangedAttackTime = MathHelper.floor(ratio * (maxRangedAttackTime - attackIntervalMin) + attackIntervalMin);
        }
    }
    
}
