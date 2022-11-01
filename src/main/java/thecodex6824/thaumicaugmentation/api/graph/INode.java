/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.api.graph;

import java.util.Set;

public interface INode<Graph extends IGraph<Self>, Self> {

    Graph getGraph();
    
    void setGraph(Graph newGraph);
    
    int getNumInputs();
    
    int getNumOutputs();
    
    int getMaxInputs();
    
    int getMaxOutputs();
    
    default void onConnected(Self other) {}
    
    default void onDisconnected(Self other) {}
    
    Set<Self> getInputs();
    
    Set<Self> getOutputs();
    
    boolean hasInput(Self in);
    
    boolean hasOutput(Self out);
    
    boolean addInput(Self input);
    
    boolean addOutput(Self output);
    
    boolean removeInput(Self input);
    
    boolean removeOutput(Self output);
    
}
