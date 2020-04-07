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

import java.util.ArrayList;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.BossInfo.Overlay;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.internal.CommonInternals;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TALootTables;
import thecodex6824.thaumicaugmentation.api.entity.PrimalWispAttackRegistry;
import thecodex6824.thaumicaugmentation.api.util.QuadConsumer;
import thecodex6824.thaumicaugmentation.api.util.RaytraceHelper;
import thecodex6824.thaumicaugmentation.common.entity.ai.EntityAIAttackRangedCustomMutex;
import thecodex6824.thaumicaugmentation.common.entity.ai.EntityAIFlyToTarget;
import thecodex6824.thaumicaugmentation.common.entity.ai.EntityAIFlyWander;
import thecodex6824.thaumicaugmentation.common.entity.ai.EntityAIHurtByTargetAnyLiving;
import thecodex6824.thaumicaugmentation.common.entity.ai.EntityAINearestAttackableTargetAnyLiving;
import thecodex6824.thaumicaugmentation.common.network.PacketFollowingOrb;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class EntityPrimalWisp extends EntityFlying implements IMob, IRangedAttackMob {
    
    protected BossInfoServer boss;
    protected Object2IntOpenHashMap<Aspect> aspectTotals;
    protected ArrayList<Aspect> aspectList;
    
    public EntityPrimalWisp(World world) {
        super(world);
        setSize(2.25F, 2.25F);
        experienceValue = 50;
        boss = new BossInfoServer(getDisplayName(), Color.PINK, Overlay.PROGRESS);
        aspectTotals = new Object2IntOpenHashMap<>();
        aspectList = new ArrayList<>();
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(150.0);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0);
        getAttributeMap().registerAttribute(ThaumcraftApiHelper.CHAMPION_MOD).setBaseValue(-2.0);
    }
    
    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        tasks.addTask(2, new EntityAIAttackRangedCustomMutex<>(this, 20, 40, 16.0F, 2));
        tasks.addTask(4, new EntityAIFlyToTarget(this, 0.1F, false));
        tasks.addTask(7, new EntityAIFlyWander(this, 0.1F));
        targetTasks.addTask(1, new EntityAIHurtByTargetAnyLiving(this, true));
        targetTasks.addTask(2, new EntityAINearestAttackableTargetAnyLiving<>(this, EntityPlayer.class, true));
    }
    
    // EntityUtils#makeChampion requires an EntityMob instance for some reason...
    protected void makeChampion() {
        if (getEntityAttribute(ThaumcraftApiHelper.CHAMPION_MOD).getAttributeValue() > -2.0)
            return;
      
        int type = rand.nextInt(ChampionModifier.mods.length);
        IAttributeInstance mod = getEntityAttribute(ThaumcraftApiHelper.CHAMPION_MOD);
        mod.removeModifier(ChampionModifier.mods[type].attributeMod);
        mod.applyModifier(ChampionModifier.mods[type].attributeMod);
        
        IAttributeInstance health = getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        health.removeModifier(EntityUtils.CHAMPION_HEALTH);
        health.applyModifier(EntityUtils.CHAMPION_HEALTH);
        IAttributeInstance attackDamage = getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        attackDamage.removeModifier(EntityUtils.CHAMPION_DAMAGE);
        attackDamage.applyModifier(EntityUtils.CHAMPION_DAMAGE);
        setHealth(getMaxHealth());
        setCustomNameTag(ChampionModifier.mods[type].getModNameLocalized() + " " + getName());
        enablePersistence();
        switch (type) {
          case 0: { // bold
              IAttributeInstance movementSpeed = getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
              movementSpeed.removeModifier(EntityUtils.BOLDBUFF);
              movementSpeed.applyModifier(EntityUtils.BOLDBUFF);
              break;
          }
          case 3: { // mighty
              attackDamage.removeModifier(EntityUtils.MIGHTYBUFF);
              attackDamage.applyModifier(EntityUtils.MIGHTYBUFF);
              break;
          }
          case 5: { // warded
              int warding = (int) (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() / 2.0);
              setAbsorptionAmount(getAbsorptionAmount() + warding);
              break;
          }
          default: break;
        } 
    }
    
    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData data) {
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0F + world.getDifficulty().getId());
        makeChampion();
        IEntityLivingData d = super.onInitialSpawn(difficulty, data);
        boss.setName(getDisplayName());
        return d;
    }
    
    @Override
    public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        boss.setName(getDisplayName());
    }
    
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote) {
            if (world.getDifficulty() == EnumDifficulty.PEACEFUL)
                setDead();
            else {
                boss.setPercent(getHealth() / getMaxHealth());
                if (ticksExisted % 20 == 0) {
                    BlockPos consume = getPosition().add(rand.nextInt(6) - rand.nextInt(6), rand.nextInt(6) - rand.nextInt(6),
                            rand.nextInt(6) - rand.nextInt(6));
                    
                    if (!world.isBlockLoaded(consume) || world.isAirBlock(consume)) {
                        if (aspectTotals.addTo(Aspect.AIR, 1) == 0)
                            aspectList.add(Aspect.AIR);
                        
                        TANetwork.INSTANCE.sendToAllTracking(new PacketFollowingOrb(consume.getX() + 0.5,
                                consume.getY() + 0.5, consume.getZ() + 0.5, Aspect.AIR.getColor(), getEntityId()), this);
                    }
                    else {
                        IBlockState state = world.getBlockState(consume);
                        ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
                        // you can tell things are going great when you need to access
                        // another mod's class called "CommonInternals"
                        AspectList aspects = CommonInternals.objectTags.get(CommonInternals.generateUniqueItemstackId(stack));
                        if (aspects != null && aspects.size() > 0) {
                            float aR = 0.0F;
                            float aG = 0.0F;
                            float aB = 0.0F;
                            for (Entry<Aspect, Integer> entry : aspects.aspects.entrySet()) {
                                if (aspectTotals.addTo(entry.getKey(), Math.min(entry.getValue(), 10)) == 0)
                                    aspectList.add(entry.getKey());
                                
                                int color = entry.getKey().getColor();
                                aR = aR * 0.5F + ((color >> 16) & 0xFF) / 510.0F;
                                aG = aG * 0.5F + ((color >> 8) & 0xFF) / 510.0F;
                                aB = aB * 0.5F + (color & 0xFF) / 510.0F;
                            }
                            
                            int packed = (((int) (aR * 255)) << 16) | (((int) (aG * 255)) << 8) | ((int) (aB * 255));
                            TANetwork.INSTANCE.sendToAllTracking(new PacketFollowingOrb(consume.getX() + 0.5,
                                    consume.getY() + 0.5, consume.getZ() + 0.5, packed, getEntityId()), this);
                        }
                    }
                }
            }
        }
        else if (world.isRemote) {
            if (ticksExisted < 2)
                ThaumicAugmentation.proxy.getRenderHelper().renderBurst(world, posX, posY, posZ, 10.0F, 0xFFFFFF);
            if (rand.nextBoolean()) {
                Aspect selected = Aspect.getPrimalAspects().get(rand.nextInt(Aspect.getPrimalAspects().size()));
                ThaumicAugmentation.proxy.getRenderHelper().renderWispParticles(posX + ((rand.nextFloat() - rand.nextFloat()) * 0.7F),
                        posY + ((rand.nextFloat() - rand.nextFloat()) * 0.7F), posZ + ((rand.nextFloat() - rand.nextFloat()) * 0.7F),
                        0.0, 0.0, 0.0, selected.getColor(), 0);
            }
        }
    }
    
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        double dx = target.posX - posX;
        double dz = target.posZ - posZ;
        renderYawOffset = rotationYaw = rotationYawHead = (float) Math.toDegrees(-Math.atan2(dx, dz));
        rotationPitch = (float) Math.toDegrees(-Math.atan2(target.posY - posY, Math.sqrt(dx * dx + dz * dz)));
        float baseDamage = (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        Entity t = RaytraceHelper.raytraceEntity(this, target.getDistance(this), entity -> entity instanceof EntityLivingBase);
        if (!(t instanceof EntityLivingBase))
            t = target;
        
        if (!aspectList.isEmpty() && rand.nextBoolean()) {
            Aspect chosen = aspectList.get(rand.nextInt(aspectList.size()));
            QuadConsumer<EntityPrimalWisp, EntityLivingBase, Aspect, Integer> attack = PrimalWispAttackRegistry.getAttack(chosen);
            if (attack != null)
                attack.accept(this, (EntityLivingBase) t, chosen, aspectTotals.getInt(chosen));
            else {
                t.attackEntityFrom(DamageSource.causeMobDamage(this).setMagicDamage(), baseDamage);
                PrimalWispAttackRegistry.createWispZap(this, t, chosen.getColor());
            }
        }
        else {
            t.attackEntityFrom(DamageSource.causeMobDamage(this).setMagicDamage(), baseDamage);
            playSound(SoundsTC.zap, 1.0F, 1.1F);
            Aspect chosen = !aspectList.isEmpty() ? aspectList.get(rand.nextInt(aspectList.size())) : Aspect.ORDER;
            PrimalWispAttackRegistry.createWispZap(this, t, chosen.getColor());
        }
    }
    
    @Override
    public float getEyeHeight() {
        return height / 2.0F;
    }
    
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return TALootTables.PRIMAL_WISP;
    }
    
    @Override
    public void setSwingingArms(boolean swingingArms) {}
    
    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        boss.addPlayer(player);
    }
    
    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        boss.removePlayer(player);
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        NBTTagCompound aspects = new NBTTagCompound();
        for (Entry<Aspect, Integer> entry : aspectTotals.entrySet())
            aspects.setInteger(entry.getKey().getTag(), entry.getValue());
        
        compound.setTag("aspectTotals", aspects);
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        boss.setName(getDisplayName());
        NBTTagCompound aspects = compound.getCompoundTag("aspectTotals");
        for (String name : aspects.getKeySet()) {
            if (aspects.hasKey(name, NBT.TAG_INT)) {
                Aspect aspect = Aspect.getAspect(name);
                if (aspect != null) {
                    aspectTotals.addTo(aspect, aspects.getInteger(name));
                    aspectList.add(aspect);
                }
            }
        }
    }
    
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }
    
    @Override
    protected void playStepSound(BlockPos pos, Block block) {}
    
    @Override
    public boolean isNonBoss() {
        return false;
    }
    
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
    
    @Override
    public boolean canBePushed() {
        return false;
    }
    
    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }
    
    @Override
    public void setInWeb() {}
    
    @Override
    protected boolean canDespawn() {
        return false;
    }
    
    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }
    
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundsTC.wisplive;
    }
    
    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.BLOCK_LAVA_EXTINGUISH;
    }
    
    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundsTC.wispdead;
    }
    
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }
    
}
