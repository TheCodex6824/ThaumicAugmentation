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

package thecodex6824.thaumicaugmentation.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IWardOpenedBlock;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorHalf;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorHalf.ArcaneDoorHalf;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorHinge;
import thecodex6824.thaumicaugmentation.api.block.property.door.IArcaneDoorOpen;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemArcaneDoor extends ItemTABase {

    public ItemArcaneDoor() {
        super("greatwood", "thaumium", "silverwood");
    }
        
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, 
            float hitX, float hitY, float hitZ) {

        IBlockState iblockstate = world.getBlockState(pos);
        Block block = iblockstate.getBlock();
        if (!block.isReplaceable(world, pos))
            pos = pos.offset(facing);

        ItemStack itemstack = player.getHeldItem(hand);
        Block toPlace = null;
        if (itemstack.getMetadata() == 0)
            toPlace = TABlocks.ARCANE_DOOR_GREATWOOD;
        else if (itemstack.getMetadata() == 1)
            toPlace = TABlocks.ARCANE_DOOR_THAUMIUM;
        else
            toPlace = TABlocks.ARCANE_DOOR_SILVERWOOD;
        if (player.canPlayerEdit(pos, facing, itemstack) && toPlace.canPlaceBlockAt(world, pos)) {
            EnumFacing enumfacing = player.getHorizontalFacing();
            int i = enumfacing.getXOffset();
            int j = enumfacing.getZOffset();
            boolean flag = i < 0 && hitZ < 0.5F || i > 0 && hitZ > 0.5F || j < 0 && hitX > 0.5F || j > 0 && hitX < 0.5F;
            placeDoor(player, itemstack, world, pos, enumfacing, toPlace, flag);
            SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
            world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
        else
            return EnumActionResult.FAIL;
    }

    protected static void placeDoor(EntityPlayer player, ItemStack stack, World worldIn, BlockPos pos, EnumFacing facing, Block door, boolean isRightHinge) {
        BlockPos blockpos = pos.offset(facing.rotateY());
        BlockPos blockpos1 = pos.offset(facing.rotateYCCW());
        int i = (worldIn.getBlockState(blockpos1).isNormalCube() ? 1 : 0) + (worldIn.getBlockState(blockpos1.up()).isNormalCube() ? 1 : 0);
        int j = (worldIn.getBlockState(blockpos).isNormalCube() ? 1 : 0) + (worldIn.getBlockState(blockpos.up()).isNormalCube() ? 1 : 0);
        boolean flag = worldIn.getBlockState(blockpos1).getBlock() == door || worldIn.getBlockState(blockpos1.up()).getBlock() == door;
        boolean flag1 = worldIn.getBlockState(blockpos).getBlock() == door || worldIn.getBlockState(blockpos.up()).getBlock() == door;

        if ((!flag || flag1) && j <= i) {
            if (flag1 && !flag || j < i)
                isRightHinge = false;
        }
        else
            isRightHinge = true;

        BlockPos blockpos2 = pos.up();
        worldIn.setBlockState(pos, door.getDefaultState().withProperty(IArcaneDoorHalf.DOOR_HALF, ArcaneDoorHalf.LOWER).withProperty(IArcaneDoorHinge.HINGE_SIDE, isRightHinge ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT).withProperty(IArcaneDoorOpen.DOOR_OPEN, false), 2);
        worldIn.setBlockState(blockpos2, door.getDefaultState().withProperty(IArcaneDoorHalf.DOOR_HALF, ArcaneDoorHalf.UPPER).withProperty(IHorizontallyDirectionalBlock.DIRECTION, facing).withProperty(IWardOpenedBlock.WARD_OPENED, false), 2);
        worldIn.getBlockState(pos).getBlock().onBlockPlacedBy(worldIn, pos, worldIn.getBlockState(pos), player, stack);
        worldIn.notifyNeighborsOfStateChange(pos, door, false);
        worldIn.notifyNeighborsOfStateChange(blockpos2, door, false);
    }

}
