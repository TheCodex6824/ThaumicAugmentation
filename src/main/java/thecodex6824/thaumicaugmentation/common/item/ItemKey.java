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
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.item.IWardAuthenticator;
import thecodex6824.thaumicaugmentation.api.warded.CapabilityWardedTile;
import thecodex6824.thaumicaugmentation.api.warded.IWardedTile;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemKey extends ItemTABase implements IWardAuthenticator {

    public ItemKey() {
        super("iron", "brass", "thaumium");
        setMaxStackSize(1);
    }

    @Override
    public boolean permitsUsage(IWardedTile tile, ItemStack stack, EntityPlayer user) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("boundTo", NBT.TAG_STRING))
            return false;

        if (stack.getMetadata() != 2)
            return stack.getTagCompound().getString("boundTo").equals(tile.getOwner());
        else {
            if (stack.getTagCompound().hasKey("boundType", NBT.TAG_STRING) && stack.getTagCompound().hasKey("boundBlockPos", NBT.TAG_INT_ARRAY)) {
                int[] pos = stack.getTagCompound().getIntArray("boundBlockPos");
                if (pos.length == 3) {
                    BlockPos block = new BlockPos(pos[0], pos[1], pos[2]);
                    return stack.getTagCompound().getString("boundTo").equals(tile.getOwner()) &&
                            stack.getTagCompound().getString("boundType").equals(tile.getUniqueTypeID()) &&
                            block.equals(tile.getPosition());
                }
                else
                    return false;
            }
            else
                return false;
        }
    }

    protected int generateKeyColor(String id) {
        UUID uuid = null;
        try {
            uuid = UUID.fromString(id);
        }
        catch (IllegalArgumentException ex) {}

        return uuid != null ? uuid.hashCode() : id.hashCode();
    }

    public int getKeyColor(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("boundToColor", NBT.TAG_INT) ?
                stack.getTagCompound().getInteger("boundToColor") : 0;
    }

    public void setBoundTo(ItemStack stack, String display, String id) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setString("boundTo", id.toString());
        stack.getTagCompound().setString("boundToDisplay", display);
        stack.getTagCompound().setInteger("boundToColor", generateKeyColor(id));
    }

    public void setBoundBlock(ItemStack stack, BlockPos pos, String type, String typeDisplay) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setIntArray("boundBlockPos", new int[] {pos.getX(), pos.getY(), pos.getZ()});
        stack.getTagCompound().setString("boundType", type);
        stack.getTagCompound().setString("boundTypeDisplay", typeDisplay);
    }

    protected String formatBlockPos(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    protected String formatBlockPos(int[] pos) {
        return "(" + pos[0] + ", " + pos[1] + ", " + pos[2] + ")";
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
            float hitY, float hitZ, EnumHand hand) {

        if (!world.isRemote && player.getHeldItem(hand).getMetadata() == 2) {
            ItemStack stack = player.getHeldItem(hand);
            TileEntity tile = world.getChunk(pos).getTileEntity(pos, EnumCreateEntityType.CHECK);
            if (!player.isSneaking() && !stack.hasTagCompound() && tile != null && tile.hasCapability(CapabilityWardedTile.WARDED_TILE, null)) {
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setString("boundTo", player.getUniqueID().toString());
                stack.getTagCompound().setString("boundToDisplay", player.getName());
                stack.getTagCompound().setInteger("boundToColor", generateKeyColor(player.getUniqueID().toString()));

                stack.getTagCompound().setIntArray("boundBlockPos", new int[] {pos.getX(), pos.getY(), pos.getZ()});
                stack.getTagCompound().setString("boundType", tile.getCapability(CapabilityWardedTile.WARDED_TILE, null).getUniqueTypeID());
                stack.getTagCompound().setString("boundTypeDisplay", world.getBlockState(pos).getBlock().getTranslationKey() + ".name");

                player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.key_bound_object", 
                        new TextComponentTranslation(world.getBlockState(pos).getBlock().getTranslationKey() + ".name"), formatBlockPos(pos)), true);
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

        if (!world.isRemote) {
            ItemStack stack = player.getHeldItem(hand);
            if (!player.isSneaking() && !stack.hasTagCompound() && stack.getMetadata() != 2) {
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setString("boundTo", player.getUniqueID().toString());
                stack.getTagCompound().setString("boundToDisplay", player.getName());
                stack.getTagCompound().setInteger("boundToColor", generateKeyColor(player.getUniqueID().toString()));
                player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.key_bound"), true);
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
            else if (player.isSneaking() && stack.hasTagCompound()) {
                stack.setTagCompound(null);
                player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.key_unbound"), true);
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
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
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("boundToDisplay")) {

            tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.bound_to", 
                    stack.getTagCompound().getString("boundToDisplay")).getFormattedText());

            if (stack.getMetadata() == 2 && stack.getTagCompound().hasKey("boundTypeDisplay", NBT.TAG_STRING)) {
                tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.bound_to_type", 
                        new TextComponentTranslation(stack.getTagCompound().getString("boundTypeDisplay"))).getFormattedText());

                int[] pos = stack.getTagCompound().getIntArray("boundBlockPos");
                if (pos.length == 3) {
                    tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.bound_to_pos", 
                            formatBlockPos(pos)).getFormattedText());
                }
            }
        }
    }

}
