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
import thecodex6824.thaumicaugmentation.api.TABlocks;

public class ResearchAidRiftMonitor implements ITheorycraftAid {

    @SuppressWarnings("unchecked")
    protected static final Class<TheorycraftCard>[] CARDS = new Class[] {ResearchCardRiftMonitor.class};
    
    @Override
    public Object getAidObject() {
        // like the jar it will still work even if not detecting anything
        // but there's no way I'm just looping over all the blocks like crazy so gg I guess
        return TABlocks.RIFT_MONITOR;
    }
    
    @Override
    public Class<TheorycraftCard>[] getCards() {
        return CARDS;
    }
    
}
