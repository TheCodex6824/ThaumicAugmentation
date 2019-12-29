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

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.math.DoubleMath;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.items.casters.foci.FocusMediumBolt;
import thaumcraft.common.items.casters.foci.FocusMediumTouch;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.entity.AutocasterFocusRegistry;
import thecodex6824.thaumicaugmentation.api.event.CastEvent;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;
import thecodex6824.thaumicaugmentation.api.warded.entity.CapabilityWardOwnerProvider;
import thecodex6824.thaumicaugmentation.api.warded.entity.WardOwnerProviderOwnable;
import thecodex6824.thaumicaugmentation.common.entity.ai.EntityLookHelperUnlimitedPitch;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusMediumBoltCompat;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusMediumTouchCompat;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;
import thecodex6824.thaumicaugmentation.init.GUIHandler.TAInventory;

public class EntityAutocaster extends EntityCreature implements IEntityOwnable {

    protected static final DataParameter<Optional<UUID>> OWNER_ID = EntityDataManager.createKey(EntityAutocaster.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<EnumFacing> FACING = EntityDataManager.createKey(EntityAutocaster.class, DataSerializers.FACING);
    protected static final DataParameter<Byte> TARGETS = EntityDataManager.createKey(EntityAutocaster.class, DataSerializers.BYTE);
    
    protected static final Predicate<EntityLivingBase> TARGET_ANIMAL = entity -> entity instanceof IAnimals;
    protected static final Predicate<EntityLivingBase> TARGET_MOB = entity -> entity instanceof IMob;
    protected static final Predicate<EntityLivingBase> TARGET_PLAYER = entity -> entity instanceof EntityPlayer;
    
    protected WeakReference<Entity> ownerRef;
    protected int cooldown;
    protected double cachedMaxDistanceSquared;
    protected EntityAINearestValidTarget targeting;
    protected WardOwnerProviderOwnable<EntityAutocaster> wardOwner;
    
    public EntityAutocaster(World world) {
        super(world);
        lookHelper = new EntityLookHelperUnlimitedPitch(this);
        ownerRef = new WeakReference<>(null);
        setSize(1.0F, 1.0F);
        stepHeight = 0.0F;
        cachedMaxDistanceSquared = -1.0;
        setNoGravity(true);
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(25.0);
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OWNER_ID, Optional.absent());
        dataManager.register(FACING, EnumFacing.UP);
        dataManager.register(TARGETS, (byte) 2);
    }
    
    @Override
    protected void initEntityAI() {
        tasks.addTask(1, new EntityAIWatchTarget());
        tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
        tasks.addTask(3, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        targeting = new EntityAINearestValidTarget(true, 5);
        targeting.addTargetSelector(TARGET_MOB);
        targetTasks.addTask(2, targeting);
    }
    
    public boolean getTargetAnimals() {
        return BitUtil.isBitSet(dataManager.get(TARGETS), 0);
    }
    
    public void setTargetAnimals(boolean target) {
        dataManager.set(TARGETS, (byte) BitUtil.setOrClearBit(dataManager.get(TARGETS), 0, target));
        if (targeting != null) {
            if (target)
                targeting.addTargetSelector(TARGET_ANIMAL);
            else
                targeting.removeTargetSelector(TARGET_ANIMAL);
            
            targeting.resetTask();
        }
    }
    
    public boolean getTargetMobs() {
        return BitUtil.isBitSet(dataManager.get(TARGETS), 1);
    }
    
    public void setTargetMobs(boolean target) {
        dataManager.set(TARGETS, (byte) BitUtil.setOrClearBit(dataManager.get(TARGETS), 1, target));
        if (targeting != null) {
            if (target)
                targeting.addTargetSelector(TARGET_MOB);
            else
                targeting.removeTargetSelector(TARGET_MOB);
            
            targeting.resetTask();
        }
    }
    
    public boolean getTargetPlayers() {
        return BitUtil.isBitSet(dataManager.get(TARGETS), 2);
    }
    
    public void setTargetPlayers(boolean target) {
        dataManager.set(TARGETS, (byte) BitUtil.setOrClearBit(dataManager.get(TARGETS), 2, target));
        if (targeting != null) {
            if (target)
                targeting.addTargetSelector(TARGET_PLAYER);
            else
                targeting.removeTargetSelector(TARGET_PLAYER);
            
            targeting.resetTask();
        }
    }
    
    public boolean getTargetFriendly() {
        return BitUtil.isBitSet(dataManager.get(TARGETS), 3);
    }
    
    public void setTargetFriendly(boolean target) {
        dataManager.set(TARGETS, (byte) BitUtil.setOrClearBit(dataManager.get(TARGETS), 3, target));
        if (targeting != null)
            targeting.resetTask();
    }
    
    @Override
    public int getTotalArmorValue() {
        return 4;
    }
    
    @Override
    @Nullable
    public Entity getOwner() {
        if (ownerRef.get() == null && dataManager.get(OWNER_ID).isPresent()) {
            if (ownerRef.get() == null) {
                List<Entity> entities = world.getEntities(Entity.class, entity -> entity != null && entity.getPersistentID().equals(dataManager.get(OWNER_ID).get()));
                if (!entities.isEmpty())
                    ownerRef = new WeakReference<>(entities.get(0));
            }
        }
        
        return ownerRef.get();
    }
    
    @Override
    @Nullable
    public UUID getOwnerId() {
        return dataManager.get(OWNER_ID).orNull();
    }
    
    public void setOwner(Entity newOwner) {
        dataManager.set(OWNER_ID, Optional.of(newOwner.getPersistentID()));
        ownerRef = new WeakReference<>(newOwner);
    }
    
    public EnumFacing getFacing() {
        return dataManager.get(FACING);
    }
    
    public void returnToOriginalRotation() {
        rotationPitch = 0.0F;
        EnumFacing facing = dataManager.get(FACING);
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN)
            rotationYaw = rotationYawHead = 0.0F;
        else
            rotationYaw = rotationYawHead = facing.getHorizontalAngle();
    }
    
