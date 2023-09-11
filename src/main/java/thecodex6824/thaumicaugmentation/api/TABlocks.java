/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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
    public static final Block VOID_RECHARGE_PEDESTAL = null;
    public static final Block IMPETUS_MIRROR = null;
    public static final Block ARCANE_TERRAFORMER = null;
    public static final Block RIFT_MONITOR = null;
    public static final Block IMPETUS_GENERATOR = null;
    public static final Block STABILITY_FIELD_GENERATOR = null;
    public static final Block IMPETUS_GATE = null;
    public static final Block STAIRS_ANCIENT = null;
    public static final Block SLAB = null;
    public static final Block SLAB_DOUBLE = null;
    public static final Block STAIRS_ELDRITCH_TILE = null;
    public static final Block BARS = null;
    public static final Block FORTIFIED_GLASS = null;
    public static final Block FORTIFIED_GLASS_PANE = null;
    public static final Block STARFIELD_GLASS = null;
    public static final Block OBELISK = null;
    public static final Block CAPSTONE = null;
    public static final Block STRANGE_CRYSTAL = null;
    public static final Block CRAB_VENT = null;
    public static final Block ELDRITCH_LOCK = null;
    public static final Block RIFT_BARRIER = null;
    public static final Block ELDRITCH_LOCK_IMPETUS = null;
    public static final Block BUTTON_GREATWOOD = null;
    public static final Block BUTTON_SILVERWOOD = null;
    public static final Block BUTTON_ARCANE_STONE = null;
    public static final Block WARDED_BUTTON_GREATWOOD = null;
    public static final Block WARDED_BUTTON_SILVERWOOD = null;
    public static final Block WARDED_BUTTON_ARCANE_STONE = null;
    public static final Block PRESSURE_PLATE_GREATWOOD = null;
    public static final Block PRESSURE_PLATE_SILVERWOOD = null;
    public static final Block PRESSURE_PLATE_ARCANE_STONE = null;
    public static final Block WARDED_PRESSURE_PLATE_GREATWOOD = null;
    public static final Block WARDED_PRESSURE_PLATE_SILVERWOOD = null;
    public static final Block WARDED_PRESSURE_PLATE_ARCANE_STONE = null;
    public static final Block URN = null;
    public static final Block ITEM_GRATE = null;
    public static final Block GLASS_TUBE = null;
    public static final Block IMPETUS_CREATIVE = null;
    public static final Block AUGMENTATION_STATION = null;
    public static final Block AUGMENT_PLANNER = null;
    public static final Block COSMETIC_STATION = null;
    public static final Block TAINTED_SLURRY = null;
    public static final Block SAND = null;

    /**
     * Returns all blocks in the mod.
     * @return All the blocks
     */
    public static Block[] getAllBlocks() {
        return new Block[] {VIS_REGENERATOR, WARDED_CHEST, ARCANE_DOOR_GREATWOOD, ARCANE_DOOR_THAUMIUM,
               ARCANE_DOOR_SILVERWOOD, TEMPORARY_LIGHT, STONE, ARCANE_TRAPDOOR_WOOD, ARCANE_TRAPDOOR_METAL, 
               ARCANE_TRAPDOOR_SILVERWOOD, TAINT_FLOWER, IMPETUS_DRAINER, IMPETUS_RELAY, IMPETUS_DIFFUSER,
               IMPETUS_MATRIX, IMPETUS_MATRIX_BASE, RIFT_FEEDER, RIFT_MOVER_INPUT, RIFT_MOVER_OUTPUT,
               RIFT_JAR, VOID_RECHARGE_PEDESTAL, IMPETUS_MIRROR, ARCANE_TERRAFORMER, RIFT_MONITOR,
               IMPETUS_GENERATOR, STABILITY_FIELD_GENERATOR, IMPETUS_GATE, STAIRS_ANCIENT, SLAB, SLAB_DOUBLE,
               STAIRS_ELDRITCH_TILE, BARS, FORTIFIED_GLASS, FORTIFIED_GLASS_PANE, STARFIELD_GLASS, OBELISK,
               CAPSTONE, STRANGE_CRYSTAL, CRAB_VENT, ELDRITCH_LOCK, RIFT_BARRIER, ELDRITCH_LOCK_IMPETUS,
               BUTTON_GREATWOOD, BUTTON_SILVERWOOD, BUTTON_ARCANE_STONE, WARDED_BUTTON_GREATWOOD, WARDED_BUTTON_SILVERWOOD,
               WARDED_BUTTON_ARCANE_STONE, PRESSURE_PLATE_GREATWOOD, PRESSURE_PLATE_SILVERWOOD, PRESSURE_PLATE_ARCANE_STONE,
               WARDED_PRESSURE_PLATE_GREATWOOD, WARDED_PRESSURE_PLATE_SILVERWOOD, WARDED_PRESSURE_PLATE_ARCANE_STONE,
               URN, ITEM_GRATE, GLASS_TUBE, IMPETUS_CREATIVE, AUGMENTATION_STATION, AUGMENT_PLANNER,
               COSMETIC_STATION, TAINTED_SLURRY, SAND};
    }

}
