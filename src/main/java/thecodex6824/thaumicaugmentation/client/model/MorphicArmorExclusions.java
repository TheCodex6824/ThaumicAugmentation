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

package thecodex6824.thaumicaugmentation.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nullable;

public final class MorphicArmorExclusions {

    protected static final List<ModelPattern> EXCLUDED = new ArrayList<>();
    
    private MorphicArmorExclusions() {}
    
    private static class ModelPattern {
        
        protected Predicate<String> regex;
        protected String original;
        
        public ModelPattern(String p) {
            original = p;
            try {
                regex = Pattern.compile(original).asPredicate();
            }
            catch (PatternSyntaxException ex) {}
        }
        
        @Nullable
        public Predicate<String> getExpr() {
            return regex;
        }
        
        public String getRawString() {
            return original;
        }
        
    }
    
    public static boolean addExcludedModelPattern(String variantPattern) {
        return EXCLUDED.add(new ModelPattern(variantPattern));
    }
    
    public static boolean isModelExcluded(String variant) {
        for (ModelPattern mp : EXCLUDED) {
            Predicate<String> p = mp.getExpr();
            if (p != null && p.test(variant))
                return true;
            else if (p == null && mp.getRawString().equals(variant))
                return true;
        }
        
        return false;
    }
    
    public static boolean removeExcludedModelPattern(String variantPattern) {
        return EXCLUDED.removeIf(p -> p.getRawString().equals(variantPattern));
    }
    
    public static void removeAllExcludedModelPatterns() {
        EXCLUDED.clear();
    }
    
}
