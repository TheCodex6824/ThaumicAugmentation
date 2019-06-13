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
import java.util.WeakHashMap;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.AugmentAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.event.AugmentEventHelper;
import thecodex6824.thaumicaugmentation.api.event.SuccessfulCastEvent;
import thecodex6824.thaumicaugmentation.common.network.PacketAugmentableItemSync;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class AugmentEventHandler {

    private AugmentEventHandler() {}
    
    private static WeakHashMap<Entity, ArrayList<ItemStack>> oldItems = new WeakHashMap<>();
    
    @SubscribeEvent
    public static void onTick(LivingUpdateEvent event) {
        if (!event.getEntityLiving().getEntityWorld().isRemote) {
            int totalIndex = 0;
            for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentItemSources()) {
                ArrayList<ItemStack> stacks = Lists.newArrayList(func.apply(event.getEntity()));
                if (!oldItems.containsKey(event.getEntity()))
                    oldItems.put(event.getEntity(), new ArrayList<>(stacks));
                
                for (int i = 0; i < stacks.size(); ++i) {
                    ItemStack current = stacks.get(i);
                    ArrayList<ItemStack> oldList = oldItems.get(event.getEntity());
                    ItemStack old = oldList.size() > i ? oldList.get(i) : ItemStack.EMPTY;
                    if (!ItemStack.areItemStacksEqual(current, old)) {
                        if (old.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null))
                            AugmentEventHelper.fireUnequipEvent(old.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), event.getEntity());
                    
                        if (current.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                            AugmentEventHelper.fireEquipEvent(current.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), event.getEntity());
                            TANetwork.INSTANCE.sendToAllTracking(new PacketAugmentableItemSync(event.getEntity().getEntityId(), totalIndex, current.getCapability(
                                    CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).serializeNBT()), event.getEntity());
                        }
                    
                        oldList.set(i, current);
                    }
                    
                    if (current.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                        IAugmentableItem cap = current.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                        AugmentEventHelper.fireTickEvent(cap, event.getEntity());
                        AugmentEventHelper.handleSync(cap, event.getEntity(), totalIndex);
                    }
                    
                    ++totalIndex;
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentItemSources()) {
                for (ItemStack stack : func.apply(event.getSource().getTrueSource())) {
                    if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                        AugmentEventHelper.fireHurtEntityEvent(stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                                event.getSource().getTrueSource(), event.getEntity());
                    }
                }
                
                for (ItemStack stack : func.apply(event.getEntity())) {
                    if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                        AugmentEventHelper.fireHurtByEntityEvent(stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                                event.getEntity(), event.getSource().getTrueSource());
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentItemSources()) {
                for (ItemStack stack : func.apply(event.getSource().getTrueSource())) {
                    if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                        AugmentEventHelper.fireDamageEntityEvent(stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                                event.getSource().getTrueSource(), event.getEntity());
                    }
                }
                
                for (ItemStack stack : func.apply(event.getEntity())) {
                    if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                        AugmentEventHelper.fireDamagedByEntityEvent(stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                                event.getEntity(), event.getSource().getTrueSource());
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onCast(SuccessfulCastEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote && event.getCasterStack().hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
            AugmentEventHelper.fireCastEvent(event.getCasterStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null), 
                    event.getCasterStack(), event.getFocusPackage(), event.getEntityLiving());
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
