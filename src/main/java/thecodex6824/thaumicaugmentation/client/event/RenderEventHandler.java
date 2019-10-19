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
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.api.casters.ICaster;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.client.ImpetusRenderingManager;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public class RenderEventHandler {

    private static final Cache<Integer, Boolean> CAST_CACHE = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(
            3000, TimeUnit.MILLISECONDS).maximumSize(250).build();
    
    private static final HashMap<DimensionalBlockPos[], Long> TRANSACTIONS = new HashMap<>();
    
    private static final ResourceLocation BEAM = new ResourceLocation("thaumcraft", "textures/misc/wispy.png");
    private static final ResourceLocation LASER = new ResourceLocation("thaumcraft", "textures/misc/beamh.png");
    
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
    
    private static void renderBeam(Entity rv, float partial, Vec3d eyePos, Vec3d from, Vec3d to) {
        Vec3d ps = new Vec3d(from.x - eyePos.x, from.y - eyePos.y, from.z - eyePos.z);
        Vec3d se = new Vec3d(to.x - from.x, to.y - from.y, to.z - from.z);
        Vec3d beam = ps.crossProduct(se).normalize();
        Vec3d width = new Vec3d(beam.x * 0.106875, beam.y * 0.106875, beam.z * 0.106875);
        Vec3d p1 = from.add(width);
        Vec3d p2 = to.add(width);
        Vec3d p3 = to.subtract(width);
        Vec3d p4 = from.subtract(width);
        double dist = from.distanceTo(to);
        
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-(rv.lastTickPosX + (rv.posX - rv.lastTickPosX) * partial),
                -(rv.lastTickPosY + (rv.posY - rv.lastTickPosY) * partial),
                -(rv.lastTickPosZ + (rv.posZ - rv.lastTickPosZ) * partial));
        
        double offset = rv.ticksExisted % 100 / 100.0;
        Minecraft.getMinecraft().renderEngine.bindTexture(BEAM);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(p1.x, p1.y, p1.z).tex(1.0 - offset, 0).color(0.4F, 0.4F, 0.5F, 0.65F).endVertex();
        buffer.pos(p2.x, p2.y, p2.z).tex(dist - offset, 0).color(0.4F, 0.4F, 0.5F, 0.65F).endVertex();
        buffer.pos(p3.x, p3.y, p3.z).tex(dist - offset, 1.0).color(0.4F, 0.4F, 0.5F, 0.65F).endVertex();
        buffer.pos(p4.x, p4.y, p4.z).tex(1.0 - offset, 1.0).color(0.4F, 0.4F, 0.5F, 0.65F).endVertex();
        t.draw();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }
    
    private static void renderLaser(Entity rv, float partial, Vec3d eyePos, Vec3d from, Vec3d to, double factor) {
        Vec3d ps = new Vec3d(from.x - eyePos.x, from.y - eyePos.y, from.z - eyePos.z);
        Vec3d se = new Vec3d(to.x - from.x, to.y - from.y, to.z - from.z);
        Vec3d beam = ps.crossProduct(se).normalize();
        Vec3d width = new Vec3d(beam.x * 0.4275 * factor, beam.y * 0.4275 * factor, beam.z * 0.4275 * factor);
        Vec3d p1 = from.add(width);
        Vec3d p2 = to.add(width);
        Vec3d p3 = to.subtract(width);
        Vec3d p4 = from.subtract(width);
        double dist = from.distanceTo(to);
        
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-(rv.lastTickPosX + (rv.posX - rv.lastTickPosX) * partial),
                -(rv.lastTickPosY + (rv.posY - rv.lastTickPosY) * partial),
                -(rv.lastTickPosZ + (rv.posZ - rv.lastTickPosZ) * partial));
        
        double offset = rv.ticksExisted % 25 / 25.0;
        Minecraft.getMinecraft().renderEngine.bindTexture(LASER);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(p1.x, p1.y, p1.z).tex(1.0 - offset, 0).color(0.5F, 0.4F, 0.5F, (float) factor).endVertex();
        buffer.pos(p2.x, p2.y, p2.z).tex(dist - offset, 0).color(0.5F, 0.4F, 0.5F, (float) factor).endVertex();
        buffer.pos(p3.x, p3.y, p3.z).tex(dist - offset, 1.0).color(0.5F, 0.4F, 0.5F, (float) factor).endVertex();
        buffer.pos(p4.x, p4.y, p4.z).tex(1.0 - offset, 1.0).color(0.5F, 0.4F, 0.5F, (float) factor).endVertex();
        t.draw();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }
    
    private static void renderCubeOutline(Entity rv, float partial, Vec3d eyePos, BlockPos blockPosition, AxisAlignedBB cube,
            float r, float g, float b, float a) {
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        
        Vec3d pos = new Vec3d(blockPosition);
        GlStateManager.translate(-(rv.lastTickPosX + (rv.posX - rv.lastTickPosX) * partial),
                -(rv.lastTickPosY + (rv.posY - rv.lastTickPosY) * partial),
                -(rv.lastTickPosZ + (rv.posZ - rv.lastTickPosZ) * partial));
        
        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GlStateManager.glLineWidth(3.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(pos.x, pos.y, pos.z).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y + 1, pos.z).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y, pos.z).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y + 1, pos.z).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y, pos.z + 1).color(r, g, b, a).endVertex();
        buffer.pos(pos.x + 1, pos.y + 1, pos.z + 1).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y, pos.z + 1).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y + 1, pos.z + 1).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y, pos.z).color(r, g, b, a).endVertex();
        buffer.pos(pos.x, pos.y + 1, pos.z).color(r, g, b, a).endVertex();
        t.draw();
        
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }
    
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        Entity renderView = Minecraft.getMinecraft().getRenderViewEntity() != null ? Minecraft.getMinecraft().getRenderViewEntity() :
            Minecraft.getMinecraft().player;
        Vec3d eyePos = renderView.getPositionEyes(event.getPartialTicks());
        WorldClient world = Minecraft.getMinecraft().world;
        
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
                    //if (world.isBlockLoaded(out.getLocation().getPos()) && world.getTileEntity(out.getLocation().getPos()) != null &&
                    //        world.getTileEntity(out.getLocation().getPos()).hasCapability(CapabilityImpetusNode.IMPETUS_NODE, null)) {
                    
                        renderBeam(renderView, event.getPartialTicks(), eyePos, node.getBeamEndpoint(), out.getBeamEndpoint());
                    //}
                }
            }
        }
        
        long time = world.getTotalWorldTime();
        Iterator<Map.Entry<DimensionalBlockPos[], Long>> iterator = TRANSACTIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<DimensionalBlockPos[], Long> entry = iterator.next();
            DimensionalBlockPos[] array = entry.getKey();
            double factor = 1.0 - (time - entry.getValue()) / 20.0;
            for (int i = 0; i < array.length - 1; ++i) {
                IImpetusNode start = ImpetusRenderingManager.findNodeByPosition(array[i]);
                IImpetusNode end = ImpetusRenderingManager.findNodeByPosition(array[i + 1]);
                if (start != null && end != null)
                    renderLaser(renderView, event.getPartialTicks(), eyePos, start.getBeamEndpoint(), end.getBeamEndpoint(), factor);
            }
            
            if (factor < 0.0)
                iterator.remove();
        }
        
        EntityPlayer player = Minecraft.getMinecraft().player;
        for (ItemStack stack : player.getHeldEquipment()) {
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("impetusBindSelection", NBT.TAG_INT_ARRAY)) {
                int[] data = stack.getTagCompound().getIntArray("impetusBindSelection");
                if (data.length == 4 && player.dimension == data[3]) {
                    BlockPos pos = new BlockPos(data[0], data[1], data[2]);
                    if (world.isBlockLoaded(pos) && world.getTileEntity(pos) != null) {
                        IImpetusNode cap = world.getTileEntity(pos).getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                        if (cap != null) {
                            Vec3d eyes = player.getPositionEyes(event.getPartialTicks());
                            if (pos.distanceSq(eyes.x, eyes.y, eyes.z) < 64 * 64) {
                                renderCubeOutline(renderView, event.getPartialTicks(), renderView.getPositionEyes(event.getPartialTicks()),
                                         pos, player.world.getBlockState(pos).getBoundingBox(player.world, pos), 0.8F, 0.8F, 1.0F, 1.0F);
                            }
                            
                            for (DimensionalBlockPos p : cap.getInputLocations()) {
                                if (p.getDimension() == player.dimension && p.getPos().distanceSq(eyes.x, eyes.y, eyes.z) < 64 * 64) {
                                    renderCubeOutline(renderView, event.getPartialTicks(), renderView.getPositionEyes(event.getPartialTicks()),
                                             p.getPos(), player.world.getBlockState(p.getPos()).getBoundingBox(player.world, p.getPos()), 1.0F, 0.8F, 0.8F, 1.0F);
                                }
                            }
                            
                            for (DimensionalBlockPos p : cap.getOutputLocations()) {
                                if (p.getDimension() == player.dimension && p.getPos().distanceSq(eyes.x, eyes.y, eyes.z) < 64 * 64) {
                                    renderCubeOutline(renderView, event.getPartialTicks(), renderView.getPositionEyes(event.getPartialTicks()),
                                             p.getPos(), player.world.getBlockState(p.getPos()).getBoundingBox(player.world, p.getPos()), 0.8F, 1.0F, 0.8F, 1.0F);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
}
