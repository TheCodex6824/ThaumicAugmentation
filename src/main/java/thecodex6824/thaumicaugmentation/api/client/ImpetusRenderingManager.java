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

package thecodex6824.thaumicaugmentation.api.client;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ImpetusRenderingManager {

    private ImpetusRenderingManager() {}
    
    private static final Int2ObjectMap<Map<DimensionalBlockPos, IImpetusNode>> nodes = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    private static final ImmutableMap<DimensionalBlockPos, IImpetusNode> EMPTY = ImmutableMap.of();
    
    public static void registerRenderableNode(IImpetusNode node) {
        Map<DimensionalBlockPos, IImpetusNode> map = nodes.get(node.getLocation().getDimension());
        if (map == null) {
            map = Collections.synchronizedMap(new HashMap<>());
            nodes.put(node.getLocation().getDimension(), map);
        }
        
        map.put(node.getLocation(), node);
    }
    
    public static boolean deregisterRenderableNode(IImpetusNode node) {
        return nodes.getOrDefault(node.getLocation().getDimension(), EMPTY).remove(node.getLocation()) != null;
    }
    
    public static Collection<IImpetusNode> getAllRenderableNodes(int dim) {
        return nodes.getOrDefault(dim, EMPTY).values();
    }
    
    @Nullable
    public static IImpetusNode findNodeByPosition(DimensionalBlockPos pos) {
        return nodes.getOrDefault(pos.getDimension(), EMPTY).get(pos);
    }
    
}
