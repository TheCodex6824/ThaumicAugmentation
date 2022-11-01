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
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ItemModelMesherForge;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.client.renderer.item.MorphicArmorWrappingTEISR;
import thecodex6824.thaumicaugmentation.client.renderer.item.MorphicWrappingTEISR;
import thecodex6824.thaumicaugmentation.common.util.MorphicArmorHelper;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class MorphicArmorBakedModel implements IBakedModel {
        
    protected static final HashSet<ResourceLocation> WARNED_ITEMS = new HashSet<>();
    
    protected IBakedModel wrappedFallback;
    protected ModelResourceLocation wrappedLoc;
    protected ItemOverrideList handler;
    
    public MorphicArmorBakedModel(IBakedModel wrappedModel) {
        wrappedFallback = wrappedModel;
        handler = new ItemOverrideList(ImmutableList.of()) {
            
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world,
                    @Nullable EntityLivingBase entity) {
                
                IBakedModel model = null;
                ItemStack disp = MorphicArmorHelper.getMorphicArmor(stack);
                if (disp.isEmpty()) {
                    model = wrappedFallback.getOverrides().handleItemState(wrappedFallback, stack, world, entity);
                    disp = stack;
                }
                else {
                    // this works because it will only ever get called if the
                    // original model in question was already builtin
                    // ones where it's not set will just wrap the vanilla handler, which is fine
                    if (!(stack.getItem().getTileEntityItemStackRenderer() instanceof MorphicWrappingTEISR))
                        stack.getItem().setTileEntityItemStackRenderer(new MorphicArmorWrappingTEISR(stack.getItem().getTileEntityItemStackRenderer()));
                    
                    model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(disp, world, entity);
                }
                
                // catch exceptions for items that don't like handleItemState being called a lot
                // example - Tinker's BakedToolModel
                try {
                    for (int i = 0; i < 10; ++i) {
                        IBakedModel next = model.getOverrides().handleItemState(model, disp, world, entity);
                        if (next == model)
                            return model;
                        else
                            model = next;
                    }
                }
                catch (Throwable ex) {
                    if (WARNED_ITEMS.add(stack.getItem().getRegistryName())) {
                        ThaumicAugmentation.getLogger().debug("Model for armor item {} threw an exception", stack.getItem().getRegistryName());
                        ThaumicAugmentation.getLogger().catching(Level.DEBUG, ex);
                    }
                
                    return model;
                }
                
                if (WARNED_ITEMS.add(stack.getItem().getRegistryName()))
                    ThaumicAugmentation.getLogger().debug("Model for armor item {} was too recursive, this might be a bug", stack.getItem().getRegistryName());
                
                return model;
            }
            
            @Override
            @Nullable
            @SuppressWarnings({"deprecation", "null"})
            public ResourceLocation applyOverride(ItemStack stack, @Nullable World world,
                    @Nullable EntityLivingBase entity) {
                
                ItemModelMesher m = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
                ItemStack disp = MorphicArmorHelper.getMorphicArmor(stack);
                if (!disp.isEmpty()) {
                    ResourceLocation r = m.getItemModel(disp).getOverrides().applyOverride(disp, world, entity);
                    if (r == null)
                        r = MiscModels.getOriginalArmorModel(((ItemModelMesherForge) m).getLocation(disp));
                    
                    return r;
                }
                else {
                    ResourceLocation r = wrappedFallback.getOverrides().applyOverride(stack, world, entity);
                    if (r == null)
                        r = MiscModels.getOriginalArmorModel(((ItemModelMesherForge) m).getLocation(stack));
                    
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
