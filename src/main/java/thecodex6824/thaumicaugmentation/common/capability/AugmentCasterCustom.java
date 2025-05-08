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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.api.casters.ICaster;
import thecodex6824.thaumicaugmentation.api.augment.Augment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.impl.custom.*;
import thecodex6824.thaumicaugmentation.api.augment.impl.impulsecannon.IImpulseCannonAugment;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.util.DamageWrapper;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterEffectProvider;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterStrengthProvider;
import thecodex6824.thaumicaugmentation.common.item.ItemImpulseCannon;

import javax.annotation.Nullable;

public class AugmentCasterCustom extends Augment implements ICustomAugment, IImpulseCannonAugment {

    protected ItemStack strength;
    protected ItemStack effect;
    
    protected ResourceLocation strengthLoc;
    protected ResourceLocation effectLoc;
    
    public AugmentCasterCustom() {
        strength = ItemStack.EMPTY;
        effect = ItemStack.EMPTY;
    }

    @Override
    public boolean canBeAppliedToItem(ItemStack augmentable) {
        if (augmentable.getItem() instanceof ICaster) {
            return getEffectProviderCaster() != null && getStrengthProviderCaster() != null;
        }
        if (augmentable.getItem() instanceof ItemImpulseCannon) {
            return getEffectProviderCannon() != null && getStrengthProviderCannon() != null;

        }
        return false;
    }

    @Override
    public void onEquip(Entity user) {
        if (!strength.isEmpty() && strengthLoc != null)
            CustomAugmentBuilder.getStrengthProvider(strengthLoc).onEquip(this, user);
        if (!effect.isEmpty() && effectLoc != null)
            CustomAugmentBuilder.getEffectProvider(effectLoc).onEquip(this, user);
    }
    
    @Override
    public void onUnequip(Entity user) {
        if (!strength.isEmpty() && strengthLoc != null)
            CustomAugmentBuilder.getStrengthProvider(strengthLoc).onUnequip(this, user);
        if (!effect.isEmpty() && effectLoc != null)
            CustomAugmentBuilder.getEffectProvider(effectLoc).onUnequip(this, user);
    }
    
    @Override
    public boolean onCastPre(ItemStack caster, FocusWrapper focus, Entity user) {
        if (!strength.isEmpty() && !effect.isEmpty() && strengthLoc != null && effectLoc != null) {
            IBuilderCasterEffectProvider effectProvider = getEffectProviderCaster();
            IBuilderCasterStrengthProvider strengthProvider = getStrengthProviderCaster();
            if (effectProvider != null && strengthProvider != null) {
                effectProvider.apply(this, user, caster, focus, strengthProvider.calculateStrength(this, focus, user));
            }
        }
        
        return false;
    }
    
    @Override
    public boolean onDamaged(Entity attacked, DamageSource source, DamageWrapper damage) {
        if (!strength.isEmpty() && strengthLoc != null)
            CustomAugmentBuilder.getStrengthProvider(strengthLoc).onDamaged(this, attacked, source, damage);
        if (!effect.isEmpty() && effectLoc != null)
            CustomAugmentBuilder.getEffectProvider(effectLoc).onDamaged(this, attacked, source, damage);
        
        return false;
    }
    
    @Override
    public boolean onHurt(Entity attacked, DamageSource source, DamageWrapper damage) {
        if (!strength.isEmpty() && strengthLoc != null)
            CustomAugmentBuilder.getStrengthProvider(strengthLoc).onHurt(this, attacked, source, damage);
        if (!effect.isEmpty() && effectLoc != null)
            CustomAugmentBuilder.getEffectProvider(effectLoc).onHurt(this, attacked, source, damage);
        
        return false;
    }
    
    @Override
    public boolean onDamagedEntity(DamageSource source, Entity attacked, DamageWrapper damage) {
        if (!strength.isEmpty() && strengthLoc != null)
            CustomAugmentBuilder.getStrengthProvider(strengthLoc).onDamagedEntity(this, source, attacked, damage);
        if (!effect.isEmpty() && effectLoc != null)
            CustomAugmentBuilder.getEffectProvider(effectLoc).onDamagedEntity(this, source, attacked, damage);
        
        return false;
    }
    
    @Override
    public boolean onHurtEntity(DamageSource source, Entity attacked, DamageWrapper damage) {
        if (!strength.isEmpty() && strengthLoc != null)
            CustomAugmentBuilder.getStrengthProvider(strengthLoc).onHurtEntity(this, source, attacked, damage);
        if (!effect.isEmpty() && effectLoc != null)
            CustomAugmentBuilder.getEffectProvider(effectLoc).onHurtEntity(this, source, attacked, damage);
        
        return false;
    }
    
    @Override
    public boolean onTick(Entity user) {
        if (!strength.isEmpty() && strengthLoc != null)
            CustomAugmentBuilder.getStrengthProvider(strengthLoc).onTick(this, user);
        if (!effect.isEmpty() && effectLoc != null)
            CustomAugmentBuilder.getEffectProvider(effectLoc).onTick(this, user);
        
        return false;
    }
    
