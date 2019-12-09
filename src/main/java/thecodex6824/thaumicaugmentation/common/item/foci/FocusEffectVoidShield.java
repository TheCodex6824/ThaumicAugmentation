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

package thecodex6824.thaumicaugmentation.common.item.foci;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.common.entity.EntityFocusShield;

public class FocusEffectVoidShield extends FocusEffect {

    protected class NodeSettingShieldHealth extends NodeSetting.NodeSettingIntRange {
        
        public NodeSettingShieldHealth() {
            super(5, 75);
        }
        
        @Override
        public int getDefault() {
            return 5;
        }
        
    }
    
    protected class NodeSettingReflectProjectiles extends NodeSetting {
        
        public NodeSettingReflectProjectiles() {
            super("reflect", "focus." + ThaumicAugmentationAPI.MODID + ".shield.reflect",
                    new NodeSetting.NodeSettingIntList(new int[] {0, 1}, new String[] {
                            "focus." + ThaumicAugmentationAPI.MODID + ".shield.reflect_no",
                            "focus." + ThaumicAugmentationAPI.MODID + ".shield.reflect_yes"
                    }), "FIRSTSTEPS");
        }
        
    }
    
    @Override
    public Aspect getAspect() {
        return Aspect.MAGIC;
    }
    
    @Override
    public int getComplexity() {
        return (int) ((getSettingValue("health") / 1.5)) - 2 + getSettingValue("reflect") * 15;
    }
    
    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[] {new NodeSetting("health", "focus." + ThaumicAugmentationAPI.MODID + ".shield.health", new NodeSettingShieldHealth(), "FIRSTSTEPS"),
                new NodeSettingReflectProjectiles()};
    }
    
    @Override
    public String getKey() {
        return "focus." + ThaumicAugmentationAPI.MODID + ".shield";
    }
    
    @Override
    public String getResearch() {
        return "FIRSTSTEPS";//return "FOCUS_SHIELD";
    }
    
    @Override
    public boolean execute(RayTraceResult result, @Nullable Trajectory trajectory, float finalPower, int whatever) {
        World world = getPackage().world;
        EntityLivingBase caster = getPackage().getCaster();
        if (caster.getActiveHand() != null) {
            ItemStack active = caster.getHeldItem(caster.getActiveHand());
            IImpetusStorage storage = active.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
            if (storage == null) {
                IAugmentableItem item = active.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                if (item != null) {
                    for (ItemStack stack : item.getAllAugments()) {
                        IImpetusStorage test = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                        if (test != null && test.canExtract() && test.getEnergyStored() >= TAConfig.shieldFocusImpetusCost.getValue()) {
                            storage = test;
                            break;
                        }
                    }
                }
            }
            
            if (!world.isRemote && result.typeOfHit == Type.ENTITY) {
                EntityFocusShield shield = null;
                if (result.entityHit instanceof EntityFocusShield)
                    shield = (EntityFocusShield) result.entityHit;
                else {
                    List<EntityFocusShield> shields = world.getEntitiesWithinAABB(EntityFocusShield.class, result.entityHit.getEntityBoundingBox().grow(1.5),
                            e -> e != null && result.entityHit.equals(e.getOwner()));
                    if (!shields.isEmpty())
                        shield = shields.get(0);
                }
                
                if (shield != null) {
                    if (!caster.isSneaking() && storage != null) {
                        double prop = Math.max(shield.getHealth() / shield.getMaxHealth(), (double) shield.getTimeAlive() / shield.getTotalLifespan());
                        if (ImpetusAPI.tryExtractFully(storage, (long) (prop * TAConfig.shieldFocusImpetusCost.getValue()))) {
                            shield.setHealth(shield.getMaxHealth());
                            shield.resetTimeAlive();
                            caster.world.playSound(null, caster.getPosition().up(), SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 
                                    SoundCategory.PLAYERS, 0.2F, 1.2F);
                            return true;
                        }
                        else {
                            caster.world.playSound(null, caster.getPosition().up(), SoundsTC.jacobs, 
                                    SoundCategory.PLAYERS, 0.2F, 0.6F);
                            
                            return false;
                        }
                    }
                    else if (caster.isSneaking() &&
                            caster.getUniqueID().equals(shield.getCasterID()) || caster.getUniqueID().equals(shield.getOwnerId())) {
                        
                        shield.setDead();
                        caster.world.playSound(null, caster.getPosition().up(), SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 
                                SoundCategory.PLAYERS, 0.2F, 0.75F);
                        return true;
                    }
                }
                else if (storage != null) {
                    if (ImpetusAPI.tryExtractFully(storage, TAConfig.shieldFocusImpetusCost.getValue())) {
                        shield = new EntityFocusShield(world);
                        shield.setOwner(result.entityHit);
                        shield.setCasterID(caster.getUniqueID());
                        shield.setMaxHealth(getSettingValue("health"));
                        shield.setHealth(shield.getMaxHealth());
                        shield.setReflect(getSettingValue("reflect") != 0);
                        caster.world.playSound(null, caster.getPosition().up(), SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 
                                SoundCategory.PLAYERS, 0.2F, 1.2F);
                        return world.spawnEntity(shield);
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void renderParticleFX(World world, double posX, double posY, double posZ, double velX, double velY,
            double velZ) {

        FXGeneric fb = new FXGeneric(world, posX, posY, posZ, velX, velY, velZ);
        fb.setMaxAge(40 + world.rand.nextInt(40));
        fb.setParticles(16, 1, 1);
        fb.setSlowDown(0.5D);
        fb.setAlphaF(new float[] { 1.0F, 0.0F });
        fb.setScale(new float[] { (float)(0.699999988079071D + world.rand.nextGaussian() * 0.30000001192092896D) });
        int color = 0x5000C8;
        fb.setRBGColorF(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
        fb.setRotationSpeed(world.rand.nextFloat(), 0.0F);
        ParticleEngine.addEffectWithDelay(world, fb, 0);
    }
    
}
