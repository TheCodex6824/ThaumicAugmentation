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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

import com.google.common.collect.Iterables;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.AugmentAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.event.AugmentEventHelper;
import thecodex6824.thaumicaugmentation.api.event.CastEvent;
import thecodex6824.thaumicaugmentation.common.network.PacketAugmentableItemSync;
import thecodex6824.thaumicaugmentation.common.network.PacketEntityCast;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class AugmentEventHandler {

    private AugmentEventHandler() {}
    
    private static WeakHashMap<Entity, ArrayList<ItemStack>> oldItems = new WeakHashMap<>();
    private static Set<Entity> hasAugments = Collections.newSetFromMap(new WeakHashMap<>());
    
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        int totalIndex = 0;
        for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentableItemSources()) {
            Iterable<ItemStack> stacks = func.apply(event.getEntity());
            if (!oldItems.containsKey(event.getEntity()))
                oldItems.put(event.getEntity(), new ArrayList<>(Collections.nCopies(Iterables.size(stacks), ItemStack.EMPTY)));
            else {
                int i = 0;
                Iterator<ItemStack> iterator = stacks.iterator();
                while (iterator.hasNext()) {
                    ItemStack current = iterator.next();
                    ArrayList<ItemStack> oldList = oldItems.get(event.getEntity());
                    ItemStack old = oldList != null && oldList.size() > i ? oldList.get(i) : ItemStack.EMPTY;
                    if (!ItemStack.areItemStacksEqual(current, old)) {
                        if (old.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null))
                            AugmentEventHelper.fireUnequipEvent(old.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), event.getEntity());
                    
                        if (current.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                            AugmentEventHelper.fireEquipEvent(current.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), event.getEntity());
                            hasAugments.add(event.getEntity());
                            if (!event.getEntity().getEntityWorld().isRemote) {
                                TANetwork.INSTANCE.sendToAllTracking(new PacketAugmentableItemSync(event.getEntity().getEntityId(), totalIndex, current.getCapability(
                                        CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).getSyncNBT()), event.getEntity());
                            }
                        }
                    
                        if (oldList != null)
                            oldList.set(i, current);
                    }
                    
                    ++i;
                    ++totalIndex;
                }
            }
        }
        
        if (totalIndex == 0)
            hasAugments.remove(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onTick(LivingUpdateEvent event) {
        if (hasAugments.contains(event.getEntity())) {
            int totalIndex = 0;
            for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentableItemSources()) {
                Iterator<ItemStack> iterator = func.apply(event.getEntity()).iterator();
                while (iterator.hasNext()) {
                    ItemStack current = iterator.next();
                    if (current.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                        IAugmentableItem cap = current.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                        AugmentEventHelper.fireTickEvent(cap, event.getEntity());
                        AugmentEventHelper.handleSync(cap, event.getEntity(), totalIndex);
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentableItemSources()) {
                if (hasAugments.contains(event.getSource().getTrueSource())) {
                    for (ItemStack stack : func.apply(event.getSource().getTrueSource())) {
                        if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                            AugmentEventHelper.fireHurtEntityEvent(stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                                    event.getSource().getTrueSource(), event.getEntity());
                        }
                    }
                }
                
                if (hasAugments.contains(event.getEntity())) {
                    for (ItemStack stack : func.apply(event.getEntity())) {
                        if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                            AugmentEventHelper.fireHurtByEntityEvent(stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                                    event.getEntity(), event.getSource().getTrueSource());
                        }
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentableItemSources()) {
                if (hasAugments.contains(event.getSource().getTrueSource())) {
                    for (ItemStack stack : func.apply(event.getSource().getTrueSource())) {
                        if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                            AugmentEventHelper.fireDamageEntityEvent(stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                                    event.getSource().getTrueSource(), event.getEntity());
                        }
                    }
                }
                
                if (hasAugments.contains(event.getEntity())) {
                    for (ItemStack stack : func.apply(event.getEntity())) {
                        if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                            AugmentEventHelper.fireDamagedByEntityEvent(stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                                    event.getEntity(), event.getSource().getTrueSource());
                        }
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onCastPre(CastEvent.Pre event) {
        if (!event.getEntity().getEntityWorld().isRemote && event.getCasterStack().hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
            AugmentEventHelper.fireCastPreEvent(event.getCasterStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                    event.getCasterStack(), event.getFocus(), event.getEntityLiving());
        }
    }
    
    @SubscribeEvent
    public static void onCastPost(CastEvent.Post event) {
        if (!event.getEntity().getEntityWorld().isRemote && event.getCasterStack().hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
            AugmentEventHelper.fireCastPostEvent(event.getCasterStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                    event.getCasterStack(), event.getFocus(), event.getEntityLiving());
            
            if (event.getCasterStack().getItem() == TAItems.GAUNTLET) {
                if (event.getEntity() instanceof EntityPlayerMP)
                    TANetwork.INSTANCE.sendTo(new PacketEntityCast(event.getEntity().getEntityId()), (EntityPlayerMP) event.getEntity());
                
                TANetwork.INSTANCE.sendToAllTracking(new PacketEntityCast(event.getEntity().getEntityId()), event.getEntity());
            }
        }
    }
    
    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
            AugmentEventHelper.fireInteractEntityEvent(event.getItemStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                    event.getEntityPlayer(), event.getItemStack(), event.getTarget(), event.getHand());
        }
    }
    
    @SubscribeEvent
    public static void onInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
            AugmentEventHelper.fireInteractBlockEvent(event.getItemStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                    event.getEntityPlayer(), event.getItemStack(), event.getPos(), event.getFace(), event.getHand());
        }
    }
    
    @SubscribeEvent
    public static void onInteractAir(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
            AugmentEventHelper.fireInteractAirEvent(event.getItemStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                    event.getEntityPlayer(), event.getItemStack(), event.getHand());
        }
    }
    
    @SubscribeEvent
    public static void onUseItem(LivingEntityUseItemEvent.Tick event) {
        if (event.getItem().hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
            AugmentEventHelper.fireUseItemEvent(event.getItem().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                    event.getEntity(), event.getItem());
        }
    }
    
}
