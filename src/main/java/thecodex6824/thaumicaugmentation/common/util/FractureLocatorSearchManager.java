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

package thecodex6824.thaumicaugmentation.common.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.TAConfig;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.WeakHashMap;

public final class FractureLocatorSearchManager {
    
    private FractureLocatorSearchManager() {}
    
    private static final WeakHashMap<EntityPlayer, Long> locationRequestTimes = new WeakHashMap<>();
    
    // for this application quad or k-d trees would be faster for lookup, but they would need to be tailored
    // to have the middle/median be on the entity to not basically be a big linked list
    // TODO maybe do this?
    private static final HashMap<Integer, HashSet<BlockPos>> locations = new HashMap<>();
    
    public static boolean canPlayerRequestLocation(EntityPlayer player) {
        Long lastRequest = locationRequestTimes.get(player);
        return lastRequest == null || lastRequest < System.currentTimeMillis() - TAConfig.fractureLocatorUpdateInterval.getValue();
    }
    
    public static void resetPlayerLocationTime(EntityPlayer player) {
        locationRequestTimes.put(player, System.currentTimeMillis());
    }
    
    @Nullable
    private static BlockPos findNearest(BlockPos point, HashSet<BlockPos> choices) {
        long minDist = Long.MAX_VALUE;
        BlockPos ret = null;
        for (BlockPos pos : choices) {
            long dist = (long) point.distanceSq(pos);
            if (dist < minDist) {
                minDist = dist;
                ret = pos;
            }
        }
        
        return ret;
    }
    
    @Nullable
    public static BlockPos findNearestFracture(World world, BlockPos pos) {
        if (locations.containsKey(world.provider.getDimension()))
            return findNearest(pos, locations.get(world.provider.getDimension()));
        
        return null;
    }
    
    public static void addFractureLocation(World world, BlockPos pos) {
        if (!locations.containsKey(world.provider.getDimension()))
            locations.put(world.provider.getDimension(), new HashSet<>());
        
        locations.get(world.provider.getDimension()).add(pos);
    }
    
    public static void addFractureLocations(World world, Collection<BlockPos> pos) {
        if (!locations.containsKey(world.provider.getDimension()))
            locations.put(world.provider.getDimension(), new HashSet<>());
        
        locations.get(world.provider.getDimension()).addAll(pos);
    }
    
    public static void removeFractureLocations(World world, BlockPos pos) {
        if (locations.containsKey(world.provider.getDimension()))
            locations.get(world.provider.getDimension()).remove(pos);
    }
    
    public static void removeFractureLocations(World world, Collection<BlockPos> pos) {
        if (locations.containsKey(world.provider.getDimension()))
            locations.get(world.provider.getDimension()).removeAll(pos);
    }
    
}