    @Override
    public boolean isCompatible(ItemStack otherAugment, IAugment otherAugmentCap) {
        if (otherAugmentCap instanceof ICustomAugment) {
            ICustomAugment aug = (ICustomAugment) otherAugmentCap;
            return !aug.getStrengthProvider().getTranslationKey().equals(strength.getTranslationKey());
        }
        
        return true;
    }

    @Override
    public double getImpulseCostModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer) {
        IBuilderCannonStrengthProvider strengthProvider = getStrengthProviderCannon();
        IBuilderCannonEffectProvider effectProvider = getEffectProviderCannon();
        if (strengthProvider != null && effectProvider != null) {
            return effectProvider.getImpulseCostModifier(this, cannonStack, user, buffer,
                    strengthProvider.calculateStrength(this, cannonStack, user));
        }
        return 1;
    }

    @Override
    public float getBaseDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
        IBuilderCannonStrengthProvider strengthProvider = getStrengthProviderCannon();
        IBuilderCannonEffectProvider effectProvider = getEffectProviderCannon();
        if (strengthProvider != null && effectProvider != null) {
            return effectProvider.getBaseDamageModifier(this, cannonStack, user, buffer, normalImpetusConsumed,
                    actualImpetusConsumed, strengthProvider.calculateStrength(this, cannonStack, user));
        }
        return 1;
    }

    @Override
    public float getMagicDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
        IBuilderCannonStrengthProvider strengthProvider = getStrengthProviderCannon();
        IBuilderCannonEffectProvider effectProvider = getEffectProviderCannon();
        if (strengthProvider != null && effectProvider != null) {
            return effectProvider.getMagicDamageModifier(this, cannonStack, user, buffer, normalImpetusConsumed,
                    actualImpetusConsumed, strengthProvider.calculateStrength(this, cannonStack, user));
        }
        return 1;
    }

    @Override
    public float getNormalDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
        IBuilderCannonStrengthProvider strengthProvider = getStrengthProviderCannon();
        IBuilderCannonEffectProvider effectProvider = getEffectProviderCannon();
        if (strengthProvider != null && effectProvider != null) {
            return effectProvider.getNormalDamageModifier(this, cannonStack, user, buffer, normalImpetusConsumed,
                    actualImpetusConsumed, strengthProvider.calculateStrength(this, cannonStack, user));
        }return 1;
    }

    @Override
    public void applyAdditionalEffectsToEntity(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d firingOrigin, Vec3d firingEnd, Entity entityHit, float baseDamage) {
        IBuilderCannonStrengthProvider strengthProvider = getStrengthProviderCannon();
        IBuilderCannonEffectProvider effectProvider = getEffectProviderCannon();
        if (strengthProvider != null && effectProvider != null) {
            effectProvider.applyAdditionalEffectsToEntity(this, cannonStack, user, firingOrigin, firingEnd,
                    entityHit, baseDamage, strengthProvider.calculateStrength(this, cannonStack, user));
        }
    }

    @Override
    public void applyAdditionalEffects(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d firingOrigin, Vec3d firingEnd, float baseDamage) {
        IBuilderCannonStrengthProvider strengthProvider = getStrengthProviderCannon();
        IBuilderCannonEffectProvider effectProvider = getEffectProviderCannon();
        if (strengthProvider != null && effectProvider != null) {
            effectProvider.applyAdditionalEffects(this, cannonStack, user, firingOrigin, firingEnd,
                    baseDamage, strengthProvider.calculateStrength(this, cannonStack, user));
        }
    }

    protected @Nullable IBuilderCasterEffectProvider getEffectProviderCaster() {
        IBuilderEffectProvider provider = CustomAugmentBuilder.getEffectProvider(effectLoc);
        return provider instanceof IBuilderCasterEffectProvider ? (IBuilderCasterEffectProvider) provider : null;
    }

    protected @Nullable IBuilderCannonEffectProvider getEffectProviderCannon() {
        IBuilderEffectProvider provider = CustomAugmentBuilder.getEffectProvider(effectLoc);
        return provider instanceof IBuilderCannonEffectProvider ? (IBuilderCannonEffectProvider) provider : null;
    }

    protected @Nullable IBuilderCasterStrengthProvider getStrengthProviderCaster() {
        IBuilderStrengthProvider provider = CustomAugmentBuilder.getStrengthProvider(strengthLoc);
        return provider instanceof IBuilderCasterStrengthProvider ? (IBuilderCasterStrengthProvider) provider : null;
    }

    protected @Nullable IBuilderCannonStrengthProvider getStrengthProviderCannon() {
        IBuilderStrengthProvider provider = CustomAugmentBuilder.getStrengthProvider(strengthLoc);
        return provider instanceof IBuilderCannonStrengthProvider ? (IBuilderCannonStrengthProvider) provider : null;
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
        CustomAugmentBuilder.getStrengthProvider(ItemCustomCasterStrengthProvider.getProviderID(
                strength)).appendAdditionalTooltip(strength, tooltip);
        tooltip.add(new TextComponentTranslation(effect.getTranslationKey()).getFormattedText());
        CustomAugmentBuilder.getEffectProvider(ItemCustomCasterEffectProvider.getProviderID(
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
