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

package thecodex6824.thaumicaugmentation.common.item.foci;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.NodeSetting.NodeSettingIntRange;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.lib.network.fx.PacketFXFocusPartImpact;
import thaumcraft.common.tiles.TileThaumcraft;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationBotania;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationHandler;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class FocusEffectWater extends FocusEffect {

    private static void invoke(Consumer<TileEntity> c, TileEntity t) {
        c.accept(t);
    }
    
    private static boolean invokePredicate(Predicate<TileEntity> c, TileEntity t) {
        return c.test(t);
    }
    
    private static final Predicate<TileEntity> IS_APOTHECARY = 
        (tile) -> {
            return invokePredicate((t) -> {
                return ((IntegrationBotania) IntegrationHandler.getIntegration(IntegrationHandler.BOTANIA_MOD_ID)).isPetalApothecary(t);
            }, 
            tile);
        };
    
    private static final Consumer<TileEntity> FILL_APOTHECARY = 
        (tile) -> {
            invoke((t) -> {
                ((IntegrationBotania) IntegrationHandler.getIntegration(IntegrationHandler.BOTANIA_MOD_ID)).fillPetalApothecary(t);
            }, 
            tile);
        };
    
    @Override
    public Aspect getAspect() {
        return Aspect.WATER;
    }
    
    @Override
    public String getKey() {
        return "focus." + ThaumicAugmentationAPI.MODID + ".water";
    }
    
    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[] {new NodeSetting("power", "focus.common.power", new NodeSettingIntRange(0, 5))};
    }
    
    @Override
    public int getComplexity() {
        return 3 + getSettingValue("power") * 2;
    }
    
    @Override
    public float getDamageForDisplay(float finalPower) {
        return (getSettingValue("power") + 1) * finalPower;
    }
    
    @Override
    public String getResearch() {
        return "FOCUS_WATER";
    }
    
    @Override
    public boolean execute(RayTraceResult result, @Nullable Trajectory trajectory, float power, int something) {
        World world = getPackage().world;
        if (world.provider.doesWaterVaporize()) {
            world.playSound(null, result.getBlockPos(), TASounds.FOCUS_WATER_IMPACT, SoundCategory.PLAYERS, 0.25F, 1.0F);
            return false;
        }
        
        Vec3d hit = result.hitVec;
        TANetwork.INSTANCE.sendToAllTracking(new PacketFXFocusPartImpact(hit.x, hit.y, hit.z, new String[] { getKey() }), new TargetPoint(
            world.provider.getDimension(), hit.x, hit.y, hit.z, 64.0D));
        
        double rangeMod = 1.0;
        MutableBlockPos pos = null;
        Entity exclude = null;
        if (result.typeOfHit == Type.ENTITY && result.entityHit != null) {
            rangeMod = 0.5;
            exclude = result.entityHit;
            pos = new MutableBlockPos(result.entityHit.getPosition());
            if ((result.entityHit instanceof EntityCreature || result.entityHit instanceof EntityWaterMob || result.entityHit instanceof EntityPlayer) &&
                    ((EntityLivingBase) result.entityHit).canBreatheUnderwater()) {
                EntityLivingBase base = (EntityLivingBase) result.entityHit;
                base.setAir(Math.max(base.getAir(), 300));
            }
            else {
                float damageMultiplier = 1.0F;
                if (result.entityHit instanceof EntityBlaze || result.entityHit instanceof EntityMagmaCube)
                    damageMultiplier = 2.0F;
                else if (result.entityHit instanceof EntitySlime || result.entityHit instanceof EntityWitch)
                    damageMultiplier = 1.5F;
                
                if (result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(result.entityHit != null ? result.entityHit : getPackage().getCaster(),
                        getPackage().getCaster()), getDamageForDisplay(power) * damageMultiplier)) {
                    
                    if (result.entityHit instanceof EntityWolf)
                        ((EntityWolf) result.entityHit).isWet = true;
                    if (damageMultiplier > 1.0F) {
                        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, result.entityHit.getSoundCategory(), Math.min(damageMultiplier - 1.0F, 1.0F), 1.0F);
                        Vec3d eyes = result.entityHit.getPositionEyes(1.0F);
                        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SMOKE_LARGE,
                                eyes.x, eyes.y, eyes.z), result.entityHit);
                    }
                }
                
                if (result.entityHit.isBurning()) {
                    result.entityHit.extinguish();
                    world.playSound(null, pos, TASounds.FOCUS_WATER_IMPACT, result.entityHit.getSoundCategory(), 0.25F, 1.0F);
                }
            }
        } 
        else if (result.typeOfHit == Type.BLOCK)
            pos = new MutableBlockPos(result.getBlockPos().offset(result.sideHit));
        
        if (pos != null) {
            int maxDist = (int) ((getSettingValue("power") + 1) * rangeMod);
            if (maxDist > 0) {
                BlockPos start = pos.toImmutable();
                List<Entity> entities = world.getEntitiesInAABBexcluding(exclude, new AxisAlignedBB(start.getX() - maxDist / 2, start.getY() - maxDist / 2,
                        start.getZ() - maxDist / 2, start.getX() + Math.ceil(maxDist / 2.0), start.getY() + Math.ceil(maxDist / 2.0), start.getZ() + Math.ceil(maxDist / 2.0)),
                        entity -> entity != null && (entity.isBurning() || entity instanceof EntityWolf));
                
                ArrayList<Vec3d> splashPositions = new ArrayList<>();
                for (Entity e : entities) {
                    if (e.getDistanceSq(start) <= maxDist * maxDist) {
                        e.extinguish();
                        world.playSound(null, pos, TASounds.FOCUS_WATER_IMPACT, e.getSoundCategory(), 0.25F, 1.0F);
                        splashPositions.add(e.getPositionVector());
                        if (e instanceof EntityWolf)
                            ((EntityWolf) e).isWet = true;
                    }
                }
                
                for (int x = -maxDist; x < maxDist + 1; ++x) {
                    for (int z = -maxDist; z < maxDist + 1; ++z) {
                        pos.setPos(x + start.getX(), 0, z + start.getZ());
                        for (int y = -maxDist; y < maxDist + 1; ++y) {
                            pos.setY(y + start.getY());
                            if (x * x + y * y + z * z <= maxDist * maxDist) {
                                IBlockState state = world.getBlockState(pos);
                                if (state.getMaterial() == Material.LAVA && state.getPropertyKeys().contains(BlockLiquid.LEVEL) && 
                                        x == 0 && y == 0 && z == 0) {
                                    
                                    if (state.getValue(BlockLiquid.LEVEL) == 0)
                                        world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                                    else
                                        world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
                                    
                                    world.playSound(null, pos, TASounds.FOCUS_WATER_IMPACT, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                    splashPositions.add(new Vec3d(pos).add(0.5, 0.5, 0.5));
                                }
                                else if (state.getMaterial() == Material.FIRE) {
                                    world.setBlockToAir(pos);
                                    world.playSound(null, pos, TASounds.FOCUS_WATER_IMPACT, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                    splashPositions.add(new Vec3d(pos).add(0.5, 0.5, 0.5));
                                }
                                else if (state.getPropertyKeys().contains(BlockFarmland.MOISTURE)) {
                                    world.setBlockState(pos, state.withProperty(BlockFarmland.MOISTURE, 7));
                                    world.playSound(null, pos, TASounds.FOCUS_WATER_IMPACT, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                    splashPositions.add(new Vec3d(pos).add(0.5, 0.5, 0.5));
                                }
                                else if (state.getPropertyKeys().contains(BlockCauldron.LEVEL)) {
                                    world.setBlockState(pos, state.withProperty(BlockCauldron.LEVEL, Math.min(state.getValue(BlockCauldron.LEVEL) + 1, 3)));
                                    world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 0.25F, 1.0F);
                                    splashPositions.add(new Vec3d(pos).add(0.5, 0.5, 0.5));
                                }
                                else {
                                    TileEntity tile = world.getTileEntity(pos);
                                    if (tile != null) {
                                        if (IntegrationHandler.isIntegrationPresent(IntegrationHandler.BOTANIA_MOD_ID) && IS_APOTHECARY.test(tile))
                                            FILL_APOTHECARY.accept(tile);
                                        else {
                                            IFluidHandler fluid = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                                            if (fluid != null) {
                                                fluid.fill(new FluidStack(FluidRegistry.WATER, 334), true);
                                                if (tile instanceof TileThaumcraft) {
                                                    tile.markDirty();
                                                    ((TileThaumcraft) tile).syncTile(false);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                List<Vec3d> list = splashPositions;
                if (list.size() * 3 > PacketParticleEffect.maxPacketData)
                    list = list.subList(0, PacketParticleEffect.maxPacketData / 3);
                    
                double[] coords = new double[list.size() * 3];
                for (int i = 0; i < list.size(); ++i) {
                    Vec3d vec = list.get(i);
                    coords[i * 3] = vec.x;
                    coords[i * 3 + 1] = vec.y;
                    coords[i * 3 + 2] = vec.z;
                }
                
                TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SPLASH_BATCH, coords), new TargetPoint(world.provider.getDimension(),
                        start.getX(), start.getY(), start.getZ(), 64.0));
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public void onCast(Entity caster) {
        caster.world.playSound(null, caster.getPosition().up(), SoundEvents.ENTITY_GENERIC_SPLASH, 
                SoundCategory.PLAYERS, 0.2F, 1.2F);
        caster.world.playSound(null, caster.getPosition().up(), SoundEvents.BLOCK_CHORUS_FLOWER_GROW, 
                SoundCategory.PLAYERS, 0.2F, 1.2F);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void renderParticleFX(World world, double posX, double posY, double posZ, double velX, double velY,
            double velZ) {

        world.spawnParticle(EnumParticleTypes.WATER_SPLASH, posX, posY, posZ, velX, velY, velZ, 0);
    }
    
}
