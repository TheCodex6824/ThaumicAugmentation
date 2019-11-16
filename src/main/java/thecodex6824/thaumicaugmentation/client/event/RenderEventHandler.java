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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.vecmath.Vector4d;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.api.casters.ICaster;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.client.ImpetusRenderingManager;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.item.CapabilityImpetusLinker;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;
import thecodex6824.thaumicaugmentation.api.item.IImpetusLinker;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public class RenderEventHandler {

    private static final Cache<Integer, Boolean> CAST_CACHE = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(
            3000, TimeUnit.MILLISECONDS).maximumSize(250).build();
    
    private static final HashMap<DimensionalBlockPos[], Long> TRANSACTIONS = new HashMap<>();
    
    private static final ResourceLocation BEAM = new ResourceLocation("thaumcraft", "textures/misc/wispy.png");
    private static final ResourceLocation LASER = new ResourceLocation("thaumcraft", "textures/misc/beamh.png");
    private static final ResourceLocation FRAME = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/misc/frame_1x1_simple.png");
    
    private static final double TRANSACTION_DURATION = 60.0;
    
    private static boolean isHoldingCaster(EntityLivingBase entity) {
        for (ItemStack stack : entity.getHeldEquipment()) {
            if (stack.getItem() instanceof ICaster || stack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null) &&
                    stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().getItem() instanceof ICaster)
                return true;
        }
        
        return false;
    }
    
    public static void onEntityCast(int id) {
        if (TAConfig.gauntletCastAnimation.getValue())
            CAST_CACHE.put(id, true);
    }
    
    public static void onImpetusTransaction(DimensionalBlockPos[] positions) {
        TRANSACTIONS.put(positions, Minecraft.getMinecraft().world.getTotalWorldTime());
    }
    
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (TAConfig.gauntletCastAnimation.getValue() && isHoldingCaster(event.getEntity())) {
            Boolean value = CAST_CACHE.getIfPresent(event.getEntity().getEntityId());
            if (value != null && event.getRenderer().getMainModel() instanceof ModelBiped) {
                ModelBiped biped = (ModelBiped) event.getRenderer().getMainModel();
                if (event.getEntity().getActiveHand() == EnumHand.MAIN_HAND)
                    biped.rightArmPose = ArmPose.BOW_AND_ARROW;
                else
                    biped.leftArmPose = ArmPose.BOW_AND_ARROW;
            }
        }
    }
    
    private static Vec3d rotate(Vec3d input, double angle, Vec3d axis) {
        double sin = Math.sin(angle * 0.5);
        Vector4d vec = new Vector4d(axis.x * sin, axis.y * sin, axis.z * sin, Math.cos(angle * 0.5));
        double d1 = -vec.x * input.x - vec.y * input.y - vec.z * input.z;
        double d2 = vec.w * input.x + vec.y * input.z - vec.z * input.y;
        double d3 = vec.w * input.y - vec.x * input.z + vec.z * input.x;
        double d4 = vec.w * input.z + vec.x * input.y - vec.y * input.x;
        return new Vec3d(d2 * vec.w - d1 * vec.x - d3 * vec.z + d4 * vec.y,
                d3 * vec.w - d1 * vec.y + d2 * vec.z - d4 * vec.x,
                d4 * vec.w - d1 * vec.z - d2 * vec.y + d3 * vec.x);
    }
    
    private static void renderNormalBeam(Entity rv, float partial, Vec3d from, Vec3d to) {
        Vec3d se = to.subtract(from);
        Vec3d axis = se.crossProduct(se.z == 0.0 ? new Vec3d(se.y, -se.x, 0.0) : new Vec3d(0.0, se.z, -se.y)).normalize();
        double dist = from.distanceTo(to);
        double offset = rv.ticksExisted % 100 / 100.0 + partial / 100.0;
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(BEAM);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        double angle = offset * Math.PI * 2;
        for (int i = 0; i < 4; ++i) {
            Vec3d perpendicular = rotate(axis, angle, se).normalize().scale(0.125);
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
            angle += Math.PI / 4;
        }
        
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }
    
    private static void renderStrongLaser(Entity rv, float partial, Vec3d eyePos, Vec3d from, Vec3d to, double factor) {
        Vec3d se = to.subtract(from);
        Vec3d axis = se.crossProduct(se.z == 0.0 ? new Vec3d(se.y, -se.x, 0.0) : new Vec3d(0.0, se.z, -se.y)).normalize();
        double dist = from.distanceTo(to);
        double offset = rv.ticksExisted % 100 / 100.0 + partial / 100.0;
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(LASER);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        double angle = offset * Math.PI * 2;
        for (int i = 0; i < 4; ++i) {
            Vec3d perpendicular = rotate(axis, angle, se).normalize().scale(0.4275);
            Vec3d p1 = from.add(perpendicular);
            Vec3d p2 = to.add(perpendicular);
            Vec3d p3 = to.subtract(perpendicular);
            Vec3d p4 = from.subtract(perpendicular);
            
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(p1.x, p1.y, p1.z).tex(1.0 - offset, 0).color(0.35F, 0.35F, 0.5F, 0.65F).endVertex();
            buffer.pos(p2.x, p2.y, p2.z).tex(dist - offset, 0).color(0.35F, 0.35F, 0.5F, 0.65F).endVertex();
            buffer.pos(p3.x, p3.y, p3.z).tex(dist - offset, 1.0).color(0.35F, 0.35F, 0.5F, 0.65F).endVertex();
            buffer.pos(p4.x, p4.y, p4.z).tex(1.0 - offset, 1.0).color(0.35F, 0.35F, 0.5F, 0.65F).endVertex();
            t.draw();
            angle += Math.PI / 4;
        }
        
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }
    
    private static void renderCubeFrame(Entity rv, float partial, Vec3d eyePos, BlockPos blockPosition, AxisAlignedBB cube,
            float r, float g, float b, float a) {
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        
        Vec3d pos = new Vec3d(blockPosition);
        
        Minecraft.getMinecraft().renderEngine.bindTexture(FRAME);
        GlStateManager.disableCull();
        
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
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
    }
    
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        Entity rv = Minecraft.getMinecraft().getRenderViewEntity() != null ? Minecraft.getMinecraft().getRenderViewEntity() :
            Minecraft.getMinecraft().player;
        Vec3d eyePos = rv.getPositionEyes(event.getPartialTicks());
        WorldClient world = Minecraft.getMinecraft().world;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(-(rv.lastTickPosX + ((rv.posX - rv.lastTickPosX) * event.getPartialTicks())),
                -(rv.lastTickPosY + ((rv.posY - rv.lastTickPosY) * event.getPartialTicks())),
                -(rv.lastTickPosZ + ((rv.posZ - rv.lastTickPosZ) * event.getPartialTicks())));
        
        Collection<IImpetusNode> nodes = ImpetusRenderingManager.getAllRenderableNodes(world.provider.getDimension());
        if (!nodes.isEmpty()) {
            List<IImpetusNode> renderNodes = nodes.stream()
                    .filter(node -> {
                        return eyePos.squareDistanceTo(new Vec3d(node.getLocation().getPos())) < 128 * 128 &&
                        world.isBlockLoaded(node.getLocation().getPos()) &&
                        world.getTileEntity(node.getLocation().getPos()) != null &&
                        world.getTileEntity(node.getLocation().getPos()).hasCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                    })
                    .sorted(Comparator.<IImpetusNode>comparingDouble(node -> eyePos.squareDistanceTo(new Vec3d(node.getLocation().getPos()))).reversed())
                    .collect(Collectors.toList());
            
            for (IImpetusNode node : renderNodes) {
                for (IImpetusNode out : node.getOutputs()) {
                    if (node.shouldDrawBeamTo(out) && out.shouldDrawBeamTo(node))
                        renderNormalBeam(rv, event.getPartialTicks(), node.getBeamEndpoint(), out.getBeamEndpoint());
                }
            }
        }
        
        long time = world.getTotalWorldTime();
        Iterator<Map.Entry<DimensionalBlockPos[], Long>> iterator = TRANSACTIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<DimensionalBlockPos[], Long> entry = iterator.next();
            DimensionalBlockPos[] array = entry.getKey();
            double factor = 1.0 - (time - entry.getValue()) / TRANSACTION_DURATION;
            for (int i = 0; i < array.length - 1; ++i) {
                IImpetusNode start = ImpetusRenderingManager.findNodeByPosition(array[i]);
                IImpetusNode end = ImpetusRenderingManager.findNodeByPosition(array[i + 1]);
                if (start != null && end != null && start.shouldDrawBeamTo(end) && end.shouldDrawBeamTo(start))
                    renderStrongLaser(rv, event.getPartialTicks(), eyePos, start.getBeamEndpoint(), end.getBeamEndpoint(), factor);
            }
            
            if (factor < 0.0)
                iterator.remove();
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
                            
                            for (DimensionalBlockPos p : cap.getInputLocations()) {
                                if (p.getDimension() == player.dimension && p.getPos().distanceSq(eyes.x, eyes.y, eyes.z) < 64 * 64) {
                                    renderCubeFrame(rv, event.getPartialTicks(), rv.getPositionEyes(event.getPartialTicks()),
                                             p.getPos(), player.world.getBlockState(p.getPos()).getBoundingBox(player.world, p.getPos()), 1.0F, 0.8F, 0.8F, 1.0F);
                                }
                            }
                            
                            for (DimensionalBlockPos p : cap.getOutputLocations()) {
                                if (p.getDimension() == player.dimension && p.getPos().distanceSq(eyes.x, eyes.y, eyes.z) < 64 * 64) {
                                    renderCubeFrame(rv, event.getPartialTicks(), rv.getPositionEyes(event.getPartialTicks()),
                                             p.getPos(), player.world.getBlockState(p.getPos()).getBoundingBox(player.world, p.getPos()), 0.8F, 1.0F, 0.8F, 1.0F);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        GlStateManager.popMatrix();
    }
    
}
