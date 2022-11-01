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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public class PacketImpetusNodeUpdate implements IMessage {

    private BlockPos node;
    private DimensionalBlockPos dest;
    private boolean output;
    private boolean remove;
    
    public PacketImpetusNodeUpdate() {}
    
    public PacketImpetusNodeUpdate(BlockPos node, DimensionalBlockPos dest, boolean output, boolean remove) {
        this.node = node.toImmutable();
        this.dest = new DimensionalBlockPos(dest.getPos().toImmutable(), dest.getDimension());
        this.output = output;
        this.remove = remove;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        node = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        dest = new DimensionalBlockPos(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
        output = buf.readBoolean();
        remove = buf.readBoolean();
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(node.getX());
        buf.writeInt(node.getY());
        buf.writeInt(node.getZ());
        buf.writeInt(dest.getPos().getX());
        buf.writeInt(dest.getPos().getY());
        buf.writeInt(dest.getPos().getZ());
        buf.writeInt(dest.getDimension());
        buf.writeBoolean(output);
        buf.writeBoolean(remove);
    }
    
    public BlockPos getNode() {
        return node;
    }
    
    public DimensionalBlockPos getDest() {
        return dest;
    }
    
    public boolean shouldRemove() {
        return remove;
    }
    
    public boolean isOutput() {
        return output;
    }
    
}
