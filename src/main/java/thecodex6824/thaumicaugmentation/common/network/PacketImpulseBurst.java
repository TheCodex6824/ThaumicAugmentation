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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketImpulseBurst implements IMessage {

    protected int id;
    protected Vec3d t;
    protected int num;
    
    public PacketImpulseBurst() {}
    
    public PacketImpulseBurst(int entityID, Vec3d target, int burstNum) {
        id = entityID;
        t = target;
        num = burstNum;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        t = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        num = buf.readInt();
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeDouble(t.x);
        buf.writeDouble(t.y);
        buf.writeDouble(t.z);
        buf.writeInt(num);
    }
    
    public int getEntityID() {
        return id;
    }
    
    public Vec3d getTarget() {
        return t;
    }
    
    public int getBurstNum() {
        return num;
    }
    
}
