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

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

public class ModelElytraBanner extends ModelBase {

    protected ModelRenderer rightWing;
    protected ModelRenderer leftWing;

    public ModelElytraBanner(float texScale, int texWidth, int texHeight) {
        textureWidth = texWidth;
        textureHeight = texHeight;
        leftWing = new ModelRenderer(this, 0, 0);
        leftWing.addBox(-10.0F, 0.0F, 0.0F, 10, 20, 2, 1.0F);
        rightWing = new ModelRenderer(this, 0, 0);
        rightWing.mirror = true;
        rightWing.addBox(0.0F, 0.0F, 0.0F, 10, 20, 2, 1.0F);
        // scale UVs to match scaling of texture
        for (ModelBox box : leftWing.cubeList) {
            for (TexturedQuad q : box.quadList) {
                for (int i = 0; i < q.vertexPositions.length; ++i) {
                    q.vertexPositions[i].texturePositionX *= texScale;
                    q.vertexPositions[i].texturePositionY *= texScale;
                }
            }
        }
        for (ModelBox box : rightWing.cubeList) {
            for (TexturedQuad q : box.quadList) {
                for (int i = 0; i < q.vertexPositions.length; ++i) {
                    q.vertexPositions[i].texturePositionX *= texScale;
                    q.vertexPositions[i].texturePositionY *= texScale;
                }
            }
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableCull();
        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isChild()) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0F, 1.5F, -0.1F);
            leftWing.render(scale);
            rightWing.render(scale);
            GlStateManager.popMatrix();
        }
        else {
            leftWing.render(scale);
            rightWing.render(scale);
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        float f = 0.2617994F;
        float f1 = -0.2617994F;
        float f2 = 0.0F;
        float f3 = 0.0F;
        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isElytraFlying()) {
            float f4 = 1.0F;

            if (entity.motionY < 0.0D) {
                Vec3d vec3d = (new Vec3d(entity.motionX, entity.motionY, entity.motionZ)).normalize();
                f4 = 1.0F - (float) Math.pow(-vec3d.y, 1.5D);
            }

            f = f4 * 0.34906584F + (1.0F - f4) * f;
            f1 = f4 * -((float) Math.PI / 2F) + (1.0F - f4) * f1;
        }
        else if (entity.isSneaking()) {
            f = ((float) Math.PI * 2F / 9F);
            f1 = -((float) Math.PI / 4F);
            f2 = 3.0F;
            f3 = 0.08726646F;
        }

        leftWing.rotationPointX = 5.0F;
        leftWing.rotationPointY = f2;
        if (entity instanceof AbstractClientPlayer) {
            AbstractClientPlayer abs = (AbstractClientPlayer) entity;
            abs.rotateElytraX = (float) ((double) abs.rotateElytraX + (double) (f - abs.rotateElytraX) * 0.1D);
            abs.rotateElytraY = (float) ((double) abs.rotateElytraY + (double) (f3 - abs.rotateElytraY) * 0.1D);
            abs.rotateElytraZ = (float) ((double) abs.rotateElytraZ + (double) (f1 - abs.rotateElytraZ) * 0.1D);
            leftWing.rotateAngleX = abs.rotateElytraX;
            leftWing.rotateAngleY = abs.rotateElytraY;
            leftWing.rotateAngleZ = abs.rotateElytraZ;
        }
        else {
            leftWing.rotateAngleX = f;
            leftWing.rotateAngleZ = f1;
            leftWing.rotateAngleY = f3;
        }

        rightWing.rotationPointX = -leftWing.rotationPointX;
        rightWing.rotateAngleY = -leftWing.rotateAngleY;
        rightWing.rotationPointY = leftWing.rotationPointY;
        rightWing.rotateAngleX = leftWing.rotateAngleX;
        rightWing.rotateAngleZ = -leftWing.rotateAngleZ;
    }
    
}
