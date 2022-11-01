/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

import com.google.common.collect.Iterables;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.AugmentAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.event.AugmentEventHelper;
import thecodex6824.thaumicaugmentation.api.event.CastEvent;
import thecodex6824.thaumicaugmentation.api.util.DamageWrapper;
import thecodex6824.thaumicaugmentation.common.network.PacketAugmentableItemSync;
import thecodex6824.thaumicaugmentation.common.network.PacketBaubleChange;
import thecodex6824.thaumicaugmentation.common.network.PacketEntityCast;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

import java.util.*;
import java.util.function.Function;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class AugmentEventHandler {

    private AugmentEventHandler() {}
    
    private static final WeakHashMap<Entity, ArrayList<ItemStack>> oldItems = new WeakHashMap<>();
    private static final Set<Entity> hasAugments = Collections.newSetFromMap(new WeakHashMap<>());
    
    public static void onEquipmentChange(EntityLivingBase entity) {
        int totalIndex = 0;
        for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentableItemSources()) {
            Iterable<ItemStack> stacks = func.apply(entity);
            if (!oldItems.containsKey(entity))
                oldItems.put(entity, new ArrayList<>(Collections.nCopies(Iterables.size(stacks), ItemStack.EMPTY)));
            else if (oldItems.get(entity).size() < totalIndex + Iterables.size(stacks) + 1)
                oldItems.get(entity).addAll(Collections.nCopies(Iterables.size(stacks), ItemStack.EMPTY));
            
            int i = 0;
            Iterator<ItemStack> iterator = stacks.iterator();
            while (iterator.hasNext()) {
                ItemStack current = iterator.next();
                ArrayList<ItemStack> oldList = oldItems.get(entity);
                ItemStack old = oldList != null && oldList.size() > i ? oldList.get(i) : ItemStack.EMPTY;
                if (!ItemStack.areItemStacksEqual(current, old)) {
                    IAugmentableItem oldCap = old.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                    if (oldCap != null)
                        AugmentEventHelper.fireUnequipEvent(oldCap, entity);
                
                    IAugmentableItem currentCap = current.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                    if (currentCap != null) {
                        AugmentEventHelper.fireEquipEvent(currentCap, entity);
                        hasAugments.add(entity);
                        if (!entity.getEntityWorld().isRemote) {
                            TANetwork.INSTANCE.sendToAllTracking(new PacketAugmentableItemSync(entity.getEntityId(), totalIndex,
                                    currentCap.getSyncNBT()), entity);
                        }
                    }
                
                    if (oldList != null)
                        oldList.set(i, current);
                }
                
                ++i;
                ++totalIndex;
            }
        }
        
        if (totalIndex == 0)
            hasAugments.remove(entity);
    }
    
    @SubscribeEvent
    public static void onEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
        onEquipmentChange(event.getEntityLiving());
    }
    
    @SubscribeEvent(priority = EventPriority.LOW) // low priority to be after baubles syncs the items
    public static void onJoinWorld(EntityJoinWorldEvent event) {
        // force a check because baubles has no events
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
            onEquipmentChange((EntityPlayer) event.getEntity());
            PacketBaubleChange pkt = new PacketBaubleChange(event.getEntity().getEntityId());
            TANetwork.INSTANCE.sendToAllTracking(pkt, event.getEntity());
            if (event.getEntity() instanceof EntityPlayerMP)
                TANetwork.INSTANCE.sendTo(pkt, (EntityPlayerMP) event.getEntity());
        }
    }
    
    @SubscribeEvent
    public static void onTick(LivingUpdateEvent event) {
        if (hasAugments.contains(event.getEntity())) {
            boolean cancel = false;
            int totalIndex = 0;
            for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentableItemSources()) {
                Iterator<ItemStack> iterator = func.apply(event.getEntity()).iterator();
                while (iterator.hasNext()) {
                    ItemStack current = iterator.next();
                    IAugmentableItem cap = current.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                    if (cap != null) {
                        cancel |= AugmentEventHelper.fireTickEvent(cap, event.getEntity());
                        AugmentEventHelper.handleSync(cap, event.getEntity(), totalIndex);
                    }
                    
                    ++totalIndex;
                }
            }
            
            // the check is done this way to not un-cancel events should the handler want those
            if (cancel)
                event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            boolean cancel = false;
            DamageWrapper damage = new DamageWrapper(event.getAmount());
            for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentableItemSources()) {
                if (hasAugments.contains(event.getSource().getTrueSource())) {
                    for (ItemStack stack : func.apply(event.getSource().getTrueSource())) {
                        IAugmentableItem cap = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                        if (cap != null)
                            cancel |= AugmentEventHelper.fireHurtEntityEvent(cap, event.getSource(), event.getEntity(), damage);
                    }
                }
                
                if (hasAugments.contains(event.getEntity())) {
                    for (ItemStack stack : func.apply(event.getEntity())) {
                        IAugmentableItem cap = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                        if (cap != null)
                            cancel |= AugmentEventHelper.fireHurtByEntityEvent(cap, event.getEntity(), event.getSource(), damage);
                    }
                }
            }
            
            event.setAmount(damage.getDamage());
            if (cancel)
                event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            boolean cancel = false;
            DamageWrapper damage = new DamageWrapper(event.getAmount());
            for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentableItemSources()) {
                if (hasAugments.contains(event.getSource().getTrueSource())) {
                    for (ItemStack stack : func.apply(event.getSource().getTrueSource())) {
                        IAugmentableItem cap = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                        if (cap != null)
                            cancel |= AugmentEventHelper.fireDamageEntityEvent(cap, event.getSource(), event.getEntity(), damage);
                    }
                }
                
                if (hasAugments.contains(event.getEntity())) {
                    for (ItemStack stack : func.apply(event.getEntity())) {
                        IAugmentableItem cap = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                        if (cap != null)
                            cancel |= AugmentEventHelper.fireDamagedByEntityEvent(cap, event.getEntity(), event.getSource(), damage);
                    }
                }
            }
            
            event.setAmount(damage.getDamage());
            if (cancel)
                event.setCanceled(true);
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
