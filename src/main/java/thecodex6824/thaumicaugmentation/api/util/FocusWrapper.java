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

package thecodex6824.thaumicaugmentation.api.util;

import thaumcraft.api.casters.FocusPackage;

public class FocusWrapper {

    protected FocusPackage focus;
    protected int cooldown;
    protected float cost;
    
    protected float originalPower;
    protected int originalCooldown;
    protected float originalCost;
    
    public FocusWrapper(FocusPackage f, int cooldownTicks, float visCost) {
        focus = f;
        cooldown = cooldownTicks;
        cost = visCost;
        
        originalPower = getFocusPower();
        originalCooldown = getCooldown();
        originalCost = getVisCost();
    }
    
    public float getFocusPower() {
        return FocusUtils.getPackagePower(focus);
    }
    
    public void setFocusPower(float newPower) {
        FocusUtils.setPackagePower(focus, newPower);
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    public void setCooldown(int newCooldown) {
        cooldown = newCooldown;
    }
    
    public float getVisCost() {
        return cost;
    }
    
    public void setVisCost(float newCost) {
        cost = newCost;
    }
    
    public FocusPackage getFocus() {
        return focus;
    }
    
    public float getOriginalFocusPower() {
        return originalPower;
    }
    
    public int getOriginalCooldown() {
        return originalCooldown;
    }
    
    public float getOriginalVisCost() {
        return originalCost;
    }
    
}
