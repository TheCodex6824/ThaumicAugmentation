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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

/**
 * Holds all of the sounds for Thaumic Augmentation.
 * @author TheCodex6824
 */
public final class TASounds {

    private TASounds() {}
    
    private static SoundEvent create(String sound) {
        ResourceLocation path = new ResourceLocation(ThaumicAugmentationAPI.MODID, sound);
        return new SoundEvent(path).setRegistryName(path);
    }
    
    public static final SoundEvent EMPTINESS_AMBIENCE = create("e_ambience");
    public static final SoundEvent EMPTINESS_MUSIC = create("e_music");
    public static final SoundEvent RIFT_ENERGY_ZAP = create("rift_energy_zap");
    public static final SoundEvent FOCUS_WATER_IMPACT = create("focus_water_impact");
    public static final SoundEvent RIFT_MOVER_INPUT_LOOP = create("rift_mover_input_loop");
    public static final SoundEvent RIFT_MOVER_OUTPUT_LOOP = create("rift_mover_output_loop");
    public static final SoundEvent ALTAR_SUMMON_START = create("altar_summon_start");
    public static final SoundEvent ALTAR_SUMMON = create("altar_summon");
    public static final SoundEvent ELYTRA_BOOST_START = create("booster_start");
    public static final SoundEvent ELYTRA_BOOST_END = create("booster_end");
    public static final SoundEvent ELYTRA_BOOST_LOOP = create("booster_loop");
    public static final SoundEvent IMPULSE_CANNON_BEAM_START = create("impulse_cannon_beam_start");
    public static final SoundEvent IMPULSE_CANNON_BEAM_END = create("impulse_cannon_beam_end");
    public static final SoundEvent IMPULSE_CANNON_BEAM_LOOP = create("impulse_cannon_beam_loop");
    public static final SoundEvent IMPULSE_CANNON_BURST = create("impulse_cannon_burst");
    public static final SoundEvent IMPULSE_CANNON_RAILGUN = create("impulse_cannon_railgun");
    
    /**
     * Returns all of the sounds in the mod.
     * @return All the sounds
     */
    public static SoundEvent[] getAllSounds() {
        return new SoundEvent[] {EMPTINESS_AMBIENCE, EMPTINESS_MUSIC, RIFT_ENERGY_ZAP, FOCUS_WATER_IMPACT,
                RIFT_MOVER_INPUT_LOOP, RIFT_MOVER_OUTPUT_LOOP, ALTAR_SUMMON_START, ALTAR_SUMMON,
                ELYTRA_BOOST_START, ELYTRA_BOOST_END, ELYTRA_BOOST_LOOP, IMPULSE_CANNON_BEAM_START,
                IMPULSE_CANNON_BEAM_END, IMPULSE_CANNON_BEAM_LOOP, IMPULSE_CANNON_BURST, IMPULSE_CANNON_RAILGUN};
    }
    
}
