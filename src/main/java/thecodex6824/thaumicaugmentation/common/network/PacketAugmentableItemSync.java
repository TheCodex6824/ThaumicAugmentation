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
import java.io.IOException;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.augment.AugmentAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;

public class PacketAugmentableItemSync implements IMessage {
    
    private int id;
    private int index;
    private NBTTagCompound nbt;
    
    public PacketAugmentableItemSync() {}
    
    public PacketAugmentableItemSync(int entityID, int i, NBTTagCompound sync) {
        id = entityID;
        index = i;
        nbt = sync;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        index = buf.readInt();
        try {
            byte[] buffer = new byte[buf.readInt()];
            buf.readBytes(buffer);
            ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
            nbt = CompressedStreamTools.readCompressed(stream);
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().warn("Unable to deserialize PacketAugmentableItemSync: " + ex.getMessage());
        }
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(index);
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(nbt, stream);
            byte[] data = stream.toByteArray();
            buf.writeInt(data.length);
            buf.writeBytes(data);
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().warn("Unable to serialize PacketAugmentableItemSync: " + ex.getMessage());
        }
    }
    
    public NBTTagCompound getTagCompound() {
        return nbt;
    }
    
    public int getItemIndex() {
        return index;
    }
    
    public int getEntityID() {
        return id;
    }
    
    public static class Handler implements IMessageHandler<PacketAugmentableItemSync, IMessage> {
        
        @Override
        public IMessage onMessage(PacketAugmentableItemSync message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.getEntityID());
                if (entity != null) {
                    int i = 0;
                    for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentableItemSources()) {
                        for (ItemStack stack : func.apply(entity)) {
                            if (i == message.getItemIndex()) {
                                if (stack.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
                                    stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null).deserializeNBT(message.getTagCompound());
                                    return;
                                }
                            }
                            
                            ++i;
                        }
                    }
                }
            });
            
            return null;
        }
        
    }
    
}
