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

package thecodex6824.thaumicaugmentation.api.impetus.node.prefab;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusGraph;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusProvider;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public class ImpetusGraph implements IImpetusGraph {

    protected Map<DimensionalBlockPos, IImpetusNode> nodes;
    
    public ImpetusGraph() {
        nodes = new HashMap<>();
    }
    
    @Override
    public boolean addNode(IImpetusNode node) {
        IImpetusGraph graph = node.getGraph();
        if (graph != null && graph != this) {
            addAndMergeGraph(node);
            return true;
        }
        else
            return nodes.put(node.getLocation(), node) == null;
    }
    
    @Override
    public boolean removeNode(IImpetusNode node) {
        if (nodes.containsKey(node.getLocation())) {
            removeAndSplitGraph(node);
            return true;
        }
        else
            return false;
    }
    
    @Override
    public int size() {
        return nodes.size();
    }
    
    @Override
    public Set<IImpetusNode> getNodes() {
        return ImmutableSet.copyOf(nodes.values());
    }
    
    @Override
    public Set<IImpetusNode> getInputs(IImpetusNode node) {
        return node.getInputs();
    }
    
    @Override
    public Set<IImpetusNode> getOutputs(IImpetusNode node) {
        return node.getOutputs();
    }
    
    @Override
    public boolean addInput(IImpetusNode node, IImpetusNode input) {
        return node.addInput(input);
    }
    
    @Override
    public boolean addOutput(IImpetusNode node, IImpetusNode output) {
        return node.addOutput(output);
    }
    
    @Override
    public boolean removeInput(IImpetusNode node, IImpetusNode input) {
        return node.removeInput(input);
    }
    
    @Override
    public boolean removeOutput(IImpetusNode node, IImpetusNode output) {
        return node.removeOutput(output);
    }
    
    @Override
    public @Nullable IImpetusNode findNodeByPosition(DimensionalBlockPos pos) {
        return nodes.get(pos);
    }
    
    @Override
    public @Nullable Deque<IImpetusNode> findPath(IImpetusNode start, IImpetusNode end) {
        if (start == end) {
            ArrayDeque<IImpetusNode> ret = new ArrayDeque<>(1);
            ret.add(start);
            return ret;
        }
        else {
            Set<IImpetusNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            ArrayDeque<ArrayDeque<IImpetusNode>> toCheck = new ArrayDeque<>();
            ArrayDeque<IImpetusNode> sPath = new ArrayDeque<>();
            sPath.add(start);
            toCheck.add(sPath);
            visited.add(start);
            
            while (!toCheck.isEmpty()) {
                 ArrayDeque<IImpetusNode> currentPath = toCheck.poll();
                 for (IImpetusNode n : currentPath.getLast().getOutputs()) {
                     if (!visited.contains(n)) {
                         if (n == end) {
                             currentPath.add(n);
                             return currentPath;
                         }
                         else {
                             ArrayDeque<IImpetusNode> copy = new ArrayDeque<>(currentPath);
                             copy.add(n);
                             toCheck.add(copy);
                             visited.add(n);
                         }
                     }
                 }
            }
            
            return null;
        }
    }
    
    @Override
    public Set<IImpetusProvider> findDirectProviders(IImpetusNode node) {
        Set<IImpetusProvider> providers = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<IImpetusNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        ArrayDeque<IImpetusNode> toCheck = new ArrayDeque<>();
        toCheck.add(node);
        while (!toCheck.isEmpty()) {
            IImpetusNode check = toCheck.pop();
            if (!visited.contains(check)) {
                visited.add(check);
                if (check != node && check instanceof IImpetusProvider)
                    providers.add((IImpetusProvider) check);
                else
                    toCheck.addAll(check.getInputs());
            }
        }
        
        return providers;
    }
    
    protected void addAndMergeGraph(IImpetusNode adding) {
        if (nodes.size() > adding.getGraph().size()) {
            IImpetusGraph otherGraph = adding.getGraph();
            for (IImpetusNode node : otherGraph.getNodes()) {
                otherGraph.removeNode(node);
                node.setGraph(this);
                nodes.put(node.getLocation(), node);
            }
        }
        else {
            IImpetusGraph otherGraph = adding.getGraph();
            for (IImpetusNode node : nodes.values()) {
                node.setGraph(otherGraph);
                otherGraph.addNode(node);
            }
            
            nodes.clear();
        }
    }
    
    protected void removeAndSplitGraph(IImpetusNode splitAt) {
        Set<IImpetusNode> notify = Collections.newSetFromMap(new IdentityHashMap<>());
        notify.addAll(splitAt.getInputs());
        notify.addAll(splitAt.getOutputs());
        for (IImpetusNode other : notify)
            other.onDisconnected(splitAt);
        
        nodes.remove(splitAt.getLocation());
        if (!nodes.isEmpty()) {
            Map<IImpetusNode, Integer> tags = new IdentityHashMap<>();
            int tag = 0;
            while (tags.size() < nodes.size()) {
                ArrayDeque<IImpetusNode> toCheck = new ArrayDeque<>();
                toCheck.add(nodes.values().stream().filter(node -> !tags.containsKey(node)).findFirst().get());
                while (!toCheck.isEmpty()) {
                    IImpetusNode node = toCheck.pop();
                    if (!tags.containsKey(node)) {
                        tags.put(node, tag);
                        toCheck.addAll(node.getInputs());
                        toCheck.addAll(node.getOutputs());
                    }
                }
                
                ++tag;
            }
            
            if (tag > 0) {
                for (int i = 0; i < tag; ++i) {
                    final int currentTag = i;
                    ImpetusGraph newGraph = new ImpetusGraph();
                    tags.entrySet().removeIf(entry -> {
                        if (entry.getValue() == currentTag) {
                            newGraph.addNode(entry.getKey());
                            return true;
                        }
                        else
                            return false;
                    });
                }
            }
        }
    }
    
}
