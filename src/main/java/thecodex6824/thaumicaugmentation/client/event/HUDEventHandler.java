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

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
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
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager;
import thecodex6824.thaumicaugmentation.client.shader.TAShaders;
import thecodex6824.thaumicaugmentation.common.util.MorphicArmorHelper;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public class HUDEventHandler {

    protected static void renderHeldImpetusLevel(IImpetusStorage storage) {
        float height = 60.0F * (float) (Math.ceil((double) storage.getEnergyStored() / storage.getMaxEnergyStored() * 10.0) / 10.0);
        GlStateManager.pushMatrix();
        GlStateManager.translate(30.0, 6.0, 0.0);
        GlStateManager.scale(0.5, 0.5, 0.5);
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
            buffer.pos(-43.0, 59.5, 0.0).tex(1.0, 0.0).endVertex();
            buffer.pos(-56.0 + height, 59.5, 0.0).tex(0.0, 0.0).endVertex();
            buffer.pos(-56.0 + height, 50.5, 0.0).tex(0.0, 1.0).endVertex();
            buffer.pos(-43.0, 50.5, 0.0).tex(1.0, 1.0).endVertex();
            t.draw();
            TAShaderManager.disableShader();
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.TC_HUD);
        }
        else {
            GlStateManager.color(0.4F, 0.4F, 0.5F, 0.8F);
            Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.TC_HUD);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-43.0, 59.5, 0.0).tex(0.40625, 0.1171875).endVertex();
            buffer.pos(-56.0 + height, 59.5, 0.0).tex(0.40625, 0.0).endVertex();
            buffer.pos(-56.0 + height, 50.5, 0.0).tex(0.4375, 0.0).endVertex();
            buffer.pos(-43.0, 50.5, 0.0).tex(0.4375, 0.1171875).endVertex();
            t.draw();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(11.0, 47.0, 0.0).tex(0.28125, 0.0078125).endVertex();
        buffer.pos(-49.0, 47.0, 0.0).tex(0.28125, 0.16796875).endVertex();
        buffer.pos(-49.0, 63.0, 0.0).tex(0.34375, 0.16796875).endVertex();
        buffer.pos(11.0, 63.0, 0.0).tex(0.34375, 0.0078125).endVertex();
        t.draw();
        GlStateManager.popMatrix();
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
            IImpetusStorage storage = findStorage(mc.player.getHeldItemMainhand());
            if (storage == null)
                storage = findStorage(mc.player.getHeldItemOffhand());
        
            if (storage != null)
                renderHeldImpetusLevel(storage);
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
