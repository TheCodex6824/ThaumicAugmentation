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
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

public class ModelTARobes extends ModelBiped {

    protected ModelRenderer hood1;
    protected ModelRenderer hood2;
    protected ModelRenderer hood3;
    protected ModelRenderer hood4;
    protected ModelRenderer chestThing;
    protected ModelRenderer mBelt;
    protected ModelRenderer mBeltB;
    protected ModelRenderer chestL;
    protected ModelRenderer chestR;
    protected ModelRenderer book;
    protected ModelRenderer scroll;
    protected ModelRenderer beltR;
    protected ModelRenderer backplate;
    protected ModelRenderer mBeltL;
    protected ModelRenderer mBeltR;
    protected ModelRenderer beltL;
    protected ModelRenderer chestplate;
    protected ModelRenderer shoulderPlateR1;
    protected ModelRenderer shoulderPlateR2;
    protected ModelRenderer shoulderPlateR3;
    protected ModelRenderer shoulderPlateTopR;
    protected ModelRenderer armR1;
    protected ModelRenderer armR2;
    protected ModelRenderer armR3;
    protected ModelRenderer armL1;
    protected ModelRenderer armL2;
    protected ModelRenderer armL3;
    protected ModelRenderer shoulderPlateL1;
    protected ModelRenderer shoulderPlateL2;
    protected ModelRenderer shoulderPlateL3;
    protected ModelRenderer shoulderPlateTopL;
    protected ModelRenderer shoulderL;
    protected ModelRenderer shoulderR;
    protected ModelRenderer frontClothR1;
    protected ModelRenderer frontClothR2;
    protected ModelRenderer sideClothR1;
    protected ModelRenderer sideClothR2;
    protected ModelRenderer sideClothR3;
    protected ModelRenderer backClothR1;
    protected ModelRenderer backClothR2;
    protected ModelRenderer backClothR3;
    protected ModelRenderer sidePanelR1;
    protected ModelRenderer legPanelR1;
    protected ModelRenderer legPanelR2;
    protected ModelRenderer legPanelR3;
    protected ModelRenderer frontClothL1;
    protected ModelRenderer frontClothL2;
    protected ModelRenderer backClothL1;
    protected ModelRenderer backClothL2;
    protected ModelRenderer backClothL3;
    protected ModelRenderer sideClothL1;
    protected ModelRenderer sideClothL2;
    protected ModelRenderer sideClothL3;
    protected ModelRenderer fociPouch;
    protected ModelRenderer legPanelL1;
    protected ModelRenderer legPanelL2;
    protected ModelRenderer legPanelL3;
    protected ModelRenderer sidePanelL1;
    
    protected boolean leftLess = false;
    
