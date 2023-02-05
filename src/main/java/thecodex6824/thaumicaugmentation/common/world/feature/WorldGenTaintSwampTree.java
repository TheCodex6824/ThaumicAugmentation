package thecodex6824.thaumicaugmentation.common.world.feature;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.common.util.Constants.BlockFlags;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;

public class WorldGenTaintSwampTree extends WorldGenAbstractTree {

	protected Block log;
	protected IBlockState leaves;
	
	public WorldGenTaintSwampTree() {
		super(false);
		log = Blocks.LOG;
		leaves = Blocks.LEAVES.getDefaultState().withProperty(BlockLeaves.DECAYABLE, false);
	}
	
	@Override
	protected void setBlockAndNotifyAdequately(World world, BlockPos pos, IBlockState state) {
		world.setBlockState(pos, state, BlockFlags.SEND_TO_CLIENTS | BlockFlags.NO_OBSERVERS);
	}
	
	@Override
	protected boolean canGrowInto(Block blockType) {
		return blockType.getDefaultState().getMaterial().isLiquid() || blockType == log || super.canGrowInto(blockType);
	}
	
	@Override
	protected void setDirtAt(World world, BlockPos pos) {
		// currently unused because we don't call any sapling callback since it doesn't exist
		IBlockState voidStone = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_VOID);
		if (world.getBlockState(pos) != voidStone) {
            setBlockAndNotifyAdequately(world, pos, voidStone);
        }
	}
	
	protected int getHeightOpaqueOnly(World world, BlockPos start) {
		MutableBlockPos mutable = new MutableBlockPos(start);
		mutable.setY(mutable.getY() - 1);
		while (mutable.getY() >= 0) {
			IBlockState state = world.getBlockState(mutable);
			if (state.getMaterial().isOpaque()) {
				return mutable.getY() + 1;
			}
			
			mutable.setY(mutable.getY() - 1);
		}
		
		return 0;
	}
	
	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		int treeHeight = rand.nextInt(9) + 10;
		MutableBlockPos mutable = new MutableBlockPos(pos);
		int growthHeight = getHeightOpaqueOnly(world, pos);
		mutable.setY(growthHeight - 1);
		IBlockState ground = world.getBlockState(mutable);
		if (ground.getBlock() == log || ground == leaves) {
			return false;
		}
		
		for (int y = pos.getY(); y < pos.getY() + treeHeight; ++y) {
			mutable.setY(y);
			IBlockState current = world.getBlockState(mutable);
			if (!canGrowInto(current.getBlock())) {
				return false;
			}
		}
		
		for (int y = pos.getY(); y < pos.getY() + treeHeight; ++y) {
			mutable.setY(y);
			setBlockAndNotifyAdequately(world, mutable, log.getDefaultState().withProperty(BlockLog.LOG_AXIS, EnumAxis.Y));
		}
		
		for (EnumFacing dir : EnumFacing.HORIZONTALS) {
			int branchHeight = rand.nextInt(7);
			mutable.setPos(pos.getX(), pos.getY() + branchHeight, pos.getZ());
			int offset = rand.nextInt(3) - 1;
			IBlockState logDir = null;
			if (dir.getAxis() == Axis.X) {
				logDir = log.getDefaultState().withProperty(BlockLog.LOG_AXIS, EnumAxis.X);
				if (offset != 0) {
					mutable.setPos(mutable.getX(), mutable.getY(), mutable.getZ() + offset);
					BlockPos extraBlock = mutable.add(0, rand.nextInt(3) - 1, 0);
					if (canGrowInto(world.getBlockState(extraBlock).getBlock())) {
						setBlockAndNotifyAdequately(world, extraBlock, logDir);
					}
				}
			}
			else {
				logDir = log.getDefaultState().withProperty(BlockLog.LOG_AXIS, EnumAxis.Z);
				if (offset != 0) {
					mutable.setPos(mutable.getX() + offset, mutable.getY(), mutable.getZ());
					BlockPos extraBlock = mutable.add(0, rand.nextInt(3) - 1, 0);
					if (canGrowInto(world.getBlockState(extraBlock).getBlock())) {
						setBlockAndNotifyAdequately(world, extraBlock, logDir);
					}
				}
			}
			
			int logNum = 0;
			int horizLogs = rand.nextInt(3) + 1;
			mutable.setPos(mutable.getX() + dir.getXOffset(), mutable.getY(), mutable.getZ() + dir.getZOffset());
			while (mutable.getY() >= 0 && canGrowInto(world.getBlockState(mutable).getBlock())) {
				setBlockAndNotifyAdequately(world, mutable, logDir);
				if (logNum < horizLogs) {
					mutable.setPos(mutable.getX() + dir.getXOffset(), mutable.getY(), mutable.getZ() + dir.getZOffset());
				}
				else if (logNum == horizLogs) {
					for (int i = 0; i < 1 + rand.nextInt(2); ++i) {
						mutable.setPos(mutable.getX(), mutable.getY() - 1, mutable.getZ());
						if (canGrowInto(world.getBlockState(mutable).getBlock())) {
							setBlockAndNotifyAdequately(world, mutable, logDir);
							mutable.setPos(mutable.getX() + dir.getXOffset(), mutable.getY(), mutable.getZ() + dir.getZOffset());
							if (canGrowInto(world.getBlockState(mutable).getBlock())) {
								setBlockAndNotifyAdequately(world, mutable, logDir);
							}
						}
					}
					
					mutable.setPos(mutable.getX(), mutable.getY() - 1, mutable.getZ());
					if (canGrowInto(world.getBlockState(mutable).getBlock())) {
						setBlockAndNotifyAdequately(world, mutable, logDir);
					}
					
					logDir = log.getDefaultState().withProperty(BlockLog.LOG_AXIS, EnumAxis.Y);
					mutable.setPos(mutable.getX() + dir.getXOffset(), mutable.getY(), mutable.getZ() + dir.getZOffset());
				}
				else {
					mutable.setPos(mutable.getX(), mutable.getY() - 1, mutable.getZ());
				}
				
				++logNum;
			}
		}
		
		int leafRadius = rand.nextInt(3) + 3;
		for (int y = -1; y <= 1; ++y) {
			int leafHeightOffset = y == 0 ? 1 : 0;
			for (int x = -leafRadius; x <= leafRadius; ++x) {
				for (int z = -leafRadius; z <= leafRadius; ++z) {
					if ((x != 0 || z != 0 || y > 0) && x * x + z * z < (leafRadius + leafHeightOffset) * (leafRadius + leafHeightOffset)) {
						mutable.setPos(pos.getX() + x, pos.getY() + treeHeight - 1 + y, pos.getZ() + z);
						IBlockState current = world.getBlockState(mutable);
						if (current.getBlock().isAir(current, world, mutable) || current.getBlock().isReplaceable(world, mutable)) {
							setBlockAndNotifyAdequately(world, mutable, leaves);
						}
					}
				}
			}
		}
		
		return true;
	}
	
}
