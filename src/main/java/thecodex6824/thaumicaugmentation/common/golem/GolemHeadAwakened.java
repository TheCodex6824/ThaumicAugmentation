package thecodex6824.thaumicaugmentation.common.golem;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.pathfinding.PathNavigateGround;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.parts.GolemHead;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.golems.ai.AIFollowOwner;
import thaumcraft.common.golems.ai.AIOwnerHurtByTarget;
import thaumcraft.common.golems.ai.AIOwnerHurtTarget;
import thecodex6824.thaumicaugmentation.api.golem.IGolemAttributeUpdateReceiver;
import thecodex6824.thaumicaugmentation.common.golem.ai.*;

public class GolemHeadAwakened implements GolemHead.IHeadFunction, IGolemAttributeUpdateReceiver {

    @Override
    public void onUpdateTick(IGolemAPI iGolemAPI) {

    }

    @Override
    public void onAttributeUpdate(EntityThaumcraftGolem golem) {
        golem.setHomePosAndDistance(golem.getHomePosition(), 256);
        golem.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(128.0);
        golem.tasks.taskEntries.clear();
        golem.targetTasks.taskEntries.clear();

        if (golem.getNavigator() instanceof PathNavigateClimber)
            golem.navigator = new PathNavigateClimberImproved(golem);
        if (golem.getNavigator() instanceof PathNavigateGround)
            golem.tasks.addTask(0, new EntityAISwimming(golem));

        if (golem.isFollowingOwner()) {
            // TC has a bug (?) where golems always have their speed set to 1.0 here
            // This might just be to make them not painfully slow and not a bug
            golem.tasks.addTask(4, new AIFollowOwner(golem, Math.max(golem.getGolemMoveSpeed(), 1.0F), 2.0F, 10.0F));
        }
        else {
            golem.tasks.addTask(2, new AIGotoHomeIfHurt(golem));
            golem.tasks.addTask(3, new AIGotoEntityImproved(golem, 4.0));
            golem.tasks.addTask(4, new AIGotoBlockImproved(golem, 4.0));
            golem.tasks.addTask(5, new AIGotoHomeImproved(golem));
        }

        golem.tasks.addTask(8, new EntityAIWatchClosest(golem, EntityPlayer.class, 8.0F));
        golem.tasks.addTask(9, new EntityAILookIdle(golem));
        if (golem.getProperties().hasTrait(EnumGolemTrait.FIGHTER)) {
            if (golem.getProperties().hasTrait(EnumGolemTrait.RANGED) && golem.getProperties().getArms().function != null)
                golem.tasks.addTask(2, golem.getProperties().getArms().function.getRangedAttackAI(golem));
            else
                golem.tasks.addTask(2, new EntityAIAttackMelee(golem, Math.max(golem.getGolemMoveSpeed(), 1.15F), false));

            if (golem.isFollowingOwner()) {
                golem.targetTasks.addTask(0, new AIOwnerHurtByTarget(golem));
                golem.targetTasks.addTask(1, new AIOwnerHurtTarget(golem));
            }

            golem.targetTasks.addTask(2, new EntityAIHurtByTarget(golem, false));
        }
    }

}
