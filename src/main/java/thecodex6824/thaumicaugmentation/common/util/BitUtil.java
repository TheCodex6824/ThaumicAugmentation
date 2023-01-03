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

package thecodex6824.thaumicaugmentation.common.util;

public final class BitUtil {

    private BitUtil() {}
    
    public static boolean isBitSet(int meta, int bit) {
        return ((meta >> bit) & 1) == 1;
    }

    public static int getBits(int meta, int start, int end) {
        int result = 0;
        for (int i = start; i < end; ++i)
            result |= (meta & (1 << i)) >>> start;

            return result;
    }

    public static int setBit(int meta, int bit, boolean predicate) {
        return predicate ? meta |= (1 << bit) : meta;
    }
    
    public static int setOrClearBit(int meta, int bit, boolean predicate) {
        return predicate ? meta |= (1 << bit) : meta & ~(1 << bit);
    }

    public static int setBits(int meta, int start, int end, int number) {
        for (int i = start; i < end; ++i)
            meta |= ((number & (1 << (i - start))) << start);

        return meta;
    }

}
