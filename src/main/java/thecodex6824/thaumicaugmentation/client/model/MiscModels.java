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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public class MiscModels {

    protected static final ResourceLocation SHIELD_MODEL = new ResourceLocation(ThaumicAugmentationAPI.MODID, "block/impetus_gate_shield");
    protected static final ModelResourceLocation SHIELD_MODEL_LOC = new ModelResourceLocation(SHIELD_MODEL, "normal");
    protected static ModelManager manager;
    
    public static IBakedModel getImpetusGateShieldModel() {
        if (manager == null)
            manager = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager();
        
        return manager.getModel(SHIELD_MODEL_LOC);
    }
    
    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
        IModel shield = ModelLoaderRegistry.getModelOrMissing(SHIELD_MODEL);
        IBakedModel shieldModel = shield.bake(shield.getDefaultState(), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
        registry.putObject(SHIELD_MODEL_LOC, shieldModel);
        
        // prepare for suffering (aka looping over all items in the game)
        ModelLoader loader = event.getModelLoader();
        for (Item item : Item.REGISTRY) {
            if (item instanceof ItemArmor) {
                for (String s : loader.getVariantNames(item)) {
                    ModelResourceLocation model = ModelLoader.getInventoryVariant(s);
                    IBakedModel old = registry.getObject(model);
                    registry.putObject(model, new MorphicArmorBakedModel(old));
                }
            }
        }
    }
    
}
