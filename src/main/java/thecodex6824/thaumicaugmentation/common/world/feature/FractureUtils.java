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

package thecodex6824.thaumicaugmentation.common.world.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.google.common.math.DoubleMath;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.common.util.WeightedRandom;
import thecodex6824.thaumicaugmentation.common.world.WorldDataCache;
import thecodex6824.thaumicaugmentation.common.world.WorldDataCache.WorldData;

public final class FractureUtils {

    private FractureUtils() {}
    
    private static HashSet<Integer> possibleDims;
    private static WeightedRandom<Integer> dimPicker;
    
    static {
        possibleDims = new HashSet<>(TAConfig.fractureDimList.getValue().size() + 1);
        possibleDims.add(TAConfig.emptinessDimID.getValue());
        for (String s : TAConfig.fractureDimList.getValue().keySet()) {
            try {
                possibleDims.add(Integer.parseInt(s));
            }
            catch (NumberFormatException ex) {}
        }
    }
    
    private static void reloadDimensionCache() {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int dim : WorldDataCache.listAllDimensions()) {
            if (dim != TADimensions.EMPTINESS.getId() && TAConfig.fractureDimList.getValue().containsKey(Integer.valueOf(dim).toString()))
                map.put(dim, TAConfig.fractureDimList.getValue().get(Integer.toString(dim)));
        }
        
        possibleDims = new HashSet<>(map.keySet());
        dimPicker = new WeightedRandom<>(map.keySet(), map.values());
    }
    
    public static void initDimensionCache() {
        reloadDimensionCache();
        TAConfig.addConfigListener(() -> {
            reloadDimensionCache();
        });
    }
    
    public static boolean canWorldHaveFracture(int id) {
        return id == TADimensions.EMPTINESS.getId() || possibleDims.contains(id);
    }

    public static double movementRatio(World world) {
        return world.provider.getMovementFactor() / TAConfig.emptinessMoveFactor.getValue();
    }
    
    public static WorldData pickRandomDimension(Random rand, double maxFactor) {
        if (dimPicker == null || dimPicker.isEmpty())
            return null;
        
        WeightedRandom<Integer> currentPicker = dimPicker;
        do {
            int dimID = currentPicker.get(rand);
            WorldData dim = WorldDataCache.getData(dimID);
            if (dim != null && dim.getMovementFactor() <= maxFactor + 0.00001)
                return dim;
            else
                currentPicker = dimPicker.removeChoice(dimID);
        } while (!currentPicker.isEmpty());

        return null;
    }

    public static boolean wouldLinkToDim(Random rand, int chunkX, int chunkZ, int targetDim) {
        return targetDim == pickRandomDimension(rand, FractureUtils.calcMaxSafeFactor(TAConfig.emptinessMoveFactor.getValue(), 
                chunkX, chunkZ)).getDimensionID();
    }
    
    public static boolean isDimAllowedForLinking(int dim) {
        if (dimPicker == null)
            initDimensionCache();

        return dimPicker.hasChoice(dim);
    }
    
    public static double calcMaxSafeFactor(double moveFactor, int chunkX, int chunkZ) {
        return Math.min(Math.abs(chunkX), Math.abs(chunkZ)) * moveFactor + moveFactor;
    }

    public static int scaleChunkCoord(int coord, double factor) {
        if (DoubleMath.isMathematicalInteger(factor)) {
            int integralFactor = (int) Math.floor(factor);
            return coord * integralFactor;
        }
        else {
            // try to remove the fraction for common cases where one scale factor is 1
            int integralFactor = (int) Math.round(1.0 / factor);
            return coord / integralFactor;
        }
    }
    
    public static BlockPos scaleBlockPosFromEmptiness(BlockPos pos, double moveFactor, long seed) {
        double factor = TAConfig.emptinessMoveFactor.getValue() / moveFactor;
        int chunkX = scaleChunkCoord(pos.getX() >> 4, factor);
        int chunkZ = scaleChunkCoord(pos.getZ() >> 4, factor);
        
        Random rand = new Random(seed);
        long xSeed = rand.nextLong() >> 2 + 1;
        long zSeed = rand.nextLong() >> 2 + 1;
        rand.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ seed);
        
        int offsetX = 8 + MathHelper.getInt(rand, -2, 2);
        int offsetZ = 8 + MathHelper.getInt(rand, -2, 2);
        return new BlockPos(chunkX * 16 + offsetX, 0, chunkZ * 16 + offsetZ);
    }
    
    public static BlockPos scaleBlockPosToEmptiness(BlockPos pos, double moveFactor, long seed) {
        double factor = moveFactor / TAConfig.emptinessMoveFactor.getValue();
        int chunkX = scaleChunkCoord(pos.getX() >> 4, factor);
        int chunkZ = scaleChunkCoord(pos.getZ() >> 4, factor);
        
        Random rand = new Random(seed);
        long xSeed = rand.nextLong() >> 2 + 1;
        long zSeed = rand.nextLong() >> 2 + 1;
        rand.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ seed);
        if (rand.nextInt(TAConfig.fractureGenChance.getValue()) != 0) {
            ThaumicAugmentation.getLogger().info("A fracture is generating, but its destination fracture will not? This is probably a bug.");
            ThaumicAugmentation.getLogger().debug("Dest dim: emptiness");
            ThaumicAugmentation.getLogger().debug("Src pos: " + pos);
        }
        
        int offsetX = 8 + MathHelper.getInt(rand, -2, 2);
        int offsetZ = 8 + MathHelper.getInt(rand, -2, 2);
        return new BlockPos(chunkX * 16 + offsetX, 0, chunkZ * 16 + offsetZ);
    }
    
}
