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

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thecodex6824.thaumicaugmentation.common.item.ItemFractureLocator;

public class PacketFractureLocatorUpdate implements IMessage {

    private boolean somethingFound;
    private int x;
    private int y;
    private int z;
    
    public PacketFractureLocatorUpdate() {}
    
    public PacketFractureLocatorUpdate(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }
    
    public PacketFractureLocatorUpdate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        somethingFound = true;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        somethingFound = buf.readBoolean();
        if (somethingFound) {
            x = buf.readInt();
            y = buf.readInt();
            z = buf.readInt();
        }
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(somethingFound);
        if (somethingFound) {
            buf.writeInt(x);
            buf.writeInt(y);
            buf.writeInt(z);
        }
    }
    
    public boolean wasFractureFound() {
        return somethingFound;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public static class Handler implements IMessageHandler<PacketFractureLocatorUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(PacketFractureLocatorUpdate message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayerSP player = Minecraft.getMinecraft().player;
                for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                    ItemStack stack = player.inventory.getStackInSlot(i);
                    if (stack.getItem() instanceof ItemFractureLocator) {
                        if (!stack.hasTagCompound())
                            stack.setTagCompound(new NBTTagCompound());
                        
                        stack.getTagCompound().setBoolean("found", message.wasFractureFound());
                        if (message.wasFractureFound())
                            stack.getTagCompound().setIntArray("pos", new int[] {message.getX(), message.getY(), message.getZ()});
                    }
                }
            });
            
            return null;
        }
        
    }
    
}
