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
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import org.lwjgl.util.vector.Vector3f;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.block.BlockGlassTube;
import thecodex6824.thaumicaugmentation.common.block.BlockGlassTube.ConnectionType;

import javax.annotation.Nullable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class GlassTubeModel implements IModel {
    
    protected static final ImmutableList<ResourceLocation> TEXTURES = ImmutableList.of(
            new ResourceLocation(ThaumicAugmentationAPI.MODID, "blocks/glass_tube_core_0"),
            new ResourceLocation(ThaumicAugmentationAPI.MODID, "blocks/glass_tube_core_1"),
            new ResourceLocation(ThaumicAugmentationAPI.MODID, "blocks/glass_tube_core_c"),
            new ResourceLocation(ThaumicAugmentationAPI.MODID, "blocks/glass_tube_core_s"),
            new ResourceLocation(ThaumicAugmentationAPI.MODID, "blocks/glass_tube_core_3"),
            new ResourceLocation(ThaumicAugmentationAPI.MODID, "blocks/glass_tube_core_4")
    );
    
    @Override
    public Collection<ResourceLocation> getTextures() {
        return TEXTURES;
    }
    
    @Override
    public IBakedModel bake(IModelState state, VertexFormat format,
            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        
        return new BakedModel(format, bakedTextureGetter);
    }
    
    public static class Loader implements ICustomModelLoader {
        
        @Override
        public boolean accepts(ResourceLocation loc) {
            return loc.getNamespace().equals("ta_special") && (loc.getPath().equals("glass_tube") || loc.getPath().equals("models/block/glass_tube"));
        }
        
        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception { 
            return new GlassTubeModel();
        }
        
        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {}
        
    }
    
    public static class BakedModel implements IBakedModel {
        
        protected VertexFormat format;
        protected Function<ResourceLocation, TextureAtlasSprite> textures;
        protected ImmutableList<BakedQuad> prebakedQuads;
        
        protected BakedModel(VertexFormat quadFormat, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
            format = quadFormat;
            textures = textureGetter;
            ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
            FaceBakery bakery = new FaceBakery();
            Vector3f start = new Vector3f(6.5F, 6.5F, 6.5F);
            Vector3f end = new Vector3f(9.5F, 9.5F, 9.5F);
            for (EnumFacing face : EnumFacing.VALUES) {
                for (ResourceLocation l : GlassTubeModel.TEXTURES) {
                    for (int angle = 0; angle < 360; angle += 90) {
                        BlockPartRotation rot = new BlockPartRotation(new Vector3f(8.0F, 8.0F, 8.0F), face.getAxis(), 0, false);
                        quads.add(bakery.makeBakedQuad(start, end, new BlockPartFace(face, 0, l.toString(),
                                new BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, angle)), textureGetter.apply(l),
                                face, ModelRotation.X0_Y0, rot, false, true));
                    }
                }
            }
            
            prebakedQuads = quads.build();
        }
        
        protected PropertyEnum<ConnectionType> propFromFacing(EnumFacing face) {
            switch (face) {
                case DOWN: return BlockGlassTube.DOWN;
                case EAST: return BlockGlassTube.EAST;
                case NORTH: return BlockGlassTube.NORTH;
                case SOUTH: return BlockGlassTube.SOUTH;
                case UP: return BlockGlassTube.UP;
                case WEST: return BlockGlassTube.WEST;
                default: throw new InvalidParameterException("BUG: null / invalid facing");
            }
        }
        
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            if (state != null && side != null) {
                ConnectionType type = state.getValue(propFromFacing(side));
                if (type == ConnectionType.NONE) {
                    ArrayList<EnumFacing> connected = new ArrayList<>();
                    for (EnumFacing f : EnumFacing.VALUES) {
                        if (f != side && f != side.getOpposite() && state.getValue(propFromFacing(f)) != ConnectionType.NONE)
                            connected.add(f);
                    }
                    
                    switch (connected.size()) {
                        case 0: return ImmutableList.of(prebakedQuads.get(side.getIndex() * 24));
                        case 1:
                            EnumFacing single = connected.get(0);
                            int angleOffset = 0;
                            if (side.getHorizontalIndex() != -1) {
                                while (single != EnumFacing.UP) {
                                    single = single.rotateAround(single.getAxis() == Axis.X ? Axis.Z : Axis.X);
                                    ++angleOffset;
                                }
                                
                                if (angleOffset % 2 != 0 && (side == EnumFacing.EAST || side == EnumFacing.SOUTH))
                                    angleOffset = (angleOffset + 2) % 4;
                            }
                            else {
                                EnumFacing orig = single;
                                while (single != EnumFacing.NORTH) {
                                    single = single.rotateY();
                                    ++angleOffset;
                                }
                                
                                if (side == EnumFacing.DOWN || orig.getAxis() == Axis.X)
                                    angleOffset = (angleOffset + 2) % 4;
                            }
                            
                            return ImmutableList.of(prebakedQuads.get(side.getIndex() * 24 + 4 + angleOffset));
                        case 2:
                            EnumFacing first = connected.get(0);
                            EnumFacing second = connected.get(1);
                            EnumFacing min = null;
                            if (first.getHorizontalIndex() != -1 && second.getHorizontalIndex() != -1) {
                                min = first.getHorizontalAngle() < second.getHorizontalAngle() ? first : second;
                                if (min == EnumFacing.SOUTH && (first == EnumFacing.EAST || second == EnumFacing.EAST))
                                    min = EnumFacing.EAST;
                            }
                            else {
                                if (first == EnumFacing.UP || second == EnumFacing.UP)
                                    min = EnumFacing.UP;
                                else if (first == side.rotateYCCW() || second == side.rotateYCCW())
                                    min = side.rotateYCCW();
                                else if (first == EnumFacing.DOWN || second == EnumFacing.DOWN)
                                    min = EnumFacing.DOWN;
                                else
                                    min = side.rotateY();
                                
                                if (min == EnumFacing.UP && (first == side.rotateY() || second == side.rotateY()))
                                    min = side.rotateY();
                            }
                            
                            angleOffset = 0;
                            if (side.getHorizontalIndex() != -1) {
                                while (min != EnumFacing.UP) {
                                    min = min.rotateAround(min.getAxis() == Axis.X ? Axis.Z : Axis.X);
                                    ++angleOffset;
                                }
                                
                                if (angleOffset % 2 != 0 && (side == EnumFacing.EAST || side == EnumFacing.SOUTH))
                                    angleOffset = (angleOffset + 2) % 4;
                            }
                            else {
                                EnumFacing orig = min;
                                while (min != EnumFacing.NORTH) {
                                    min = min.rotateY();
                                    ++angleOffset;
                                }
                                
                                if (side == EnumFacing.UP && orig.getAxis() == Axis.X)
                                    angleOffset = (angleOffset + 2) % 4;
                                else if (side == EnumFacing.DOWN && first.getAxis() != second.getAxis())
                                    angleOffset = (angleOffset + 1) % 4;
                            }
                            
                            if (first.getAxis() != second.getAxis())
                                return ImmutableList.of(prebakedQuads.get(side.getIndex() * 24 + 8 + angleOffset));
                            else
                                return ImmutableList.of(prebakedQuads.get(side.getIndex() * 24 + 12 + angleOffset));
                        case 3:
                            single = null;
                            for (EnumFacing f : EnumFacing.VALUES) {
                                if (f.getAxis() != side.getAxis() && !connected.contains(f)) {
                                    single = f;
                                    break;
                                }
                            }
                            
                            if (single == null)
                                throw new NullPointerException("BUG: null missing side");
                            
                            angleOffset = 0;
                            if (side.getHorizontalIndex() != -1) {
                                while (single != EnumFacing.UP) {
                                    single = single.rotateAround(single.getAxis() == Axis.X ? Axis.Z : Axis.X);
                                    ++angleOffset;
                                }
                                
                                if (angleOffset % 2 != 0 && (side == EnumFacing.EAST || side == EnumFacing.SOUTH))
                                    angleOffset = (angleOffset + 2) % 4;
                            }
                            else {
                                EnumFacing orig = single;
                                while (single != EnumFacing.NORTH) {
                                    single = single.rotateY();
                                    ++angleOffset;
                                }
                                
                                if (side == EnumFacing.DOWN || orig.getAxis() == Axis.X)
                                    angleOffset = (angleOffset + 2) % 4;
                            }
                            
                            return ImmutableList.of(prebakedQuads.get(side.getIndex() * 24 + 16 + angleOffset));
                        case 4: return ImmutableList.of(prebakedQuads.get(side.getIndex() * 24 + 20));
                        default: break;
                    }
                }
            }
            
            return ImmutableList.of();
        }
        
        @Override
        public TextureAtlasSprite getParticleTexture() {
            return textures.apply(GlassTubeModel.TEXTURES.get(0));
        }
        
        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }
        
        @Override
        public boolean isAmbientOcclusion() {
            return false;
        }
        
        @Override
        public boolean isAmbientOcclusion(IBlockState state) {
            return false;
        }
        
        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }
        
        @Override
        public boolean isGui3d() {
            return false;
        }
        
    }
    
}
