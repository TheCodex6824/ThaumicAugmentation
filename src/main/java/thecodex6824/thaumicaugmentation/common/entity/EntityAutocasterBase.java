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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.google.common.math.DoubleMath;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.items.casters.ItemCaster;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.items.casters.foci.FocusMediumBolt;
import thaumcraft.common.items.casters.foci.FocusMediumTouch;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.entity.AutocasterFocusRegistry;
import thecodex6824.thaumicaugmentation.api.event.CastEvent;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;
import thecodex6824.thaumicaugmentation.common.entity.ai.EntityLookHelperUnlimitedPitch;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusMediumBoltCompat;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusMediumTouchCompat;

public abstract class EntityAutocasterBase extends EntityCreature {

    protected static final DataParameter<EnumFacing> FACING = EntityDataManager.createKey(EntityAutocasterBase.class, DataSerializers.FACING);
    
    protected int cooldown;
    protected double cachedMaxDistanceSquared;
    
    public EntityAutocasterBase(World world) {
        super(world);
        lookHelper = new EntityLookHelperUnlimitedPitch(this);
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
        dataManager.register(FACING, EnumFacing.UP);
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
    
    protected abstract void dropItemFromPlacement();
    
    protected abstract int getHealRate();
    
    protected abstract boolean isDisabled();
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            rotationYaw = rotationYawHead;
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
                    dropItemFromPlacement();
                    setDead();
                }
            }
            
            if (ticksExisted % getHealRate() == 0)
                heal(1.0F);
            
            if (cooldown > 0)
                --cooldown;
            
            if (cooldown == 0 && !isDisabled() && getAttackTarget() != null)
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
                ItemStack tempHold = new ItemStack(ItemsTC.casterBasic);
                ((ItemCaster) tempHold.getItem()).setFocus(tempHold, held);
                setHeldItem(EnumHand.MAIN_HAND, tempHold);
                CastEvent.Pre preEvent = new CastEvent.Pre(this, tempHold, new FocusWrapper(f, 
                        (int) (((ItemFocus) held.getItem()).getActivationTime(held)), visCost));
                MinecraftForge.EVENT_BUS.post(preEvent);
                visCost = preEvent.getFocus().getVisCost();
                if (!preEvent.isCanceled() && DoubleMath.fuzzyEquals(AuraHelper.drainVis(world, getPosition(), visCost, false), visCost, 0.00001)) {
                    FocusEngine.castFocusPackage(this, f, true);
                    cooldown = preEvent.getFocus().getCooldown();
                    MinecraftForge.EVENT_BUS.post(new CastEvent.Post(this, tempHold, preEvent.getFocus()));
                }
                
                setHeldItem(EnumHand.MAIN_HAND, ((ItemCaster) tempHold.getItem()).getFocusStack(tempHold));
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
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }
    
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return canBePushed() ? getEntityBoundingBox() : null;
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        
        compound.setInteger("dir", dataManager.get(FACING).getIndex());
        compound.setInteger("cooldown", cooldown);
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        dataManager.set(FACING, EnumFacing.byIndex(compound.getInteger("dir")));
        cooldown = compound.getInteger("cooldown");
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
            super(EntityAutocasterBase.this, requireSight, false);
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
            EntityLivingBase entity = getAttackTarget();
            if (entity == null)
                entity = target;
            
            if (entity == null || !entity.isEntityAlive())
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

                    if (!canAttackClass(target.getClass()) || !selectors.stream().anyMatch(pred -> pred.test(target)))
                        return false;
                    else {
                        setAttackTarget(entity);
                        return true;
                    }
                }
            }
        }
        
        @Override
        protected boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
            if (target == null || EntityAutocasterBase.this == target || !target.isEntityAlive() || !canAttackClass(target.getClass()) || (shouldCheckSight && !getEntitySenses().canSee(target)))
                return false;
            
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
    
}
