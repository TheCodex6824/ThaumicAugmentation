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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.common.util.Constants.NBT;
import thecodex6824.thaumicaugmentation.api.internal.TAInternals;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public final class NodeHelper {

    private NodeHelper() {}
    
    @SuppressWarnings("null")
    public static boolean handleCasterInteract(TileEntity provider, World world, ItemStack stack, EntityPlayer player, BlockPos pos, 
            EnumFacing face, EnumHand hand) {
        
        if (!world.isRemote) {
            if (!stack.hasTagCompound())
                stack.setTagCompound(new NBTTagCompound());
            
            if (player.isSneaking()) {
                if (stack.getTagCompound().hasKey("impetusBindSelection", NBT.TAG_INT_ARRAY)) {
                    int[] data = stack.getTagCompound().getIntArray("impetusBindSelection");
                    if (data.length == 4 && data[0] == pos.getX() && data[1] == pos.getY() && data[2] == pos.getZ() &&
                            data[3] == world.provider.getDimension()) {
                        
                        stack.getTagCompound().removeTag("impetusBindSelection");
                        return true;
                    }
                }
                
                stack.getTagCompound().setIntArray("impetusBindSelection", new int[] {pos.getX(), pos.getY(), pos.getZ(),
                        world.provider.getDimension()});
                return true;
            }
            else if (stack.getTagCompound().hasKey("impetusBindSelection", NBT.TAG_INT_ARRAY)) {
                DimensionalBlockPos first = new DimensionalBlockPos(stack.getTagCompound().getIntArray("impetusBindSelection"));
                IImpetusNode node = provider.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                if (!first.equals(node.getLocation()) && world.isBlockLoaded(first.getPos())) {
                    TileEntity te = world.getChunk(first.getPos()).getTileEntity(first.getPos(), EnumCreateEntityType.CHECK);
                    if (te != null) {
                        IImpetusNode otherNode = te.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                        if (otherNode != null) {
                            if (node.getInputLocations().contains(first)) {
                                node.removeInput(te.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null));
                                provider.markDirty();
                                te.markDirty();
                                syncRemovedImpetusNodeInput(node, first);
                            }
                            else {
                                if (otherNode.getNumOutputs() >= otherNode.getMaxOutputs())
                                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_limit_out"), true);
                                else if (node.getNumInputs() >= node.getMaxInputs())
                                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_limit_in"), true);
                                else {
                                    node.addInput(te.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null));
                                    provider.markDirty();
                                    te.markDirty();
                                    syncAddedImpetusNodeInput(node, first);
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
        
        return false;
    }
    
    public static ConsumeResult consumeImpetusFromConnectedProviders(long amount, IImpetusConsumer dest) {
        if (amount <= 0)
            return new ConsumeResult(0, Collections.emptyList());
        
        ArrayList<IImpetusProvider> providers = new ArrayList<>(dest.getGraph().findDirectProviders(dest));
        if (!providers.isEmpty()) {
            providers.sort((p1, p2) -> (int) Math.max(1, Math.min(-1, p1.provide(Long.MAX_VALUE, true) - p2.provide(Long.MAX_VALUE, true))));
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
                long actuallyDrawn = p.provide(Math.min(step + (remain > 0 ? 1 : 0), amount - drawn), false);
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
                        n.onTransaction(dest, actuallyDrawn);
                    
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
