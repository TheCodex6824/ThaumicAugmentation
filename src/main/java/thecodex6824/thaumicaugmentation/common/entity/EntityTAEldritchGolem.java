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

package thecodex6824.thaumicaugmentation.common.entity;

import java.lang.reflect.Field;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.monster.boss.EntityCultistLeader;
import thaumcraft.common.entities.monster.boss.EntityCultistPortalGreater;
import thaumcraft.common.entities.monster.boss.EntityEldritchGolem;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.entities.monster.cult.EntityCultistPortalLesser;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.common.entity.ai.EntityAIAttackRangedCustomMutex;

public class EntityTAEldritchGolem extends EntityEldritchGolem {

    protected static final Field CHARGING_BEAM;
    protected static final Field BEAM_CHARGE;
    
    static {
        try {
            CHARGING_BEAM = EntityEldritchGolem.class.getDeclaredField("chargingBeam");
            CHARGING_BEAM.setAccessible(true);
            BEAM_CHARGE = EntityEldritchGolem.class.getDeclaredField("beamCharge");
            BEAM_CHARGE.setAccessible(true);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public EntityTAEldritchGolem(World world) {
        super(world);
        setSize(1.75F, 2.95F);
    }
    
    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIAttackMelee(this, 1.1, false));
        tasks.addTask(3, new EntityAIMoveTowardsRestriction(this, 0.8));
        tasks.addTask(5, new EntityAIWander(this, 0.8));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(6, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityCultistLeader.class, true));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityCultist.class, true));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(this, EntityCultistPortalGreater.class, true));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(this, EntityCultistPortalLesser.class, true));
    }
    
    @Override
    public float getEyeHeight() {
        return isHeadless() ? 2.55F : 2.35F;
    }
    
    @Override
    public void generateName() {
        int mod = (int) getEntityAttribute(ThaumcraftApiHelper.CHAMPION_MOD).getAttributeValue();
        if (mod >= 0) {
            setCustomNameTag(new TextComponentTranslation("thaumicaugmentation.text.entity.eldritch_golem",
                    ChampionModifier.mods[mod].getModNameLocalized(),
                    new TextComponentTranslation("entity." + EntityList.getEntityString(this) + ".name")).getFormattedText());
        }
    }
    
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance diff, IEntityLivingData data) {
        EntityUtils.makeChampion(this, true);
        return super.onInitialSpawn(diff, data);
    }
    
    public boolean isChargingBeam() {
        try {
            return CHARGING_BEAM.getBoolean(this);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void setChargingBeam(boolean charging) {
        try {
            CHARGING_BEAM.setBoolean(this, charging);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public int getBeamCharge() {
        try {
            return BEAM_CHARGE.getInt(this);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void setBeamCharge(int charge) {
        try {
            BEAM_CHARGE.setInt(this, charge);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected boolean shouldBeRedOrb() {
        switch (world.getDifficulty()) {
            case NORMAL: return rand.nextInt(5) == 0;
            case HARD: return rand.nextInt(3) == 0;
            default: return false;
        }
    }
    
    protected void fixHeadlessAI() {
        EntityAIBase replace = null;
        for (EntityAITaskEntry task : tasks.taskEntries) {
            if (task.action.getClass() == AILongRangeAttack.class) {
                replace = task.action;
                break;
            }
        }
        
        if (replace != null) {
            tasks.removeTask(replace);
            tasks.addTask(2, new EntityAIAttackRangedCustomMutex<>(this, 5, 32.0F, 0));
        }
    }
    
    @Override
    protected void updateEntityActionState() {
        // TODO Auto-generated method stub
        super.updateEntityActionState();
    }
    
    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        if (getAttackTarget() != null && (!getAttackTarget().isEntityAlive() || getAttackTarget() == this))
            setAttackTarget(null);
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        boolean wasHeadless = isHeadless();
        boolean result = super.attackEntityFrom(source, damage);
        if (!world.isRemote && !wasHeadless && isHeadless())
            fixHeadlessAI();
        
        return result;
    }
    
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        if (canEntityBeSeen(target) && !isChargingBeam() && getBeamCharge() > 0) {
            setBeamCharge(getBeamCharge() - (15 + rand.nextInt(15)));
            lookHelper.setLookPositionWithEntity(target, getHorizontalFaceSpeed(), getVerticalFaceSpeed());
            Vec3d look = getLookVec();
            EntityTAGolemOrb orb = new EntityTAGolemOrb(world, this, target, shouldBeRedOrb());
            orb.setLocationAndAngles(orb.posX + look.x, orb.posY, orb.posZ + look.z, rand.nextInt(360), 0.0F);
            double vx = target.posX + target.motionX - posX;
            double vy = target.posY + target.motionY - posY - target.height / 2.0F;
            double vz = target.posZ + target.motionZ - posZ;
            orb.shoot(vx, vy, vz, 0.66F, 5.0F);
            playSound(SoundsTC.egattack, 1.0F, 1.0F + rand.nextFloat() * 0.1F);
            world.spawnEntity(orb);
        }
    }
    
    @Override
    public boolean isOnSameTeam(Entity entity) {
        if (getTeam() != null)
            return isOnScoreboardTeam(entity.getTeam());
        else
            return entity instanceof IEldritchMob;
    }
    
    @Override
    public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        bossInfo.setName(getDisplayName());
    }
    
    @Override
    public int getHorizontalFaceSpeed() {
        return isHeadless() ? 30 : 10;
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        if (isHeadless())
            fixHeadlessAI();
    }
    
}
