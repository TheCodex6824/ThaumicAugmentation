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

package thecodex6824.thaumicaugmentation.api.impetus.node;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public final class NodeHelper {

    private NodeHelper() {}
    
    @SuppressWarnings("null")
    public static boolean handleCasterInteract(ICapabilityProvider provider, World world, ItemStack stack, EntityPlayer player, BlockPos pos, 
            EnumFacing face, EnumHand hand) {
        
        if (!world.isRemote) {
            if (!stack.hasTagCompound())
                stack.setTagCompound(new NBTTagCompound());
            
            if (player.isSneaking()) {
                stack.getTagCompound().setIntArray("impetusBindSelection", new int[] {pos.getX(), pos.getY(), pos.getZ(),
                        world.provider.getDimension()});
                return true;
            }
            else if (stack.getTagCompound().hasKey("impetusBindSelection", NBT.TAG_INT_ARRAY)) {
                DimensionalBlockPos first = new DimensionalBlockPos(stack.getTagCompound().getIntArray("impetusBindSelection"));
                IImpetusNode node = provider.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                if (!first.equals(node.getLocation()) && world.isBlockLoaded(first.getPos())) {
                    TileEntity te = world.getChunk(first.getPos()).getTileEntity(first.getPos(), EnumCreateEntityType.CHECK);
                    if (te != null) {
                        IImpetusNode otherNode = te.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                        if (otherNode != null) {
                            if (node.getInputLocations().contains(first))
                                node.removeInput(te.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null));
                            else {
                                if (otherNode.getNumOutputs() >= otherNode.getMaxOutputs())
                                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_limit_out"), true);
                                else if (node.getNumInputs() >= node.getMaxInputs())
                                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_limit_in"), true);
                                else {
                                    node.addInput(te.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null));
                                    te.markDirty();
                                }
                            }
                        }
                    }
                }
                else
                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.impetus_link_same_pos"), true);
                    
                return true;
            }
        }
        
        return false;
    }
    
}
