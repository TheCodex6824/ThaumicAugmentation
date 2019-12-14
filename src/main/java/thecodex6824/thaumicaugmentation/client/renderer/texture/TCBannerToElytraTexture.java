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

package thecodex6824.thaumicaugmentation.client.renderer.texture;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import thaumcraft.api.aspects.Aspect;

public class TCBannerToElytraTexture extends AbstractTexture {

    protected static final ResourceLocation ELYTRA_TEXTURE = new ResourceLocation("minecraft", "textures/entity/elytra.png");
    
    protected ResourceLocation texture;
    protected Aspect aspect;
    protected int color;
    
    public TCBannerToElytraTexture(ResourceLocation tex) {
        this(tex, null, 0);
    }
    
    public TCBannerToElytraTexture(ResourceLocation tex, @Nullable Aspect symbol, int baseColor) {
        texture = tex;
        aspect = symbol;
        color = baseColor;
    }
    
    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {
        deleteGlTexture();
        try (IResource resource = resourceManager.getResource(texture)){
            BufferedImage banner = TextureUtil.readBufferedImage(resource.getInputStream());
            if ((color & 0x11FFFFFF) > 0) {
                for (int y = 0; y < banner.getHeight(); ++y) {
                    for (int x = 0; x < banner.getWidth(); ++x) {
                        int c = (color & 0xFF0000) << 8 & -16777216;
                        int orig = banner.getRGB(x, y);
                        int nC = MathHelper.multiplyColor(orig, color) & 0xFFFFFF;
                        banner.setRGB(x, y, c | nC | (orig & 0xFF000000));
                    }
                }
            }
            if (aspect != null) {
                try (IResource a = resourceManager.getResource(aspect.getImage())) {
                    BufferedImage as = TextureUtil.readBufferedImage(a.getInputStream());
                    BufferedImage temp = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D t2d = temp.createGraphics();
                    t2d.drawImage(as, 0, 0, 16, 16, null);
                    t2d.dispose();
                    for (int y = 0; y < temp.getHeight(); ++y) {
                        for (int x = 0; x < temp.getWidth(); ++x)
                            temp.setRGB(x, y, aspect.getColor() | (temp.getRGB(x, y) & 0xFF000000));
                    }
                    Graphics2D g2d = banner.createGraphics();
                    g2d.drawImage(temp, 38, 16, null);
                    g2d.dispose();
                }
            }
            
            try (IResource elytraResource = resourceManager.getResource(ELYTRA_TEXTURE)) {
                BufferedImage elytra = TextureUtil.readBufferedImage(elytraResource.getInputStream());
                BufferedImage cutout = new BufferedImage(banner.getWidth(), banner.getHeight(), banner.getType());
                Graphics2D cg2d = cutout.createGraphics();
                cg2d.setTransform(AffineTransform.getScaleInstance(2.0, 2.0));
                cg2d.drawImage(elytra, -18, 0, null);
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
                for (int y = 0; y < Math.min(img.getHeight(), cutout.getHeight()); ++y) {
                    for (int x = 0; x < Math.min(img.getWidth(), cutout.getWidth()); ++x) {
                        if ((cutout.getRGB(x, y) & 0xFF000000) != 0)
                            img.setRGB(x - lowestX, y - lowestY, banner.getRGB(x, y));
                    }
                }
                
                if (aspect == null) {
                    // copy a different part of the texture over the side
                    // without this the symbol will appear on the sides
                    Graphics2D g2d = img.createGraphics();
                    g2d.drawImage(img, 0, 21, 2, 44, 26, 4, 28, 26, null);
                    g2d.dispose();
                }
                
                BufferedImage complete = new BufferedImage((int) (elytra.getWidth() * 0.75), (int) (elytra.getHeight() * 1.5), img.getType());
                complete.createGraphics().drawImage(img, 0, 0, null);
                TextureUtil.uploadTextureImage(getGlTextureId(), complete);
            }
        }
    }
    
}
