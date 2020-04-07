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

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class EntityAIFlyWander extends EntityAIBase {

    protected EntityFlying entity;
    protected BlockPos dest;
    protected float speed;
    
    public EntityAIFlyWander(EntityFlying flying, float flySpeed) {
        entity = flying;
        setMutexBits(1);
        speed = flySpeed;
    }
    
    @Override
    public boolean shouldExecute() {
        return entity.getAttackTarget() == null;
    }
    
    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }
    
    @Override
    public void startExecuting() {
        Random rand = entity.getRNG();
        boolean ground = false;
        for (int i = 1; i < 6; ++i) {
            IBlockState below = entity.getEntityWorld().getBlockState(entity.getPosition().down(i));
            AxisAlignedBB box = below.getCollisionBoundingBox(entity.getEntityWorld(), dest);
            if (box != null && box != Block.NULL_AABB) {
                ground = true;
                break;
            }
        }
        dest = entity.getPosition().add(rand.nextInt(7) - rand.nextInt(7), ground ? rand.nextInt(5) - 2 : rand.nextInt(5) - 3,
                rand.nextInt(7) - rand.nextInt(7));
    }
    
    @Override
    public void updateTask() {
        boolean newPos = false;
        if (!entity.getEntityWorld().isBlockLoaded(dest))
            newPos = true;
        
        if (!newPos) {
            IBlockState destState = entity.getEntityWorld().getBlockState(dest);
            AxisAlignedBB box = destState.getCollisionBoundingBox(entity.getEntityWorld(), dest);
            if (box != null && box != Block.NULL_AABB || entity.getEntityWorld().isOutsideBuildHeight(dest))
                newPos = true;
        }
        
        if (newPos || entity.getDistanceSqToCenter(dest) < 4.0F || entity.getRNG().nextInt(30) == 0) {
            Random rand = entity.getRNG();
            boolean ground = false;
            for (int i = 1; i < 6; ++i) {
                IBlockState below = entity.getEntityWorld().getBlockState(entity.getPosition().down(i));
                AxisAlignedBB box = below.getCollisionBoundingBox(entity.getEntityWorld(), dest);
                if (box != null && box != Block.NULL_AABB) {
                    ground = true;
                    break;
                }
            }
            dest = entity.getPosition().add(rand.nextInt(7) - rand.nextInt(7), ground ? rand.nextInt(5) - 2 : rand.nextInt(5) - 3,
                    rand.nextInt(7) - rand.nextInt(7));
        }
        
        double x = dest.getX() + 0.5 - entity.posX;
        double y = dest.getY() + 0.1 - entity.posY;
        double z = dest.getZ() + 0.5 - entity.posZ;
        entity.motionX += (Math.signum(x) * 0.5 - entity.motionX) * speed;
        entity.motionY += (Math.signum(y) * 0.7 - entity.motionY) * speed;
        entity.motionZ += (Math.signum(z) * 0.5 - entity.motionZ) * speed;
        entity.velocityChanged = true;
        float yaw = (float) (Math.toDegrees(Math.atan2(entity.motionZ, entity.motionX))) - 90.0F;
        entity.rotationYaw += MathHelper.wrapDegrees(yaw - entity.rotationYaw);
        entity.moveForward = 0.15F;
    }
    
}
