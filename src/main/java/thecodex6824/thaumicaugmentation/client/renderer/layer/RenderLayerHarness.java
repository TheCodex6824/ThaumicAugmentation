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

package thecodex6824.thaumicaugmentation.client.renderer.layer;

import java.util.Random;
import java.util.function.Predicate;

import org.lwjgl.opengl.GL11;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.client.lib.obj.AdvancedModelLoader;
import thaumcraft.client.lib.obj.IModelCustom;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.builder.IElytraHarnessAugment;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationBotania;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationHandler;

public class RenderLayerHarness implements LayerRenderer<EntityPlayer> {

    protected static final ModelElytra ELYTRA_MODEL_DEFAULT = new ModelElytra();
    
    private static boolean invoke(Predicate<ItemStack> p, ItemStack stack) {
        return p.test(stack);
    }
    
    private static final Predicate<ItemStack> PHANTOM_INK = 
        (stack) -> {
            return invoke((s) -> {
                return ((IntegrationBotania) IntegrationHandler.getIntegration(IntegrationHandler.BOTANIA_MOD_ID)).isPhantomInked(s);
            }, 
            stack);
    };
    
    protected RenderPlayer render;
    protected ModelBiped base;
    protected IModelCustom thaumostatic;
    
    public RenderLayerHarness(RenderPlayer renderer) {
        render = renderer;
        base = new ModelBiped(1.0F);
        thaumostatic = AdvancedModelLoader.loadModel(TATextures.THAUMOSTATIC_MODEL);
    }
    
    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        
        ItemStack harness = ItemStack.EMPTY;
        IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
        if (baubles != null) {
            ItemStack stack = baubles.getStackInSlot(BaubleType.BODY.getValidSlots()[0]);
            if (stack.getItem() == TAItems.THAUMOSTATIC_HARNESS || stack.getItem() == TAItems.ELYTRA_HARNESS)
                harness = stack;
        }
        
