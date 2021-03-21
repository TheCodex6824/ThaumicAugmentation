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

package thecodex6824.thaumicaugmentation.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class RaytraceHelper {

    private RaytraceHelper() {}
    
    public static Vec3d raytracePosition(EntityLivingBase user, double maxDistance) {
        Vec3d eyes = user.getPositionEyes(1.0F);
        Vec3d look = user.getLook(1.0F);
        Vec3d extended = eyes.add(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);
        RayTraceResult blockCheck = user.getEntityWorld().rayTraceBlocks(eyes, extended, false, true, true);
        maxDistance = blockCheck != null ? blockCheck.hitVec.distanceTo(eyes) : maxDistance;
        List<Entity> list = user.getEntityWorld().getEntitiesInAABBexcluding(user,
                user.getEntityBoundingBox().expand(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance).grow(1.0, 1.0, 1.0),
                Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> entity != null && entity.canBeCollidedWith()
        ));
        
        double dist = maxDistance;
        Vec3d ret = blockCheck != null ? blockCheck.hitVec : extended;
        for (Entity entity : list) {
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            if (aabb.contains(eyes)) {
                ret = eyes;
                break;
            }
            else {
                RayTraceResult res = aabb.calculateIntercept(eyes, extended);
                if (res != null) {
                    double newDist = eyes.distanceTo(res.hitVec);
                    if (newDist < dist) {
                        ret = res.hitVec;
                        dist = newDist;
                    }
                }
            }
        }

        return ret;
    }
    
    public static Vec3d raytracePosition(EntityLivingBase user, double maxDistance, float partialTicks) {
        Vec3d eyes = user.getPositionEyes(partialTicks);
        Vec3d look = user.getLook(partialTicks);
        Vec3d extended = eyes.add(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);
        RayTraceResult blockCheck = user.getEntityWorld().rayTraceBlocks(eyes, extended, false, true, true);
        maxDistance = blockCheck != null ? blockCheck.hitVec.distanceTo(eyes) : maxDistance;
        List<Entity> list = user.getEntityWorld().getEntitiesInAABBexcluding(user,
                user.getEntityBoundingBox().expand(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance).grow(1.0, 1.0, 1.0),
                Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> entity != null && entity.canBeCollidedWith()
        ));
        
        double dist = maxDistance;
        Vec3d ret = blockCheck != null ? blockCheck.hitVec : extended;
        for (Entity entity : list) {
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            if (aabb.contains(eyes)) {
                ret = eyes;
                break;
            }
            else {
                RayTraceResult res = aabb.calculateIntercept(eyes, extended);
                if (res != null) {
                    double newDist = eyes.distanceTo(res.hitVec);
                    if (newDist < dist) {
                        ret = res.hitVec;
                        dist = newDist;
                    }
                }
            }
        }

        return ret;
    }
    
    @Nullable
    public static Entity raytraceEntity(EntityLivingBase user, double maxDistance) {
        return raytraceEntity(user, maxDistance, e -> true);
    }
    
    @Nullable
    public static Entity raytraceEntity(EntityLivingBase user, double maxDistance, Predicate<Entity> acceptor) {
        Vec3d eyes = user.getPositionEyes(1.0F);
        Vec3d look = user.getLook(1.0F);
        Vec3d extended = eyes.add(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);
        RayTraceResult blockCheck = user.getEntityWorld().rayTraceBlocks(eyes, extended, false, true, true);
        maxDistance = blockCheck != null ? blockCheck.hitVec.distanceTo(eyes) : maxDistance;
        List<Entity> list = user.getEntityWorld().getEntitiesInAABBexcluding(user,
                user.getEntityBoundingBox().expand(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance).grow(1.0, 1.0, 1.0),
                Predicates.and(entity -> entity != null && entity.canBeCollidedWith(), Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> acceptor.test(entity))
        ));
        
        double dist = maxDistance;
        Entity ret = null;
        for (Entity entity : list) {
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            if (aabb.contains(eyes)) {
                ret = entity;
                break;
            }
            else {
                RayTraceResult res = aabb.calculateIntercept(eyes, extended);
                if (res != null) {
                    double newDist = eyes.distanceTo(res.hitVec);
                    if (newDist < dist) {
                        ret = entity;
                        dist = newDist;
                    }
                }
            }
        }

        return ret;
    }
    
    @Nullable
    public static Pair<Entity, Vec3d> raytraceEntityAndPos(EntityLivingBase user, double maxDistance, Predicate<Entity> acceptor) {
        Vec3d eyes = user.getPositionEyes(1.0F);
        Vec3d look = user.getLook(1.0F);
        Vec3d extended = eyes.add(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);
        RayTraceResult blockCheck = user.getEntityWorld().rayTraceBlocks(eyes, extended, false, true, true);
        maxDistance = blockCheck != null ? blockCheck.hitVec.distanceTo(eyes) : maxDistance;
        List<Entity> list = user.getEntityWorld().getEntitiesInAABBexcluding(user,
                user.getEntityBoundingBox().expand(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance).grow(1.0, 1.0, 1.0),
                Predicates.and(entity -> entity != null && entity.canBeCollidedWith(), Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> acceptor.test(entity))
        ));
        
        double dist = maxDistance;
        Pair<Entity, Vec3d> ret = null;
        for (Entity entity : list) {
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            if (aabb.contains(eyes)) {
                ret = Pair.of(entity, eyes);
                break;
            }
            else {
                RayTraceResult res = aabb.calculateIntercept(eyes, extended);
                if (res != null) {
                    double newDist = eyes.distanceTo(res.hitVec);
                    if (newDist < dist) {
                        ret = Pair.of(entity, res.hitVec);
                        dist = newDist;
                    }
                }
            }
        }

        return ret;
    }
    
    public static List<Entity> raytraceEntities(EntityLivingBase user, double maxDistance) {
        Vec3d eyes = user.getPositionEyes(1.0F);
        Vec3d look = user.getLook(1.0F);
        Vec3d extended = eyes.add(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);
        RayTraceResult blockCheck = user.getEntityWorld().rayTraceBlocks(eyes, extended, false, true, true);
        maxDistance = blockCheck != null ? blockCheck.hitVec.distanceTo(eyes) : maxDistance;
        List<Entity> list = user.getEntityWorld().getEntitiesInAABBexcluding(user,
                user.getEntityBoundingBox().expand(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance).grow(1.0, 1.0, 1.0),
                Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> entity != null && entity.canBeCollidedWith()
        ));
      
        ArrayList<Entity> ret = new ArrayList<>();
        for (Entity entity : list) {
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            if (aabb.contains(eyes) || aabb.calculateIntercept(eyes, extended) != null)
                ret.add(entity);
        }

        ret.sort((e1, e2) -> Double.compare(e1.getDistanceSq(user), e2.getDistanceSq(user)));
        return ret;
    }
    
    public static List<Entity> raytraceEntities(World world, Vec3d start, Vec3d end) {
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(start.x, start.y, start.z, end.x, end.y, end.z),
                Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> entity != null && entity.canBeCollidedWith()
        ));
      
        ArrayList<Entity> ret = new ArrayList<>();
        for (Entity entity : list) {
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            if (aabb.contains(start) || aabb.calculateIntercept(start, end) != null)
                ret.add(entity);
        }

        ret.sort((e1, e2) -> Double.compare(e1.getDistanceSq(start.x, start.y, start.z), e2.getDistanceSq(start.x, start.y, start.z)));
        return ret;
    }
    
}
