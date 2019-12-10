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

package thecodex6824.thaumicaugmentation.common.internal;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.builder.IThaumostaticHarnessAugment;

public final class TAHooksClient {

    private TAHooksClient() {}
    
    public static boolean checkPlayerSprintState(EntityPlayerSP player, boolean sprint) {
        if (sprint && !player.isCreative() && !player.isSpectator() && player.capabilities.isFlying) {
            IBaublesItemHandler baubles = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
            if (baubles != null) {
                ItemStack stack = baubles.getStackInSlot(BaubleType.BODY.getValidSlots()[0]);
                IAugmentableItem augmentable = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                if (augmentable != null) {
                    for (ItemStack augment : augmentable.getAllAugments()) {
                        IAugment aug = augment.getCapability(CapabilityAugment.AUGMENT, null);
                        if (aug instanceof IThaumostaticHarnessAugment) {
                            if (!((IThaumostaticHarnessAugment) aug).shouldAllowSprintFly(player))
                                return false;
                        }
                    }
                }
            }
        }
        
        return sprint;
    }
    
}
