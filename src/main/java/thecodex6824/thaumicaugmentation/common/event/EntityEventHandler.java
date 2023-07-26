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

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.entity.CapabilityPortalState;
import thecodex6824.thaumicaugmentation.api.entity.IPortalState;
import thecodex6824.thaumicaugmentation.api.entity.PlayerMovementAbilityManager;
import thecodex6824.thaumicaugmentation.api.entity.PortalStateManager;
import thecodex6824.thaumicaugmentation.api.event.FluxRiftDestroyBlockEvent;
import thecodex6824.thaumicaugmentation.api.event.FocusTouchGetEntityEvent;
import thecodex6824.thaumicaugmentation.api.tile.CapabilityRiftJar;
import thecodex6824.thaumicaugmentation.api.tile.IRiftJar;
import thecodex6824.thaumicaugmentation.api.util.RaytraceHelper;
import thecodex6824.thaumicaugmentation.api.ward.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorageServer;
import thecodex6824.thaumicaugmentation.common.entity.EntityFocusShield;
import thecodex6824.thaumicaugmentation.common.entity.EntityPrimalWisp;
import thecodex6824.thaumicaugmentation.common.item.ItemThaumiumRobes.MaskType;
import thecodex6824.thaumicaugmentation.common.network.PacketLivingEquipmentChange;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.tile.TileRiftJar;
import thecodex6824.thaumicaugmentation.common.world.ChunkGeneratorEmptiness;
import thecodex6824.thaumicaugmentation.common.world.structure.MapGenEldritchSpire;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class EntityEventHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        IPortalState state = event.getEntity().getCapability(CapabilityPortalState.PORTAL_STATE, null);
        if (state != null && state.isInPortal())
            PortalStateManager.markEntityInPortal(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onProjectileCollide(ProjectileImpactEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote && event.getEntity() instanceof EntityEnderPearl) {
            WorldServer w = (WorldServer) event.getEntity().getEntityWorld();
            BlockPos check = event.getEntity().getPosition();
            if (w.getChunkProvider().isInsideStructure(w, "EldritchSpire", check)) {
                MapGenEldritchSpire.Start start = ((ChunkGeneratorEmptiness) w.getChunkProvider().chunkGenerator).getSpireStart(check);
                if (start != null) {
                    IWardStorage storage = w.getChunk(check).getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                    if (storage instanceof IWardStorageServer && ((IWardStorageServer) storage).isWardOwner(start.getWard())) { 
                        event.getEntity().setDead();
                        Vec3d pos = event.getEntity().getPositionVector();
                        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.ENDER_EYE_BREAK, pos.x,
                                pos.y, pos.z, Item.getIdFromItem(Items.ENDER_PEARL)), event.getEntity());
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
        
        if (event.getRayTraceResult() != null && event.getRayTraceResult().entityHit instanceof EntityFocusShield) {
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
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == Phase.END)
            PortalStateManager.tick();
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.getEntityWorld().isRemote) {
            PacketLivingEquipmentChange packet = new PacketLivingEquipmentChange(entity.getEntityId(),
                    event.getSlot(), event.getTo());
            TANetwork.INSTANCE.sendToAllTracking(packet, entity);
            if (entity instanceof EntityPlayerMP)
                TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) entity);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingUpdate(LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (PlayerMovementAbilityManager.isValidSideForMovement(player))
                PlayerMovementAbilityManager.tick(player);
        }
    }
    
    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        ItemStack head = event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (head.getItem() == TAItems.THAUMIUM_ROBES_HOOD && head.hasTagCompound() && head.getTagCompound().getInteger("maskType") == MaskType.WITHER.getID() &&
                event.getSource().getTrueSource() instanceof EntityLivingBase &&
                event.getEntity().getEntityWorld().rand.nextFloat() < event.getAmount() / 10.0F) {
            
            PotionEffect wither = new PotionEffect(MobEffects.WITHER, 80);
            EntityLivingBase base = (EntityLivingBase) event.getSource().getTrueSource();
            if (base.isPotionApplicable(wither))
                base.addPotionEffect(wither);
        }
        
        if (event.getSource().getTrueSource() instanceof EntityLivingBase) {
            head = ((EntityLivingBase) event.getSource().getTrueSource()).getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (head.getItem() == TAItems.THAUMIUM_ROBES_HOOD && head.hasTagCompound() && head.getTagCompound().getInteger("maskType") == MaskType.LIFESTEAL.getID() &&
                    event.getEntity().getEntityWorld().rand.nextFloat() < event.getAmount() / 12.0F) {
                
                ((EntityLivingBase) event.getSource().getTrueSource()).heal(1.0F);
            }
        }
    }
    
    // we want to do the same thing for both target and trajectory events, so register for the superclass
    @SubscribeEvent
    public static void onTouchTrajectory(FocusTouchGetEntityEvent event) {
        if (event.getRay() != null && event.getRay().entityHit instanceof EntityFocusShield) {
            EntityFocusShield hit = (EntityFocusShield) event.getRay().entityHit;
            EntityLivingBase caster = event.getFocus().getPackage().getCaster();
            if (caster != null && hit.getOwnerId() != null && hit.getOwnerId().equals(caster.getUniqueID())) {
                Pair<Entity, Vec3d> p = RaytraceHelper.raytraceEntityAndPos(caster, event.getRange(), e -> e != hit);
                if (p != null) {
                    event.setRay(new RayTraceResult(p.getKey(), p.getValue()));
                }
                else {
                    event.setRay(null);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onFluxRiftDestroyBlock(FluxRiftDestroyBlockEvent event) {
        if (event.getDestroyedBlock().getBlock() == TABlocks.RIFT_JAR) {
            World world = event.getEntity().getEntityWorld();
            TileEntity tile = world.getTileEntity(event.getPosition());
            if (tile instanceof TileRiftJar) {
                IRiftJar jar = tile.getCapability(CapabilityRiftJar.RIFT_JAR, null);
                if (jar != null && jar.hasRift()) {
                    BlockPos pos = event.getPosition();
                    world.destroyBlock(pos, false);
                    world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 3.0F, false);
                    EntityPrimalWisp wisp = new EntityPrimalWisp(world);
                    wisp.setPositionAndRotation(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                            world.rand.nextInt(360) - 180, 0.0F);
                    wisp.rotationYawHead = wisp.rotationYaw;
                    wisp.renderYawOffset = wisp.rotationYaw;
                    wisp.onInitialSpawn(world.getDifficultyForLocation(pos), null);
                    world.spawnEntity(wisp);
                    event.getRift().setDead();
                    event.setCanceled(true);
                }
            }
        }
    }
    
}
