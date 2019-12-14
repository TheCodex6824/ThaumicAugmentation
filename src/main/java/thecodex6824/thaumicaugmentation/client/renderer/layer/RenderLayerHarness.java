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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.block.Block;
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
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.client.lib.obj.AdvancedModelLoader;
import thaumcraft.client.lib.obj.IModelCustom;
import thaumcraft.common.blocks.basic.BlockBannerTCItem;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.builder.IElytraHarnessAugment;
import thecodex6824.thaumicaugmentation.client.model.ModelElytraBanner;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TCBannerToElytraTexture;
import thecodex6824.thaumicaugmentation.client.renderer.texture.VanillaBannerToElytraTexture;

public class RenderLayerHarness implements LayerRenderer<EntityPlayer> {

    protected static final ResourceLocation BASE_TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/models/harness/harness_base.png");
    protected static final ResourceLocation THAUMOSTATIC_TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/models/harness/thaumostatic_module.png"); 
    protected static final ResourceLocation THAUMOSTATIC_MODEL = new ResourceLocation(ThaumicAugmentationAPI.MODID, "models/entity/thaumostatic_module.obj");
    protected static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/models/harness/lightning_ring.png");
    
    protected static final ResourceLocation ELYTRA_TEXTURE = new ResourceLocation("minecraft", "textures/entity/elytra.png");
    protected static final ModelElytra ELYTRA_MODEL_DEFAULT = new ModelElytra();
    protected static final ModelElytraBanner ELYTRA_MODEL_VANILLA_BANNER = new ModelElytraBanner(1.75F, 42, 48);
    protected static final ModelElytraBanner ELYTRA_MODEL_TC_BANNER = new ModelElytraBanner(2.0F, 48, 48);
    protected static final ResourceLocation VANILLA_BANNER_BASE = new ResourceLocation("minecraft", "textures/entity/banner_base.png");
    protected static final ResourceLocation TC_BANNER = new ResourceLocation("thaumcraft", "textures/models/banner_blank.png");
    protected static final ResourceLocation CRIMSON_BANNER = new ResourceLocation("thaumcraft", "textures/models/banner_cultist.png");
    protected static final Cache<ItemStack, ResourceLocation> BANNER_CACHE = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterAccess(10, TimeUnit.SECONDS).removalListener(new RemovalListener<ItemStack, ResourceLocation>() {
        
        @Override
        public void onRemoval(RemovalNotification<ItemStack, ResourceLocation> notification) {
            Minecraft.getMinecraft().getTextureManager().deleteTexture(notification.getValue());
        }
        
    }).build();
    
    protected RenderPlayer render;
    protected ModelBiped base;
    protected IModelCustom thaumostatic;
    protected HashMap<Block, EnumDyeColor> bannerReverseMap;
    
