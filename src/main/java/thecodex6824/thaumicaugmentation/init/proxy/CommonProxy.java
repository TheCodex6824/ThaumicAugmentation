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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.internal.TAInternals;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;
import thecodex6824.thaumicaugmentation.common.container.ContainerArcaneTerraformer;
import thecodex6824.thaumicaugmentation.common.event.WardEventHandler;
import thecodex6824.thaumicaugmentation.common.event.WardEventHandlerNoCoremodFallback;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationHandler;
import thecodex6824.thaumicaugmentation.common.internal.InternalMethodProvider;
import thecodex6824.thaumicaugmentation.common.network.PacketInteractGUI;
import thecodex6824.thaumicaugmentation.common.util.ITARenderHelper;
import thecodex6824.thaumicaugmentation.common.util.TARenderHelperCommon;
import thecodex6824.thaumicaugmentation.init.CapabilityHandler;
import thecodex6824.thaumicaugmentation.init.GUIHandler;
import thecodex6824.thaumicaugmentation.init.MiscHandler;
import thecodex6824.thaumicaugmentation.init.ResearchHandler;
import thecodex6824.thaumicaugmentation.init.WorldHandler;

public class CommonProxy implements ISidedProxy {

    protected static ITARenderHelper renderHelper;

    @Override
    public IAnimationStateMachine loadASM(ResourceLocation loc, ImmutableMap<String, ITimeValue> params) {
        return null;
    }

    @Override
    public ITARenderHelper getRenderHelper() {
        if (renderHelper == null)
            renderHelper = new TARenderHelperCommon();

        return renderHelper;
    }
    
    @Override
    public void registerRenderableImpetusNode(IImpetusNode node) {}
    
    @Override
    public boolean deregisterRenderableImpetusNode(IImpetusNode node) {
        return false;
    }
    
    @Override
    public boolean isOpenToLAN() {
        return false;
    }
    
    @Override
    public void handlePacketClient(IMessage message, MessageContext context) {
        ThaumicAugmentation.getLogger().warn("A packet was received on the wrong side: " + message.getClass().toString());
    }
    
    @Override
    public void handlePacketServer(IMessage message, MessageContext context) {
        if (message instanceof PacketInteractGUI)
            handleInteractGUIPacket((PacketInteractGUI) message, context);
        else
            ThaumicAugmentation.getLogger().warn("An unknown packet was received and will be dropped: " + message.getClass().toString());
    }
    
    protected void handleInteractGUIPacket(PacketInteractGUI message, MessageContext context) {
        EntityPlayerMP sender = context.getServerHandler().player;
        if (sender != null && sender.openContainer instanceof ContainerArcaneTerraformer) {
            ContainerArcaneTerraformer terraformer = (ContainerArcaneTerraformer) sender.openContainer;
            if (!terraformer.getTile().isRunning()) {
                if (message.getComponentID() == 0)
                    terraformer.getTile().setRadius(Math.max(Math.min(message.getSelectionValue(), 32), 1));
                else if (message.getComponentID() == 1)
                    terraformer.getTile().setCircle(message.getSelectionValue() != 0);
            }
        }
    }

    @Override
    public void preInit() {
        TAConfigHolder.preInit();
        CapabilityHandler.preInit();
        WorldHandler.preInit();
        NetworkRegistry.INSTANCE.registerGuiHandler(ThaumicAugmentation.instance, new GUIHandler());
        TAInternals.setInternalMethodProvider(new InternalMethodProvider());
        MiscHandler.preInit();
        IntegrationHandler.preInit();
        
        if (!TAConfig.disableWardFocus.getValue()) {
            if (ThaumicAugmentationAPI.isCoremodAvailable())
                MinecraftForge.EVENT_BUS.register(new WardEventHandler());
            else
                MinecraftForge.EVENT_BUS.register(new WardEventHandlerNoCoremodFallback());
        }
    }

    @Override
    public void init() {
        WorldHandler.init();
        ResearchHandler.init();
        MiscHandler.init();
        IntegrationHandler.init();
    }

    @Override
    public void postInit() {
        WorldHandler.postInit();
        IntegrationHandler.postInit();
    }

}
