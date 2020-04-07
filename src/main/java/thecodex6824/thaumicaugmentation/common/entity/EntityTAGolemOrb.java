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
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;

public class EntityTAGolemOrb extends EntityThrowable {

    protected static final DataParameter<Boolean> RED = EntityDataManager.createKey(EntityTAGolemOrb.class, DataSerializers.BOOLEAN);
    
    protected WeakReference<EntityLivingBase> target;
    protected UUID targetID;
    
    public EntityTAGolemOrb(World world) {
        super(world);
        target = new WeakReference<>(null);
        setSize(1.0F, 1.0F);
    }
    
    public EntityTAGolemOrb(World world, EntityLivingBase shooter, EntityLivingBase targetEntity, boolean red) {
        super(world, shooter);
        target = new WeakReference<>(targetEntity);
        dataManager.set(RED, red);
        setSize(1.0F, 1.0F);
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(RED, false);
    }
    
    public boolean isRed() {
        return dataManager.get(RED);
    }
    
    public void setRed(boolean red) {
        dataManager.set(RED, red);
    }
    
    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote && thrower != null && result.typeOfHit == Type.ENTITY) {
            result.entityHit.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, thrower),
                    (float) thrower.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * (isRed() ? 1.0F : 0.0F));
        }
        else if (world.isRemote)
            ThaumicAugmentation.proxy.getRenderHelper().renderBurst(world, posX, posY, posZ, 1.0F, 0xFFFFFF);
        
        if (!world.isRemote) {
            playSound(SoundsTC.shock, 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
            setDead();
        }
    }
    
    protected int getLifespan() {
        if (isRed())
            return Math.max(100, 120 * world.getDifficulty().getId());
        else
            return Math.max(60, 80 * world.getDifficulty().getId());
    }
    
    protected double getPull(double currentVelocity, double dd) {
        double pull = 0.125 * world.getDifficulty().getId();
        if (Math.signum(currentVelocity) != Math.signum(dd))
            pull *= 2;
        
        return pull;
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            if (targetID != null) {
                List<EntityLivingBase> found = world.getEntities(EntityLivingBase.class, e -> e != null && e.getUniqueID().equals(targetID));
                if (!found.isEmpty()) {
                    target = new WeakReference<>(found.get(0));
                    targetID = null;
                }
            }
            
            EntityLivingBase entity = target.get();
            if (ticksExisted > getLifespan())
                setDead();
            else if (entity != null) {
                if (!entity.isEntityAlive())
                    target.clear();
                else {
                    double dist = getDistanceSq(entity);
                    double dx = (entity.posX - posX) / dist;
                    double dy = (entity.posY + entity.getEyeHeight() - posY) / dist;
                    double dz = (entity.posZ - posZ) / dist;
                    motionX += dx * getPull(motionX, dx);
                    motionY += dy * getPull(motionY, dy);
                    motionZ += dz * getPull(motionZ, dz);
                    motionX = MathHelper.clamp(motionX, -0.3F, 0.3F);
                    motionY = MathHelper.clamp(motionY, -0.3F, 0.3F);
                    motionZ = MathHelper.clamp(motionZ, -0.3F, 0.3F);
                    markVelocityChanged();
                }
            }
        }
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source))
            return false;
        
        Entity hitter = source.getTrueSource();
        if (hitter == null)
            hitter = source.getImmediateSource();
        
        if (hitter != null) {
            Vec3d hit = hitter.getLookVec();
            motionX = hit.x * 0.9;
            motionY = hit.y * 0.9;
            motionZ = hit.z * 0.9;
            playSound(SoundsTC.zap, 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
            markVelocityChanged();
            return true;
        }
        
        return false;
    }
    
    @Override
    protected float getGravityVelocity() {
        return 0.0F;
    }
    
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        targetID = compound.getUniqueId("target");
        dataManager.set(RED, compound.getBoolean("red"));
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        EntityLivingBase t = target.get();
        if (t != null)
            compound.setUniqueId("target", t.getUniqueID());
        
        compound.setBoolean("red", dataManager.get(RED));
    }
    
}
