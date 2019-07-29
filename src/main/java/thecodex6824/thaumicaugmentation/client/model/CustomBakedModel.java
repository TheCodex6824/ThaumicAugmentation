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

import java.util.Collections;
import java.util.List;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.TRSRTransformation;

public class CustomBakedModel implements IBakedModel {

    protected final List<BakedQuad> quads;
    protected ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transform;
    
    public CustomBakedModel(List<BakedQuad> q, 
            ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms) {
        quads = q;
        transform = transforms;
    }
    
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side == null)
            return quads;
        else
            return Collections.emptyList();
    }
    
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
    
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return !quads.isEmpty() ? quads.get(0).getSprite() : null;
    }
    
    @Override
    public boolean isAmbientOcclusion() {
        return !quads.isEmpty() ? quads.get(0).shouldApplyDiffuseLighting() : false;
    }
    
    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }
    
    @Override
    public boolean isGui3d() {
        return false;
    }
    
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
        return PerspectiveMapWrapper.handlePerspective(this, transform, cameraTransformType);
    }
    
}
