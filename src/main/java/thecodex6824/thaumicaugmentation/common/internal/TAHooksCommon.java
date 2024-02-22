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

import java.util.Random;

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.items.casters.foci.FocusMediumTouch;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.event.EntityInOuterLandsEvent;
import thecodex6824.thaumicaugmentation.api.event.FluxRiftDestroyBlockEvent;
import thecodex6824.thaumicaugmentation.api.event.FocusTouchGetEntityEvent;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
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

    public static boolean checkWardFlammability(IBlockAccess access, BlockPos pos) {
	boolean flammableAllowed = true;
	if (access instanceof World) {
	    flammableAllowed = !hasWard((World) access, pos);
	}

	return flammableAllowed;
    }

    public static int checkWardFlammability(int oldValue, World world, BlockPos pos) {
	// this return will be bitwise anded with the original fire encouragement/flammability value
	// the oldValue == 0 check is to skip the capability lookup if there is no point
	return oldValue == 0 || hasWard(world, pos) ? 0 : oldValue;
    }

    public static int checkWardNeighborFireEncouragement(int oldValue, World world, BlockPos pos, EnumFacing facing) {
	// same deal as above
	return oldValue == 0 || hasWard(world, pos.offset(facing)) ? 0 : oldValue;
    }

    public static boolean checkWardRandomTick(WorldServer world, BlockPos pos, IBlockState state, Random rand) {
	return !hasWard(world, pos);
    }

    public static boolean checkWardGeneric(World world, BlockPos pos) {
	return !hasWard(world, pos);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean isCompatibleSlab(World world, BlockPos pos, EnumFacing dir, ItemStack slab) {
	IBlockState state = world.getBlockState(pos);
	if (state.getPropertyKeys().contains(BlockSlab.HALF) && state.getBlock() instanceof BlockSlab) {
	    BlockSlab block = (BlockSlab) state.getBlock();
	    Comparable item = block.getTypeForItem(slab);
	    if (dir == EnumFacing.UP && state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM ||
		    dir == EnumFacing.DOWN && state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP) {

		Object value = state.getValue(block.getVariantProperty());
		try {
		    return item.compareTo(value) == 0;
		}
		catch (ClassCastException ex) {
		    // if getTypeForItem and the variant property value have different types, this may happen
		    // ex: rustic slabs return int for getTypeForItem, but vanilla slabs use EnumBlockHalf
		}
	    }
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
	World world = entity.getEntityWorld();
	if (world != null && world.provider != null) {
	    EntityInOuterLandsEvent event = new EntityInOuterLandsEvent(entity);
	    MinecraftForge.EVENT_BUS.post(event);
	    return event.getResult() == Result.ALLOW || (event.getResult() == Result.DEFAULT &&
		    world.provider.getDimension() == TADimensions.EMPTINESS.getId());
	}

	return false;
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

    private static int[] getValidMetadata(Item item) {
	IntRBTreeSet visitedMeta = new IntRBTreeSet();
	for (CreativeTabs tab : item.getCreativeTabs()) {
	    NonNullList<ItemStack> stacks = NonNullList.create();
	    item.getSubItems(tab, stacks);
	    for (ItemStack stack : stacks) {
		if (stack.getItem() == item)
		    visitedMeta.add(stack.getMetadata());
	    }
	}

	return visitedMeta.toIntArray();
    }

    public static ItemStack cycleItemStack(ItemStack fallback, Object thing, int counter) {
	if (thing instanceof ItemStack) {
	    ItemStack stack = (ItemStack) thing;
	    if (!stack.isEmpty() && stack.getHasSubtypes() && stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
		int[] validMeta = getValidMetadata(stack.getItem());
		if (validMeta.length > 0) {
		    int timer = 5000 / validMeta.length;
		    int metaIndex = (int) ((counter + System.currentTimeMillis() / timer) % validMeta.length);
		    ItemStack copy = stack.copy();
		    copy.setItemDamage(validMeta[metaIndex]);
		    return copy;
		}
	    }
	}

	return fallback;
    }

    public static boolean onAddTile(Chunk chunk, BlockPos pos, TileEntity tile) {
	return !tile.hasCapability(CapabilityImpetusNode.IMPETUS_NODE, null) ||
		chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) != tile;
    }

}
