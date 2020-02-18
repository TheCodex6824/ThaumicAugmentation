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

package thecodex6824.thaumicaugmentation.common.research.theorycraft;

import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import thaumcraft.api.research.theorycraft.ResearchTableData;
import thaumcraft.api.research.theorycraft.TheorycraftCard;
import thecodex6824.thaumicaugmentation.common.util.WeightedRandom;

public class ResearchCardRiftJar extends TheorycraftCard {

    @Override
    public int getInspirationCost() {
        return 1;
    }
    
    @Override
    public boolean isAidOnly() {
        return true;
    }
    
    @Override
    public String getLocalizedName() {
        return new TextComponentTranslation("thaumicaugmentation.research.card.rift_jar.title").getFormattedText();
    }
    
    @Override
    public String getLocalizedText() {
        return new TextComponentTranslation("thaumicaugmentation.research.card.rift_jar.text").getFormattedText();
    }
    
    @Override
    public boolean activate(EntityPlayer player, ResearchTableData data) {
        ArrayList<String> choices = data.getAvailableCategories(player);
        CardHelper.removeBlacklistedCategories(choices);
        choices.removeAll(data.categoriesBlocked);
        WeightedRandom<String> picker = new WeightedRandom<>(choices, Collections.nCopies(choices.size(), 1));
        for (int i = 0; i < Math.min(choices.size(), 3); ++i) {
            String key = picker.get(player.getRNG());
            data.categoryTotals.put(key, data.categoryTotals.getOrDefault(key, 0) + player.getRNG().nextInt(21) + 10);
        }
        
        data.penaltyStart += 2;
        return true;
    }
    
}
