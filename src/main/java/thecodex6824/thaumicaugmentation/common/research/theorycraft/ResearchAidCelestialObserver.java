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

package thecodex6824.thaumicaugmentation.common.research.theorycraft;

import thaumcraft.api.research.theorycraft.ITheorycraftAid;
import thaumcraft.api.research.theorycraft.TheorycraftCard;
import thaumcraft.common.lib.research.theorycraft.CardCelestial;
import thecodex6824.thaumicaugmentation.common.entity.EntityCelestialObserver;

public class ResearchAidCelestialObserver implements ITheorycraftAid {

    @SuppressWarnings("unchecked")
    protected static final Class<TheorycraftCard>[] CARDS = new Class[] {CardCelestial.class};
    
    @Override
    public Object getAidObject() {
        return EntityCelestialObserver.class;
    }
    
    @Override
    public Class<TheorycraftCard>[] getCards() {
        return CARDS;
    }
    
}