        boolean invis = IntegrationHandler.isIntegrationPresent(IntegrationHandler.BOTANIA_MOD_ID) && PHANTOM_INK.test(harness);
        if (!harness.isEmpty()) {
            if (!invis) {
                base.setModelAttributes(render.getMainModel());
                base.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTicks);
                render.bindTexture(TATextures.HARNESS_BASE_TEXTURE);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                base.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale * 0.75F);
            }
            if (harness.getItem() == TAItems.THAUMOSTATIC_HARNESS) {
                if (!invis) {
                    render.bindTexture(TATextures.THAUMOSTATIC_TEXTURE);
                    GlStateManager.pushMatrix();
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.scale(scale * 1.8F, scale * 1.8F, scale * 1.8F);
                    GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
                    GlStateManager.translate(0.0F, -5.0F, 1.7F);
                    if (render.getMainModel().isSneak)
                        GlStateManager.rotate((float) Math.toDegrees(base.bipedBody.rotateAngleX), 1.0F, 0.0F, 0.0F);
                    
                    GlStateManager.translate(0.0F, 5.33F, -5.2F);
                    thaumostatic.renderAll();
                    GlStateManager.disableRescaleNormal();
                    GlStateManager.popMatrix();
                    
                    if (player.capabilities.isFlying) {
                        render.bindTexture(TATextures.LIGHTNING_TEXTURE);
                        GlStateManager.scale(1.5F, 1.5F, 1.0F);
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(-0.5F, -0.35F, 0.5F);
                        double offset = player.ticksExisted % 16 / 16.0;
                        Tessellator t = Tessellator.getInstance();
                        BufferBuilder buffer = t.getBuffer();
                        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                        buffer.pos(0.0, 1.0, 0.0).tex(offset, 1.0).endVertex();
                        buffer.pos(1.0, 1.0, 0.0).tex(offset + 0.0625, 1.0).endVertex();
                        buffer.pos(1.0, 0.0, 0.0).tex(offset + 0.0625, 0.0).endVertex();
                        buffer.pos(0.0, 0.0, 0.0).tex(offset, 0.0).endVertex();
                        t.draw();
                        GlStateManager.popMatrix();
                        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.translate(-0.5F, -0.35F, -0.5F);
                        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                        buffer.pos(0.0, 1.0, 0.0).tex(offset, 1.0).endVertex();
                        buffer.pos(1.0, 1.0, 0.0).tex(offset + 0.0625, 1.0).endVertex();
                        buffer.pos(1.0, 0.0, 0.0).tex(offset + 0.0625, 0.0).endVertex();
                        buffer.pos(0.0, 0.0, 0.0).tex(offset, 0.0).endVertex();
                        t.draw();
                    }
                    
                    GlStateManager.popMatrix();
                }
                
                if (player.capabilities.isFlying) {
                    Vec3d backOrig = new Vec3d(0.0, 1.25, -0.4);
                    Vec3d back = backOrig.rotateYaw((float) Math.toRadians(-player.renderYawOffset)).add(player.posX, player.posY, player.posZ);
                    for (int i = 0; i < 3; ++i) {
                        Vec3d target = new Vec3d((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 2,
                                backOrig.y + (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 2, backOrig.z - 0.2).rotateYaw((float) Math.toRadians(-player.renderYawOffset)).add(player.posX, player.posY, player.posZ);
                        FXDispatcher.INSTANCE.arcBolt(back.x, back.y, back.z, target.x, target.y, target.z, 0.5F, 0.5F, 1.0F, 0.075F);
                    }
                }
                
                GlStateManager.disableBlend();
            }
            else if (harness.getItem() == TAItems.ELYTRA_HARNESS) {
                ModelBase model = ELYTRA_MODEL_DEFAULT;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                
                boolean cosmeticRendered = false;
                IAugmentableItem augmentable = harness.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                if (augmentable != null) {
                    for (ItemStack aug : augmentable.getAllAugments()) {
                        IAugment cap = aug.getCapability(CapabilityAugment.AUGMENT, null);
                        if (cap instanceof IElytraHarnessAugment) {
                            GlStateManager.pushMatrix();
                            GL11.glColor3ub((byte) 255, (byte) 255, (byte) 255);
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                            IElytraHarnessAugment e = (IElytraHarnessAugment) cap;
                            if (!invis || e.isCosmetic())
                                e.render(aug, render, base, player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                            if (e.isCosmetic()) {
                                cosmeticRendered = true;
                                if (player.isElytraFlying() && player.getRNG().nextBoolean()) {
                                    e.renderFlightParticles(aug, render, base, player, limbSwing, limbSwingAmount,
                                            partialTicks,ageInTicks, netHeadYaw, headPitch, scale);
                                }
                            }
                            GlStateManager.popMatrix();
                        }
                    }
                }
                
                if (!cosmeticRendered) {
                    if (player instanceof AbstractClientPlayer) {
                        AbstractClientPlayer abs = (AbstractClientPlayer) player;
                        if (abs.isPlayerInfoSet() && abs.getLocationElytra() != null)
                            render.bindTexture(abs.getLocationElytra());
                        else if (abs.hasPlayerInfo() && abs.getLocationCape() != null && abs.isWearing(EnumPlayerModelParts.CAPE))
                            render.bindTexture(abs.getLocationCape());
                        else
                            render.bindTexture(TATextures.ELYTRA_TEXTURE);
                    }
                    else
                        render.bindTexture(TATextures.ELYTRA_TEXTURE);
                    
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0.0F, 0.0F, 0.125F);
                    model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
                    model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    
                    Random rand = player.getRNG();
                    if (player.isElytraFlying() && rand.nextBoolean()) {
                        int particleColor = Aspect.FLIGHT.getColor();
                        FXGeneric fx = new FXGeneric(Minecraft.getMinecraft().world, player.posX + (rand.nextFloat() - rand.nextFloat()),
                                player.posY + rand.nextFloat(), player.posZ - (rand.nextFloat() - rand.nextFloat()), 0, 0, 0);
                        fx.setRBGColorF(((particleColor >> 16) & 0xFF) / 255.0F, ((particleColor >> 8) & 0xFF) / 255.0F, (particleColor & 0xFF) / 255.0F);
                        fx.setAlphaF(0.9F, 0.0F);
                        fx.setGridSize(64);
                        fx.setParticles(264, 8, 1);
                        fx.setScale(1.0F);
                        fx.setLayer(1);
                        fx.setLoop(true);
                        fx.setRotationSpeed(rand.nextFloat(), rand.nextBoolean() ? 1.0F : -1.0F);
                        ParticleEngine.addEffect(Minecraft.getMinecraft().world, fx);
                    }
                }
            }
        }
    }
    
    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
    
}
