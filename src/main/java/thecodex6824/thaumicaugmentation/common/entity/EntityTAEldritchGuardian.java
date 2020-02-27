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

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.capabilities.IPlayerWarp.EnumWarpType;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.entities.projectile.EntityEldritchOrb;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXSonic;
import thecodex6824.thaumicaugmentation.api.TALootTables;
import thecodex6824.thaumicaugmentation.api.event.EntityInOuterLandsEvent;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;

public class EntityTAEldritchGuardian extends EntityEldritchGuardian {

    protected static final DataParameter<Boolean> TRANSPARENT = EntityDataManager.createKey(EntityTAEldritchGuardian.class,
            DataSerializers.BOOLEAN);
    
    protected static final Field LAST_BLAST;
    
    static {
        try {
            LAST_BLAST = EntityEldritchGuardian.class.getDeclaredField("lastBlast");
            LAST_BLAST.setAccessible(true);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public EntityTAEldritchGuardian(World world) {
        super(world);
    }
    
    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new AILongRangeAttack(this, 3.0, 1.0, 20, 40, 24.0F));
        tasks.addTask(3, new EntityAIAttackMelee(this, 1.0, false));
        tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.8));
        tasks.addTask(7, new EntityAIWander(this, 1.0));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityCultist.class, true));
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(TRANSPARENT, true);
    }
    
    public boolean isTransparent() {
        return dataManager.get(TRANSPARENT);
    }
    
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance diff, IEntityLivingData data) {
        IEntityLivingData d = super.onInitialSpawn(diff, data); 
        EntityInOuterLandsEvent event = new EntityInOuterLandsEvent(this);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.ALLOW || (event.getResult() == Result.DEFAULT && world.provider.getDimension() == TADimensions.EMPTINESS.getId())) {
            dataManager.set(TRANSPARENT, false);
            float difficultyMod = Math.max(0.25F, diff.getAdditionalDifficulty() / 2.0F);
            float absorbAmount = (float) getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() / 2.0F * difficultyMod;
            setAbsorptionAmount(getAbsorptionAmount() + absorbAmount);
        }
        else
            dataManager.set(TRANSPARENT, true);
        
        return d;
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote && hurtResistantTime <= 0 && ticksExisted % 25 == 0) {
            EntityInOuterLandsEvent event = new EntityInOuterLandsEvent(this);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.getResult() == Result.ALLOW || (event.getResult() == Result.DEFAULT && world.provider.getDimension() == TADimensions.EMPTINESS.getId())) {
                double bh = getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() / 2.0;
                if (getAbsorptionAmount() < bh)
                    setAbsorptionAmount(getAbsorptionAmount() + 1.0F);
            }
        }
    }
    
    protected void setLastBlast(boolean newValue) {
        try {
            LAST_BLAST.setBoolean(this, newValue);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected boolean getLastBlast() {
        try {
            return LAST_BLAST.getBoolean(this);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        if (rand.nextFloat() > 0.15F) {
            EntityEldritchOrb blast = new EntityEldritchOrb(world, this);
            blast.ignoreEntity = this;
            boolean newBlast = !getLastBlast();
            setLastBlast(newBlast);
            world.setEntityState(this, (byte) (newBlast ? 16 : 15));
            int rr = newBlast ? 90 : 180;
            double xx = Math.cos((rotationYaw + rr) % 360.0 / 180.0 * Math.PI) * 0.5;
            double zz = Math.sin((rotationYaw + rr) % 360.0 / 180.0 * Math.PI) * 0.5;
            blast.setPosition(blast.posX - xx, blast.posY, blast.posZ - zz);
            Vec3d v = target.getPositionVector().add(target.motionX * 10.0, target.motionY * 10.0,
                    target.motionZ * 10.0).subtract(getPositionVector()).normalize();
            blast.shoot(v.x, v.y, v.z, 1.25F, 1.5F);
            playSound(SoundsTC.egattack, 2.0F, 1.0F + rand.nextFloat() * 0.1F);
            world.spawnEntity(blast);
        }
        else if (canEntityBeSeen(target)) {
            target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 400, 0));
            if (target instanceof EntityPlayer)
                ThaumcraftApi.internalMethods.addWarpToPlayer((EntityPlayer) target, 1 + rand.nextInt(3), EnumWarpType.TEMPORARY); 
          
            playSound(SoundsTC.egscreech, 3.0F, 1.0F + rand.nextFloat() * 0.1F);
            PacketHandler.INSTANCE.sendToAllTracking(new PacketFXSonic(getEntityId()), this);
        }
    }
    
    @Override
    public boolean getCanSpawnHere() {
        return world.getDifficulty() != EnumDifficulty.PEACEFUL &&
                getBlockPathWeight(new BlockPos(posX, getEntityBoundingBox().minY, posZ)) >= 0.0F &&
                world.getBlockState(getPosition().down()).canEntitySpawn(this);
    }
    
    @Override
    public boolean isOnSameTeam(Entity entity) {
        return super.isOnSameTeam(entity) || isOnScoreboardTeam(entity.getTeam());
    }
    
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return TALootTables.ELDRITCH_GUARDIAN;
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean("transparent", dataManager.get(TRANSPARENT));
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        dataManager.set(TRANSPARENT, nbt.getBoolean("transparent"));
    }
    
    @Override
    protected Item getDropItem() {
        return null;
    }
    
}
