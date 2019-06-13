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

package thecodex6824.thaumicaugmentation.init.proxy;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import thaumcraft.api.casters.ICaster;
import thaumcraft.common.items.casters.ItemFocus;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.item.IAssociatedAspect;
import thecodex6824.thaumicaugmentation.api.item.IDyeableItem;
import thecodex6824.thaumicaugmentation.client.renderer.ListeningAnimatedTESR;
import thecodex6824.thaumicaugmentation.client.renderer.RenderDimensionalFracture;
import thecodex6824.thaumicaugmentation.client.renderer.TARenderHelperClient;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager;
import thecodex6824.thaumicaugmentation.client.shader.TAShaders;
import thecodex6824.thaumicaugmentation.client.sound.ClientSoundHandler;
import thecodex6824.thaumicaugmentation.common.entity.EntityDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.item.ItemKey;
import thecodex6824.thaumicaugmentation.common.tile.TileVisRegenerator;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedChest;
import thecodex6824.thaumicaugmentation.common.util.ITARenderHelper;

public class ClientProxy extends CommonProxy {

    @Override
    public IAnimationStateMachine loadASM(ResourceLocation loc, ImmutableMap<String, ITimeValue> params) {
        return ModelLoaderRegistry.loadASM(loc, params);
    }

    @Override
    public ITARenderHelper getRenderHelper() {
        if (renderHelper == null)
            renderHelper = new TARenderHelperClient();

        return renderHelper;
    }

    @Override
    public void preInit() {
        super.preInit();
        RenderingRegistry.registerEntityRenderingHandler(EntityDimensionalFracture.class, new IRenderFactory<EntityDimensionalFracture>() {
            @Override
            public Render<EntityDimensionalFracture> createRenderFor(RenderManager manager) {
                return new RenderDimensionalFracture(manager);
            }
        });
    }

    @Override
    public void init() {
        super.init();
        ClientSoundHandler.init();
        ClientRegistry.bindTileEntitySpecialRenderer(TileVisRegenerator.class, new ListeningAnimatedTESR<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileWardedChest.class, new ListeningAnimatedTESR<>());
        registerItemColorHandlers();
    }

    @Override
    public void postInit() {
        super.postInit();
        if (TAShaderManager.shouldUseShaders()) {
            TAShaders.FRACTURE = TAShaderManager.registerShader(new ResourceLocation(ThaumicAugmentationAPI.MODID, "fracture"));
            TAShaders.EMPTINESS_SKY = TAShaderManager.registerShader(new ResourceLocation(ThaumicAugmentationAPI.MODID, "emptiness_sky"));
        }
    }

    private static void registerItemColorHandlers() {
        ItemColors registerTo = Minecraft.getMinecraft().getItemColors();
        IItemColor casterFocusColors = new IItemColor() {
            @Override
            public int colorMultiplier(ItemStack stack, int tintIndex) {
                if (tintIndex == 1 && stack.getItem() instanceof ICaster && ((ICaster) stack.getItem()).getFocus(stack) != null)
                    return ((ItemFocus) ((ICaster) stack.getItem()).getFocus(stack)).getFocusColor(((ICaster) stack.getItem()).getFocusStack(stack));
                else if (tintIndex == 2 && stack.getItem() instanceof IDyeableItem)
                    return ((IDyeableItem) stack.getItem()).getDyedColor(stack);

                return -1;
            }
        };
        registerTo.registerItemColorHandler(casterFocusColors, TAItems.GAUNTLET);

        IItemColor keyIDColors = new IItemColor() {
            @Override
            public int colorMultiplier(ItemStack stack, int tintIndex) {
                if (tintIndex == 1 && stack.getItem() instanceof ItemKey)
                    return ((ItemKey) stack.getItem()).getKeyColor(stack);

                return -1;
            }
        };
        registerTo.registerItemColorHandler(keyIDColors, TAItems.KEY);

        IItemColor dyeableMisc = new IItemColor() {
            @Override
            public int colorMultiplier(ItemStack stack, int tintIndex) {
                if (tintIndex == 1 && stack.getItem() instanceof IDyeableItem)
                    return ((IDyeableItem) stack.getItem()).getDyedColor(stack);

                return -1;
            }
        };
        registerTo.registerItemColorHandler(dyeableMisc, TAItems.VOID_BOOTS);
        
        IItemColor elementalResonatorColor = new IItemColor() {
            @Override
            public int colorMultiplier(ItemStack stack, int tintIndex) {
                if (tintIndex == 1 && stack.getItem() instanceof IAssociatedAspect)
                    return ((IAssociatedAspect) stack.getItem()).getAspect(stack).getColor();
                
                return -1;
            }
        };
        registerTo.registerItemColorHandler(elementalResonatorColor, TAItems.AUGMENT_CASTER_ELEMENTAL);
    }

}
