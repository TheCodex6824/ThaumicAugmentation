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
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public class PacketImpetusTransaction implements IMessage {

    // if someone needs to increase this sometime, here ya go
    public static int maxPathLength = 1024;
    
    private DimensionalBlockPos[] positions;
    
    public PacketImpetusTransaction() {}
    
    public PacketImpetusTransaction(DimensionalBlockPos[] path) {
        positions = Arrays.copyOf(path, path.length);
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        byte[] buffer = new byte[Math.min(buf.readInt(), maxPathLength * 16)];
        buf.readBytes(buffer);
        try (DataInputStream stream = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(buffer)))) {
            positions = new DimensionalBlockPos[Math.min(stream.readInt(), maxPathLength)];
            for (int i = 0; i < positions.length; ++i)
                positions[i] = new DimensionalBlockPos(stream.readInt(), stream.readInt(), stream.readInt(), stream.readInt());
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().error("Unable to deserialize PacketImpetusTransaction: " + ex.getMessage());
        }
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream(4 + positions.length * 4 * 4);
                DataOutputStream data = new DataOutputStream(new GZIPOutputStream(stream))) {
            data.writeInt(positions.length);
            for (DimensionalBlockPos pos : positions) {
                data.writeInt(pos.getPos().getX());
                data.writeInt(pos.getPos().getY());
                data.writeInt(pos.getPos().getZ());
                data.writeInt(pos.getDimension());
            }
            
            data.close();
            byte[] buffer = stream.toByteArray();
            buf.writeInt(buffer.length);
            buf.writeBytes(buffer);
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().error("Unable to serialize PacketImpetusTransaction: " + ex.getMessage());
        }
    }
    
    public DimensionalBlockPos[] getPositions() {
        return positions;
    }
    
}
