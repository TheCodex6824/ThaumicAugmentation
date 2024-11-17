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

package thecodex6824.thaumicaugmentation.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.CasterAugmentBuilder;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.IBuilderCasterEffectProvider;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.IBuilderCasterStrengthProvider;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.ICustomCasterAugment;
import thecodex6824.thaumicaugmentation.api.util.DamageWrapper;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;
import thecodex6824.thaumicaugmentation.common.item.builder.StrengthProviderElemental;

public class AugmentHandler {

    public static void registerAugmentBuilderComponents() {
	CasterAugmentBuilder.registerStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_experience"), new IBuilderCasterStrengthProvider() {
	    @Override
	    public double calculateStrength(ICustomCasterAugment augment, FocusWrapper focus, Entity entity) {
		if (entity instanceof EntityPlayer) {
		    EntityPlayer player = (EntityPlayer) entity;
		    return Math.max(Math.min(player.experienceLevel * TAConfig.experienceModifierScale.getValue(),
			    TAConfig.experienceModifierCap.getValue()), TAConfig.experienceModifierBase.getValue());
		}
		else
		    return 1.0;
	    }
	});

	CasterAugmentBuilder.registerStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_elemental"),
		new StrengthProviderElemental(), (stack) -> {
		    stack.getTagCompound().setString("aspect", Aspect.ORDER.getTag());
		});

	CasterAugmentBuilder.registerStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_overworld"), new IBuilderCasterStrengthProvider() {
	    @Override
	    public double calculateStrength(ICustomCasterAugment augment, FocusWrapper focus, Entity entity) {
		return TAConfig.dimensionalModifierOverworldDims.getValue().contains(entity.dimension) ?
			TAConfig.dimensionalModifierOverworldPostiveFactor.getValue() :
			    TAConfig.dimensionalModifierOverworldNegativeFactor.getValue();
	    }
	});
	CasterAugmentBuilder.registerStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_nether"), new IBuilderCasterStrengthProvider() {
	    @Override
	    public double calculateStrength(ICustomCasterAugment augment, FocusWrapper focus, Entity entity) {
		return TAConfig.dimensionalModifierNetherDims.getValue().contains(entity.dimension) ?
			TAConfig.dimensionalModifierNetherPostiveFactor.getValue() :
			    TAConfig.dimensionalModifierNetherNegativeFactor.getValue();
	    }
	});
	CasterAugmentBuilder.registerStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_end"), new IBuilderCasterStrengthProvider() {
	    @Override
	    public double calculateStrength(ICustomCasterAugment augment, FocusWrapper focus, Entity entity) {
		return TAConfig.dimensionalModifierEndDims.getValue().contains(entity.dimension) ?
			TAConfig.dimensionalModifierEndPostiveFactor.getValue() :
			    TAConfig.dimensionalModifierEndNegativeFactor.getValue();
	    }
	});
	CasterAugmentBuilder.registerStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_emptiness"), new IBuilderCasterStrengthProvider() {
	    @Override
	    public double calculateStrength(ICustomCasterAugment augment, FocusWrapper focus, Entity entity) {
		return TAConfig.dimensionalModifierEmptinessDims.getValue().contains(entity.dimension) ?
			TAConfig.dimensionalModifierEmptinessPostiveFactor.getValue() :
			    TAConfig.dimensionalModifierEmptinessNegativeFactor.getValue();
	    }
	});

	CasterAugmentBuilder.registerStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_frenzy"), new IBuilderCasterStrengthProvider() {

	    @Override
	    public void onTick(ICustomCasterAugment augment, Entity user) {
		ItemStack stack = augment.getStrengthProvider();
		if (stack.hasTagCompound() && stack.getTagCompound().getInteger("frenzyCooldown") > 0) {
		    int newCooldown = stack.getTagCompound().getInteger("frenzyCooldown") - 1;
		    if (newCooldown == 0) {
			stack.getTagCompound().setInteger("frenzy", 0);
			stack.getTagCompound().setInteger("frenzyCooldown", -1);
		    }
		    else
			stack.getTagCompound().setInteger("frenzyCooldown", newCooldown);
		}
	    }

	    @Override
	    public void onUnequip(ICustomCasterAugment augment, Entity user) {
		ItemStack stack = augment.getStrengthProvider();
		if (!stack.hasTagCompound())
		    stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("frenzy", 0);
		stack.getTagCompound().setInteger("frenzyCooldown", -1);
	    }

	    @Override
	    public void onHurtEntity(ICustomCasterAugment augment, DamageSource source, Entity attacked, DamageWrapper damage) {
		ItemStack stack = augment.getStrengthProvider();
		if (!stack.hasTagCompound())
		    stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("frenzy", Math.min(stack.getTagCompound().getInteger("frenzy") + 1, TAConfig.frenzyModifierMaxLevel.getValue()));
		stack.getTagCompound().setInteger("frenzyCooldown", TAConfig.frenzyModifierCooldown.getValue());
	    }

	    @Override
	    public double calculateStrength(ICustomCasterAugment augment, FocusWrapper focus, Entity user) {
		ItemStack stack = augment.getStrengthProvider();
		if (!stack.hasTagCompound())
		    stack.setTagCompound(new NBTTagCompound());

		return 1.0 + stack.getTagCompound().getInteger("frenzy") * TAConfig.frenzyModifierScaleFactor.getValue();
	    }
	});

	CasterAugmentBuilder.registerEffectProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "effect_power"), new IBuilderCasterEffectProvider() {
	    @Override
	    public void apply(ICustomCasterAugment augment, Entity entity, ItemStack caster, FocusWrapper focus, double strength) {
		focus.setFocusPower(focus.getFocusPower() + (focus.getOriginalFocusPower() * (float) strength - focus.getOriginalFocusPower()));
	    }
	});
	CasterAugmentBuilder.registerEffectProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "effect_cast_speed"), new IBuilderCasterEffectProvider() {
	    @Override
	    public void apply(ICustomCasterAugment augment, Entity entity, ItemStack caster, FocusWrapper focus, double strength) {
		focus.setCooldown((int) Math.ceil(focus.getCooldown() * (1.0 / strength)));
	    }
	});
	CasterAugmentBuilder.registerEffectProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "effect_cost"), new IBuilderCasterEffectProvider() {
	    @Override
	    public void apply(ICustomCasterAugment augment, Entity entity, ItemStack caster, FocusWrapper focus, double strength) {
		focus.setVisCost(focus.getVisCost() * (float) (1.0 / strength));
	    }
	});
    }

}
