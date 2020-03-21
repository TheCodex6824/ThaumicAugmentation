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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.math.DoubleMath;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.animation.TimeValues.VariableValue;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.api.crafting.IInfusionStabiliserExt;
import thaumcraft.api.items.IGogglesDisplayExtended;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IImpetusCellInfo;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.ConsumeResult;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.BufferedImpetusProsumer;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.tile.trait.IAnimatedTile;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

@SuppressWarnings("deprecation")
public class TileImpetusMatrix extends TileEntity implements ITickable, IAnimatedTile, IBreakCallback, IGogglesDisplayExtended {

    protected static final long CELL_CAPACITY = 500;
    protected static final float MIN_STABILITY = -100.0F;
    protected static final float MAX_STABILITY = 25.0F;
    protected static final DecimalFormat STAB_FORMATTER = new DecimalFormat("#######.##");
    
    protected class MatrixImpetusStorage implements IImpetusStorage, INBTSerializable<NBTTagCompound> {
        
        protected long energy;
        
        @Override
        public boolean canExtract() {
            return true;
        }
        
        @Override
        public boolean canReceive() {
            return true;
        }
        
        @Override
        public long getEnergyStored() {
            return energy;
        }
        
        @Override
        public long getMaxEnergyStored() {
            return getTotalCells() * CELL_CAPACITY;
        }
        
        @Override
        public long extractEnergy(long maxEnergy, boolean simulate) {
            if (canExtract()) {
                long amount = Math.min(5 * getTotalCells(), Math.min(energy, Math.min(getTotalCells() * CELL_CAPACITY, maxEnergy)));
                if (!simulate) {
                    energy -= amount;
                    onEnergyChanged();
                }
                
                return amount;
            }
            
            return 0;
        }
        
        @Override
        public long receiveEnergy(long maxEnergy, boolean simulate) {
            if (canReceive()) {
                long amount = Math.min(Math.min(getTotalCells() * CELL_CAPACITY, maxEnergy), getMaxEnergyStored() - energy);
                if (!simulate) {
                    energy += amount;
                    onEnergyChanged();
                }
                
                return amount;
            }
            
            return 0;
        }
        
        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("energy", energy);
            return tag;
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            energy = nbt.getLong("energy");
        }
        
        @Override
        public void onEnergyChanged() {
            markDirty();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
        
        public void validateEnergy() {
            energy = Math.max(Math.min(energy, getTotalCells() * CELL_CAPACITY), 0);
        }
        
    }
    
    protected MatrixImpetusStorage buffer;
    protected BufferedImpetusProsumer prosumer;
    protected IAnimationStateMachine asm;
    protected float stability;
    protected float gain;
    protected int ticks;
    protected int lastResult;
    
    public TileImpetusMatrix() {
        buffer = new MatrixImpetusStorage();
        prosumer = new BufferedImpetusProsumer(1, 1, buffer) {
            @Override
            public long onTransaction(IImpetusConsumer originator, Deque<IImpetusNode> path, long energy, boolean simulate) {
                if (!simulate)
                    markDirty();
                
                return energy;
            }
        };
        ticks = ThreadLocalRandom.current().nextInt(20);
        asm = ThaumicAugmentation.proxy.loadASM(new ResourceLocation(ThaumicAugmentationAPI.MODID, "asms/block/impetus_matrix.json"), 
                ImmutableMap.<String, ITimeValue>of("cycle_length", new VariableValue(20), "delay", new VariableValue(ticks)));
        lastResult = -1;
        gain = -1.0F;
    }
    
