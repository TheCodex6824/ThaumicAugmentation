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

package thecodex6824.thaumicaugmentation.common.event;

import java.util.HashSet;

import com.google.common.base.Predicates;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.entity.CapabilityPortalState;
import thecodex6824.thaumicaugmentation.api.entity.PortalStateManager;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.common.entity.EntityFocusShield;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class EntityEventHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!TAConfig.disableEmptiness.getValue() && event.getWorld().provider.getDimension() == TADimensions.EMPTINESS.getId() && event.getEntity().getClass() == EntityFluxRift.class)
            event.setCanceled(true);
        else if (event.getEntity().hasCapability(CapabilityPortalState.PORTAL_STATE, null) && 
                event.getEntity().getCapability(CapabilityPortalState.PORTAL_STATE, null).isInPortal()) {
            
            PortalStateManager.markEntityInPortal(event.getEntity());
        }
    }
    
    @SubscribeEvent
    public static void onProjectileCollide(ProjectileImpactEvent event) {
        if (event.getRayTraceResult().entityHit instanceof EntityFocusShield) {
            EntityFocusShield s = (EntityFocusShield) event.getRayTraceResult().entityHit;
            if (s.getOwner() != null) {
                // owner's projectiles should always pass through
                Entity projectile = event.getEntity();
                if (projectile instanceof EntityThrowable && s.getOwner().equals(((EntityThrowable) projectile).getThrower())) {
                    event.setCanceled(true);
                    return;
                }
                else if (projectile instanceof EntityArrow && s.getOwner().equals(((EntityArrow) projectile).shootingEntity)) {
                    event.setCanceled(true);
                    return;
                }
                else if (projectile instanceof EntityFireball && s.getOwner().equals(((EntityFireball) projectile).shootingEntity)) {
                    event.setCanceled(true);
                    return;
                }
                
                // TODO: check velocty + collision to allow outward projectiles?
            }
            
            // we check for projectile reflection later as we want damage to be applied to the shield
        }
    }
    
    @SubscribeEvent
    public static void onGetCollisionBoxes(GetCollisionBoxesEvent event) {
        if (event.getEntity() != null) {
            IntOpenHashSet checked = new IntOpenHashSet();
            HashSet<AxisAlignedBB> toRemove = new HashSet<>();
            for (AxisAlignedBB box : event.getCollisionBoxesList()) {
                for (Entity e : event.getWorld().getEntitiesInAABBexcluding(event.getEntity(), box, Predicates.instanceOf(EntityFocusShield.class))) {
                    if (!checked.contains(e.getEntityId())) {
                        checked.add(e.getEntityId());
                        if (event.getEntity().equals(((EntityFocusShield) e).getOwner()) && box.equals(e.getCollisionBoundingBox())) {
                            toRemove.add(box);
                            continue;
                        }
                    }
                }
            }
            
            event.getCollisionBoxesList().removeAll(toRemove);
        }
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == Phase.END)
            PortalStateManager.tick();
    }
    
}
