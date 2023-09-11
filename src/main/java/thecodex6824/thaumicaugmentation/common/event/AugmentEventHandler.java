/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.event.AugmentEventHelper;
import thecodex6824.thaumicaugmentation.api.event.CastEvent;
import thecodex6824.thaumicaugmentation.api.util.DamageWrapper;
import thecodex6824.thaumicaugmentation.common.capability.CapabilityEquipmentTracker;
import thecodex6824.thaumicaugmentation.common.capability.IEquipmentTracker;
import thecodex6824.thaumicaugmentation.common.network.PacketBaubleChange;
import thecodex6824.thaumicaugmentation.common.network.PacketEntityCast;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class AugmentEventHandler {

    private AugmentEventHandler() {}
    
    @SubscribeEvent
    public static void onEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
    	EquipmentSyncEventHandler.onEquipmentChange(event.getEntityLiving());
    }
    
    @SubscribeEvent(priority = EventPriority.LOW) // low priority to be after baubles syncs the items
    public static void onJoinWorld(EntityJoinWorldEvent event) {
        // force a check because baubles has no events
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
        	EquipmentSyncEventHandler.onEquipmentChange((EntityPlayer) event.getEntity());
            PacketBaubleChange pkt = new PacketBaubleChange(event.getEntity().getEntityId());
            TANetwork.INSTANCE.sendToAllTracking(pkt, event.getEntity());
            if (event.getEntity() instanceof EntityPlayerMP)
                TANetwork.INSTANCE.sendTo(pkt, (EntityPlayerMP) event.getEntity());
        }
    }
    
    @SubscribeEvent
    public static void onTick(LivingUpdateEvent event) {
    	IEquipmentTracker tracker = event.getEntity().getCapability(CapabilityEquipmentTracker.EQUIPMENT_TRACKER, null);
        if (tracker != null && tracker.hasAugments()) {
            boolean cancel = false;
            IItemHandler inv = tracker.getLiveEquipment();
            for (int i : tracker.getAugmentSlots()) {
            	ItemStack current = inv.getStackInSlot(i);
                IAugmentableItem cap = current.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                if (cap != null) {
                    cancel |= AugmentEventHelper.fireTickEvent(cap, event.getEntity());
                    AugmentEventHelper.handleSync(cap, event.getEntity(), i);
                }
            }
            
            // the check is done this way to not un-cancel events should the handler want those
            if (cancel) {
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            boolean cancel = false;
            DamageWrapper damage = new DamageWrapper(event.getAmount());
            if (event.getSource().getTrueSource() != null) {
            	Entity source = event.getSource().getTrueSource();
	            IEquipmentTracker sourceTracker = source.getCapability(CapabilityEquipmentTracker.EQUIPMENT_TRACKER, null);
	            if (sourceTracker != null && sourceTracker.hasAugments()) {
	            	IItemHandler inv = sourceTracker.getLiveEquipment();
	            	for (int i : sourceTracker.getAugmentSlots()) {
	            		ItemStack stack = inv.getStackInSlot(i);
	                    IAugmentableItem cap = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
	                    if (cap != null) {
	                        cancel |= AugmentEventHelper.fireHurtEntityEvent(cap, event.getSource(), event.getEntity(), damage);
	                    }
	            	}
	            }
            }
            
            IEquipmentTracker targetTracker = event.getEntity().getCapability(CapabilityEquipmentTracker.EQUIPMENT_TRACKER, null);
            if (targetTracker != null && targetTracker.hasAugments()) {
            	IItemHandler inv = targetTracker.getLiveEquipment();
            	for (int i = 0; i < inv.getSlots(); ++i) {
            		ItemStack stack = inv.getStackInSlot(i);
                    IAugmentableItem cap = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                    if (cap != null) {
                        cancel |= AugmentEventHelper.fireHurtByEntityEvent(cap, event.getEntity(), event.getSource(), damage);
                    }
            	}
            }
            
            event.setAmount(damage.getDamage());
            if (cancel) {
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            boolean cancel = false;
            DamageWrapper damage = new DamageWrapper(event.getAmount());
            if (event.getSource().getTrueSource() != null) {
            	Entity source = event.getSource().getTrueSource();
	            IEquipmentTracker sourceTracker = source.getCapability(CapabilityEquipmentTracker.EQUIPMENT_TRACKER, null);
	            if (sourceTracker != null && sourceTracker.hasAugments()) {
	            	IItemHandler inv = sourceTracker.getLiveEquipment();
	                for (int i = 0; i < inv.getSlots(); ++i) {
	                	ItemStack stack = inv.getStackInSlot(i);
	                    IAugmentableItem cap = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
	                    if (cap != null) {
	                        cancel |= AugmentEventHelper.fireDamageEntityEvent(cap, event.getSource(), event.getEntity(), damage);
	                    }
	                }
	            }
            }
            
            IEquipmentTracker targetTracker = event.getEntity().getCapability(CapabilityEquipmentTracker.EQUIPMENT_TRACKER, null);
            if (targetTracker != null && targetTracker.hasAugments()) {
            	IItemHandler inv = targetTracker.getLiveEquipment();
                for (int i = 0; i < inv.getSlots(); ++i) {
                	ItemStack stack = inv.getStackInSlot(i);
                    IAugmentableItem cap = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                    if (cap != null) {
                        cancel |= AugmentEventHelper.fireDamagedByEntityEvent(cap, event.getEntity(), event.getSource(), damage);
                    }
                }
            }
            
            event.setAmount(damage.getDamage());
            if (cancel) {
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onCastPre(CastEvent.Pre event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            IAugmentableItem cap = event.getCasterStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
            if (cap != null && AugmentEventHelper.fireCastPreEvent(cap, event.getCasterStack(), event.getFocus(),
                    event.getEntityLiving())) {
                    
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onCastPost(CastEvent.Post event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            IAugmentableItem cap = event.getCasterStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
            if (cap != null) {
                AugmentEventHelper.fireCastPostEvent(cap, event.getCasterStack(), event.getFocus(),
                        event.getEntityLiving());
                
                if (event.getCasterStack().getItem() == TAItems.GAUNTLET) {
                    if (event.getEntity() instanceof EntityPlayerMP)
                        TANetwork.INSTANCE.sendTo(new PacketEntityCast(event.getEntity().getEntityId()), (EntityPlayerMP) event.getEntity());
                    
                    TANetwork.INSTANCE.sendToAllTracking(new PacketEntityCast(event.getEntity().getEntityId()), event.getEntity());
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        IAugmentableItem cap = event.getItemStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (cap != null && AugmentEventHelper.fireInteractEntityEvent(cap, event.getEntityPlayer(), event.getItemStack(),
                event.getTarget(), event.getHand())) {
            
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        IAugmentableItem cap = event.getItemStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (cap != null && AugmentEventHelper.fireInteractBlockEvent(cap, event.getEntityPlayer(), event.getItemStack(),
                event.getPos(), event.getFace(), event.getHand())) {
                
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onInteractAir(PlayerInteractEvent.RightClickItem event) {
        IAugmentableItem cap = event.getItemStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (cap != null && AugmentEventHelper.fireInteractAirEvent(cap, event.getEntityPlayer(), event.getItemStack(),
                event.getHand())) {
                
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onUseItem(LivingEntityUseItemEvent.Tick event) {
        IAugmentableItem cap = event.getItem().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (cap != null && AugmentEventHelper.fireUseItemEvent(cap, event.getEntity(), event.getItem()))
            event.setCanceled(true);
    }
    
}
