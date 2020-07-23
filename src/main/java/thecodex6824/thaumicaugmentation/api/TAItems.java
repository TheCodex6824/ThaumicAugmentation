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

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

/**
 * Holds all of Thaumic Augmentation's items and the creative tab. The fields will be populated
 * after preInit.
 * @author TheCodex6824
 */
@ObjectHolder(ThaumicAugmentationAPI.MODID)
public final class TAItems {

    private TAItems() {}
    
    public static final Item GAUNTLET = null;
    public static final Item MATERIAL = null;
    public static final Item SEAL_COPIER = null;
    public static final Item ARCANE_DOOR = null;
    public static final Item KEY = null;
    public static final Item VOID_BOOTS = null;
    public static final Item RIFT_SEED = null;
    public static final Item AUGMENT_CASTER_RIFT_ENERGY_STORAGE = null;
    public static final Item FRACTURE_LOCATOR = null;
    public static final Item AUGMENT_BUILDER_POWER = null;
    public static final Item AUGMENT_BUILDER_EFFECT = null;
    public static final Item AUGMENT_CUSTOM = null;
    public static final Item MORPHIC_TOOL = null;
    public static final Item PRIMAL_CUTTER = null;
    public static final Item RIFT_JAR = null;
    public static final Item IMPETUS_MIRROR = null;
    public static final Item IMPETUS_LINKER = null;
    public static final Item BIOME_SELECTOR = null;
    public static final Item THAUMOSTATIC_HARNESS = null;
    public static final Item THAUMOSTATIC_HARNESS_AUGMENT = null;
    public static final Item ELYTRA_HARNESS = null;
    public static final Item ELYTRA_HARNESS_AUGMENT = null;
    public static final Item AUTOCASTER_PLACER = null;
    public static final Item IMPULSE_CANNON = null;
    public static final Item IMPULSE_CANNON_AUGMENT = null;
    public static final Item FOCUS_ANCIENT = null;
    public static final Item ELDRITCH_LOCK_KEY = null;
    public static final Item STARFIELD_GLASS = null;
    public static final Item OBELISK_PLACER = null;
    public static final Item RESEARCH_NOTES = null;
    public static final Item AUGMENT_VIS_BATTERY = null;

    public static CreativeTabs CREATIVE_TAB = new CreativeTabs(ThaumicAugmentationAPI.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(MATERIAL, 1, 2);
        }
    };
    
    public static CreativeTabs BIOME_SELECTOR_CREATIVE_TAB = new CreativeTabs(ThaumicAugmentationAPI.MODID + ".biome_selector") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(BIOME_SELECTOR);
        }
        
        @Override
        public boolean hasSearchBar() {
            return true;
        }
    };
    
    static {
        BIOME_SELECTOR_CREATIVE_TAB.setBackgroundImageName("item_search.png");
    }

    /**
     * Returns all the items in the mod.
     * @return All the items
     */
    public static Item[] getAllItems() {
        return new Item[] {GAUNTLET, MATERIAL, SEAL_COPIER, ARCANE_DOOR, KEY, VOID_BOOTS, RIFT_SEED, AUGMENT_CASTER_RIFT_ENERGY_STORAGE,
                FRACTURE_LOCATOR, AUGMENT_BUILDER_POWER, AUGMENT_BUILDER_EFFECT, AUGMENT_CUSTOM, MORPHIC_TOOL,
                PRIMAL_CUTTER, RIFT_JAR, IMPETUS_MIRROR, IMPETUS_LINKER, BIOME_SELECTOR, THAUMOSTATIC_HARNESS, THAUMOSTATIC_HARNESS_AUGMENT,
                ELYTRA_HARNESS, ELYTRA_HARNESS_AUGMENT, AUTOCASTER_PLACER, IMPULSE_CANNON, IMPULSE_CANNON_AUGMENT,
                FOCUS_ANCIENT, ELDRITCH_LOCK_KEY, STARFIELD_GLASS, OBELISK_PLACER, RESEARCH_NOTES, AUGMENT_VIS_BATTERY};
    }

}
