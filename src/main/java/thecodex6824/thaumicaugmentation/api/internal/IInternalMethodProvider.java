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

import org.apache.logging.log4j.Logger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.api.casters.FocusPackage;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
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
    
    public Logger getModLogger();
    
    public void sendWispZap(Entity source, Entity target, int color);
    
    public void syncAugmentableItem(Entity holder, int index, IAugmentableItem item);
    
    public void sendVoidStreaksEffect(World world, Vec3d source, Vec3d target, double scale);
    
    /**
     * The only purpose of this is to break a dependency between the API and the main mod,
     * it will also be deleted with the API method in the next major release.
     */
    @Deprecated
    public void replaceAndFixFoci(FocusPackage fPackage, EntityLivingBase caster);
    
}
