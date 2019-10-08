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

package thecodex6824.thaumicaugmentation.api.client;

import java.util.Collection;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;

public final class ImpetusRenderingManager {

    private ImpetusRenderingManager() {}
    
    private static Multimap<Integer, IImpetusNode> nodes = MultimapBuilder.hashKeys().hashSetValues().build();
    
    public static boolean registerRenderableNode(IImpetusNode node) {
        return nodes.put(node.getLocation().getDimension(), node);
    }
    
    public static boolean deregisterRenderableNode(IImpetusNode node) {
        return nodes.remove(node.getLocation().getDimension(), node);
    }
    
    public static Collection<IImpetusNode> getAllRenderableNodes(int dim) {
        return nodes.get(dim);
    }
    
}
