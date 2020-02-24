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

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskPart;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskPart.ObeliskPart;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskType;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskType.ObeliskType;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemObeliskPlacer extends ItemTABase {

    public ItemObeliskPlacer() {
        super("eldritch", "ancient");
        setHasSubtypes(true);
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!world.isRemote && player.canPlayerEdit(pos.offset(facing), facing, player.getHeldItem(hand))) {
            BlockPos offset = pos.offset(facing);
            for (int i = 0; i < 5; ++i) {
                IBlockState state = world.getBlockState(offset.offset(EnumFacing.UP, i));
                if (!state.getBlock().isReplaceable(world, offset.offset(EnumFacing.UP, i)))
                    return EnumActionResult.FAIL;
            }
            
            ObeliskType type = player.getHeldItem(hand).getMetadata() == 0 ? ObeliskType.ELDRITCH : ObeliskType.ANCIENT;
            for (int i = 0; i < 5; ++i) {
                if (i == 0 || i == 4) {
                    world.setBlockState(offset.offset(EnumFacing.UP, i), TABlocks.OBELISK.getDefaultState().withProperty(
                            IObeliskPart.OBELISK_PART,ObeliskPart.CAP).withProperty(IObeliskType.OBELISK_TYPE, type), 2);
                }
                else if (i != 2) {
                    world.setBlockState(offset.offset(EnumFacing.UP, i), TABlocks.OBELISK.getDefaultState().withProperty(
                            IObeliskPart.OBELISK_PART, ObeliskPart.INNER).withProperty(IObeliskType.OBELISK_TYPE, type), 2);
                }
                else {
                    world.setBlockState(offset.offset(EnumFacing.UP, i), TABlocks.OBELISK.getDefaultState().withProperty(
                            IObeliskPart.OBELISK_PART, ObeliskPart.MIDDLE).withProperty(IObeliskType.OBELISK_TYPE, type), 2);
                }
            }

            if (!player.isCreative())
                player.getHeldItem(hand).shrink(1);
            
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }
    
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.creative_only").setStyle(new Style().setColor(TextFormatting.DARK_PURPLE)).getFormattedText());
    }
    
}
