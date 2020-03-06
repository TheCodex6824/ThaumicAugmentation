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
            TAShaderManager.enableShader(TAShaders.FLUX_RIFT_HUD, TAShaders.SHADER_CALLBACK_CONSTANT_SPHERE_ZOOMED);
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
        buffer.pos(-49.0 + height, 47.0, 0.0).tex(0.28125, 0.0078125).endVertex();
        buffer.pos(-49.0, 47.0, 0.0).tex(0.28125, 0.16796875).endVertex();
        buffer.pos(-49.0, 63.0, 0.0).tex(0.34375, 0.16796875).endVertex();
        buffer.pos(-49.0 + height, 63.0, 0.0).tex(0.34375, 0.0078125).endVertex();
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
    
    /*protected static void renderItemStack(ItemStack stack, Entity holder, double x, double y) {
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, (int) x, (int) y);
    }
    
    protected static void renderDurabilityBar(ItemStack stack, double x, double y) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        double durability = stack.getItem().getDurabilityForDisplay(stack);
        int rgb = stack.getItem().getRGBDurabilityForDisplay(stack);
        int width = Math.round(13.0F - (float) (durability * 13.0F));
        
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x + 2, y + 13, 0.0).color(0.0F, 0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(x + 2, y + 15, 0.0).color(0.0F, 0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(x + 15, y + 15, 0.0).color(0.0F, 0.0F, 0.0F, 1.0F).endVertex();
        buffer.pos(x + 15, y + 13, 0.0).color(0.0F, 0.0F, 0.0F, 1.0F).endVertex();
        t.draw();
        
        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x + 2, y + 13, 0.0).color(red, green, blue, 1.0F).endVertex();
        buffer.pos(x + 2, y + 14, 0.0).color(red, green, blue, 1.0F).endVertex();
        buffer.pos(x + 2 + width, y + 14, 0.0).color(red, green, blue, 1.0F).endVertex();
        buffer.pos(x + 2 + width, y + 13, 0.0).color(red, green, blue, 1.0F).endVertex();
        t.draw();
        
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
    }
    
    protected static void renderVisBar(ItemStack stack, double x, double y, double percent) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.TC_HUD);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        double width = percent * 13.0;
        GlStateManager.color(0.75294118F, 1.0F, 1.0F, 0.8F);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x + 2 + width, y + 15, 0.0).tex(0.40625, 0.1171875).endVertex();
        buffer.pos(x + 2, y + 15, 0.0).tex(0.40625, 0.0).endVertex();
        buffer.pos(x + 2, y + 17, 0.0).tex(0.4375, 0.0).endVertex();
        buffer.pos(x + 2 + width, y + 17, 0.0).tex(0.4375, 0.1171875).endVertex();
        t.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x + 15, y + 14, 0.0).tex(0.28125, 0.0078125).endVertex();
        buffer.pos(x + 2, y + 14, 0.0).tex(0.28125, 0.16796875).endVertex();
        buffer.pos(x + 2, y + 18, 0.0).tex(0.34375, 0.16796875).endVertex();
        buffer.pos(x + 15, y + 18, 0.0).tex(0.34375, 0.0078125).endVertex();
        t.draw();
    }
    
    protected static void renderImpetusBar(ItemStack stack, double x, double y, double percent) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.TC_HUD);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        double width = percent * 13.0;
        GlStateManager.color(0.4F, 0.4F, 0.5F, 0.8F);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x + 2 + width, y + 15, 0.0).tex(0.40625, 0.1171875).endVertex();
        buffer.pos(x + 2, y + 15, 0.0).tex(0.40625, 0.0).endVertex();
        buffer.pos(x + 2, y + 17, 0.0).tex(0.4375, 0.0).endVertex();
        buffer.pos(x + 2 + width, y + 17, 0.0).tex(0.4375, 0.1171875).endVertex();
        t.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x + 15, y + 14, 0.0).tex(0.28125, 0.0078125).endVertex();
        buffer.pos(x + 2, y + 14, 0.0).tex(0.28125, 0.16796875).endVertex();
        buffer.pos(x + 2, y + 18, 0.0).tex(0.34375, 0.16796875).endVertex();
        buffer.pos(x + 15, y + 18, 0.0).tex(0.34375, 0.0078125).endVertex();
        t.draw();
    }
    
    protected static void renderBaubleStatus() {
        Entity check = Minecraft.getMinecraft().getRenderViewEntity();
        if (check == null)
            check = Minecraft.getMinecraft().player;
        
        IBaublesItemHandler baubles = check.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
        if (baubles != null) {
            int baseY = 40;
            for (int i = 0; i < baubles.getSlots(); ++i) {
                boolean renderedStack = false;
                ItemStack stack = baubles.getStackInSlot(i);
                int renderY = baseY;
                if (stack.isItemStackDamageable() && stack.isItemDamaged()) {
                    if (!renderedStack) {
                        renderItemStack(stack, check, 4.0, renderY + 1);
                        renderedStack = true;
                    }
                    
                    renderDurabilityBar(stack, 3.5, renderY);
                }
                
                renderY += 2;
                if (stack.getItem() instanceof IRechargable || stack.getItem() instanceof ISpecialVisDisplay) {
                    if (!(stack.getItem() instanceof ISpecialVisDisplay)) {
                        IRechargable rechargable = (IRechargable) stack.getItem();
                        EntityLivingBase entity = check instanceof EntityLivingBase ? (EntityLivingBase) check : null;
                        if (rechargable.showInHud(stack, entity) == EnumChargeDisplay.PERIODIC &&
                                RechargeHelper.getCharge(stack) < rechargable.getMaxCharge(stack, entity)) {
                            
                            if (!renderedStack) {
                                renderItemStack(stack, check, 4.0, baseY + 1);
                                renderedStack = true;
                            }
                            
                            double percent = RechargeHelper.getCharge(stack) / (double) rechargable.getMaxCharge(stack, entity);
                            renderVisBar(stack, 3.5, renderY, percent);
                            renderY += 4;
                        }
                    }
                    else {
                        ISpecialVisDisplay display = (ISpecialVisDisplay) stack.getItem();
                        EntityLivingBase entity = check instanceof EntityLivingBase ? (EntityLivingBase) check : null;
                        if (display.shouldDisplayVisBar(stack, entity)) {
                            if (!renderedStack) {
                                renderItemStack(stack, check, 4.0, baseY + 1);
                                renderedStack = true;
                            }
                            
                            renderVisBar(stack, 3.5, renderY, display.getVisBarFullness(stack, entity));
                            renderY += 4;
                        }
                    }
                }
                
                IImpetusStorage impetus = findStorage(stack);
                if (impetus != null && impetus.getEnergyStored() < impetus.getMaxEnergyStored()) {
                    if (!renderedStack) {
                        renderItemStack(stack, check, 4.0, baseY + 1);
                        renderedStack = true;
                    }
                    
                    renderImpetusBar(stack, 3.5, renderY, (double) impetus.getEnergyStored() / impetus.getMaxEnergyStored());
                    renderY += 4;
                }
                
                if (renderedStack)
                    baseY = renderY + 16;
            }
        }
    }*/
    
    @SubscribeEvent
    public static void onRenderHUD(RenderGameOverlayEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.getType() == ElementType.TEXT && mc.inGameHasFocus && Minecraft.isGuiEnabled()) {
            IImpetusStorage storage = findStorage(mc.player.getHeldItemMainhand());
            if (storage == null)
                storage = findStorage(mc.player.getHeldItemOffhand());
        
            if (storage != null)
                renderHeldImpetusLevel(storage);
            
            //renderBaubleStatus();
        }
    }
    
}
