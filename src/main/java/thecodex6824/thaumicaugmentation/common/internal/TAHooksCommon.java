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

package thecodex6824.thaumicaugmentation.common.internal;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.items.casters.foci.FocusMediumTouch;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.event.EntityInOuterLandsEvent;
import thecodex6824.thaumicaugmentation.api.event.FluxRiftDestroyBlockEvent;
import thecodex6824.thaumicaugmentation.api.event.FocusTouchGetEntityEvent;
import thecodex6824.thaumicaugmentation.api.ward.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorageServer;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.common.event.AugmentEventHandler;
import thecodex6824.thaumicaugmentation.common.item.trait.IElytraCompat;
import thecodex6824.thaumicaugmentation.common.network.PacketBaubleChange;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.util.MorphicArmorHelper;
import thecodex6824.thaumicaugmentation.common.world.ChunkGeneratorEmptiness;
import thecodex6824.thaumicaugmentation.common.world.structure.MapGenEldritchSpire;

import javax.annotation.Nullable;
import java.util.Random;

public final class TAHooksCommon {

    private TAHooksCommon() {}
    
    private static boolean hasWard(World world, BlockPos pos) {
        if (world != null && pos != null && world.getChunkProvider() != null && world.isBlockLoaded(pos)) {
            Chunk chunk = world.getChunk(pos);
            if (chunk != null) {
                IWardStorage ward = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                if (ward != null)
                    return ward.hasWard(pos);
            }
        }
        
        return false;
    }
    
    public static float checkWardHardness(float oldHardness, World world, BlockPos pos) {
        return hasWard(world, pos) ? -1.0F : oldHardness;
    }
    
    public static float checkWardResistance(float oldResistance, World world, BlockPos pos) {
        return hasWard(world, pos) ? 6000000.0F : oldResistance;
    }
    
    public static int checkWardFlammability(int oldFlammability, IBlockAccess access, BlockPos pos) {
        if (oldFlammability == 0)
            return 0;
        else if (access instanceof World)
            return hasWard((World) access, pos) ? 0 : oldFlammability;
        else
            return oldFlammability;
    }
    
    public static int checkWardFireEncouragement(int oldEncouragement, IBlockAccess access, BlockPos pos) {
        if (oldEncouragement == 0)
            return 0;
        else if (access instanceof World)
            return hasWard((World) access, pos) ? 0 : oldEncouragement;
        else
            return oldEncouragement;
    }
    
    public static boolean checkWardRandomTick(WorldServer world, BlockPos pos, IBlockState state, Random rand) {
        return !hasWard(world, pos);
    }
    
    public static boolean checkWardGeneric(World world, BlockPos pos) {
        return !hasWard(world, pos);
    }

    @SuppressWarnings("rawtypes")
    private static boolean isCompatibleSlab(World world, BlockPos pos, EnumFacing dir, ItemStack slab) {
        IBlockState state = world.getBlockState(pos);
        if (state.getPropertyKeys().contains(BlockSlab.HALF) && state.getBlock() instanceof BlockSlab) {
            BlockSlab block = (BlockSlab) state.getBlock();
            Comparable item = block.getTypeForItem(slab);
            return (dir == EnumFacing.UP && state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM ||
                    dir == EnumFacing.DOWN && state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP) &&
                    item.compareTo(state.getValue(block.getVariantProperty())) == 0;
        }

        return false;
    }

    public static boolean checkWardSlab(World world, BlockPos pos, EnumFacing placeDir, ItemStack stack) {
        if (placeDir.getAxis() == EnumFacing.Axis.Y && isCompatibleSlab(world, pos, placeDir, stack))
            return !hasWard(world, pos) && !hasWard(world, pos.offset(placeDir));
        else
            return !hasWard(world, pos.offset(placeDir));
    }
    
    public static void checkElytra(ItemStack chestArmorStack, EntityPlayerMP player) {
        IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
        if (baubles != null) {
            ItemStack stack = baubles.getStackInSlot(BaubleType.BODY.getValidSlots()[0]);
            if (stack.getItem() instanceof IElytraCompat && ((IElytraCompat) stack.getItem()).allowElytraFlight(player, stack))
                player.setElytraFlying();
        }
    }
    
