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
 * after preInit.
 * @author TheCodex6824
 * 
 */
@ObjectHolder(ThaumicAugmentationAPI.MODID)
public class TABlocks {

    public static final Block VIS_REGENERATOR = null;
    public static final Block WARDED_CHEST = null;
    public static final Block ARCANE_DOOR = null;
    public static final Block TEMPORARY_LIGHT = null;
    public static final Block STONE = null;
    public static final Block DIMENSIONAL_FRACTURE = null;
    public static final Block ARCANE_TRAPDOOR_WOOD = null;
    public static final Block ARCANE_TRAPDOOR_METAL = null;

    public static Block[] getAllBlocks() {
        return new Block[] {VIS_REGENERATOR, WARDED_CHEST, ARCANE_DOOR, TEMPORARY_LIGHT, STONE,
                DIMENSIONAL_FRACTURE, ARCANE_TRAPDOOR_WOOD, ARCANE_TRAPDOOR_METAL};
    }

}
