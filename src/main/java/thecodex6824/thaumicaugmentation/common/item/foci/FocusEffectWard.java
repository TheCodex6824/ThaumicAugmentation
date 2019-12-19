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

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAConfig.TileWardMode;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IUnwardableBlock;
import thecodex6824.thaumicaugmentation.api.warded.entity.CapabilityWardOwnerProvider;
import thecodex6824.thaumicaugmentation.api.warded.entity.IWardOwnerProvider;
import thecodex6824.thaumicaugmentation.api.warded.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.storage.IWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.storage.IWardStorageServer;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class FocusEffectWard extends FocusEffect {
    
    @Override
    public Aspect getAspect() {
        return Aspect.PROTECT;
    }
    
    @Override
    public int getComplexity() {
        return 23;
    }
    
    @Override
    public String getKey() {
        return "focus." + ThaumicAugmentationAPI.MODID + ".ward";
    }
    
    @Override
    public String getResearch() {
        return "FOCUS_WARD";
    }
    
    @Override
    public boolean execute(RayTraceResult result, @Nullable Trajectory trajectory, float finalPower, int whatever) {
        if (!TAConfig.disableWardFocus.getValue()) {
            World world = getPackage().getCaster().getEntityWorld();
            if (!world.isRemote && result.typeOfHit == Type.BLOCK && getPackage().getCaster() instanceof EntityPlayer && 
                    world.isBlockModifiable((EntityPlayer) getPackage().getCaster(), result.getBlockPos()) &&
                    !world.isAirBlock(result.getBlockPos())) {
                
                BlockPos pos = result.getBlockPos();
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof IUnwardableBlock) {
                    if (((IUnwardableBlock) state.getBlock()).shouldBeUnwardable(world, pos, state, getPackage().getCaster()))
                        return false;
                }
                
                Chunk chunk = getPackage().getCaster().getEntityWorld().getChunk(pos);
                if (chunk.hasCapability(CapabilityWardStorage.WARD_STORAGE, null)) {
                    if (TAConfig.tileWardMode.getValue() != TileWardMode.ALL) {
                        TileEntity tile = chunk.getTileEntity(pos, EnumCreateEntityType.CHECK);
                        if (TAConfig.tileWardMode.getValue() == TileWardMode.NOTICK && tile instanceof ITickable)
                            return false;
                        else if (TAConfig.tileWardMode.getValue() == TileWardMode.NONE && tile != null)
                            return false;
                    }
                    
                    IWardStorage wardStorage = chunk.getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                    if (wardStorage instanceof IWardStorageServer) {
                        IWardStorageServer storage = (IWardStorageServer) wardStorage;
                        UUID owner = getPackage().getCasterUUID();
                        IWardOwnerProvider provider = getPackage().getCaster().getCapability(CapabilityWardOwnerProvider.WARD_OWNER, null);
                        if (provider != null) {
                            UUID maybe = provider.getWardOwnerUUID();
                            if (maybe != null)
                                owner = maybe;
                        }
                        
                        if (!storage.hasWard(pos)) {
                            storage.setWard(world, pos, owner);
                            TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.POOF, pos.getX(), pos.getY(), pos.getZ(), Aspect.PROTECT.getColor(), result.sideHit.getIndex()),
                                    new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 64.0));
                        }
                        else if (storage.getWard(pos).equals(owner)) {
                            storage.clearWard(world, pos);
                            TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.POOF, pos.getX(), pos.getY(), pos.getZ(), Aspect.PROTECT.getColor(), result.sideHit.getIndex()),
                                    new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 64.0));
                        }
                    }
                }
            }
        }
        
        return false;
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
