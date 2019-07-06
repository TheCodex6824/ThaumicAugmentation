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

package thecodex6824.thaumicaugmentation.init.proxy;

import com.google.common.collect.ImmutableMap;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import thecodex6824.thaumicaugmentation.common.network.PacketFullWardSync;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketWardUpdate;
import thecodex6824.thaumicaugmentation.common.util.ITARenderHelper;

public interface ISidedProxy {

    public IAnimationStateMachine loadASM(ResourceLocation loc, ImmutableMap<String, ITimeValue> params);

    public ITARenderHelper getRenderHelper();
    
    public void handleParticlePacket(PacketParticleEffect message);
    
    public void handleFullWardSyncPacket(PacketFullWardSync message);
    
    public void handleWardUpdatePacket(PacketWardUpdate message);

    public void preInit();

    public void init();

    public void postInit();

}
