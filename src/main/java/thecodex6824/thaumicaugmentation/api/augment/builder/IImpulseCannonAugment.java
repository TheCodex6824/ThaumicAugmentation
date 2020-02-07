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

package thecodex6824.thaumicaugmentation.api.augment.builder;

import net.minecraft.entity.EntityLivingBase;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;

public interface IImpulseCannonAugment extends IAugment {
    
    public enum LensModelType {
        
        BEAM,
        RAILGUN,
        BURST;
        
    }
    
    public LensModelType getLensModel();
    
    public boolean isTickable(EntityLivingBase user);
    
    public default void onCannonUsage(EntityLivingBase user) {}
    
    public default void onCannonTick(EntityLivingBase user, int tickCount) {}
    
    public default void onStopCannonTick(EntityLivingBase user, int tickCount) {}
    
    public default long getImpetusCostPerUsage(EntityLivingBase user) {
        return 0;
    }
    
    public default long getImpetusCostPerTick(EntityLivingBase user, int tickCount) {
        return 0;
    }
    
    public int getMaxUsageDuration();
    
}
