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

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.item.BiomeSelector;
import thecodex6824.thaumicaugmentation.api.item.CapabilityBiomeSelector;
import thecodex6824.thaumicaugmentation.api.item.IBiomeSelector;
import thecodex6824.thaumicaugmentation.common.capability.SimpleCapabilityProvider;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemBiomeSelector extends ItemTABase {

    public ItemBiomeSelector() {
        super();
        setHasSubtypes(true);
    }
    
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        SimpleCapabilityProvider<IBiomeSelector> provider = new SimpleCapabilityProvider<>(new BiomeSelector(),
                CapabilityBiomeSelector.BIOME_SELECTOR);
        if (nbt != null) {
            NBTTagCompound data = nbt.getCompoundTag("Parent");
            if (!data.isEmpty())
                provider.deserializeNBT(nbt);
        }
        
        return provider;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        // can't do server side only as syncing the cap will be a total pain
        // in this case the client will be able to figure out the expected value on its own
        if (player != null && hand != null) {
            ItemStack stack = player.getHeldItem(hand);
            IBiomeSelector biome = stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null);
            if (biome != null) {
                if (biome.getBiomeID().equals(IBiomeSelector.EMPTY)) {
                    biome.setBiomeID(world.getBiome(player.getPosition()).getRegistryName());
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
                else if (player.isSneaking() && !biome.getBiomeID().equals(IBiomeSelector.EMPTY)) {
                    biome.setBiomeID(IBiomeSelector.EMPTY);
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
            }
        }
        
        return super.onItemRightClick(world, player, hand);
    }
    
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        IBiomeSelector selected = stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null);
        if (selected != null) {
            String biomeName = "";
            ResourceLocation id = selected.getBiomeID();
            Biome biome = Biome.REGISTRY.getObject(id);
            if (id.equals(IBiomeSelector.EMPTY))
                biomeName = new TextComponentTranslation("thaumicaugmentation.text.biome_empty").getFormattedText();
            else if (id.equals(IBiomeSelector.RESET))
                biomeName = new TextComponentTranslation("thaumicaugmentation.text.biome_reset").getFormattedText();
            else if (biome != null)
                biomeName = biome.getBiomeName();
            else
                biomeName = id.getPath();
            
            tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.selected_biome", biomeName).getFormattedText());
        }
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.BIOME_SELECTOR_CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            ItemStack stack = new ItemStack(this);
            stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null).setBiomeID(IBiomeSelector.EMPTY);
            items.add(stack);
            stack = new ItemStack(this);
            stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null).setBiomeID(IBiomeSelector.RESET);
            items.add(stack);
            Iterator<Biome> i = Biome.REGISTRY.iterator();
            while (i.hasNext()) {
                Biome b = i.next();
                stack = new ItemStack(this);
                stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null).setBiomeID(b.getRegistryName());
                items.add(stack);
            }
        }
    }
    
}
