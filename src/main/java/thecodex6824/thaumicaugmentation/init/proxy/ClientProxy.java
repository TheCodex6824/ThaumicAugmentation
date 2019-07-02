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

import java.util.Random;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.ICaster;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.client.fx.other.FXBlockWard;
import thaumcraft.common.items.casters.ItemFocus;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.item.IAssociatedAspect;
import thecodex6824.thaumicaugmentation.api.item.IDyeableItem;
import thecodex6824.thaumicaugmentation.api.warded.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.ClientWardStorageValue;
import thecodex6824.thaumicaugmentation.api.warded.IWardStorageClient;
import thecodex6824.thaumicaugmentation.client.renderer.ListeningAnimatedTESR;
import thecodex6824.thaumicaugmentation.client.renderer.RenderDimensionalFracture;
import thecodex6824.thaumicaugmentation.client.renderer.TARenderHelperClient;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager;
import thecodex6824.thaumicaugmentation.client.shader.TAShaders;
import thecodex6824.thaumicaugmentation.client.sound.ClientSoundHandler;
import thecodex6824.thaumicaugmentation.common.entity.EntityDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.item.ItemFractureLocator;
import thecodex6824.thaumicaugmentation.common.item.ItemKey;
import thecodex6824.thaumicaugmentation.common.network.PacketFullWardSync;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketWardUpdate;
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
    public void handleParticlePacket(PacketParticleEffect message) {
        Random rand = Minecraft.getMinecraft().world.rand;
        switch (message.getEffect()) {
            case VIS_REGENERATOR: {
                double[] d = message.getData();
                if (d.length == 3) {
                    for (int i = 0; i < rand.nextInt(3) + 3; ++i) {
                        double x = d[0] + rand.nextGaussian() / 4, y = d[1] + rand.nextDouble() / 2, z = d[2] + rand.nextGaussian() / 4;
                        double vX = rand.nextGaussian() / 4, vY = rand.nextDouble() / 2, vZ = rand.nextGaussian() / 4;
                        FXDispatcher.INSTANCE.drawVentParticles(x, y, z, vX, vY, vZ, Aspect.AURA.getColor());
                    }
                }
                
                break;
            }
            case VOID_STREAKS: {
                double[] d = message.getData();
                if (d.length == 7) {
                    double x1 = d[0], y1 = d[1], z1 = d[2];
                    double x2 = d[3], y2 = d[4], z2 = d[5];
                    float scale = (float) d[6];
                    FXDispatcher.INSTANCE.voidStreak(x1, y1, z1, x2, y2, z2, rand.nextInt(), scale);
                }
                
                break;
            }
            case WARD: {
                double[] d = message.getData();
                if (d.length == 7) {
                    double x = d[0], y = d[1], z = d[2];
                    int index = (int) d[3];
                    double hitX = d[4], hitY = d[5], hitZ = d[6];
                    FXBlockWard ward = new FXBlockWard(FXDispatcher.INSTANCE.getWorld(), x + 0.5, y + 0.5, z + 0.5, 
                            EnumFacing.byIndex(index), (float) hitX, (float) hitY, (float) hitZ);
                    ward.onUpdate();
                    ward.onUpdate();
                    FMLClientHandler.instance().getClient().effectRenderer.addEffect(ward);
                }
                
                break;
            }
            case POOF: {
                double[] d = message.getData();
                if (d.length == 4) {
                    double x = d[0], y = d[1], z = d[2];
                    int index = (int) d[3];
                    FXDispatcher.INSTANCE.drawBamf(new BlockPos(x, y, z), Aspect.PROTECT.getColor(), true, true,
                            EnumFacing.byIndex(index));
                }
                
                break;
            }
        }
    }
    
    @Override
    public void handleFullWardSyncPacket(PacketFullWardSync message) {
        NBTTagCompound tag = message.getTag();
        if (tag.getBoolean("o")) {
            World world = FMLClientHandler.instance().getClient().world;
            int chunkX = tag.getInteger("x"), chunkZ = tag.getInteger("z");
            if (world.isBlockLoaded(new BlockPos(chunkX << 4, 0, chunkZ << 4)) && world.getChunk(chunkX, chunkZ).hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                if (world.getChunk(chunkX, chunkZ).getCapability(CapabilityWardStorage.WARD_STORAGE, null) instanceof IWardStorageClient) {
                    IWardStorageClient storage = (IWardStorageClient) world.getChunk(chunkX, chunkZ).getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                    storage.deserializeNBT(tag);
                }
            }
        }
    }
    
    @Override
    public void handleWardUpdatePacket(PacketWardUpdate message) {
        BlockPos pos = new BlockPos(message.getX(), message.getY(), message.getZ());
        World world = FMLClientHandler.instance().getClient().world;
        if (world.isBlockLoaded(pos) && world.getChunk(pos).hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
            if (world.getChunk(pos).getCapability(CapabilityWardStorage.WARD_STORAGE, null) instanceof IWardStorageClient) {
                IWardStorageClient storage = (IWardStorageClient) world.getChunk(pos).getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                storage.setWard(pos, ClientWardStorageValue.fromID(message.getStatus()));
            }
        }
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
        
        IItemColor fractureLocatorColor = new IItemColor() {
            @Override
            public int colorMultiplier(ItemStack stack, int tintIndex) {
                if (tintIndex == 1 && stack.getItem() instanceof ItemFractureLocator)
                    return ((ItemFractureLocator) stack.getItem()).getTintColor(stack);
                
                return -1;
            }
        };
        registerTo.registerItemColorHandler(fractureLocatorColor, TAItems.FRACTURE_LOCATOR);
    }

}
