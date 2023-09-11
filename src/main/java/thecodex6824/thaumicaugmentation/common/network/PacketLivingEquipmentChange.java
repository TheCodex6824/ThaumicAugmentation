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

package thecodex6824.thaumicaugmentation.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketLivingEquipmentChange implements IMessage {
    
    protected int entity;
    protected EntityEquipmentSlot slot;
    
    public PacketLivingEquipmentChange() {}
    
    public PacketLivingEquipmentChange(int entityID, EntityEquipmentSlot equipSlot) {
        entity = entityID;
        slot = equipSlot;
    }
    
    protected EntityEquipmentSlot slotFromIndex(int slotIndex) {
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotIndex() == slotIndex)
                return slot;
        }
        
        return EntityEquipmentSlot.MAINHAND;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        entity = buf.readInt();
        slot = slotFromIndex(buf.readInt());
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entity);
        buf.writeInt(slot.getSlotIndex());
    }
    
    public int getEntityID() {
        return entity;
    }
    
    public EntityEquipmentSlot getSlot() {
        return slot;
    }
    
}
