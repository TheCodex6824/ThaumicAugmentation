/**
 *	Thaumic Augmentation
 *	Copyright (c) 2019 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.item.foci;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.Trajectory;

import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;

import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.tile.TileTemporaryLight;

public class FocusEffectLight extends FocusEffect {

	@Override
	public Aspect getAspect() {
		return Aspect.LIGHT;
	}
	
	@Override
	public int getComplexity() {
		return getSettingValue("duration") / 5 + (int) (getSettingValue("intensity") * 1.25F);
	}
	
	@Override
	public String getKey() {
		return "focus." + ThaumicAugmentationAPI.MODID + ".light";
	}
	
	@Override
	public String getResearch() {
		return "FOCUS_LIGHT";
	}
	
	@Override
	public boolean execute(RayTraceResult result, Trajectory trajectory, float finalPower, int something) {
		if (result.typeOfHit == Type.BLOCK) {
			IBlockState state = getPackage().world.getBlockState(result.getBlockPos());
			if (state.getBlock().isAir(state, getPackage().world, result.getBlockPos()) || 
					state.getBlock().isReplaceable(getPackage().world, result.getBlockPos())) {
				
				return placeLightSource(result.getBlockPos(), getSettingValue("duration"), getSettingValue("intensity"));
			}
			else {
				BlockPos pos = result.getBlockPos().offset(result.sideHit);
				state = getPackage().world.getBlockState(pos);
				if (state.getBlock().isAir(state, getPackage().world, pos) || 
					state.getBlock().isReplaceable(getPackage().world, pos))
					
					return placeLightSource(pos, getSettingValue("duration"), getSettingValue("intensity"));
			}
		}
		else if (result.typeOfHit == Type.MISS) 
			return placeLightSource(result.getBlockPos(), getSettingValue("duration"), getSettingValue("intensity"));
		else if (result.entityHit instanceof EntityLivingBase) {
			((EntityLivingBase) result.entityHit).addPotionEffect(new PotionEffect(MobEffects.GLOWING, getSettingValue("duration") * 20, 0, true, false));
			return true;
		}
		
		return false;
	}
	
	protected boolean placeLightSource(BlockPos pos, int duration, int intensity) {
		boolean result = getPackage().world.setBlockState(pos, TABlocks.TEMPORARY_LIGHT.getDefaultState());
		if (getPackage().world.getTileEntity(pos) instanceof TileTemporaryLight) {
			TileTemporaryLight tile = (TileTemporaryLight) getPackage().world.getTileEntity(pos);
			tile.setTicksRemaining(duration * 20);
			tile.setLightLevel(intensity);
		}
		
		return result;
	}
	
	@Override
	public NodeSetting[] createSettings() {
		return new NodeSetting[] {
				new NodeSetting("intensity", "focus." + ThaumicAugmentationAPI.MODID + ".light.intensity", new NodeSetting.NodeSettingIntRange(1, 15)),
				new NodeSetting("duration", "focus." + ThaumicAugmentationAPI.MODID + ".light.duration", new NodeSetting.NodeSettingIntRange(10, 180))
		};
	}
	
	@Override
	public void onCast(Entity caster) {
		caster.world.playSound(null, caster.getPosition().up(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 
				SoundCategory.PLAYERS, 0.2F, 1.2F);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderParticleFX(World world, double posX, double posY, double posZ, double velX, double velY,
			double velZ) {
		
		FXGeneric fb = new FXGeneric(world, posX, posY, posZ, velX, velY, velZ);
	    fb.setMaxAge(40 + world.rand.nextInt(40));
	    fb.setParticles(16, 1, 1);
	    fb.setSlowDown(0.5D);
	    fb.setAlphaF(new float[] { 1.0F, 0.0F });
	    fb.setScale(new float[] { (float)(0.699999988079071D + world.rand.nextGaussian() * 0.30000001192092896D) });
	    int color = getAspect().getColor();
	    fb.setRBGColorF(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
	    fb.setRotationSpeed(world.rand.nextFloat(), 0.0F);
	    ParticleEngine.addEffectWithDelay(world, fb, 0);
	}
	
}
