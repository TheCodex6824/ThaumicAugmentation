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

package thecodex6824.thaumicaugmentation.init;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.ILightSourceBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IStarfieldGlassType;
import thecodex6824.thaumicaugmentation.api.block.property.ITABarsType;
import thecodex6824.thaumicaugmentation.api.block.property.ITASlabType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.client.renderer.AugmentRenderer;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public final class ModelRegistryHandler {

    private ModelRegistryHandler() {}
    
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {

        OBJLoader.INSTANCE.addDomain(ThaumicAugmentationAPI.MODID);
        
        ModelLoader.setCustomStateMapper(TABlocks.TEMPORARY_LIGHT, new StateMap.Builder().ignore(ILightSourceBlock.LIGHT_LEVEL).build());
        ModelLoader.setCustomStateMapper(TABlocks.STONE, new StateMap.Builder().withName(ITAStoneType.STONE_TYPE).build());
        ModelLoader.setCustomStateMapper(TABlocks.SLAB, new StateMap.Builder().withName(ITASlabType.SLAB_TYPE).ignore(ITASlabType.DOUBLE).withSuffix("_slab").build());
        ModelLoader.setCustomStateMapper(TABlocks.SLAB_DOUBLE, new StateMap.Builder().withName(ITASlabType.SLAB_TYPE).ignore(ITASlabType.DOUBLE).ignore(BlockSlab.HALF).withSuffix("_slab_double").build());
        ModelLoader.setCustomStateMapper(TABlocks.BARS, new StateMap.Builder().withName(ITABarsType.BARS_TYPE).build());
        ModelLoader.setCustomStateMapper(TABlocks.STARFIELD_GLASS, new StateMap.Builder().withName(IStarfieldGlassType.GLASS_TYPE).build());
        ModelLoader.setCustomStateMapper(TABlocks.STRANGE_CRYSTAL, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation("ta_special:directional_retexture:ta_special:obj_tinted:" + state.getBlock().getRegistryName(),
                        getPropertyString(state.getProperties()));
            }
        });
        
        for (Block b : TABlocks.getAllBlocks()) {
            if (b instanceof IModelProvider<?>)
                ((IModelProvider<?>) b).registerModels();
        }

        for (Item item : TAItems.getAllItems()) {
            if (item instanceof IModelProvider<?>)
                ((IModelProvider<?>) item).registerModels();
        }
        
        AugmentRenderer.loadModels();
    }

}
