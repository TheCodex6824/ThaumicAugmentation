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

package thecodex6824.thaumicaugmentation.api.event;

import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import thaumcraft.common.items.casters.foci.FocusMediumTouch;

@Cancelable
public class FocusTouchGetEntityEvent extends WorldEvent {

    protected FocusMediumTouch touch;
    protected thaumcraft.api.casters.Trajectory trajectory;
    protected RayTraceResult result;
    protected double range;
    
    public FocusTouchGetEntityEvent(FocusMediumTouch focus, thaumcraft.api.casters.Trajectory trajectory, RayTraceResult original, double range) {
        super(focus.getPackage().world);
        this.touch = focus;
        this.trajectory = trajectory;
        result = original;
        this.range = range;
    }
    
    public FocusMediumTouch getFocus() {
        return touch;
    }
    
    public thaumcraft.api.casters.Trajectory getTrajectory() {
        return trajectory;
    }
    
    public RayTraceResult getRay() {
        return result;
    }
    
    public double getRange() {
        return range;
    }
    
    public void setRay(RayTraceResult ray) {
        result = ray;
    }
    
    public static class Trajectory extends FocusTouchGetEntityEvent {
        
        public Trajectory(FocusMediumTouch focus, thaumcraft.api.casters.Trajectory trajectory, RayTraceResult original, double range) {
            super(focus, trajectory, original, range);
        }
        
    }
    
    public static class Target extends FocusTouchGetEntityEvent {
        
        public Target(FocusMediumTouch focus, thaumcraft.api.casters.Trajectory trajectory, RayTraceResult original, double range) {
            super(focus, trajectory, original, range);
        }
        
    }
    
}
