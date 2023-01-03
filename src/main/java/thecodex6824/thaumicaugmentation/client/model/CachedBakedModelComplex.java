/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.client.model;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.client.event.IClientResourceReloadDispatcher;
import thecodex6824.thaumicaugmentation.common.util.IResourceReloadDispatcher;

import java.util.function.Function;
import java.util.function.Predicate;

public class CachedBakedModelComplex<P, T extends IBakedModel> implements ISelectiveResourceReloadListener {

    protected Function<P, T> getter;
    protected T cached;
    
    public CachedBakedModelComplex(Function<P, T> generator) {
        getter = generator;
        IResourceReloadDispatcher dispatcher = ThaumicAugmentation.proxy.getResourceReloadDispatcher();
        if (dispatcher instanceof IClientResourceReloadDispatcher)
            ((IClientResourceReloadDispatcher) dispatcher).registerListener(this);
    }
    
    public T get(P input) {
        if (cached != null)
            return cached;
        
        cached = getter.apply(input);
        return cached;
    }
    
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        if (resourcePredicate.test(VanillaResourceType.MODELS))
            cached = null;
    }
    
}
