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

package thecodex6824.thaumicaugmentation.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.casters.ICaster;
import thaumcraft.common.config.ModConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager;
import thecodex6824.thaumicaugmentation.client.shader.TAShaders;
import thecodex6824.thaumicaugmentation.common.util.MorphicArmorHelper;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public class HUDEventHandler {

    protected static void renderHeldImpetusLevel(ItemStack stack, IImpetusStorage storage) {
        boolean bottom = ModConfig.CONFIG_GRAPHICS.dialBottom;
        boolean caster = stack.getItem() instanceof ICaster;
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        double yMin = caster ? (bottom ? res.getScaledHeight() - 37.5 : 32.5) : (bottom ? res.getScaledHeight() - 10.5 : 5.5);
        double yMax = caster ? (bottom ? res.getScaledHeight() - 32.5 : 37.5) : (bottom ? res.getScaledHeight() - 5.5 : 10.5);
        float height = 30.0F * (float) (Math.ceil((double) storage.getEnergyStored() / storage.getMaxEnergyStored() * 10.0) / 10.0);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        if (TAShaderManager.shouldUseShaders()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.RIFT);
            TAShaderManager.enableShader(TAShaders.FLUX_RIFT_HUD, TAShaders.SHADER_CALLBACK_CONSTANT_SPHERE_ZOOMED_20);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(8.0, yMax, 0.0).tex(1.0, 0.0).endVertex();
            buffer.pos(8.0 + height, yMax, 0.0).tex(0.0, 0.0).endVertex();
            buffer.pos(8.0 + height, yMin, 0.0).tex(0.0, 1.0).endVertex();
            buffer.pos(8.0, yMin, 0.0).tex(1.0, 1.0).endVertex();
            t.draw();
            TAShaderManager.disableShader();
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.TC_HUD);
        }
        else {
            GlStateManager.color(0.4F, 0.4F, 0.5F, 0.8F);
            Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.TC_HUD);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(8.0, yMax, 0.0).tex(0.40625, 0.1171875).endVertex();
            buffer.pos(8.0 + height, yMax, 0.0).tex(0.40625, 0.0).endVertex();
            buffer.pos(8.0 + height, yMin, 0.0).tex(0.4375, 0.0).endVertex();
            buffer.pos(8.0, yMin, 0.0).tex(0.4375, 0.1171875).endVertex();
            t.draw();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(41.5, yMin - 1.75, 0.0).tex(0.28125, 0.0078125).endVertex();
        buffer.pos(4.5, yMin - 1.75, 0.0).tex(0.28125, 0.16796875).endVertex();
        buffer.pos(4.5, yMax + 1.75, 0.0).tex(0.34375, 0.16796875).endVertex();
        buffer.pos(41.5, yMax + 1.75, 0.0).tex(0.34375, 0.0078125).endVertex();
        t.draw();
    }
    
    @Nullable
    protected static IImpetusStorage findStorage(ItemStack stack) {
        IImpetusStorage s = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
        if (s == null) {
            IAugmentableItem item = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
            if (item != null) {
                for (ItemStack a : item.getAllAugments()) {
                    s = a.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                    if (s != null)
                        return s;
                }
            }
        }
        
        return s;
    }
    
    @SubscribeEvent
    public static void onRenderHUD(RenderGameOverlayEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.getType() == ElementType.POTION_ICONS && mc.inGameHasFocus && Minecraft.isGuiEnabled()) {
            ItemStack held = mc.player.getHeldItemMainhand();
            IImpetusStorage storage = findStorage(held);
            if (storage == null) {
                held = mc.player.getHeldItemOffhand();
                storage = findStorage(held);
            }
        
            if (storage != null)
                renderHeldImpetusLevel(held, storage);
        }
    }
    
    @SubscribeEvent
    public static void onRenderHUDPre(RenderGameOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiIngame ingame = mc.ingameGUI;
        if (ingame.remainingHighlightTicks > 0 && MorphicArmorHelper.hasMorphicArmor(mc.player.inventory.getCurrentItem()))
            ingame.remainingHighlightTicks = 0;
    }
    
}
