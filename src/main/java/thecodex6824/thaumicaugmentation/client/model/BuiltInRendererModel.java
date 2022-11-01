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

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class BuiltInRendererModel implements IModel {

    protected IModel wrap;
    
    protected BuiltInRendererModel(@Nullable IModel wrapped) {
        wrap = wrapped;
    }
    
    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
    
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }
    
    @Override
    public Collection<ResourceLocation> getTextures() {
        return Collections.emptyList();
    }
    
    @Override
    public IBakedModel bake(IModelState state, VertexFormat format,
            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        
        return new BakedModel(wrap != null ? wrap.bake(state, format, bakedTextureGetter) : null);
    }
    
    public static class Loader implements ICustomModelLoader {
        
        public Loader() {}
        
        @Override
        public boolean accepts(ResourceLocation loc) {
            return loc.getNamespace().equals("ta_special") && loc.getPath().startsWith("renderer_builtin:");
        }
        
        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception {
            if (!(modelLocation instanceof ModelResourceLocation))
                throw new Exception("Resource locations must be ModelResourceLocation instances, as a variant is needed");
                
            String component = modelLocation.getPath().split(":", 2)[1];
            return new BuiltInRendererModel(ModelLoaderRegistry.getModel(new ModelResourceLocation(component, ((ModelResourceLocation) modelLocation).getVariant())));
        }
        
        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {}
        
    }
    
    public static class BakedModel implements IBakedModel {
        
        protected IBakedModel wrapped;
        protected TransformType lastTransform;
        
        protected BakedModel(@Nullable IBakedModel toWrap) {
            wrapped = toWrap;
        }
        
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return wrapped != null ? wrapped.getQuads(state, side, rand) : Collections.emptyList();
        }
        
        @Override
        public ItemOverrideList getOverrides() {
            return wrapped.getOverrides();
        }
        
        @Override
        public TextureAtlasSprite getParticleTexture() {
            return wrapped != null ? wrapped.getParticleTexture() : null;
        }
        
        @Override
        public boolean isAmbientOcclusion() {
            return wrapped != null && wrapped.isAmbientOcclusion();
        }
        
        @Override
        public boolean isBuiltInRenderer() {
            return true;
        }
        
        @Override
        public boolean isGui3d() {
            return wrapped != null && wrapped.isGui3d();
        }
        
        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return ItemCameraTransforms.DEFAULT;
        }
        
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
            lastTransform = cameraTransformType;
            return Pair.of(this, wrapped.handlePerspective(cameraTransformType).getValue());
        }
        
        public IBakedModel getWrappedModel() {
            return wrapped;
        }
        
        public TransformType getLastTransformType() {
            return lastTransform;
        }
        
    }
    
}