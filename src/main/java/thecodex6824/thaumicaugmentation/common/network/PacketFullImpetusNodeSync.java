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

package thecodex6824.thaumicaugmentation.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PacketFullImpetusNodeSync implements IMessage {

    private BlockPos node;
    private NBTTagCompound tag;
    
    public PacketFullImpetusNodeSync() {}
    
    public PacketFullImpetusNodeSync(BlockPos node, NBTTagCompound toSend) {
        this.node = node.toImmutable();
        tag = toSend;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        node = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        try {
            byte[] buffer = new byte[buf.readInt()];
            buf.readBytes(buffer);
            ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
            tag = CompressedStreamTools.readCompressed(stream);
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().warn("Unable to deserialize PacketFullImpetusNodeSync: " + ex.getMessage());
        }
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(node.getX());
        buf.writeInt(node.getY());
        buf.writeInt(node.getZ());
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(tag, stream);
            byte[] data = stream.toByteArray();
            buf.writeInt(data.length);
            buf.writeBytes(data);
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().warn("Unable to serialize PacketFullImpetusNodeSync: " + ex.getMessage());
        }
    }
    
    public NBTTagCompound getTag() {
        return tag;
    }
    
    public BlockPos getNode() { 
        return node;
    }
    
}
