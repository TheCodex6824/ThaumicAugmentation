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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
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
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public class RenderEventHandler {

    private static final Cache<Integer, Boolean> CAST_CACHE = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(
            3000, TimeUnit.MILLISECONDS).maximumSize(250).build();
    private static final ResourceLocation BEAM = new ResourceLocation("thaumcraft", "textures/misc/wispy.png");
    
    private static boolean isHoldingCaster(EntityLivingBase entity) {
        for (ItemStack stack : entity.getHeldEquipment()) {
            if (stack.getItem() instanceof ICaster || stack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null) &&
                    stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).getFunctionalStack().getItem() instanceof ICaster)
                return true;
        }
        
        return false;
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
        Vec3d width = new Vec3d(beam.x * 0.07125, beam.y * 0.07125, beam.z * 0.07125);
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
    
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        Collection<IImpetusNode> nodes = ImpetusRenderingManager.getAllRenderableNodes(Minecraft.getMinecraft().world.provider.getDimension());
        if (!nodes.isEmpty()) {
            Entity renderView = Minecraft.getMinecraft().getRenderViewEntity() != null ? Minecraft.getMinecraft().getRenderViewEntity() :
                Minecraft.getMinecraft().player;
            Vec3d eyePos = renderView.getPositionEyes(event.getPartialTicks());
            List<IImpetusNode> renderNodes = nodes.stream()
                    .filter(node -> eyePos.squareDistanceTo(new Vec3d(node.getLocation().getPos())) < 128 * 128)
                    .sorted(Comparator.<IImpetusNode>comparingDouble(node -> eyePos.squareDistanceTo(new Vec3d(node.getLocation().getPos()))).reversed())
                    .collect(Collectors.toList());
            
            for (IImpetusNode node : renderNodes) {
                for (IImpetusNode out : node.getOutputs())
                    renderBeam(renderView, event.getPartialTicks(), eyePos, node.getLocationForRendering(), out.getLocationForRendering());
            }
        }
    }
    
}
