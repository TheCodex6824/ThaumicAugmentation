/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.item.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemBlockNoImpetusNodeNBT extends ItemBlock {

    public ItemBlockNoImpetusNodeNBT(Block block) {
        super(block);
    }
    
    protected void setTileEntityNBTCustom(World world, @Nullable EntityPlayer player, BlockPos pos, ItemStack stack) {
        if (!world.isRemote) {
            NBTTagCompound tag = stack.getSubCompound("BlockEntityTag");
            if (tag != null) {
                TileEntity tile = world.getTileEntity(pos);
                if (tile != null && (!tile.onlyOpsCanSetNbt() || (player != null && player.canUseCommandBlock()))) {
                    NBTTagCompound nbttagcompound1 = tile.writeToNBT(new NBTTagCompound());
                    NBTTagCompound nbttagcompound2 = nbttagcompound1.copy();
                    tag.removeTag("node");
                    nbttagcompound1.merge(tag);
                    nbttagcompound1.setInteger("x", pos.getX());
                    nbttagcompound1.setInteger("y", pos.getY());
                    nbttagcompound1.setInteger("z", pos.getZ());
                    if (!nbttagcompound1.equals(nbttagcompound2)) {
                        tile.readFromNBT(nbttagcompound1);
                        tile.markDirty();
                    }
                }
            }
        }
    }
    
    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, IBlockState newState) {
        
        if (!world.setBlockState(pos, newState, 11))
            return false;

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == block) {
            setTileEntityNBTCustom(world, player, pos, stack);
            block.onBlockPlacedBy(world, pos, state, player, stack);

            if (player instanceof EntityPlayerMP)
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
        }

        return true;
    }
    
}