    public static boolean updateElytraFlag(EntityLivingBase entity, boolean flag) {
        if (flag)
            return true;
        else if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
            if (baubles != null) {
                ItemStack stack = baubles.getStackInSlot(BaubleType.BODY.getValidSlots()[0]);
                if (stack.getItem() instanceof IElytraCompat)
                    return ((IElytraCompat) stack.getItem()).allowElytraFlight(player, stack);
            }
        }
        
        return false;
    }
    
    public static ItemStack getLeftoverInfusionIngredientStack(ItemStack input, Object output) {
        if (output instanceof ItemStack && input.getItem() != ItemsTC.primordialPearl) {
            if (((ItemStack) output).getItem() == TAItems.MORPHIC_TOOL)
                return ItemStack.EMPTY;
            else if (MorphicArmorHelper.hasMorphicArmor((ItemStack) output))
                return ItemStack.EMPTY;
        }
            
        return input;
    }
    
    public static void onBaubleChanged(@Nullable EntityLivingBase entity) {
        if (entity != null && !entity.getEntityWorld().isRemote) {
            AugmentEventHandler.onEquipmentChange(entity);
            PacketBaubleChange pkt = new PacketBaubleChange(entity.getEntityId());
            TANetwork.INSTANCE.sendToAllTracking(pkt, entity);
            if (entity instanceof EntityPlayerMP)
                TANetwork.INSTANCE.sendTo(pkt, (EntityPlayerMP) entity);
        }
    }
    
    public static boolean isInOuterLands(Entity entity) {
        EntityInOuterLandsEvent event = new EntityInOuterLandsEvent(entity);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult() == Result.ALLOW || (event.getResult() == Result.DEFAULT &&
                entity.getEntityWorld().provider.getDimension() == TADimensions.EMPTINESS.getId());
    }
    
    public static boolean shouldAllowRunicShield(ItemStack stack) {
        return stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null);
    }
    
    public static RayTraceResult fireTrajectoryGetEntityEvent(RayTraceResult original, FocusMediumTouch touch, Trajectory trajectory, double range) {
        if (original != null && original.entityHit != null) {
            FocusTouchGetEntityEvent.Trajectory event = new FocusTouchGetEntityEvent.Trajectory(touch, trajectory, original, range);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled())
                return event.getRay();
            else
                return new RayTraceResult(null);
        }
        
        return original;
    }
    
    public static RayTraceResult fireTargetGetEntityEvent(RayTraceResult original, FocusMediumTouch touch, Trajectory trajectory, double range) {
        if (original != null && original.entityHit != null) {
            FocusTouchGetEntityEvent.Target event = new FocusTouchGetEntityEvent.Target(touch, trajectory, original, range);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled())
                return event.getRay();
            else
                return new RayTraceResult(null);
        }
        
        return original;
    }
    
    public static boolean fireFluxRiftDestroyBlockEvent(EntityFluxRift rift, BlockPos pos, IBlockState state) {
        return MinecraftForge.EVENT_BUS.post(new FluxRiftDestroyBlockEvent(rift, pos, state));
    }
    
    public static boolean onAttemptTeleport(EntityLivingBase entity, double origX, double origY, double origZ) {
        if (!entity.getEntityWorld().isRemote) {
            WorldServer w = (WorldServer) entity.getEntityWorld();
            BlockPos check = entity.getPosition();
            if (w.getChunkProvider().isInsideStructure(w, "EldritchSpire", check)) {
                MapGenEldritchSpire.Start start = ((ChunkGeneratorEmptiness) w.getChunkProvider().chunkGenerator).getSpireStart(check);
                if (start != null) {
                    IWardStorage storage = w.getChunk(check).getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                    return !(storage instanceof IWardStorageServer && ((IWardStorageServer) storage).isWardOwner(start.getWard()));
                }
            }
        }
        
        return true;
    }

    public static boolean checkSweepingEdge(EntityPlayer player, ItemStack stack) {
        return stack.getItem() == TAItems.PRIMAL_CUTTER;
    }
    
}
