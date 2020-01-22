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

package thecodex6824.thaumicaugmentation.client.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.client.lib.obj.AdvancedModelLoader;
import thaumcraft.client.lib.obj.IModelCustom;
import thaumcraft.common.blocks.basic.BlockBannerTCItem;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.client.model.ModelElytraBanner;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TCBannerToElytraTexture;
import thecodex6824.thaumicaugmentation.client.renderer.texture.VanillaBannerToElytraTexture;

public class AugmentRenderer {

    protected static final ResourceLocation ELYTRA_BOOSTER_TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/models/harness/elytra_booster.png"); 
    protected static final ResourceLocation ELYTRA_BOOSTER_MODEL = new ResourceLocation(ThaumicAugmentationAPI.MODID, "models/entity/elytra_booster.obj");
    
    protected static IModelCustom elytraBooster;
    protected static HashMap<Block, EnumDyeColor> bannerReverseMap;
    
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
    
    public static void loadModels() {
        elytraBooster = AdvancedModelLoader.loadModel(ELYTRA_BOOSTER_MODEL);
        bannerReverseMap = new HashMap<>();
        for (Map.Entry<EnumDyeColor, Block> entry : BlocksTC.banners.entrySet())
            bannerReverseMap.put(entry.getValue(), entry.getKey());
    }
    
    public static void renderElytraBooster(RenderPlayer renderer, ModelBiped base, EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        
        renderer.bindTexture(ELYTRA_BOOSTER_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(scale * 16.0F, scale * 16.0F, scale * 16.0F);
        if (renderer.getMainModel().isSneak)
            GlStateManager.rotate((float) Math.toDegrees(base.bipedBody.rotateAngleX), 1.0F, 0.0F, 0.0F);
        
        GlStateManager.translate(-0.5F, 0.0F, -0.5F);
        elytraBooster.renderAll();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }
    
    public static void renderBanner(ItemStack cosmetic, RenderPlayer renderer, ModelBiped base, EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
     
        ModelBase model = null;
        if (cosmetic.getItem() == Items.BANNER) {
            try {
                final ItemStack ref = cosmetic;
                renderer.bindTexture(BANNER_CACHE.get(cosmetic, () -> {
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
                model = ELYTRA_MODEL_VANILLA_BANNER;
            }
            catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
        else if (cosmetic.getItem() == ItemBlock.getItemFromBlock(BlocksTC.bannerCrimsonCult)) {
            try {
                final ItemStack ref = cosmetic;
                renderer.bindTexture(BANNER_CACHE.get(cosmetic, () -> {
                    ResourceLocation textureID = new ResourceLocation(ThaumicAugmentationAPI.MODID, "banner_cache" + ref.hashCode());
                    Minecraft.getMinecraft().getTextureManager().loadTexture(textureID, new TCBannerToElytraTexture(CRIMSON_BANNER));
                    return textureID;
                }));
                model = ELYTRA_MODEL_TC_BANNER;
            }
            catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
        else if (cosmetic.getItem() instanceof BlockBannerTCItem) {
            try {
                final ItemStack ref = cosmetic;
                renderer.bindTexture(BANNER_CACHE.get(cosmetic, () -> {
                    Aspect aspect = null;
                    if (ref.hasTagCompound())
                       aspect = Aspect.getAspect(ref.getTagCompound().getString("aspect"));
                    ResourceLocation textureID = new ResourceLocation(ThaumicAugmentationAPI.MODID, "banner_cache" + ref.hashCode());
                    Minecraft.getMinecraft().getTextureManager().loadTexture(textureID, new TCBannerToElytraTexture(TC_BANNER,
                            aspect, bannerReverseMap.get(((ItemBlock) ref.getItem()).getBlock()).getColorValue()));
                    return textureID;
                }));
                model = ELYTRA_MODEL_TC_BANNER;
            }
            catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        if (model != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.125F);
            model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
            model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }
    
    public static void renderBannerParticle(ItemStack cosmetic, RenderPlayer renderer, ModelBiped base, EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        
        Random rand = player.getRNG();
        int particleColor = Aspect.FLIGHT.getColor();
        if (cosmetic.getItem() == Items.BANNER)
            particleColor = ItemBanner.getBaseColor(cosmetic).getColorValue();
        else if (cosmetic.getItem() == ItemBlock.getItemFromBlock(BlocksTC.bannerCrimsonCult))
            particleColor = 0xFF0000;
        else if (cosmetic.getItem() instanceof BlockBannerTCItem)
            particleColor = bannerReverseMap.get(((ItemBlock) cosmetic.getItem()).getBlock()).getColorValue();

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
