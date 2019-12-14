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

import java.util.Random;

import com.google.common.collect.ImmutableMap;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.common.golems.client.gui.SealBaseContainer;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.builder.IElytraHarnessAugment;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.warded.IWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageServer;
import thecodex6824.thaumicaugmentation.common.container.ContainerArcaneTerraformer;
import thecodex6824.thaumicaugmentation.common.container.ContainerWardedChest;
import thecodex6824.thaumicaugmentation.common.network.PacketElytraBoost;
import thecodex6824.thaumicaugmentation.common.network.PacketInteractGUI;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneTerraformer;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedChest;
import thecodex6824.thaumicaugmentation.common.util.ITARenderHelper;
import thecodex6824.thaumicaugmentation.common.util.TARenderHelperServer;
import thecodex6824.thaumicaugmentation.init.GUIHandler.TAInventory;

public class ServerProxy implements ISidedProxy {

    protected static ITARenderHelper renderHelper;

    @Override
    public IAnimationStateMachine loadASM(ResourceLocation loc, ImmutableMap<String, ITimeValue> params) {
        return null;
    }

    @Override
    public ITARenderHelper getRenderHelper() {
        if (renderHelper == null)
            renderHelper = new TARenderHelperServer();

        return renderHelper;
    }
    
    @Override
    public IWardStorage createWardStorageInstance(World world) {
        return new WardStorageServer();
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
    public boolean isSingleplayer() {
        return false;
    }
    
    @Override
    public boolean isJumpDown() {
        return false;
    }
    
    @Override
    public Container getServerGUIElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (TAInventory.values()[ID]) {
            case WARDED_CHEST: return new ContainerWardedChest(player.inventory, 
                (TileWardedChest) world.getTileEntity(new BlockPos(x, y, z)));
            case ARCANE_TERRAFORMER: return new ContainerArcaneTerraformer(player.inventory, 
                    (TileArcaneTerraformer) world.getTileEntity(new BlockPos(x, y, z)));
            default: return null;
        }
    }
    
    @Override
    public Object getClientGUIElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        throw new UnsupportedOperationException("Cannot get client GUI element on the server side!");
    }
    
    @Override
    public Object getSealContainer(World world, EntityPlayer player, BlockPos pos, EnumFacing face, ISealEntity seal) {
        return new SealBaseContainer(player.inventory, world, seal);
    }
    
    @Override
    public Object getSealGUI(World world, EntityPlayer player, BlockPos pos, EnumFacing face, ISealEntity seal) {
        throw new UnsupportedOperationException("Cannot get client GUI element on the server side!");
    }
    
    @Override
    public void handlePacketClient(IMessage message, MessageContext context) {
        ThaumicAugmentation.getLogger().warn("A packet was received on the wrong side: " + message.getClass().toString());
    }
    
    @Override
    public void handlePacketServer(IMessage message, MessageContext context) {
        if (message instanceof PacketElytraBoost)
            handleElytraBoostPacket((PacketElytraBoost) message, context);
        else if (message instanceof PacketInteractGUI)
            handleInteractGUIPacket((PacketInteractGUI) message, context);
        else
            ThaumicAugmentation.getLogger().warn("An unknown packet was received and will be dropped: " + message.getClass().toString());
    }
    
    protected void handleElytraBoostPacket(PacketElytraBoost message, MessageContext context) {
        EntityPlayerMP entity = context.getServerHandler().player;
        ItemStack body = BaublesApi.getBaublesHandler(entity).getStackInSlot(BaubleType.BODY.getValidSlots()[0]);
        if (!body.isEmpty() && body.getItem() == TAItems.ELYTRA_HARNESS) {
            IAugmentableItem i = body.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
            if (i != null) {
                ItemStack augment = ItemStack.EMPTY;
                for (ItemStack stack : i.getAllAugments()) {
                    IAugment aug = stack.getCapability(CapabilityAugment.AUGMENT, null);
                    if (aug instanceof IElytraHarnessAugment && !((IElytraHarnessAugment) aug).isCosmetic()) {
                        augment = stack;
                        break;
                    }
                }
                
                IImpetusStorage energy = augment.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                if (energy != null && entity.isElytraFlying() && energy.extractEnergy(1, false) == 1) {
                    Vec3d vec3d = entity.getLookVec();
                    entity.motionX += vec3d.x * 0.1 + (vec3d.x * 1.5 - entity.motionX) * 0.5;
                    entity.motionY += vec3d.y * 0.1 + (vec3d.y * 1.5 - entity.motionY) * 0.5;
                    entity.motionZ += vec3d.z * 0.1 + (vec3d.z * 1.5 - entity.motionZ) * 0.5;
                    Random rand = entity.world.rand;
                    PacketParticleEffect packet = new PacketParticleEffect(ParticleEffect.FIRE,
                            entity.posX + rand.nextFloat(), entity.posY + rand.nextFloat() * 2, entity.posZ + rand.nextFloat(),
                            1.0, 0x101030);
                    TANetwork.INSTANCE.sendTo(packet, entity);
                    TANetwork.INSTANCE.sendToAllTracking(packet, entity);
                    // we don't send a velocity update packet here as it makes the client's view of the player glitch out
                    // at this point the client is correct anyway, we only need to send one if they are wrong
                }
                else
                    entity.connection.sendPacket(new SPacketEntityVelocity(entity));
            }
            else
                entity.connection.sendPacket(new SPacketEntityVelocity(entity));
        }
        else
            entity.connection.sendPacket(new SPacketEntityVelocity(entity));
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
    public void preInit() {}

    @Override
    public void init() {}

    @Override
    public void postInit() {}

}
