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

package thecodex6824.thaumicaugmentation.common.internal;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import thecodex6824.thaumicaugmentation.api.warded.CapabilityWardStorage;

public class TAHooks {

    public static float checkWardHardness(float oldHardness, World world, BlockPos pos) {
        if (world.isBlockLoaded(pos) && world.getChunk(pos).hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
            if (world.getChunk(pos).getCapability(CapabilityWardStorage.WARD_STORAGE, null).hasWard(pos))
                return -1.0F; // bedrock value
        }
        
        return oldHardness;
    }
    
    public static float checkWardResistance(float oldResistance, World world, BlockPos pos) {
        if (world.isBlockLoaded(pos) && world.getChunk(pos).hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
            if (world.getChunk(pos).getCapability(CapabilityWardStorage.WARD_STORAGE, null).hasWard(pos))
                return 6000000.0F; // bedrock value
        }
        
        return oldResistance;
    }
    
    public static int checkWardFlammability(int oldFlammability, IBlockAccess access, BlockPos pos) {
        if (access instanceof World) {
            World world = (World) access;
            if (world.isBlockLoaded(pos) && world.getChunk(pos).hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                if (world.getChunk(pos).getCapability(CapabilityWardStorage.WARD_STORAGE, null).hasWard(pos))
                    return 0;
            }
        }
        
        return oldFlammability;
    }
    
    public static boolean checkWardRandomTick(WorldServer world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isBlockLoaded(pos) && world.getChunk(pos).hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
            if (world.getChunk(pos).getCapability(CapabilityWardStorage.WARD_STORAGE, null).hasWard(pos))
                return false;
        }
        
        return true;
    }
    
}
