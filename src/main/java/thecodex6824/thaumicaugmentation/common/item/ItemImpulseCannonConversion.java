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

package thecodex6824.thaumicaugmentation.common.item;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.*;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.impl.impulsecannon.IImpulseCannonAugment;
import thecodex6824.thaumicaugmentation.api.augment.impl.impulsecannon.IImpulseCannonConversion;
import thecodex6824.thaumicaugmentation.api.augment.impl.impulsecannon.IImpulseCannonRaytraceOverridingAugment;
import thecodex6824.thaumicaugmentation.api.entity.IImpulseSpecialEntity;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.util.RaytraceHelper;
import thecodex6824.thaumicaugmentation.common.capability.provider.SimpleCapabilityProviderNoSave;
import thecodex6824.thaumicaugmentation.common.event.ScheduledTaskHandler;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.network.*;
import thecodex6824.thaumicaugmentation.common.network.PacketRecoil.RecoilType;

public class ItemImpulseCannonConversion extends ItemTABase {
    public static final ModelResourceLocation LENS_BEAM = new ModelResourceLocation(ThaumicAugmentationAPI.MODID + ":impulse_cannon_beam", "inventory");
    public static final ModelResourceLocation LENS_RAILGUN = new ModelResourceLocation(ThaumicAugmentationAPI.MODID + ":impulse_cannon_railgun", "inventory");
    public static final ModelResourceLocation LENS_BURST = new ModelResourceLocation(ThaumicAugmentationAPI.MODID + ":impulse_cannon_burst", "inventory");

    protected final IImpulseCannonConversion[] conversions;
    
