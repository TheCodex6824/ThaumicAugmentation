package thecodex6824.thaumicaugmentation.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import thaumcraft.common.blocks.world.taint.ITaintBlock;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.TAFluids;
import thecodex6824.thaumicaugmentation.api.TAMaterials;

public class BlockTaintedSlurry extends BlockFluidClassic implements ITaintBlock {

	public BlockTaintedSlurry() {
		super(TAFluids.TAINTED_SLURRY, TAMaterials.TAINTED_SLURRY);
		setTickRandomly(false);
		tickRate = 10;
	}
	
	@Override
	public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos pos, IBlockState state, Entity entity,
			double yToTest, Material material, boolean testingHead) {
		
		double height = getFilledPercentage(world, pos);
		boolean res = yToTest >= pos.getY() + 1 - height;
		if (res && (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator())) {
			entity.motionX *= 0.75;
			entity.motionY *= (entity.motionY > 0.0 ? 0.75 : 1.0);
			entity.motionZ *= 0.75;
		}
		
		return res;
	}
	
	@Override
	public boolean canCollideCheck(IBlockState state, boolean fullHit) {
		return fullHit;
	}
	
	@Override
	public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d vec) {
		Vec3d parent = super.modifyAcceleration(world, pos, entity, vec);
		return parent.scale(0.95);
	}
	
	@Override
	public SoundType getSoundType() {
		return SoundsTC.GORE;
	}
	
	@Override
	public void die(World world, BlockPos pos, IBlockState state) {}
	
}
