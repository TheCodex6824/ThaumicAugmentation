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

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.item.CapabilityWardAuthenticator;
import thecodex6824.thaumicaugmentation.api.item.IWardAuthenticator;
import thecodex6824.thaumicaugmentation.api.warded.tile.CapabilityWardedTile;
import thecodex6824.thaumicaugmentation.api.warded.tile.IWardedTile;
import thecodex6824.thaumicaugmentation.common.capability.WardAuthenticatorKey;
import thecodex6824.thaumicaugmentation.common.capability.WardAuthenticatorThaumiumKey;
import thecodex6824.thaumicaugmentation.common.capability.provider.CapabilityProviderKey;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemKey extends ItemTABase {

    public ItemKey() {
        super("iron", "brass", "thaumium");
        setMaxStackSize(1);
    }
    
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        WardAuthenticatorKey cap = null;
        if (stack.getMetadata() == 2) {
            cap = new WardAuthenticatorThaumiumKey();
            // migration from stack tag based data
            if (stack.hasTagCompound()) {
                boolean didSomething = false;
                if (stack.getTagCompound().hasKey("boundTo", NBT.TAG_STRING)) {
                    cap.setOwner(UUID.fromString(stack.getTagCompound().getString("boundTo")));
                    didSomething = true;
                }
                
                if (stack.getTagCompound().hasKey("boundToDisplay", NBT.TAG_STRING)) {
                    cap.setOwnerName(stack.getTagCompound().getString("boundToDisplay"));
                    didSomething = true;
                }
                
                if (stack.getTagCompound().hasKey("boundType", NBT.TAG_STRING)) {
                    ((WardAuthenticatorThaumiumKey) cap).setBoundType(stack.getTagCompound().getString("boundType"));
                    didSomething = true;
                }
                
                if (stack.getTagCompound().hasKey("boundTypeDisplay", NBT.TAG_STRING)) {
                    ((WardAuthenticatorThaumiumKey) cap).setBoundTypeName(stack.getTagCompound().getString("boundTypeDisplay"));
                    didSomething = true;
                }
            
                if (stack.getTagCompound().hasKey("boundBlockPos", NBT.TAG_INT_ARRAY)) {
                    int[] coords = stack.getTagCompound().getIntArray("boundBlockPos");
                    if (coords.length == 3) {
                        ((WardAuthenticatorThaumiumKey) cap).setBoundPosition(new BlockPos(coords[0], coords[1], coords[2]));
                        didSomething = true;
                    }
                }
                
                // if the cap provider has NBT it will overwrite the migration, so save it now
                if (nbt != null && didSomething)
                    nbt.setTag("Parent", cap.serializeNBT());
            }
        }
        else {
            cap = new WardAuthenticatorKey();
            if (stack.hasTagCompound()) {
                boolean didSomething = false;
                if (stack.getTagCompound().hasKey("boundTo", NBT.TAG_STRING)) {
                    cap.setOwner(UUID.fromString(stack.getTagCompound().getString("boundTo")));
                    didSomething = true;
                }
                
                if (stack.getTagCompound().hasKey("boundToDisplay", NBT.TAG_STRING)) {
                    cap.setOwnerName(stack.getTagCompound().getString("boundToDisplay"));
                    didSomething = true;
                }
                
                if (nbt != null && didSomething)
                    nbt.setTag("Parent", cap.serializeNBT());
            }
        }
        
        return new CapabilityProviderKey(cap);
    }

    protected int generateKeyColor(UUID id) {
        return id.hashCode();
    }

    public int getKeyColor(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("boundToColor", NBT.TAG_INT) ?
                stack.getTagCompound().getInteger("boundToColor") : 0;
    }

    protected String formatBlockPos(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
            float hitY, float hitZ, EnumHand hand) {

        if (!world.isRemote && player.getHeldItem(hand).getMetadata() == 2) {
            ItemStack stack = player.getHeldItem(hand);
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) {
                IWardedTile warded = tile.getCapability(CapabilityWardedTile.WARDED_TILE, null);
                if (!player.isSneaking() && !stack.hasTagCompound() && warded != null) {
                    WardAuthenticatorThaumiumKey key = (WardAuthenticatorThaumiumKey) stack.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null);
                    key.setOwner(player.getUniqueID());
                    key.setOwnerName(player.getName());
                    key.setBoundPosition(pos);
                    key.setBoundType(warded.getUniqueTypeID());
                    key.setBoundTypeName(world.getBlockState(pos).getBlock().getTranslationKey() + ".name");
    
                    stack.setTagCompound(new NBTTagCompound());
                    stack.getTagCompound().setInteger("boundToColor", generateKeyColor(player.getUniqueID()));
                    
                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.key_bound_object", 
                            new TextComponentTranslation(world.getBlockState(pos).getBlock().getTranslationKey() + ".name"), formatBlockPos(pos)), true);
                    return EnumActionResult.SUCCESS;
                }
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

        if (!world.isRemote) {
            ItemStack stack = player.getHeldItem(hand);
            IWardAuthenticator auth = stack.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null);
            if (auth instanceof WardAuthenticatorKey) {
                WardAuthenticatorKey key = (WardAuthenticatorKey) auth;
                if (!player.isSneaking() && !stack.hasTagCompound() && stack.getMetadata() != 2) {
                    key.setOwner(player.getUniqueID());
                    key.setOwnerName(player.getName());
                    stack.setTagCompound(new NBTTagCompound());
                    stack.getTagCompound().setInteger("boundToColor", generateKeyColor(player.getUniqueID()));
                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.key_bound"), true);
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
                else if (player.isSneaking() && stack.hasTagCompound()) {
                    key.reset();
                    stack.setTagCompound(null);
                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.key_unbound"), true);
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }
    
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.hasTagCompound())
            tag.setTag("item", stack.getTagCompound().copy());
        
        tag.setTag("cap", ((WardAuthenticatorKey) stack.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null)).serializeNBT());
        return tag;
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("cap", NBT.TAG_COMPOUND))
                ((WardAuthenticatorKey) stack.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null)).deserializeNBT(nbt.getCompoundTag("cap"));
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
    public boolean hasContainerItem(ItemStack stack) {
        return stack.getMetadata() == 1 || stack.getMetadata() == 2;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        if (stack.getMetadata() == 1)
            return stack.copy();
        else if (stack.getMetadata() == 2 && stack.hasTagCompound())
            return stack.copy();
        else
            return ItemStack.EMPTY;
    }
    
    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return TAMaterials.RARITY_MAGICAL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
        IWardAuthenticator auth = stack.getCapability(CapabilityWardAuthenticator.WARD_AUTHENTICATOR, null);
        if (auth instanceof WardAuthenticatorKey) {
            WardAuthenticatorKey key = (WardAuthenticatorKey) auth;
            if (key.hasOwner()) {
                tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.bound_to", 
                        key.getOwnerName()).getFormattedText());
                if (stack.getMetadata() == 2 && key instanceof WardAuthenticatorThaumiumKey) {
                    WardAuthenticatorThaumiumKey thaum = (WardAuthenticatorThaumiumKey) key;
                    tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.bound_to_type", 
                            new TextComponentTranslation(thaum.getBoundTypeName())).getFormattedText());
                    tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.bound_to_pos", 
                            formatBlockPos(thaum.getBoundPosition())).getFormattedText());
                }
            }
        }
    }

}
