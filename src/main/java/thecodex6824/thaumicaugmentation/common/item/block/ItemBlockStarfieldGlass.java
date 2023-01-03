/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.item.block;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMultiTexture;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IStarfieldGlassType;
import thecodex6824.thaumicaugmentation.api.block.property.IStarfieldGlassType.GlassType;
import thecodex6824.thaumicaugmentation.client.renderer.item.RenderItemBlockStarfieldGlass;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;

import java.util.stream.Collectors;

public class ItemBlockStarfieldGlass extends ItemMultiTexture implements IModelProvider<Item> {

    
    public ItemBlockStarfieldGlass() {
        super(TABlocks.STARFIELD_GLASS, null, IStarfieldGlassType.GLASS_TYPE.getAllowedValues().stream().map(
                IStarfieldGlassType.GLASS_TYPE::getName).collect(Collectors.toList()).toArray(new String[IStarfieldGlassType.GLASS_TYPE.getAllowedValues().size()]));
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        setTileEntityItemStackRenderer(new RenderItemBlockStarfieldGlass());
        for (GlassType type : GlassType.values()) {
            ModelLoader.setCustomModelResourceLocation(this, type.getMeta(), new ModelResourceLocation(
                    "ta_special:renderer_builtin:" + ThaumicAugmentationAPI.MODID + ":" + type.getName(), "inventory"));
        }
    }
    
}
