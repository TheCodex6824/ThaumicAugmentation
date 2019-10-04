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

import java.util.ArrayList;
import java.util.Deque;

import net.minecraft.util.math.BlockPos;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusProvider;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public class BufferedImpetusConsumer extends ImpetusNode implements IImpetusConsumer {
    
    protected IImpetusStorage buffer;
    
    public BufferedImpetusConsumer(int totalInputs, int totalOutputs, IImpetusStorage owning) {
        this(totalInputs, totalOutputs, new DimensionalBlockPos(new BlockPos(0, 0, 0), 0), owning);
    }
    
    public BufferedImpetusConsumer(int totalInputs, int totalOutputs, DimensionalBlockPos location, IImpetusStorage owning) {
        super(totalInputs, totalOutputs, location);
        buffer = owning;
    }
    
    @Override
    public long consume(long amount) {
        if (amount <= 0)
            return 0;
        else
            amount = Math.min(amount, buffer.receiveEnergy(Long.MAX_VALUE, true));
        
        ArrayList<IImpetusProvider> providers = new ArrayList<>(graph.findDirectProviders(this));
        if (!providers.isEmpty()) {
            providers.sort((p1, p2) -> (int) Math.max(1, Math.min(-1, p1.provide(Long.MAX_VALUE, true) - p2.provide(Long.MAX_VALUE, true))));
            ArrayList<Deque<IImpetusNode>> paths = new ArrayList<>(providers.size());
            for (IImpetusProvider p : providers) {
                Deque<IImpetusNode> path = graph.findPath(p, this);
                if (path != null)
                    paths.add(path);
            }
            
            long drawn = 0;
            long step = amount / providers.size();
            long remain = amount % providers.size();
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
                
                Deque<IImpetusNode> nodes = paths.get(i);
                for (IImpetusNode n : nodes)
                    n.onTransaction(this, actuallyDrawn);
            }
            
            return buffer.receiveEnergy(drawn, false);
        }
        
        return 0;
    }
    
    public IImpetusStorage getConsumer() {
        return buffer;
    }
    
}
