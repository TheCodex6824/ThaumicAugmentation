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
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;

public class MorphicToolModel implements IModel {

    private static final ImmutableList<ResourceLocation> DEPS = ImmutableList.of(
            new ResourceLocation("thaumcraft", "item/enchanted_placeholder"));
    
    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
    
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return DEPS;
    }
    
    @Override
    public Collection<ResourceLocation> getTextures() {
        return Collections.emptyList();
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
    
    public static class BakedModel implements IBakedModel {
        
        private static TRSRTransformation create(float tx, float ty, float tz, float ax, float ay, float az, float s) {
            return TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
                    new Vector3f(tx / 16, ty / 16, tz / 16), TRSRTransformation.quatFromXYZDegrees(
                            new Vector3f(ax, ay, az)), new Vector3f(s, s, s), null));
        }
        
        private static final ImmutableMap<TransformType, TRSRTransformation> TRANSFORMS = ImmutableMap.<TransformType, TRSRTransformation>builder().put(
                TransformType.GROUND, create(0, 2, 0, 0, 0, 0, 0.5F)).put(
                TransformType.HEAD, create(0, 13, 7, 0, 180, 0, 1)).put(
                TransformType.THIRD_PERSON_RIGHT_HAND, create(0, 3, 1, 0, 0, 0, 0.55F)).put(
                TransformType.THIRD_PERSON_LEFT_HAND, create(0, 3, 1, 0, 0, 0, 0.55F)).put(
                TransformType.FIRST_PERSON_RIGHT_HAND, create(1.13F, 3.2F, 1.13F, 0, -90, 25, 0.68F)).put(
                TransformType.FIRST_PERSON_LEFT_HAND, create(1.13F, 3.2F, 1.13F, 0, 90, -25, 0.68F)).put(
                TransformType.FIXED, create(0, 0, 0, 0, 180, 0, 1)).build();
        
        protected IBakedModel wrappedFallback;
        
        protected ItemOverrideList handler = new ItemOverrideList(ImmutableList.of()) {
            
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world,
                    @Nullable EntityLivingBase entity) {
                
                IMorphicTool tool = stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null);
                if (tool.getDisplayStack().isEmpty())
                    return wrappedFallback;
                else
                    return Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(tool.getDisplayStack(), world, entity);
            }
            
        };
        
        protected BakedModel(IBakedModel wrappedModel) {
            wrappedFallback = wrappedModel;
        }
        
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
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
            return PerspectiveMapWrapper.handlePerspective(this, TRANSFORMS, cameraTransformType);
        }
        
    }
    
}
