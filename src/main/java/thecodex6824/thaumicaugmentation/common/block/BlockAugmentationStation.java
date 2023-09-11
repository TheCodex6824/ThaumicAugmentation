/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 WillPastor.
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

package thecodex6824.thaumicaugmentation.common.block;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thecodex6824.thaumicaugmentation.api.block.property.IAugmentationStationPart;
import thecodex6824.thaumicaugmentation.api.block.property.IAugmentationStationPart.AugmentationStationPart;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockSimpleStation;
import thecodex6824.thaumicaugmentation.common.item.block.ItemBlockAugmentationStation;
import thecodex6824.thaumicaugmentation.common.tile.TileAugmentationStation;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;
import thecodex6824.thaumicaugmentation.init.GUIHandler.TAInventory;

public class BlockAugmentationStation extends BlockSimpleStation {
	
	public BlockAugmentationStation() {
		super();
		setDefaultState(getDefaultState().withRotation(Rotation.NONE).withProperty(
				IAugmentationStationPart.AUGMENTATION_STATION_PART, AugmentationStationPart.LOWER_RIGHT));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, IHorizontallyDirectionalBlock.DIRECTION,
				IAugmentationStationPart.AUGMENTATION_STATION_PART);
	}
	
	@Override
	protected TAInventory getInventoryType() {
		return TAInventory.AUGMENTATION_STATION;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return state.getValue(IAugmentationStationPart.AUGMENTATION_STATION_PART).hasTile();
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileAugmentationStation();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = getDefaultState().withProperty(IHorizontallyDirectionalBlock.DIRECTION,
				EnumFacing.byHorizontalIndex(BitUtil.getBits(meta, 0, 2)));
		state = state.withProperty(IAugmentationStationPart.AUGMENTATION_STATION_PART,
				AugmentationStationPart.fromMeta(BitUtil.getBits(meta, 2, 4)));
		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = BitUtil.setBits(0, 0, 2,
				state.getValue(IHorizontallyDirectionalBlock.DIRECTION).getHorizontalIndex());
		return BitUtil.setBits(meta, 2, 4,
				state.getValue(IAugmentationStationPart.AUGMENTATION_STATION_PART).getMeta());
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return state.getValue(IAugmentationStationPart.AUGMENTATION_STATION_PART).hasTile() ?
				EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE;
	}
	
	public boolean canPlaceBlockAtStateful(World world, BlockPos pos, IBlockState toPlace) {
		EnumFacing dir = toPlace.getValue(IHorizontallyDirectionalBlock.DIRECTION);
        for (AugmentationStationPart part : AugmentationStationPart.values()) {
        	BlockPos offset = part.getOffsetFromTile(pos, dir);
        	if (!world.isBlockLoaded(offset) || !canPlaceBlockAt(world, offset)) {
        		return false;
        	}
        }
        
        return true;
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(
				IAugmentationStationPart.AUGMENTATION_STATION_PART, AugmentationStationPart.LOWER_RIGHT);
	}
    
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
            ItemStack stack) {
 
    	EnumFacing myDirection = state.getValue(IHorizontallyDirectionalBlock.DIRECTION);
    	for (Map.Entry<AugmentationStationPart, BlockPos> otherPos : AugmentationStationPart.getAllPositions(pos,
    			state.getValue(IHorizontallyDirectionalBlock.DIRECTION)).entrySet()) {
    		
    		if (!otherPos.getKey().hasTile()) {
    			world.setBlockState(otherPos.getValue(), getDefaultState()
    					.withProperty(IHorizontallyDirectionalBlock.DIRECTION, myDirection)
    					.withProperty(IAugmentationStationPart.AUGMENTATION_STATION_PART,
                        otherPos.getKey()), BlockFlags.SEND_TO_CLIENTS);
    		}
    	}
    }
    
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
    	AugmentationStationPart part = state.getValue(IAugmentationStationPart.AUGMENTATION_STATION_PART);
    	EnumFacing facing = state.getValue(IHorizontallyDirectionalBlock.DIRECTION);
    	for (Map.Entry<AugmentationStationPart, BlockPos> otherPos : AugmentationStationPart.getAllPositions(part.getTilePos(pos, facing),
    			state.getValue(IHorizontallyDirectionalBlock.DIRECTION)).entrySet()) {
    			
    		if (otherPos.getKey() != part && otherPos.getValue().equals(fromPos)) {
	    		IBlockState theirState = world.getBlockState(otherPos.getValue());
	    		boolean destroy = theirState.getBlock() != this;
	    		if (!destroy) {
	    			BlockPos myTilePos = part.getTilePos(pos, facing);
	    			BlockPos theirTilePos = theirState.getValue(IAugmentationStationPart.AUGMENTATION_STATION_PART)
	    					.getTilePos(fromPos, theirState.getValue(IHorizontallyDirectionalBlock.DIRECTION));
	    			if (!myTilePos.equals(theirTilePos)) {
	    				destroy = true;
	    			}
	    		}
	    		
	    		if (destroy) {
	    			world.setBlockToAir(pos);
	    		}
	    		
	    		break;
    		}
    	}
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
    		EnumFacing facing, float hitX, float hitY, float hitZ) {
    	
    	AugmentationStationPart part = state.getValue(IAugmentationStationPart.AUGMENTATION_STATION_PART);
    	BlockPos tilePos = part.getTilePos(pos, state.getValue(IHorizontallyDirectionalBlock.DIRECTION));
    	if (!part.hasTile()) {
    		if (world.isBlockLoaded(tilePos)) {
	    		IBlockState tileState = world.getBlockState(tilePos);
	    		// make sure it's actually the tile, otherwise we will loop potentially forever and blow the call stack
	    		if (tileState.getValue(IAugmentationStationPart.AUGMENTATION_STATION_PART).hasTile()) {
	    			return onBlockActivated(world, tilePos, tileState, player, hand, facing, hitX, hitY, hitZ);
	    		}
    		}
    		
    		return false;
    	}
    	
    	return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
    	if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileAugmentationStation) {
                IItemHandler items = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (items != null) {
                    for (int i = 0; i < items.getSlots(); ++i) {
                        ItemStack stack = items.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            world.spawnEntity(new EntityItem(world, pos.getX() + 0.5 + world.rand.nextGaussian() / 2, 
                                    pos.getY() + 0.5 + Math.abs(world.rand.nextGaussian() / 2), pos.getZ() + 0.5 + world.rand.nextGaussian() / 2, stack));
                        }
                    }
                }
            }
        }
        
        super.breakBlock(world, pos, state);
    }
    
    @Override
    public ItemBlock createItemBlock() {
    	return new ItemBlockAugmentationStation(this);
    }
	
}


