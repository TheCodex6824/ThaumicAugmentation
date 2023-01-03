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

package thecodex6824.thaumicaugmentation.test;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import thecodex6824.thaumicaugmentation.common.util.WeightedRandom;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class TestWeightedRandom {
    
    @Test
    public void testRandom() {
        Random rand = new Random(1337);
        ImmutableList<Integer> answers = ImmutableList.of(1, 3, 3, 2, 3, 3, 3, 2, 2, 3);
        WeightedRandom<Integer> picker = new WeightedRandom<>(ImmutableList.of(1, 2, 3), ImmutableList.of(3, 5, 7));
        for (int i = 0; i < answers.size(); ++i)
            assertEquals(answers.get(i), picker.get(rand));
    }
    
    @Test
    public void testRemoveChoice() {
        Random rand = new Random(1337);
        ImmutableList<Integer> answers = ImmutableList.of(1, 3, 3, 2, 3, 2, 2, 2, 1, 2);
        WeightedRandom<Integer> picker = new WeightedRandom<>(ImmutableList.of(1, 2, 3), ImmutableList.of(3, 5, 7));
        for (int i = 0; i < answers.size() / 2; ++i)
            assertEquals(answers.get(i), picker.get(rand));
        
        picker = picker.removeChoice(3);
        for (int i = answers.size() / 2; i < answers.size(); ++i)
            assertEquals(answers.get(i), picker.get(rand));
    }
    
}
