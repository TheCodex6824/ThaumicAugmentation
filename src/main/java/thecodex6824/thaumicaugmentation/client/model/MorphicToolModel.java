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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
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
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class MorphicToolModel implements IModel {
    
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
    
    public static class BakedModel implements IBakedModel {
        
        protected static final HashSet<ResourceLocation> WARNED_ITEMS = new HashSet<>();
        
        protected IBakedModel wrappedFallback;
        protected ItemOverrideList handler;
        
        protected BakedModel(IBakedModel wrappedModel) {
            wrappedFallback = wrappedModel;
            handler = new ItemOverrideList(ImmutableList.of()) {
                
                @Override
                public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world,
                        @Nullable EntityLivingBase entity) {
                    
                    IBakedModel model = null;
                    ItemStack disp = ItemStack.EMPTY;
                    IMorphicTool tool = stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null);
                    if (tool != null) {
                        disp = tool.getDisplayStack();
                        if (disp.isEmpty())
                            disp = tool.getFunctionalStack();
                        
                        if (!disp.isEmpty())
                            model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(disp, world, entity);
                    }
                    
                    if (model == null) {
                        model = wrappedFallback.getOverrides().handleItemState(wrappedFallback, stack, world, entity);
                        disp = stack;
                    }
                
                    try {
                        for (int i = 0; i < 10; ++i) {
                            IBakedModel next = model.getOverrides().handleItemState(model, disp, world, entity);
                            if (next == model)
                                return model;
                            else
                                model = next;
                        }
                    }
                    catch (Exception ex) {
                        if (WARNED_ITEMS.add(stack.getItem().getRegistryName())) {
                            ThaumicAugmentation.getLogger().debug("Model for item {} threw an exception", stack.getItem().getRegistryName());
                            ThaumicAugmentation.getLogger().catching(Level.DEBUG, ex);
                        }
                        
                        return model;
                    }
                    
                    if (WARNED_ITEMS.add(stack.getItem().getRegistryName()))
                        ThaumicAugmentation.getLogger().debug("Model for item {} was too recursive, this might be a bug", stack.getItem().getRegistryName());
                    
                    return model;
                }
                
                @Override
                @Nullable
                @SuppressWarnings("deprecation")
                public ResourceLocation applyOverride(ItemStack stack, @Nullable World world,
                        @Nullable EntityLivingBase entity) {
                    
                    ItemStack disp = ItemStack.EMPTY;
                    IMorphicTool tool = stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null);
                    if (tool != null) {
                        disp = tool.getDisplayStack();
                        if (disp.isEmpty())
                            disp = tool.getFunctionalStack();
                    }
                    
                    ItemModelMesher m = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
                    if (!disp.isEmpty()) {
                        ResourceLocation r = m.getItemModel(disp).getOverrides().applyOverride(disp, world, entity);
                        if (r == null)
                            r = ((ItemModelMesherForge) m).getLocation(disp);
                        
                        return r;
                    }
                    else {
                        ResourceLocation r = wrappedFallback.getOverrides().applyOverride(stack, world, entity);
                        if (r == null)
                            r = ((ItemModelMesherForge) m).getLocation(disp);
                        
                        return r;
                    }
                }
            };
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
            return Pair.of(this, wrappedFallback.handlePerspective(cameraTransformType).getValue());
        }
        
    }
    
}
