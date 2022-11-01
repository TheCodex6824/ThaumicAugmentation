/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.util;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class WeightedRandom<T extends Comparable<T>> {

    private final ImmutableList<T> choice;
    private final int[] weight;
    
    protected WeightedRandom(List<T> choices, int[] calculatedWeights) {
        choice = new ImmutableList.Builder<T>().addAll(choices).build();
        weight = calculatedWeights;
    }
    
    public WeightedRandom(Map<T, Integer> pairs) {
        choice = ImmutableList.copyOf(pairs.keySet());
        weight = new int[pairs.size()];
        ArrayList<Integer> tempWeights = new ArrayList<>(pairs.values());
        for (int i = 0; i < pairs.size(); ++i)
            weight[i] = i > 0 ? weight[i - 1] + tempWeights.get(i) : tempWeights.get(i);
    }
    
    public WeightedRandom(List<T> choices, List<Integer> weights) {
        if (choices.size() != weights.size())
            throw new IllegalArgumentException("Different amount of choices and weights");
        
        choice = ImmutableList.copyOf(choices);
        weight = new int[weights.size()];
        for (int i = 0; i < weights.size(); ++i)
            weight[i] = i > 0 ? weight[i - 1] + weights.get(i) : weights.get(i);
    }
    
    public WeightedRandom(WeightedRandom<T> toCopy) {
        choice = ImmutableList.copyOf(toCopy.choice);
        weight = Arrays.copyOf(toCopy.weight, toCopy.weight.length);
    }
    
    private int binarySearch(int n) {
        int left = 0;
        int right = weight.length - 1;
        while (left < right) {
            int check = left + (right - left) / 2;
            if (weight[check] <= n && weight[check + 1] > n)
                return check + 1;
            else if (weight[check] <= n)
                left = check + 1;
            else if (weight[check] > n)
                right = check;
        }
        
        if (weight[0] > n)
            return 0;
        
        return -1;
    }
    
    public boolean isEmpty() {
        return choice.isEmpty();
    }
    
    public boolean hasChoice(T c) {
        return choice.contains(c);
    }
    
    public T get(Random rand) {
        if (weight.length == 0)
            return null;
        
        return choice.get(binarySearch(rand.nextInt(weight[weight.length - 1])));
    }
    
    private int linearSearch(List<T> toSearch, T toFind) {
        for (int i = 0; i < toSearch.size(); ++i) {
            if (toSearch.get(i).equals(toFind))
                return i;
        }
        
        return -1;
    }
    
    public WeightedRandom<T> removeChoice(T element) {
        ArrayList<T> newList = new ArrayList<>(choice);
        ArrayList<Integer> weightList = new ArrayList<>(Arrays.asList(ArrayUtils.toObject(weight)));
        int toRemoveIndex = linearSearch(newList, element);
        newList.remove(toRemoveIndex);
        int value = toRemoveIndex > 0 ? weightList.remove(toRemoveIndex) - weightList.get(toRemoveIndex - 1) : weightList.remove(toRemoveIndex);
        for (int i = toRemoveIndex; i < weightList.size(); ++i)
            weightList.set(i, weightList.get(i) - value);
        return new WeightedRandom<>(newList, ArrayUtils.toPrimitive(weightList.toArray(new Integer[weightList.size()])));
    }
    
    public WeightedRandom<T> removeChoice(Collection<T> toRemove) {
        ArrayList<T> newList = new ArrayList<>(choice);
        ArrayList<Integer> weightList = new ArrayList<>(Arrays.asList(ArrayUtils.toObject(weight)));
        for (T element : toRemove) {
            int toRemoveIndex = linearSearch(newList, element);
            newList.remove(toRemoveIndex);
            int value = toRemoveIndex > 0 ? weightList.remove(toRemoveIndex) - weightList.get(toRemoveIndex - 1) : weightList.remove(toRemoveIndex);
            for (int i = toRemoveIndex; i < weightList.size(); ++i)
                weightList.set(i, weightList.get(i) - value);
        }
        return new WeightedRandom<>(newList, ArrayUtils.toPrimitive(weightList.toArray(new Integer[weightList.size()])));
    }
    
}