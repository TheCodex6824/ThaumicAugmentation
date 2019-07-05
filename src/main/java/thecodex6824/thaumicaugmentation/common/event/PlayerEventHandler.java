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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumResearchFlag;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.entity.PlayerMovementAbilityManager;
import thecodex6824.thaumicaugmentation.api.item.IArmorReduceFallDamage;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class PlayerEventHandler {

    private PlayerEventHandler() {}
    
    @SubscribeEvent
    public static void onJoin(PlayerLoggedInEvent event) {
        TAConfigHolder.loadOrSyncConfig(event.player);
        if (!ThaumcraftCapabilities.knowsResearchStrict(event.player, "THAUMIC_AUGMENTATION_BASE@1") &&
                (ThaumcraftCapabilities.knowsResearchStrict(event.player, "FIRSTSTEPS") || ThaumcraftCapabilities.knowsResearchStrict(event.player, "~FIRSTSTEPS"))) {
    
            ThaumcraftCapabilities.getKnowledge(event.player).addResearch("THAUMIC_AUGMENTATION_BASE");
            ThaumcraftCapabilities.getKnowledge(event.player).setResearchFlag("THAUMIC_AUGMENTATION_BASE", EnumResearchFlag.RESEARCH);
            ThaumcraftCapabilities.getKnowledge(event.player).setResearchStage("THAUMIC_AUGMENTATION_BASE", 2);
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof EntityPlayer && PlayerMovementAbilityManager.isValidSideForMovement((EntityPlayer) event.getEntity()))
            PlayerMovementAbilityManager.onJump((EntityPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof EntityPlayer && PlayerMovementAbilityManager.isValidSideForMovement((EntityPlayer) event.getEntity()))
            PlayerMovementAbilityManager.tick((EntityPlayer) event.getEntity());
        
        if (!event.getEntity().getEntityWorld().isRemote && event.getEntity().getEntityWorld().getTotalWorldTime() % 40 == 0 &&
                event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if (!TAConfig.disableEmptiness.getValue() && player.getEntityWorld().provider.getDimension() == TADimensions.EMPTINESS.getId() && 
                    !ThaumcraftCapabilities.knowsResearchStrict(player, "m_ENTERVOID")) {
                ThaumcraftCapabilities.getKnowledge(player).addResearch("m_ENTERVOID");
                player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.entered_void"), true);
            }
        }
    }

    @SubscribeEvent
    public static void onFallFirst(LivingAttackEvent event) {
        // damage can't be reduced to non-zero here, but cancelling it removes the screen shake and damage sound
        if (event.getSource() == DamageSource.FALL) {
            float damage = event.getAmount();
            for (ItemStack stack : event.getEntityLiving().getArmorInventoryList()) {
                if (stack.getItem() instanceof IArmorReduceFallDamage) {
                    damage = ((IArmorReduceFallDamage) stack.getItem()).getNewFallDamage(stack, damage, event.getEntityLiving().fallDistance);
                }
            }

            damage = Math.max(0.0F, damage);
            if (damage < 1.0F) 
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFallDamage(LivingHurtEvent event) {
        // this is needed to actually reduce damage if it's not 0
        if (event.getSource() == DamageSource.FALL) {
            float damage = event.getAmount();
            for (ItemStack stack : event.getEntityLiving().getArmorInventoryList()) {
                if (stack.getItem() instanceof IArmorReduceFallDamage) {
                    damage = ((IArmorReduceFallDamage) stack.getItem()).getNewFallDamage(stack, damage, event.getEntityLiving().fallDistance);
                }
            }

            damage = Math.max(0.0F, damage);
            if (damage < 1.0F) {
                event.setAmount(0.0F);
                event.setCanceled(true);
            }
            else
                event.setAmount(damage);
        }
    }

    @SubscribeEvent
    public static void onFallSound(PlaySoundAtEntityEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if (event.getSound() == SoundEvents.ENTITY_PLAYER_BIG_FALL || event.getSound() == SoundEvents.ENTITY_PLAYER_SMALL_FALL) {
                boolean shouldSilenceFall = false;
                for (ItemStack stack : player.getArmorInventoryList()) {
                    if (stack.getItem() instanceof IArmorReduceFallDamage) {
                        shouldSilenceFall = true;
                        break;
                    }
                }

                if (shouldSilenceFall)
                    event.setCanceled(true);
            }
        }
    }

}
