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

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import thaumcraft.common.entities.EntityFluxRift;

public final class RiftHelper {

    private RiftHelper() {}
    
    public static Vec3d getRiftCenter(EntityFluxRift rift) {
        return rift.points.get(rift.points.size() / 2);
    }
    
    public static Vec3d getRiftCenter(FluxRiftReconstructor rift) {
        return rift.getPoints()[rift.getPoints().length / 2];
    }
    
    public static Vec3d pickRandomPointOnRift(EntityFluxRift rift) {
        return rift.points.get(ThreadLocalRandom.current().nextInt(rift.points.size()));
    }
    
    public static Vec3d pickRandomPointOnRift(FluxRiftReconstructor rift) {
        return rift.getPoints()[ThreadLocalRandom.current().nextInt(rift.getPoints().length)];
    }
    
    public static Vec3d pickRandomPointOnRiftWithInstability(EntityFluxRift rift, int ticks) {
        int index = ThreadLocalRandom.current().nextInt(rift.points.size());
        Vec3d point = rift.points.get(index);
        float time = ticks + Minecraft.getMinecraft().getRenderPartialTicks();
        if (index > rift.points.size() / 2)
            time -= index * 10;
        else if (index < rift.points.size() / 2)
            time += index * 10;
        
        float stab = Math.max(Math.min(1.0F - rift.getRiftStability() / 50.0F, 1.5F), 0.0F);
        return point.add(Math.sin(time / 50.0) * 0.10000000149011612 * stab, Math.sin(time / 60.0) * 0.10000000149011612 * stab,
                Math.sin(time / 70.0) * 0.10000000149011612 * stab);
    }
    
    public static Vec3d pickRandomPointOnRiftWithInstability(EntityFluxRift rift, int ticks, float partialTicks) {
        int index = ThreadLocalRandom.current().nextInt(rift.points.size());
        Vec3d point = rift.points.get(index);
        float time = ticks + partialTicks;
        if (index > rift.points.size() / 2)
            time -= index * 10;
        else if (index < rift.points.size() / 2)
            time += index * 10;
        
        float stab = Math.max(Math.min(1.0F - rift.getRiftStability() / 50.0F, 1.5F), 0.0F);
        return point.add(Math.sin(time / 50.0) * 0.10000000149011612 * stab, Math.sin(time / 60.0) * 0.10000000149011612 * stab,
                Math.sin(time / 70.0) * 0.10000000149011612 * stab);
    }
    
    public static Vec3d pickRandomPointOnRiftWithInstability(FluxRiftReconstructor rift, float stability, int ticks, float partialTicks) {
        int index = ThreadLocalRandom.current().nextInt(rift.points.length);
        Vec3d point = rift.points[index];
        float time = ticks + partialTicks;
        if (index > rift.points.length / 2)
            time -= index * 10;
        else if (index < rift.points.length / 2)
            time += index * 10;
        
        float stab = Math.max(Math.min(1.0F - stability / 50.0F, 1.5F), 0.0F);
        return point.add(Math.sin(time / 50.0) * 0.10000000149011612 * stab, Math.sin(time / 60.0) * 0.10000000149011612 * stab,
                Math.sin(time / 70.0) * 0.10000000149011612 * stab);
    }
    
}
