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

import java.util.Deque;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.graph.INode;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public interface IImpetusNode extends INode<IImpetusGraph, IImpetusNode> {
    
    public Set<DimensionalBlockPos> getInputLocations();
    
    public Set<DimensionalBlockPos> getOutputLocations();
    
    public default long onTransaction(Deque<IImpetusNode> path, long energy, boolean simulate) {
        return energy;
    }
    
    public DimensionalBlockPos getLocation();
    
    public void setLocation(DimensionalBlockPos location);
    
    public Vec3d getBeamEndpoint();
    
    public boolean shouldPhysicalBeamLinkTo(IImpetusNode other);
    
    public boolean shouldEnforceBeamLimitsWith(IImpetusNode other);
    
    public boolean canConnectNodeAsInput(IImpetusNode toConnect);
    
    public boolean canConnectNodeAsOutput(IImpetusNode toConnect);
    
    public boolean canRemoveNodeAsInput(IImpetusNode toRemove);
    
    public boolean canRemoveNodeAsOutput(IImpetusNode toRemove);
    
    public double getMaxConnectDistance(IImpetusNode toConnect);
    
    public boolean addInputLocation(DimensionalBlockPos toConnect);
    
    public boolean addOutputLocation(DimensionalBlockPos toConnect);
    
    public boolean removeInputLocation(DimensionalBlockPos toRemove);
    
    public boolean removeOutputLocation(DimensionalBlockPos toRemove);

    public void shouldTryToReConnect(World world);

    public void init(World world);
    
    public void unload();
    
    public void destroy();
    
    public NBTTagCompound getSyncNBT();
    
    public void readSyncNBT(NBTTagCompound tag);
    
}
