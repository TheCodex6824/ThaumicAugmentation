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
import net.minecraft.entity.projectile.EntityArrow;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityFocusShield extends EntityLivingBase implements IEntityOwnable {

    protected static final DataParameter<Optional<UUID>> OWNER_ID = EntityDataManager.createKey(EntityFocusShield.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    
    protected int lifespan;
    protected boolean reflect;
    protected boolean firstUpdate;
    protected WeakReference<Entity> ownerRef;
    
    public EntityFocusShield(World world) {
        super(world);
        ownerRef = new WeakReference<>(null);
        maxHurtResistantTime = 0;
        setSize(2.0F, 3.0F);
        setHealth(getMaxHealth());
        firstUpdate = true;
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(5.0F);
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OWNER_ID, Optional.absent());
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
        lookVec = lookVec.scale(1.5);
        setLocationAndAngles(newOwner.posX + lookVec.x, newOwner.posY + lookVec.y, newOwner.posZ + lookVec.z, newOwner.rotationYaw, newOwner.rotationPitch);
    }
    
    public void setReflect(boolean ref) {
        reflect = ref;
    }
    
    public boolean canReflect() {
        return reflect;
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (reflect && !world.isRemote) {
            Entity entity = source.getImmediateSource();
            if (entity instanceof IProjectile) {
                Entity newProjectile = EntityList.newEntity(entity.getClass(), entity.world);
                newProjectile.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, -entity.rotationYaw, -entity.rotationPitch);
                newProjectile.motionX = -entity.motionX;
                newProjectile.motionY = -entity.motionY;
                newProjectile.motionZ = -entity.motionZ;
                newProjectile.velocityChanged = true;
                newProjectile.world.spawnEntity(newProjectile);
            }
        }
        
        return super.attackEntityFrom(source, amount);
    }
    
    @Override
    protected void collideWithEntity(Entity entity) {
        Entity owner = ownerRef.get();
        if (owner != null) {
            if (entity.equals(owner))
                return;
            else if (entity instanceof EntityThrowable && owner.equals(((EntityThrowable) entity).getThrower()))
                return;
            else if (entity instanceof EntityArrow && owner.equals(((EntityArrow) entity).shootingEntity))
                return;
        }
        
        super.collideWithEntity(entity);
    }
    
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (firstUpdate || (ownerRef.get() == null && dataManager.get(OWNER_ID).isPresent())) {
            if (ownerRef.get() == null) {
                List<Entity> entities = world.getEntities(Entity.class, entity -> entity.getPersistentID().equals(dataManager.get(OWNER_ID).get()));
                if (!entities.isEmpty())
                    ownerRef = new WeakReference<>(entities.get(0));
            }
            
            firstUpdate = false;
        }
        
        Entity owner = ownerRef.get();
        if (owner != null) {
            if (!world.isRemote) {
                if (owner.dimension != dimension) {
                    changeDimension(owner.dimension);
                    return;
                }
                
                Vec3d lookVec = owner.getLookVec();
                lookVec = lookVec.scale(1.5);
                setLocationAndAngles(owner.posX + lookVec.x, owner.posY + lookVec.y, owner.posZ + lookVec.z, owner.rotationYaw, owner.rotationPitch);
            }
            else {
                Vec3d lookVec = owner.getLook(Minecraft.getMinecraft().getRenderPartialTicks());
                lookVec = lookVec.scale(1.5);
                setLocationAndAngles(owner.posX + lookVec.x, owner.posY + lookVec.y, owner.posZ + lookVec.z, owner.rotationYaw, owner.rotationPitch);
            }
        }
        else if (owner == null || !owner.isEntityAlive())
            setDead();
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("lifespan", lifespan);
        compound.setBoolean("reflect", reflect);
        Entity owner = ownerRef.get();
        if (owner != null)
            compound.setUniqueId("owner", owner.getPersistentID());
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        lifespan = compound.getInteger("lifespan");
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
    
}