    public RenderLayerHarness(RenderPlayer renderer) {
        render = renderer;
        base = new ModelBiped(1.0F);
        thaumostatic = AdvancedModelLoader.loadModel(THAUMOSTATIC_MODEL);
        bannerReverseMap = new HashMap<>();
        for (Map.Entry<EnumDyeColor, Block> entry : BlocksTC.banners.entrySet())
            bannerReverseMap.put(entry.getValue(), entry.getKey());
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
        
        if (!harness.isEmpty()) {
            base.setModelAttributes(render.getMainModel());
            base.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTicks);
            render.bindTexture(BASE_TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            base.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            if (harness.getItem() == TAItems.THAUMOSTATIC_HARNESS) {
                render.bindTexture(THAUMOSTATIC_TEXTURE);
                GlStateManager.pushMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.enableRescaleNormal();
                GlStateManager.scale(0.1F, 0.1F, 0.1F);
                GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.33F, -3.7F);
                thaumostatic.renderAll();
                GlStateManager.disableRescaleNormal();
                GlStateManager.popMatrix();
                render.bindTexture(LIGHTNING_TEXTURE);
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
                GlStateManager.popMatrix();
                Vec3d backOrig = new Vec3d(0.0, 1.25, -0.4);
                Vec3d back = backOrig.rotateYaw((float) Math.toRadians(-player.renderYawOffset)).add(player.posX, player.posY, player.posZ);
                for (int i = 0; i < 3; ++i) {
                    Vec3d target = new Vec3d((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 2,
                            backOrig.y + (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 2, backOrig.z - 0.2).rotateYaw((float) Math.toRadians(-player.renderYawOffset)).add(player.posX, player.posY, player.posZ);
                    FXDispatcher.INSTANCE.arcBolt(back.x, back.y, back.z, target.x, target.y, target.z, 0.5F, 0.5F, 1.0F, 0.075F);
                }
                GlStateManager.disableBlend();
            }
            else if (harness.getItem() == TAItems.ELYTRA_HARNESS) {
                ModelBase model = ELYTRA_MODEL_DEFAULT;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                
                ItemStack cosmetic = ItemStack.EMPTY;
                IAugmentableItem augmentable = harness.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                if (augmentable != null) {
                    for (ItemStack aug : augmentable.getAllAugments()) {
                        IAugment cap = aug.getCapability(CapabilityAugment.AUGMENT, null);
                        if (cap instanceof IElytraHarnessAugment && ((IElytraHarnessAugment) cap).isCosmetic()) {
                            cosmetic = aug;
                            break;
                        }
                    }
                }
                
                int particleColor = Aspect.FLIGHT.getColor();
                if (!cosmetic.isEmpty()) {
                    if (cosmetic.getItem() == Items.BANNER) {
                        try {
                            final ItemStack ref = cosmetic;
                            render.bindTexture(BANNER_CACHE.get(cosmetic, () -> {
                                ArrayList<ResourceLocation> patterns = new ArrayList<>();
                                ArrayList<EnumDyeColor> colors = new ArrayList<>();
                                patterns.add(new ResourceLocation("minecraft", "textures/entity/banner/" + BannerPattern.BASE.getFileName() + ".png"));
                                colors.add(ItemBanner.getBaseColor(ref));
                                NBTTagList list = ref.getOrCreateSubCompound("BlockEntityTag").getTagList("Patterns", NBT.TAG_COMPOUND);
                                for (int i = 0; i < list.tagCount(); ++i) {
                                    NBTTagCompound c = list.getCompoundTagAt(i);
                                    BannerPattern pattern = BannerPattern.byHash(c.getString("Pattern"));
                                    if (pattern != null) {
                                        ResourceLocation justName = new ResourceLocation(pattern.getFileName());
                                        patterns.add(new ResourceLocation(justName.getNamespace(), "textures/entity/banner/" + justName.getPath() + ".png"));
                                        colors.add(EnumDyeColor.byDyeDamage(c.getInteger("Color")));
                                    }
                                }
                                
                                ResourceLocation textureID = new ResourceLocation(ThaumicAugmentationAPI.MODID, "banner_cache" + ref.hashCode());
                                Minecraft.getMinecraft().getTextureManager().loadTexture(textureID, new VanillaBannerToElytraTexture(VANILLA_BANNER_BASE, patterns, colors));
                                return textureID;
                            }));
                            particleColor = ItemBanner.getBaseColor(cosmetic).getColorValue();
                            model = ELYTRA_MODEL_VANILLA_BANNER;
                        }
                        catch (ExecutionException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    else if (cosmetic.getItem() == ItemBlock.getItemFromBlock(BlocksTC.bannerCrimsonCult)) {
                        try {
                            final ItemStack ref = cosmetic;
                            render.bindTexture(BANNER_CACHE.get(cosmetic, () -> {
                                ResourceLocation textureID = new ResourceLocation(ThaumicAugmentationAPI.MODID, "banner_cache" + ref.hashCode());
                                Minecraft.getMinecraft().getTextureManager().loadTexture(textureID, new TCBannerToElytraTexture(CRIMSON_BANNER));
                                return textureID;
                            }));
                            particleColor = 0xFF0000;
                            model = ELYTRA_MODEL_TC_BANNER;
                        }
                        catch (ExecutionException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    else if (cosmetic.getItem() instanceof BlockBannerTCItem) {
                        try {
                            final ItemStack ref = cosmetic;
                            render.bindTexture(BANNER_CACHE.get(cosmetic, () -> {
                                Aspect aspect = null;
                                if (ref.hasTagCompound())
                                   aspect = Aspect.getAspect(ref.getTagCompound().getString("aspect"));
                                ResourceLocation textureID = new ResourceLocation(ThaumicAugmentationAPI.MODID, "banner_cache" + ref.hashCode());
                                Minecraft.getMinecraft().getTextureManager().loadTexture(textureID, new TCBannerToElytraTexture(TC_BANNER,
                                        aspect, bannerReverseMap.get(((ItemBlock) ref.getItem()).getBlock()).getColorValue()));
                                return textureID;
                            }));
                            particleColor = bannerReverseMap.get(((ItemBlock) ref.getItem()).getBlock()).getColorValue();
                            model = ELYTRA_MODEL_TC_BANNER;
                        }
                        catch (ExecutionException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    else
                        render.bindTexture(ELYTRA_TEXTURE);
                }
                else if (player instanceof AbstractClientPlayer) {
                    AbstractClientPlayer abs = (AbstractClientPlayer) player;
                    if (abs.isPlayerInfoSet() && abs.getLocationElytra() != null)
                        render.bindTexture(abs.getLocationElytra());
                    else if (abs.hasPlayerInfo() && abs.getLocationCape() != null && abs.isWearing(EnumPlayerModelParts.CAPE))
                        render.bindTexture(abs.getLocationCape());
                    else
                        render.bindTexture(ELYTRA_TEXTURE);
                }
                else
                    render.bindTexture(ELYTRA_TEXTURE);

                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 0.0F, 0.125F);
                model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
                model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

                // TODO add enchanted glow cosmetic option?
                /*if (itemstack.isItemEnchanted())
                {
                    LayerArmorBase.renderEnchantedGlint(this.renderPlayer, entitylivingbaseIn, this.modelElytra, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                }*/

                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                
                Random rand = player.getRNG();
                if (player.isElytraFlying() && rand.nextBoolean()) {
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
    
    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
    
}
