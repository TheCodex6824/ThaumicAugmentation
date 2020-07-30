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

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import thecodex6824.thaumicaugmentation.client.renderer.item.MorphicWrappingTEISR;

public abstract class MorphicItemModel implements IModel {
    
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
    
    public static abstract class BakedModel implements IBakedModel {
        
        protected IBakedModel wrappedFallback;
        
        protected ItemOverrideList handler;
        
        protected abstract ItemStack getMorphicItem(ItemStack stack);
        
        protected BakedModel(IBakedModel wrappedModel) {
            wrappedFallback = wrappedModel;
            handler = new ItemOverrideList(ImmutableList.of()) {
                
                @Override
                public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world,
                        @Nullable EntityLivingBase entity) {
                    
                    ItemStack disp = getMorphicItem(stack);
                    if (disp.isEmpty())
                        return wrappedFallback.getOverrides().handleItemState(wrappedFallback, stack, world, entity);
                    else {
                        if (!(stack.getItem().getTileEntityItemStackRenderer() instanceof MorphicWrappingTEISR)) {
                            stack.getItem().setTileEntityItemStackRenderer(new MorphicWrappingTEISR(stack.getItem().getTileEntityItemStackRenderer()) {
                                @Override
                                protected ItemStack getMorphicItem(ItemStack stack) {
                                    return BakedModel.this.getMorphicItem(stack);
                                }
                            });
                        }
                        
                        return Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(disp, world, entity);
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
