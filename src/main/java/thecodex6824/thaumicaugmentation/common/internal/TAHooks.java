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
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import thaumcraft.api.items.ItemsTC;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.warded.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.IWardStorage;

public class TAHooks {

    private static boolean hasWard(World world, BlockPos pos) {
        if (world != null && pos != null && world.getChunkProvider() != null && world.isBlockLoaded(pos)) {
            Chunk chunk = world.getChunk(pos);
            if (chunk != null) {
                IWardStorage ward = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                if (ward != null)
                    return ward.hasWard(pos);
            }
        }
        
        return false;
    }
    
    public static float checkWardHardness(float oldHardness, World world, BlockPos pos) {
        return hasWard(world, pos) ? -1.0F : oldHardness;
    }
    
    public static float checkWardResistance(float oldResistance, World world, BlockPos pos) {
        return hasWard(world, pos) ? 6000000.0F : oldResistance;
    }
    
    public static int checkWardFlammability(int oldFlammability, IBlockAccess access, BlockPos pos) {
        if (oldFlammability == 0)
            return 0;
        else if (access instanceof World)
            return hasWard((World) access, pos) ? 0 : oldFlammability;
        else
            return oldFlammability;
    }
    
    public static int checkWardFireEncouragement(int oldEncouragement, IBlockAccess access, BlockPos pos) {
        if (oldEncouragement == 0)
            return 0;
        else if (access instanceof World)
            return hasWard((World) access, pos) ? 0 : oldEncouragement;
        else
            return oldEncouragement;
    }
    
    public static boolean checkWardRandomTick(WorldServer world, BlockPos pos, IBlockState state, Random rand) {
        return !hasWard(world, pos);
    }
    
    public static boolean checkWardGeneric(World world, BlockPos pos) {
        return !hasWard(world, pos);
    }
    
    public static ItemStack getLeftoverInfusionIngredientStack(ItemStack input, Object output) {
        if (output instanceof ItemStack && ((ItemStack) output).getItem() == TAItems.MORPHIC_TOOL) {
            if (input.getItem() != ItemsTC.primordialPearl)
                return ItemStack.EMPTY;
        }
        
        return input;
    }
    
}
