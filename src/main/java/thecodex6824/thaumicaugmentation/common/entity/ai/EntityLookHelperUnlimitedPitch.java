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

package thecodex6824.thaumicaugmentation.common.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.util.math.MathHelper;

public class EntityLookHelperUnlimitedPitch extends EntityLookHelper {

    protected boolean doWeirdStuff;
    
    public EntityLookHelperUnlimitedPitch(EntityLiving entity, boolean doRenderYawOffset) {
        super(entity);
        doWeirdStuff = doRenderYawOffset;
    }
    
    @Override
    public void onUpdateLook() {
        if (isLooking) {
            isLooking = false;
            double d0 = posX - entity.posX;
            double d1 = posY - (entity.posY + entity.getEyeHeight());
            double d2 = posZ - entity.posZ;
            double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
            float f = (float) (Math.toDegrees(MathHelper.atan2(d2, d0))) - 90.0F;
            float f1 = (float) (-Math.toDegrees(MathHelper.atan2(d1, d3)));
            entity.rotationPitch = updateRotation(entity.rotationPitch, f1, deltaLookPitch);
            entity.rotationYawHead = updateRotation(entity.rotationYawHead, f, deltaLookYaw);
        }
        else if (doWeirdStuff)
            entity.rotationYawHead = updateRotation(entity.rotationYawHead, entity.renderYawOffset, 10.0F);

        if (doWeirdStuff) {
            float f2 = MathHelper.wrapDegrees(entity.rotationYawHead - entity.renderYawOffset);
            if (!entity.getNavigator().noPath()) {
                if (f2 < -75.0F)
                    entity.rotationYawHead = entity.renderYawOffset - 75.0F;
    
                if (f2 > 75.0F)
                    entity.rotationYawHead = entity.renderYawOffset + 75.0F;
            }
        }
    }
    
}
