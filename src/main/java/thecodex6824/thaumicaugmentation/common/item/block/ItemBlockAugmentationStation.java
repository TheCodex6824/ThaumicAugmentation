package thecodex6824.thaumicaugmentation.common.item.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.common.block.BlockAugmentationStation;

public class ItemBlockAugmentationStation extends ItemBlock {

	public ItemBlockAugmentationStation(BlockAugmentationStation block) {
		super(block);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		IBlockState existingState = world.getBlockState(pos);
        if (!existingState.getBlock().isReplaceable(world, pos)) {
            pos = pos.offset(facing);
        }

        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack) &&
        		world.mayPlace(block, pos, false, facing, player)) {
        	
            int meta = getMetadata(stack.getMetadata());
            IBlockState toPlace = block.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, player, hand);
            if (((BlockAugmentationStation) block).canPlaceBlockAtStateful(world, pos, toPlace) &&
            		placeBlockAt(stack, player, world, pos, facing, hitX, hitY, hitZ, toPlace)) {
            	
            	toPlace = world.getBlockState(pos);
                SoundType soundtype = toPlace.getBlock().getSoundType(toPlace, world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.shrink(1);
                return EnumActionResult.SUCCESS;
            }
        }
        
        return EnumActionResult.FAIL;
	}
	
}
