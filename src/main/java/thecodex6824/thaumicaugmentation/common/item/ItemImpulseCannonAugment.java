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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import org.jetbrains.annotations.NotNull;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.impulsecannon.IImpulseCannonAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.impulsecannon.IImpulseCannonRaytraceOverridingAugment;
import thecodex6824.thaumicaugmentation.common.capability.provider.SimpleCapabilityProviderNoSave;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ItemImpulseCannonAugment extends ItemTABase {

    protected final IImpulseCannonAugment[] augments;

    public ItemImpulseCannonAugment() {
        super("gyroscope");
        augments = new IImpulseCannonAugment[subItemNames.length];
        setMaxStackSize(1);
        setHasSubtypes(true);
        augments[0] = new IImpulseCannonRaytraceOverridingAugment() {
            @Override
            public @NotNull Vec3d overrideFiringRayTrace(EntityLivingBase user, Vec3d sourcePosition,
                                                         Vec3d originalRayTrace, float partialTicks) {
                // first, set up an AABB to gather entities within correction range
                float maxAngle = TAConfig.cannonGyroscopeCorrectionAngle.getValue();
                AxisAlignedBB bb = new AxisAlignedBB(sourcePosition, originalRayTrace);
                bb = bb.union(new AxisAlignedBB(originalRayTrace.rotatePitch(maxAngle), originalRayTrace.rotatePitch(-maxAngle)).offset(sourcePosition));
                bb = bb.union(new AxisAlignedBB(originalRayTrace.rotateYaw(maxAngle), originalRayTrace.rotateYaw(-maxAngle)).offset(sourcePosition));
                List<Entity> gather = user.getEntityWorld().getEntitiesWithinAABB(Entity.class, bb);
                if (gather.isEmpty()) return originalRayTrace;
                // second, get vectors from source to entity centers,
                // then evaluate for which one has the smallest angle to the original trace.
                Vec3d smallest = originalRayTrace;
                boolean smallestIsAlive = false;
                double smallestAngle = maxAngle;
                for (Entity e : gather) {
                    if (e == user) continue;
                    boolean alive = e instanceof EntityLivingBase living && !living.isDead && living.getHealth() > 0;
                    if (smallestIsAlive && !alive) continue; // do not prefer an unalive target over an alive one
                    Vec3d vector = e.getEntityBoundingBox().getCenter().subtract(sourcePosition);
                    // definition of angle between two vectors:
                    // arccos of their dot product divided by the product of their lengths.
                    double ang = Math.toDegrees(Math.acos(originalRayTrace.dotProduct(vector) / Math.sqrt(originalRayTrace.lengthSquared() * vector.lengthSquared())));
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
    
}
