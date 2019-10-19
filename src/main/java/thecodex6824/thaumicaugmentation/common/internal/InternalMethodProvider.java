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

package thecodex6824.thaumicaugmentation.common.internal;

import java.util.Collection;

import com.google.common.base.Predicates;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.internal.IInternalMethodProvider;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterEffectProvider;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterStrengthProvider;
import thecodex6824.thaumicaugmentation.common.network.PacketFullImpetusNodeSync;
import thecodex6824.thaumicaugmentation.common.network.PacketImpetusNodeUpdate;
import thecodex6824.thaumicaugmentation.common.network.PacketImpetusTransaction;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class InternalMethodProvider implements IInternalMethodProvider {
    
    @Override
    public void addConfigListener(Runnable listener) {
        TAConfigHolder.addListener(listener);
    }
    
    @Override
    public boolean removeConfigListener(Runnable listener) {
        return TAConfigHolder.removeListener(listener);
    }
    
    @Override
    public ItemStack createCasterStrengthProviderStack(ResourceLocation id) {
        return ItemCustomCasterStrengthProvider.create(id);
    }
    
    @Override
    public String getCasterStrengthProviderID(ItemStack stack) {
        return ItemCustomCasterStrengthProvider.getProviderIDString(stack);
    }
    
    @Override
    public ItemStack createCasterEffectProviderStack(ResourceLocation id) {
        return ItemCustomCasterEffectProvider.create(id);
    }
    
    @Override
    public String getCasterEffectProviderID(ItemStack stack) {
        return ItemCustomCasterEffectProvider.getProviderIDString(stack);
    }
    
    @Override
    public void syncImpetusTransaction(Collection<IImpetusNode> path) {
        DimensionalBlockPos[] positions = new DimensionalBlockPos[path.size()];
        Multimap<Integer, ChunkPos> chunks = MultimapBuilder.hashKeys().hashSetValues().build();
        int i = 0;
        for (IImpetusNode node : path) {
            DimensionalBlockPos newPos = new DimensionalBlockPos(node.getLocation().getPos().toImmutable(), node.getLocation().getDimension());
            positions[i] = newPos;
            ++i;
            chunks.put(newPos.getDimension(), new ChunkPos(newPos.getPos()));
        }
        
        PacketImpetusTransaction packet = new PacketImpetusTransaction(positions);
        for (int dim : chunks.keySet()) {
            WorldServer world = DimensionManager.getWorld(dim);
            if (world != null) {
                for (EntityPlayerMP player : world.getPlayers(EntityPlayerMP.class, Predicates.alwaysTrue())) {
                    for (ChunkPos pos : chunks.get(dim)) {
                        if (world.getPlayerChunkMap().isPlayerWatchingChunk(player, pos.x, pos.z)) {
                            TANetwork.INSTANCE.sendTo(packet, player);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void fullySyncImpetusNode(IImpetusNode node) {
        DimensionalBlockPos pos = node.getLocation();
        TANetwork.INSTANCE.sendToAllTracking(new PacketFullImpetusNodeSync(pos.getPos(), node.serializeNBT()),
                new TargetPoint(pos.getDimension(), pos.getPos().getX() + 0.5, pos.getPos().getY() + 0.5,
                pos.getPos().getZ() + 0.5, 64));
    }
    
    @Override
    public void updateImpetusNode(IImpetusNode node, DimensionalBlockPos connection, boolean output, boolean remove) {
        DimensionalBlockPos pos = node.getLocation();
        TANetwork.INSTANCE.sendToAllTracking(new PacketImpetusNodeUpdate(pos.getPos(), connection,
                output, remove), new TargetPoint(pos.getDimension(), pos.getPos().getX() + 0.5, pos.getPos().getY() + 0.5,
                pos.getPos().getZ() + 0.5, 64));
    }
    
}
