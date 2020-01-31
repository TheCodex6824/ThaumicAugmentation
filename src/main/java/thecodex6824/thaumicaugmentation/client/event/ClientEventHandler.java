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

import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.items.casters.ItemFocus;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.warded.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.storage.ClientWardStorageValue;
import thecodex6824.thaumicaugmentation.api.warded.storage.IWardStorageClient;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;
import thecodex6824.thaumicaugmentation.common.event.AugmentEventHandler;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public final class ClientEventHandler {

    private ClientEventHandler() {}
    
    private static void handleAugmentTooltips(ItemTooltipEvent event, IAugmentableItem cap) {
        LinkedList<LinkedList<String>> tooltip = new LinkedList<>();
        for (ItemStack augment : cap.getAllAugments()) {
            if (augment.hasCapability(CapabilityAugment.AUGMENT, null)) {
                LinkedList<String> thisTooltip = new LinkedList<>();
                thisTooltip.add(augment.getDisplayName());
                IAugment aug = augment.getCapability(CapabilityAugment.AUGMENT, null);
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
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
            IAugmentableItem cap = event.getItemStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
            if (cap.isAugmented()) {
                event.getToolTip().add(TextFormatting.RED + new TextComponentTranslation("thaumicaugmentation.text.augmented", 
                        TextFormatting.RESET, cap.getUsedAugmentSlots(), cap.getTotalAugmentSlots()).getFormattedText());
                handleAugmentTooltips(event, cap);
            }
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
        if (!TAConfig.disableWardFocus.getValue() && event.phase == Phase.END) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            if (mc.player != null && mc.world != null && mc.world.getTotalWorldTime() % 2 == 0 && mc.getRenderViewEntity() == mc.player) {
                if (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ICaster) {
                    ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
                    if (((ICaster) stack.getItem()).getFocus(stack) instanceof ItemFocus && 
                            focusContainsWardFocus(((ICaster) stack.getItem()).getFocusStack(stack))) {
                        
                        handleWardOverlay(mc.player.rayTrace(Math.min(mc.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() * 2, 32),
                                mc.getRenderPartialTicks()));
                        return;
                    }
                }   
                
                if (mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ICaster) {
                    ItemStack stack = mc.player.getHeldItem(EnumHand.OFF_HAND);
                    if (((ICaster) stack.getItem()).getFocus(stack) instanceof ItemFocus && 
                            focusContainsWardFocus(((ICaster) stack.getItem()).getFocusStack(stack))) {
                        
                        handleWardOverlay(mc.player.rayTrace(Math.min(mc.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() * 2, 32),
                                mc.getRenderPartialTicks()));
                    }
                }
            }
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
    
}
