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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.CasterAugmentBuilder;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterStrengthProvider;

public class CasterStrengthProviderModel implements IModel {

    protected ImmutableMap<ResourceLocation, ResourceLocation> models;
    
    protected CasterStrengthProviderModel(ImmutableMap<ResourceLocation, ResourceLocation> m) {
        models = m;
    }
    
    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
    
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return new ArrayList<>(models.values());
    }
    
    @Override
    public Collection<ResourceLocation> getTextures() {
        return Collections.emptyList();
    }
    
    @Override
    public IBakedModel bake(IModelState state, VertexFormat format,
            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        
        ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms = PerspectiveMapWrapper.getTransforms(state);
        IModelState transformedState = new SimpleModelState(transforms);
        IBakedModel missingModel = ModelLoaderRegistry.getMissingModel().bake(transformedState, DefaultVertexFormats.ITEM,
                ModelLoader.defaultTextureGetter());
        
        ImmutableMap.Builder<String, IBakedModel> builder = new ImmutableMap.Builder<>();
        for (Map.Entry<ResourceLocation, ResourceLocation> entry : models.entrySet()) {
            IModel model = ModelLoaderRegistry.getModelOrMissing(entry.getValue());
            if (model != ModelLoaderRegistry.getMissingModel())
                builder.put(entry.getKey().toString(), model.bake(transformedState, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter()));
        }
        
        return new BakedModel(missingModel, builder.build(), transforms);
    }
    
    public static class Loader implements ICustomModelLoader {
        
        @Override
        public boolean accepts(ResourceLocation loc) {
            return loc.getNamespace().equals("ta_special") && loc.getPath().equals("models/item/strength_provider");
        }
        
        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception {
            ImmutableMap.Builder<ResourceLocation, ResourceLocation> builder = ImmutableMap.builder();
            Set<ResourceLocation> providers = CasterAugmentBuilder.getAllStrengthProviders();
            for (ResourceLocation l : providers)
                builder.put(l, new ResourceLocation(l.getNamespace(), "item/" + l.getPath()));
            
            return new CasterStrengthProviderModel(builder.build());
        }
        
        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {}
        
    }
    
    public static class BakedModel implements IBakedModel {
        
        protected IBakedModel wrappedFallback;
        protected ImmutableMap<String, IBakedModel> baked;
        protected ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transform;
        
        protected ItemOverrideList handler = new ItemOverrideList(ImmutableList.of()) {
            
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world,
                    EntityLivingBase entity) {
                
                return baked.getOrDefault(ItemCustomCasterStrengthProvider.getProviderID(stack).toString(),
                        wrappedFallback);
            }
            
        };
        
        protected BakedModel(IBakedModel wrappedModel, ImmutableMap<String, IBakedModel> bakedModels, 
                ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> itemTransforms) {
            wrappedFallback = wrappedModel;
            baked = bakedModels;
            transform = itemTransforms;
        }
        
        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
            return Collections.emptyList();
        }
        
        @Override
        public ItemOverrideList getOverrides() {
            return handler;
        }
        
        @Override
        public TextureAtlasSprite getParticleTexture() {
            return wrappedFallback.getParticleTexture();
        }
        
        @Override
        public boolean isAmbientOcclusion() {
            return wrappedFallback.isAmbientOcclusion();
        }
        
        @Override
        public boolean isBuiltInRenderer() {
            return wrappedFallback.isBuiltInRenderer();
        }
        
        @Override
        public boolean isGui3d() {
            return wrappedFallback.isGui3d();
        }
        
        @Override
        @SuppressWarnings("deprecation")
        public ItemCameraTransforms getItemCameraTransforms() {
            return wrappedFallback.getItemCameraTransforms();
        }
        
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
            return PerspectiveMapWrapper.handlePerspective(this, transform, cameraTransformType);
        }
        
    }
    
}
