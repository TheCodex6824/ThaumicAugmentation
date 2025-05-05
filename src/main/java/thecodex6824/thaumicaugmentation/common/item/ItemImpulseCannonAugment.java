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

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.impl.impulsecannon.IImpulseCannonAugment;
import thecodex6824.thaumicaugmentation.api.augment.impl.impulsecannon.IImpulseCannonCustomMount;
import thecodex6824.thaumicaugmentation.api.augment.impl.impulsecannon.IImpulseCannonRaytraceOverridingAugment;
import thecodex6824.thaumicaugmentation.api.entity.DamageSourceImpetus;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.common.capability.provider.SimpleCapabilityProviderNoSave;
import thecodex6824.thaumicaugmentation.common.event.ScheduledTaskHandler;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemImpulseCannonAugment extends ItemTABase {

    protected final IImpulseCannonAugment[] augments;

    protected static final Map<BlockPos, BreakInformation> solidifierBreakCache = new Object2ReferenceOpenHashMap<>();

    public ItemImpulseCannonAugment() {
        super("gyroscope", "hyperion", "energizer", "destabilizer", "solidifier", "purifier", "mount");
        augments = new IImpulseCannonAugment[subItemNames.length];
        setMaxStackSize(1);
        setHasSubtypes(true);
        // gyroscope
        augments[0] = new IImpulseCannonRaytraceOverridingAugment() {
            private static final Vec3d up = new Vec3d(0, 1, 0);
            private static final Vec3d left = new Vec3d(0, 0, 1);
            private static final Vec3d front = new Vec3d(1, 0, 0);

            private static Vec3d rotate(Vec3d vec, Vec3d axis, float radians) {
                // rotates the vector around the given axis by the given angle using Rodrigues' formula
                float cos = MathHelper.cos(radians);
                return vec.scale(cos)
                        .add(axis.crossProduct(vec).scale(MathHelper.sin(radians)))
                        .add(axis.scale(axis.dotProduct(vec) * (1 - cos)));
            }

            @Override
            public @NotNull Vec3d overrideFiringRayTrace(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d sourcePosition,
                                                         Vec3d originalRayTrace, float partialTicks) {
                // first, set up an AABB to gather entities within correction range
                float maxAngle = (float) Math.toRadians(TAConfig.cannonGyroscopeCorrectionAngle.getValue());
                AxisAlignedBB bb = new AxisAlignedBB(Vec3d.ZERO, originalRayTrace);
                Vec3d horizontal = originalRayTrace.crossProduct(up).normalize();
                Vec3d vertical;
                if (horizontal == Vec3d.ZERO) {
                    // the original ray trace is straight up/down
                    horizontal = left;
                    vertical = front;
                } else {
                    vertical = originalRayTrace.crossProduct(horizontal).normalize();
                }
                bb = bb.union(new AxisAlignedBB(rotate(originalRayTrace, horizontal, maxAngle), rotate(originalRayTrace, horizontal, -maxAngle)));
                bb = bb.union(new AxisAlignedBB(rotate(originalRayTrace, vertical, maxAngle), rotate(originalRayTrace, vertical, -maxAngle)));
                bb = bb.offset(sourcePosition);
                List<Entity> gather = user.getEntityWorld().getEntitiesWithinAABB(Entity.class, bb);
                if (gather.isEmpty()) return originalRayTrace;
                // second, get vectors from source to entity centers,
                // then evaluate for which one has the smallest angle to the original trace.
                Vec3d smallest = originalRayTrace;
                boolean smallestIsAlive = false;
                double smallestAngle = maxAngle;
                for (Entity e : gather) {
                    if (e == user) continue;
                    boolean alive = e instanceof EntityLivingBase && !e.isDead && ((EntityLivingBase) e).getHealth() > 0;
                    if (smallestIsAlive && !alive) continue; // do not prefer an unalive target over an alive one
                    Vec3d vector = e.getEntityBoundingBox().getCenter().subtract(sourcePosition);
                    // definition of angle between two vectors:
                    // arccos of their dot product divided by the product of their lengths.
                    double ang = Math.acos(originalRayTrace.dotProduct(vector) / Math.sqrt(originalRayTrace.lengthSquared() * vector.lengthSquared()));
                    // give an alive target priority over an unalive one
                    if (ang >= maxAngle || (ang >= smallestAngle && (smallestIsAlive || !alive))) continue;
                    // if there's a block in the way, skip the entity.
                    if (user.getEntityWorld().rayTraceBlocks(sourcePosition, sourcePosition.add(vector), false, true, false) != null) continue;
                    smallestAngle = (float) ang;
                    smallest = vector;
                    smallestIsAlive = alive;
                }
                // finally, rescale smallest to be the same length as the original scan
                return smallest.scale(Math.sqrt(originalRayTrace.lengthSquared() / smallest.lengthSquared()));
            }
        };
        // hyperion
        augments[1] = new IImpulseCannonAugment() {

            private double efficiencyFactor() {
                return TAConfig.cannonHyperionEfficiencyFactor.getValue();
            }

            @Override
            public double getImpulseCostModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer) {
                return efficiencyFactor();
            }

            @Override
            public float getBaseDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
                if (!buffer.canExtract()) return 1;
                double extracted = buffer.extractEnergy(Long.MAX_VALUE, false) + Math.ceil(actualImpetusConsumed);
                return (float) (extracted / actualImpetusConsumed);
            }

            @Override
            public void applyAdditionalEffects(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d firingOrigin, Vec3d firingEnd, float baseDamage) {
                int particleCount = (int) Math.sqrt(baseDamage);
                for (int i = 0; i < particleCount; i++) {
                    Random random = new Random();
                    double ratio = (i + 0.5 + random.nextGaussian()) / particleCount;
                    if (user.getEntityWorld() instanceof WorldServer) {
                        ((WorldServer) user.getEntityWorld()).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE,
                                MathHelper.clampedLerp(firingOrigin.x, firingEnd.x, ratio),
                                MathHelper.clampedLerp(firingOrigin.y, firingEnd.y, ratio),
                                MathHelper.clampedLerp(firingOrigin.z, firingEnd.z, ratio),
                                1, 2, 2, 2D, 1);
                    }

                }
            }
        };
        // energizer
        augments[2] = new IImpulseCannonAugment() {
            @Override
            public float getNormalDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
                return TAConfig.cannonEnergizerNormalFactor.getValue();
            }

            @Override
            public float getMagicDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
                return TAConfig.cannonEnergizerMagicFactor.getValue();
            }
        };
        // destabilizer
        augments[3] = new IImpulseCannonAugment() {
            @Override
            public float getNormalDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
                return TAConfig.cannonDestabilizerNormalFactor.getValue();
            }

            @Override
            public float getMagicDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
                return TAConfig.cannonDestabilizerMagicFactor.getValue();
            }
        };
        // solidifier
        augments[4] = new IImpulseCannonAugment() {
            @Override
            public void applyAdditionalEffectsToEntity(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d firingOrigin, Vec3d firingEnd, Entity entityHit, float baseDamage) {
                if (entityHit instanceof EntityLivingBase) {
                    Vec3d vec = firingEnd.subtract(firingOrigin).normalize();
                    double factor = Math.max(0.1, Math.log(baseDamage)) * TAConfig.cannonSolidifierKnockbackStrength.getValue();
                    entityHit.motionX += vec.x * factor;
                    entityHit.motionY += vec.y * factor;
                    entityHit.motionZ += vec.z * factor;
                    if (entityHit instanceof EntityPlayerMP) {
                        EntityPlayerMP mp = (EntityPlayerMP) entityHit;
                        mp.connection.sendPacket(new SPacketEntityVelocity(mp));
                    }
                }
            }


            @Override
            public void applyAdditionalEffects(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d firingOrigin, Vec3d firingEnd, float baseDamage) {
                Vec3d offset = firingEnd.subtract(firingOrigin);
                firingEnd = offset.add(offset.normalize().scale(Math.sqrt(baseDamage))).add(firingOrigin);
                int tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
                while (baseDamage > 0) {
                    RayTraceResult result = user.getEntityWorld().rayTraceBlocks(firingOrigin, firingEnd, false, true, false);
                    if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK) break;
                    baseDamage = BreakInformation.damageBlock(user.getEntityWorld(), result.getBlockPos(), baseDamage, tick);
                }
            }
        };
        // purifier
        augments[5] = new IImpulseCannonAugment() {

            @Override
            public double getImpulseCostModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer) {
                return 2;
            }

            @Override
            public float getBaseDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
                return 0.2f;
            }

            @Override
            public void applyAdditionalEffectsToEntity(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d firingOrigin, Vec3d firingEnd, Entity entityHit, float baseDamage) {
                DamageSource source = new DamageSourceImpetus(user, user.getPositionVector()).setDamageBypassesArmor().setDamageIsAbsolute();
                entityHit.attackEntityFrom(source, baseDamage * 2);
                if (entityHit instanceof EntityLivingBase) {
                    EntityLivingBase base = (EntityLivingBase) entityHit;
                    base.hurtResistantTime = Math.min(base.hurtResistantTime, 2);
                    base.lastDamage = 0.0F;
                }
            }
        };
        // mount
        augments[6] = new IImpulseCannonCustomMount() {
            @Override
            public boolean givesExtraSlot() {
                return true;
            }
        };
    }
    
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        SimpleCapabilityProviderNoSave<IAugment> provider =
                new SimpleCapabilityProviderNoSave<>(augments[stack.getMetadata()], CapabilityAugment.AUGMENT);
        if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
            provider.deserializeNBT(nbt.getCompoundTag("Parent"));
        
        return provider;
    }

    protected static void updateBreakInformation() {
        int tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        for (Iterator<Map.Entry<BlockPos, BreakInformation>> iterator = solidifierBreakCache.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<BlockPos, BreakInformation> entry = iterator.next();
            if (entry.getValue().world.getBlockState(entry.getKey()) != entry.getValue().blockState) {
                entry.getValue().world.sendBlockBreakProgress(BreakInformation.BREAKER_ID, entry.getKey(), 0);
                iterator.remove();
                continue;
            }
            if (entry.getValue().update(tick)) {
                iterator.remove();
            }
        }
        if (!solidifierBreakCache.isEmpty())
            ScheduledTaskHandler.registerTask(ItemImpulseCannonAugment::updateBreakInformation, 1);
    }

    protected static class BreakInformation {

        public static final int BREAKER_ID = -1;

        final World world;
        final BlockPos pos;
        final IBlockState blockState;
        int progress; // 10 levels of breakage
        int lastProgressTick;

        public BreakInformation(World world, BlockPos pos, IBlockState state) {
            this.world = world;
            this.pos = pos;
            this.blockState = state;
        }

        public static float getBreakStrength() {
            return TAConfig.cannonSolidifierBreakStrength.getValue();
        }

        public static float damageBlock(World world, BlockPos pos, float incoming, int tick) {
            IBlockState state = world.getBlockState(pos);
            BreakInformation existing = solidifierBreakCache.get(pos);
            if (existing == null) {
                float hardness = state.getBlockHardness(world, pos);
                if (incoming / hardness >= 1) {
                    state.getBlock().dropBlockAsItem(world, pos, state, 0);
                    world.setBlockToAir(pos);
                    return incoming - hardness;
                } else {
                    existing = new BreakInformation(world, pos, state);
                    if (solidifierBreakCache.isEmpty())
                        ScheduledTaskHandler.registerTask(ItemImpulseCannonAugment::updateBreakInformation, 1);
                    solidifierBreakCache.put(pos, existing);
                }
            }
            return existing.damage(incoming, tick);
        }

        public float damage(float incoming, int tick) {
            this.lastProgressTick = tick;
            float hardness = blockState.getBlockHardness(world, pos);
            float damage = getBreakStrength() * incoming / hardness;
            if (damage < 1) return 0;
            if (damage + progress >= 10) {
                world.sendBlockBreakProgress(BREAKER_ID, pos, 0);
                blockState.getBlock().dropBlockAsItem(world, pos, blockState, 0);
                world.setBlockToAir(pos);
                return incoming - (10 - progress) * hardness / getBreakStrength();
            } else {
                progress += damage;
                world.sendBlockBreakProgress(BREAKER_ID, pos, progress);
                return 0;
            }
        }

        public boolean update(int tick) {
            if (tick >= 10 + lastProgressTick) {
                progress -= 1;
                world.sendBlockBreakProgress(BREAKER_ID, pos, progress);
            }
            return progress <= 0 || progress >= 10;
        }
    }
    
}
