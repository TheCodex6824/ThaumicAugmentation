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

import java.util.ArrayDeque;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusModSplit;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.aspect.AspectElementInteractionManager;
import thecodex6824.thaumicaugmentation.api.aspect.AspectUtil;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.CasterAugmentBuilder;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.IBuilderCasterEffectProvider;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.IBuilderCasterStrengthProvider;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.ICustomCasterAugment;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;

public class AugmentHandler {

    public static void registerAugmentBuilderComponents() {
        CasterAugmentBuilder.registerStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_experience"), new IBuilderCasterStrengthProvider() {
            @Override
            public double calculateStrength(ICustomCasterAugment augment, FocusWrapper focus, Entity entity) {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    return Math.max(Math.min(player.experienceLevel / TAConfig.experienceModifierScale.getValue(),
                            TAConfig.experienceModifierCap.getValue()), TAConfig.experienceModifierBase.getValue());
                }
                else
                    return 1.0;
            }
        });
        
        CasterAugmentBuilder.registerStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_elemental"), new IBuilderCasterStrengthProvider() {
            
            private Aspect getAspect(ItemStack stack) {
                if (!stack.hasTagCompound() || Aspect.getAspect(stack.getTagCompound().getString("aspect")) == null)
                    return Aspect.ORDER;
                
                return Aspect.getAspect(stack.getTagCompound().getString("aspect"));
            }
            
            @Override
            public void appendAdditionalTooltip(ItemStack component, List<String> tooltip) {
                Aspect aspect = getAspect(component);
                tooltip.add("  " + new TextComponentTranslation("thaumicaugmentation.text.elemental_aspect",
                        AspectUtil.getChatColorForAspect(aspect) + aspect.getName() + TextFormatting.RESET).getFormattedText());
            }
            
            @Override
            public int calculateTintColor(ICustomCasterAugment augment) {
                return getAspect(augment.getStrengthProvider()).getColor();
            }
            
            @Override
            public double calculateStrength(ICustomCasterAugment augment, FocusWrapper focus, Entity entity) {
                double totalMultiplier = 1.0;
                ArrayDeque<IFocusElement> nodes = new ArrayDeque<>(focus.getFocus().nodes);
                while (!nodes.isEmpty()) {
                    IFocusElement node = nodes.pop();
                    if (node instanceof FocusEffect && ((FocusEffect) node).getAspect() == getAspect(augment.getStrengthProvider()))
                        totalMultiplier *= TAConfig.elementalModifierPositiveFactor.getValue();
                    else if (node instanceof FocusModSplit) {
                        for (FocusPackage f : ((FocusModSplit) node).getSplitPackages())
                            nodes.addAll(f.nodes);
                    }
                    else if (node instanceof FocusNode && AspectElementInteractionManager.getNegativeAspects(
                            getAspect(augment.getStrengthProvider())).contains(((FocusNode) node).getAspect())) {
                        totalMultiplier *= TAConfig.elementalModifierNegativeFactor.getValue();
                    }
                }
                
                return totalMultiplier;
            }
        }, (stack) -> {
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
            public void onHurtEntity(ICustomCasterAugment augment, Entity user, Entity attacked) {
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
