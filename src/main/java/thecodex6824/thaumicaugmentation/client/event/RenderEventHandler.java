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

package thecodex6824.thaumicaugmentation.client.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.vecmath.Vector4d;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.FogMode;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.api.casters.ICaster;
import thaumcraft.client.fx.ParticleEngine;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.client.ImpetusRenderingManager;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.item.CapabilityImpetusLinker;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;
import thecodex6824.thaumicaugmentation.api.item.IImpetusLinker;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.api.util.RaytraceHelper;
import thecodex6824.thaumicaugmentation.client.fx.FXImpulseBeam;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager;
import thecodex6824.thaumicaugmentation.client.shader.TAShaders;
import thecodex6824.thaumicaugmentation.client.sound.SoundHandleSpecialSound;
import thecodex6824.thaumicaugmentation.common.block.trait.INoBlockOutline;
import thecodex6824.thaumicaugmentation.common.util.IShaderRenderingCallback;
import thecodex6824.thaumicaugmentation.common.util.ISoundHandle;
import thecodex6824.thaumicaugmentation.common.util.MorphicArmorHelper;
import thecodex6824.thaumicaugmentation.common.util.ShaderType;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public class RenderEventHandler {

    private static final Cache<Integer, Boolean> CAST_CACHE = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(
            3000, TimeUnit.MILLISECONDS).maximumSize(250).build();
    private static final Cache<EntityLivingBase, FXImpulseBeam> IMPULSE_CACHE = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterAccess(
            3000, TimeUnit.MILLISECONDS).weakKeys().maximumSize(50).<EntityLivingBase, FXImpulseBeam>removalListener(k -> {
                k.getValue().setExpired();
            }).build();
    
    private static final Object2LongMap<DimensionalBlockPos[]> TRANSACTIONS = Object2LongMaps.synchronize(new Object2LongOpenHashMap<>());
    private static final Object2IntOpenHashMap<DimensionalBlockPos> FRAME_COLORS = new Object2IntOpenHashMap<>();
    
    private static boolean renderShaders = false;
    private static final ArrayList<ArrayList<IShaderRenderingCallback>> SHADER_RENDERS = new ArrayList<>(ShaderType.values().length);
    
    private static final double TRANSACTION_DURATION = 60.0;
    
    private static final NonNullList<ItemStack> tempArmor = NonNullList.withSize(4, ItemStack.EMPTY);
    
    static {
        for (int i = 0; i < ShaderType.values().length; ++i)
            SHADER_RENDERS.add(new ArrayList<>());
    }
    
    public static void onEntityCast(int id) {
        if (TAConfig.gauntletCastAnimation.getValue())
            CAST_CACHE.put(id, true);
    }
    
    public static void onImpetusTransaction(DimensionalBlockPos[] positions) {
        TRANSACTIONS.put(positions, Minecraft.getMinecraft().world.getTotalWorldTime());
    }
    
    public static void onRenderShaderTile(ShaderType type, IShaderRenderingCallback tile) {
        SHADER_RENDERS.get(type.getIndex()).add(tile);
        renderShaders = true;
    }
    
    public static void onImpulseBeam(EntityLivingBase entity, boolean stop) {
        if (stop) {
            FXImpulseBeam beam = IMPULSE_CACHE.getIfPresent(entity);
            if (beam != null)
                beam.setExpired();
            
            IMPULSE_CACHE.invalidate(entity);
        }
        else {
            FXImpulseBeam beam = IMPULSE_CACHE.getIfPresent(entity);
            if (beam == null) {
                Vec3d dest = RaytraceHelper.raytracePosition(entity, TAConfig.cannonBeamRange.getValue());
                beam = new FXImpulseBeam(entity.getEntityWorld(), entity, dest.x, dest.y, dest.z, 0.35F, 0.35F, 0.65F, Integer.MAX_VALUE);
                beam.setPulse(true);
                beam.setFollowOwner(true);
                beam.setImpactTicks(Integer.MAX_VALUE);
                beam.setEndsLoop(true);
                IMPULSE_CACHE.put(entity, beam);
                ParticleEngine.addEffect(entity.getEntityWorld(), beam);
                final int id = entity.getEntityId();
                ISoundHandle handle = ThaumicAugmentation.proxy.playSpecialSound(TASounds.IMPULSE_CANNON_BEAM_LOOP, SoundCategory.PLAYERS,
                        old -> {
                            Entity e = Minecraft.getMinecraft().world.getEntityByID(id);
                            if (e != null && !e.isDead && IMPULSE_CACHE.getIfPresent(e) != null)
                                return e.getPositionVector();
                            else
                                return null;
                        }, (float) entity.posX, (float) entity.posY, (float) entity.posZ, 0.01F, 1.0F, true, 0);
                if (handle instanceof SoundHandleSpecialSound) {
                    SoundHandleSpecialSound s = (SoundHandleSpecialSound) handle;
                    s.setFadeIn(40);
                    if (ThaumicAugmentation.proxy.isEntityRenderView(entity))
                        s.setAttenuationType(AttenuationType.NONE);
                }
            }
        }
    }
    
    @Nullable
    private static EnumHand findCaster(EntityLivingBase entity) {
        for (EnumHand hand : EnumHand.values()) {
            ItemStack stack = entity.getHeldItem(hand);
            if (stack.getItem() instanceof ICaster)
                return hand;
            else if (stack.getItem() == TAItems.MORPHIC_TOOL) {
                IMorphicTool tool = stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null);
                if (tool != null) {
                    EnumAction action = tool.getDisplayStack().getItemUseAction();
                    if (tool.getFunctionalStack().getItem() instanceof ICaster &&
                            (action == EnumAction.NONE || action == EnumAction.BOW))
                        return hand;
                }
            }
        }
        
        return null;
    }
    
    @Nullable
    private static EnumHand findImpulseCannon(EntityLivingBase entity) {
        ItemStack stack = entity.getHeldItemMainhand();
        if (stack.getItem() == TAItems.IMPULSE_CANNON)
            return EnumHand.MAIN_HAND;
        
        stack = entity.getHeldItemOffhand();
        if (stack.getItem() == TAItems.IMPULSE_CANNON)
            return EnumHand.OFF_HAND;
        
        return null;
    }
    
    public static void onRotationAngles(ModelBiped model, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            if (TAConfig.gauntletCastAnimation.getValue() && CAST_CACHE.getIfPresent(living.getEntityId()) != null) {
                EnumHand hand = findCaster(living);
                if (hand != null && model instanceof ModelBiped) {
                    EnumHandSide side = living.getPrimaryHand();
                    if (hand == EnumHand.OFF_HAND)
                        side = side.opposite();
                    
                    if (side == EnumHandSide.RIGHT) {
                        model.bipedRightArm.rotateAngleX = -1.5707963F + model.bipedHead.rotateAngleX;
                        model.bipedRightArm.rotateAngleY = model.bipedHead.rotateAngleY;
                        model.bipedRightArm.rotateAngleZ = model.bipedHead.rotateAngleZ;
                    }
                    else {
                        model.bipedLeftArm.rotateAngleX = -1.5707963F + model.bipedHead.rotateAngleX;
                        model.bipedLeftArm.rotateAngleY = model.bipedHead.rotateAngleY;
                        model.bipedLeftArm.rotateAngleZ = model.bipedHead.rotateAngleZ;
                    }
                    
                    return;
                }
            }
            
            EnumHand hand = findImpulseCannon(living);
            if (hand != null) {
                EnumHand oppositeHand = hand == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                EnumHandSide side = living.getPrimaryHand();
                if (hand == EnumHand.OFF_HAND)
                    side = side.opposite();
                
                if (side == EnumHandSide.RIGHT) {
                    model.bipedRightArm.rotateAngleX = -1.5707963F + model.bipedHead.rotateAngleX;
                    model.bipedRightArm.rotateAngleY = model.bipedHead.rotateAngleY;
                    model.bipedRightArm.rotateAngleZ = model.bipedHead.rotateAngleZ;
                    
                    if (living.getHeldItem(oppositeHand).isEmpty()) {
                        model.bipedLeftArm.rotateAngleX = -1.5707963F + model.bipedHead.rotateAngleX;
                        model.bipedLeftArm.rotateAngleY = model.bipedHead.rotateAngleY + 0.62831853F;
                        model.bipedLeftArm.rotateAngleZ = model.bipedHead.rotateAngleZ;
                    }
                }
                else {
                    model.bipedLeftArm.rotateAngleX = -1.5707963F + model.bipedHead.rotateAngleX;
                    model.bipedLeftArm.rotateAngleY = model.bipedHead.rotateAngleY;
                    model.bipedLeftArm.rotateAngleZ = model.bipedHead.rotateAngleZ;
                    
                    if (living.getHeldItem(oppositeHand).isEmpty()) {
                        model.bipedRightArm.rotateAngleX = -1.5707963F + model.bipedHead.rotateAngleX;
                        model.bipedRightArm.rotateAngleY = model.bipedHead.rotateAngleY - 0.62831853F;
                        model.bipedRightArm.rotateAngleZ = model.bipedHead.rotateAngleZ;
                    }
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
        List<ItemStack> armor = MorphicArmorHelper.getArmorInventory(event.getEntity());
        for (int i = 0; i < armor.size(); ++i) {
            ItemStack s = armor.get(i);
            ItemStack disp = MorphicArmorHelper.getMorphicArmor(s);
            if (!disp.isEmpty()) {
                tempArmor.set(i, s);
                armor.set(i, disp);
            }
        }
    }
    
    private static void restoreArmor(EntityLivingBase entity) {
        List<ItemStack> armor = MorphicArmorHelper.getArmorInventory(entity);
        for (int i = 0; i < armor.size(); ++i) {
            ItemStack s = tempArmor.get(i);
            if (!s.isEmpty()) {
                armor.set(i, s);
                tempArmor.set(i, ItemStack.EMPTY);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRenderLivingPreCheck(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (event.isCanceled())
            restoreArmor(event.getEntity());
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onRenderLivingPost(RenderLivingEvent.Post<EntityLivingBase> event) {
        restoreArmor(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onRenderBlockOutline(DrawBlockHighlightEvent event) {
        if (event.getTarget().getBlockPos() != null && event.getPlayer().getEntityWorld().getBlockState(
                event.getTarget().getBlockPos()).getBlock() instanceof INoBlockOutline) {
            
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onFogSetup(EntityViewRenderEvent.RenderFogEvent event) {
    	if (event.getState().getMaterial() == TAMaterials.TAINTED_SLURRY) {
    		GlStateManager.setFog(FogMode.EXP);
            GlStateManager.setFogDensity(0.1F);
    	}
    	else if (!TAConfig.disableEmptiness.getValue() && event.getEntity().dimension == TAConfig.emptinessDimID.getValue()) {
    		GlStateManager.setFogStart(Math.min(event.getFarPlaneDistance(), 192) * 0.4F);
    		GlStateManager.setFogEnd(Math.min(event.getFarPlaneDistance(), 192) * 0.9F);
    	}
    }
    
    private static Vec3d rotate(Vec3d input, double angle, Vec3d axis) {
        double sin = Math.sin(angle * 0.5);
        Vector4d vec = new Vector4d(axis.x * sin, axis.y * sin, axis.z * sin, Math.cos(angle * 0.5));
        double d = -vec.x * input.x - vec.y * input.y - vec.z * input.z;
        double d1 = vec.w * input.x + vec.y * input.z - vec.z * input.y;
        double d2 = vec.w * input.y - vec.x * input.z + vec.z * input.x;
        double d3 = vec.w * input.z + vec.x * input.y - vec.y * input.x;
        return new Vec3d(d1 * vec.w - d * vec.x - d2 * vec.z + d3 * vec.y,
                d2 * vec.w - d * vec.y + d1 * vec.z - d3 * vec.x,
                d3 * vec.w - d * vec.z - d1 * vec.y + d2 * vec.x);
    }
    
    private static void renderNormalBeam(Entity rv, float partial, Vec3d from, Vec3d to) {
        Vec3d se = to.subtract(from);
        Vec3d axis = (se.z == 0.0 ? new Vec3d(se.y, -se.x, 0.0) : new Vec3d(0.0, se.z, -se.y)).normalize();
        double dist = from.distanceTo(to);
        double offset = rv.ticksExisted % 100 / 100.0 + partial / 100.0;
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.BEAM);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        double angle = 0;
        for (int i = 0; i < 4; ++i) {
            Vec3d perpendicular = rotate(axis, angle, se).normalize().scale(0.0625);
            Vec3d p1 = from.add(perpendicular);
            Vec3d p2 = to.add(perpendicular);
            Vec3d p3 = to.subtract(perpendicular);
            Vec3d p4 = from.subtract(perpendicular);
            
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(p1.x, p1.y, p1.z).tex(1.0 - offset, 0).color(0.4F, 0.4F, 0.5F, 0.65F).endVertex();
            buffer.pos(p2.x, p2.y, p2.z).tex(dist - offset, 0).color(0.4F, 0.4F, 0.5F, 0.65F).endVertex();
            buffer.pos(p3.x, p3.y, p3.z).tex(dist - offset, 1.0).color(0.4F, 0.4F, 0.5F, 0.65F).endVertex();
            buffer.pos(p4.x, p4.y, p4.z).tex(1.0 - offset, 1.0).color(0.4F, 0.4F, 0.5F, 0.65F).endVertex();
            t.draw();
            angle += Math.PI / 24;
        }
        
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }
    
    private static void renderStrongLaser(Entity rv, float partial, Vec3d eyePos, Vec3d from, Vec3d to, double factor) {
        Vec3d se = to.subtract(from);
        Vec3d axis = (se.z == 0.0 ? new Vec3d(se.y, -se.x, 0.0) : new Vec3d(0.0, se.z, -se.y)).normalize();
        double dist = from.distanceTo(to);
        double offset = rv.ticksExisted % 100 / 100.0 + partial / 100.0;
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.LASER);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        double angle = 0;//offset * Math.PI * 2;
        for (int i = 0; i < 4; ++i) {
            Vec3d perpendicular = rotate(axis, angle, se).normalize().scale(0.4275);
            Vec3d p1 = from.add(perpendicular);
            Vec3d p2 = to.add(perpendicular);
            Vec3d p3 = to.subtract(perpendicular);
            Vec3d p4 = from.subtract(perpendicular);
            
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(p1.x, p1.y, p1.z).tex(1.0 - offset, 0).color(0.35F, 0.35F, 0.5F, (float) factor).endVertex();
            buffer.pos(p2.x, p2.y, p2.z).tex(dist - offset, 0).color(0.35F, 0.35F, 0.5F, (float) factor).endVertex();
            buffer.pos(p3.x, p3.y, p3.z).tex(dist - offset, 1.0).color(0.35F, 0.35F, 0.5F, (float) factor).endVertex();
            buffer.pos(p4.x, p4.y, p4.z).tex(1.0 - offset, 1.0).color(0.35F, 0.35F, 0.5F, (float) factor).endVertex();
            t.draw();
            angle += Math.PI / 4;
        }
        
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }
    
    private static void renderCubeFrame(Entity rv, float partial, Vec3d eyePos, BlockPos blockPosition, AxisAlignedBB cube,
            float r, float g, float b, float a) {
        
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        
        Vec3d pos = new Vec3d(blockPosition);
        Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.FRAME);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(pos.x, pos.y + 1, pos.z + 1).tex(0, 0).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y + 1, pos.z + 1).tex(1, 0).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y, pos.z + 1).tex(0, 1).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y, pos.z + 1).tex(1, 1).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y, pos.z).tex(1, 0).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y + 1, pos.z + 1).tex(0, 1).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y + 1, pos.z).tex(0, 0).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y + 1, pos.z + 1).tex(1, 1).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y + 1, pos.z).tex(1, 0).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y, pos.z + 1).tex(0, 1).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y, pos.z).tex(0, 0).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y, pos.z).tex(1, 0).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y + 1, pos.z).tex(0, 1).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y + 1, pos.z).tex(1, 1).color(r, g, b, a).endVertex();
        t.draw();
        
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
    }
    
    public static void onRenderEntities(int pass) {
        if (pass == 0) {
            float pt = ThaumicAugmentation.proxy.isSingleplayer() && Minecraft.getMinecraft().isGamePaused() ?
                    0.0F : Minecraft.getMinecraft().getRenderPartialTicks();
            Entity rv = Minecraft.getMinecraft().getRenderViewEntity() != null ? Minecraft.getMinecraft().getRenderViewEntity() :
                Minecraft.getMinecraft().player;
            
            double rX = rv.lastTickPosX + (rv.posX - rv.lastTickPosX) * pt;
            double rY = rv.lastTickPosY + (rv.posY - rv.lastTickPosY) * pt;
            double rZ = rv.lastTickPosZ + (rv.posZ - rv.lastTickPosZ) * pt;
            if (renderShaders) {
                GlStateManager.disableLighting();
                GlStateManager.enableCull();
                for (ShaderType type : ShaderType.values()) {
                    ArrayList<IShaderRenderingCallback> toRender = SHADER_RENDERS.get(type.getIndex());
                    if (!toRender.isEmpty()) {
                        switch (type.getIndex()) {
                            case 0: {
                                if (TAShaderManager.shouldUseShaders())
                                    TAShaderManager.enableShader(TAShaders.FLUX_RIFT, TAShaders.SHADER_CALLBACK_GENERIC_SPHERE);
                                else
                                    GlStateManager.color(0.1F, 0.4F, 0.5F, 1.0F);
                                
                                Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.RIFT);
                                break;
                            }
                            case 1: {
                                if (TAShaderManager.shouldUseShaders())
                                    TAShaderManager.enableShader(TAShaders.FRACTURE, TAShaders.SHADER_CALLBACK_GENERIC_SPHERE);
                                else
                                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                                
                                Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.EMPTINESS_SKY);
                                break;
                            }
                            case 2: {
                                if (TAShaderManager.shouldUseShaders())
                                    TAShaderManager.enableShader(TAShaders.MIRROR, TAShaders.SHADER_CALLBACK_GENERIC_SPHERE);
                                else
                                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                                
                                Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.MIRROR);
                                break;
                            }
                            default: {
                                // should be unreachable
                                Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.RIFT);
                                break;
                            }
                        }
                        
                        for (IShaderRenderingCallback tile : toRender)
                            tile.renderWithShader(type, rX, rY, rZ);
                        
                        if (TAShaderManager.shouldUseShaders())
                            TAShaderManager.disableShader();
                        else
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        
                        toRender.clear();
                    }
                }
                
                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                GlStateManager.disableCull();
                GlStateManager.enableLighting();
                renderShaders = false;
            }
        }
    }
    
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        Entity rv = Minecraft.getMinecraft().getRenderViewEntity() != null ? Minecraft.getMinecraft().getRenderViewEntity() :
            Minecraft.getMinecraft().player;
        Vec3d eyePos = rv.getPositionEyes(event.getPartialTicks());
        WorldClient world = Minecraft.getMinecraft().world;
        GlStateManager.pushMatrix();
        GlStateManager.translate(-(rv.lastTickPosX + (rv.posX - rv.lastTickPosX) * event.getPartialTicks()),
                -(rv.lastTickPosY + (rv.posY - rv.lastTickPosY) * event.getPartialTicks()), -(rv.lastTickPosZ + (rv.posZ - rv.lastTickPosZ) * event.getPartialTicks()));
        Collection<IImpetusNode> nodes = ImpetusRenderingManager.getAllRenderableNodes(world.provider.getDimension());
        if (!nodes.isEmpty()) {
            List<IImpetusNode> renderNodes = nodes.stream()
                    .filter(node -> {
                        if (world.isBlockLoaded(node.getLocation().getPos())) {
                            TileEntity tile = world.getTileEntity(node.getLocation().getPos());
                            return tile != null && tile.hasCapability(CapabilityImpetusNode.IMPETUS_NODE, null) && eyePos.squareDistanceTo(new Vec3d(node.getLocation().getPos())) < 128 * 128;   
                        }
                        
                        return false;
                    })
                    .sorted(Comparator.<IImpetusNode>comparingDouble(node -> eyePos.squareDistanceTo(new Vec3d(node.getLocation().getPos()))).reversed())
                    .collect(Collectors.toList());
            
            for (IImpetusNode node : renderNodes) {
                for (IImpetusNode out : node.getOutputs()) {
                    if (node.shouldPhysicalBeamLinkTo(out) && out.shouldPhysicalBeamLinkTo(node))
                        renderNormalBeam(rv, event.getPartialTicks(), node.getBeamEndpoint(), out.getBeamEndpoint());
                }
            }
        }
        
        long time = world.getTotalWorldTime();
        Iterator<Object2LongOpenHashMap.Entry<DimensionalBlockPos[]>> iterator = TRANSACTIONS.object2LongEntrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<DimensionalBlockPos[], Long> entry = iterator.next();
            DimensionalBlockPos[] array = entry.getKey();
            long passed = time - entry.getValue();
            if (passed < 0 || passed > TRANSACTION_DURATION)
                iterator.remove();
            else {
                for (int i = 0; i < array.length - 1; ++i) {
                    IImpetusNode start = ImpetusRenderingManager.findNodeByPosition(array[i]);
                    IImpetusNode end = ImpetusRenderingManager.findNodeByPosition(array[i + 1]);
                    if (start != null && end != null && start.shouldPhysicalBeamLinkTo(end) && end.shouldPhysicalBeamLinkTo(start))
                        renderStrongLaser(rv, event.getPartialTicks(), eyePos, start.getBeamEndpoint(), end.getBeamEndpoint(), 1.0 - passed / TRANSACTION_DURATION);
                }
            }
        }
        
        EntityPlayer player = Minecraft.getMinecraft().player;
        for (ItemStack stack : player.getHeldEquipment()) {
            IImpetusLinker linker = stack.getCapability(CapabilityImpetusLinker.IMPETUS_LINKER, null);
            if (linker != null) {
                DimensionalBlockPos dimPos = linker.getOrigin();
                if (!dimPos.isInvalid() && player.dimension == dimPos.getDimension()) {
                    BlockPos pos = dimPos.getPos();
                    if (world.isBlockLoaded(pos) && world.getTileEntity(pos) != null) {
                        IImpetusNode cap = world.getTileEntity(pos).getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                        if (cap != null) {
                            Vec3d eyes = player.getPositionEyes(event.getPartialTicks());
                            if (pos.distanceSq(eyes.x, eyes.y, eyes.z) < 64 * 64) {
                                renderCubeFrame(rv, event.getPartialTicks(), rv.getPositionEyes(event.getPartialTicks()),
                                         pos, player.world.getBlockState(pos).getBoundingBox(player.world, pos), 0.8F, 0.8F, 1.0F, 1.0F);
                            }
                            
                            FRAME_COLORS.clear();
                            for (DimensionalBlockPos p : cap.getInputLocations())
                                FRAME_COLORS.addTo(p, 1);
                            
                            for (DimensionalBlockPos p : cap.getOutputLocations())
                                FRAME_COLORS.addTo(p, 2);
                            
                            for (DimensionalBlockPos p : FRAME_COLORS.keySet()) {
                                if (p.getDimension() == player.dimension && p.getPos().distanceSq(eyes.x, eyes.y, eyes.z) < 64 * 64) {
                                    int color = FRAME_COLORS.getInt(p);
                                    float r = 0.8F, g = 0.8F, b = 0.8F, a = 0.8F;
                                    switch (color) {
                                        case 1: {
                                            r = 1.0F;
                                            break;
                                        }
                                        case 2: {
                                            g = 1.0F;
                                            break;
                                        }
                                        default: {
                                            r = 1.0F;
                                            b = 1.0F;
                                            break;
                                        }
                                    }
                                    
                                    renderCubeFrame(rv, event.getPartialTicks(), rv.getPositionEyes(event.getPartialTicks()),
                                             p.getPos(), player.world.getBlockState(p.getPos()).getBoundingBox(player.world, p.getPos()), r, g, b, a);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        ArrayList<EntityLivingBase> toRemove = new ArrayList<>();
        for (Map.Entry<EntityLivingBase, FXImpulseBeam> entry : IMPULSE_CACHE.asMap().entrySet()) {
            if (findImpulseCannon(entry.getKey()) != null) {
                Vec3d dest = RaytraceHelper.raytracePosition(entry.getKey(), TAConfig.cannonBeamRange.getValue(), event.getPartialTicks());
                entry.getValue().updateBeamTarget(dest.x, dest.y, dest.z);
            }
            else
                toRemove.add(entry.getKey());
        }
        
        for (EntityLivingBase e : toRemove)
            IMPULSE_CACHE.invalidate(e);
        
        GlStateManager.popMatrix();
    }
    
}
