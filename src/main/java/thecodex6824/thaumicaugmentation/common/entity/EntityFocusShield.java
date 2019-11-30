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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.entity.ICastedEntity;

public class EntityFocusShield extends EntityLivingBase implements IEntityOwnable, ICastedEntity {

    protected static final DataParameter<Optional<UUID>> OWNER_ID = EntityDataManager.createKey(EntityFocusShield.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Optional<UUID>> CASTER_ID = EntityDataManager.createKey(EntityFocusShield.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    
    protected int timeAlive;
    protected int totalLifespan;
    protected boolean reflect;
    protected int aloneTicks;
    protected WeakReference<Entity> ownerRef;
    
    public EntityFocusShield(World world) {
        super(world);
        ownerRef = new WeakReference<>(null);
        maxHurtResistantTime = 0;
        setSize(1.8F, 2.0F);
        setMaxHealth(5.0F);
        setHealth(getMaxHealth());
    }
    
    public void setMaxHealth(float newMax) {
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(newMax);
        totalLifespan = (int) (newMax * 200);
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OWNER_ID, Optional.absent());
        dataManager.register(CASTER_ID, Optional.absent());
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
    
    public void setOwner(Entity newOwner) {
        dataManager.set(OWNER_ID, Optional.of(newOwner.getPersistentID()));
        ownerRef = new WeakReference<>(newOwner);
        Vec3d lookVec = newOwner.getLookVec();
        lookVec = lookVec.scale(1.75);
        setLocationAndAngles(newOwner.posX + lookVec.x, newOwner.posY + lookVec.y + 0.07, newOwner.posZ + lookVec.z, newOwner.getRotationYawHead(), newOwner.rotationPitch);
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
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.FALL)
            return false;
        else if (reflect && !world.isRemote) {
            Entity entity = source.getImmediateSource();
            if (entity instanceof IProjectile || entity instanceof EntityFireball) {
                Entity newEntity = EntityList.newEntity(entity.getClass(), entity.world);
                if (newEntity != null) {
                    newEntity.setLocationAndAngles(entity.posX - entity.motionX, entity.posY - entity.motionY, entity.posZ - entity.motionZ, -entity.rotationYaw, -entity.rotationPitch);
                    newEntity.motionX = -entity.motionX;
                    newEntity.motionY = -entity.motionY;
                    newEntity.motionZ = -entity.motionZ;
                    if (entity instanceof EntityFireball && newEntity instanceof EntityFireball) {
                        EntityFireball original = (EntityFireball) entity;
                        EntityFireball fireball = (EntityFireball) newEntity;
                        fireball.accelerationX = -original.accelerationX;
                        fireball.accelerationY = -original.accelerationY;
                        fireball.accelerationZ = -original.accelerationZ;
                    }
                    newEntity.velocityChanged = true;
                    newEntity.world.spawnEntity(newEntity);
                    Entity owner = ownerRef.get();
                    if (owner != null) {
                        if (newEntity instanceof EntityThrowable && owner instanceof EntityLivingBase)
                            ((EntityThrowable) newEntity).thrower = (EntityLivingBase) owner;
                        else if (newEntity instanceof EntityArrow)
                            ((EntityArrow) newEntity).shootingEntity = owner;
                        else if (newEntity instanceof EntityFireball && owner instanceof EntityLivingBase)
                            ((EntityFireball) newEntity).shootingEntity = (EntityLivingBase) owner;
                    }
                }
            }
        }
        
        return super.attackEntityFrom(source, amount);
    }
    
    @Override
    public boolean canBeCollidedWith() {
        // I hate this
        if (world.isRemote && Minecraft.getMinecraft().player.equals(ownerRef.get())) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            if (trace.length >= 3 && trace[2].getClassName().equals("net.minecraft.client.renderer.EntityRenderer$1"))
                return false;
        }
        
        return super.canBeCollidedWith();
    }
    
