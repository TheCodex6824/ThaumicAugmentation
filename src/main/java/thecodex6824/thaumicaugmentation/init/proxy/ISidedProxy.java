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

import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.golems.seals.ISealEntity;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorage;
import thecodex6824.thaumicaugmentation.common.util.ISoundHandle;
import thecodex6824.thaumicaugmentation.common.util.ITARenderHelper;

public interface ISidedProxy {

    public IAnimationStateMachine loadASM(ResourceLocation loc, ImmutableMap<String, ITimeValue> params);

    public ITARenderHelper getRenderHelper();
    
    public IWardStorage createWardStorageInstance(World world);
    
    public void handlePacketClient(IMessage message, MessageContext context);
    
    public void handlePacketServer(IMessage message, MessageContext context);
    
    public Container getServerGUIElement(int ID, EntityPlayer player, World world, int x, int y, int z);
    
    public Object getClientGUIElement(int ID, EntityPlayer player, World world, int x, int y, int z);
    
    public Object getSealContainer(World world, EntityPlayer player, BlockPos pos, EnumFacing face, ISealEntity seal);
    
    public Object getSealGUI(World world, EntityPlayer player, BlockPos pos, EnumFacing face, ISealEntity seal);
    
    public void registerRenderableImpetusNode(IImpetusNode node);
    
    public boolean deregisterRenderableImpetusNode(IImpetusNode node);
    
    public boolean isOpenToLAN();
    
    public boolean isSingleplayer();
    
    public boolean isElytraBoostKeyDown();
    
    public boolean isPvPEnabled();
    
    public boolean isEntityClientPlayer(Entity e);
    
    public boolean isEntityRenderView(Entity e);
    
    public float getPartialTicks();
    
    public ISoundHandle playSpecialSound(SoundEvent sound, SoundCategory category, Function<Vec3d, Vec3d> tick,
            float x, float y, float z, float vol, float pitch, boolean repeat, int repeatDelay);
    
    public void preInit();

    public void init();

    public void postInit();
    
}
