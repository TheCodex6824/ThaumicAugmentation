/**
 *  Thaumic Augmentation
 *  Copyright (c) 2024 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.item.builder;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusModSplit;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.aspect.AspectElementInteractionManager;
import thecodex6824.thaumicaugmentation.api.aspect.AspectUtil;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.IBuilderCasterStrengthProvider;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.ICustomCasterAugment;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;

public class StrengthProviderElemental implements IBuilderCasterStrengthProvider {

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
	boolean positive = false;
	Set<Aspect> negatives = new HashSet<>();
	ArrayDeque<IFocusElement> nodes = new ArrayDeque<>(focus.getFocus().nodes);
	while (!nodes.isEmpty()) {
	    IFocusElement node = nodes.pop();
	    if (!positive && node instanceof FocusEffect && ((FocusEffect) node).getAspect() == getAspect(augment.getStrengthProvider())) {
		totalMultiplier *= TAConfig.elementalModifierPositiveFactor.getValue();
		positive = true;
	    }
	    else if (node instanceof FocusModSplit) {
		for (FocusPackage f : ((FocusModSplit) node).getSplitPackages()) {
		    nodes.addAll(f.nodes);
		}
	    }
	    else if (node instanceof FocusNode && !negatives.contains(((FocusNode) node).getAspect())) {
		Aspect strengthAspect = getAspect(augment.getStrengthProvider());
		Aspect nodeAspect = ((FocusNode) node).getAspect();
		if (AspectElementInteractionManager.getNegativeAspects(strengthAspect).contains(nodeAspect) && negatives.add(nodeAspect)) {
		    totalMultiplier *= TAConfig.elementalModifierNegativeFactor.getValue();
		}
	    }
	}

	return totalMultiplier;
    }

}
