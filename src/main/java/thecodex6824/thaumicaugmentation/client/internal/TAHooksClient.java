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

package thecodex6824.thaumicaugmentation.client.internal;

import org.lwjgl.opengl.GL11;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.AnimationTESR;
import thaumcraft.client.gui.GuiResearchPage.BlueprintBlockAccess;
import thaumcraft.client.renderers.models.gear.ModelCustomArmor;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.builder.IThaumostaticHarnessAugment;
import thecodex6824.thaumicaugmentation.client.event.RenderEventHandler;
import thecodex6824.thaumicaugmentation.common.item.trait.IElytraCompat;

public final class TAHooksClient {

    private TAHooksClient() {}
    
    public static boolean checkPlayerSprintState(EntityPlayerSP player, boolean sprint) {
        if (sprint && !player.isCreative() && !player.isSpectator() && player.capabilities.isFlying) {
            IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
            if (baubles != null) {
                ItemStack stack = baubles.getStackInSlot(BaubleType.BODY.getValidSlots()[0]);
                IAugmentableItem augmentable = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                if (augmentable != null) {
                    if (augmentable.getUsedAugmentSlots() > 0) {
                        for (ItemStack augment : augmentable.getAllAugments()) {
                            IAugment aug = augment.getCapability(CapabilityAugment.AUGMENT, null);
                            if (aug instanceof IThaumostaticHarnessAugment) {
                                if (!((IThaumostaticHarnessAugment) aug).shouldAllowSprintFly(player))
                                    return false;
                            }
                        }
                        
                        return sprint;
                    }
                    else
                        return false;
                }
            }
        }
        
        return sprint;
    }
    
    public static void checkElytra(EntityPlayerSP player) {
        // if regular elytra is also being worn, let vanilla send the packet
        ItemStack chestArmorStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chestArmorStack.getItem() != Items.ELYTRA || !ItemElytra.isUsable(chestArmorStack)) {
            IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
            if (baubles != null) {
                ItemStack stack = baubles.getStackInSlot(BaubleType.BODY.getValidSlots()[0]);
                if (stack.getItem() instanceof IElytraCompat && ((IElytraCompat) stack.getItem()).allowElytraFlight(player, stack))
                    player.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING));
            }
        }
    }
    
    public static boolean shouldRenderCape(EntityPlayerSP player) {
        IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
        if (baubles != null) {
            ItemStack stack = baubles.getStackInSlot(BaubleType.BODY.getValidSlots()[0]);
            if (stack.getItem() instanceof IElytraCompat)
                return false;
        }
        
        return true;
    }
    
    public static void handleBipedRotation(ModelBiped model, Entity entity) {
        RenderEventHandler.onRotationAngles(model, entity);
    }
    
    public static float getRobeRotationDivisor(Entity entity) {
        float f = 1.0F;
        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getTicksElytraFlying() > 4) {
            f = (float) (entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ);
            f /= 0.2F;
            f = Math.max(f * f * f, 1.0F);
        }
        
        return f;
    }
    
    public static void correctRotationPoints(ModelBiped model) {
        if (model instanceof ModelCustomArmor) {
            if (model.isSneak) {
                model.bipedRightLeg.rotationPointY = 13.0F;
                model.bipedLeftLeg.rotationPointY = 13.0F;
                model.bipedHead.rotationPointY = 4.5F;
                
                model.bipedBody.rotationPointY = 4.5F;
                model.bipedRightArm.rotationPointY = 5.0F;
                model.bipedLeftArm.rotationPointY = 5.0F;
            }
            else {
                model.bipedBody.rotationPointY = 0.0F;
                model.bipedRightArm.rotationPointY = 2.0F;
                model.bipedLeftArm.rotationPointY = 2.0F;
            }
            
            model.bipedHeadwear.rotationPointX = model.bipedHead.rotationPointX;
            model.bipedHeadwear.rotationPointY = model.bipedHead.rotationPointY;
            model.bipedHeadwear.rotationPointZ = model.bipedHead.rotationPointZ;
        }
    }
    
    public static void onRenderEntities(int pass) {
        RenderEventHandler.onRenderEntities(pass);
    }
    
    public static void renderFastTESRBlueprint(TileEntity tile, BlockPos pos, BlueprintBlockAccess world) {
        TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer(tile);
        if (tesr != null) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            // properly do the thing TC tried to do but failed
            try {
                if (tesr instanceof AnimationTESR) {
                    // Animation TESRs do special things that won't work without actually being in the world
                    // Luckily, they still have models we can fall back to
                    tile.setWorld(Minecraft.getMinecraft().world);
                    BlockRendererDispatcher render = Minecraft.getMinecraft().getBlockRendererDispatcher();
                    IBlockState fake = world.getBlockState(pos);
                    IBakedModel model = render.getBlockModelShapes().getModelForState(fake);
                    render.getBlockModelRenderer().renderModel(tile.getWorld(), model, fake, pos, buffer, false);
                }
                else {
                    // No idea what it can do, so just hope for the best
                    tesr.renderTileEntityFast(tile, pos.getX(), pos.getY(), pos.getZ(), Minecraft.getMinecraft().getRenderPartialTicks(),
                            0, 1.0F, buffer);
                }
            }
            catch (Exception ex) {
                // something doesn't like the fake world, not much we can do
            }
            finally {
                Tessellator.getInstance().draw();
            }
        }
    }
    
}
