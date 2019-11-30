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

package thecodex6824.thaumicaugmentation.client.gui;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.item.CapabilityBiomeSelector;
import thecodex6824.thaumicaugmentation.api.item.IBiomeSelector;
import thecodex6824.thaumicaugmentation.client.gui.component.ButtonSpinner;
import thecodex6824.thaumicaugmentation.common.container.ContainerArcaneTerraformer;
import thecodex6824.thaumicaugmentation.common.network.PacketInteractGUI;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneTerraformer;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeUtil;

public class GUIArcaneTerraformer extends GuiContainer {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/gui/arcane_terraformer.png");
    protected static final Cache<ItemStack, Object2IntAVLTreeMap<Aspect>> COSTS =
            CacheBuilder.newBuilder().maximumSize(25).concurrencyLevel(1).build();

    protected boolean buttonsDisabled;
    
    public GUIArcaneTerraformer(ContainerArcaneTerraformer c) {
        super(c);
        ySize = 176;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        ContainerArcaneTerraformer c = (ContainerArcaneTerraformer) inventorySlots;
        buttonList.add(new ButtonSpinner(0, guiLeft + xSize - 64, (this.height - this.ySize) / 2 + 27, 48) {
            @Override
            public String getLabel() {
                return new TextComponentTranslation("thaumicaugmentation.text.terraformer_radius").getFormattedText();
            }
            
            @Override
            public String getDisplayedValue() {
                return Integer.toString(c.getTile().getRadius());
            }
            
            @Override
            public void onDecrement() {
                c.getTile().setRadius(c.getTile().getRadius() - 1);
                sync();
                COSTS.invalidateAll();
            }
            
            @Override
            public void onIncrement() {
                c.getTile().setRadius(c.getTile().getRadius() + 1);
                sync();
                COSTS.invalidateAll();
            }
            
            protected void sync() {
                TANetwork.INSTANCE.sendToServer(new PacketInteractGUI(id, c.getTile().getRadius()));
            }
        });
        buttonList.add(new ButtonSpinner(1, guiLeft + xSize - 64, (this.height - this.ySize) / 2 + 57, 48) {
            @Override
            public String getLabel() {
                return new TextComponentTranslation("thaumicaugmentation.text.terraformer_circle_mode").getFormattedText();
            }
            
            @Override
            public String getDisplayedValue() {
                return c.getTile().isCircle() ? new TextComponentTranslation("thaumicaugmentation.text.terraformer_circle").getFormattedText() :
                    new TextComponentTranslation("thaumicaugmentation.text.terraformer_square").getFormattedText();
            }
            
            @Override
            public void onDecrement() {
                c.getTile().setCircle(!c.getTile().isCircle());
                sync();
                COSTS.invalidateAll();
            }
            
            @Override
            public void onIncrement() {
                c.getTile().setCircle(!c.getTile().isCircle());
                sync();
                COSTS.invalidateAll();
            }
            
            protected void sync() {
                TANetwork.INSTANCE.sendToServer(new PacketInteractGUI(id, c.getTile().isCircle() ? 1 : 0));
            }
        });
    }
    
    @Override
    public void updateScreen() {
        ContainerArcaneTerraformer c = (ContainerArcaneTerraformer) inventorySlots;
        if (c.getTile().isRunning() != buttonsDisabled) {
            buttonsDisabled = c.getTile().isRunning();
            for (GuiButton button : buttonList)
                button.enabled = !buttonsDisabled;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        int width = (this.width - this.xSize) / 2;
        int height = (this.height - this.ySize) / 2;
        drawTexturedModalRect(width, height, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        RenderHelper.disableStandardItemLighting();
        ContainerArcaneTerraformer container = (ContainerArcaneTerraformer) inventorySlots;
        TileArcaneTerraformer tile = container.getTile();
        if (container.getSlot(0).getHasStack()) {
            ItemStack stack = inventorySlots.getSlot(0).getStack();
            IBiomeSelector selected = stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null);
            if (selected != null && !selected.getBiomeID().equals(IBiomeSelector.EMPTY)) {
                Object2IntAVLTreeMap<Aspect> costs = null;
                try {
                    costs = COSTS.get(stack, () -> {
                        boolean reset = selected.getBiomeID().equals(IBiomeSelector.RESET);
                        Biome changeTo = reset ? BiomeUtil.getNaturalBiome(tile.getWorld(), tile.getPos(), Biomes.PLAINS) :
                                Biome.REGISTRY.getObject(selected.getBiomeID());
                        Object2IntAVLTreeMap<Aspect> map = new Object2IntAVLTreeMap<>((a1, a2) ->  {return a1.getName().compareTo(a2.getName());});
                        MutableBlockPos check = new MutableBlockPos(0, 0, 0);
                        int extra = 0;
                        for (int x = -tile.getRadius(); x < tile.getRadius(); ++x) {
                            for (int z = -tile.getRadius(); z < tile.getRadius(); ++z) {
                                if (!tile.isCircle() || x * x + z * z < tile.getRadius() * tile.getRadius()) {
                                    check.setPos(tile.getPos().getX() + x, tile.getPos().getY(), tile.getPos().getZ() + z);
                                    if (reset)
                                        changeTo = BiomeUtil.getNaturalBiome(tile.getWorld(), check, Biomes.PLAINS);
                                    if (!BiomeUtil.areBiomesSame(tile.getWorld(), check, changeTo)) {
                                        for (BiomeDictionary.Type type : BiomeDictionary.getTypes(changeTo)) {
                                            Aspect aspect = BiomeUtil.getAspectForType(type, Aspect.EXCHANGE);
                                            if (aspect != null) {
                                                if (aspect == Aspect.ORDER || aspect == Aspect.ENTROPY)
                                                    map.addTo(Aspect.EXCHANGE, 1);
                                                else if (aspect.isPrimal() || aspect == Aspect.EXCHANGE)
                                                    map.addTo(aspect, 1);
                                            }
                                        }
                                        
                                        ++extra;
                                    }
                                }
                            }
                        }
                        
                        if (extra > 0)
                            map.addTo(Aspect.EXCHANGE, extra);
                        
                        return map;
                    });
                }
                catch (ExecutionException ex) {
                    ThaumicAugmentation.getLogger().error("Something threw an exception when it really should not have!");
                    throw new RuntimeException(ex);
                }
                
                if (!costs.isEmpty()) {
                    GlStateManager.enableBlend();
                    int drawX = 14;
                    int drawY = 14;
                    for (Map.Entry<Aspect, Integer> entry : costs.entrySet()) {
                        UtilsFX.drawTag(drawX, drawY, entry.getKey(), entry.getValue(), 0, 0.0);
                        drawX += 17;
                        if (drawX > 31) {
                            drawX = 14;
                            drawY += 17;
                        }
                    }
                    GlStateManager.disableBlend();
                }
            }
        }
        
        for (GuiButton button : buttonList) {
            if (button.isMouseOver()) {
                button.drawButtonForegroundLayer(mouseX - guiLeft, mouseY - guiTop);
                break;
            }
        }
        
        RenderHelper.enableStandardItemLighting();
    }

}
