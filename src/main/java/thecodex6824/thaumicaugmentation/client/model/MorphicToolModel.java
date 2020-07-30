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

package thecodex6824.thaumicaugmentation.client.model;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;

public class MorphicToolModel extends MorphicItemModel {
    
    protected static final List<ResourceLocation> DEPS = ImmutableList.of(
            new ResourceLocation("thaumcraft", "item/enchanted_placeholder"));
    
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return DEPS;
    }
    
    @Override
    public IBakedModel bake(IModelState state, VertexFormat format,
            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        
        IModel base = ModelLoaderRegistry.getModelOrLogError(DEPS.get(0), "Could not get base item model");
        IBakedModel baseBaked = base.bake(base.getDefaultState(), DefaultVertexFormats.ITEM,
                ModelLoader.defaultTextureGetter());
        
        return new BakedModel(baseBaked);
    }
    
    public static class Loader implements ICustomModelLoader {
        
        @Override
        public boolean accepts(ResourceLocation loc) {
            return loc.getNamespace().equals("ta_special") && loc.getPath().equals("morphic_tool");
        }
        
        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception {
            return new MorphicToolModel();
        }
        
        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {}
        
    }
    
    public static class BakedModel extends MorphicItemModel.BakedModel {
        
        protected BakedModel(IBakedModel wrappedModel) {
            super(wrappedModel);
        }
        
        @Override
        protected ItemStack getMorphicItem(ItemStack stack) {
            IMorphicTool tool = stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null);
            return tool != null ? tool.getDisplayStack() : ItemStack.EMPTY;
        }
        
    }
    
}