    public ItemImpulseCannonConversion() {
        super("railgun", "burst", "crystal", "recurse");
        conversions = new IImpulseCannonConversion[subItemNames.length];
        setMaxStackSize(1);
        setHasSubtypes(true);
        // railgun
        conversions[0] = new IImpulseCannonConversion() {

            @Override
            public @Nonnull ModelResourceLocation getLensModel() {
                return LENS_RAILGUN;
            }

            @Override
            public int getMaxUsageDuration(ItemStack cannonStack, ItemStack augmentStack) {
                return 1;
            }

            @Override
            public boolean isTickable(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user) {
                return false;
            }

            @Override
            public long getImpetusCostPerUsage(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer) {
                return TAConfig.cannonRailgunCost.getValue();
            }

            @Override
            public void onCannonUsage(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, long impetusConsumed, IImpetusStorage buffer, Map<ItemStack, IImpulseCannonAugment> augments) {
                Vec3d origin = user.getPositionEyes(1);
                Vec3d scan = user.getLookVec().scale(TAConfig.cannonRailgunRange.getValue());
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    if (aug.getValue() instanceof IImpulseCannonRaytraceOverridingAugment) {
                        scan = ((IImpulseCannonRaytraceOverridingAugment) aug.getValue())
                                .overrideFiringRayTrace(cannonStack, aug.getKey(), user, origin, scan);
                        break;
                    }
                }
                float baseDamage = TAConfig.cannonRailgunDamage.getValue();
                float magicFactor = 0.5f;
                float normalFactor = 0.5f;
                long cost = getImpetusCostPerUsage(cannonStack, augmentStack, user, buffer);
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    baseDamage *= aug.getValue().getBaseDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, impetusConsumed);
                    magicFactor *= aug.getValue().getMagicDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, impetusConsumed);
                    normalFactor *= aug.getValue().getNormalDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, impetusConsumed);
                }
                scan = RaytraceHelper.shortenRaytraceByBlocks(user.getEntityWorld(), origin, origin.add(scan));
                List<Entity> ents = RaytraceHelper.raytraceEntities(user.getEntityWorld(), origin, scan);
                for (Entity e : ents) {
                    if (e == user) continue;
                    if (!(e instanceof IImpulseSpecialEntity) || !((IImpulseSpecialEntity) e).shouldImpulseCannonIgnore(user)) {
                        for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                            aug.getValue().applyAdditionalEffectsToEntity(cannonStack, aug.getKey(), user, origin, scan, e, baseDamage);
                        }
                        ImpetusAPI.causeImpetusDamage(user, e, baseDamage * magicFactor, baseDamage * normalFactor);
                        if (e instanceof IImpulseSpecialEntity && ((IImpulseSpecialEntity) e).shouldStopRailgunBeam(user)) {
                            // make the beam terminate near the beam blocking entity
                            scan = scan.subtract(origin);
                            double scale = Math.sqrt(origin.squareDistanceTo(e.posX, e.posY, e.posZ) / scan.lengthSquared());
                            if (scale < 1) scan = scan.scale(scale);
                            scan = origin.add(scan);
                            break;
                        }
                    }
                }
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    aug.getValue().applyAdditionalEffects(cannonStack, aug.getKey(), user, origin, scan, baseDamage);
                }

                Random rand = user.getRNG();
                user.getEntityWorld().playSound(null, new BlockPos(user.getPositionEyes(1.0F)), TASounds.IMPULSE_CANNON_RAILGUN,
                        SoundCategory.PLAYERS, 1.0F, (rand.nextFloat() - rand.nextFloat()) / 2.0F + 1.0F);
                PacketImpulseRailgunProjectile packet = new PacketImpulseRailgunProjectile(user.getEntityId(), scan);
                PacketRecoil recoil = new PacketRecoil(user.getEntityId(), RecoilType.IMPULSE_RAILGUN);
                TANetwork.INSTANCE.sendToAllTracking(packet, user);
                TANetwork.INSTANCE.sendToAllTracking(recoil, user);
                if (user instanceof EntityPlayer) {
                    handleCooldown(user, TAConfig.cannonRailgunCooldown.getValue());
                    if (user instanceof EntityPlayerMP) {
                        TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) user);
                        TANetwork.INSTANCE.sendTo(recoil, (EntityPlayerMP) user);
                    }
                }
            }
        };
        // burst
        conversions[1] = new IImpulseCannonConversion() {

            @Override
            public @Nonnull ModelResourceLocation getLensModel() {
                return LENS_BURST;
            }

            @Override
            public int getMaxUsageDuration(ItemStack cannonStack, ItemStack augmentStack) {
                return 5;
            }

            @Override
            public boolean isTickable(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user) {
                return false;
            }

            @Override
            public long getImpetusCostPerUsage(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer) {
                return TAConfig.cannonBurstCost.getValue();
            }

            private void tick(ItemStack cannonStack, EntityLivingBase user, Map<ItemStack, IImpulseCannonAugment> augments, float baseDamage,
                              float magicFactor, float normalFactor, int num) {
                int c = TAConfig.cannonBurstCount.getValue() - 1;
                Vec3d origin = user.getPositionEyes(1);
                Vec3d scan = user.getLookVec().scale(TAConfig.cannonBurstRange.getValue());
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    if (aug.getValue() instanceof IImpulseCannonRaytraceOverridingAugment) {
                        scan = ((IImpulseCannonRaytraceOverridingAugment) aug.getValue())
                                .overrideFiringRayTrace(cannonStack, aug.getKey(), user, origin, scan);
                        break;
                    }
                }
                scan = RaytraceHelper.shortenRaytraceByBlocks(user.getEntityWorld(), origin, origin.add(scan));
                Entity e = null;
                for (Entity entity : RaytraceHelper.raytraceEntities(user.getEntityWorld(), origin, scan)) {
                    if (entity != user) {
                        e = entity;
                        break;
                    }
                }
                if (e != null && (!(e instanceof IImpulseSpecialEntity) || !((IImpulseSpecialEntity) e).shouldImpulseCannonIgnore(user))) {
                    for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                        aug.getValue().applyAdditionalEffectsToEntity(cannonStack, aug.getKey(), user, origin, scan, e, baseDamage);
                    }
                    if (ImpetusAPI.causeImpetusDamage(user, e, baseDamage * magicFactor, baseDamage * normalFactor)
                            && num < c && e instanceof EntityLivingBase) {
                        EntityLivingBase base = (EntityLivingBase) e;
                        base.hurtResistantTime = Math.min(base.hurtResistantTime, 1);
                        base.lastDamage = 0.0F;
                    }
                }
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    aug.getValue().applyAdditionalEffects(cannonStack, aug.getKey(), user, origin, scan, baseDamage);
                }

                Random rand = user.getRNG();
                user.getEntityWorld().playSound(null, new BlockPos(user.getPositionEyes(1.0F)), TASounds.IMPULSE_CANNON_BURST,
                        SoundCategory.PLAYERS, 1.0F, (rand.nextFloat() - rand.nextFloat()) / 2.0F + 1.0F);
                PacketImpulseBurst packet = new PacketImpulseBurst(user.getEntityId(), scan, num);
                TANetwork.INSTANCE.sendToAllTracking(packet, user);
                if (user instanceof EntityPlayerMP)
                    TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) user);

                if (num < c)
                    ScheduledTaskHandler.registerTask(() -> tick(cannonStack, user, augments, baseDamage, magicFactor, normalFactor, num + 1), TAConfig.cannonBurstDelay.getValue());
            }

            @Override
            public void onCannonUsage(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, long impetusConsumed, IImpetusStorage buffer, Map<ItemStack, IImpulseCannonAugment> augments) {
                float baseDamage = TAConfig.cannonBurstDamage.getValue();
                float magicFactor = 0.5f;
                float normalFactor = 0.5f;
                long cost = getImpetusCostPerUsage(cannonStack, augmentStack, user, buffer);
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    baseDamage *= aug.getValue().getBaseDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, impetusConsumed);
                    magicFactor *= aug.getValue().getMagicDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, impetusConsumed);
                    normalFactor *= aug.getValue().getNormalDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, impetusConsumed);
                }
                float finalBaseDamage = baseDamage;
                float finalMagicFactor = magicFactor;
                float finalNormalFactor = normalFactor;
                ScheduledTaskHandler.registerTask(() -> tick(cannonStack, user, augments, finalBaseDamage, finalMagicFactor, finalNormalFactor, 0), 0);
                PacketRecoil recoil = new PacketRecoil(user.getEntityId(), RecoilType.IMPULSE_BURST);
                TANetwork.INSTANCE.sendToAllTracking(recoil, user);
                if (user instanceof EntityPlayer) {
                    handleCooldown(user, TAConfig.cannonBurstCooldown.getValue());
                    if (user instanceof EntityPlayerMP)
                        TANetwork.INSTANCE.sendTo(recoil, (EntityPlayerMP) user);
                }
            }
        };
        // crystal
        conversions[2] = new IImpulseCannonConversion() {

            private @Nullable BeamInformation lastInformation;

            @Override
            public boolean isTickable(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user) {
                return true;
            }

            @Override
            public long getImpetusCostPerUsage(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer) {
                return TAConfig.cannonCrystalCostInitial.getValue();
            }

            @Override
            public double getImpetusCostPerTick(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, int useTicksLeft, IImpetusStorage buffer) {
                return TAConfig.cannonCrystalCostTick.getValue();
            }

            @Override
            public void onCannonUsage(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, long impetusConsumed, IImpetusStorage buffer, Map<ItemStack, IImpulseCannonAugment> augments) {
                user.getEntityWorld().playSound(null, new BlockPos(user.getPositionEyes(1.0F)), TASounds.IMPULSE_CANNON_BEAM_START,
                        SoundCategory.PLAYERS, 1.0F, (user.getRNG().nextFloat() - user.getRNG().nextFloat()) / 4.0F + 1.0F);
            }

            @Override
            public void onCannonTick(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, int useTicksLeft, double impetusConsumed, IImpetusStorage buffer, Map<ItemStack, IImpulseCannonAugment> augments) {
                final int beamCount = TAConfig.cannonCrystalBeamCount.getValue();
                if (lastInformation == null || user.ticksExisted % 20 == 0 || lastInformation.numBeams() != beamCount) {
                    float spread = TAConfig.cannonCrystalSpread.getValue();
                    double length = TAConfig.cannonCrystalRange.getValue();
                    Vector3f[] rots = new Vector3f[beamCount];
                    for (int i = 0; i < beamCount; i++) {
                        double s = Math.cos(Math.toRadians(spread));
                        // sample uniformly from s to 1
                        double z = (user.getRNG().nextFloat() * (1 - s)) + s;
                        // sample uniformly from 0 to 2pi
                        double phi = user.getRNG().nextFloat() * 2 * Math.PI;
                        rots[i] = new Vector3f((float) (Math.sqrt(1 - (z * z)) * Math.cos(phi)),
                                (float) (Math.sqrt(1 - (z * z)) * Math.sin(phi)), (float) z);
                    }
                    lastInformation = new BeamInformation(rots, length);
                    PacketImpulseBeam packet = new PacketImpulseBeam(user.getEntityId(), lastInformation);
                    TANetwork.INSTANCE.sendToAllTracking(packet, user);
                    if (user instanceof EntityPlayerMP)
                        TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) user);
                }
                float baseDamage = TAConfig.cannonCrystalDamage.getValue();
                float magicFactor = 0.5f;
                float normalFactor = 0.5f;
                double cost = getImpetusCostPerTick(cannonStack, augmentStack, user, useTicksLeft, buffer);
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    baseDamage *= aug.getValue().getBaseDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, impetusConsumed);
                    magicFactor *= aug.getValue().getMagicDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, impetusConsumed);
                    normalFactor *= aug.getValue().getNormalDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, impetusConsumed);
                }
                for (int i = 0; i < lastInformation.numBeams(); i++) {
                    Vec3d origin = user.getPositionEyes(1);
                    Vec3d scan = lastInformation.transform(i, user.getLookVec().scale(TAConfig.cannonBeamRange.getValue()));
                    for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                        if (aug.getValue() instanceof IImpulseCannonRaytraceOverridingAugment) {
                            scan = ((IImpulseCannonRaytraceOverridingAugment) aug.getValue())
                                    .overrideFiringRayTrace(cannonStack, aug.getKey(), user, origin, scan);
                            break;
                        }
                    }
                    scan = RaytraceHelper.shortenRaytraceByBlocks(user.getEntityWorld(), origin, origin.add(scan));
                    Entity e = null;
                    for (Entity entity : RaytraceHelper.raytraceEntities(user.getEntityWorld(), origin, scan)) {
                        if (entity != user) {
                            e = entity;
                            break;
                        }
                    }
                    if (e != null && (!(e instanceof IImpulseSpecialEntity) || !((IImpulseSpecialEntity) e).shouldImpulseCannonIgnore(user))) {
                        for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                            aug.getValue().applyAdditionalEffectsToEntity(cannonStack, aug.getKey(), user, origin, scan, e, baseDamage);
                        }
                        ImpetusAPI.causeImpetusDamage(user, e, baseDamage * magicFactor, baseDamage * normalFactor);
                        if (e instanceof EntityLivingBase) {
                            EntityLivingBase base = (EntityLivingBase) e;
                            base.hurtResistantTime = Math.min(base.hurtResistantTime, 2);
                            base.lastDamage = 0.0F;
                        }
                    }
                    for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                        aug.getValue().applyAdditionalEffects(cannonStack, aug.getKey(), user, origin, scan, baseDamage);
                    }
                }
            }

            @Override
            public void onStopCannonTick(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, int useTicksLeft, IImpetusStorage buffer, Map<ItemStack, IImpulseCannonAugment> augments) {
                lastInformation = null;
                Random rand = user.getRNG();
                user.getEntityWorld().playSound(null, new BlockPos(user.getPositionEyes(1.0F)), TASounds.IMPULSE_CANNON_BEAM_END,
                        SoundCategory.PLAYERS, (rand.nextFloat() - rand.nextFloat()) / 2.0F + 0.5F, (rand.nextFloat() - rand.nextFloat()) / 4.0F + 1.0F);
                PacketImpulseBeam packet = new PacketImpulseBeam(user.getEntityId(), null);
                TANetwork.INSTANCE.sendToAllTracking(packet, user);
                if (user instanceof EntityPlayerMP)
                    TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) user);
            }
        };
        // recurse
        conversions[3] = new IImpulseCannonConversion() {

            @Override
            public boolean isTickable(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user) {
                return true;
            }

            @Override
            public long getImpetusCostPerUsage(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer) {
                return TAConfig.cannonRecurseCostInitial.getValue();
            }

            @Override
            public double getImpetusCostPerTick(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, int useTicksLeft, IImpetusStorage buffer) {
                return TAConfig.cannonRecurseCostTick.getValue();
            }

            @Override
            public void onCannonTick(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, int useTicksLeft, double impetusConsumed, IImpetusStorage buffer, Map<ItemStack, IImpulseCannonAugment> augments) {
                if (useTicksLeft % 2 == 0) return; // only play the sound every 2 ticks
                int ticksUsed = getMaxUsageDuration(cannonStack, augmentStack) - useTicksLeft;
                double factor = Math.pow(ticksUsed, TAConfig.cannonRecurseExponent.getValue());
                // TODO this sound is a placeholder
                user.getEntityWorld().playSound(null, new BlockPos(user.getPositionEyes(1.0F)), SoundEvents.BLOCK_NOTE_SNARE,
                        SoundCategory.PLAYERS, 1.0F, (float) (2 - 75/(50 + factor)));
            }

            @Override
            public void onStopCannonTick(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, int useTicksLeft, IImpetusStorage buffer, Map<ItemStack, IImpulseCannonAugment> augments) {
                int ticksUsed = getMaxUsageDuration(cannonStack, augmentStack) - useTicksLeft;
                double factor = Math.pow(ticksUsed, TAConfig.cannonRecurseExponent.getValue());
                Vec3d origin = user.getPositionEyes(1);
                Vec3d scan = user.getLookVec().scale(TAConfig.cannonRecurseRange.getValue() * factor);
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    if (aug.getValue() instanceof IImpulseCannonRaytraceOverridingAugment) {
                        scan = ((IImpulseCannonRaytraceOverridingAugment) aug.getValue())
                                .overrideFiringRayTrace(cannonStack, aug.getKey(), user, origin, scan);
                        break;
                    }
                }
                float baseDamage = (float) (TAConfig.cannonRecurseDamage.getValue() * factor);
                float magicFactor = 0.5f;
                float normalFactor = 0.5f;
                // calculate what should've been consumed by the cannon up to this point on the fly
                long cost = getImpetusCostPerUsage(cannonStack, augmentStack, user, buffer) * ticksUsed + getImpetusCostPerUsage(cannonStack, augmentStack, user, buffer);
                double actual = cost;
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    actual *= aug.getValue().getImpulseCostModifier(cannonStack, aug.getKey(), user, buffer);
                }
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    baseDamage *= aug.getValue().getBaseDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, actual);
                    magicFactor *= aug.getValue().getMagicDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, actual);
                    normalFactor *= aug.getValue().getNormalDamageModifier(cannonStack, aug.getKey(), user, buffer, cost, actual);
                }
                scan = RaytraceHelper.shortenRaytraceByBlocks(user.getEntityWorld(), origin, origin.add(scan));
                List<Entity> ents = RaytraceHelper.raytraceEntities(user.getEntityWorld(), origin, scan);
                for (Entity e : ents) {
                    if (e == user) continue;
                    if (!(e instanceof IImpulseSpecialEntity) || !((IImpulseSpecialEntity) e).shouldImpulseCannonIgnore(user)) {
                        for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                            aug.getValue().applyAdditionalEffectsToEntity(cannonStack, aug.getKey(), user, origin, scan, e, baseDamage);
                        }
                        ImpetusAPI.causeImpetusDamage(user, e, baseDamage * magicFactor, baseDamage * normalFactor);
                        break;
                    }
                }
                for (Map.Entry<ItemStack, IImpulseCannonAugment> aug : augments.entrySet()) {
                    aug.getValue().applyAdditionalEffects(cannonStack, aug.getKey(), user, origin, scan, baseDamage);
                }

                Random rand = user.getRNG();
                float log = (float) Math.log1p(factor) / 4;
                // TODO this sound is a placeholder
                user.getEntityWorld().playSound(null, new BlockPos(user.getPositionEyes(1.0F)), TASounds.IMPULSE_CANNON_RAILGUN,
                        SoundCategory.PLAYERS, 0.5f + log, (rand.nextFloat() - rand.nextFloat()) / 3.0F + 1.0F - log);
                PacketImpulseRailgunProjectile packet = new PacketImpulseRailgunProjectile(user.getEntityId(), scan);
                PacketRecoil recoil = new PacketRecoil(user.getEntityId(), RecoilType.IMPULSE_RAILGUN);
                TANetwork.INSTANCE.sendToAllTracking(packet, user);
                TANetwork.INSTANCE.sendToAllTracking(recoil, user);
                if (user instanceof EntityPlayer) {
                    handleCooldown(user, (int) (TAConfig.cannonRecurseCooldown.getValue() * factor));
                    if (user instanceof EntityPlayerMP) {
                        TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) user);
                        TANetwork.INSTANCE.sendTo(recoil, (EntityPlayerMP) user);
                    }
                }
            }
        };
    }
    
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        SimpleCapabilityProviderNoSave<IAugment> provider =
                new SimpleCapabilityProviderNoSave<>(conversions[stack.getMetadata()], CapabilityAugment.AUGMENT);
        if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
            provider.deserializeNBT(nbt.getCompoundTag("Parent"));
        
        return provider;
    }

    public static final class BeamInformation implements IMessage {

        private Vector3f[] rotations;
        private double length;

        public BeamInformation() {}

        public BeamInformation(Vector3f[] rotations, double length) {
            this.rotations = rotations;
            this.length = length;
        }

        public static BeamInformation getForUnaugmentedCannon() {
            return new BeamInformation(new Vector3f[] { new Vector3f(0, 0, 1) }, TAConfig.cannonBeamRange.getValue());
        }

        public Vector3f[] getRotations() {
            return rotations;
        }

        public int numBeams() {
            return rotations.length;
        }

        public double getLength() {
            return length;
        }

        public Function<Vec3d, Vec3d> computeRotationFunction(int beamIndex) {
             return computeRotationFunction(rotations[beamIndex]);
        }

        public Vec3d transform(int beamIndex, Vec3d t) {
            return transform(rotations[beamIndex], t);
        }

        public static Function<Vec3d, Vec3d> computeRotationFunction(Vector3f rotation) {
            return t -> transform(rotation, t);
        }

        public static Vec3d transform(Vector3f rotation, Vec3d t) {
            if (rotation.lengthSquared() != 1) {
                rotation.normalize();
            }
            Vector3d v = new Vector3d(t.x, t.y, t.z);
            v.normalize();
            Vector3d up = new Vector3d(0, 1, 0);
            Vector3d cross = new Vector3d();
            cross.cross(v, up);
            if (cross.lengthSquared() == 0) {
                up = new Vector3d(0, 0, 1);
                cross.cross(v, up);
            }
            cross.normalize();
            up.cross(v, cross);
            up.normalize();

            cross.scale(rotation.x);
            up.scale(rotation.y);
            v.scale(rotation.z);
            v.add(up);
            v.add(cross);
            v.scale(t.length());
            return new Vec3d(v.x, v.y, v.z);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int count = buf.readInt();
            rotations = new Vector3f[count];
            for (int i = 0; i < count; i++) {
                rotations[i] = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
            }
            length = buf.readDouble();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(rotations.length);
            for (Vector3f rot : rotations) {
                buf.writeFloat(rot.x);
                buf.writeFloat(rot.y);
                buf.writeFloat(rot.z);
            }
            buf.writeDouble(length);
        }
    }
}
