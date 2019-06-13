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

package thecodex6824.thaumicaugmentation.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import thaumcraft.api.casters.FocusPackage;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.common.network.PacketAugmentableItemSync;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public final class AugmentEventHelper {
    
    private AugmentEventHelper() {}
    
    public static void fireEquipEvent(IAugmentableItem cap, Entity entity) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onEquip(a, entity);
        }
    }
    
    public static void fireUnequipEvent(IAugmentableItem cap, Entity entity) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onUnequip(a, entity);
        }
    }
    
    public static void fireCastEvent(IAugmentableItem cap, ItemStack caster, FocusPackage focusPackage, Entity entity) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onCast(a, caster, focusPackage, entity);
        }
    }
    
    public static void fireHurtEntityEvent(IAugmentableItem cap, Entity attacker, Entity attacked) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onHurtEntity(a, attacker, attacked);
        }
    }
    
    public static void fireHurtByEntityEvent(IAugmentableItem cap, Entity attacked, Entity attacker) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onHurt(a, attacked, attacker);
        }
    }
    
    public static void fireDamageEntityEvent(IAugmentableItem cap, Entity attacker, Entity attacked) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onDamagedEntity(a, attacker, attacked);
        }
    }
    
    public static void fireDamagedByEntityEvent(IAugmentableItem cap, Entity attacked, Entity attacker) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onDamaged(a, attacked, attacker);
        }
    }
    
    public static void fireInteractEntityEvent(IAugmentableItem cap, Entity user, ItemStack used, Entity target, EnumHand hand) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onInteractEntity(a, user, used, target, hand);
        }
    }
    
    public static void fireInteractBlockEvent(IAugmentableItem cap, Entity user, ItemStack used, BlockPos target, EnumFacing face, EnumHand hand) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onInteractBlock(a, user, used, target, face, hand);
        }
    }
    
    public static void fireInteractAirEvent(IAugmentableItem cap, Entity user, ItemStack used, EnumHand hand) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onInteractAir(a, user, used, hand);
        }
    }
    
    public static void fireUseItemEvent(IAugmentableItem cap, Entity user, ItemStack used) {
        for (ItemStack a : cap.getAllAugments()) {
            if (a.getItem() instanceof IAugment)
                ((IAugment) a.getItem()).onUseItem(a, user, used);
        }
    }
    
    public static void fireTickEvent(IAugmentableItem cap, Entity entity) {
        for (ItemStack stack : cap.getAllAugments()) {
            if (stack.getItem() instanceof IAugment)
                ((IAugment) stack.getItem()).onTick(stack, entity);
        }
    }
    
    public static void handleSync(IAugmentableItem cap, Entity entity, int index) {
        boolean sync = false;
        for (ItemStack stack : cap.getAllAugments()) {
            if (stack.getItem() instanceof IAugment) {
                IAugment aug = (IAugment) stack.getItem();
                if (aug.shouldSync(stack) && entity.getEntityWorld().getTotalWorldTime() % aug.getSyncInterval(stack) == 0) {
                    sync = true;
                    break;
                }
            }
        }
        
        if (sync) {
            PacketAugmentableItemSync syncPacket = new PacketAugmentableItemSync(entity.getEntityId(), index, cap.serializeNBT());
            if (entity instanceof EntityPlayerMP)
                TANetwork.INSTANCE.sendTo(syncPacket, (EntityPlayerMP) entity);
          
            TANetwork.INSTANCE.sendToAllTracking(syncPacket, entity);
        }
    }
    
}
