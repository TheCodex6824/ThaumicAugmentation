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

package thecodex6824.thaumicaugmentation.api.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thaumcraft.api.casters.FocusModSplit;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.common.items.casters.foci.*;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectExchangeCompat;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusMediumBoltCompat;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusMediumTouchCompat;

import java.lang.reflect.Field;
import java.util.List;

/*
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

    public static void replaceAndFixFoci(FocusPackage fPackage, EntityLivingBase caster) {
        List<IFocusElement> nodes = fPackage.nodes;
        for (int i = 0; i < nodes.size(); ++i) {
            IFocusElement element = nodes.get(i);
            if (element.getClass() == FocusMediumTouch.class)
                nodes.set(i, new FocusMediumTouchCompat((FocusMediumTouch) element));
            else if (element.getClass() == FocusMediumBolt.class)
                nodes.set(i, new FocusMediumBoltCompat((FocusMediumBolt) element));
            else if (element.getClass() == FocusEffectExchange.class)
                nodes.set(i, new FocusEffectExchangeCompat((FocusEffectExchange) element));
            else if (element.getClass() == FocusModSplitTarget.class || element.getClass() == FocusModSplitTrajectory.class) {
                FocusModSplit mod = (FocusModSplit) element;
                for (FocusPackage p : mod.getSplitPackages()) {
                    p.setCasterUUID(fPackage.getCasterUUID());
                    p.world = caster.getEntityWorld();
                }
            }
        }
    }
    
}
