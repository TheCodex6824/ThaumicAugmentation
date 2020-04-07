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
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
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
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.capabilities.IPlayerWarp.EnumWarpType;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.monster.boss.EntityCultistLeader;
import thaumcraft.common.entities.monster.boss.EntityCultistPortalGreater;
import thaumcraft.common.entities.monster.boss.EntityEldritchWarden;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.entities.monster.cult.EntityCultistPortalLesser;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.entities.projectile.EntityEldritchOrb;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXSonic;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.api.event.EntityInOuterLandsEvent;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;

public class EntityTAEldritchWarden extends EntityEldritchWarden {

    protected static final DataParameter<Boolean> TRANSPARENT = EntityDataManager.createKey(EntityTAEldritchWarden.class,
            DataSerializers.BOOLEAN);
    
    protected static final Field LAST_BLAST;
    protected static final Field FRENZY_COUNTER;
    
    static {
        try {
            LAST_BLAST = EntityEldritchWarden.class.getDeclaredField("lastBlast");
            LAST_BLAST.setAccessible(true);
            FRENZY_COUNTER = EntityEldritchWarden.class.getDeclaredField("fieldFrenzyCounter");
            FRENZY_COUNTER.setAccessible(true);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected static final String[] NAMES = new String[] {
            "Aphoom-Zhah", 
            "Basatan", 
            "Chaugnar Faugn", 
            "Mnomquah", 
            "Nyogtha", 
            "Oorn", 
            "Shaikorth", 
            "Rhan-Tegoth", 
            "Rhogog", 
            "Shudde M'ell", 
            "Vulthoom", 
            "Yag-Kosha", 
            "Yibb-Tstll", 
            "Zathog", 
            "Zushakon"
    };
    
    protected static String generateName(Random rng) {
        return NAMES[rng.nextInt(NAMES.length)];
    }
    
    public EntityTAEldritchWarden(World world) {
        super(world);
        setSize(0.8F, 2.25F);
    }
    
    @Override
    public float getEyeHeight() {
        return 2.1F;
    }
    
    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new AILongRangeAttack(this, 2.5, 1.0, 20, 40, 24.0F));
        tasks.addTask(3, new EntityAIAttackMelee(this, 1.1, false));
        tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.8));
        tasks.addTask(7, new EntityAIWander(this, 1.0));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityCultistLeader.class, true));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityCultist.class, true));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(this, EntityCultistPortalGreater.class, true));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(this, EntityCultistPortalLesser.class, true));
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
        EntityUtils.makeChampion(this, true);
        IEntityLivingData d = super.onInitialSpawn(diff, data);
        bossInfo.setName(getDisplayName());
        EntityInOuterLandsEvent event = new EntityInOuterLandsEvent(this);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.ALLOW || (event.getResult() == Result.DEFAULT && world.provider.getDimension() == TADimensions.EMPTINESS.getId()))
            dataManager.set(TRANSPARENT, false);
        else
            dataManager.set(TRANSPARENT, true);
        
        return d;
    }
    
    @Override
    public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        bossInfo.setName(getDisplayName());
    }
    
    @Override
    public void generateName() {
        int cIndex = (int) getEntityAttribute(ThaumcraftApiHelper.CHAMPION_MOD).getAttributeValue();
        setCustomNameTag(new TextComponentTranslation("thaumicaugmentation.text.entity.eldritch_warden", generateName(rand),
                ChampionModifier.mods[cIndex].getModNameLocalized()).getFormattedText());
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote && getSpawnTimer() == 1) {
            if (world.getDifficulty() == EnumDifficulty.HARD) {
                for (int i = 0; i < 3; ++i) {
                    EntityFocusShield shield = new EntityFocusShield(world);
                    shield.setOwner(this);
                    shield.setCasterID(getPersistentID());
                    shield.setColor(0x606060);
                    shield.setMaxHealth(150.0F, false);
                    shield.setInfiniteLifespan();
                    shield.setReflect(true);
                    shield.setYawOffset(i * 120.0F);
                    shield.setRotate(true);
                    shield.setHealth(shield.getMaxHealth());
                    world.spawnEntity(shield);
                    world.playSound(null, shield.getPosition(), SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 
                            SoundCategory.HOSTILE, 0.75F, 1.2F);
                }
            }
            else if (world.getDifficulty() == EnumDifficulty.NORMAL) {
                EntityFocusShield shield = new EntityFocusShield(world);
                shield.setOwner(this);
                shield.setCasterID(getPersistentID());
                shield.setColor(0x606060);
                shield.setMaxHealth(100.0F, false);
                shield.setInfiniteLifespan();
                shield.setReflect(true);
                shield.setHealth(shield.getMaxHealth());
                world.spawnEntity(shield);
                world.playSound(null, shield.getPosition(), SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 
                        SoundCategory.HOSTILE, 1.0F, 1.2F);
            }
            else {
                EntityFocusShield shield = new EntityFocusShield(world);
                shield.setOwner(this);
                shield.setCasterID(getPersistentID());
                shield.setColor(0x606060);
                shield.setMaxHealth(50.0F, false);
                shield.setInfiniteLifespan();
                shield.setHealth(shield.getMaxHealth());
                world.spawnEntity(shield);
                world.playSound(null, shield.getPosition(), SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 
                        SoundCategory.HOSTILE, 1.0F, 1.2F);
            }
        }
    }
    
    protected int getFieldFrenzyCounter() {
        try {
            return FRENZY_COUNTER.getInt(this);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    protected void updateEntityActionState() {
        if (getSpawnTimer() == 0 && getFieldFrenzyCounter() == 0)
            super.updateEntityActionState();
    }
    
    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        bossInfo2.setPercent(Math.min(bossInfo2.getPercent(), 1.0F));
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
        if (rand.nextFloat() > 0.2F) {
            EntityEldritchOrb blast = new EntityEldritchOrb(world, this);
            blast.ignoreEntity = this;
            boolean newBlast = !getLastBlast();
            setLastBlast(newBlast);
            world.setEntityState(this, (byte) (newBlast ? 16 : 15));
            int rr = newBlast ? 90 : 180;
            double xx = Math.cos((rotationYaw + rr) % 360.0 / 180.0 * Math.PI) * 0.5;
            double zz = Math.sin((rotationYaw + rr) % 360.0 / 180.0 * Math.PI) * 0.5;
            blast.setPosition(blast.posX - xx, blast.posY, blast.posZ - zz);
            double x = target.posX + target.motionX - posX;
            double y = target.posY + target.motionY - posY - (target.height / 2.0F);
            double z = target.posZ + target.motionZ - posZ;
            blast.shoot(x, y, z, 2.0F, 0.25F);
            playSound(SoundsTC.egattack, 2.0F, 1.0F + rand.nextFloat() * 0.1F);
            world.spawnEntity(blast);
        }
        else if (canEntityBeSeen(target)) {
            target.addVelocity(-Math.sin(rotationYaw * Math.PI / 180.0) * 1.5, 0.1, Math.cos(rotationYaw * Math.PI / 180.0) * 1.5);
            target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 400, 0));
            target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 400, 0));
            if (target instanceof EntityPlayer)
                ThaumcraftApi.internalMethods.addWarpToPlayer((EntityPlayer) target, 3 + rand.nextInt(3), EnumWarpType.TEMPORARY); 
          
            playSound(SoundsTC.egscreech, 4.0F, 1.0F + rand.nextFloat() * 0.1F);
            PacketHandler.INSTANCE.sendToAllTracking(new PacketFXSonic(getEntityId()), this);
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
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean("transparent", dataManager.get(TRANSPARENT));
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        dataManager.set(TRANSPARENT, nbt.getBoolean("transparent"));
        bossInfo.setName(getDisplayName());
    }
    
}
