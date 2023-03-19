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
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.common.entities.projectile.EntityFocusCloud;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.fx.PacketFXShield;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.entity.ICastedEntity;
import thecodex6824.thaumicaugmentation.api.entity.IImpulseSpecialEntity;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class EntityFocusShield extends EntityLivingBase implements IEntityOwnable, ICastedEntity, IImpulseSpecialEntity {

    protected static final DataParameter<Optional<UUID>> OWNER_ID = EntityDataManager.createKey(EntityFocusShield.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Optional<UUID>> CASTER_ID = EntityDataManager.createKey(EntityFocusShield.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Integer> COLOR = EntityDataManager.createKey(EntityFocusShield.class, DataSerializers.VARINT);
    protected static final DataParameter<Integer> LIFESPAN = EntityDataManager.createKey(EntityFocusShield.class, DataSerializers.VARINT);
    protected static final DataParameter<Float> YAW_OFFSET = EntityDataManager.createKey(EntityFocusShield.class, DataSerializers.FLOAT);
    protected static final DataParameter<Boolean> ROTATE = EntityDataManager.createKey(EntityFocusShield.class, DataSerializers.BOOLEAN);
    
    protected static final Field CLOUD_FOCUS;
    
    static {
        try {
            CLOUD_FOCUS = EntityFocusCloud.class.getDeclaredField("focusPackage");
            CLOUD_FOCUS.setAccessible(true);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected int timeAlive;
    protected boolean reflect;
    protected int aloneTicks;
    protected WeakReference<Entity> ownerRef;
    
    public EntityFocusShield(World world) {
        super(world);
        ownerRef = new WeakReference<>(null);
        maxHurtResistantTime = 0;
        setSize(1.8F, 1.8F);
        setHealth(getMaxHealth());
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0);
        getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(5.0);
        setMaxHealth(5.0F, true);
    }
    
    public void setMaxHealth(float newMax) {
        setMaxHealth(newMax, true);
    }
    
    public void setMaxHealth(float newMax, boolean updateLifespan) {
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(newMax);
        if (updateLifespan)
            dataManager.set(LIFESPAN, (int) (newMax * 150));
    }
    
    public void setInfiniteLifespan() {
        dataManager.set(LIFESPAN, -1);
    }
    
    public void resetTimeAlive() {
        timeAlive = 0;
    }
    
    public int getTimeAlive() {
        return timeAlive;
    }
    
    public int getTotalLifespan() {
        return dataManager.get(LIFESPAN);
    }
    
    public int getColor() {
        return dataManager.get(COLOR);
    }
    
    public void setColor(int newColor) {
        dataManager.set(COLOR, newColor);
    }
    
    public void setYawOffset(float yaw) {
        dataManager.set(YAW_OFFSET, yaw);
    }
    
    public float getYawOffset() {
        return dataManager.get(YAW_OFFSET);
    }
    
    public void setRotate(boolean shouldRotate) {
        dataManager.set(ROTATE, shouldRotate);
    }
    
    public boolean getRotate() {
        return dataManager.get(ROTATE);
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OWNER_ID, Optional.absent());
        dataManager.register(CASTER_ID, Optional.absent());
        dataManager.register(COLOR, 0x5000C8);
        dataManager.register(LIFESPAN, 1500);
        dataManager.register(YAW_OFFSET, 0.0F);
        dataManager.register(ROTATE, false);
    }
    
    @Override
    @Nullable
    public Entity getOwner() {
        return ownerRef.get();
    }
    
    @Override
    @Nullable
    public UUID getOwnerId() {
        return dataManager.get(OWNER_ID).get();
    }
    
    public void setOwner(@Nullable Entity newOwner) {
        if (newOwner != null) {
            dataManager.set(OWNER_ID, Optional.of(newOwner.getPersistentID()));
            ownerRef = new WeakReference<>(newOwner);
            float lYaw = 0.0F;
            if (newOwner instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase) newOwner;
                lYaw = living.rotationYawHead + getYawOffset();
            }
            else
                lYaw = newOwner.rotationYaw + getYawOffset();
            
            Vec3d lookVec = getEntityLookVector(lYaw, newOwner.rotationPitch).scale(1.5);
            setLocationAndAngles(newOwner.posX + lookVec.x, newOwner.posY + lookVec.y + (newOwner.height / 2.0) - height / 2.0,
                    newOwner.posZ + lookVec.z, lYaw, newOwner.rotationPitch);
        }
        else {
            dataManager.set(OWNER_ID, Optional.absent());
            ownerRef = new WeakReference<>(null);
        }
    }
    
    @Override
    @Nullable
    public UUID getCasterID() {
        return dataManager.get(CASTER_ID).get();
    }
    
    @Override
    public void setCasterID(@Nullable UUID newCaster) {
        dataManager.set(CASTER_ID, Optional.fromNullable(newCaster));
    }
    
    public void setReflect(boolean ref) {
        reflect = ref;
    }
    
    public boolean canReflect() {
        return reflect;
    }
    
    @Override
    public boolean shouldStopRailgunBeam(EntityLivingBase beamShooter) {
        return canReflect();
    }
    
    @Override
    public boolean shouldImpulseCannonIgnore(EntityLivingBase beamShooter) {
        return beamShooter.equals(ownerRef.get());
    }
    
    protected float getCloudPower(EntityFocusCloud cloud) {
        FocusPackage p = null;
        try {
            p = (FocusPackage) CLOUD_FOCUS.get(cloud);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        float power = 0.0F;
        for (IFocusElement node : p.nodes) {
            if (node instanceof FocusEffect)
                power += ((FocusEffect) node).getDamageForDisplay(1.0F);
        }
        
        return power;
    }
    
    protected boolean trySetProjectileOwner(Entity proj) {
    	Entity owner = ownerRef.get();
    	if (owner == null) {
    		return false;
    	}
    	
    	if (proj instanceof IThrowableEntity) {
    		((IThrowableEntity) proj).setThrower(owner);
    		return true;
    	}
    	else if (proj instanceof EntityThrowable && owner instanceof EntityLivingBase) {
    		((EntityThrowable) proj).thrower = (EntityLivingBase) owner;
    		return true;
    	}
    	else if (proj instanceof EntityArrow) {
    		((EntityArrow) proj).shootingEntity = owner;
    		return true;
    	}
    	else if (proj instanceof EntityFireball && owner instanceof EntityLivingBase) {
    		((EntityFireball) proj).shootingEntity = (EntityLivingBase) owner;
    		return true;
    	}
    	
    	return false;
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.FALL)
            return false;
        else if (source.getDamageType().equals("player") && ownerRef.get() != null && ownerRef.get().equals(source.getImmediateSource()))
            return false;
        else if (super.attackEntityFrom(source, amount)) {
	        if (!world.isRemote) {
	        	if (reflect) {
		            Entity entity = source.getImmediateSource();
		            if ((entity instanceof IProjectile || entity instanceof EntityFireball) && entity.isEntityAlive()) {
		                Entity newEntity = EntityList.newEntity(entity.getClass(), entity.world);
		                if (newEntity != null) {
		                    NBTTagCompound toCopy = entity.serializeNBT();
		                    toCopy.setUniqueId("UUID", newEntity.getUniqueID());
		                    newEntity.deserializeNBT(toCopy);
		                    newEntity.setLocationAndAngles(entity.posX - entity.motionX, entity.posY - entity.motionY, entity.posZ - entity.motionZ, -entity.rotationYaw, -entity.rotationPitch);
		                    newEntity.motionX = -entity.motionX;
		                    newEntity.motionY = -entity.motionY;
		                    newEntity.motionZ = -entity.motionZ;
		                    if (trySetProjectileOwner(newEntity)) {
			                    int attempts = 0;
			                    while (newEntity.getEntityBoundingBox().intersects(getEntityBoundingBox()) && attempts++ < 10) {
			                        newEntity.setPosition(newEntity.posX + newEntity.motionX, newEntity.posY + newEntity.motionY, newEntity.posZ + newEntity.motionZ);
			                    }
			                    
			                    if (attempts < 10) {
				                    if (entity instanceof EntityFireball && newEntity instanceof EntityFireball) {
				                        EntityFireball original = (EntityFireball) entity;
				                        EntityFireball fireball = (EntityFireball) newEntity;
				                        fireball.accelerationX = -original.accelerationX;
				                        fireball.accelerationY = -original.accelerationY;
				                        fireball.accelerationZ = -original.accelerationZ;
				                    }
				                    else if (newEntity instanceof EntityTippedArrow) {
				                        ((EntityTippedArrow) newEntity).setPotionEffect(entity instanceof EntityTippedArrow ?
				                                ((EntityTippedArrow) entity).getArrowStack() : new ItemStack(Items.ARROW));
				                    }
				                    
				                    newEntity.velocityChanged = true;
				                    newEntity.world.spawnEntity(newEntity);
				                    // entity could still spawn regardless of spawnEntity return value (events), so always kill it
				                    entity.setDead();
			                    }
		                    }
		                }
		            }
	        	}
	        	
	        	int targetID = -1;
                if (source.getTrueSource() != null)
                    targetID = source.getTrueSource().getEntityId();
                else if (source == DamageSource.FALLING_BLOCK)
                    targetID = -3;
                else if (source.getImmediateSource() != null)
                    targetID = source.getImmediateSource().getEntityId();
                
                TANetwork.INSTANCE.sendToAllTracking(new PacketFXShield(getEntityId(), targetID), this);
	        }
	        
	        return true;
        }
        
        return false;
    }
    
    @Override
    public boolean canBeCollidedWith() {
        if (world.isRemote && ThaumicAugmentation.proxy.isEntityClientPlayer(ownerRef.get()))
            return false;
        
        return super.canBeCollidedWith();
    }
    
    protected boolean entityBypassesShield(Entity entity) {
    	Entity owner = ownerRef.get();
        if (owner != null) {
            if (entity.equals(owner))
                return true;
            else if (entity instanceof IThrowableEntity && owner.equals(((IThrowableEntity) entity).getThrower()))
            	return true;
            else if (entity instanceof IEntityOwnable && owner.equals(((IEntityOwnable) entity).getOwner()))
                return true;
            else if (entity instanceof EntityThrowable && owner.equals(((EntityThrowable) entity).getThrower()))
                return true;
            else if (entity instanceof EntityArrow && owner.equals(((EntityArrow) entity).shootingEntity))
                return true;
            else if (entity instanceof EntityFireball && owner.equals(((EntityFireball) entity).shootingEntity))
                return true;
        }
        
        return false;
    }
    
    @Override
    protected void collideWithEntity(Entity entity) {
        if (!entityBypassesShield(entity)) {
        	super.collideWithEntity(entity);
        }
    }
    
    @Override
    public void onCollideWithPlayer(EntityPlayer entity) {
        Entity owner = getOwner();
        if (owner != null) {
            if (entity.equals(owner))
                return;
        }
        
        super.onCollideWithPlayer(entity);
    }
    
    @Override
    public void applyEntityCollision(Entity entity) {
    	if (!entityBypassesShield(entity)) {
    		super.applyEntityCollision(entity);
    	}
    }
    
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entity) {
    	return entityBypassesShield(entity) ? null : getEntityBoundingBox();
    }
    
    public void resetBoundingBoxes() {
        double heightOffset = Math.abs(rotationPitch) / 90.0;
        Vec3d pos1 = new Vec3d(-width / 2.0, -heightOffset, -0.0625);
        Vec3d pos2 = new Vec3d(width / 2.0, height - heightOffset, 0.0625);
        pos1 = pos1.rotatePitch((float) Math.toRadians(rotationPitch)).rotateYaw((float) Math.toRadians(rotationYaw)).add(posX, posY, posZ);
        pos2 = pos2.rotatePitch((float) Math.toRadians(rotationPitch)).rotateYaw((float) Math.toRadians(rotationYaw)).add(posX, posY, posZ);
        setEntityBoundingBox(new AxisAlignedBB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z));
    }
    
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        // TODO investigate selective shield collision without lag
        return null;//getEntityBoundingBox();
    }
    
    @Override
    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        prevPosX = lastTickPosX = posX;
        prevPosY = lastTickPosY = posY;
        prevPosZ = lastTickPosZ = posZ;
        prevRotationYaw = prevRotationYawHead = rotationYaw;
        prevRotationPitch = rotationPitch;
        posX = x;
        posY = y;
        posZ = z;
        setRotation(yaw, pitch);
        rotationYawHead = rotationYaw;
    }
    
    protected Vec3d getEntityLookVector(float yaw, float pitch) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d(f1 * f2, f3, f * f2);
    }
    
    @Override
    public void onLivingUpdate() {
        int lifespan = getTotalLifespan();
        if (lifespan > 0) {
            ++timeAlive;
            if (!world.isRemote && timeAlive > lifespan && isEntityAlive()) {
                attackEntityFrom(DamageSource.OUT_OF_WORLD, 100000.0F);
                return;
            }
        }
        
        if ((ownerRef.get() == null || ownerRef.get().isDead) && dataManager.get(OWNER_ID).isPresent()) {
            ownerRef.clear();
            List<Entity> entities = world.getEntities(Entity.class, entity -> entity != null && entity.getPersistentID().equals(dataManager.get(OWNER_ID).get()));
            if (!entities.isEmpty())
                ownerRef = new WeakReference<>(entities.get(0));
            else
                ++aloneTicks;
        }
        
        Entity owner = ownerRef.get();
        if (owner != null) {
            if (!owner.isEntityAlive()) {
                if (!world.isRemote)
                    setDead();
                else
                    ownerRef.clear();
            }
            else {
                if (getRotate())
                    setYawOffset(getYawOffset() + 2.0F);
                
                if (!world.isRemote) {
                    if (owner.dimension != dimension) {
                        changeDimension(owner.dimension);
                        return;
                    }
                    
                    float lYaw = 0.0F;
                    if (owner instanceof EntityLivingBase) {
                        EntityLivingBase living = (EntityLivingBase) owner;
                        lYaw = living.rotationYawHead + getYawOffset();
                    }
                    else
                        lYaw = owner.rotationYaw + getYawOffset();
                    
                    Vec3d lookVec = getEntityLookVector(lYaw, owner.rotationPitch).scale(1.5);
                    setLocationAndAngles(owner.posX + lookVec.x, owner.posY + lookVec.y + (owner.height / 1.5) - height / 2.0,
                            owner.posZ + lookVec.z, lYaw, owner.rotationPitch);
                    motionX = owner.motionX;
                    motionY = owner.motionY;
                    motionZ = owner.motionZ;
                    resetBoundingBoxes();
                    collideWithNearbyEntities();
                    if (canReflect()) {
                        for (EntityFocusCloud e : world.getEntitiesWithinAABB(EntityFocusCloud.class, owner.getEntityBoundingBox().grow(1.0))) {
                            damageEntity(DamageSource.causeIndirectDamage(e, e.getOwner()), getCloudPower(e) / 4.0F);
                            float radius = e.getRadius();
                            if (radius > 1.0F)
                                e.setRadius(radius - 1);
                            else
                                e.setDead();
                        }
                    }
                }
                else {
                    float pt = ThaumicAugmentation.proxy.getPartialTicks();
                    float lPitch = owner.prevRotationPitch + (owner.rotationPitch - owner.prevRotationPitch) * pt;
                    float lYaw = 0.0F;
                    if (owner instanceof EntityLivingBase) {
                        EntityLivingBase living = (EntityLivingBase) owner;
                        lYaw = living.prevRotationYawHead + (living.rotationYawHead - living.prevRotationYawHead) * pt + getYawOffset();
                    }
                    else
                        lYaw = owner.prevRotationYaw + (owner.rotationYaw - owner.prevRotationYaw) * pt + getYawOffset();
                    
                    Vec3d lookVec = getEntityLookVector(lYaw, lPitch).scale(1.5);
                    setLocationAndAngles(owner.posX + lookVec.x, owner.posY + lookVec.y + (owner.height / 1.5) - height / 2.0,
                            owner.posZ + lookVec.z, lYaw, lPitch);
                    motionX = owner.motionX;
                    motionY = owner.motionY;
                    motionZ = owner.motionZ;
                    resetBoundingBoxes();
                    
                    AxisAlignedBB box = getEntityBoundingBox();
                    double xDiff = box.maxX - box.minX;
                    double yDiff = box.maxY - box.minY;
                    double zDiff = box.maxZ - box.minZ;
                    int color = dataManager.get(COLOR);
                    float r = ((color >> 16) & 0xFF) / 255.0F;
                    float g = ((color >> 8) & 0xFF) / 255.0F;
                    float b = (color & 0xFF) / 255.0F;
                    for (int i = 0; i < 2; ++i) {
                        ThaumicAugmentation.proxy.getRenderHelper().renderFireMote(world, (float) (box.minX + rand.nextFloat() * xDiff), (float) (box.minY + rand.nextFloat() * yDiff),
                                (float) (box.minZ + rand.nextFloat() * zDiff), (float) motionX, 0.05F, (float) motionZ,
                                r, g, b, 0.35F, 1.5F);
                    }
                }
            }
        }
        else if (!world.isRemote && aloneTicks >= 20)
            setDead();
    }
    
    @Override
    protected void onDeathUpdate() {
        ++deathTime;
        if (this.deathTime == 20) {
            setDead();
            if (!world.isRemote) {
                TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.FIRE_EXPLOSION,
                        posX, posY, posZ, 1.0, dataManager.get(COLOR)), this);
            }
        }
    }
    
    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (key == OWNER_ID)
            ownerRef.clear();
    }
    
    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundsTC.craftfail;
    }
    
    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundsTC.runicShieldEffect;
    }
    
    @Override
    public SoundCategory getSoundCategory() {
        Entity owner = ownerRef.get();
        return owner != null ? owner.getSoundCategory() : super.getSoundCategory();
    }
    
    @Override
    protected float getSoundPitch() {
        return (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F;
    }
    
    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("timeAlive", timeAlive);
        compound.setInteger("lifespan", dataManager.get(LIFESPAN));
        compound.setBoolean("reflect", reflect);
        Entity owner = ownerRef.get();
        if (owner != null)
            compound.setUniqueId("owner", owner.getPersistentID());
        if (getCasterID() != null)
            compound.setUniqueId("caster", getCasterID());
        
        compound.setInteger("color", getColor());
        compound.setFloat("offset", getYawOffset());
        compound.setBoolean("rotate", getRotate());
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        timeAlive = compound.getInteger("timeAlive");
        dataManager.set(LIFESPAN, compound.getInteger("lifespan"));
        reflect = compound.getBoolean("reflect");
        dataManager.set(OWNER_ID, Optional.fromNullable(compound.getUniqueId("owner")));
        setCasterID(compound.getUniqueId("caster"));
        setColor(compound.getInteger("color"));
        setYawOffset(compound.getFloat("offset"));
        setRotate(compound.getBoolean("rotate"));
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {}

    @Override
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.RIGHT;
    }
    
    @Override
    public void move(MoverType type, double x, double y, double z) {
        if (type == MoverType.SELF)
            super.move(type, x, y, z);
    }
    
    @Override
    public boolean canBeHitWithPotion() {
        return false;
    }
    
    @Override
    public boolean canBePushed() {
        return isEntityAlive();
    }
    
    @Override
    protected boolean canBeRidden(Entity entityIn) {
        return false;
    }
    
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
    
    @Override
    protected boolean canDropLoot() {
        return false;
    }
    
    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return false;
    }
    
    @Override
    public boolean canRenderOnFire() {
        return false;
    }
    
    @Override
    public boolean canTrample(World world, Block block, BlockPos pos, float fallDistance) {
        return false;
    }
    
    @Override
    protected boolean canTriggerWalking() {
        return false;
    }
    
    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }
    
    @Override
    public boolean hasNoGravity() {
        return true;
    }
    
    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return false;
    }
    
    @Override
    public boolean isPotionApplicable(PotionEffect potioneffectIn) {
        return false;
    }
    
    @Override
    public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {}
    
    @Override
    public void setFire(int seconds) {}
    
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }
    
}
