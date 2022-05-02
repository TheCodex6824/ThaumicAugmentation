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
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketApplyAugmentConfiguration implements IMessage {

    protected int id;
    
    public PacketApplyAugmentConfiguration() {}
    
    public PacketApplyAugmentConfiguration(int id) {
        this.id = id;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
    }
    
    public int getConfigIndex() {
        return id;
    }
    
}
