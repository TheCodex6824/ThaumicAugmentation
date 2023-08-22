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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class PacketFullImpetusNodeSync extends PacketSyncTagCompound {

    private BlockPos node;
    
    public PacketFullImpetusNodeSync() {}
    
    public PacketFullImpetusNodeSync(BlockPos node, NBTTagCompound toSend) {
    	super(toSend);
        this.node = node.toImmutable();
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        node = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        super.fromBytes(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(node.getX());
        buf.writeInt(node.getY());
        buf.writeInt(node.getZ());
        super.toBytes(buf);
    }
    
    public BlockPos getNode() { 
        return node;
    }
    
}
