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

package thecodex6824.thaumicaugmentation.common.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;

import javax.annotation.Nullable;

public abstract class EntityAITargetAnyLiving extends EntityAIBase {

    protected final EntityLiving entity;
    protected boolean sight;
    protected int targetUnseenTicks;
    protected EntityLivingBase target;
    protected int unseenMemoryTicks;

    public EntityAITargetAnyLiving(EntityLiving living, boolean checkSight) {
        entity = living;
        sight = checkSight;
    }
    
    protected boolean isSuitableTarget(@Nullable EntityLivingBase check, boolean includeInvincibles) {
        if (check == null || check == entity || !check.isEntityAlive())
            return false;
        else if (!entity.canAttackClass(check.getClass()) || entity.isOnSameTeam(check))
            return false;
        else {
            if (entity instanceof IEntityOwnable && ((IEntityOwnable) entity).getOwnerId() != null) {
                IEntityOwnable ownable = (IEntityOwnable) entity;
                if (check instanceof IEntityOwnable && ((IEntityOwnable) check).getOwnerId().equals(ownable.getOwnerId()))
                    return false;

                if (check == ownable.getOwner())
                    return false;
            }
            else if (check instanceof EntityPlayer && !includeInvincibles && ((EntityPlayer) check).capabilities.disableDamage)
                return false;
            
            return !sight || entity.getEntitySenses().canSee(check);
        }
    }

    @Override
    public void startExecuting() {
        unseenMemoryTicks = 300;
        targetUnseenTicks = 0;
    }
    
    protected double getTargetDistance() {
        IAttributeInstance attribute = entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        return attribute == null ? 16.0 : attribute.getAttributeValue();
    }
    
    @Override
    public boolean shouldContinueExecuting() {
        EntityLivingBase t = entity.getAttackTarget();
        if (t == null)
            t = target;

        if (t == null)
            return false;
        else if (!t.isEntityAlive())
            return false;
        else {
            Team myTeam = entity.getTeam();
            Team theirTeam = t.getTeam();

            if (myTeam != null && myTeam == theirTeam)
                return false;
            else {
                double dist = getTargetDistance();
                if (entity.getDistanceSq(t) > dist * dist)
                    return false;
                else {
                    if (sight) {
                        if (entity.getEntitySenses().canSee(t))
                            targetUnseenTicks = 0;
                        else if (++targetUnseenTicks > unseenMemoryTicks)
                            return false;
                    }

                    if (t instanceof EntityPlayer && ((EntityPlayer) t).capabilities.disableDamage)
                        return false;
                    else {
                        entity.setAttackTarget(t);
                        return true;
                    }
                }
            }
        }
    }
    
    @Override
    public void resetTask() {
        entity.setAttackTarget(null);
        target = null;
    }
    
}
