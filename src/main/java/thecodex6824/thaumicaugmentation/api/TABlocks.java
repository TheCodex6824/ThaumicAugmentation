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

package thecodex6824.thaumicaugmentation.api;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

/**
 * Holds all of Thaumic Augmentation's blocks. The fields will be populated
 * after preInit (during the block registry event).
 * @author TheCodex6824
 */
@ObjectHolder(ThaumicAugmentationAPI.MODID)
public final class TABlocks {

    private TABlocks() {}
    
    public static final Block VIS_REGENERATOR = null;
    public static final Block WARDED_CHEST = null;
    public static final Block ARCANE_DOOR_GREATWOOD = null;
    public static final Block ARCANE_DOOR_THAUMIUM = null;
    public static final Block ARCANE_DOOR_SILVERWOOD = null;
    public static final Block TEMPORARY_LIGHT = null;
    public static final Block STONE = null;
    public static final Block ARCANE_TRAPDOOR_WOOD = null;
    public static final Block ARCANE_TRAPDOOR_METAL = null;
    public static final Block ARCANE_TRAPDOOR_SILVERWOOD = null;
    public static final Block TAINT_FLOWER = null;
    public static final Block IMPETUS_DRAINER = null;
    public static final Block IMPETUS_RELAY = null;
    public static final Block IMPETUS_DIFFUSER = null;
    public static final Block IMPETUS_MATRIX = null;
    public static final Block IMPETUS_MATRIX_BASE = null;
    public static final Block RIFT_FEEDER = null;
    public static final Block RIFT_MOVER_INPUT = null;
    public static final Block RIFT_MOVER_OUTPUT = null;
    public static final Block RIFT_JAR = null;

    /**
     * Returns all blocks in the mod.
     * @return All the blocks
     */
    public static Block[] getAllBlocks() {
        return new Block[] {VIS_REGENERATOR, WARDED_CHEST, ARCANE_DOOR_GREATWOOD, ARCANE_DOOR_THAUMIUM,
               ARCANE_DOOR_SILVERWOOD, TEMPORARY_LIGHT, STONE, ARCANE_TRAPDOOR_WOOD, ARCANE_TRAPDOOR_METAL, 
               ARCANE_TRAPDOOR_SILVERWOOD, TAINT_FLOWER, IMPETUS_DRAINER, IMPETUS_RELAY, IMPETUS_DIFFUSER,
               IMPETUS_MATRIX, IMPETUS_MATRIX_BASE, RIFT_FEEDER, RIFT_MOVER_INPUT, RIFT_MOVER_OUTPUT,
               RIFT_JAR};
    }

}
