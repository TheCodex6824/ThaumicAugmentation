package thecodex6824.thaumicaugmentation.common.fluid;

import java.awt.Color;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aura.AuraHelper;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

public class FluidTaintedSlurry extends Fluid {

	public FluidTaintedSlurry() {
		super("tainted_slurry",
				new ResourceLocation(ThaumicAugmentationAPI.MODID, "blocks/tainted_slurry_still"),
				new ResourceLocation(ThaumicAugmentationAPI.MODID, "blocks/tainted_slurry_flowing"));
		setColor(Color.MAGENTA);
		setDensity(1050);
		setTemperature(315);
		setViscosity(4000);
	}
	
	@Override
	public boolean doesVaporize(FluidStack fluidStack) {
		return true;
	}
	
	@Override
	public void vaporize(EntityPlayer player, World world, BlockPos pos, FluidStack fluidStack) {
		super.vaporize(player, world, pos, fluidStack);
		AuraHelper.polluteAura(world, pos, (int) Math.ceil(fluidStack.amount / 100), true);
	}
	
}
