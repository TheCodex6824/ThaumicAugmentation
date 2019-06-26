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
import java.util.ListIterator;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.warded.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.IWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.IWardStorageServer;
import thecodex6824.thaumicaugmentation.api.warded.WardSyncManager;
import thecodex6824.thaumicaugmentation.api.warded.WardSyncManager.DimensionalChunkPos;
import thecodex6824.thaumicaugmentation.api.warded.WardSyncManager.WardUpdateEntry;
import thecodex6824.thaumicaugmentation.common.network.PacketFullWardSync;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketWardUpdate;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class WardEventHandler {

    private static void sendWardParticles(World world, BlockPos pos, EnumFacing facing) {
        if (!world.isRemote) {
            TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.WARD, pos.getX(), pos.getY(), pos.getZ(), facing.getIndex(),
                    0.5, 0.5, 0.5), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
        }
    }
    
    private static boolean isPlayerInChunkRange(DimensionalChunkPos pos, EntityPlayerMP player) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(pos.dim).getPlayerChunkMap().isPlayerWatchingChunk(player, pos.x, pos.z);
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (!TAConfig.disableWardFocus.getValue() && event.phase == Phase.END) {
            for (Map.Entry<DimensionalChunkPos, WardUpdateEntry> entry : WardSyncManager.getEntries()) {
                DimensionalChunkPos pos = entry.getKey();
                for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                    if (player.dimension == pos.dim && isPlayerInChunkRange(pos, player)) {
                        byte send = 0;
                        if (entry.getValue().update.equals(player.getUniqueID()))
                            send = 1;
                        else if (!entry.getValue().update.equals(IWardStorageServer.EMPTY_UUID))
                            send = 2;
                        
                        TANetwork.INSTANCE.sendTo(new PacketWardUpdate(entry.getValue().pos, send), player);
                    }
                }
            }
            
            WardSyncManager.clearEntries();
        }
    }
    
    @SubscribeEvent
    public static void onTrackChunk(ChunkWatchEvent event) {
        if (!TAConfig.disableWardFocus.getValue() && event.getChunkInstance().hasCapability(CapabilityWardStorage.WARD_STORAGE, null) && 
                event.getChunkInstance().getCapability(CapabilityWardStorage.WARD_STORAGE, null) instanceof IWardStorageServer) {
            IWardStorageServer storage = (IWardStorageServer) event.getChunkInstance().getCapability(CapabilityWardStorage.WARD_STORAGE, null);
            TANetwork.INSTANCE.sendTo(new PacketFullWardSync(storage.fullSyncToClient(event.getChunkInstance(), event.getPlayer().getUniqueID())), event.getPlayer());
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!TAConfig.disableWardFocus.getValue()) {
            BlockPos pos = event.getPos();
            EntityPlayer player = event.getEntityPlayer();
            Chunk chunk = player.getEntityWorld().getChunk(pos);
            if (chunk != null && chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                IWardStorage storage = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                if (storage.hasWard(pos) && !event.getEntityPlayer().isCreative()) {
                    RayTraceResult ray = player.getEntityWorld().rayTraceBlocks(player.getPositionEyes(1.0F), player.getLookVec().scale(
                            player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()).add(new Vec3d(pos)), false, false, true);
                    sendWardParticles(event.getEntityPlayer().getEntityWorld(), pos, ray.sideHit);
                    event.setCanceled(true);
                    event.setNewSpeed(0.0F);
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!TAConfig.disableWardFocus.getValue()) {
            BlockPos pos = event.getPos();
            Chunk chunk = event.getWorld().getChunk(pos);
            if (chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                IWardStorage storage = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                if (storage.hasWard(pos) && !event.getPlayer().isCreative())
                    event.setCanceled(true);
                else if (storage instanceof IWardStorageServer && event.getPlayer().isCreative())
                    ((IWardStorageServer) storage).clearWard(event.getWorld(), pos);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockPunch(PlayerInteractEvent.LeftClickBlock event) {
        if (!TAConfig.disableWardFocus.getValue()) {
            BlockPos pos = event.getPos();
            Chunk chunk = event.getWorld().getChunk(pos);
            if (chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                IWardStorage storage = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                if (storage.hasWard(pos) && !event.getEntityPlayer().isCreative()) {
                    event.setCanceled(true);
                    event.setUseBlock(Result.DENY);
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!TAConfig.disableWardFocus.getValue()) {
            BlockPos pos = event.getPos();
            Chunk chunk = event.getWorld().getChunk(pos);
            if (chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                IWardStorage storage = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                if (storage.hasWard(pos) && !event.getEntityPlayer().isCreative())
                    event.setUseBlock(Result.DENY);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!TAConfig.disableWardFocus.getValue()) {
            if (event.getState().getMaterial() == Material.FIRE) {
                BlockPos notifier = event.getPos();
                for (EnumFacing facing : event.getNotifiedSides()) {
                    BlockPos pos = notifier.offset(facing);
                    if (!event.getWorld().isAirBlock(pos)) {
                        Chunk chunk = event.getWorld().getChunk(pos);
                        if (chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                            IWardStorage storage = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                            if (storage.hasWard(pos)) {
                                event.getWorld().setBlockState(notifier, Blocks.AIR.getDefaultState(), 2);
                                event.setCanceled(true);
                                return;
                            }
                        }
                    }
                }
            }
            else if (event.getWorld().isAirBlock(event.getPos())) {
                Chunk chunk = event.getWorld().getChunk(event.getPos());
                if (chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                    IWardStorage storage = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                    if (storage instanceof IWardStorageServer && storage.hasWard(event.getPos()))
                        ((IWardStorageServer) storage).clearWard(event.getWorld(), event.getPos());
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!TAConfig.disableWardFocus.getValue()) {
            BlockPos pos = event.getPos();
            Chunk chunk = event.getWorld().getChunk(pos);
            if (chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                IWardStorage storage = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                if (storage instanceof IWardStorageServer && storage.hasWard(event.getPos()))
                    ((IWardStorageServer) storage).clearWard(event.getWorld(), event.getPos());
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockPlaceMulti(BlockEvent.EntityMultiPlaceEvent event) {
        if (!TAConfig.disableWardFocus.getValue()) {
            for (BlockSnapshot b : event.getReplacedBlockSnapshots()) {
                BlockPos pos = b.getPos();
                Chunk chunk = event.getWorld().getChunk(pos);
                if (chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                    IWardStorage storage = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                    if (storage.hasWard(pos) && (!(event.getEntity() instanceof EntityPlayer) || !((EntityPlayer) event.getEntity()).isCreative()))
                        event.setCanceled(true);
                    else if (storage instanceof IWardStorageServer && event.getEntity() instanceof EntityPlayer && 
                            ((EntityPlayer) event.getEntity()).isCreative())
                        ((IWardStorageServer) storage).clearWard(event.getWorld(), pos);
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDestruction(LivingDestroyBlockEvent event) {
        if (!TAConfig.disableWardFocus.getValue()) {
            BlockPos pos = event.getPos();
            Chunk chunk = event.getEntity().getEntityWorld().getChunk(pos);
            if (chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                IWardStorage storage = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                if (storage.hasWard(pos) && (!(event.getEntity() instanceof EntityPlayer) || !((EntityPlayer) event.getEntity()).isCreative()))
                    event.setCanceled(true);
                else if (storage instanceof IWardStorageServer && event.getEntity() instanceof EntityPlayer && 
                        ((EntityPlayer) event.getEntity()).isCreative())
                    ((IWardStorageServer) storage).clearWard(event.getEntity().getEntityWorld(), pos);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!TAConfig.disableWardFocus.getValue()) {
            HashSet<BlockPos> blockers = new HashSet<>();
            ListIterator<BlockPos> iterator = event.getAffectedBlocks().listIterator();
            while (iterator.hasNext()) {
                BlockPos pos = iterator.next();
                Chunk chunk = event.getWorld().getChunk(pos);
                if (chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null) && 
                        chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null).hasWard(pos)) {
                    blockers.add(pos);
                    iterator.remove();
                }
            }
            
            iterator = event.getAffectedBlocks().listIterator();
            while (iterator.hasNext()) {
                BlockPos pos = iterator.next();
                RayTraceResult ray = event.getWorld().rayTraceBlocks(event.getExplosion().getPosition(), new Vec3d(pos));
                if (ray != null && ray.typeOfHit == Type.BLOCK && blockers.contains(ray.getBlockPos()))
                    iterator.remove();
            }
            
            ListIterator<Entity> entityIterator = event.getAffectedEntities().listIterator();
            while (entityIterator.hasNext()) {
                Entity entity = entityIterator.next();
                RayTraceResult ray = event.getWorld().rayTraceBlocks(event.getExplosion().getPosition(), entity.getPositionVector());
                if (ray != null && ray.typeOfHit == Type.BLOCK && blockers.contains(ray.getBlockPos()))
                    entityIterator.remove();
                else {
                    ray = event.getWorld().rayTraceBlocks(event.getExplosion().getPosition(), entity.getPositionEyes(1.0F));
                    if (ray != null && ray.typeOfHit == Type.BLOCK && blockers.contains(ray.getBlockPos()))
                        entityIterator.remove();
                }
            }
        }
    }
    
}
