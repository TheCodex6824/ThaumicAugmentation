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

package thecodex6824.thaumicaugmentation.common.integration;

import net.minecraft.world.World;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;
import thecodex6824.auracontrol.api.AuraControlAPI;
import thecodex6824.auracontrol.api.CapabilityOriginalAuraInfo;
import thecodex6824.auracontrol.api.IOriginalAuraInfo;

public class IntegrationAuraControl implements IIntegrationHolder {

    @Override
    public void preInit() {}
    
    @Override
    public void init() {}
    
    @Override
    public void postInit() {}
    
    public void handleAura(World world, int chunkX, int chunkZ) {
        AuraControlAPI.handleAura(world, chunkX, chunkZ);
    }
    
    public boolean resetAura(World world, int chunkX, int chunkZ) {
        IOriginalAuraInfo info = world.getChunk(chunkX, chunkZ).getCapability(CapabilityOriginalAuraInfo.AURA_INFO, null);
        if (info != null) {
            AuraChunk c = AuraHandler.getAuraChunk(world.provider.getDimension(), chunkX, chunkZ);
            if (c == null)
                AuraHandler.addAuraChunk(world.provider.getDimension(), world.getChunk(chunkX, chunkZ), info.getBase(), info.getVis(), info.getFlux());
            else {
                c.setBase(info.getBase());
                c.setVis(info.getVis());
                c.setFlux(info.getFlux());
            }
            
            return true;
        }
        
        return false;
    }
    
}
