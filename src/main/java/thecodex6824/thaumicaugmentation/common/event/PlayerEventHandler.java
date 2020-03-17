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

import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import thaumcraft.api.capabilities.IPlayerKnowledge.EnumResearchFlag;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.entity.PlayerMovementAbilityManager;
import thecodex6824.thaumicaugmentation.api.item.IArmorReduceFallDamage;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;
import thecodex6824.thaumicaugmentation.common.network.PacketFlightState;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class PlayerEventHandler {

    private static final WeakHashMap<Entity, Float> FALL_DAMAGE = new WeakHashMap<>();
    private static final WeakHashMap<EntityPlayer, Boolean> CREATIVE_FLIGHT = new WeakHashMap<>();
    
    private PlayerEventHandler() {}
    
    @SubscribeEvent
    @SuppressWarnings("null")
    public static void onJoin(PlayerLoggedInEvent event) {
        TAConfigHolder.loadOrSyncConfig(event.player);
        if (!ThaumcraftCapabilities.knowsResearchStrict(event.player, "THAUMIC_AUGMENTATION_BASE@1") &&
                (ThaumcraftCapabilities.knowsResearch(event.player, "FIRSTSTEPS") || ThaumcraftCapabilities.knowsResearch(event.player, "~FIRSTSTEPS"))) {
    
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

    protected static void checkResearch(EntityPlayer player) {
        if (!TAConfig.disableEmptiness.getValue() && player.getEntityWorld().provider.getDimension() == TADimensions.EMPTINESS.getId() && 
                        !ThaumcraftCapabilities.knowsResearchStrict(player, "m_ENTERVOID")) {
                    
            ThaumcraftCapabilities.getKnowledge(player).addResearch("m_ENTERVOID");
            player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.entered_void").setStyle(
                    new Style().setColor(TextFormatting.DARK_PURPLE)), true);
        }
        
        Biome biome = player.getEntityWorld().getBiome(player.getPosition());
        if (BiomeDictionary.hasType(biome, Type.OCEAN) && !ThaumcraftCapabilities.knowsResearchStrict(player, "m_OCEAN")) {
            ThaumcraftCapabilities.getKnowledge(player).addResearch("m_OCEAN");
            player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.ocean").setStyle(
                    new Style().setColor(TextFormatting.DARK_PURPLE)), true);
        }
        
        if (BiomeDictionary.hasType(biome, Type.MOUNTAIN) && !ThaumcraftCapabilities.knowsResearchStrict(player, "m_MOUNTAIN")) {
            ThaumcraftCapabilities.getKnowledge(player).addResearch("m_MOUNTAIN");
            player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.mountain").setStyle(
                    new Style().setColor(TextFormatting.DARK_PURPLE)), true);
        }
        
        if (BiomeDictionary.hasType(biome, Type.SANDY) && BiomeDictionary.hasType(biome, Type.HOT) && !ThaumcraftCapabilities.knowsResearchStrict(player, "m_DESERT")) {
            ThaumcraftCapabilities.getKnowledge(player).addResearch("m_DESERT");
            player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.desert").setStyle(
                    new Style().setColor(TextFormatting.DARK_PURPLE)), true);
        }
        
        if (BiomeDictionary.hasType(biome, Type.FOREST) && !ThaumcraftCapabilities.knowsResearchStrict(player, "m_FOREST")) {
            ThaumcraftCapabilities.getKnowledge(player).addResearch("m_FOREST");
            player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.forest").setStyle(
                    new Style().setColor(TextFormatting.DARK_PURPLE)), true);
        }
        
        StatisticsManager stats = player.getServer().getPlayerList().getPlayerStatsFile(player);
        if (stats.readStat(StatList.FLY_ONE_CM) > 199999 && !ThaumcraftCapabilities.knowsResearchStrict(player, "m_ELYTRAFLY")) {
            ThaumcraftCapabilities.getKnowledge(player).addResearch("m_ELYTRAFLY");
            player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.elytra_fly").setStyle(
                    new Style().setColor(TextFormatting.DARK_PURPLE)), true);
        }
        
        if (stats.readStat(StatList.DIVE_ONE_CM) > 7999 && !ThaumcraftCapabilities.knowsResearchStrict(player, "m_LONGTIMEINWATER")) {
            ThaumcraftCapabilities.getKnowledge(player).addResearch("m_LONGTIMEINWATER");
            player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.long_time_in_water").setStyle(
                    new Style().setColor(TextFormatting.DARK_PURPLE)), true);
        }
    }
    
    @SubscribeEvent
    public static void onTick(PlayerTickEvent event) {
        if (event.phase == Phase.END) {
            EntityPlayer player = event.player;
            if (PlayerMovementAbilityManager.isValidSideForMovement(player))
                PlayerMovementAbilityManager.tick(player);
            
            if (!player.getEntityWorld().isRemote) {
                if (player.getEntityWorld().getTotalWorldTime() % 40 == 0)
                    checkResearch(player);
                
                Boolean fly = Boolean.valueOf(player.capabilities.isFlying);
                if (!fly.equals(CREATIVE_FLIGHT.get(player))) {
                    PacketFlightState packet = new PacketFlightState(player.getEntityId(), fly);
                    if (player instanceof EntityPlayerMP)
                        TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) player);
                    
                    TANetwork.INSTANCE.sendToAllTracking(packet, player);
                    CREATIVE_FLIGHT.put(player, fly);
                }
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
            else
                FALL_DAMAGE.put(event.getEntity(), damage);
        }
    }

    @SubscribeEvent
    public static void onFallDamage(LivingHurtEvent event) {
        // this is needed to actually reduce damage if it's not 0
        if (event.getSource() == DamageSource.FALL && FALL_DAMAGE.containsKey(event.getEntity())) {
            float damage = FALL_DAMAGE.remove(event.getEntity());
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
