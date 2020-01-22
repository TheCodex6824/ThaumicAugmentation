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

package thecodex6824.thaumicaugmentation.common.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;

public class PacketLivingEquipmentChange implements IMessage {

    // max NBT stack size = 2MB
    protected static final int MAX_NBT_SIZE = 1024 * 1024 * 2;
    
    protected int entity;
    protected EntityEquipmentSlot slot;
    protected ItemStack stack;
    
    public PacketLivingEquipmentChange() {}
    
    public PacketLivingEquipmentChange(int entityID, EntityEquipmentSlot equipSlot, ItemStack newStack) {
        entity = entityID;
        slot = equipSlot;
        stack = newStack.copy();
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
        int itemID = buf.readInt();
        if (itemID >= 0) {
            int count = buf.readInt();
            int meta = buf.readInt();
            stack = new ItemStack(Item.getItemById(itemID), count, meta);
            int payloadSize = buf.readInt();
            if (payloadSize > 0) {
                byte[] buffer = new byte[Math.min(payloadSize, MAX_NBT_SIZE)];
                buf.readBytes(buffer, 0, buffer.length);
                try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(buffer))) {
                    stack.getItem().readNBTShareTag(stack, CompressedStreamTools.read(stream));
                }
                catch (IOException ex) {
                    ThaumicAugmentation.getLogger().warn("Unable to deserialize PacketLivingEquipmentChange: " + ex.getMessage());
                }
            }
        }
        else
            stack = ItemStack.EMPTY;
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entity);
        buf.writeInt(slot.getSlotIndex());
        if (stack.isEmpty())
            buf.writeInt(-1);
        else {
            buf.writeInt(Item.getIdFromItem(stack.getItem()));
            buf.writeInt(stack.getCount());
            buf.writeInt(stack.getMetadata());
            if (stack.isItemStackDamageable() || stack.getItem().getShareTag()) {
                NBTTagCompound tag = stack.getItem().getNBTShareTag(stack);
                if (tag != null) {
                    byte[] tagBuffer = null;
                    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream output = new DataOutputStream(bytes)) {
                        CompressedStreamTools.write(tag, output);
                        tagBuffer = bytes.toByteArray();
                    }
                    catch (IOException ex) {
                        ThaumicAugmentation.getLogger().warn("Unable to serialize PacketLivingEquipmentChange: " + ex.getMessage());
                    }
                    
                    buf.writeInt(Math.min(tagBuffer.length, MAX_NBT_SIZE));
                    buf.writeBytes(tagBuffer, 0, Math.min(tagBuffer.length, MAX_NBT_SIZE));
                }
                else
                    buf.writeInt(-1);
            }
            else
                buf.writeInt(-1);
        }
    }
    
    public int getEntityID() {
        return entity;
    }
    
    public EntityEquipmentSlot getSlot() {
        return slot;
    }
    
    public ItemStack getStack() {
        return stack;
    }
    
}
