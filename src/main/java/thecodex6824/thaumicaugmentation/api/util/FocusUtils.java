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

package thecodex6824.thaumicaugmentation.api.util;

import java.lang.reflect.Field;

import net.minecraftforge.fml.common.FMLCommonHandler;
import thaumcraft.api.casters.FocusPackage;

/**
 * Utility methods for working with Thaumcraft foci.
 * @author TheCodex6824
 */
public final class FocusUtils {

    private static final Field PACKAGE_POWER;
    
    static {
        Field f = null;
        try {
            f = FocusPackage.class.getDeclaredField("power");
            f.setAccessible(true);
        }
        catch (Exception ex) {
            FMLCommonHandler.instance().raiseException(ex, "Failed to access Thaumcraft's FocusPackage#power", true);
        }
        
        PACKAGE_POWER = f;
    }
    
    private FocusUtils() {}
    
    public static void setPackagePower(FocusPackage fPackage, float newPower) {
        try {
            PACKAGE_POWER.setFloat(fPackage, newPower);
        }
        catch (Exception ex) {
            FMLCommonHandler.instance().raiseException(ex, "Failed to set Thaumcraft's FocusPackage#power", true);
        }
    }
    
    public static float getPackagePower(FocusPackage fPackage) {
        return fPackage.getPower();
    }
    
}
