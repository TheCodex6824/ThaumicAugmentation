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

package thecodex6824.thaumicaugmentation.common.golem;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.parts.GolemHead;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.golems.ai.AIFollowOwner;
import thaumcraft.common.golems.ai.AIOwnerHurtByTarget;
import thaumcraft.common.golems.ai.AIOwnerHurtTarget;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.golem.IGolemAttributeUpdateReceiver;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.common.capability.ResizableImpetusStorage;
import thecodex6824.thaumicaugmentation.common.golem.ai.*;

public class GolemHeadAwakened implements GolemHead.IHeadFunction, IGolemAttributeUpdateReceiver {

    protected static final ResourceLocation STORAGE_KEY = new ResourceLocation(ThaumicAugmentationAPI.MODID, "golem_head_awakened");
    protected static final double SPEED_MOD_NORMAL = 1.0;
    protected static final double SPEED_MOD_NO_POWER = 0.25;
    protected static final int ENERGY_TIME = 200;

    protected boolean power;
    protected int ticks = ENERGY_TIME;

    @Override
    public void onUpdateTick(IGolemAPI golemApi) {
        EntityLivingBase golem = golemApi.getGolemEntity();
        if (golem instanceof EntityThaumcraftGolem) {
            IImpetusStorage storage = golem.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
            if (storage != null) {
                if (ticks > 0) {
                    --ticks;
                    if (ticks == 0) {
                        if (storage.extractEnergy(1, false) == 1) {
                            power = true;
                            ticks = ENERGY_TIME;
                        }
                        else {
                            power = false;
                            updateAttributes((EntityThaumcraftGolem) golem, SPEED_MOD_NO_POWER);
                        }
                    }
                }
                else if (storage.extractEnergy(1, false) == 1) {
                    power = true;
                    ticks = 100;
                    updateAttributes((EntityThaumcraftGolem) golem, SPEED_MOD_NORMAL);
                }
            }
        }
    }

    protected void updateAttributes(EntityThaumcraftGolem golem, double speedMod) {
        golem.tasks.taskEntries.clear();
        golem.targetTasks.taskEntries.clear();

        if (golem.getNavigator() instanceof PathNavigateClimber)
            golem.navigator = new PathNavigateClimberImproved(golem);
        if (golem.getNavigator() instanceof PathNavigateGround)
            golem.tasks.addTask(0, new EntityAISwimming(golem));

        double speed = golem.getGolemMoveSpeed() * speedMod;
        if (golem.isFollowingOwner()) {
            // TC has a bug (?) where golems always have their speed set to 1.0 here
            // This might just be to make them not painfully slow and not a bug

            // The MCP names for distance fields seem to be wrong - first number is max distance, second is min
            golem.tasks.addTask(4, new AIFollowOwner(golem, Math.max(golem.getGolemMoveSpeed(), 1.0F) * speedMod, 8.0F, 2.0F));
        }
        else {
            golem.tasks.addTask(2, new AIGotoHomeIfHurt(golem, speed));
            golem.tasks.addTask(3, new AIGotoEntityImproved(golem, 1.5, speed));
            golem.tasks.addTask(4, new AIGotoBlockImproved(golem, 1.5, speed));
            golem.tasks.addTask(5, new AIGotoHomeImproved(golem, speed));
        }

        golem.tasks.addTask(8, new EntityAIWatchClosest(golem, EntityPlayer.class, 8.0F));
        golem.tasks.addTask(9, new EntityAILookIdle(golem));
        if (golem.getProperties().hasTrait(EnumGolemTrait.FIGHTER)) {
            if (golem.getProperties().hasTrait(EnumGolemTrait.RANGED) && golem.getProperties().getArms().function != null)
                golem.tasks.addTask(2, golem.getProperties().getArms().function.getRangedAttackAI(golem));
            else
                golem.tasks.addTask(2, new EntityAIAttackMelee(golem, Math.max(golem.getGolemMoveSpeed(), 1.15F) * speedMod, false));

            if (golem.isFollowingOwner()) {
                golem.targetTasks.addTask(0, new AIOwnerHurtByTarget(golem));
                golem.targetTasks.addTask(1, new AIOwnerHurtTarget(golem));
            }

            golem.targetTasks.addTask(2, new EntityAIHurtByTarget(golem, false));
        }
    }

    @Override
    public void onAttributeUpdate(EntityThaumcraftGolem golem) {
        IImpetusStorage storage = golem.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
        if (storage instanceof ResizableImpetusStorage)
            ((ResizableImpetusStorage) storage).ensureEnergyStorage(STORAGE_KEY, 1000);

        golem.setHomePosAndDistance(golem.getHomePosition(), 256);
        golem.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(128.0);
        power |= storage.extractEnergy(1, true) == 1;
        updateAttributes(golem, power ? SPEED_MOD_NORMAL : SPEED_MOD_NO_POWER);
    }

}
