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

package thecodex6824.thaumicaugmentation.common.golem.ai;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.golems.seals.SealHandler;
import thaumcraft.common.golems.tasks.TaskHandler;
import thecodex6824.thaumicaugmentation.common.golem.SealAttack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class AIGotoImproved extends EntityAIBase {

    protected EntityThaumcraftGolem entity;
    protected double reachDistSq;
    protected double defaultReachDistSq;
    protected int moveCooldown;
    protected int taskTime;
    protected BlockPos lastTargetPosition;
    protected IntOpenHashSet deferredTasks;
    protected boolean requiresPathUpdates;
    
    public AIGotoImproved(EntityThaumcraftGolem golem, double reachDist) {
        entity = golem;
        reachDistSq = reachDist * reachDist;
        defaultReachDistSq = reachDistSq;
        moveCooldown = golem.getRNG().nextInt(5) + 1;
        taskTime = golem.getRNG().nextInt(5);
        deferredTasks = new IntOpenHashSet();
        setMutexBits(1);
    }
    
    protected abstract boolean getTask();

    protected boolean acceptOrDeferTask(Task task) {
        if (deferredTasks.add(task.getId()))
            return false;

        deferredTasks.rem(task.getId());
        requiresPathUpdates = true;
        return true;
    }
    
    protected boolean isGolemValidForTask(Task task) {
        ISealEntity seal = SealHandler.getSealEntity(entity.dimension, task.getSealPos());
        if (seal != null && seal.getSeal() != null) {
            if (seal.isLocked() && !entity.getOwnerId().equals(UUID.fromString(seal.getOwner())))
                return false;
            
            Set<EnumGolemTrait> golemTraits = entity.getProperties().getTraits();
            List<EnumGolemTrait> sealReq = seal.getSeal().getRequiredTags() != null ?
                    Arrays.asList(seal.getSeal().getRequiredTags()) : ImmutableList.of();
            List<EnumGolemTrait> sealForbidden = seal.getSeal().getForbiddenTags() != null ?
                    Arrays.asList(seal.getSeal().getForbiddenTags()) : ImmutableList.of();
                    
            if (!golemTraits.containsAll(sealReq))
                return false;
            else if (sealForbidden.stream().anyMatch(golemTraits::contains))
                return false;
        }
        
        return true;
    }
    
    protected int getGolemCooldownDuration() {
        if (entity.getProperties().hasTrait(EnumGolemTrait.SMART))
            return 2;
        else
            return 4;
    }
    
    @Override
    public boolean shouldExecute() {
        if (--moveCooldown == 0) {
            moveCooldown = getGolemCooldownDuration();
            if (entity.getTask() != null && !entity.getTask().isSuspended())
                return false;
            
            boolean validTask = getTask();
            if (validTask && entity.getTask() != null && entity.getTask().getSealPos() != null) {
                ISealEntity seal = GolemHelper.getSealEntity(entity.dimension, entity.getTask().getSealPos());
                if (seal != null)
                    seal.getSeal().onTaskStarted(entity.getGolemWorld(), entity, entity.getTask());
                
                if (entity.getTask().getType() == 1) {
                    entity.getNavigator().tryMoveToEntityLiving(entity.getTask().getEntity(),
                            entity.getGolemMoveSpeed());
                    lastTargetPosition = entity.getTask().getEntity().getPosition().toImmutable();
                }
                else {
                    BlockPos target = entity.getTask().getPos();
                    entity.getNavigator().tryMoveToXYZ(target.getX(), target.getY(), target.getZ(),
                            entity.getGolemMoveSpeed());
                    lastTargetPosition = target.toImmutable();
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void startExecuting() {
        taskTime = 0;
        deferredTasks.clear();
    }
    
    @Override
    public boolean shouldContinueExecuting() {
        return entity.getTask() != null && !entity.getTask().isSuspended() &&
                !entity.getTask().isCompleted() && taskTime < 1200;
    }

    @Override
    public void updateTask() {
        if (entity.getTask() != null) {
            double distSq = entity.getTask().getType() == 1 ? entity.getDistanceSq(entity.getTask().getEntity()) :
                    entity.getDistanceSq(entity.getTask().getPos());
            if (distSq < reachDistSq) {
                TaskHandler.completeTask(entity.getTask(), entity);
                if (entity.getTask().isCompleted()) {
                    taskTime = 0;
                    return;
                }
            }
            else if (entity.getTask().getType() == 1 && !entity.getTask().getEntity().isEntityAlive()) {
                entity.getTask().setSuspended(true);
                taskTime = 0;
                return;
            }

            entity.getTask().setCompletion(false);
            if (++taskTime % getGolemCooldownDuration() == 0) {
                if (entity.getTask().getType() == 1 &&
                        (requiresPathUpdates || entity.getTask().getEntity().getDistanceSq(lastTargetPosition) > reachDistSq)) {
                    entity.getNavigator().tryMoveToEntityLiving(entity.getTask().getEntity(),
                            entity.getGolemMoveSpeed());
                    lastTargetPosition = entity.getTask().getEntity().getPosition().toImmutable();
                }
                else if (requiresPathUpdates || entity.getTask().getPos().distanceSq(lastTargetPosition) > reachDistSq) {
                    BlockPos target = entity.getTask().getPos();
                    entity.getNavigator().tryMoveToXYZ(target.getX(), target.getY(), target.getZ(),
                            entity.getGolemMoveSpeed());
                    lastTargetPosition = target.toImmutable();
                }
            }
        }
    }
    
    @Override
    public void resetTask() {
        Task task = entity.getTask();
        if (task != null) {
            if (!task.isCompleted() && task.isReserved() &&
                    ModConfig.CONFIG_GRAPHICS.showGolemEmotes) {

                ISealEntity seal = SealHandler.getSealEntity(entity.world.provider.getDimension(), task.getSealPos());
                if (seal == null || !(seal.getSeal() instanceof SealAttack))
                    entity.getGolemWorld().setEntityState(entity, (byte) 6);
            }
            else if (task.isCompleted() && !task.isSuspended())
                task.setSuspended(true);
            
            task.setReserved(false);
        }

        entity.setTask(null);
        entity.getNavigator().clearPath();
        reachDistSq = defaultReachDistSq;
        requiresPathUpdates = false;
    }

}
