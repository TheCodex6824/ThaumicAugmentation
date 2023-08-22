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

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.common.lib.network.fx.PacketFXFocusPartImpact;
import thaumcraft.common.lib.network.fx.PacketFXShield;
import thaumcraft.common.lib.network.misc.PacketAuraToClient;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

public final class TANetwork {

    private TANetwork() {}
    
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ThaumicAugmentationAPI.MODID);

    public static void init() {
        int id = 0;
        INSTANCE.registerMessage(PacketAuraToClient.class, PacketAuraToClient.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketParticleEffect.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketConfigSync.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketAugmentableItemSync.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketFractureLocatorUpdate.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketFullWardSync.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketWardUpdate.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketEntityCast.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketFullImpetusNodeSync.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketImpetusNodeUpdate.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketImpetusTransaction.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketRiftJarInstability.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketBiomeUpdate.class, id++, Side.CLIENT);
        // this is a TC packet but the handler impl doesn't add a scheduled task
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketFXShield.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketImpulseBeam.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketImpulseBurst.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketImpulseRailgunProjectile.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(PacketFXFocusPartImpact.class, PacketFXFocusPartImpact.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketLivingEquipmentChange.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketBaubleChange.class, id++, Side.CLIENT);
        // not to be confused with TC's PacketFXWispZap that does more rendering on the network thread
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketWispZap.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketFollowingOrb.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketFlightState.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketBoostState.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketRecoil.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketTerraformerWork.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketEssentiaUpdate.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketPartialAugmentConfigurationStorageSync.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(new GenericClientMessageHandler<>(), PacketFullAugmentConfigurationStorageSync.class, id++, Side.CLIENT);
        
        INSTANCE.registerMessage(new GenericServerMessageHandler<>(), PacketInteractGUI.class, id++, Side.SERVER);
        INSTANCE.registerMessage(new GenericServerMessageHandler<>(), PacketElytraBoost.class, id++, Side.SERVER);
        INSTANCE.registerMessage(new GenericServerMessageHandler<>(), PacketApplyAugmentConfiguration.class, id++, Side.SERVER);
    }

}
