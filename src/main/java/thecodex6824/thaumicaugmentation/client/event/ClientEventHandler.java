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

package thecodex6824.thaumicaugmentation.client.event;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiFunction;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.api.items.RechargeHelper;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.ClientWardStorageValue;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorageClient;
import thecodex6824.thaumicaugmentation.client.sound.SoundHandleSpecialSound;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;
import thecodex6824.thaumicaugmentation.common.event.AugmentEventHandler;
import thecodex6824.thaumicaugmentation.common.network.PacketElytraBoost;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.util.ISoundHandle;
import thecodex6824.thaumicaugmentation.common.util.MorphicArmorHelper;

@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public final class ClientEventHandler {

    private static final Set<EntityPlayer> CREATIVE_FLIGHT = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<EntityPlayer> ELYTRA_BOOSTS = Collections.newSetFromMap(new WeakHashMap<>());
    
    private static class RecoilEntry {
        
        public final long start;
        public final long duration;
        public final BiFunction<EntityLivingBase, Long, Float> recoilFunc;
        
        public RecoilEntry(long startTime, long totalTime, BiFunction<EntityLivingBase, Long, Float> recoilPattern) {
            start = startTime;
            duration = totalTime;
            recoilFunc = recoilPattern;
        }
        
    }
    
    private static final WeakHashMap<EntityLivingBase, RecoilEntry> RECOIL = new WeakHashMap<>();
    
    private ClientEventHandler() {}
    
    private static void handleAugmentTooltips(ItemTooltipEvent event, IAugmentableItem cap) {
        LinkedList<LinkedList<String>> tooltip = new LinkedList<>();
        for (ItemStack augment : cap.getAllAugments()) {
            IAugment aug = augment.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null) {
                LinkedList<String> thisTooltip = new LinkedList<>();
                thisTooltip.add(augment.getDisplayName());
                if (aug.hasAdditionalAugmentTooltip())
                    aug.appendAdditionalAugmentTooltip(thisTooltip);
                
                tooltip.add(thisTooltip);
            }
        }
        
        int num = 1;
        for (LinkedList<String> list : tooltip) {
            event.getToolTip().add(" " + num + ". " + list.remove(0));
            for (String str : list)
                event.getToolTip().add("   " + str);
            
            ++num;
        }
    }
    
    @SubscribeEvent
    @SuppressWarnings("null")
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack disp = MorphicArmorHelper.getMorphicArmor(event.getItemStack());
        if (!disp.isEmpty()) {
            List<String> newTooltip = disp.getTooltip(event.getEntityPlayer(), event.getFlags());
            if (!newTooltip.isEmpty()) {
                event.getToolTip().remove(0);
                event.getToolTip().add(0, newTooltip.get(0));
            }
        }
        
        if (!TAConfig.disableStabilizerText.getValue() && event.getEntityPlayer() != null && event.getItemStack().getItem() instanceof ItemBlock) {
            ItemBlock item = (ItemBlock) event.getItemStack().getItem();
            if (item.getBlock() instanceof IInfusionStabiliser) {
                IPlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(event.getEntityPlayer());
                if (knowledge != null && knowledge.isResearchComplete("INFUSION")) {
                    event.getToolTip().add(TextFormatting.DARK_PURPLE +
                            new TextComponentTranslation("thaumicaugmentation.text.infusion_stabilizer").getFormattedText());
                }
            }
        }
        
        IAugmentableItem cap = event.getItemStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (cap != null && cap.isAugmented()) {
            event.getToolTip().add(TextFormatting.RED + new TextComponentTranslation("thaumicaugmentation.text.augmented", 
                    TextFormatting.RESET, cap.getUsedAugmentSlots(), cap.getTotalAugmentSlots()).getFormattedText());
            handleAugmentTooltips(event, cap);
        }
    }
    
    private static boolean focusContainsWardFocus(ItemStack focus) {
        FocusPackage f = ItemFocus.getPackage(focus);
        for (IFocusElement element : f.nodes) {
            if (element.getKey().equals("focus." + ThaumicAugmentationAPI.MODID + ".ward"))
                return true;
        }
        
        return false;
    }
    
    private static void handleWardOverlay(RayTraceResult result) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        World world = mc.world;
        if (result != null && result.typeOfHit == Type.BLOCK) {
            BlockPos p = result.getBlockPos();
            for (int offsetX = -2; offsetX <= 2; ++offsetX) { 
                for (int offsetY = -2; offsetY <= 2; ++offsetY) {
                    for (int offsetZ = -2; offsetZ <= 2; ++offsetZ) {
                        BlockPos pos = p.add(offsetX, offsetY, offsetZ);
                        ClientWardStorageValue value = ((IWardStorageClient) world.getChunk(pos).getCapability(CapabilityWardStorage.WARD_STORAGE, null)).getWard(pos);
                        if (value != ClientWardStorageValue.EMPTY) {
                            float red = 1.0F;
                            float green = 0.0F;
                            if (value == ClientWardStorageValue.OWNED_SELF) {
                                red = 0.0F;
                                green = 1.0F;
                            }
                            
                            AxisAlignedBB box = world.getBlockState(pos).getBoundingBox(world, pos);
                            for (EnumFacing dir : EnumFacing.values()) {
                                float x = pos.getX() + 0.5F + dir.getXOffset() * 0.5F;
                                float y = pos.getY() + 0.5F + dir.getYOffset() * 0.5F;
                                float z = pos.getZ() + 0.5F + dir.getZOffset() * 0.5F;
                                if (dir.getXOffset() == 0)
                                    x += world.rand.nextGaussian() * 0.5;
                                if (dir.getYOffset() == 0)
                                    y += world.rand.nextGaussian() * 0.5;
                                if (dir.getZOffset() == 0)
                                    z += world.rand.nextGaussian() * 0.5;
                                
                                x = MathHelper.clamp(x, pos.getX() + (float) box.minX, pos.getX() + (float) box.maxX);
                                y = MathHelper.clamp(y, pos.getY() + (float) box.minY, pos.getY() + (float) box.maxY);
                                z = MathHelper.clamp(z, pos.getZ() + (float) box.minZ, pos.getZ() + (float) box.maxZ);
                                if ((mc.gameSettings.particleSetting == 0 && world.getTotalWorldTime() % 2 == 0) || world.getTotalWorldTime() % 4 == 0) {
                                    FXDispatcher.INSTANCE.drawSimpleSparkle(world.rand, x, y, z, 0, 0, 0, 0.5F + (float) world.rand.nextGaussian() / 8, 
                                            red, green, 0.0F, 0, 1.0F, 0.0001F, 8);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase == Phase.END && mc.world != null) {
            EntityPlayer player = mc.player;
            if (!TAConfig.disableWardFocus.getValue()) {
                if (player != null && mc.world != null && mc.world.getTotalWorldTime() % 2 == 0 && mc.getRenderViewEntity() == player) {
                    if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ICaster) {
                        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
                        if (((ICaster) stack.getItem()).getFocus(stack) instanceof ItemFocus && 
                                focusContainsWardFocus(((ICaster) stack.getItem()).getFocusStack(stack))) {
                            
                            handleWardOverlay(player.rayTrace(Math.min(player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() * 2, 32),
                                    mc.getRenderPartialTicks()));
                            return;
                        }
                    }   
                    
                    if (player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ICaster) {
                        ItemStack stack = player.getHeldItem(EnumHand.OFF_HAND);
                        if (((ICaster) stack.getItem()).getFocus(stack) instanceof ItemFocus && 
                                focusContainsWardFocus(((ICaster) stack.getItem()).getFocusStack(stack))) {
                            
                            handleWardOverlay(player.rayTrace(Math.min(player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() * 2, 32),
                                    mc.getRenderPartialTicks()));
                        }
                    }
                }
            }
            
            Boolean boost = ELYTRA_BOOSTS.contains(player);
            if (!boost && ThaumicAugmentation.proxy.isEntityRenderView(player) && ThaumicAugmentation.proxy.isElytraBoostKeyDown() &&
                    player.isElytraFlying() && player.getTicksElytraFlying() >= 2) {
                
                IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
                boolean done = false;
                if (baubles != null) {
                    for (int slot : BaubleType.BODY.getValidSlots()) {
                        ItemStack body = baubles.getStackInSlot(slot);
                        if (body.getItem() == TAItems.ELYTRA_HARNESS) {
                            IAugmentableItem augmentable = body.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                            if (augmentable != null) {
                                for (ItemStack aug : augmentable.getAllAugments()) {
                                    if (aug.getItem() == TAItems.ELYTRA_HARNESS_AUGMENT && aug.getMetadata() == 0) {
                                        IImpetusStorage impetus = aug.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                                        if (impetus != null && (player.isCreative() || impetus.extractEnergy(1, true) == 1)) {
                                            onBoostChange(player, true);
                                            PacketElytraBoost boostPacket = new PacketElytraBoost(true);
                                            TANetwork.INSTANCE.sendToServer(boostPacket);
                                            done = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (done)
                            break;
                    }
                }
            }
            else if (boost && ThaumicAugmentation.proxy.isEntityRenderView(player)) {
                boolean stopping = !ThaumicAugmentation.proxy.isElytraBoostKeyDown() || !player.isElytraFlying() || player.getTicksElytraFlying() < 2;
                if (!stopping) {
                    IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
                    stopping = true;
                    if (baubles != null) {
                        for (int slot : BaubleType.BODY.getValidSlots()) {
                            ItemStack body = baubles.getStackInSlot(slot);
                            if (body.getItem() == TAItems.ELYTRA_HARNESS) {
                                IAugmentableItem augmentable = body.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                                if (augmentable != null) {
                                    for (ItemStack aug : augmentable.getAllAugments()) {
                                        if (aug.getItem() == TAItems.ELYTRA_HARNESS_AUGMENT && aug.getMetadata() == 0) {
                                            IImpetusStorage impetus = aug.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                                            if (impetus != null && (player.isCreative() || impetus.extractEnergy(1, true) == 1)) {
                                                stopping = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if (!stopping)
                                break;
                        }
                    }
                }
                
                if (stopping) {
                    onBoostChange(player, false);
                    PacketElytraBoost boostPacket = new PacketElytraBoost(false);
                    TANetwork.INSTANCE.sendToServer(boostPacket);
                }
            }
            
            if (!ThaumicAugmentation.proxy.isSingleplayer() || !mc.isGamePaused()) {
                long now = mc.world.getTotalWorldTime();
                Iterator<Entry<EntityLivingBase, RecoilEntry>> i = RECOIL.entrySet().iterator();
                while (i.hasNext()) {
                    Entry<EntityLivingBase, RecoilEntry> entry = i.next();
                    RecoilEntry recoil = entry.getValue();
                    if (now >= recoil.start + recoil.duration)
                        i.remove();
                    else {
                        EntityLivingBase entity = entry.getKey();
                        entity.prevRotationPitch = entity.rotationPitch;
                        entity.rotationPitch += recoil.recoilFunc.apply(entity, now - recoil.start);
                    }
                }
            }
        }
    }
    
    public static void onFlightChange(EntityPlayer player, boolean flying) {
        if (flying != CREATIVE_FLIGHT.contains(player)) {
            IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
            if (baubles != null) {
                for (int slot : BaubleType.BODY.getValidSlots()) {
                    ItemStack body = baubles.getStackInSlot(slot);
                    if (body.getItem() == TAItems.THAUMOSTATIC_HARNESS && RechargeHelper.getCharge(body) > 0) {
                        final int id = player.getEntityId();
                        if (flying) {
                            ISoundHandle handle = ThaumicAugmentation.proxy.playSpecialSound(SoundsTC.hhon, SoundCategory.PLAYERS,
                                    old -> {
                                        Entity entity = Minecraft.getMinecraft().world.getEntityByID(id);
                                        if (entity instanceof EntityPlayer && !entity.isDead)
                                            return entity.getPositionVector();
                                        else
                                            return null;
                                    }, (float) player.posX, (float) player.posY, (float) player.posZ, 1.0F, 1.0F, false, 0);
                            if (ThaumicAugmentation.proxy.isEntityRenderView(player) && handle instanceof SoundHandleSpecialSound)
                                ((SoundHandleSpecialSound) handle).setAttenuationType(AttenuationType.NONE);
                            
                            handle = ThaumicAugmentation.proxy.playSpecialSound(SoundsTC.jacobs, SoundCategory.PLAYERS,
                                    old -> {
                                        Entity entity = Minecraft.getMinecraft().world.getEntityByID(id);
                                        if (entity instanceof EntityPlayer && !entity.isDead && CREATIVE_FLIGHT.contains((EntityPlayer) entity))
                                            return entity.getPositionVector();
                                        else
                                            return null;
                                    }, (float) player.posX, (float) player.posY, (float) player.posZ, 0.05F, 1.0F, true, 0);
                            if (ThaumicAugmentation.proxy.isEntityRenderView(player) && handle instanceof SoundHandleSpecialSound)
                                ((SoundHandleSpecialSound) handle).setAttenuationType(AttenuationType.NONE);
                        }
                        else {
                            ISoundHandle handle = ThaumicAugmentation.proxy.playSpecialSound(SoundsTC.hhoff, SoundCategory.PLAYERS,
                                    old -> {
                                        Entity entity = Minecraft.getMinecraft().world.getEntityByID(id);
                                        if (entity instanceof EntityPlayer && !entity.isDead)
                                            return entity.getPositionVector();
                                        else
                                            return null;
                                    }, (float) player.posX, (float) player.posY, (float) player.posZ, 1.0F, 1.0F, false, 0);
                            if (ThaumicAugmentation.proxy.isEntityRenderView(player) && handle instanceof SoundHandleSpecialSound)
                                ((SoundHandleSpecialSound) handle).setAttenuationType(AttenuationType.NONE);
                        }
                            
                        break;
                    }
                }
            }
            
            if (flying)
                CREATIVE_FLIGHT.add(player);
            else
                CREATIVE_FLIGHT.remove(player);
        }
    }
    
    public static void onBoostChange(EntityPlayer player, boolean boost) {
        final int id = player.getEntityId();
        if (boost && !ELYTRA_BOOSTS.contains(player)) {
            ELYTRA_BOOSTS.add(player);
            ISoundHandle handle = ThaumicAugmentation.proxy.playSpecialSound(TASounds.ELYTRA_BOOST_START, SoundCategory.PLAYERS,
                    old -> {
                        Entity entity = Minecraft.getMinecraft().world.getEntityByID(id);
                        if (entity instanceof EntityPlayer && !entity.isDead)
                            return entity.getPositionVector();
                        else
                            return null;
                    }, (float) player.posX, (float) player.posY, (float) player.posZ, 1.0F, 1.0F, false, 0);
            if (ThaumicAugmentation.proxy.isEntityRenderView(player) && handle instanceof SoundHandleSpecialSound)
                ((SoundHandleSpecialSound) handle).setAttenuationType(AttenuationType.NONE);
            
            handle = ThaumicAugmentation.proxy.playSpecialSound(TASounds.ELYTRA_BOOST_LOOP, SoundCategory.PLAYERS,
                    old -> {
                        Entity entity = Minecraft.getMinecraft().world.getEntityByID(id);
                        if (entity instanceof EntityPlayer && !entity.isDead && ELYTRA_BOOSTS.contains((EntityPlayer) entity))
                            return entity.getPositionVector();
                        else
                            return null;
                    }, (float) player.posX, (float) player.posY, (float) player.posZ, 0.6F, 1.0F, true, 0);
            if (ThaumicAugmentation.proxy.isEntityRenderView(player) && handle instanceof SoundHandleSpecialSound)
                ((SoundHandleSpecialSound) handle).setAttenuationType(AttenuationType.NONE);
        }
        else if (!boost && ELYTRA_BOOSTS.contains(player)) {
            ELYTRA_BOOSTS.remove(player);
            ISoundHandle handle = ThaumicAugmentation.proxy.playSpecialSound(TASounds.ELYTRA_BOOST_END, SoundCategory.PLAYERS,
                    old -> {
                        Entity entity = Minecraft.getMinecraft().world.getEntityByID(id);
                        if (entity instanceof EntityPlayer && !entity.isDead)
                            return entity.getPositionVector();
                        else
                            return null;
                    }, (float) player.posX, (float) player.posY, (float) player.posZ, 1.0F, 1.0F, false, 0);
            if (ThaumicAugmentation.proxy.isEntityRenderView(player) && handle instanceof SoundHandleSpecialSound)
                ((SoundHandleSpecialSound) handle).setAttenuationType(AttenuationType.NONE);
        }
    }
    
    @SubscribeEvent
    public static void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals(ThaumicAugmentationAPI.MODID)) {
            TAConfigHolder.syncLocally();
            if (ThaumicAugmentation.proxy.isSingleplayer())
                TAConfigHolder.loadConfigValues(Side.SERVER);
            else
                TAConfigHolder.loadConfigValues(Side.CLIENT);
            
            for (Runnable r : TAConfigHolder.getListeners())
                r.run();
        }
    }
    
    public static void onClientEquipmentChange(ClientLivingEquipmentChangeEvent event) {
        // will have already updated augment state in SP due to server check
        if (!ThaumicAugmentation.proxy.isSingleplayer())
            AugmentEventHandler.onEquipmentChange(event.getEntityLiving());
    }
    
    public static boolean isBoosting(EntityPlayer player) {
        return ELYTRA_BOOSTS.contains(player);  
    }
    
    public static void onRecoil(EntityLivingBase living, BiFunction<EntityLivingBase, Long, Float> func, long duration) {
        RECOIL.put(living, new RecoilEntry(Minecraft.getMinecraft().world.getTotalWorldTime() + 1, duration, func));
    }
    
}
