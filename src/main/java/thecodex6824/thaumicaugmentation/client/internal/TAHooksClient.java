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

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import thaumcraft.client.renderers.models.gear.ModelCustomArmor;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.builder.IThaumostaticHarnessAugment;
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
    
    public static void checkElytra(ItemStack chestArmorStack, EntityPlayerSP player) {
        // if regular elytra is also being worn, let vanilla send the packet
        if (chestArmorStack.getItem() != Items.ELYTRA || !ItemElytra.isUsable(chestArmorStack)) {
            IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
            if (baubles != null) {
                ItemStack stack = baubles.getStackInSlot(BaubleType.BODY.getValidSlots()[0]);
                if (stack.getItem() instanceof IElytraCompat && ((IElytraCompat) stack.getItem()).allowElytraFlight(player, stack))
                    player.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING));
            }
        }
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
    
    public static void handleBipedRotation(ModelBiped model, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
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
    
    public static float getRobeRotationDivisor(Entity entity) {
        float f = 1.0F;
        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getTicksElytraFlying() > 4) {
            f = (float) (entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ);
            f /= 0.2F;
            f = Math.max(f * f * f, 1.0F);
        }
        
        return f;
    }
    
    public static void correctCustomArmorRotationPoints(ModelCustomArmor model) {
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