    public ModelTARobes(float scale) {
        super(scale, 0, 128, 64);
        
        hood1 = new ModelRenderer(this, 16, 7);
        hood1.addBox(-4.5F, -9.0F, -4.6F, 9, 9, 9);
        hood1.setTextureSize(128, 64);
        setRotation(hood1, 0.0F, 0.0F, 0.0F);
        hood2 = new ModelRenderer(this, 52, 13);
        hood2.addBox(-4.0F, -9.7F, 2.0F, 8, 9, 3);
        hood2.setTextureSize(128, 64);
        setRotation(hood2, -0.2268928F, 0.0F, 0.0F);
        hood3 = new ModelRenderer(this, 52, 14);
        hood3.addBox(-3.5F, -10.0F, 3.5F, 7, 8, 3);
        hood3.setTextureSize(128, 64);
        setRotation(hood3, -0.3490659F, 0.0F, 0.0F);
        hood4 = new ModelRenderer(this, 53, 15);
        hood4.addBox(-3.0F, -10.7F, 3.5F, 6, 7, 3);
        hood4.setTextureSize(128, 64);
        setRotation(hood4, -0.5759587F, 0.0F, 0.0F);
        chestThing = new ModelRenderer(this, 56, 50);
        chestThing.addBox(-2.5F, 1.0F, -4.0F, 5, 7, 1);
        chestThing.setTextureSize(128, 64);
        setRotation(chestThing, 0.0F, 0.0F, 0.0F);
        mBelt = new ModelRenderer(this, 16, 55);
        mBelt.addBox(-4.0F, 7.0F, -3.0F, 8, 5, 1);
        mBelt.setTextureSize(128, 64);
        setRotation(mBelt, 0.0F, 0.0F, 0.0F);
        mBeltB = new ModelRenderer(this, 16, 55);
        mBeltB.addBox(-4.0F, 7.0F, -4.0F, 8, 5, 1);
        mBeltB.setTextureSize(128, 64);
        setRotation(mBeltB, 0.0F, 3.141593F, 0.0F);
        chestL = new ModelRenderer(this, 108, 38);
        chestL.mirror = true;
        chestL.addBox(2.1F, 0.5F, -3.5F, 2, 8, 1);
        chestL.setTextureSize(128, 64);
        setRotation(chestL, 0.0F, 0.0F, 0.0F);
        chestR = new ModelRenderer(this, 108, 38);
        chestR.addBox(-4.1F, 0.5F, -3.5F, 2, 8, 1);
        chestR.setTextureSize(128, 64);
        setRotation(chestR, 0.0F, 0.0F, 0.0F);
        book = new ModelRenderer(this, 81, 16);
        book.addBox(1.0F, 0.0F, 4.0F, 5, 7, 2);
        book.setTextureSize(128, 64);
        setRotation(book, 0.0F, 0.0F, 0.7679449F);
        scroll = new ModelRenderer(this, 78, 25);
        scroll.addBox(-2.0F, 9.5F, 4.0F, 8, 3, 3);
        scroll.setTextureSize(128, 64);
        setRotation(scroll, 0.0F, 0.0F, 0.1919862F);
        beltR = new ModelRenderer(this, 16, 36);
        beltR.addBox(-5.0F, 4.0F, -3.0F, 1, 3, 6);
        beltR.setTextureSize(128, 64);
        setRotation(beltR, 0.0F, 0.0F, 0.0F);
        backplate = new ModelRenderer(this, 36, 45);
        backplate.addBox(-4.0F, 1.0F, 1.9F, 8, 11, 2);
        backplate.setTextureSize(128, 64);
        setRotation(this.backplate, 0.0F, 0.0F, 0.0F);
        mBeltL = new ModelRenderer(this, 16, 36);
        mBeltL.addBox(4.0F, 8.0F, -3.0F, 1, 3, 6);
        mBeltL.setTextureSize(128, 64);
        setRotation(mBeltL, 0.0F, 0.0F, 0.0F);
        mBeltR = new ModelRenderer(this, 16, 36);
        mBeltR.addBox(-5.0F, 8.0F, -3.0F, 1, 3, 6);
        mBeltR.setTextureSize(128, 64);
        setRotation(mBeltR, 0.0F, 0.0F, 0.0F);
        beltL = new ModelRenderer(this, 16, 36);
        beltL.addBox(4.0F, 4.0F, -3.0F, 1, 3, 6);
        beltL.setTextureSize(128, 64);
        setRotation(beltL, 0.0F, 0.0F, 0.0F);
        chestplate = new ModelRenderer(this, 16, 25);
        chestplate.addBox(-4.0F, 1.0F, -3.0F, 8, 6, 1);
        chestplate.setTextureSize(128, 64);
        setRotation(chestplate, 0.0F, 0.0F, 0.0F);
        shoulderPlateR1 = new ModelRenderer(this, 56, 33);
        shoulderPlateR1.addBox(-4.5F, -1.5F, -3.5F, 1, 4, 7);
        shoulderPlateR1.setTextureSize(128, 64);
        setRotation(shoulderPlateR1, 0.0F, 0.0F, 0.4363323F);
        shoulderPlateR2 = new ModelRenderer(this, 40, 33);
        shoulderPlateR2.addBox(-3.5F, 1.5F, -3.5F, 1, 3, 7);
        shoulderPlateR2.setTextureSize(128, 64);
        setRotation(shoulderPlateR2, 0.0F, 0.0F, 0.4363323F);
        shoulderPlateR3 = new ModelRenderer(this, 40, 33);
        shoulderPlateR3.addBox(-2.5F, 3.5F, -3.5F, 1, 3, 7);
        shoulderPlateR3.setTextureSize(128, 64);
        setRotation(shoulderPlateR3, 0.0F, 0.0F, 0.4363323F);
        shoulderPlateTopR = new ModelRenderer(this, 56, 25);
        shoulderPlateTopR.addBox(-5.5F, -2.5F, -3.5F, 2, 1, 7);
        shoulderPlateTopR.setTextureSize(128, 64);
        setRotation(shoulderPlateTopR, 0.0F, 0.0F, 0.4363323F);
        armR1 = new ModelRenderer(this, 88, 39);
        armR1.addBox(-3.5F, 2.5F, -2.5F, 5, 7, 5);
        armR1.setTextureSize(128, 64);
        setRotation(armR1, 0.0F, 0.0F, 0.0F);
        armR2 = new ModelRenderer(this, 76, 32);
        armR2.addBox(-3.0F, 5.5F, 2.5F, 4, 4, 2);
        armR2.setTextureSize(128, 64);
        setRotation(armR2, 0.0F, 0.0F, 0.0F);
        armR3 = new ModelRenderer(this, 88, 32);
        armR3.addBox(-2.5F, 3.5F, 2.5F, 3, 2, 1);
        armR3.setTextureSize(128, 64);
        setRotation(armR3, 0.0F, 0.0F, 0.0F);
        shoulderPlateL1 = new ModelRenderer(this, 56, 33);
        shoulderPlateL1.addBox(3.5F, -1.5F, -3.5F, 1, 4, 7);
        shoulderPlateL1.setTextureSize(128, 64);
        setRotation(shoulderPlateL1, 0.0F, 0.0F, -0.4363323F);
        shoulderPlateL2 = new ModelRenderer(this, 40, 33);
        shoulderPlateL2.addBox(2.5F, 1.5F, -3.5F, 1, 3, 7);
        shoulderPlateL2.setTextureSize(128, 64);
        setRotation(shoulderPlateL2, 0.0F, 0.0F, -0.4363323F);
        shoulderPlateL3 = new ModelRenderer(this, 40, 33);
        shoulderPlateL3.addBox(1.5F, 3.5F, -3.5F, 1, 3, 7);
        shoulderPlateL3.setTextureSize(128, 64);
        setRotation(shoulderPlateL3, 0.0F, 0.0F, -0.4363323F);
        shoulderPlateTopL = new ModelRenderer(this, 56, 25);
        shoulderPlateTopL.addBox(3.5F, -2.5F, -3.5F, 2, 1, 7);
        shoulderPlateTopL.setTextureSize(128, 64);
        setRotation(shoulderPlateTopL, 0.0F, 0.0F, -0.4363323F);
        shoulderR = new ModelRenderer(this, 16, 45);
        shoulderR.mirror = true;
        shoulderR.addBox(-3.5F, -2.5F, -2.5F, 5, 5, 5);
        shoulderR.setTextureSize(128, 64);
        setRotation(shoulderR, 0.0F, 0.0F, 0.0F);
        armL1 = new ModelRenderer(this, 88, 39);
        armL1.mirror = true;
        armL1.addBox(-1.5F, 2.5F, -2.5F, 5, 7, 5);
        armL1.setTextureSize(128, 64);
        setRotation(armL1, 0.0F, 0.0F, 0.0F);
        armL2 = new ModelRenderer(this, 76, 32);
        armL2.addBox(-1.0F, 5.5F, 2.5F, 4, 4, 2);
        armL2.setTextureSize(128, 64);
        setRotation(armL2, 0.0F, 0.0F, 0.0F);
        armL3 = new ModelRenderer(this, 88, 32);
        armL3.addBox(-0.5F, 3.5F, 2.5F, 3, 2, 1);
        armL3.setTextureSize(128, 64);
        setRotation(armL3, 0.0F, 0.0F, 0.0F);
        shoulderL = new ModelRenderer(this, 16, 45);
        shoulderL.addBox(-1.5F, -2.5F, -2.5F, 5, 5, 5);
        shoulderL.setTextureSize(128, 64);
        shoulderL.mirror = true;
        setRotation(shoulderL, 0.0F, 0.0F, 0.0F);
        frontClothR1 = new ModelRenderer(this, 108, 38);
        frontClothR1.addBox(2.0F, -11.5F, -1.25F, 3, 8, 1);
        frontClothR1.setRotationPoint(-3.0F, 11.0F, -2.9F);
        frontClothR1.setTextureSize(128, 64);
        setRotation(frontClothR1, -0.1047198F, 0.0F, 0.0F);
        frontClothR2 = new ModelRenderer(this, 108, 47);
        frontClothR2.addBox(2.0F, -3.25F, -2.25F, 3, 3, 1);
        frontClothR2.setRotationPoint(-3.0F, 11.0F, -2.9F);
        frontClothR2.setTextureSize(128, 64);
        setRotation(frontClothR2, -0.3316126F, 0.0F, 0.0F);
        frontClothL1 = new ModelRenderer(this, 108, 38);
        frontClothL1.mirror = true;
        frontClothL1.addBox(-2.0F, -11.5F, -1.25F, 3, 8, 1);
        frontClothL1.setRotationPoint(0.0F, 11.0F, -2.9F);
        frontClothL1.setTextureSize(128, 64);
        setRotation(frontClothL1, -0.1047198F, 0.0F, 0.0F);
        frontClothL2 = new ModelRenderer(this, 108, 47);
        frontClothL2.mirror = true;
        frontClothL2.addBox(-2.0F, -3.25F, -2.25F, 3, 3, 1);
        frontClothL2.setRotationPoint(0.0F, 11.0F, -2.9F);
        frontClothL2.setTextureSize(128, 64);
        setRotation(frontClothL2, -0.3316126F, 0.0F, 0.0F);
        backClothR1 = new ModelRenderer(this, 118, 16);
        backClothR1.mirror = true;
        backClothR1.addBox(2.0F, -12.0F, 0.0F, 4, 8, 1);
        backClothR1.setRotationPoint(-4.0F, 11.5F, 2.9F);
        backClothR1.setTextureSize(128, 64);
        setRotation(backClothR1, 0.1047198F, 0.0F, 0.0F);
        backClothR2 = new ModelRenderer(this, 123, 9);
        backClothR2.addBox(2.0F, -4.0F, 0.48F, 1, 2, 1);
        backClothR2.setRotationPoint(-4.0F, 11.5F, 2.9F);
        backClothR2.setTextureSize(128, 64);
        setRotation(backClothR2, 0.2268928F, 0.0F, 0.0F);
        backClothR3 = new ModelRenderer(this, 120, 12);
        backClothR3.mirror = true;
        backClothR3.addBox(3.0F, -4.0F, 0.48F, 3, 3, 1);
        backClothR3.setRotationPoint(-4.0F, 11.5F, 2.9F);
        backClothR3.setTextureSize(128, 64);
        setRotation(backClothR3, 0.2268928F, 0.0F, 0.0F);
        backClothL1 = new ModelRenderer(this, 118, 16);
        backClothL1.addBox(-2.0F, -12.0F, 0.0F, 4, 8, 1);
        backClothL1.setRotationPoint(0.0F, 11.5F, 2.9F);
        backClothL1.setTextureSize(128, 64);
        setRotation(backClothL1, 0.1047198F, 0.0F, 0.0F);
        backClothL2 = new ModelRenderer(this, 123, 9);
        backClothL2.mirror = true;
        backClothL2.addBox(1.0F, -4.0F, 0.48F, 1, 2, 1);
        backClothL2.setRotationPoint(0.0F, 11.5F, 2.9F);
        backClothL2.setTextureSize(128, 64);
        setRotation(backClothL2, 0.2268928F, 0.0F, 0.0F);
        backClothL3 = new ModelRenderer(this, 120, 12);
        backClothL3.addBox(-2.0F, -4.0F, 0.48F, 3, 3, 1);
        backClothL3.setRotationPoint(0.0F, 11.5F, 2.9F);
        backClothL3.setTextureSize(128, 64);
        setRotation(backClothL3, 0.2268928F, 0.0F, 0.0F);
        sideClothL1 = new ModelRenderer(this, 116, 42);
        sideClothL1.addBox(1.5F, 0.5F, -2.5F, 1, 5, 5);
        sideClothL1.setTextureSize(128, 64);
        setRotation(sideClothL1, 0.0F, 0.0F, -0.122173F);
        sideClothL2 = new ModelRenderer(this, 116, 34);
        sideClothL2.addBox(0.5F, 5.5F, -2.5F, 1, 3, 5);
        sideClothL2.setTextureSize(128, 64);
        setRotation(sideClothL2, 0.0F, 0.0F, -0.296706F);
        sideClothL3 = new ModelRenderer(this, 116, 1);
        sideClothL3.addBox(-1.4F, 8.4F, -2.5F, 1, 3, 5);
        sideClothL3.setTextureSize(128, 64);
        setRotation(sideClothL3, 0.0F, 0.0F, -0.5235988F);
        sideClothR1 = new ModelRenderer(this, 116, 42);
        sideClothR1.addBox(-2.5F, 0.5F, -2.5F, 1, 5, 5);
        sideClothR1.setTextureSize(128, 64);
        setRotation(sideClothR1, 0.0F, 0.0F, 0.122173F);
        sideClothR2 = new ModelRenderer(this, 116, 34);
        sideClothR2.addBox(-1.5F, 5.5F, -2.5F, 1, 3, 5);
        sideClothR2.setTextureSize(128, 64);
        setRotation(sideClothR2, 0.0F, 0.0F, 0.296706F);
        sideClothR3 = new ModelRenderer(this, 116, 1);
        sideClothR3.addBox(0.4F, 8.4F, -2.5F, 1, 3, 5);
        sideClothR3.setTextureSize(128, 64);
        setRotation(sideClothR3, 0.0F, 0.0F, 0.5235988F);
        sidePanelR1 = new ModelRenderer(this, 116, 25);
        sidePanelR1.addBox(-2.5F, 0.5F, -2.5F, 1, 4, 5);
        sidePanelR1.setTextureSize(128, 64);
        sidePanelR1.mirror = true;
        setRotation(sidePanelR1, 0.0F, 0.0F, 0.4363323F);
        sidePanelL1 = new ModelRenderer(this, 116, 25);
        sidePanelL1.addBox(1.5F, 0.5F, -2.5F, 1, 4, 5);
        sidePanelL1.setTextureSize(128, 64);
        setRotation(sidePanelL1, 0.0F, 0.0F, -0.4363323F);
        legPanelR1 = new ModelRenderer(this, 76, 38);
        legPanelR1.addBox(-3.0F, 0.5F, -3.5F, 2, 3, 1);
        legPanelR1.setTextureSize(128, 64);
        setRotation(legPanelR1, -0.4363323F, 0.0F, 0.0F);
        legPanelR2 = new ModelRenderer(this, 76, 42);
        legPanelR2.addBox(-3.0F, 2.5F, -2.5F, 2, 3, 1);
        legPanelR2.setTextureSize(128, 64);
        setRotation(legPanelR2, -0.4363323F, 0.0F, 0.0F);
        legPanelR3 = new ModelRenderer(this, 82, 38);
        legPanelR3.addBox(-3.0F, 4.5F, -1.5F, 2, 3, 1);
        legPanelR3.setTextureSize(128, 64);
        setRotation(legPanelR3, -0.4363323F, 0.0F, 0.0F);
        legPanelL1 = new ModelRenderer(this, 76, 38);
        legPanelL1.mirror = true;
        legPanelL1.addBox(1.0F, 0.5F, -3.5F, 2, 3, 1);
        legPanelL1.setTextureSize(128, 64);
        setRotation(legPanelL1, -0.4363323F, 0.0F, 0.0F);
        legPanelL2 = new ModelRenderer(this, 76, 42);
        legPanelL2.mirror = true;
        legPanelL2.addBox(1.0F, 2.5F, -2.5F, 2, 3, 1);
        legPanelL2.setTextureSize(128, 64);
        setRotation(legPanelL2, -0.4363323F, 0.0F, 0.0F);
        legPanelL3 = new ModelRenderer(this, 82, 38);
        legPanelL3.mirror = true;
        legPanelL3.addBox(1.0F, 4.5F, -1.5F, 2, 3, 1);
        legPanelL3.setTextureSize(128, 64);
        setRotation(legPanelL3, -0.4363323F, 0.0F, 0.0F);
        fociPouch = new ModelRenderer(this, 100, 20);
        fociPouch.addBox(3.5F, 0.5F, -2.5F, 3, 6, 5);
        fociPouch.setTextureSize(128, 64);
        setRotation(fociPouch, 0.0F, 0.0F, -0.122173F);
        
        bipedHeadwear.cubeList.clear();
        bipedHead.cubeList.clear();
        bipedHead.addChild(hood1);
        bipedHead.addChild(hood2);
        bipedHead.addChild(hood3);
        bipedHead.addChild(hood4);
        bipedBody.cubeList.clear();
        bipedBody.addChild(mBelt);
        bipedBody.addChild(mBeltB);
        bipedBody.addChild(mBeltL);
        bipedBody.addChild(mBeltR);
        bipedLeftLeg.cubeList.clear();
        if (scale < 1.0F) {
            bipedLeftLeg.addChild(fociPouch);
            bipedRightLeg.addChild(frontClothR1);
            bipedRightLeg.addChild(frontClothR2);
            bipedRightLeg.addChild(frontClothL1);
            bipedRightLeg.addChild(frontClothL2);
            frontClothL1.offsetX = 0.235F;
            frontClothL2.offsetX = 0.235F;
            bipedLeftLeg.addChild(backClothR1);
            bipedLeftLeg.addChild(backClothR2);
            bipedLeftLeg.addChild(backClothR3);
            bipedLeftLeg.addChild(backClothL1);
            bipedLeftLeg.addChild(backClothL2);
            bipedLeftLeg.addChild(backClothL3);
            backClothR1.offsetX = -0.235F;
            backClothR2.offsetX = -0.235F;
            backClothR3.offsetX = -0.235F;
        }
        else {
            bipedBody.addChild(chestplate);
            bipedBody.addChild(chestThing);
            bipedBody.addChild(scroll);
            bipedBody.addChild(backplate);
            bipedBody.addChild(book);
            bipedBody.addChild(chestL);
            bipedBody.addChild(chestR);
        } 
        
        bipedRightArm.cubeList.clear();
        bipedRightArm.addChild(shoulderR);
        bipedRightArm.addChild(armR1);
        bipedRightArm.addChild(armR2);
        bipedRightArm.addChild(armR3);
        bipedRightArm.addChild(shoulderPlateTopR);
        bipedRightArm.addChild(shoulderPlateR1);
        bipedRightArm.addChild(shoulderPlateR2);
        bipedRightArm.addChild(shoulderPlateR3);
        bipedLeftArm.cubeList.clear();
        bipedLeftArm.addChild(shoulderL);
        bipedLeftArm.addChild(armL1);
        bipedLeftArm.addChild(armL2);
        bipedLeftArm.addChild(armL3);
        bipedLeftArm.addChild(shoulderPlateTopL);
        bipedLeftArm.addChild(shoulderPlateL1);
        bipedLeftArm.addChild(shoulderPlateL2);
        bipedLeftArm.addChild(shoulderPlateL3);
        bipedRightLeg.cubeList.clear();
        bipedRightLeg.addChild(legPanelR1);
        bipedRightLeg.addChild(legPanelR2);
        bipedRightLeg.addChild(legPanelR3);
        bipedRightLeg.addChild(sidePanelR1);
        bipedRightLeg.addChild(sideClothR1);
        bipedRightLeg.addChild(sideClothR2);
        bipedRightLeg.addChild(sideClothR3);
        bipedLeftLeg.addChild(legPanelL1);
        bipedLeftLeg.addChild(legPanelL2);
        bipedLeftLeg.addChild(legPanelL3);
        bipedLeftLeg.addChild(sidePanelL1);
        bipedLeftLeg.addChild(sideClothL1);
        bipedLeftLeg.addChild(sideClothL2);
        bipedLeftLeg.addChild(sideClothL3);
    }
    
