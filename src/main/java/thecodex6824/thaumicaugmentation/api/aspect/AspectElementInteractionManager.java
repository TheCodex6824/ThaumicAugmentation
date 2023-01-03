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

package thecodex6824.thaumicaugmentation.api.aspect;

import thaumcraft.api.aspects.Aspect;

import java.util.*;

/*
 * Handles a registry of aspect "interactions", to use for mechanics where
 * aspects interact with each other.
 * @author TheCodex6824
 */
public final class AspectElementInteractionManager {

    private AspectElementInteractionManager() {}
    
    private static HashMap<Aspect, Set<Aspect>> negativeInteractions;
    
    public static void init() {
        negativeInteractions = new HashMap<>();
        
        addNegativeInteraction(Aspect.AIR, Aspect.VOID, Aspect.EARTH, Aspect.WATER);
        addNegativeInteraction(Aspect.ALCHEMY, Aspect.MECHANISM, Aspect.TOOL);
        addNegativeInteraction(Aspect.AURA, Aspect.FLUX, Aspect.VOID);
        addNegativeInteraction(Aspect.AVERSION, Aspect.MIND, Aspect.LIFE);
        addNegativeInteraction(Aspect.BEAST, Aspect.MAN, Aspect.TRAP);
        addNegativeInteraction(Aspect.COLD, Aspect.FIRE, Aspect.MOTION);
        addNegativeInteraction(Aspect.CRAFT, Aspect.ALCHEMY, Aspect.MAGIC);
        addNegativeInteraction(Aspect.CRYSTAL, Aspect.ENTROPY, Aspect.METAL);
        addNegativeInteraction(Aspect.DARKNESS, Aspect.LIGHT, Aspect.FIRE);
        addNegativeInteraction(Aspect.DEATH, Aspect.LIFE, Aspect.UNDEAD);
        addNegativeInteraction(Aspect.DESIRE, Aspect.EXCHANGE, Aspect.VOID);
        addNegativeInteraction(Aspect.EARTH, Aspect.AIR, Aspect.WATER, Aspect.FLIGHT);
        addNegativeInteraction(Aspect.ELDRITCH, Aspect.MIND, Aspect.MAN);
        addNegativeInteraction(Aspect.ENERGY, Aspect.FLUX, Aspect.MECHANISM);
        addNegativeInteraction(Aspect.ENTROPY, Aspect.ORDER, Aspect.CRAFT);
        addNegativeInteraction(Aspect.EXCHANGE, Aspect.DESIRE, Aspect.AVERSION);
        addNegativeInteraction(Aspect.FIRE, Aspect.WATER, Aspect.COLD, Aspect.PLANT);
        addNegativeInteraction(Aspect.FLIGHT, Aspect.EARTH, Aspect.MAN);
        addNegativeInteraction(Aspect.FLUX, Aspect.AURA, Aspect.MAGIC);
        addNegativeInteraction(Aspect.LIFE, Aspect.DEATH, Aspect.UNDEAD);
        addNegativeInteraction(Aspect.LIGHT, Aspect.DARKNESS, Aspect.VOID);
        addNegativeInteraction(Aspect.MAGIC, Aspect.FLUX, Aspect.TOOL);
        addNegativeInteraction(Aspect.MAN, Aspect.BEAST, Aspect.ELDRITCH);
        addNegativeInteraction(Aspect.MECHANISM, Aspect.MAGIC, Aspect.ALCHEMY);
        addNegativeInteraction(Aspect.METAL, Aspect.PLANT, Aspect.CRYSTAL);
        addNegativeInteraction(Aspect.MIND, Aspect.ELDRITCH, Aspect.DESIRE);
        addNegativeInteraction(Aspect.MOTION, Aspect.TRAP, Aspect.COLD);
        addNegativeInteraction(Aspect.ORDER, Aspect.ENTROPY, Aspect.ELDRITCH);
        addNegativeInteraction(Aspect.PLANT, Aspect.FIRE, Aspect.COLD);
        addNegativeInteraction(Aspect.PROTECT, Aspect.AVERSION, Aspect.TRAP);
        addNegativeInteraction(Aspect.SENSES, Aspect.DARKNESS, Aspect.SOUL);
        addNegativeInteraction(Aspect.SOUL, Aspect.UNDEAD, Aspect.MECHANISM);
        addNegativeInteraction(Aspect.TOOL, Aspect.MAGIC, Aspect.ALCHEMY);
        addNegativeInteraction(Aspect.TRAP, Aspect.FLIGHT, Aspect.MOTION);
        addNegativeInteraction(Aspect.UNDEAD, Aspect.LIFE, Aspect.SOUL);
        addNegativeInteraction(Aspect.VOID, Aspect.AIR, Aspect.LIFE);
        addNegativeInteraction(Aspect.WATER, Aspect.FIRE, Aspect.EARTH, Aspect.AIR);
    }
    
    /*
     * Adds a negative interaction, where the presence of one or more aspects has a
     * negative effect on another aspect.
     * @param aspect The aspect that will be affected
     * @param negatives The aspects that cause the effect
     */
    public static void addNegativeInteraction(Aspect aspect, Aspect... negatives) {
        negativeInteractions.put(aspect, new HashSet<>(Arrays.asList(negatives)));
    }
    
    /*
     * Returns all aspects that cause a negative effect in the passed aspect.
     * @param target The aspect to get negative interactions for
     * @return A set of all aspects that cause a negative interaction
     */
    public static Set<Aspect> getNegativeAspects(Aspect target) {
        return negativeInteractions.getOrDefault(target, Collections.emptySet());
    }
    
}
