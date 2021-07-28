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

package thecodex6824.thaumicaugmentation.common.item.foci;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.items.casters.foci.FocusMediumBolt;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.common.internal.TAHooksCommon;

public class FocusMediumBoltCompat extends FocusMediumBolt {

    protected FocusMediumBolt wrapped;
    
    public FocusMediumBoltCompat(FocusMediumBolt toWrap) {
        super();
        wrapped = toWrap;
        initialize();
    }
    
    @Override
    public boolean canSupply(EnumSupplyType type) {
        return wrapped.canSupply(type);
    }
    
    @Override
    public NodeSetting[] createSettings() {
        return wrapped != null ? wrapped.createSettings() : null;
    }
    
    @Override
    public boolean execute(Trajectory trajectory) {
        wrapped.setPackage(getPackage());
        return wrapped.execute(trajectory);
    }
    
    @Override
    public Aspect getAspect() {
        return wrapped.getAspect();
    }
    
    @Override
    public int getComplexity() {
        return wrapped.getComplexity();
    }
    
    @Override
    public String getKey() {
        return wrapped.getKey();
    }
    
    @Override
    public float getPowerMultiplier() {
        return wrapped.getPowerMultiplier();
    }
    
    @Override
    public String getResearch() {
        return wrapped.getResearch();
    }
    
    @Override
    public EnumUnitType getType() {
        return wrapped.getType();
    }
    
    @Override
    public String getUnlocalizedName() {
        return wrapped.getUnlocalizedName();
    }
    
    @Override
    public String getUnlocalizedText() {
        return wrapped.getUnlocalizedText();
    }
    
    @Override
    public boolean hasIntermediary() {
        return wrapped.hasIntermediary();
    }
    
    @Override
    public boolean isExclusive() {
        return wrapped.isExclusive();
    }
    
    @Override
    public void setParent(FocusNode parent) {
        wrapped.setParent(parent);
        super.setParent(parent);
    }
    
    @Override
    public RayTraceResult[] supplyTargets() {
        if (!(getPackage().getCaster() instanceof EntityPlayer)) {
            if (getParent() == null)
                return new RayTraceResult[0];
            
            ArrayList<RayTraceResult> targets = new ArrayList<>();
            for (Trajectory sT : getParent().supplyTrajectories()) {
              Vec3d end = sT.direction.normalize();
              RayTraceResult ray = EntityUtils.getPointedEntityRay(getPackage().world, getPackage().getCaster(), sT.source, end, 0.25D, 16.0, 0.25F, false);
              ray = TAHooksCommon.fireTargetGetEntityEvent(ray, this, sT, 16.0);
              if (ray == null) {
                  end = end.scale(16.0);
                  end = end.add(sT.source);
                  ray = getPackage().world.rayTraceBlocks(sT.source, end);
              } 
              
              if (ray != null)
                  targets.add(ray);
            } 
            
            return targets.toArray(new RayTraceResult[0]);
        }
        else
            return wrapped.supplyTargets();
    }
    
    @Override
    public Trajectory[] supplyTrajectories() {
        if (!(getPackage().getCaster() instanceof EntityPlayer)) {
            if (getParent() == null) 
                return new Trajectory[0];
            
            Trajectory[] supplied = getParent().supplyTrajectories();
            Trajectory[] trajectories = new Trajectory[supplied.length];
            for (int i = 0; i < supplied.length; ++i) {
                Trajectory sT = supplied[i];
                Vec3d end = sT.direction.normalize();
                RayTraceResult ray = EntityUtils.getPointedEntityRay(getPackage().world, getPackage().getCaster(), sT.source, end, 0.25D, 16.0, 0.25F, false);
                ray = TAHooksCommon.fireTrajectoryGetEntityEvent(ray, this, sT, 16.0);
                if (ray == null) {
                    end = end.scale(16.0);
                    end = end.add(sT.source);
                    ray = getPackage().world.rayTraceBlocks(sT.source, end);
                    if (ray != null)
                        end = ray.hitVec;
              }
              else if (ray.entityHit != null) {
                  end = end.scale(sT.source.distanceTo(ray.entityHit.getPositionVector()));
                  end = end.add(sT.source);
              } 

              trajectories[i] = new Trajectory(end, sT.direction.normalize());
            } 
            
            return trajectories;
        }
        else
            return wrapped.supplyTrajectories();
    }
    
    @Override
    public EnumSupplyType[] willSupply() {
        return wrapped.willSupply();
    }
    
}
