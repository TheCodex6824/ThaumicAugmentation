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

package thecodex6824.thaumicaugmentation.common.research;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumDifficulty;
import thaumcraft.api.research.IScanThing;
import thaumcraft.api.research.ScanEntity;

public class ScanEntityWithPeacefulFallback implements IScanThing {

    protected String key;
    protected ScanEntity entity;
    protected IScanThing fallback;
    
    public ScanEntityWithPeacefulFallback(String research, ScanEntity scanEntity, IScanThing scanFallback) {
        key = research;
        entity = scanEntity;
        fallback = scanFallback;
    }
    
    @Override
    public boolean checkThing(EntityPlayer player, Object thing) {
        if (player.getEntityWorld().getDifficulty() != EnumDifficulty.PEACEFUL)
            return entity.checkThing(player, thing);
        else
            return fallback.checkThing(player, thing);
    }
    
    @Override
    public String getResearchKey(EntityPlayer player, Object thing) {
        return key;
    }
    
    @Override
    public void onSuccess(EntityPlayer player, Object object) {}
    
}
