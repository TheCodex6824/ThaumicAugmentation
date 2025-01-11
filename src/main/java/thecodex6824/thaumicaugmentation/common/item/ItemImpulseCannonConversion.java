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
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import org.jetbrains.annotations.NotNull;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.impulsecannon.IImpulseCannonAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.impulsecannon.IImpulseCannonConversion;
import thecodex6824.thaumicaugmentation.api.augment.builder.impulsecannon.IImpulseCannonRaytraceOverridingAugment;
import thecodex6824.thaumicaugmentation.api.entity.IImpulseSpecialEntity;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.util.RaytraceHelper;
import thecodex6824.thaumicaugmentation.common.capability.provider.SimpleCapabilityProviderNoSave;
import thecodex6824.thaumicaugmentation.common.event.ScheduledTaskHandler;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.network.PacketImpulseBurst;
import thecodex6824.thaumicaugmentation.common.network.PacketImpulseRailgunProjectile;
import thecodex6824.thaumicaugmentation.common.network.PacketRecoil;
import thecodex6824.thaumicaugmentation.common.network.PacketRecoil.RecoilType;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class ItemImpulseCannonConversion extends ItemTABase {
    public static final ModelResourceLocation LENS_BEAM = new ModelResourceLocation(ThaumicAugmentationAPI.MODID + ":impulse_cannon_beam", "inventory");
    public static final ModelResourceLocation LENS_RAILGUN = new ModelResourceLocation(ThaumicAugmentationAPI.MODID + ":impulse_cannon_railgun", "inventory");
    public static final ModelResourceLocation LENS_BURST = new ModelResourceLocation(ThaumicAugmentationAPI.MODID + ":impulse_cannon_burst", "inventory");

    protected final IImpulseCannonConversion[] conversions;
    
    public ItemImpulseCannonConversion() {
        super("railgun", "burst");
        conversions = new IImpulseCannonConversion[subItemNames.length];
        setMaxStackSize(1);
        setHasSubtypes(true);
        // railgun
        conversions[0] = new IImpulseCannonConversion() {

            @Override
            public @NotNull ModelResourceLocation getLensModel() {
                return LENS_RAILGUN;
            }

            @Override
            public int getMaxUsageDuration() {
                return 1;
            }

            @Override
            public boolean isTickable(EntityLivingBase user) {
                return false;
            }

            @Override
            public long getImpetusCostPerUsage(EntityLivingBase user, IImpetusStorage buffer) {
                return TAConfig.cannonRailgunCost.getValue();
            }

            @Override
            public void onCannonUsage(EntityLivingBase user, long impetusConsumed, IImpetusStorage buffer, List<IImpulseCannonAugment> augmentList) {
                Vec3d origin = user.getPositionEyes(1);
                Vec3d scan = user.getLookVec().scale(TAConfig.cannonRailgunRange.getValue());
                for (IImpulseCannonAugment aug : augmentList) {
                    if (aug instanceof IImpulseCannonRaytraceOverridingAugment override) {
                        scan = override.overrideFiringRayTrace(user, origin, scan);
                        break;
                    }
                }
                float baseDamage = TAConfig.cannonRailgunDamage.getValue();
                float magicFactor = 0.5f;
                float normalFactor = 0.5f;
                long cost = getImpetusCostPerUsage(user, buffer);
                for (IImpulseCannonAugment aug : augmentList) {
                    baseDamage *= aug.getBaseDamageModifier(buffer, cost, impetusConsumed);
                    magicFactor *= aug.getMagicDamageModifier(buffer, cost, impetusConsumed);
                    normalFactor *= aug.getNormalDamageModifier(buffer, cost, impetusConsumed);
                }
                scan = RaytraceHelper.shortenRaytraceByBlocks(user.getEntityWorld(), origin, origin.add(scan));
                List<Entity> ents = RaytraceHelper.raytraceEntities(user.getEntityWorld(), origin, scan);
                for (Entity e : ents) {
                    if (e == user) continue;
                    if (!(e instanceof IImpulseSpecialEntity ent) || !ent.shouldImpulseCannonIgnore(user)) {
                        for (IImpulseCannonAugment aug : augmentList) {
                            aug.applyAdditionalEffectsToEntity(user, origin, scan, e, baseDamage);
                        }
                        ImpetusAPI.causeImpetusDamage(user, e, baseDamage * magicFactor, baseDamage * normalFactor);
                        if (e instanceof IImpulseSpecialEntity ent && ent.shouldStopRailgunBeam(user)) {
                            // make the beam terminate near the beam blocking entity
                            scan = scan.subtract(origin);
                            double scale = Math.sqrt(origin.squareDistanceTo(e.posX, e.posY, e.posZ) / scan.lengthSquared());
                            if (scale < 1) scan = scan.scale(scale);
                            scan = origin.add(scan);
                            break;
                        }
                    }
                }
                for (IImpulseCannonAugment aug : augmentList) {
                    aug.applyAdditionalEffects(user, origin, scan, baseDamage);
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
            public @NotNull ModelResourceLocation getLensModel() {
                return LENS_BURST;
            }

            @Override
            public int getMaxUsageDuration() {
                return 5;
            }

            @Override
            public boolean isTickable(EntityLivingBase user) {
                return false;
            }

            @Override
            public long getImpetusCostPerUsage(EntityLivingBase user, IImpetusStorage buffer) {
                return TAConfig.cannonBurstCost.getValue();
            }

            private void tick(EntityLivingBase user, List<IImpulseCannonAugment> augments, float baseDamage,
                              float magicFactor, float normalFactor, int num) {
                int c = TAConfig.cannonBurstCount.getValue() - 1;
                Vec3d origin = user.getPositionEyes(1);
                Vec3d scan = user.getLookVec().scale(TAConfig.cannonBurstRange.getValue());
                for (IImpulseCannonAugment aug : augments) {
                    if (aug instanceof IImpulseCannonRaytraceOverridingAugment override) {
                        scan = override.overrideFiringRayTrace(user, origin, scan);
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
                if (e != null && (!(e instanceof IImpulseSpecialEntity ent) || !ent.shouldImpulseCannonIgnore(user))) {
                    for (IImpulseCannonAugment aug : augments) {
                        aug.applyAdditionalEffectsToEntity(user, origin, scan, e, baseDamage);
                    }
                    if (ImpetusAPI.causeImpetusDamage(user, e, baseDamage * magicFactor, baseDamage * normalFactor)
                            && num < c && e instanceof EntityLivingBase base) {
                        base.hurtResistantTime = Math.min(base.hurtResistantTime, 1);
                        base.lastDamage = 0.0F;
                    }
                }
                for (IImpulseCannonAugment aug : augments) {
                    aug.applyAdditionalEffects(user, origin, scan, baseDamage);
                }

                Random rand = user.getRNG();
                user.getEntityWorld().playSound(null, new BlockPos(user.getPositionEyes(1.0F)), TASounds.IMPULSE_CANNON_BURST,
                        SoundCategory.PLAYERS, 1.0F, (rand.nextFloat() - rand.nextFloat()) / 2.0F + 1.0F);
                PacketImpulseBurst packet = new PacketImpulseBurst(user.getEntityId(), scan, num);
                TANetwork.INSTANCE.sendToAllTracking(packet, user);
                if (user instanceof EntityPlayerMP)
                    TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) user);

                if (num < c)
                    ScheduledTaskHandler.registerTask(() -> tick(user, augments, baseDamage, magicFactor, normalFactor, num + 1), TAConfig.cannonBurstDelay.getValue());
            }

            @Override
            public void onCannonUsage(EntityLivingBase user, long impetusConsumed, IImpetusStorage buffer, List<IImpulseCannonAugment> augmentList) {
                float baseDamage = TAConfig.cannonBurstDamage.getValue();
                float magicFactor = 0.5f;
                float normalFactor = 0.5f;
                long cost = getImpetusCostPerUsage(user, buffer);
                for (IImpulseCannonAugment aug : augmentList) {
                    baseDamage *= aug.getBaseDamageModifier(buffer, cost, impetusConsumed);
                    magicFactor *= aug.getMagicDamageModifier(buffer, cost, impetusConsumed);
                    normalFactor *= aug.getNormalDamageModifier(buffer, cost, impetusConsumed);
                }
                float finalBaseDamage = baseDamage;
                float finalMagicFactor = magicFactor;
                float finalNormalFactor = normalFactor;
                ScheduledTaskHandler.registerTask(() -> tick(user, augmentList, finalBaseDamage, finalMagicFactor, finalNormalFactor, 0), 0);
                PacketRecoil recoil = new PacketRecoil(user.getEntityId(), RecoilType.IMPULSE_BURST);
                TANetwork.INSTANCE.sendToAllTracking(recoil, user);
                if (user instanceof EntityPlayer) {
                    handleCooldown(user, TAConfig.cannonBurstCooldown.getValue());
                    if (user instanceof EntityPlayerMP)
                        TANetwork.INSTANCE.sendTo(recoil, (EntityPlayerMP) user);
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
    
}
