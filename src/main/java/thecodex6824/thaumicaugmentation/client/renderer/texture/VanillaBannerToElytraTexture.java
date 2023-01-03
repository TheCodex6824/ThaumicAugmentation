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

package thecodex6824.thaumicaugmentation.client.renderer.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.MinecraftForgeClient;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class VanillaBannerToElytraTexture extends AbstractTexture {

    protected static final ResourceLocation ELYTRA_TEXTURE = new ResourceLocation("minecraft", "textures/entity/elytra.png");
    
    protected ResourceLocation texture;
    protected List<ResourceLocation> patterns;
    protected List<EnumDyeColor> colors;
    
    public VanillaBannerToElytraTexture(ResourceLocation id, List<ResourceLocation> bannerPatterns, List<EnumDyeColor> bannerColors) {
        texture = id;
        patterns = bannerPatterns;
        colors = bannerColors;
    }
    
    @Override
    @SuppressWarnings("resource") // TextureUtil#readBufferedImage closes resource
    public void loadTexture(IResourceManager resourceManager) throws IOException {
        deleteGlTexture();
        try (IResource resource = resourceManager.getResource(texture)){
            BufferedImage banner = TextureUtil.readBufferedImage(resource.getInputStream());
            int type = banner.getType();
            if (type == BufferedImage.TYPE_CUSTOM)
                type = BufferedImage.TYPE_4BYTE_ABGR;
            
            BufferedImage temp = new BufferedImage(banner.getWidth(), banner.getHeight(), type);
            Graphics2D g2d = temp.createGraphics();
            g2d.drawImage(banner, 0, 0, null);
            for (int i = 0; i < Math.min(patterns.size(), colors.size()); ++i) {
                ResourceLocation currentTexture = patterns.get(i);
                int currentColor = colors.get(i).getColorValue();
                if (currentTexture != null) {
                    BufferedImage layer = MinecraftForgeClient.getImageLayer(currentTexture, resourceManager);
                    if (layer.getWidth() == temp.getWidth() && temp.getHeight() == banner.getHeight() && layer.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                        for (int y = 0; y < layer.getHeight(); ++y) {
                            for (int x = 0; x < layer.getWidth(); ++x) {
                                int color = layer.getRGB(x, y);
                                if ((color & -16777216) != 0) {
                                    int c = (color & 0xFF0000) << 8 & -16777216;
                                    int orig = banner.getRGB(x, y);
                                    int nC = MathHelper.multiplyColor(orig, currentColor) & 0xFFFFFF;
                                    layer.setRGB(x, y, c | nC);
                                }
                            }
                        }
                        
                        g2d.drawImage(layer, 0, 0, null);
                    }
                }
            }
            g2d.dispose();
            banner = new BufferedImage(temp.getWidth(), temp.getHeight(), type);
            g2d = banner.createGraphics();
            g2d.scale(-1.0, 1.0);
            g2d.translate(-temp.getWidth(), 0);
            g2d.drawImage(temp, 0, 0, null);
            g2d.dispose();
            try (IResource elytraResource = resourceManager.getResource(ELYTRA_TEXTURE)) {
                BufferedImage elytra = TextureUtil.readBufferedImage(elytraResource.getInputStream());
                BufferedImage cutout = new BufferedImage(banner.getWidth(), banner.getHeight(), banner.getType());
                Graphics2D cg2d = cutout.createGraphics();
                cg2d.scale(1.85, 1.85);
                cg2d.drawImage(elytra, -12, 0, null);
                cg2d.dispose();
                BufferedImage img = new BufferedImage(banner.getWidth(), banner.getHeight(), banner.getType());
                int lowestX = cutout.getWidth(), lowestY = cutout.getHeight();
                for (int y = 0; y < Math.min(img.getHeight(), cutout.getHeight()); ++y) {
                    for (int x = 0; x < Math.min(img.getWidth(), cutout.getWidth()); ++x) {
                        if ((cutout.getRGB(x, y) & 0xFF000000) != 0) {
                            if (x < lowestX)
                                lowestX = x;
                            if (y < lowestY)
                                lowestY = y;
                        }
                    }
                }
                for (int y = lowestY; y < cutout.getHeight(); ++y) {
                    for (int x = lowestX; x < cutout.getWidth(); ++x) {
                        if ((cutout.getRGB(x, y) & 0xFF000000) != 0)
                            img.setRGB(x - lowestX, y - lowestY, banner.getRGB(x, y));
                    }
                }
                
                // clean up processing artifacts from scaling
                int newWidth = (int) (elytra.getWidth() * (21.0 / 32.0));
                for (int y = 0; y < img.getHeight(); ++y) {
                    if ((img.getRGB(0, y) & 0xFF000000) != 0)
                        img.setRGB(0, y, img.getRGB(newWidth - 1, y));
                }
                for (int x = 0; x < img.getWidth(); ++x) {
                    img.setRGB(x, 1, 0);
                    img.setRGB(x, 2, 0);
                    img.setRGB(x, 3, 0);
                }
                
                BufferedImage complete = new BufferedImage(newWidth, (int) (elytra.getHeight() * 1.5), img.getType());
                cg2d = complete.createGraphics();
                cg2d.drawImage(img, 0, 0, null);
                cg2d.dispose();
                TextureUtil.uploadTextureImage(getGlTextureId(), complete);
            }
        }
    }
    
}
