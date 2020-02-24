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

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

public class OBJTintedModel implements IModel {

    protected IModel model;
    
    public OBJTintedModel(IModel wrapped) {
        model = wrapped;
    }
    
    @Override
    public IModelState getDefaultState() {
        return model.getDefaultState();
    }
    
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return model.getDependencies();
    }
    
    @Override
    public Collection<ResourceLocation> getTextures() {
        return model.getTextures();
    }
    
    @Override
    public IBakedModel bake(IModelState state, VertexFormat format,
            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        
        return new BakedModel(model.bake(state, format, bakedTextureGetter));
    }
    
    public static class Loader implements ICustomModelLoader {
        
        @Override
        public boolean accepts(ResourceLocation loc) {
            return loc.getNamespace().equals("ta_special") && loc.getPath().startsWith("obj_tinted:");
        }
        
        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception {
            if (!(modelLocation instanceof ModelResourceLocation))
                throw new Exception("Resource locations must be ModelResourceLocation instances, as a variant is needed");
                
            String component = modelLocation.getPath().split(":", 2)[1];
            return new OBJTintedModel(ModelLoaderRegistry.getModel(new ModelResourceLocation(component, ((ModelResourceLocation) modelLocation).getVariant())));
        }
        
        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {}
        
    }
    
    public static class BakedModel implements IBakedModel {
        
        protected IBakedModel wrapped;
        protected ItemOverrideList overrides;
        
        protected BakedModel(IBakedModel wrappedModel) {
            wrapped = wrappedModel;
            overrides = new ItemOverrideList(ImmutableList.of()) {
                @Override
                public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world,
                        @Nullable EntityLivingBase entity) {
                    
                    IBakedModel old = wrapped.getOverrides().handleItemState(originalModel, stack, world, entity);
                    if (old != BakedModel.this)
                        return new BakedModel(old);
                    else
                        return old;
                }
            };
        }
        
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            List<BakedQuad> quads = wrapped.getQuads(state, side, rand);
            if (!quads.isEmpty()) {
                ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
                TextureAtlasSprite tinted = quads.get(0).getSprite();
                int index = 0;
                for (BakedQuad old : quads) {
                    if (old.getSprite().equals(tinted)) {
                        builder.add(new BakedQuad(old.getVertexData(), index, old.getFace(), old.getSprite(),
                                old.shouldApplyDiffuseLighting(), old.getFormat()));
                    }
                    else {
                        tinted = old.getSprite();
                        ++index;
                        builder.add(new BakedQuad(old.getVertexData(), index, old.getFace(), old.getSprite(),
                                old.shouldApplyDiffuseLighting(), old.getFormat()));
                    }
                }
                
                return builder.build();
            }
            
            return quads;
        }
        
        @Override
        public ItemOverrideList getOverrides() {
            return overrides;
        }
        
        @Override
        public TextureAtlasSprite getParticleTexture() {
            return wrapped.getParticleTexture();
        }
        
        @Override
        public boolean isAmbientOcclusion() {
            return wrapped.isAmbientOcclusion();
        }
        
        @Override
        public boolean isAmbientOcclusion(IBlockState state) {
            return wrapped.isAmbientOcclusion(state);
        }
        
        @Override
        public boolean isBuiltInRenderer() {
            return wrapped.isBuiltInRenderer();
        }
        
        @Override
        public boolean isGui3d() {
            return wrapped.isGui3d();
        }
        
        @Override
        @SuppressWarnings("deprecation")
        public ItemCameraTransforms getItemCameraTransforms() {
            return wrapped.getItemCameraTransforms();
        }
        
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
            return wrapped.handlePerspective(cameraTransformType);
        }
        
    }
    
}
