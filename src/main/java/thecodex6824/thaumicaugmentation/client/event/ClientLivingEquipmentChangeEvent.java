/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;

public class ClientLivingEquipmentChangeEvent extends LivingEvent {
   
    protected EntityEquipmentSlot slot;
    protected ItemStack to;

    public ClientLivingEquipmentChangeEvent(EntityLivingBase entity, EntityEquipmentSlot equipmentSlot,
            ItemStack toStack) {
        
        super(entity);
        slot = equipmentSlot;
        to = toStack;
    }

    public EntityEquipmentSlot getSlot() { 
        return slot;
    }
    
    public ItemStack getTo() { 
        return to;
    }

}
