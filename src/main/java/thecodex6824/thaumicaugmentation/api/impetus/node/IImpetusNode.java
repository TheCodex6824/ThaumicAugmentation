/*
 * Thaumic Augmentation
 * Copyright (c) 2023 TheCodex6824.
 *
 * This file is part of Thaumic Augmentation.
 *
 * Thaumic Augmentation is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Thaumic Augmentation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Thaumic Augmentation.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumicaugmentation.api.impetus.node;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.graph.INode;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

import java.util.Deque;
import java.util.Set;

public interface IImpetusNode extends INode<IImpetusGraph, IImpetusNode> {

    Set<DimensionalBlockPos> getInputLocations();

    Set<DimensionalBlockPos> getOutputLocations();

    default long onTransaction(Deque<IImpetusNode> path, long energy, boolean simulate) {
        return energy;
    }

    DimensionalBlockPos getLocation();

    void setLocation(DimensionalBlockPos location);

    Vec3d getBeamEndpoint();

    boolean shouldPhysicalBeamLinkTo(IImpetusNode other);

    boolean shouldEnforceBeamLimitsWith(IImpetusNode other);

    boolean canConnectNodeAsInput(IImpetusNode toConnect);

    boolean canConnectNodeAsOutput(IImpetusNode toConnect);

    boolean canRemoveNodeAsInput(IImpetusNode toRemove);

    boolean canRemoveNodeAsOutput(IImpetusNode toRemove);

    double getMaxConnectDistance(IImpetusNode toConnect);

    boolean addInputLocation(DimensionalBlockPos toConnect);

    boolean addOutputLocation(DimensionalBlockPos toConnect);

    boolean removeInputLocation(DimensionalBlockPos toRemove);

    boolean removeOutputLocation(DimensionalBlockPos toRemove);

    void init(World world);

    void unload();

    void destroy();

    NBTTagCompound getSyncNBT();

    void readSyncNBT(NBTTagCompound tag);

}
