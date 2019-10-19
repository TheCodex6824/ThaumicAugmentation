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

package thecodex6824.thaumicaugmentation.api.internal;

import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public interface IInternalMethodProvider {
    
    public void addConfigListener(Runnable listener);
    
    public boolean removeConfigListener(Runnable listener);
    
    public ItemStack createCasterStrengthProviderStack(ResourceLocation id);
    
    public String getCasterStrengthProviderID(ItemStack stack);
    
    public ItemStack createCasterEffectProviderStack(ResourceLocation id);
    
    public String getCasterEffectProviderID(ItemStack stack);
    
    public void syncImpetusTransaction(Collection<IImpetusNode> path);
    
    public void fullySyncImpetusNode(IImpetusNode node);
    
    public void updateImpetusNode(IImpetusNode node, DimensionalBlockPos connection, boolean output, boolean remove);
    
}
