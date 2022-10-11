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

package thecodex6824.thaumicaugmentation.api.impetus.node.prefab;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.INBTSerializable;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusGraph;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public class ImpetusNode implements IImpetusNode, INBTSerializable<NBTTagCompound> {

    protected IImpetusGraph graph;
    protected int maxInputs;
    protected int maxOutputs;
    protected DimensionalBlockPos loc;
    protected Set<DimensionalBlockPos> inputs;
    protected Set<DimensionalBlockPos> outputs;

    private boolean hasUnloadedNodes;

    public ImpetusNode(int totalInputs, int totalOutputs) {
        this(totalInputs, totalOutputs, DimensionalBlockPos.INVALID);
    }

    public ImpetusNode(int totalInputs, int totalOutputs, DimensionalBlockPos location) {
        maxInputs = totalInputs;
        maxOutputs = totalOutputs;
        loc = location;
        inputs = new HashSet<>();
        outputs = new HashSet<>();
        graph = new ImpetusGraph();
        if (!loc.isInvalid())
            graph.addNode(this);
    }

    @Override
    public IImpetusGraph getGraph() {
        return graph;
    }

    @Override
    public void setGraph(IImpetusGraph newGraph) {
        graph = newGraph;
    }

    @Override
    public int getNumInputs() {
        return inputs.size();
    }

    @Override
    public int getNumOutputs() {
        return outputs.size();
    }

    @Override
    public int getMaxInputs() {
        return maxInputs;
    }

    @Override
    public int getMaxOutputs() {
        return maxOutputs;
    }

    @Override
    public boolean hasInput(IImpetusNode in) {
        return inputs.contains(in.getLocation());
    }

    @Override
    public boolean hasOutput(IImpetusNode out) {
        return outputs.contains(out.getLocation());
    }

    @Override
    public boolean addInput(IImpetusNode input) {
        boolean newToUs = addInputLocation(input.getLocation());
        boolean newToThem = input.addOutputLocation(loc);
        boolean result = graph.addNode(input);
        if (newToUs || result)
            onConnected(input);
        if (newToThem || result)
            input.onConnected(this);

        return result;
    }

    @Override
    public boolean addOutput(IImpetusNode output) {
        boolean newToUs = addOutputLocation(output.getLocation());
        boolean newToThem = output.addInputLocation(loc);
        boolean result = graph.addNode(output);
        if (newToUs || result)
            onConnected(output);
        if (newToThem || result)
            output.onConnected(this);

        return result;
    }

    @Override
    public boolean removeInput(IImpetusNode input) {
        boolean removedUs = input.removeOutputLocation(loc);
        boolean removedThem = inputs.remove(input.getLocation());
        if (removedUs)
            onDisconnected(input);
        if (removedThem)
            input.onDisconnected(this);

        return removedThem;
    }

    @Override
    public boolean removeOutput(IImpetusNode output) {
        boolean removedUs = output.removeInputLocation(loc);
        boolean removedThem = outputs.remove(output.getLocation());
        if (removedUs)
            onDisconnected(output);
        if (removedThem)
            output.onDisconnected(this);

        return removedThem;
    }

    @Override
    public boolean canConnectNodeAsInput(IImpetusNode toConnect) {
        return true;
    }

    @Override
    public boolean canConnectNodeAsOutput(IImpetusNode toConnect) {
        return true;
    }

    @Override
    public boolean canRemoveNodeAsInput(IImpetusNode toRemove) {
        return true;
    }

    @Override
    public boolean canRemoveNodeAsOutput(IImpetusNode toRemove) {
        return true;
    }

    @Override
    public double getMaxConnectDistance(IImpetusNode toConnect) {
        return 8.0;
    }

    @Override
    public boolean addInputLocation(DimensionalBlockPos toConnect) {
        if (inputs.size() == maxInputs && !inputs.contains(toConnect))
            throw new IndexOutOfBoundsException("Exceeded maximum amount of inputs for node (" + inputs.size() + ")");

        return inputs.add(toConnect);
    }

    @Override
    public boolean addOutputLocation(DimensionalBlockPos toConnect) {
        if (outputs.size() == maxOutputs && !outputs.contains(toConnect))
            throw new IndexOutOfBoundsException("Exceeded maximum amount of outputs for node (" + outputs.size() + ")");

        return outputs.add(toConnect);
    }

    @Override
    public boolean removeInputLocation(DimensionalBlockPos toRemove) {
        return inputs.remove(toRemove);
    }

    @Override
    public boolean removeOutputLocation(DimensionalBlockPos toRemove) {
        return outputs.remove(toRemove);
    }

    @Override
    public Set<DimensionalBlockPos> getInputLocations() {
        return inputs;
    }

    @Override
    public Set<DimensionalBlockPos> getOutputLocations() {
        return outputs;
    }

    @Override
    public Set<IImpetusNode> getInputs() {
        return inputs.stream().map(loc -> graph.findNodeByPosition(loc)).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public Set<IImpetusNode> getOutputs() {
        return outputs.stream().map(loc -> graph.findNodeByPosition(loc)).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public DimensionalBlockPos getLocation() {
        return loc;
    }

    @Override
    public void setLocation(DimensionalBlockPos location) {
        if (!loc.isInvalid())
            graph.removeNode(this);

        loc = location;
        graph.addNode(this);
    }

    @Override
    public Vec3d getBeamEndpoint() {
        return new Vec3d(loc.getPos().getX() + 0.5, loc.getPos().getY() + 0.5, loc.getPos().getZ() + 0.5);
    }

    @Override
    public boolean shouldPhysicalBeamLinkTo(IImpetusNode other) {
        return true;
    }

    @Override
    public boolean shouldEnforceBeamLimitsWith(IImpetusNode other) {
        return true;
    }

    @Override
    public void unload() {
        graph.removeNode(this);
    }

    @Override
    public void destroy() {
        for (IImpetusNode node : graph.getInputs(this))
            removeInput(node);

        for (IImpetusNode node : graph.getOutputs(this))
            removeOutput(node);

        unload();
    }

    @Override
    public boolean hasUnloadedNodes(World world) {
        if (!hasUnloadedNodes) {
            return false;
        }

        List<DimensionalBlockPos> invalid = new ArrayList<>();

        List<Boolean> checks = new ArrayList<>();
        for (DimensionalBlockPos pos : inputs) {
            checks.add(validateNodeInput(world, pos, invalid));
        }
        removeInvalidPos(invalid, inputs);

        for (DimensionalBlockPos pos : outputs) {
            checks.add(validateNodeOutput(world, pos, invalid));
        }
        removeInvalidPos(invalid, outputs);

        if (!checks.contains(false)) {
            hasUnloadedNodes = false;
            return false;
        }
        return true;
    }

    private boolean validateNodeInput(World world, DimensionalBlockPos pos, List<DimensionalBlockPos> invalid) {
        if (world.provider.getDimension() == pos.getDimension()) {
            TileEntity te = world.getTileEntity(pos.getPos());
            if (te != null) {
                IImpetusNode possible = te.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                if (possible != null) {
                    if (world.isBlockLoaded(pos.getPos())) {
                        addInput(possible);
                        return true;
                    }
                    hasUnloadedNodes = true;
                    return false;
                }
            }
        }
        //remove pos if tile is null or tile dos not have IMPETUS_NODE capability
        invalid.add(pos);
        return false;
    }

    private boolean validateNodeOutput(World world, DimensionalBlockPos pos, List<DimensionalBlockPos> invalid) {
        if (world.provider.getDimension() == pos.getDimension()) {
            TileEntity te = world.getTileEntity(pos.getPos());
            if (te != null) {
                IImpetusNode possible = te.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                if (possible != null) {
                    if (world.isBlockLoaded(pos.getPos())) {
                        addOutput(possible);
                        return true;
                    }
                    hasUnloadedNodes = true;
                    return false;
                }
            }
        }
        //remove pos if tile is null or tile dos not have IMPETUS_NODE capability
        invalid.add(pos);
        return false;
    }

    private void removeInvalidPos(List<DimensionalBlockPos> invalid, Set<DimensionalBlockPos> inout) {
        if (!invalid.isEmpty()) {
            invalid.forEach(inout::remove);
            invalid.clear();
        }
    }

    protected void initServer() {
        List<DimensionalBlockPos> invalid = new ArrayList<>();

        for (DimensionalBlockPos pos : inputs) {
            World world = DimensionManager.getWorld(pos.getDimension());
            validateNodeInput(world, pos, invalid);
        }
        removeInvalidPos(invalid, inputs);

        for (DimensionalBlockPos pos : outputs) {
            World world = DimensionManager.getWorld(pos.getDimension());
            validateNodeOutput(world, pos, invalid);
        }
        removeInvalidPos(invalid, outputs);
    }

    protected void initClient(World world) {
        List<DimensionalBlockPos> invalid = new ArrayList<>();

        for (DimensionalBlockPos pos : inputs) {
            validateNodeInput(world, pos, invalid);
        }
        removeInvalidPos(invalid, inputs);

        for (DimensionalBlockPos pos : outputs) {
            validateNodeOutput(world, pos, invalid);
        }
        removeInvalidPos(invalid, outputs);
    }

    @Override
    public void init(World world) {
        if (!world.isRemote)
            initServer();
        else
            initClient(world);
    }

    @Override
    public NBTTagCompound getSyncNBT() {
        return serializeNBT();
    }

    @Override
    public void readSyncNBT(NBTTagCompound tag) {
        deserializeNBT(tag);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("inputs", NBT.TAG_INT_ARRAY);
        for (int i = 0; i < list.tagCount(); ++i)
            inputs.add(new DimensionalBlockPos(list.getIntArrayAt(i)));

        list = nbt.getTagList("outputs", NBT.TAG_INT_ARRAY);
        for (int i = 0; i < list.tagCount(); ++i)
            outputs.add(new DimensionalBlockPos(list.getIntArrayAt(i)));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList inputs = new NBTTagList();
        for (DimensionalBlockPos pos : this.inputs)
            inputs.appendTag(new NBTTagIntArray(pos.toArray()));

        NBTTagList outputs = new NBTTagList();
        for (DimensionalBlockPos pos : this.outputs)
            outputs.appendTag(new NBTTagIntArray(pos.toArray()));

        tag.setTag("inputs", inputs);
        tag.setTag("outputs", outputs);
        return tag;
    }

}
