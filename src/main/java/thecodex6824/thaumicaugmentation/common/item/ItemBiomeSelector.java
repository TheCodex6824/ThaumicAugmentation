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

package thecodex6824.thaumicaugmentation.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.item.BiomeSelector;
import thecodex6824.thaumicaugmentation.api.item.CapabilityBiomeSelector;
import thecodex6824.thaumicaugmentation.api.item.IBiomeSelector;
import thecodex6824.thaumicaugmentation.common.capability.provider.SimpleCapabilityProvider;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.util.ItemHelper;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

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
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.hasTagCompound()) {
            NBTTagCompound item = stack.getTagCompound().copy();
            if (!ThaumicAugmentation.proxy.isSingleplayer() && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
                item.removeTag("cap");

            tag.setTag("item", item);
        }

        NBTTagCompound cap = ItemHelper.tryMakeCapabilityTag(stack, CapabilityBiomeSelector.BIOME_SELECTOR);
        if (cap != null)
            tag.setTag("cap", cap);

        return tag;
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("cap", NBT.TAG_COMPOUND))
                ((BiomeSelector) stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null)).deserializeNBT(nbt.getCompoundTag("cap"));
            if (nbt.hasKey("item", NBT.TAG_COMPOUND))
                stack.setTagCompound(nbt.getCompoundTag("item"));
            else if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                nbt.removeTag("cap");
                if (!nbt.isEmpty())
                    stack.setTagCompound(nbt);
            }
            
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && !ThaumicAugmentation.proxy.isSingleplayer()) {
                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
                
                stack.getTagCompound().setTag("cap", nbt.getCompoundTag("cap"));
            }
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
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
            boolean capWorkaround = FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && !ThaumicAugmentation.proxy.isSingleplayer();
            
            ItemStack stack = new ItemStack(this);
            stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null).setBiomeID(IBiomeSelector.EMPTY);
            if (capWorkaround) {
                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
                
                stack.getTagCompound().setTag("cap", ((BiomeSelector) stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null)).serializeNBT());
            }
            items.add(stack);
            
            stack = new ItemStack(this);
            stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null).setBiomeID(IBiomeSelector.RESET);
            if (capWorkaround) {
                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
                
                stack.getTagCompound().setTag("cap", ((BiomeSelector) stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null)).serializeNBT());
            }
            items.add(stack);
            
            Iterator<Biome> i = Biome.REGISTRY.iterator();
            while (i.hasNext()) {
                Biome b = i.next();
                stack = new ItemStack(this);
                stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null).setBiomeID(b.getRegistryName());
                if (capWorkaround) {
                    if (!stack.hasTagCompound())
                        stack.setTagCompound(new NBTTagCompound());
                    
                    stack.getTagCompound().setTag("cap", ((BiomeSelector) stack.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null)).serializeNBT());
                }
                items.add(stack);
            }
        }
    }
    
}
