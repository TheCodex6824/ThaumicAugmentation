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

package thecodex6824.thaumicaugmentation.common.capability;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.casters.ICaster;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.Augment;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;

import java.util.List;

public class AugmentCasterVisBattery extends Augment {

    protected boolean syncNeeded;
    protected float vis;
    
    public float getMaxVis() {
        return 40.0F;
    }
    
    public float getVis() {
        return vis;
    }

    public void setVis(float newVis) {
        vis = newVis;
        syncNeeded = true;
    }
    
    @Override
    public boolean canBeAppliedToItem(ItemStack augmentable) {
        return augmentable.getItem() instanceof ICaster;
    }
    
    @Override
    public boolean isCompatible(ItemStack otherAugment) {
        return otherAugment.getItem() != TAItems.AUGMENT_VIS_BATTERY;
    }
    
    @Override
    public boolean onCastPre(ItemStack caster, FocusWrapper focusPackage, Entity user) {
        float difference = AuraHelper.getVis(user.getEntityWorld(), user.getPosition()) - focusPackage.getVisCost();
        if (difference < 0.0F) {
            float extract = Math.min(-difference, vis);
            vis -= extract;
            focusPackage.setVisCost(focusPackage.getVisCost() - extract);
            syncNeeded = true;
        }
        
        return false;
    }
    
    @Override
    public boolean onTick(Entity user) {
        float max = getMaxVis();
        if (!user.getEntityWorld().isRemote && user.getEntityWorld().getTotalWorldTime() % 10 == 0 && vis < max) {
            float inAura = AuraHelper.getVis(user.getEntityWorld(), user.getPosition());
            if (inAura > AuraHelper.getAuraBase(user.getEntityWorld(), user.getPosition()) * 0.75F) {
                float input = Math.min(max - vis, Math.min(inAura, 1.0F));
                vis += AuraHelper.drainVis(user.getEntityWorld(), user.getPosition(), input, false);
                syncNeeded = true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean shouldSync() {
        boolean sync = syncNeeded;
        syncNeeded = false;
        return sync;
    }
    
    @Override
    public boolean hasAdditionalAugmentTooltip() {
        return true;
    }
    
    @Override
    public void appendAdditionalAugmentTooltip(List<String> tooltip) {
        tooltip.add(new TextComponentString(new TextComponentTranslation("tc.charge").getFormattedText() + " " +
                Math.round(vis)).setStyle(new Style().setColor(TextFormatting.YELLOW)).getFormattedText());
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("vis", vis);
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        vis = nbt.getFloat("vis");
    }
    
}
