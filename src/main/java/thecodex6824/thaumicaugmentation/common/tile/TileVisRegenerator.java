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

package thecodex6824.thaumicaugmentation.common.tile;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.animation.TimeValues.VariableValue;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thaumcraft.api.aura.AuraHelper;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.tile.trait.IAnimatedTile;

public class TileVisRegenerator extends TileEntity implements ITickable, IAnimatedTile {

    private static final int DELAY = 100;

    protected IAnimationStateMachine asm;
    protected VariableValue cycleLength;
    protected VariableValue actionTime;
    protected int delay = ThreadLocalRandom.current().nextInt(-5, 6);
    protected boolean lastState = false;
    protected int ticks;

    public TileVisRegenerator() {
        super();
        cycleLength = new VariableValue(1);
        actionTime = new VariableValue(-1);
        asm = ThaumicAugmentation.proxy.loadASM(new ResourceLocation(ThaumicAugmentationAPI.MODID, "asms/block/vis_regenerator.json"), 
                ImmutableMap.of("cycle_length", cycleLength, "act_time", actionTime, "delay", new VariableValue(delay)));
        ticks = ThreadLocalRandom.current().nextInt(20);
    }

    private float getAuraOffset() {
        return Math.max(Math.min((float) Math.pow(2, (-1.0F / 96) * AuraHelper.getAuraBase(world, pos)),
                AuraHelper.getAuraBase(world, pos) - AuraHelper.getVis(world, pos)) - AuraHelper.getFlux(world, pos), 0);
    }

    @Override
    public void update() {
        if (!world.isRemote && ticks++ % DELAY == 0 && world.getBlockState(pos).getValue(IEnabledBlock.ENABLED) &&
                AuraHelper.getVis(world, pos) + AuraHelper.getFlux(world, pos) < AuraHelper.getAuraBase(world, pos)) { 

            if (AuraHelper.getFlux(world, pos) > AuraHelper.getVis(world, pos)) {
                AuraHelper.polluteAura(world, pos, getAuraOffset(), true);
            }
            else {
                AuraHelper.addVis(world, pos, getAuraOffset());

                TANetwork.INSTANCE.sendToAllAround(new PacketParticleEffect(ParticleEffect.VIS_REGENERATOR, 
                        pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), new TargetPoint(world.provider.getDimension(), 
                        pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 48));
            }
        }
        else if (world.isRemote && (ticks++ + delay) % 5 == 0) {
            float aura = getAuraOffset();
            cycleLength.setValue(Math.min(1.0F / Math.max(aura, Float.MIN_VALUE), 15));
            IBlockState state = world.getBlockState(pos);
            boolean enabled = state.getPropertyKeys().contains(IEnabledBlock.ENABLED) && state.getValue(IEnabledBlock.ENABLED);
            if (enabled != lastState) {
                lastState = enabled;
                actionTime.setValue(Animation.getWorldTime(world, Animation.getPartialTickTime()));
                asm.transition(lastState ? "starting" : "stopping");
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityAnimation.ANIMATION_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityAnimation.ANIMATION_CAPABILITY ? CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm) : 
            super.getCapability(capability, facing);
    }

    @Override
    public void handleEvents(float time, Iterable<Event> pastEvents) {
        // don't need to worry about these atm
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

}
