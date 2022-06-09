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

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class EntityItemBlockRiftJar extends EntityItem {

    protected boolean ignoreDamage;
    
    public EntityItemBlockRiftJar(World world) {
        super(world);
    }
    
    public EntityItemBlockRiftJar(World world, double x, double y, double z) {
        super(world, x, y, z);
    }
    
    public EntityItemBlockRiftJar(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }
    
    protected void breakAndDoBadThings(int size, int seed) {
         
        List<EntityFluxRift> rifts = world.getEntitiesWithinAABB(EntityFluxRift.class, new AxisAlignedBB(posX, posY, posZ, 
                posX, posY, posZ).grow(32.0F));
        if (!rifts.isEmpty()) {
            for (EntityFluxRift rift : rifts) {
                AuraHelper.polluteAura(world, rift.getPosition(), (float) Math.sqrt(rift.getRiftSize()), true);
                rift.setDead();
            }
            
            EntityPrimalWisp wisp = new EntityPrimalWisp(world);
            wisp.setPositionAndRotation(posX + 0.5, posY + 0.5, posZ + 0.5,
                    world.rand.nextInt(360) - 180, 0.0F);
            wisp.rotationYawHead = wisp.rotationYaw;
            wisp.renderYawOffset = wisp.rotationYaw;
            wisp.onInitialSpawn(world.getDifficultyForLocation(wisp.getPosition()), null);
            if (world.spawnEntity(wisp)) {
                wisp.playSound(SoundEvents.BLOCK_GLASS_BREAK, 0.75F, 1.0F);
                wisp.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 0.75F);
                TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.EXPLOSION,
                        wisp.posX, wisp.posY, wisp.posZ), wisp);
            }
        }
        else {
            EntityFluxRift rift = new EntityFluxRift(world);
            rift.setPositionAndRotation(posX, posY, posZ, rotationYaw, rotationPitch);
            rift.setRiftSeed(seed);
            rift.setRiftSize(size);
            rift.setRiftStability(-50.0F);
            if (world.spawnEntity(rift)) {
                AuraHelper.polluteAura(world, rift.getPosition(), (float) Math.sqrt(size), true);
                rift.playSound(SoundEvents.BLOCK_GLASS_BREAK, 0.75F, 1.0F);
                rift.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 0.75F);
                TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.EXPLOSION,
                        rift.posX, rift.posY, rift.posZ), rift);
            }
        }
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        super.attackEntityFrom(source, amount);
        if (isDead && !ignoreDamage) {
            ItemStack stack = getItem();
            int size = stack.getTagCompound().getInteger("size");
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("seed", NBT.TAG_INT) && 
                    size > 0) {
                
                ignoreDamage = true;
                breakAndDoBadThings(size, stack.getTagCompound().getInteger("seed"));
            }
        }
        
        return false;
    }
    
    @Override
    protected void outOfWorld() {
        ItemStack stack = getItem();
        int size = stack.getTagCompound().getInteger("size");
        if (!ModConfig.CONFIG_MISC.wussMode && stack.hasTagCompound() && stack.getTagCompound().hasKey("seed", NBT.TAG_INT) && 
                size > 0) {
            
            AuraHelper.polluteAura(world, getPosition(), size, true);
        }
        
        super.outOfWorld();
    }
    
    @Override
    public void setDead() {
        if (!isDead) {
            StackTraceElement[] trace = new Throwable().getStackTrace();
            if (trace.length >= 2 && trace[1].getClassName().equals("thaumcraft.common.entities.EntityFluxRift")) {
                ItemStack stack = getItem();
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("seed", NBT.TAG_INT) && 
                        stack.getTagCompound().getInteger("size") > 0) {
                    
                    for (EntityFluxRift rift : world.getEntitiesWithinAABB(EntityFluxRift.class, getEntityBoundingBox().grow(1.0)))
                        rift.setDead();
                    
                    ignoreDamage = true;
                    world.createExplosion(null, posX, posY, posZ, 3.0F, false);
                    EntityPrimalWisp wisp = new EntityPrimalWisp(world);
                    wisp.setPositionAndRotation(posX, posY + height / 2.0F, posZ,
                            world.rand.nextInt(360) - 180, 0.0F);
                    wisp.rotationYawHead = wisp.rotationYaw;
                    wisp.renderYawOffset = wisp.rotationYaw;
                    wisp.onInitialSpawn(world.getDifficultyForLocation(getPosition()), null);
                    world.spawnEntity(wisp);
                }
            }
        }
        
        super.setDead();
    }
    
}
