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

package thecodex6824.thaumicaugmentation.common.entity.ai;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAINearestAttackableTargetAnyLiving<T extends EntityLivingBase> extends EntityAITargetAnyLiving {
    
    protected Class<T> targetClass;
    protected int targetChance;
    protected Predicate <? super T> targetEntitySelector;
    protected T targetEntity;

    public EntityAINearestAttackableTargetAnyLiving(EntityLiving living, Class<T> classTarget, boolean checkSight) {
        this(living, classTarget, checkSight, 10, null);
    }

    public EntityAINearestAttackableTargetAnyLiving(EntityLiving living, Class<T> classTarget, boolean checkSight, int chance, @Nullable Predicate <? super T> targetSelector)
    {
        super(living, checkSight);
        targetClass = classTarget;
        targetChance = chance;
        setMutexBits(1);
        targetEntitySelector = (input) -> {
            if (input == null || (targetSelector != null && !targetSelector.apply(input)))
                return false;
            else
                return !EntitySelectors.NOT_SPECTATING.apply(input) ? false : isSuitableTarget(input, false);
        };
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return entity.getEntityBoundingBox().grow(targetDistance, 4.0, targetDistance);
    }
    
    @Override
    public boolean shouldExecute() {
        if (targetChance > 0 && entity.getRNG().nextInt(targetChance) != 0)
            return false;
        else {
            List<T> entities = entity.world.getEntitiesWithinAABB(targetClass, getTargetableArea(getTargetDistance()), targetEntitySelector);
            if (entities.isEmpty())
                return false;
            else {
                Collections.sort(entities, (e1, e2) -> Double.compare(entity.getDistanceSq(e1), entity.getDistanceSq(e2)));
                targetEntity = entities.get(0);
                return true;
            }
        }
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        entity.setAttackTarget(targetEntity);
    }
    
}
