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

package thecodex6824.thaumicaugmentation.client.fx;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.client.fx.particles.FXGeneric;

import java.lang.ref.WeakReference;

public class FXGenericP2ECustomSpeed extends FXGeneric {

    protected WeakReference<Entity> entity;
    protected double speedMin;
    protected double speedMax;
    
    public FXGenericP2ECustomSpeed(World world, double x, double y, double z, Entity target) {
        this(world, x, y, z, target, -0.105, 0.105);
    }
    
    public FXGenericP2ECustomSpeed(World world, double x, double y, double z, Entity target,
            double minSpeed, double maxSpeed) {
        
        super(world, x, y, z, 0.0, 0.0, 0.0);
        entity = new WeakReference<>(target);
        speedMin = minSpeed;
        speedMax = maxSpeed;
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        Entity target = entity.get();
        if (target == null)
            setExpired();
        else {
            double dist = target.getDistance(posX, posY, posZ);
            if (dist < 0.6)
                setExpired();
            else if (dist < 4.0)
                particleScale *= 0.9F;
            
            double dx = (target.posX - posX) / dist;
            double dy = (target.posY - posY) / dist;
            double dz = (target.posZ - posZ) / dist;
            motionX = MathHelper.clamp(motionX + dx, speedMin, speedMax);
            motionY = MathHelper.clamp(motionY + dy, speedMin, speedMax);
            motionZ = MathHelper.clamp(motionZ + dz, speedMin, speedMax);
        }
    }
    
}
