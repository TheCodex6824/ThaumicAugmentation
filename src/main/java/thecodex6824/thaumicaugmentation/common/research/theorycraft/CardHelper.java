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

package thecodex6824.thaumicaugmentation.common.research.theorycraft;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;

import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;

public final class CardHelper {

    private static ImmutableSet<String> BLOCKED = ImmutableSet.of();
    
    private CardHelper() {}
    
    private static void updateBlockList() {
        BLOCKED = ImmutableSet.copyOf(TAConfig.deniedCategories.getValue());
    }
    
    static {
        TAConfigHolder.addListener(CardHelper::updateBlockList);
        updateBlockList();
    }
    
    public static boolean isCategoryBlocked(String category) {
        return BLOCKED.contains(category);
    }
    
    public static boolean removeBlockedCategories(Collection<String> removeFrom) {
        return removeFrom.removeAll(BLOCKED);
    }
    
}
