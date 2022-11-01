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

package thecodex6824.thaumicaugmentation.api.ward.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;

import javax.annotation.Nullable;
import java.util.UUID;

public class WardOwnerProviderOwnable<T extends Entity & IEntityOwnable> implements IWardOwnerProvider {

    protected T entity;
    
    public WardOwnerProviderOwnable(T ownable) {
        entity = ownable;
    }
    
    @Override
    @Nullable
    public UUID getWardOwnerUUID() {
        return entity.getOwnerId();
    }
    
}