    protected void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
    
    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entity) {
        
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            swingProgress = living.getSwingProgress(Minecraft.getMinecraft().getRenderPartialTicks());
            isSneak = living.isSneaking();
            isRiding = living.isRiding();
            isChild = living.isChild();
            ItemStack mainHand = living.getHeldItemMainhand();
            ItemStack offHand = living.getHeldItemOffhand();
            ArmPose mainPose = ArmPose.EMPTY;
            ArmPose offPose = ArmPose.EMPTY;
            if (!mainHand.isEmpty()) {
                mainPose = ModelBiped.ArmPose.ITEM;
                if (living.getItemInUseCount() > 0) {
                    EnumAction action = mainHand.getItemUseAction();
                    if (action == EnumAction.BLOCK)
                        mainPose = ArmPose.BLOCK;
                    else if (action == EnumAction.BOW)
                        mainPose = ArmPose.BOW_AND_ARROW;
                } 
            } 
            if (!offHand.isEmpty()) {
                offPose = ModelBiped.ArmPose.ITEM;
                if (living.getItemInUseCount() > 0) {
                    EnumAction action = offHand.getItemUseAction();
                    if (action == EnumAction.BLOCK)
                        offPose = ArmPose.BLOCK;
                    else if (action == EnumAction.BOW)
                        offPose = ArmPose.BOW_AND_ARROW;
                } 
            }
            
            if (living.getPrimaryHand() == EnumHandSide.RIGHT) {
                rightArmPose = mainPose;
                leftArmPose = offPose;
            }
            else {
                rightArmPose = offPose;
                leftArmPose = mainPose;
            } 
        }
        
        if (entity instanceof EntityArmorStand) {
            EntityArmorStand stand = (EntityArmorStand) entity;
            bipedHead.rotateAngleX = 0.017453292F * stand.getHeadRotation().getX();
            bipedHead.rotateAngleY = 0.017453292F * stand.getHeadRotation().getY();
            bipedHead.rotateAngleZ = 0.017453292F * stand.getHeadRotation().getZ();
            bipedHead.setRotationPoint(0.0F, 1.0F, 0.0F);
            bipedBody.rotateAngleX = 0.017453292F * stand.getBodyRotation().getX();
            bipedBody.rotateAngleY = 0.017453292F * stand.getBodyRotation().getY();
            bipedBody.rotateAngleZ = 0.017453292F * stand.getBodyRotation().getZ();
            bipedLeftArm.rotateAngleX = 0.017453292F * stand.getLeftArmRotation().getX();
            bipedLeftArm.rotateAngleY = 0.017453292F * stand.getLeftArmRotation().getY();
            bipedLeftArm.rotateAngleZ = 0.017453292F * stand.getLeftArmRotation().getZ();
            bipedRightArm.rotateAngleX = 0.017453292F * stand.getRightArmRotation().getX();
            bipedRightArm.rotateAngleY = 0.017453292F * stand.getRightArmRotation().getY();
            bipedRightArm.rotateAngleZ = 0.017453292F * stand.getRightArmRotation().getZ();
            bipedLeftLeg.rotateAngleX = 0.017453292F * stand.getLeftLegRotation().getX();
            bipedLeftLeg.rotateAngleY = 0.017453292F * stand.getLeftLegRotation().getY();
            bipedLeftLeg.rotateAngleZ = 0.017453292F * stand.getLeftLegRotation().getZ();
            bipedLeftLeg.setRotationPoint(1.9F, 11.0F, 0.0F);
            bipedRightLeg.rotateAngleX = 0.017453292F * stand.getRightLegRotation().getX();
            bipedRightLeg.rotateAngleY = 0.017453292F * stand.getRightLegRotation().getY();
            bipedRightLeg.rotateAngleZ = 0.017453292F * stand.getRightLegRotation().getZ();
            bipedRightLeg.setRotationPoint(-1.9F, 11.0F, 0.0F);
            copyModelAngles(bipedHead, bipedHeadwear);
        }
        else
            super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
    
        // dynamically reparent robe leg parts to make front/back bit be connected
        if (!leftLess && bipedLeftLeg.rotateAngleX < bipedRightLeg.rotateAngleX) {
            leftLess = true;
            bipedRightLeg.childModels.remove(frontClothR1);
            bipedLeftLeg.addChild(frontClothR1);
            frontClothR1.offsetX = -0.235F;
            bipedRightLeg.childModels.remove(frontClothR2);
            bipedLeftLeg.addChild(frontClothR2);
            frontClothR2.offsetX = -0.235F;
            
            bipedRightLeg.childModels.remove(frontClothL1);
            bipedLeftLeg.addChild(frontClothL1);
            frontClothL1.offsetX = 0.0F;
            bipedRightLeg.childModels.remove(frontClothL2);
            bipedLeftLeg.addChild(frontClothL2);
            frontClothL2.offsetX = 0.0F;
            
            bipedLeftLeg.childModels.remove(backClothL1);
            bipedRightLeg.addChild(backClothL1);
            backClothL1.offsetX = 0.235F;
            bipedLeftLeg.childModels.remove(backClothL2);
            bipedRightLeg.addChild(backClothL2);
            backClothL2.offsetX = 0.235F;
            bipedLeftLeg.childModels.remove(backClothL3);
            bipedRightLeg.addChild(backClothL3);
            backClothL3.offsetX = 0.235F;
            
            bipedLeftLeg.childModels.remove(backClothR1);
            bipedRightLeg.addChild(backClothR1);
            backClothR1.offsetX = 0.0F;
            bipedLeftLeg.childModels.remove(backClothR2);
            bipedRightLeg.addChild(backClothR2);
            backClothR2.offsetX = 0.0F;
            bipedLeftLeg.childModels.remove(backClothR3);
            bipedRightLeg.addChild(backClothR3);
            backClothR3.offsetX = 0.0F;
        }
        else if (leftLess && bipedLeftLeg.rotateAngleX > bipedRightLeg.rotateAngleX) {
            leftLess = false;
            bipedLeftLeg.childModels.remove(frontClothR1);
            bipedRightLeg.addChild(frontClothR1);
            frontClothR1.offsetX = 0.0F;
            bipedLeftLeg.childModels.remove(frontClothR2);
            bipedRightLeg.addChild(frontClothR2);
            frontClothR2.offsetX = 0.0F;
            
            bipedLeftLeg.childModels.remove(frontClothL1);
            bipedRightLeg.addChild(frontClothL1);
            frontClothL1.offsetX = 0.235F;
            bipedLeftLeg.childModels.remove(frontClothL2);
            bipedRightLeg.addChild(frontClothL2);
            frontClothL2.offsetX = 0.235F;
            
            bipedRightLeg.childModels.remove(backClothR1);
            bipedLeftLeg.addChild(backClothR1);
            backClothR1.offsetX = -0.235F;
            bipedRightLeg.childModels.remove(backClothR2);
            bipedLeftLeg.addChild(backClothR2);
            backClothR2.offsetX = -0.235F;
            bipedRightLeg.childModels.remove(backClothR3);
            bipedLeftLeg.addChild(backClothR3);
            backClothR3.offsetX = -0.235F;
            
            bipedRightLeg.childModels.remove(backClothL1);
            bipedLeftLeg.addChild(backClothL1);
            backClothL1.offsetX = 0.0F;
            bipedRightLeg.childModels.remove(backClothL2);
            bipedLeftLeg.addChild(backClothL2);
            backClothL2.offsetX = 0.0F;
            bipedRightLeg.childModels.remove(backClothL3);
            bipedLeftLeg.addChild(backClothL3);
            backClothL3.offsetX = 0.0F;
        }
    }
    
    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
        
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        GlStateManager.pushMatrix();
        if (isChild) {
            GlStateManager.scale(0.75F, 0.75F, 0.75F);
            GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            bipedHead.render(scale);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            bipedBody.render(scale);
            bipedRightArm.render(scale);
            bipedLeftArm.render(scale);
            bipedRightLeg.render(scale);
            bipedLeftLeg.render(scale);
            bipedHeadwear.render(scale);
        }
        else {
            GlStateManager.scale(1.01F, 1.01F, 1.01F);
            if (entity.isSneaking())
                GlStateManager.translate(0.0F, 0.2F, 0.0F);

            bipedHead.render(scale);
            bipedBody.render(scale);
            bipedRightArm.render(scale);
            bipedLeftArm.render(scale);
            bipedRightLeg.render(scale);
            bipedLeftLeg.render(scale);
            bipedHeadwear.render(scale);
        }

        GlStateManager.popMatrix();
    }
    
}
