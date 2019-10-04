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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class MorphicEventHandler {

    /*
     * An implementation note: these event handlers are here because quite a few item-related events are posted outside the Item code,
     * so I can't do anything to them without listeners. Additionally, this is not an exhaustive collection of 
     * every single event possible that can be fired, instead I put the few that I think would most commonly be used.
     * I don't think that most modded items would need events working to be at least functional - most of my 
     * worry comes from vanilla items that have been overhauled/changed with extra functions. I also don't
     * want to add too many events, because the poor GC is already getting worked hard enough by BlockPos and
     * friends...
     */
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getItem().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
            ItemStack func = event.getItem().getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack();
            if (!func.isEmpty()) {
                LivingEntityUseItemEvent.Start newEvent = new LivingEntityUseItemEvent.Start(event.getEntityLiving(),
                        func, event.getDuration());
                MinecraftForge.EVENT_BUS.post(newEvent);
                event.setDuration(newEvent.getDuration());
                event.setCanceled(newEvent.isCanceled());
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getItem().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
            ItemStack func = event.getItem().getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack();
            if (!func.isEmpty()) {
                LivingEntityUseItemEvent.Tick newEvent = new LivingEntityUseItemEvent.Tick(event.getEntityLiving(),
                        func, event.getDuration());
                MinecraftForge.EVENT_BUS.post(newEvent);
                event.setDuration(newEvent.getDuration());
                event.setCanceled(newEvent.isCanceled());
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onUseStop(LivingEntityUseItemEvent.Stop event) {
        if (event.getItem().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
            ItemStack func = event.getItem().getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack();
            if (!func.isEmpty()) {
                LivingEntityUseItemEvent.Stop newEvent = new LivingEntityUseItemEvent.Stop(event.getEntityLiving(),
                        func, event.getDuration());
                MinecraftForge.EVENT_BUS.post(newEvent);
                event.setDuration(newEvent.getDuration());
                event.setCanceled(newEvent.isCanceled());
            }
        }
    }
    
    @SuppressWarnings("null")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
            ItemStack func = event.getItem().getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack();
            if (!func.isEmpty()) {
                LivingEntityUseItemEvent.Finish newEvent = new LivingEntityUseItemEvent.Finish(event.getEntityLiving(),
                        func, event.getDuration(),
                        event.getResultStack().getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack());
                MinecraftForge.EVENT_BUS.post(newEvent);
                event.setDuration(newEvent.getDuration());
                // event not cancelable
            }
        }
    }
    
    @SuppressWarnings("null")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        boolean fromMorphic = event.getFrom().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null) &&
                !event.getFrom().getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().isEmpty();
        boolean toMorphic = event.getTo().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null) &&
                !event.getTo().getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().isEmpty();
        if (fromMorphic || toMorphic) {
            LivingEquipmentChangeEvent newEvent = new LivingEquipmentChangeEvent(event.getEntityLiving(), event.getSlot(),
                    fromMorphic ? event.getFrom().getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack() : event.getFrom(),
                    toMorphic ? event.getTo().getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack() : event.getFrom());
            MinecraftForge.EVENT_BUS.post(newEvent);
        }
    }
    
    private static void setStackSilently(EntityLivingBase entity, EnumHand hand, ItemStack stack) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (hand == EnumHand.MAIN_HAND)
                player.inventory.mainInventory.set(player.inventory.currentItem, stack);
            else if (hand == EnumHand.OFF_HAND)
                player.inventory.offHandInventory.set(0, stack);
        }
        else
            entity.setHeldItem(hand, stack);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
            ItemStack old = event.getItemStack();
            if (!old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().isEmpty()) {
                setStackSilently(event.getEntityPlayer(), event.getHand(), old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack());
                PlayerInteractEvent.EntityInteract newEvent = new PlayerInteractEvent.EntityInteract(
                        event.getEntityPlayer(), event.getHand(), event.getTarget());
                MinecraftForge.EVENT_BUS.post(newEvent);
                setStackSilently(event.getEntityPlayer(), event.getHand(), old);
                event.setCanceled(newEvent.isCanceled());
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteractEntitySpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getItemStack().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
            ItemStack old = event.getItemStack();
            if (!old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().isEmpty()) {
                setStackSilently(event.getEntityPlayer(), event.getHand(), old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack());
                PlayerInteractEvent.EntityInteractSpecific newEvent = new PlayerInteractEvent.EntityInteractSpecific(
                        event.getEntityPlayer(), event.getHand(), event.getTarget(), event.getLocalPos());
                MinecraftForge.EVENT_BUS.post(newEvent);
                setStackSilently(event.getEntityPlayer(), event.getHand(), old);
                event.setCanceled(newEvent.isCanceled());
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteractLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getItemStack().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
            ItemStack old = event.getItemStack();
            if (!old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().isEmpty()) {
                setStackSilently(event.getEntityPlayer(), event.getHand(), old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack());
                PlayerInteractEvent.LeftClickBlock newEvent = new PlayerInteractEvent.LeftClickBlock(
                        event.getEntityPlayer(), event.getPos(), event.getFace(), event.getHitVec());
                MinecraftForge.EVENT_BUS.post(newEvent);
                setStackSilently(event.getEntityPlayer(), event.getHand(), old);
                event.setUseBlock(newEvent.getUseBlock());
                event.setUseItem(newEvent.getUseItem());
                event.setCanceled(newEvent.isCanceled());
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteractRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
            ItemStack old = event.getItemStack();
            if (!old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().isEmpty()) {
                setStackSilently(event.getEntityPlayer(), event.getHand(), old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack());
                PlayerInteractEvent.RightClickBlock newEvent = new PlayerInteractEvent.RightClickBlock(
                        event.getEntityPlayer(), event.getHand(), event.getPos(), event.getFace(), event.getHitVec());
                MinecraftForge.EVENT_BUS.post(newEvent);
                setStackSilently(event.getEntityPlayer(), event.getHand(), old);
                event.setUseBlock(newEvent.getUseBlock());
                event.setUseItem(newEvent.getUseItem());
                event.setCanceled(newEvent.isCanceled());
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteractRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
            ItemStack old = event.getItemStack();
            if (!old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().isEmpty()) {
                setStackSilently(event.getEntityPlayer(), event.getHand(), old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack());
                PlayerInteractEvent.RightClickItem newEvent = new PlayerInteractEvent.RightClickItem(
                        event.getEntityPlayer(), event.getHand());
                MinecraftForge.EVENT_BUS.post(newEvent);
                setStackSilently(event.getEntityPlayer(), event.getHand(), old);
                event.setCanceled(newEvent.isCanceled());
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && event.getPlayer().getActiveHand() != null) {
            EnumHand hand = event.getPlayer().getActiveHand();
            ItemStack stack = event.getPlayer().getHeldItem(hand);
            if (stack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
                if (!stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().isEmpty()) {
                    setStackSilently(event.getPlayer(), hand, stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack());
                    BlockEvent.BreakEvent newEvent = new BlockEvent.BreakEvent(event.getWorld(), event.getPos(),
                            event.getState(), event.getPlayer());
                    MinecraftForge.EVENT_BUS.post(newEvent);
                    setStackSilently(event.getPlayer(), hand, stack);
                    event.setCanceled(newEvent.isCanceled());
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockHarvest(BlockEvent.HarvestDropsEvent event) {
        if (event.getHarvester() != null && event.getHarvester().getActiveHand() != null) {
            EnumHand hand = event.getHarvester().getActiveHand();
            ItemStack stack = event.getHarvester().getHeldItem(hand);
            if (stack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null)) {
                if (!stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().isEmpty()) {
                    setStackSilently(event.getHarvester(), hand, stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack());
                    BlockEvent.HarvestDropsEvent newEvent = new BlockEvent.HarvestDropsEvent(event.getWorld(), event.getPos(),
                            event.getState(), event.getFortuneLevel(), event.getDropChance(), event.getDrops(),
                            event.getHarvester(), event.isSilkTouching());
                    MinecraftForge.EVENT_BUS.post(newEvent);
                    setStackSilently(event.getHarvester(), hand, stack);
                    event.setDropChance(newEvent.getDropChance());
                    // can't cancel
                }
            }
        }
    }
    
}