    @Override
    protected void collideWithEntity(Entity entity) {
        Entity owner = ownerRef.get();
        if (owner != null) {
            if (entity.equals(owner) || (world.isRemote && entity.equals(Minecraft.getMinecraft().getRenderViewEntity())))
                return;
            else if (entity instanceof EntityThrowable && owner.equals(((EntityThrowable) entity).getThrower()))
                return;
            else if (entity instanceof EntityArrow && owner.equals(((EntityArrow) entity).shootingEntity))
                return;
            else if (entity instanceof EntityFireball && owner.equals(((EntityFireball) entity).shootingEntity))
                return;
        }
        
        super.collideWithEntity(entity);
    }
    
    @Override
    public void onCollideWithPlayer(EntityPlayer entity) {
        Entity owner = ownerRef.get();
        if (owner != null) {
            if (entity.equals(owner) || (world.isRemote && entity.equals(Minecraft.getMinecraft().getRenderViewEntity())))
                return;
        }
        
        super.onCollideWithPlayer(entity);
    }
    
    @Override
    public void applyEntityCollision(Entity entity) {
        Entity owner = ownerRef.get();
        if (owner != null) {
            if (entity.equals(owner) || (world.isRemote && entity.equals(Minecraft.getMinecraft().getRenderViewEntity())))
                return;
            else if (entity instanceof EntityThrowable && owner.equals(((EntityThrowable) entity).getThrower()))
                return;
            else if (entity instanceof EntityArrow && owner.equals(((EntityArrow) entity).shootingEntity))
                return;
            else if (entity instanceof EntityFireball && owner.equals(((EntityFireball) entity).shootingEntity))
                return;
        }
        
        super.applyEntityCollision(entity);
    }
    
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entity) {
        Entity owner = ownerRef.get();
        if (owner != null) {
            if (entity.equals(owner) || (world.isRemote && entity.equals(Minecraft.getMinecraft().getRenderViewEntity())))
                return null;
            else if (entity instanceof EntityThrowable && owner.equals(((EntityThrowable) entity).getThrower()))
                return null;
            else if (entity instanceof EntityArrow && owner.equals(((EntityArrow) entity).shootingEntity))
                return null;
            else if (entity instanceof EntityFireball && owner.equals(((EntityFireball) entity).shootingEntity))
                return null;
        }
        
        return getEntityBoundingBox();
    }
    
    protected void resetBoundingBoxes() {
        double heightOffset = Math.abs(rotationPitch) / 90.0 * 0.9;
        Vec3d pos1 = new Vec3d(-1, -heightOffset, 0);
        Vec3d pos2 = new Vec3d(1, 2 - heightOffset, 0.125);
        pos1 = pos1.rotatePitch((float) Math.toRadians(rotationPitch)).rotateYaw((float) Math.toRadians(rotationYaw)).add(posX, posY, posZ);
        pos2 = pos2.rotatePitch((float) Math.toRadians(rotationPitch)).rotateYaw((float) Math.toRadians(rotationYaw)).add(posX, posY, posZ);
        setEntityBoundingBox(new AxisAlignedBB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z));
    }
    
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }
    
    @Override
    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        prevPosX = lastTickPosX = posX;
        prevPosY = lastTickPosY = posY;
        prevPosZ = lastTickPosZ = posZ;
        posX = x;
        posY = y;
        posZ = z;
        rotationYaw = yaw;
        rotationPitch = pitch;
        setPosition(posX, posY, posZ);
    }
    
    @Override
    public void onLivingUpdate() {
        ++timeAlive;
        if (timeAlive > totalLifespan && isEntityAlive()) {
            setHealth(0);
            return;
        }
        
        if (ownerRef.get() == null && dataManager.get(OWNER_ID).isPresent()) {
            if (ownerRef.get() == null) {
                List<Entity> entities = world.getEntities(Entity.class, entity -> entity != null && entity.getPersistentID().equals(dataManager.get(OWNER_ID).get()));
                if (!entities.isEmpty())
                    ownerRef = new WeakReference<>(entities.get(0));
                else
                    ++aloneTicks;
            }
        }
        
        Entity owner = ownerRef.get();
        if (owner != null) {
            if (!owner.isEntityAlive())
                setDead();
            else {
                if (!world.isRemote) {
                    if (owner.dimension != dimension) {
                        changeDimension(owner.dimension);
                        return;
                    }
                    
                    Vec3d lookVec = owner.getLookVec();
                    lookVec = lookVec.scale(1.75);
                    setLocationAndAngles(owner.posX + lookVec.x, owner.posY + lookVec.y + 0.1, owner.posZ + lookVec.z, owner.getRotationYawHead(), owner.rotationPitch);
                    motionX = owner.motionX;
                    motionY = owner.motionY;
                    motionZ = owner.motionZ;
                }
                else {
                    Vec3d lookVec = owner.getLook(Minecraft.getMinecraft().getRenderPartialTicks());
                    lookVec = lookVec.scale(1.75);
                    setLocationAndAngles(owner.posX + lookVec.x, owner.posY + lookVec.y + 0.1, owner.posZ + lookVec.z, owner.getRotationYawHead(), owner.rotationPitch);
                    motionX = owner.motionX;
                    motionY = owner.motionY;
                    motionZ = owner.motionZ;
                }
                
                resetBoundingBoxes();
                collideWithNearbyEntities();
                if (world.isRemote) {
                    AxisAlignedBB box = getEntityBoundingBox();
                    double xDiff = box.maxX - box.minX;
                    double yDiff = box.maxY - box.minY;
                    double zDiff = box.maxZ - box.minZ;
                    for (int i = 0; i < 2; ++i) {
                        FXDispatcher.INSTANCE.drawFireMote((float) (box.minX + rand.nextFloat() * xDiff), (float) (box.minY + rand.nextFloat() * yDiff),
                                (float) (box.minZ + rand.nextFloat() * zDiff), (float) motionX, 0.05F, (float) motionZ,
                                1.0F, 0.0F, 1.0F, 0.35F, 1.5F);
                    }
                }
            }
        }
        else if (aloneTicks >= 10)
            setDead();
    }
    
    @Override
    protected void onDeathUpdate() {
        ++deathTime;
        if (this.deathTime == 20) {
            setDead();
            if (world.isRemote) {
                AxisAlignedBB box = getEntityBoundingBox();
                double xDiff = box.maxX - box.minX;
                double yDiff = box.maxY - box.minY;
                double zDiff = box.maxZ - box.minZ;
                for (int i = 0; i < 20; ++i) {
                    double vX = this.rand.nextGaussian() * 0.02D;
                    double vY = this.rand.nextGaussian() * 0.02D;
                    double vZ = this.rand.nextGaussian() * 0.02D;
                    FXDispatcher.INSTANCE.drawFireMote((float) (box.minX + rand.nextFloat() * xDiff), (float) (box.minY + rand.nextFloat() * yDiff),
                            (float) (box.minZ + rand.nextFloat() * zDiff), (float) vX, (float) vY, (float) vZ,
                            1.0F, 0.0F, 1.0F, 0.55F, 1.0F);
                }
            }
        }
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
        return rand.nextFloat() + 0.25F;
    }
    
    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("timeAlive", timeAlive);
        compound.setInteger("lifespan", totalLifespan);
        compound.setBoolean("reflect", reflect);
        Entity owner = ownerRef.get();
        if (owner != null)
            compound.setUniqueId("owner", owner.getPersistentID());
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        timeAlive = compound.getInteger("timeAlive");
        totalLifespan = compound.getInteger("lifespan");
        reflect = compound.getBoolean("reflect");
        dataManager.set(OWNER_ID, Optional.of(compound.getUniqueId("owner")));
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
    public boolean canExplosionDestroyBlock(Explosion explosionIn, World worldIn, BlockPos pos,
            IBlockState blockStateIn, float p_174816_5_) {
        // TODO Auto-generated method stub
        return super.canExplosionDestroyBlock(explosionIn, worldIn, pos, blockStateIn, p_174816_5_);
    }
    
    @Override
    public float getExplosionResistance(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn) {
        // TODO Auto-generated method stub
        return super.getExplosionResistance(explosionIn, worldIn, pos, blockStateIn);
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
