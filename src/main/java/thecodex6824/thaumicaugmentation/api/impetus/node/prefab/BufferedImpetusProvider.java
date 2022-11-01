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

package thecodex6824.thaumicaugmentation.api.impetus.node.prefab;

import net.minecraft.util.math.BlockPos;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusProvider;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public class BufferedImpetusProvider extends ImpetusNode implements IImpetusProvider {
    
    protected IImpetusStorage buffer;
    
    public BufferedImpetusProvider(int totalInputs, int totalOutputs, IImpetusStorage owning) {
        this(totalInputs, totalOutputs, new DimensionalBlockPos(new BlockPos(0, 0, 0), 0), owning);
    }
    
    public BufferedImpetusProvider(int totalInputs, int totalOutputs, DimensionalBlockPos location, IImpetusStorage storage) {
        super(totalInputs, totalOutputs, location);
        buffer = storage;
    }
    
    @Override
    public long provide(long energy, boolean simulate) {
        return buffer.extractEnergy(energy, simulate);
    }
    
    public IImpetusStorage getProvider() {
        return buffer;
    }
    
}
