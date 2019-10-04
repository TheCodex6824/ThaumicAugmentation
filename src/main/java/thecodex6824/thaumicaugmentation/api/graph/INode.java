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

package thecodex6824.thaumicaugmentation.api.graph;

import java.util.Set;

public interface INode<Graph extends IGraph<Self, ?>, Self> {

    public Graph getGraph();
    
    public void setGraph(Graph newGraph);
    
    public int getNumInputs();
    
    public int getNumOutputs();
    
    public int getMaxInputs();
    
    public int getMaxOutputs();
    
    public void onConnected(Self other);
    
    public void onDisconnected(Self other);
    
    public Set<Self> getInputs();
    
    public Set<Self> getOutputs();
    
    public boolean hasInput(Self in);
    
    public boolean hasOutput(Self out);
    
    public void addInput(Self input);
    
    public void addOutput(Self output);
    
    public boolean removeInput(Self input);
    
    public boolean removeOutput(Self output);
    
}
