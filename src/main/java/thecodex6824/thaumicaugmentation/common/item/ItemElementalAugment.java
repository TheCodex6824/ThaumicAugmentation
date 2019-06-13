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

package thecodex6824.thaumicaugmentation.common.item;

import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Sets;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.casters.IFocusElement;
import thecodex6824.thaumicaugmentation.api.aspect.AspectElementInteractionManager;
import thecodex6824.thaumicaugmentation.api.aspect.AspectUtil;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.item.IAssociatedAspect;
import thecodex6824.thaumicaugmentation.api.util.AugmentUtils;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemElementalAugment extends ItemTABase implements IAugment, IAssociatedAspect {

    private static final HashSet<TextFormatting> DISALLOWED_COLORS = Sets.newHashSet(TextFormatting.BLACK);
    
    public ItemElementalAugment() {
        super();
        setMaxStackSize(1);
    }
    
    @Override
    public void setAspect(ItemStack stack, Aspect newAspect) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        stack.getTagCompound().setString("aspect", newAspect.getTag());
    }
    
    @Override
    public Aspect getAspect(ItemStack stack) {
        if (!stack.hasTagCompound() || Aspect.getAspect(stack.getTagCompound().getString("aspect")) == null)
            return Aspect.ORDER;
        
        return Aspect.getAspect(stack.getTagCompound().getString("aspect"));
    }
    
    @Override
    public boolean canBeAppliedToItem(ItemStack stack, ItemStack augmentable) {
        return augmentable.getItem() instanceof ICaster;
    }
    
    @Override
    public boolean isCompatible(ItemStack stack, ItemStack otherAugment) {
        return stack.getItem() != otherAugment.getItem();
    }
    
    @Override
    public void onCast(ItemStack stack, ItemStack caster, FocusPackage focusPackage, Entity user) {
        float totalMultiplier = 1.0F;
        for (IFocusElement node : focusPackage.nodes) {
            if (node instanceof FocusEffect && ((FocusEffect) node).getAspect() == getAspect(stack))
                totalMultiplier *= 1.5F;
            else if (node instanceof FocusNode && AspectElementInteractionManager.getNegativeAspects(getAspect(stack)).contains(
                    ((FocusNode) node).getAspect())) {
                totalMultiplier *= 0.5F;
            }
        }
        
        AugmentUtils.setPackagePower(focusPackage, focusPackage.getPower() * totalMultiplier);
    }
    
    @Override
    public boolean hasAdditionalAugmentTooltip(ItemStack stack) {
        return true;
    }
    
    @Override
    public void appendAdditionalAugmentTooltip(ItemStack stack, List<String> tooltip) {
        Aspect aspect = getAspect(stack);
        tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.elemental_aspect", AspectUtil.getChatColorForAspect(aspect, DISALLOWED_COLORS) + aspect.getName()).getFormattedText());
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        Aspect aspect = getAspect(stack);
        tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.elemental_aspect", AspectUtil.getChatColorForAspect(aspect, DISALLOWED_COLORS) + aspect.getName()).getFormattedText());
    }
    
}