    protected float calculateStabilityGain() {
        HashSet<BlockPos> positions = new HashSet<>();
        MutableBlockPos check = new MutableBlockPos();
        for (int x = -2; x < 3; ++x) {
            for (int z = -2; z < 3; ++z) {
                check.setPos(x + pos.getX(), 0, z + pos.getZ());
                for (int y = -3; y < 4; ++y) {
                    check.setY(y + pos.getY());
                    if (world.isBlockLoaded(check) && world.isBlockLoaded(new BlockPos(-x + pos.getX(), y + pos.getY(), -z + pos.getZ()))) {
                        Block b1 = world.getBlockState(check).getBlock();
                        if (b1 == Blocks.SKULL || (b1 instanceof IInfusionStabiliser && ((IInfusionStabiliser) b1).canStabaliseInfusion(world, check)))
                            positions.add(check.toImmutable());
                    }
                }
            }
        }
        
        float result = 0.0F;
        ArrayList<BlockPos> issues = new ArrayList<>();
        Object2IntOpenHashMap<Block> counts = new Object2IntOpenHashMap<>();
        HashSet<BlockPos> visited = new HashSet<>();
        MutableBlockPos negative = new MutableBlockPos();
        for (BlockPos positive : positions) {
            if (!visited.contains(positive)) {
                negative.setPos(-(positive.getX() - pos.getX()) + pos.getX(), positive.getY(), -(positive.getZ() - pos.getZ()) + pos.getZ());
                float stab1 = 0.0F, stab2 = 0.0F;
                Block b1 = world.getBlockState(positive).getBlock();
                Block b2 = world.getBlockState(negative).getBlock();
                if (b1 instanceof IInfusionStabiliserExt)
                    stab1 = ((IInfusionStabiliserExt) b1).getStabilizationAmount(world, positive);
                else if (b1 == Blocks.SKULL || b1 instanceof IInfusionStabiliser)
                    stab1 = 0.1F;
                
                if (b2 instanceof IInfusionStabiliserExt)
                    stab2 = ((IInfusionStabiliserExt) b2).getStabilizationAmount(world, negative);
                else if (b2 == Blocks.SKULL || b2 instanceof IInfusionStabiliser)
                    stab2 = 0.1F;
                
                if (b1 == b2 && b1 != null && stab1 > 0.0F && DoubleMath.fuzzyEquals(stab1, stab2, 0.00001F)) {
                    if (b1 instanceof IInfusionStabiliserExt && ((IInfusionStabiliserExt) b1).hasSymmetryPenalty(world, positive, negative)) {
                        result -= ((IInfusionStabiliserExt) b1).getSymmetryPenalty(world, positive);
                        issues.add(positive);
                    }
                    else {
                        int current = counts.getInt(b1);
                        result += current > 0 ? stab1 * Math.pow(0.75, current) : stab1;
                        counts.addTo(b1, 1);
                    }
                }
                else {
                    result -= Math.max(stab1, stab2);
                    issues.add(positive);
                    issues.add(negative.toImmutable());
                }
                
                visited.add(positive);
                visited.add(negative.toImmutable());
            }
        }
        
        for (BlockPos p : issues) {
            if (world.rand.nextInt(25) == 0) {
                TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SPARK, p.getX(), p.getY(), p.getZ(),
                        5.0F, Aspect.ELDRITCH.getColor()), new TargetPoint(world.provider.getDimension(), p.getX(), p.getY(), p.getZ(), 64));
            }
        }
        
        return result;
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ++ticks % 10 == 0) {
            if (ticks % 20 == 0) {
                NodeHelper.validateOutputs(world, prosumer);
                buffer.validateEnergy();
                ConsumeResult result = prosumer.consume(getTotalCells() * CELL_CAPACITY, false);
                if (result.energyConsumed > 0) {
                    NodeHelper.syncAllImpetusTransactions(result.paths.keySet());
                    for (Map.Entry<Deque<IImpetusNode>, Long> entry : result.paths.entrySet())
                        NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
                }
            }
            
            float oldGain = gain;
            gain = calculateStabilityGain();
            
            float oldStab = stability;
            stability -= world.rand.nextFloat() * getStabilityLossPerSecond();
            stability += gain;
            stability = Math.max(Math.min(stability, MAX_STABILITY), MIN_STABILITY);
            if (stability < 0.0F && world.rand.nextInt(1500) <= Math.abs(stability)) {
                if (world.rand.nextInt(5) == 0) {
                    if (world.rand.nextBoolean()) {
                        IBlockState state = world.getBlockState(pos.up());
                        int info = state.getValue(IImpetusCellInfo.CELL_INFO);
                        if (IImpetusCellInfo.getNumberOfCells(info) > 0) {
                            ArrayList<EnumFacing> dirs = new ArrayList<>(IImpetusCellInfo.getCellDirections(info));
                            EnumFacing selected = dirs.get(world.rand.nextInt(dirs.size()));
                            world.setBlockState(pos.up(), state.withProperty(IImpetusCellInfo.CELL_INFO, IImpetusCellInfo.setCellPresent(info, selected, false)));
                            Entity drop = new EntityItem(world, pos.getX() + 0.5 * (1.0 - Math.abs(selected.getXOffset())) + selected.getXOffset(), pos.getY() + 1.0 + 0.5 * (1.0 - Math.abs(selected.getYOffset())) + selected.getYOffset(),
                                    pos.getZ() + 0.5 * (1.0 - Math.abs(selected.getZOffset())) + selected.getZOffset(), new ItemStack(TAItems.MATERIAL, 1, 3));
                            drop.motionX = selected.getXOffset() * 0.05;
                            drop.motionY = selected.getYOffset() * 0.05;
                            drop.motionZ = selected.getZOffset() * 0.05;
                            world.spawnEntity(drop);
                        }
                        else {
                            state = world.getBlockState(pos.down());
                            info = state.getValue(IImpetusCellInfo.CELL_INFO);
                            if (IImpetusCellInfo.getNumberOfCells(info) > 0) {
                                ArrayList<EnumFacing> dirs = new ArrayList<>(IImpetusCellInfo.getCellDirections(info));
                                EnumFacing selected = dirs.get(world.rand.nextInt(dirs.size()));
                                world.setBlockState(pos.down(), state.withProperty(IImpetusCellInfo.CELL_INFO, IImpetusCellInfo.setCellPresent(info, selected, false)));
                                Entity drop = new EntityItem(world, pos.getX() + 0.5 * (1.0 - Math.abs(selected.getXOffset())) + selected.getXOffset(), pos.getY() - 1.0 + 0.5 * (1.0 - Math.abs(selected.getYOffset())) + selected.getYOffset(),
                                        pos.getZ() + 0.5 * (1.0 - Math.abs(selected.getZOffset())) + selected.getZOffset(), new ItemStack(TAItems.MATERIAL, 1, 3));
                                drop.motionX = selected.getXOffset() * 0.05;
                                drop.motionY = selected.getYOffset() * 0.05;
                                drop.motionZ = selected.getZOffset() * 0.05;
                                world.spawnEntity(drop);
                            }
                        }
                    }
                    else {
                        IBlockState state = world.getBlockState(pos.down());
                        int info = state.getValue(IImpetusCellInfo.CELL_INFO);
                        if (IImpetusCellInfo.getNumberOfCells(info) > 0) {
                            ArrayList<EnumFacing> dirs = new ArrayList<>(IImpetusCellInfo.getCellDirections(info));
                            EnumFacing selected = dirs.get(world.rand.nextInt(dirs.size()));
                            world.setBlockState(pos.down(), state.withProperty(IImpetusCellInfo.CELL_INFO, IImpetusCellInfo.setCellPresent(info, selected, false)));
                            Entity drop = new EntityItem(world, pos.getX() + 0.5 * (1.0 - Math.abs(selected.getXOffset())) + selected.getXOffset(), pos.getY() - 1.0 + 0.5 * (1.0 - Math.abs(selected.getYOffset())) + selected.getYOffset(),
                                    pos.getZ() + 0.5 * (1.0 - Math.abs(selected.getZOffset())) + selected.getZOffset(), new ItemStack(TAItems.MATERIAL, 1, 3));
                            drop.motionX = selected.getXOffset() * 0.05;
                            drop.motionY = selected.getYOffset() * 0.05;
                            drop.motionZ = selected.getZOffset() * 0.05;
                            world.spawnEntity(drop);
                        }
                        else {
                            state = world.getBlockState(pos.up());
                            info = state.getValue(IImpetusCellInfo.CELL_INFO);
                            if (IImpetusCellInfo.getNumberOfCells(info) > 0) {
                                ArrayList<EnumFacing> dirs = new ArrayList<>(IImpetusCellInfo.getCellDirections(info));
                                EnumFacing selected = dirs.get(world.rand.nextInt(dirs.size()));
                                world.setBlockState(pos.up(), state.withProperty(IImpetusCellInfo.CELL_INFO, IImpetusCellInfo.setCellPresent(info, selected, false)));
                                Entity drop = new EntityItem(world, pos.getX() + 0.5 * (1.0 - Math.abs(selected.getXOffset())) + selected.getXOffset(), pos.getY() + 1.0 + 0.5 * (1.0 - Math.abs(selected.getYOffset())) + selected.getYOffset(),
                                        pos.getZ() + 0.5 * (1.0 - Math.abs(selected.getZOffset())) + selected.getZOffset(), new ItemStack(TAItems.MATERIAL, 1, 3));
                                drop.motionX = selected.getXOffset() * 0.05;
                                drop.motionY = selected.getYOffset() * 0.05;
                                drop.motionZ = selected.getZOffset() * 0.05;
                                world.spawnEntity(drop);
                            }
                        }
                    }
                    
                    buffer.validateEnergy();
                    world.playSound(null, pos, SoundsTC.grind, SoundCategory.BLOCKS, 0.6F, 1.0F);
                    world.playSound(null, pos, SoundsTC.shock, SoundCategory.BLOCKS, 0.6F, 1.0F);
                    TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SPARK, pos.getX() + 0.5,
                            pos.getY() + 0.5, pos.getZ() + 0.5, 25.0F, Aspect.ELDRITCH.getColor()),
                            new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
                }
                else {
                    int discharge = (int) (getTotalCells() * world.rand.nextFloat() * 50.0F);
                    buffer.extractEnergy(discharge, false);
                    world.playSound(null, pos, SoundsTC.shock, SoundCategory.BLOCKS, 0.6F, 1.0F);
                    TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SPARK, pos.getX() + 0.5,
                            pos.getY() + 0.5, pos.getZ() + 0.5, 15.0F, Aspect.ELDRITCH.getColor()),
                            new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
                }
                
                stability += 5.0F + world.rand.nextFloat() * 5.0F;
            }
            
            int level = getComparatorOutput();
            if (level != lastResult) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                world.updateComparatorOutputLevel(pos.down(), world.getBlockState(pos.down()).getBlock());
                world.updateComparatorOutputLevel(pos.up(), world.getBlockState(pos.up()).getBlock());
                lastResult = level;
            }
            
            if (!DoubleMath.fuzzyEquals(gain, oldGain, 0.00001) || !DoubleMath.fuzzyEquals(stability, oldStab, 0.00001))
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }
    
    public int getTotalCells() {
        int total = 0;
        IBlockState state = world.getBlockState(pos.up());
        if (state.getPropertyKeys().contains(IImpetusCellInfo.CELL_INFO))
            total += IImpetusCellInfo.getNumberOfCells(state.getValue(IImpetusCellInfo.CELL_INFO));
        
        state = world.getBlockState(pos.down());
        if (state.getPropertyKeys().contains(IImpetusCellInfo.CELL_INFO))
            total += IImpetusCellInfo.getNumberOfCells(state.getValue(IImpetusCellInfo.CELL_INFO));
        
        return total;
    }
    
    public int getComparatorOutput() {
        return (int) (buffer.getEnergyStored() / (double) buffer.getMaxEnergyStored() * 15.0);
    }
    
    protected float getStabilityLossPerSecond() {
        return getTotalCells() / 16.0F;
    }
    
    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        prosumer.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        prosumer.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void onLoad() {
        prosumer.init(world);
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(prosumer);
    }
    
    @Override
    public void invalidate() {
        prosumer.unload();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(prosumer);
    }
    
    @Override
    public void onBlockBroken() {
        for (IImpetusNode input : prosumer.getInputs())
            NodeHelper.syncRemovedImpetusNodeOutput(input, prosumer.getLocation());
        
        for (IImpetusNode output : prosumer.getOutputs())
            NodeHelper.syncRemovedImpetusNodeInput(output, prosumer.getLocation());
        
        prosumer.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(prosumer);
    }
    
    @Override
    public String[] getIGogglesText() {
        String stabName = null;
        if (stability > MAX_STABILITY / 2.0F)
            stabName = "stability.VERY_STABLE";
        else if (stability >= 0.0F)
            stabName = "stability.STABLE";
        else if (stability > -25.0F)
            stabName = "stability.UNSTABLE";
        else
            stabName = "stability.VERY_UNSTABLE";
        
        float loss = getStabilityLossPerSecond();
        if (loss > 0.0F) {
            return new String[] {
                TextFormatting.BOLD + new TextComponentTranslation(stabName).getFormattedText(),
                TextFormatting.GOLD + "" + TextFormatting.ITALIC + STAB_FORMATTER.format(gain) + 
                    " " + new TextComponentTranslation("stability.gain").getFormattedText(),
                TextFormatting.RED + "" + TextFormatting.ITALIC + new TextComponentTranslation("stability.range").getFormattedText() + TextFormatting.RED +
                TextFormatting.ITALIC + STAB_FORMATTER.format(loss) + " " + new TextComponentTranslation("stability.loss").getFormattedText()
            };
        }
        else {
            return new String[] {
                TextFormatting.BOLD + new TextComponentTranslation(stabName).getFormattedText(),
                TextFormatting.GOLD + "" + TextFormatting.ITALIC + STAB_FORMATTER.format(gain) + 
                    " " + new TextComponentTranslation("stability.gain").getFormattedText()
            };
        }
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", prosumer.serializeNBT());
        tag.setFloat("gain", gain);
        tag.setFloat("stab", stability);
        tag.setLong("energy", buffer.getEnergyStored());
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        prosumer.init(world);
        gain = tag.getFloat("gain");
        stability = tag.getFloat("stab");
        buffer.energy = tag.getLong("energy");
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("gain", gain);
        tag.setFloat("stab", stability);
        tag.setLong("energy", buffer.getEnergyStored());
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        gain = pkt.getNbtCompound().getFloat("gain");
        stability = pkt.getNbtCompound().getFloat("stab");
        buffer.energy = pkt.getNbtCompound().getLong("energy");
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("node", prosumer.serializeNBT());
        tag.setTag("energy", buffer.serializeNBT());
        tag.setFloat("stab", stability);
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        buffer.deserializeNBT(nbt.getCompoundTag("energy"));
        prosumer.deserializeNBT(nbt.getCompoundTag("node"));
        stability = nbt.getFloat("stab");
    }
    
    @Override
    public void handleEvents(float time, Iterable<Event> pastEvents) {}
    
    @Override
    public boolean hasFastRenderer() {
        return true;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE || capability == CapabilityImpetusStorage.IMPETUS_STORAGE ||
                capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return true;
        else
            return super.hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return CapabilityImpetusNode.IMPETUS_NODE.cast(prosumer);
        else if (capability == CapabilityImpetusStorage.IMPETUS_STORAGE)
            return CapabilityImpetusStorage.IMPETUS_STORAGE.cast(buffer);
        else if (capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm);
        else
            return super.getCapability(capability, facing);
    }
    
}