    public void setFacing(EnumFacing face) {
        dataManager.set(FACING, face);
        returnToOriginalRotation();
    }
    
    @Override
    public boolean isOnSameTeam(Entity other) {
        Entity owner = getOwner();
        if (other.equals(owner))
            return true;
        else if (owner != null && other.isOnSameTeam(getOwner()))
            return true;
        else
            return super.isOnSameTeam(other);
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            rotationYaw = rotationYawHead;
            if (getAttackTarget() != null && isOnSameTeam(getAttackTarget()))
                setAttackTarget(null);
            
            EnumFacing facing = dataManager.get(FACING);
            BlockPos checkPos = getPosition().offset(facing.getOpposite());
            if (!world.getBlockState(checkPos).isSideSolid(world, checkPos, facing.getOpposite())) {
                boolean ok = false;
                for (EnumFacing face : EnumFacing.VALUES) {
                    checkPos = getPosition().offset(face.getOpposite());
                    if (face != facing && world.getBlockState(checkPos).isSideSolid(world, checkPos, face.getOpposite())) {
                        setFacing(face);
                        setPosition(Math.floor(posX) + 0.5, Math.floor(posY), Math.floor(posZ) + 0.5);
                        ok = true;
                        break;
                    }
                }
                
                if (!ok) {
                    dropFocus();
                    entityDropItem(new ItemStack(TAItems.AUTOCASTER_PLACER), 0.5F);
                    setDead();
                }
            }
            
            if (ticksExisted % 40 == 0)
                heal(1.0F);
            
            if (cooldown > 0)
                --cooldown;
            
            if (cooldown == 0 && getAttackTarget() != null)
                attackEntityWithFocus();
        }
    }
    
    @Override
    public void setHeldItem(EnumHand hand, ItemStack stack) {
        super.setHeldItem(hand, stack);
        if (hand == EnumHand.MAIN_HAND)
            cachedMaxDistanceSquared = -1.0;
    }
    
    protected double getMaxFocusDistanceSquared(ItemStack stack) {
        if (cachedMaxDistanceSquared < 0) {
            double d = AutocasterFocusRegistry.getMaxDistance(stack);
            cachedMaxDistanceSquared = d * d;
        }
        
        return cachedMaxDistanceSquared;
    }
    
    protected void fixFoci(FocusPackage f) {
        List<IFocusElement> nodes = f.nodes;
        for (int i = 0; i < nodes.size(); ++i) {
            IFocusElement element = nodes.get(i);
            if (element.getClass() == FocusMediumTouch.class)
                nodes.set(i, new FocusMediumTouchCompat((FocusMediumTouch) element));
            else if (element.getClass() == FocusMediumBolt.class)
                nodes.set(i, new FocusMediumBoltCompat((FocusMediumBolt) element));
        }
    }
    
    protected void attackEntityWithFocus() {
        ItemStack held = getHeldItemMainhand();
        if (held != null && held.getItem() instanceof ItemFocus) {
            double d = getDistanceSq(getAttackTarget());
            if (d <= getMaxFocusDistanceSquared(held)) {
                FocusPackage f = ItemFocus.getPackage(held);
                fixFoci(f);
                f.setCasterUUID(this.getUniqueID());
                float visCost = ((ItemFocus) held.getItem()).getVisCost(held);
                // TODO maybe use a wrapper caster item for the events
                CastEvent.Pre preEvent = new CastEvent.Pre(this, held, new FocusWrapper(f, 
                        (int) (((ItemFocus) held.getItem()).getActivationTime(held)), visCost));
                MinecraftForge.EVENT_BUS.post(preEvent);
                visCost = preEvent.getFocus().getVisCost();
                if (!preEvent.isCanceled() && DoubleMath.fuzzyEquals(AuraHelper.drainVis(world, getPosition(), visCost, false), visCost, 0.00001)) {
                    FocusEngine.castFocusPackage(this, f, true);
                    cooldown = preEvent.getFocus().getCooldown();
                    MinecraftForge.EVENT_BUS.post(new CastEvent.Post(this, held, preEvent.getFocus()));
                }
            }
        }
    }
    
    @Override
    public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {}
    
    @Override
    public void move(MoverType type, double x, double y, double z) {}
    
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return canBePushed() ? getEntityBoundingBox() : null;
    }
    
    protected void dropFocus() {
        if (getHeldItemMainhand() != null && !getHeldItemMainhand().isEmpty())
            entityDropItem(getHeldItemMainhand(), 0.5F);
    }
    
    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && player.equals(getOwner()) && !isDead) {
            if (player.isSneaking()) {
                playSound(SoundsTC.zap, 1.0F, 1.0F);
                dropFocus();
                entityDropItem(new ItemStack(TAItems.AUTOCASTER_PLACER), 0.5F);
                setDead();
                player.swingArm(hand);
                return true;
            }
            else {
                player.openGui(ThaumicAugmentation.instance, TAInventory.AUTOCASTER.getID(), world, getEntityId(), 0, 0);
                return true;
            }
        }
        else
            return super.processInteract(player, hand);
    }
    
    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!world.isRemote)
            dropFocus();
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (dataManager.get(OWNER_ID).isPresent())
            compound.setUniqueId("owner", dataManager.get(OWNER_ID).get());
        
        compound.setInteger("dir", dataManager.get(FACING).getIndex());
        compound.setInteger("cooldown", cooldown);
        compound.setByte("targets", dataManager.get(TARGETS));
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("owner"))
            dataManager.set(OWNER_ID, Optional.of(compound.getUniqueId("owner")));
        
        dataManager.set(FACING, EnumFacing.byIndex(compound.getInteger("dir")));
        cooldown = compound.getInteger("cooldown");
        dataManager.set(TARGETS, compound.getByte("targets"));
        // sync value to AI fields
        setTargetAnimals(getTargetAnimals());
        setTargetMobs(getTargetMobs());
        setTargetPlayers(getTargetPlayers());
        setTargetFriendly(getTargetFriendly());
    }
    
    @Override
    public float getEyeHeight() {
        return 0.5F;
    }
    
    @Override
    protected boolean canDespawn() {
        return false;
    }
    
    @Override
    public int getTalkInterval() {
        return 240;
    }
    
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundsTC.clack;
    }
    
    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundsTC.clack;
    }
    
    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundsTC.tool;
    }
    
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
    
    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }
    
    @Override
    public boolean isPotionApplicable(PotionEffect potioneffectIn) {
        return false;
    }
    
    @Override
    public int getVerticalFaceSpeed() {
        return 20;
    }
    
    @Override
    public int getHorizontalFaceSpeed() {
        return 10;
    }
    
    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
        if (getTargetAnimals() && IAnimals.class.isAssignableFrom(cls) && !IMob.class.isAssignableFrom(cls))
            return true;
        else if (getTargetMobs() && IMob.class.isAssignableFrom(cls))
            return true;
        else if (getTargetPlayers() && EntityPlayer.class.isAssignableFrom(cls))
            return true;
        else
            return false;
    }
    
    protected class EntityAIWatchTarget extends EntityAIBase {
        
        public EntityAIWatchTarget() {
            setMutexBits(2);
        }
        
        @Override
        public boolean shouldExecute() {
            return getAttackTarget() != null;
        }
        
        protected double getTargetDistance() {
            IAttributeInstance range = getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
            return range != null ? range.getAttributeValue() : 16.0;
        }
        
        @Override
        public void startExecuting() {
            cooldown = Math.max(cooldown, 20);
        }
        
        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase e = getAttackTarget();
            double d = getTargetDistance() * 1.5;
            return e != null && e.isEntityAlive() && getDistanceSq(e) <= d * d;
        }
        
        @Override
        public void updateTask() {
            getLookHelper().setLookPositionWithEntity(getAttackTarget(), getHorizontalFaceSpeed(), getVerticalFaceSpeed());
        }
        
    }
    
    protected class EntityAINearestValidTarget extends EntityAITarget {
        
        protected Set<Predicate<EntityLivingBase>> selectors;
        protected int chance;
        protected int unseenTicks;
        
        public EntityAINearestValidTarget(boolean requireSight, int targetChance) {
            super(EntityAutocaster.this, requireSight, false);
            chance = targetChance;
            selectors = new HashSet<>();
            setMutexBits(1);
        }
        
        public void addTargetSelector(Predicate<EntityLivingBase> selector) {
            selectors.add(selector);
        }
        
        public void removeTargetSelector(Predicate<EntityLivingBase> selector) {
            selectors.remove(selector);
        }
        
        protected boolean isSuitableTarget(@Nullable EntityLivingBase target) {
            return isSuitableTarget(target, false);
        }
        
        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase entity = this.taskOwner.getAttackTarget();
            if (entity == null)
                entity = target;
            
            if (entity == null || !entity.isEntityAlive())
                return false;
            else {
                Team myTeam = getTeam();
                Team theirTeam = entity.getTeam();

                if (myTeam != null && myTeam == theirTeam && !getTargetFriendly())
                    return false;
                else if (myTeam != null && myTeam != theirTeam && getTargetFriendly())
                    return false;
                else {
                    double d = getTargetDistance() * 1.5;
                    if (getDistanceSq(entity) > d * d)
                        return false;
                    else {
                        if (shouldCheckSight) {
                            if (getEntitySenses().canSee(entity))
                                unseenTicks = 0;
                            else if (++unseenTicks > unseenMemoryTicks)
                                return false;
                        }

                        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.disableDamage)
                            return false;
                        else {
                            setAttackTarget(entity);
                            return true;
                        }
                    }
                }
            }
        }
        
        @Override
        protected boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
            if (target == null || EntityAutocaster.this == target || !target.isEntityAlive() || !canAttackClass(target.getClass()) || (shouldCheckSight && !getEntitySenses().canSee(target)))
                return false;
            else if (target instanceof EntityPlayer && !ThaumicAugmentation.proxy.isPvPEnabled())
                return false;
            
            Team myTeam = getTeam();
            Team theirTeam = target.getTeam();
            if (myTeam != null && myTeam == theirTeam && !getTargetFriendly())
                return false;
            else if (myTeam != null && myTeam != theirTeam && getTargetFriendly())
                return false;
            if (target.equals(getOwner()) && !getTargetFriendly())
                return false;
            else if (target instanceof IEntityOwnable) {
                IEntityOwnable ownable = (IEntityOwnable) target;
                if (ownable.getOwner() != null && ownable.getOwner().equals(getOwner()) && !getTargetFriendly())
                    return false;
                else if (ownable.getOwner() != null && !ownable.getOwner().equals(getOwner()) && getTargetFriendly())
                    return false;
            }
            
            if (target instanceof EntityPlayer && ((EntityPlayer) target).capabilities.disableDamage)
                return false;
            
            return selectors.stream().anyMatch(pred -> pred.test(target));
        }
        
        @Override
        public boolean shouldExecute() {
            if (chance > 0 && rand.nextInt(chance) != 0)
                return false;
            
            double d = getTargetDistance();
            List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class,
                    getEntityBoundingBox().grow(d, d, d), Predicates.and(EntitySelectors.NOT_SPECTATING, this::isSuitableTarget));
            targets.sort((entity1, entity2) -> Double.compare(getDistanceSq(entity1), getDistanceSq(entity2)));
            if (targets.isEmpty())
                return false;
            
            target = targets.get(0);
            return true;
        }
        
        @Override
        public void startExecuting() {
            super.startExecuting();
            setAttackTarget(target);
        }
        
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityWardOwnerProvider.WARD_OWNER ? true : super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityWardOwnerProvider.WARD_OWNER) {
            if (wardOwner == null)
                wardOwner = new WardOwnerProviderOwnable<>(this);
            
            return CapabilityWardOwnerProvider.WARD_OWNER.cast(wardOwner);
        }
        else
            return super.getCapability(capability, facing);
    }
    
}
