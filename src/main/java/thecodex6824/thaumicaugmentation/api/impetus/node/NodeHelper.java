/**
 * Thaumic Augmentation
 * Copyright (c) 2019 TheCodex6824.
 * <p>
 * This file is part of Thaumic Augmentation.
 * <p>
 * Thaumic Augmentation is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Thaumic Augmentation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Thaumic Augmentation.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumicaugmentation.api.impetus.node;

import java.util.*;
import java.util.function.BiConsumer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.common.DimensionManager;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.internal.TAInternals;
import thecodex6824.thaumicaugmentation.api.item.CapabilityImpetusLinker;
import thecodex6824.thaumicaugmentation.api.item.IImpetusLinker;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.api.util.RaytraceHelper;

public final class NodeHelper {

    private NodeHelper() {
    }

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
            } else if (!origin.isInvalid()) {
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
                            } else {
                                if (otherNode.getNumOutputs() >= otherNode.getMaxOutputs())
                                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_limit_out"), true);
                                else if (node.getNumInputs() >= node.getMaxInputs())
                                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_limit_in"), true);
                                else if (node.canConnectNodeAsInput(otherNode) && otherNode.canConnectNodeAsOutput(node)) {
                                    double dist = node.getLocation().getPos().distanceSq(otherNode.getLocation().getPos());
                                    double nodeMax = node.getMaxConnectDistance(otherNode);
                                    double otherNodeMax = otherNode.getMaxConnectDistance(node);
                                    if (dist > Math.min(nodeMax * nodeMax, otherNodeMax * otherNodeMax))
                                        player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_too_far"), true);
                                    else if (!nodesPassDefaultCollisionCheck(world, node, otherNode))
                                        player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_blocked"), true);
                                    else {
                                        node.addInput(otherNode);
                                        provider.markDirty();
                                        te.markDirty();
                                        syncAddedImpetusNodeInput(node, origin);
                                    }
                                }
                            }
                        }
                    }
                } else
                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_same_pos"), true);

                return true;
            }
        } else if (world.isRemote)
            return true;

        return false;
    }

    public static ConsumeResult consumeImpetusFromConnectedProviders(long amount, IImpetusConsumer dest, boolean simulate) {
        if (amount <= 0)
            return new ConsumeResult(0, Collections.emptyMap());

        ArrayList<IImpetusProvider> providers = new ArrayList<>(dest.getGraph().findDirectProviders(dest));
        if (!providers.isEmpty()) {
            providers.sort((p1, p2) -> Long.compare(p2.provide(Long.MAX_VALUE, true), p1.provide(Long.MAX_VALUE, true)));
            if (amount < providers.size()) {
                int remove = providers.size() - (int) amount;
                for (int i = 0; i < remove; ++i)
                    providers.remove(providers.size() - 1);
            }

            ArrayList<Deque<IImpetusNode>> paths = new ArrayList<>(providers.size());
            HashSet<IImpetusProvider> removedProviders = new HashSet<>();
            for (IImpetusProvider p : providers) {
                Deque<IImpetusNode> path = dest.getGraph().findPath(p, dest);
                if (path != null)
                    paths.add(path);
                else {
                    validateFullGraph(p.getGraph());
                    removedProviders.add(p);
                }
            }

            providers.removeAll(removedProviders);
            if (providers.size() > 0) {
                long drawn = 0;
                long step = amount / providers.size();
                long remain = amount % providers.size();
                HashMap<Deque<IImpetusNode>, Long> usedPaths = new HashMap<>();
                for (int i = 0; i < providers.size(); ++i) {
                    IImpetusProvider p = providers.get(i);
                    long actuallyDrawn = p.provide(Math.min(step + (remain > 0 ? 1 : 0), amount - drawn), true);
                    if (actuallyDrawn > 0) {
                        Deque<IImpetusNode> nodes = paths.get(i);
                        for (IImpetusNode n : nodes) {
                            actuallyDrawn = n.onTransaction(nodes, actuallyDrawn, simulate);
                            if (actuallyDrawn <= 0)
                                break;
                        }

                        if (actuallyDrawn > 0) {
                            actuallyDrawn = p.provide(actuallyDrawn, simulate);
                            usedPaths.put(nodes, actuallyDrawn);
                            drawn += actuallyDrawn;
                            if (actuallyDrawn < step && i < providers.size() - 1) {
                                step = (amount - drawn) / (providers.size() - (i + 1));
                                remain = (amount - drawn) % (providers.size() - (i + 1));
                            } else
                                --remain;
                        } else if (i < providers.size() - 1) {
                            step = (amount - drawn) / (providers.size() - (i + 1));
                            remain = (amount - drawn) % (providers.size() - (i + 1));
                        }
                    } else if (i < providers.size() - 1) {
                        step = (amount - drawn) / (providers.size() - (i + 1));
                        remain = (amount - drawn) % (providers.size() - (i + 1));
                    }
                }

                return new ConsumeResult(drawn, usedPaths);
            }
        }

        return new ConsumeResult(0, Collections.emptyMap());
    }

    public static boolean nodesPassDefaultCollisionCheck(World sharedWorld, IImpetusNode node1, IImpetusNode node2) {
        Vec3d start = node1.getBeamEndpoint();
        Vec3d target = node2.getBeamEndpoint();
        boolean clear = true;
        while (!start.equals(target)) {
            RayTraceResult r = sharedWorld.rayTraceBlocks(start, target, false, true, false);
            if (r == null || node2.getLocation().getPos().equals(r.getBlockPos()))
                break;
            else if (r.getBlockPos() != null && r.hitVec != null) {
                IBlockState state = sharedWorld.getBlockState(r.getBlockPos());
                if (!node1.getLocation().getPos().equals(r.getBlockPos()) &&
                        (state.isOpaqueCube() || state.getLightOpacity(sharedWorld, r.getBlockPos()) > 0)) {
                    clear = false;
                    break;
                } else {
                    double dX = Math.max(-1, Math.min(1, target.x - r.hitVec.x));
                    double dY = Math.max(-1, Math.min(1, target.y - r.hitVec.y));
                    double dZ = Math.max(-1, Math.min(1, target.z - r.hitVec.z));
                    start = start.add(dX, dY, dZ);
                }
            }
        }

        return clear;
    }

    public static void validateFullGraph(IImpetusGraph graph) {
        // check for nodes with invalid links
        // i.e. an input with no corresponding output, or the other way around
        HashSet<IImpetusNode> toRemove = new HashSet<>();
        for (IImpetusNode node : graph.getNodes()) {
            for (IImpetusNode input : node.getInputs()) {
                if (!input.hasOutput(node))
                    toRemove.add(input);
            }

            for (IImpetusNode n : toRemove)
                node.removeInput(n);

            toRemove.clear();
            for (IImpetusNode output : node.getOutputs()) {
                if (!output.hasInput(node))
                    toRemove.add(output);
            }

            for (IImpetusNode n : toRemove)
                node.removeOutput(n);

            toRemove.clear();
        }
    }

    public static void validateOutputs(World sharedWorld, IImpetusNode node) {
        HashSet<IImpetusNode> changed = new HashSet<>();

        //Check if unloaded nodes exist
        if (node.getOutputLocations().size() != node.getOutputs().size()) {
            List<DimensionalBlockPos> invalidDimPos = new ArrayList<>();

            //Tries to reconnect unloaded nodes
            for (DimensionalBlockPos pos : node.getOutputLocations()) {
                World world = DimensionManager.getWorld(pos.getDimension());
                if (world.provider.getDimension() == pos.getDimension() && world.isBlockLoaded(pos.getPos())) {
                    TileEntity te = world.getTileEntity(pos.getPos());
                    if (te != null) {
                        IImpetusNode possible = te.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                        if (possible != null) {
                            node.addOutput(possible);
                            continue;
                        }
                    }
                    //remove pos if tile is null or tile dos not have IMPETUS_NODE capability
                    invalidDimPos.add(pos);
                }
            }

            invalidDimPos.forEach(node.getOutputLocations()::remove);
        }

        for (IImpetusNode output : node.getOutputs()) {
            if (sharedWorld.provider.getDimension() == node.getLocation().getDimension() &&
                    sharedWorld.provider.getDimension() == output.getLocation().getDimension()) {

                boolean enforce1 = node.shouldEnforceBeamLimitsWith(output);
                boolean enforce2 = output.shouldEnforceBeamLimitsWith(node);
                if (enforce1 || enforce2) {
                    double dist = node.getLocation().getPos().distanceSq(output.getLocation().getPos());
                    if ((enforce1 && dist > node.getMaxConnectDistance(output) * node.getMaxConnectDistance(output)) ||
                            (enforce2 && dist > output.getMaxConnectDistance(node) * output.getMaxConnectDistance(node)) ||
                            !nodesPassDefaultCollisionCheck(sharedWorld, node, output)) {

                        node.removeOutput(output);
                        changed.add(node);
                        changed.add(output);
                        syncRemovedImpetusNodeOutput(node, output.getLocation());
                        syncRemovedImpetusNodeInput(output, node.getLocation());
                    }
                }
            }
        }

        for (
                IImpetusNode n : changed) {
            if (n.getLocation().getDimension() == sharedWorld.provider.getDimension()) {
                TileEntity tile = sharedWorld.getTileEntity(n.getLocation().getPos());
                if (tile != null)
                    tile.markDirty();
            }
        }

    }

    public static void damageEntitiesFromTransaction(Deque<IImpetusNode> path, long energy) {
        damageEntitiesFromTransaction(path, (node, entity) -> ImpetusAPI.causeImpetusDamage(node.getLocation().getDimension() == entity.dimension ?
                new Vec3d(node.getLocation().getPos()) : null, entity, Math.max(energy / 10.0F, 1.0F)));
    }

    public static void damageEntitiesFromTransaction
            (Deque<IImpetusNode> path, BiConsumer<IImpetusNode, Entity> damageFunc) {
        if (path.size() >= 2) {
            Iterator<IImpetusNode> iterator = path.iterator();
            IImpetusNode first = iterator.next();
            while (iterator.hasNext()) {
                IImpetusNode second = iterator.next();
                if (first.getLocation().getDimension() == second.getLocation().getDimension() && first.shouldPhysicalBeamLinkTo(second) &&
                        second.shouldPhysicalBeamLinkTo(first)) {
                    World world = DimensionManager.getWorld(first.getLocation().getDimension());
                    if (world != null) {
                        for (Entity e : RaytraceHelper.raytraceEntities(world, first.getBeamEndpoint(), second.getBeamEndpoint()))
                            damageFunc.accept(first, e);
                    }
                }

                first = second;
            }
        }
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

    public static void syncDestroyedImpetusNode(IImpetusNode node) {
        for (IImpetusNode n : node.getInputs())
            NodeHelper.syncRemovedImpetusNodeOutput(n, node.getLocation());

        for (IImpetusNode n : node.getOutputs())
            NodeHelper.syncRemovedImpetusNodeInput(n, node.getLocation());
    }

}
