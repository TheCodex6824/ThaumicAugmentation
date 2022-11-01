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
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import javax.annotation.Nullable;

public class PacketRecoil implements IMessage {

    public enum RecoilType {
        IMPULSE_BURST(0),
        IMPULSE_RAILGUN(1);
        
        private final int id;
        
        RecoilType(int i) {
            id = i;
        }
        
        public int getID() {
            return id;
        }
        
        @Nullable
        public static RecoilType fromID(int id) {
            for (RecoilType effect : values()) {
                if (effect.getID() == id)
                    return effect;
            }
            
            return null;
        }
    }
    
    protected int id;
    protected RecoilType type;
    
    public PacketRecoil() {}
    
    public PacketRecoil(int entityID, RecoilType recoilType) {
        id = entityID;
        type = recoilType;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        type = RecoilType.fromID(buf.readInt());
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(type.getID());
    }
    
    public int getEntityID() {
        return id;
    }
    
    public RecoilType getRecoilType() {
        return type;
    }
    
}
