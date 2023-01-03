/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.IElytraHarnessAugment;
import thecodex6824.thaumicaugmentation.common.capability.provider.SimpleCapabilityProviderNoSave;
import vazkii.botania.api.item.IBaubleRender.RenderType;
import vazkii.botania.api.item.IPetalApothecary;
import vazkii.botania.api.item.IPhantomInkable;
import vazkii.botania.common.item.equipment.bauble.ItemFlightTiara;

import java.util.List;
import java.util.Random;

public class IntegrationBotania implements IIntegrationHolder {

    @Override
    public void preInit() {}
    
    @Override
    public void init() {}
    
    @Override
    public void postInit() {}
    
    @Override
    public boolean registerEventBus() {
        return true;
    }
    
    public boolean isPhantomInked(ItemStack stack) {
        return stack.getItem() instanceof IPhantomInkable && ((IPhantomInkable) stack.getItem()).hasPhantomInk(stack);
    }
    
    public boolean isPetalApothecary(TileEntity tile) {
        return tile instanceof IPetalApothecary;
    }
    
    public void fillPetalApothecary(TileEntity tile) {
        if (tile instanceof IPetalApothecary) {
            IPetalApothecary p = (IPetalApothecary) tile;
            if (!p.hasWater())
                p.setWater(true);
        }
    }
    
    @SubscribeEvent
    public void onAttachCapabilityItemStack(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof ItemFlightTiara) {
            event.addCapability(new ResourceLocation(ThaumicAugmentationAPI.MODID, "elytra_harness_augment"), new SimpleCapabilityProviderNoSave<>(new IElytraHarnessAugment() {
                @Override
                public boolean isCosmetic() {
                    return true;
                }   
                
                @Override
                public boolean hasAdditionalAugmentTooltip() {
                    return true;
                }
                
                @Override
                public void appendAdditionalAugmentTooltip(List<String> tooltip) {
                    tooltip.add(new TextComponentTranslation("botania.wings" + event.getObject().getMetadata()).getFormattedText());
                }
                
                @Override
                @SideOnly(Side.CLIENT)
                public int getCosmeticItemTint() {
                    switch (event.getObject().getMetadata()) {
                        case 2: return 0x1A1A1A;
                        case 3: return 0x0099FF;
                        case 4: return 0xFF4D4D;
                        case 5: return 0x4D004D;
                        case 6: return 0x660000;
                        case 7: return 0x339933;
                        case 8: return 0xD9D933;
                        case 9: return 0x00FF00;
                        default: return -1;
                    }
                }
                
                @Override
                @SideOnly(Side.CLIENT)
                public void render(ItemStack stack, RenderPlayer renderer, ModelBiped base, EntityPlayer player, float limbSwing,
                        float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
                        float scale) {
                    
                    ItemFlightTiara item = (ItemFlightTiara) stack.getItem();
                    boolean oldFly = player.capabilities.isFlying;
                    player.capabilities.isFlying = player.isElytraFlying();
                    // most things render on BODY, Jibril's halo renders on anything else (but HEAD makes the most sense)
                    item.onPlayerBaubleRender(stack, player, RenderType.BODY, partialTicks);
                    float yaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
                    float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
                    float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
                    GlStateManager.rotate(yawOffset, 0, -1, 0);
                    GlStateManager.rotate(yaw - 270, 0, 1, 0);
                    GlStateManager.rotate(pitch, 0, 0, 1);
                    // slightly raise the halo to make it not clip with some TC armor
                    GlStateManager.translate(0.0, -0.1, 0.0);
                    item.onPlayerBaubleRender(stack, player, RenderType.HEAD, partialTicks);
                    player.capabilities.isFlying = oldFly;
                }
                
                @Override
                @SideOnly(Side.CLIENT)
                public void renderFlightParticles(ItemStack cosmetic, RenderPlayer renderer, ModelBiped base, EntityPlayer player,
                        float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
                        float headPitch, float scale) {
                    
                    Random rand = player.getRNG();
                    float r = 1.0F, g = 1.0F, b = 1.0F;
                    switch (cosmetic.getMetadata()) {
                        case 2: {
                            r = 0.1F;
                            g = 0.1F;
                            b = 0.1F;
                            break;
                        }
                        case 3: {
                            r = 0.0F;
                            g = 0.6F;
                            break;
                        }
                        case 4: {
                            g = 0.3F;
                            b = 0.3F;
                            break;
                        }
                        case 5: {
                            r = 0.6F;
                            g = 0.0F;
                            b = 0.6F;
                            break;
                        }
                        case 6: {
                            r = 0.4F;
                            g = 0.0F;
                            b = 0.0F;
                            break;
                        }
                        case 7: {
                            r = 0.2F;
                            g = 0.6F;
                            b = 0.2F;
                            break;
                        }
                        case 8: {
                            r = 0.85F;
                            g = 0.85F;
                            b = 0.2F;
                            break;
                        }
                        case 9: {
                            r = 0.0F;
                            b = 0.0F;
                            break;
                        }
                        default: break;
                    }

                    FXGeneric fx = new FXGeneric(Minecraft.getMinecraft().world, player.posX + (rand.nextFloat() - rand.nextFloat()),
                            player.posY + rand.nextFloat(), player.posZ - (rand.nextFloat() - rand.nextFloat()), 0, 0, 0);
                    fx.setRBGColorF(r, g, b);
                    fx.setAlphaF(0.9F, 0.0F);
                    fx.setGridSize(64);
                    fx.setParticles(264, 8, 1);
                    fx.setScale(1.0F);
                    fx.setLayer(1);
                    fx.setLoop(true);
                    fx.setRotationSpeed(rand.nextFloat(), rand.nextBoolean() ? 1.0F : -1.0F);
                    ParticleEngine.addEffect(Minecraft.getMinecraft().world, fx);
                }
                
            }, CapabilityAugment.AUGMENT));
        }
    }
    
}
