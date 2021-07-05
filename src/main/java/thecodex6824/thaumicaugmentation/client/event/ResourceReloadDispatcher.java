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

package thecodex6824.thaumicaugmentation.client.event;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;

public class ResourceReloadDispatcher implements IClientResourceReloadDispatcher, ISelectiveResourceReloadListener {

    protected Set<ISelectiveResourceReloadListener> listeners;
    
    public ResourceReloadDispatcher() {
        listeners = Collections.newSetFromMap(new WeakHashMap<ISelectiveResourceReloadListener, Boolean>());
    }
    
    @Override
    public boolean registerListener(ISelectiveResourceReloadListener listener) {
        return listeners.add(listener);
    }
    
    @Override
    public boolean deregisterListener(ISelectiveResourceReloadListener listener) {
        return listeners.remove(listener);
    }
    
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        for (ISelectiveResourceReloadListener l : listeners)
            l.onResourceManagerReload(resourceManager, resourcePredicate);
    }
    
}
