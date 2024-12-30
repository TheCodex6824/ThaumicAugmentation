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

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thecodex6824.thaumcraftfix.api.event.EntityInOuterLandsEvent;
import thecodex6824.thaumcraftfix.api.event.FluxRiftDestroyBlockEvent;

public class IntegrationThaumcraftFix implements IIntegrationHolder {

    @Override
    public void preInit() {}

    @Override
    public void init() {}

    @Override
    public void postInit() {}

    @Override
    public boolean registerEventBus() {
	return true;
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void onFluxRiftEatBlock(FluxRiftDestroyBlockEvent event) {
	MinecraftForge.EVENT_BUS.post(
		new thecodex6824.thaumicaugmentation.api.event.FluxRiftDestroyBlockEvent(
			event.getRift(), event.getPosition(), event.getDestroyedBlock()));
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void isEntityInOuterLands(EntityInOuterLandsEvent event) {
	MinecraftForge.EVENT_BUS.post(
		new thecodex6824.thaumicaugmentation.api.event.EntityInOuterLandsEvent(event.getEntity()));
    }

}
