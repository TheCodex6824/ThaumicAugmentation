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

package thecodex6824.thaumicaugmentation.common.capability;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants.NBT;
import thecodex6824.thaumicaugmentation.api.augment.Augment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.CasterAugmentBuilder;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.ICustomCasterAugment;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterEffectProvider;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterStrengthProvider;

public class AugmentCasterCustom extends Augment implements ICustomCasterAugment {

    protected ItemStack strength;
    protected ItemStack effect;
    
    protected ResourceLocation strengthLoc;
    protected ResourceLocation effectLoc;
    
    public AugmentCasterCustom() {
        strength = ItemStack.EMPTY;
        effect = ItemStack.EMPTY;
    }
    
    @Override
    public void onEquip(Entity user) {
        if (!strength.isEmpty() && strengthLoc != null)
            CasterAugmentBuilder.getStrengthProvider(strengthLoc).onEquip(this, user);
        if (!effect.isEmpty() && effectLoc != null)
            CasterAugmentBuilder.getEffectProvider(effectLoc).onEquip(this, user);
    }
    
    @Override
    public void onUnequip(Entity user) {
        if (!strength.isEmpty() && strengthLoc != null)
            CasterAugmentBuilder.getStrengthProvider(strengthLoc).onUnequip(this, user);
        if (!effect.isEmpty() && effectLoc != null)
            CasterAugmentBuilder.getEffectProvider(effectLoc).onUnequip(this, user);
    }
    
    @Override
    public void onCastPre(ItemStack caster, FocusWrapper focus, Entity user) {
        if (!strength.isEmpty() && !effect.isEmpty() && strengthLoc != null && effectLoc != null) {
            CasterAugmentBuilder.getEffectProvider(effectLoc).apply(this, user, caster, focus,
                    CasterAugmentBuilder.getStrengthProvider(strengthLoc).calculateStrength(this, focus, user));
        }
    }
    
    @Override
    public void onDamaged(Entity user, @Nullable Entity attacker) {
        if (!strength.isEmpty() && strengthLoc != null)
            CasterAugmentBuilder.getStrengthProvider(strengthLoc).onDamaged(this, user, attacker);
        if (!effect.isEmpty() && effectLoc != null)
            CasterAugmentBuilder.getEffectProvider(effectLoc).onDamaged(this, user, attacker);
    }
    
    @Override
    public void onHurt(Entity user, @Nullable Entity attacker) {
        if (!strength.isEmpty() && strengthLoc != null)
            CasterAugmentBuilder.getStrengthProvider(strengthLoc).onHurt(this, user, attacker);
        if (!effect.isEmpty() && effectLoc != null)
            CasterAugmentBuilder.getEffectProvider(effectLoc).onHurt(this, user, attacker);
    }
    
    @Override
    public void onDamagedEntity(Entity user, Entity attacked) {
        if (!strength.isEmpty() && strengthLoc != null)
            CasterAugmentBuilder.getStrengthProvider(strengthLoc).onDamagedEntity(this, user, attacked);
        if (!effect.isEmpty() && effectLoc != null)
            CasterAugmentBuilder.getEffectProvider(effectLoc).onDamagedEntity(this, user, attacked);
    }
    
    @Override
    public void onHurtEntity(Entity user, Entity attacked) {
        if (!strength.isEmpty() && strengthLoc != null)
            CasterAugmentBuilder.getStrengthProvider(strengthLoc).onHurtEntity(this, user, attacked);
        if (!effect.isEmpty() && effectLoc != null)
            CasterAugmentBuilder.getEffectProvider(effectLoc).onHurtEntity(this, user, attacked);
    }
    
    @Override
    public void onTick(Entity user) {
        if (!strength.isEmpty() && strengthLoc != null)
            CasterAugmentBuilder.getStrengthProvider(strengthLoc).onTick(this, user);
        if (!effect.isEmpty() && effectLoc != null)
            CasterAugmentBuilder.getEffectProvider(effectLoc).onTick(this, user);
    }
    
    @Override
    public boolean isCompatible(ItemStack otherAugment) {
        if (otherAugment.hasCapability(CapabilityAugment.AUGMENT, null) && otherAugment.getCapability(
                CapabilityAugment.AUGMENT, null).getClass() == getClass()) {
            IAugment a = otherAugment.getCapability(CapabilityAugment.AUGMENT, null);
            if (a instanceof ICustomCasterAugment) {
                ICustomCasterAugment aug = (ICustomCasterAugment) a;
                return !aug.getStrengthProvider().getTranslationKey().equals(strength.getTranslationKey());
            }
        }
        
        return true;
    }
    
    @Override
    public void setStrengthProvider(ItemStack s) {
        strength = s.copy();
        strengthLoc = strength.hasTagCompound() ? new ResourceLocation(strength.getTagCompound().getString("id")) : null;
    }
    
    @Override
    public ItemStack getStrengthProvider() {
        return strength;
    }
    
    @Override
    public void setEffectProvider(ItemStack e) {
        effect = e.copy();
        effectLoc = effect.hasTagCompound() ? new ResourceLocation(effect.getTagCompound().getString("id")) : null;
    }
    
    @Override
    public ItemStack getEffectProvider() {
        return effect;
    }
    
    @Override
    public boolean hasAdditionalAugmentTooltip() {
        return true;
    }
    
    @Override
    public void appendAdditionalAugmentTooltip(List<String> tooltip) {
        tooltip.add(new TextComponentTranslation(strength.getTranslationKey()).getFormattedText());
        CasterAugmentBuilder.getStrengthProvider(ItemCustomCasterStrengthProvider.getProviderID(
                strength)).appendAdditionalTooltip(strength, tooltip);
        tooltip.add(new TextComponentTranslation(effect.getTranslationKey()).getFormattedText());
        CasterAugmentBuilder.getEffectProvider(ItemCustomCasterEffectProvider.getProviderID(
                effect)).appendAdditionalTooltip(effect, tooltip);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("strength", NBT.TAG_COMPOUND)) {
            strength = new ItemStack(nbt.getCompoundTag("strength"));
            strengthLoc = strength.hasTagCompound() ? new ResourceLocation(strength.getTagCompound().getString("id")) : null;
        }
        if (nbt.hasKey("effect", NBT.TAG_COMPOUND)) {
            effect = new ItemStack(nbt.getCompoundTag("effect"));
            effectLoc = effect.hasTagCompound() ? new ResourceLocation(effect.getTagCompound().getString("id")) : null;
        }
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (!strength.isEmpty())
            tag.setTag("strength", strength.serializeNBT());
        if (!effect.isEmpty())
            tag.setTag("effect", effect.serializeNBT());
        
        return tag;
    }
    
}
