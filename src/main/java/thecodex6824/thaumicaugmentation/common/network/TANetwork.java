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

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.common.lib.network.misc.PacketAuraToClient;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

public final class TANetwork {

    private TANetwork() {}
    
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ThaumicAugmentationAPI.MODID);

    public static void init() {
        int id = 0;
        INSTANCE.registerMessage(PacketAuraToClient.class, PacketAuraToClient.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketParticleEffect.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketConfigSync.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketAugmentableItemSync.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketFractureLocatorUpdate.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketFullWardSync.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketWardUpdate.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketEntityCast.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketFullImpetusNodeSync.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketImpetusNodeUpdate.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketImpetusTransaction.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericMessageHandler<>(), PacketRiftJarInstability.class, id++, Side.CLIENT);
    }

}
