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

package thecodex6824.thaumicaugmentation.api.impetus.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import thecodex6824.thaumicaugmentation.api.internal.TAInternals;
import thecodex6824.thaumicaugmentation.api.item.CapabilityImpetusLinker;
import thecodex6824.thaumicaugmentation.api.item.IImpetusLinker;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public final class NodeHelper {

    private NodeHelper() {}
    
    @SuppressWarnings("null")
    public static boolean handleLinkInteract(TileEntity provider, World world, ItemStack stack, EntityPlayer player, BlockPos pos, 
            EnumFacing face, EnumHand hand) {
        
        IImpetusLinker linker = stack.getCapability(CapabilityImpetusLinker.IMPETUS_LINKER, null);
        if (!world.isRemote && linker != null) {
            DimensionalBlockPos origin = linker.getOrigin();
            if (player.isSneaking()) {
                if (!origin.isInvalid() && origin.getPos().getX() == pos.getX() && origin.getPos().getY() == pos.getY() &&
                            origin.getPos().getZ() == pos.getZ() && origin.getDimension() == world.provider.getDimension()) {
                        
                    linker.setOrigin(DimensionalBlockPos.INVALID);
                    return true;
                }
                
                linker.setOrigin(new DimensionalBlockPos(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension()));
                return true;
            }
            else if (!origin.isInvalid()) {
                IImpetusNode node = provider.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                if (!origin.equals(node.getLocation()) && world.isBlockLoaded(origin.getPos())) {
                    TileEntity te = world.getChunk(origin.getPos()).getTileEntity(origin.getPos(), EnumCreateEntityType.CHECK);
                    if (te != null) {
                        IImpetusNode otherNode = te.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                        if (otherNode != null) {
                            if (node.getInputLocations().contains(origin)) {
                                if (node.canRemoveNodeAsInput(otherNode) && otherNode.canRemoveNodeAsOutput(node)) {
                                    node.removeInput(otherNode);
                                    provider.markDirty();
                                    te.markDirty();
                                    syncRemovedImpetusNodeInput(node, origin);
                                }
                            }
                            else {
                                if (otherNode.getNumOutputs() >= otherNode.getMaxOutputs())
                                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_limit_out"), true);
                                else if (node.getNumInputs() >= node.getMaxInputs())
                                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_limit_in"), true);
                                else if (node.canConnectNodeAsInput(otherNode) && otherNode.canConnectNodeAsOutput(node)) {
                                    node.addInput(otherNode);
                                    provider.markDirty();
                                    te.markDirty();
                                    syncAddedImpetusNodeInput(node, origin);
                                }
                            }
                        }
                    }
                }
                else
                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_same_pos"), true);
                    
                return true;
            }
        }
        else if (world.isRemote)
            return true;
        
        return false;
    }
    
    public static ConsumeResult consumeImpetusFromConnectedProviders(long amount, IImpetusConsumer dest, boolean simulate) {
        if (amount <= 0)
            return new ConsumeResult(0, Collections.emptyList());
        
        ArrayList<IImpetusProvider> providers = new ArrayList<>(dest.getGraph().findDirectProviders(dest));
        if (!providers.isEmpty()) {
            providers.sort((p1, p2) -> Long.compare(p2.provide(Long.MAX_VALUE, true), p1.provide(Long.MAX_VALUE, true)));
            if (amount < providers.size()) {
                int remove = providers.size() - (int) amount;
                for (int i = 0; i < remove; ++i)
                    providers.remove(providers.size() - 1 - i);
            }
            
            ArrayList<Deque<IImpetusNode>> paths = new ArrayList<>(providers.size());
            for (IImpetusProvider p : providers) {
                Deque<IImpetusNode> path = dest.getGraph().findPath(p, dest);
                if (path != null)
                    paths.add(path);
            }
            
            long drawn = 0;
            long step = amount / providers.size();
            long remain = amount % providers.size();
            ArrayList<Deque<IImpetusNode>> usedPaths = new ArrayList<>();
            for (int i = 0; i < providers.size(); ++i) {
                IImpetusProvider p = providers.get(i);
                long actuallyDrawn = p.provide(Math.min(step + (remain > 0 ? 1 : 0), amount - drawn), simulate);
                drawn += actuallyDrawn;
                if (actuallyDrawn < step && i < providers.size() - 1) {
                    step = (amount - drawn) / (providers.size() - (i + 1));
                    remain = (amount - drawn) % (providers.size() - (i + 1));
                }
                else
                    --remain;
                
                if (actuallyDrawn > 0) {
                    Deque<IImpetusNode> nodes = paths.get(i);
                    for (IImpetusNode n : nodes)
                        n.onTransaction(dest, nodes, actuallyDrawn, simulate);
                    
                    usedPaths.add(nodes);
                }
            }
            
            return new ConsumeResult(drawn, usedPaths);
        }
        
        return new ConsumeResult(0, Collections.emptyList());
    }
    
    public static void syncImpetusTransaction(Deque<IImpetusNode> path) {
        TAInternals.syncImpetusTransaction(path);
    }
    
    public static void syncAllImpetusTransactions(Collection<Deque<IImpetusNode>> paths) {
        for (Deque<IImpetusNode> path : paths)
            TAInternals.syncImpetusTransaction(path);
    }
    
    public static void syncImpetusNodeFully(IImpetusNode node) {
        TAInternals.fullySyncImpetusNode(node);
    }
    
    public static void syncAddedImpetusNodeInput(IImpetusNode node, DimensionalBlockPos input) {
        TAInternals.updateImpetusNode(node, input, false, false);
    }
    
    public static void syncAddedImpetusNodeOutput(IImpetusNode node, DimensionalBlockPos output) {
        TAInternals.updateImpetusNode(node, output, true, false);
    }
    
    public static void syncRemovedImpetusNodeInput(IImpetusNode node, DimensionalBlockPos input) {
        TAInternals.updateImpetusNode(node, input, false, true);
    }
    
    public static void syncRemovedImpetusNodeOutput(IImpetusNode node, DimensionalBlockPos output) {
        TAInternals.updateImpetusNode(node, output, true, true);
    }
    
}
