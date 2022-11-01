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

package thecodex6824.thaumicaugmentation.client.model;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.util.ArrayList;

public class TAModelLoader implements ICustomModelLoader {

    private final ArrayList<ICustomModelLoader> loaders = new ArrayList<>();
    
    public void registerLoader(ICustomModelLoader loader) {
        loaders.add(loader);
    }
    
    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getNamespace().equals("ta_special");
    }
    
    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        for (ICustomModelLoader loader : loaders) {
            if (loader.accepts(modelLocation))
                return loader.loadModel(modelLocation);
        }
        
        throw new Exception("Model had the ta_special namespace, but no loaders were able to load it: " + modelLocation);
    }
    
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        for (ICustomModelLoader loader : loaders)
            loader.onResourceManagerReload(resourceManager);
    }
    
}
