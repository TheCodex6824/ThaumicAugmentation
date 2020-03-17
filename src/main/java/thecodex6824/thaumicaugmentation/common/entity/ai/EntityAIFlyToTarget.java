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

import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;

public class EntityAIFlyToTarget extends EntityAIBase {

    protected EntityFlying entity;
    protected float speed;
    protected boolean rotate;
    
    public EntityAIFlyToTarget(EntityFlying flying, float flySpeed, boolean updateRotation) {
        entity = flying;
        setMutexBits(1);
        speed = flySpeed;
        updateRotation = rotate;
    }
    
    @Override
    public boolean shouldExecute() {
        return entity.getAttackTarget() != null && !entity.getAttackTarget().isDead;
    }
    
    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }
    
    @Override
    public void updateTask() {
        EntityLivingBase target = entity.getAttackTarget();
        double x = target.posX - entity.posX;
        double y = target.posY + target.getEyeHeight() - entity.posY;
        double z = target.posZ - entity.posZ;
        entity.motionX += (Math.signum(x) * 0.5 - entity.motionX) * speed;
        entity.motionY += (Math.signum(y) * 0.7 - entity.motionY) * speed;
        entity.motionZ += (Math.signum(z) * 0.5 - entity.motionZ) * speed;
        entity.moveForward = 0.15F;
        if (rotate) {
            float yaw = (float) (Math.toDegrees(Math.atan2(entity.motionZ, entity.motionX))) - 90.0F;
            entity.rotationYaw += MathHelper.wrapDegrees(yaw - entity.rotationYaw);
        }
    }
    
}
