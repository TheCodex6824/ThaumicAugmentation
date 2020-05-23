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
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;
import thecodex6824.thaumicaugmentation.common.network.PacketAugmentableItemSync;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

/**
 * Utility class for firing augment-related events.
 * @author TheCodex6824
 */
public final class AugmentEventHelper {
    
    private AugmentEventHelper() {}
    
    public static void fireEquipEvent(IAugmentableItem cap, Entity entity) {
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                aug.onEquip(entity);
        }
    }
    
    public static void fireUnequipEvent(IAugmentableItem cap, Entity entity) {
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                aug.onUnequip(entity);
        }
    }
    
    public static boolean fireCastPreEvent(IAugmentableItem cap, ItemStack caster, FocusWrapper focusPackage, Entity entity) {
        boolean res = false;
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                res |= aug.onCastPre(caster, focusPackage, entity);
        }
        
        return res;
    }
    
    public static void fireCastPostEvent(IAugmentableItem cap, ItemStack caster, FocusWrapper focusPackage, Entity entity) {
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                aug.onCastPost(caster, focusPackage, entity);
        }
    }
    
    public static boolean fireHurtEntityEvent(IAugmentableItem cap, Entity attacker, Entity attacked) {
        boolean res = false;
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                res |= aug.onHurtEntity(attacker, attacked);
        }
        
        return res;
    }
    
    public static boolean fireHurtByEntityEvent(IAugmentableItem cap, Entity attacked, Entity attacker) {
        boolean res = false;
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                res |= aug.onHurt(attacked, attacker);
        }
        
        return res;
    }
    
    public static boolean fireDamageEntityEvent(IAugmentableItem cap, Entity attacker, Entity attacked) {
        boolean res = false;
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                res |= aug.onDamagedEntity(attacker, attacked);
        }
        
        return res;
    }
    
    public static boolean fireDamagedByEntityEvent(IAugmentableItem cap, Entity attacked, Entity attacker) {
        boolean res = false;
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                res |= aug.onDamaged(attacked, attacker);
        }
        
        return res;
    }
    
    public static boolean fireInteractEntityEvent(IAugmentableItem cap, Entity user, ItemStack used, Entity target, EnumHand hand) {
        boolean res = false;
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                res |= aug.onInteractEntity(user, used, target, hand);
        }
        
        return res;
    }
    
    public static boolean fireInteractBlockEvent(IAugmentableItem cap, Entity user, ItemStack used, BlockPos target, EnumFacing face, EnumHand hand) {
        boolean res = false;
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                res |= aug.onInteractBlock(user, used, target, face, hand);
        }
        
        return res;
    }
    
    public static boolean fireInteractAirEvent(IAugmentableItem cap, Entity user, ItemStack used, EnumHand hand) {
        boolean res = false;
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                res |= aug.onInteractAir(user, used, hand);
        }
        
        return res;
    }
    
    public static boolean fireUseItemEvent(IAugmentableItem cap, Entity user, ItemStack used) {
        boolean res = false;
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                res |= aug.onUseItem(user, used);
        }
        
        return res;
    }
    
    public static boolean fireTickEvent(IAugmentableItem cap, Entity entity) {
        boolean res = false;
        for (ItemStack a : cap.getAllAugments()) {
            IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
            if (aug != null)
                res |= aug.onTick(entity);
        }
        
        return res;
    }
    
    public static void handleSync(IAugmentableItem cap, Entity entity, int index) {
        if (!entity.getEntityWorld().isRemote) {
            boolean sync = false;
            for (ItemStack a : cap.getAllAugments()) {
                IAugment aug = a.getCapability(CapabilityAugment.AUGMENT, null);
                if (aug != null && aug.shouldSync()) {
                    sync = true;
                    break;
                }
            }
            
            if (sync) {
                PacketAugmentableItemSync syncPacket = new PacketAugmentableItemSync(entity.getEntityId(), index, cap.getSyncNBT());
                if (entity instanceof EntityPlayerMP)
                    TANetwork.INSTANCE.sendTo(syncPacket, (EntityPlayerMP) entity);
              
                TANetwork.INSTANCE.sendToAllTracking(syncPacket, entity);
            }
        }
    }
    
}
