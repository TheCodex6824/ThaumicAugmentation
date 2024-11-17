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

package thecodex6824.thaumicaugmentation.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.common.items.casters.foci.FocusEffectEarth;
import thaumcraft.common.items.casters.foci.FocusEffectFlux;
import thaumcraft.common.items.casters.foci.FocusModSplitTarget;
import thecodex6824.thaumicaugmentation.api.aspect.AspectElementInteractionManager;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.IBuilderCasterStrengthProvider;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.ICustomCasterAugment;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;
import thecodex6824.thaumicaugmentation.common.item.builder.StrengthProviderElemental;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectWater;

public class TestAugment {

    private ICustomCasterAugment makeStrengthProvider(String aspect) {
	return new ICustomCasterAugment() {
	    @Override
	    public void setStrengthProvider(ItemStack s) {}

	    @Override
	    public void setEffectProvider(ItemStack e) {}

	    @Override
	    public ItemStack getStrengthProvider() {
		ItemStack stack = new ItemStack(Items.APPLE);
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setString("aspect", aspect);
		return stack;
	    }

	    @Override
	    public ItemStack getEffectProvider() {
		return ItemStack.EMPTY;
	    }
	};
    }

    @BeforeAll
    static void testSetup() {
	// TODO move this into global test setup like TC Fix has once unit test framework is ported
	Bootstrap.register();
	TAConfigHolder.preInit();
	AspectElementInteractionManager.init();
    }

    // positive multiplier should only apply once
    @Test
    void testStrengthProviderElementalPositiveFactor() {
	IBuilderCasterStrengthProvider elemental = new StrengthProviderElemental();
	FocusPackage p = new FocusPackage();
	p.addNode(new FocusModSplitTarget());
	p.addNode(new FocusEffectFlux());
	p.addNode(new FocusEffectFlux());
	assertEquals(1.75f, elemental.calculateStrength(makeStrengthProvider("vitium"),
		new FocusWrapper(p, 1, 1.0f), null));
    }

    // negative multiplier should only apply once *per aspect*
    @Test
    void testStrengthProviderElementalNegativeFactor() {
	IBuilderCasterStrengthProvider elemental = new StrengthProviderElemental();
	FocusPackage p = new FocusPackage();
	p.addNode(new FocusModSplitTarget());
	p.addNode(new FocusModSplitTarget());
	p.addNode(new FocusEffectEarth());
	p.addNode(new FocusEffectEarth());
	p.addNode(new FocusEffectWater());
	assertEquals(0.5625f, elemental.calculateStrength(makeStrengthProvider("aer"),
		new FocusWrapper(p, 1, 1.0f), null));
    }

}
