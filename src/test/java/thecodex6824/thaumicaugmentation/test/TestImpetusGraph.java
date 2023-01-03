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

package thecodex6824.thaumicaugmentation.test;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import org.junit.Test;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusGraph;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusProvider;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.BufferedImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.BufferedImpetusProvider;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.ImpetusNode;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

import java.util.Deque;
import java.util.Set;

import static org.junit.Assert.*;

public class TestImpetusGraph {

    @Test
    public void testDimensionalBlockPos() {
        DimensionalBlockPos first = new DimensionalBlockPos(0, 0, 0, 0);
        DimensionalBlockPos second = new DimensionalBlockPos(0, 0, 0, 0);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        
        first = new DimensionalBlockPos(0, 0, 0, 1);
        assertNotEquals(first, second);
        
        first = new DimensionalBlockPos(0, 1, 0, 0);
        assertNotEquals(first, second);
        
        first = DimensionalBlockPos.INVALID;
        assertNotEquals(first, second);
        
        second = DimensionalBlockPos.INVALID;
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
    
    @Test
    public void testInvalidNodeLocation() {
        // nodes without a location should not be in a graph
        ImpetusNode node = new ImpetusNode(2, 2);
        assertTrue(node.getGraph().isEmpty());
        
        node.setLocation(new DimensionalBlockPos(0, 0, 0, 0));
        assertFalse(node.getGraph().isEmpty());
    }
    
    @Test
    public void testRemove() {
        ImpetusNode node = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 0, 0, 0));
        node.getGraph().removeNode(node);
        assertTrue(node.getGraph().isEmpty());
    }
    
    @Test
    public void testConnection() {
        ImpetusNode node1 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 0, 0, 0));
        ImpetusNode node2 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 0, 3, 0));
        ImpetusNode node3 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 0, 2, 0));
    
        IImpetusGraph old = node1.getGraph();
        node3.addInput(node2);
        node2.addInput(node1);
        
        assertTrue(old.isEmpty());
        assertEquals(3, node1.getGraph().size());
    }
    
    @Test
    public void testFindProviders() {
        BufferedImpetusProvider p = new BufferedImpetusProvider(2, 2, new DimensionalBlockPos(0, 0, 0, 0), new ImpetusStorage(1000));
        ImpetusNode n1 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 0, 1, 0));
        ImpetusNode n2 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 1, 1, 0));
        ImpetusNode n3 = new ImpetusNode(2, 2, new DimensionalBlockPos(1, 0, 1, 0));
        
        n1.addInput(p);
        n2.addInput(n1);
        n2.addInput(n3);
        
        Set<IImpetusProvider> providers = n1.getGraph().findDirectProviders(n2);
        assertEquals(1, providers.size());
    }
    
    @Test
    public void testPath() {
        ImpetusNode node1 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 0, 0, 0));
        ImpetusNode node2 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 0, 3, 0));
        ImpetusNode node3 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 5, -1, 0));
        ImpetusNode node4 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 15, -6, 0));
        ImpetusNode node5 = new ImpetusNode(2, 2, new DimensionalBlockPos(4, 15, -6, 0));
        
        node1.addOutput(node2);
        node2.addOutput(node3);
        node2.addOutput(node1);
        node3.addOutput(node4);
        node4.addOutput(node3);
        node5.addOutput(node4);
        node1.addOutput(node5);
        
        Deque<IImpetusNode> nodes = node1.getGraph().findPath(node1, node3);
        assertNotNull(nodes);
        assertEquals(3, nodes.size());
        assertEquals(nodes.pop(), node1);
        assertEquals(nodes.pop(), node2);
        assertEquals(nodes.pop(), node3);
    }
    
    @Test
    public void testEnergyTransfer() {
        BufferedImpetusConsumer consumer = new BufferedImpetusConsumer(5, 5, new DimensionalBlockPos(0, 0, 0, 0), new ImpetusStorage(1000, 50));
        BufferedImpetusProvider provider1 = new BufferedImpetusProvider(5, 5, new DimensionalBlockPos(0, 1, 0, 0), new ImpetusStorage(1000, 1000, 20));
        BufferedImpetusProvider provider2 = new BufferedImpetusProvider(5, 5, new DimensionalBlockPos(0, 1, 1, 0), new ImpetusStorage(1000, 1000, 20));
        ImpetusNode node = new ImpetusNode(2, 2, new DimensionalBlockPos(1, 1, 1, 0));
        
        node.addInput(provider1);
        node.addInput(provider2);
        consumer.addInput(node);
        
        provider1.getProvider().receiveEnergy(Long.MAX_VALUE, false);
        provider2.getProvider().receiveEnergy(Long.MAX_VALUE, false);
        
        consumer.consume(Long.MAX_VALUE, false);
        assertEquals(40, consumer.getConsumer().getEnergyStored());
    }
    
    @Test
    public void testSmallTransfer() {
        BufferedImpetusConsumer consumer = new BufferedImpetusConsumer(5, 5, new DimensionalBlockPos(0, 0, 0, 0), new ImpetusStorage(1000, 50));
        BufferedImpetusProvider provider1 = new BufferedImpetusProvider(5, 5, new DimensionalBlockPos(0, 1, 0, 0), new ImpetusStorage(1000, 1000, 20));
        BufferedImpetusProvider provider2 = new BufferedImpetusProvider(5, 5, new DimensionalBlockPos(0, 1, 1, 0), new ImpetusStorage(1000, 1000, 20));
        BufferedImpetusProvider provider3 = new BufferedImpetusProvider(5, 5, new DimensionalBlockPos(1, 1, 1, 0), new ImpetusStorage(1000, 1000, 20));
        BufferedImpetusProvider provider4 = new BufferedImpetusProvider(5, 5, new DimensionalBlockPos(2, 3, 1, 0), new ImpetusStorage(1000, 1000, 20));
        BufferedImpetusProvider provider5 = new BufferedImpetusProvider(5, 5, new DimensionalBlockPos(3, 1, 2, 0), new ImpetusStorage(1000, 1000, 20));
    
        consumer.addInput(provider1);
        consumer.addInput(provider2);
        consumer.addInput(provider3);
        consumer.addInput(provider4);
        consumer.addInput(provider5);
        
        provider1.getProvider().receiveEnergy(Long.MAX_VALUE, false);
        provider2.getProvider().receiveEnergy(Long.MAX_VALUE, false);
        provider3.getProvider().receiveEnergy(Long.MAX_VALUE, false);
        provider4.getProvider().receiveEnergy(Long.MAX_VALUE, false);
        provider5.getProvider().receiveEnergy(Long.MAX_VALUE, false);
        
        consumer.consume(1, false);
        assertEquals(1, consumer.getConsumer().getEnergyStored());
        
        int taken = 0;
        if (provider1.getProvider().getEnergyStored() != provider1.getProvider().getMaxEnergyStored())
            ++taken;
        if (provider2.getProvider().getEnergyStored() != provider2.getProvider().getMaxEnergyStored())
            ++taken;
        if (provider3.getProvider().getEnergyStored() != provider3.getProvider().getMaxEnergyStored())
            ++taken;
        if (provider4.getProvider().getEnergyStored() != provider3.getProvider().getMaxEnergyStored())
            ++taken;
        if (provider5.getProvider().getEnergyStored() != provider3.getProvider().getMaxEnergyStored())
            ++taken;
        
        assertEquals(taken, 1);
    }
    
    @Test
    public void testSaving() {
        ImpetusNode node1 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 0, 0, 0));
        ImpetusNode node2 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 0, 3, 0));
        node2.addInput(node1);
        
        NBTTagCompound tag1 = node1.serializeNBT();
        NBTTagCompound tag2 = node2.serializeNBT();
        
        NBTTagList correct1 = new NBTTagList();
        correct1.appendTag(new NBTTagIntArray(node2.getLocation().toArray()));
        
        NBTTagList correct2 = new NBTTagList();
        correct2.appendTag(new NBTTagIntArray(node1.getLocation().toArray()));
        
        assertEquals(tag1.getTagList("outputs", NBT.TAG_INT_ARRAY), correct1);
        assertEquals(tag2.getTagList("inputs", NBT.TAG_INT_ARRAY), correct2);
        
        // deserializing in testing is currently problematic because of the coupling with World
        // the most we can do is just make sure that the saved connections made it into the connection sets
        node2 = new ImpetusNode(2, 2, new DimensionalBlockPos(0, 0, 3, 0));
        node2.deserializeNBT(tag2);
        assertTrue(node2.getInputLocations().contains(node1.getLocation()));
    }
    
}
