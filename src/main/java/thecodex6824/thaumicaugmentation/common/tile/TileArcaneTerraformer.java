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

import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.math.DoubleMath;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.ConsumeResult;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.SimpleImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.item.CapabilityBiomeSelector;
import thecodex6824.thaumicaugmentation.api.item.IBiomeSelector;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeUtil;

public class TileArcaneTerraformer extends TileEntity implements IInteractWithCaster, ITickable {

    protected ItemStackHandler inventory;
    protected SimpleImpetusConsumer consumer;
    protected int radius;
    protected ResourceLocation activeBiome;
    protected int currentX, currentZ;
    protected boolean impetusPaid, essentiaPaid, visPaid;
    
    public TileArcaneTerraformer() {
        super();
        inventory = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }
            
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.hasCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null);
            }
            
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        consumer = new SimpleImpetusConsumer(1, 0);
        radius = 16;
    }
    
    @Override
    public boolean onCasterRightClick(World world, ItemStack stack, EntityPlayer player, BlockPos pos, EnumFacing facing,
            EnumHand hand) {
        
        if (!world.isRemote) {
            if (activeBiome == null) {
                ItemStack inv = inventory.getStackInSlot(0);
                if (!inv.isEmpty()) {
                    IBiomeSelector selected = inv.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null);
                    if (selected != null) {
                        activeBiome = selected.getBiomeID();
                        currentX = 0;
                        currentZ = 0;
                        markDirty();
                        world.playSound(null, pos, SoundsTC.craftstart, SoundCategory.BLOCKS, 0.5F, 1.0F);
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                    }
                }
            }
            else {
                activeBiome = null;
                currentX = 0;
                currentZ = 0;
                markDirty();
                world.playSound(null, pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 0.5F, 1.0F);
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
            }
        }
        
        return true;
    }
    
    @Override
    public void update() {
        if (!world.isRemote && activeBiome != null && world.getTotalWorldTime() % 10 == 0) {
            ItemStack inv = inventory.getStackInSlot(0);
            if (inv.isEmpty() ||
                    !inv.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null).getBiomeID().equals(activeBiome)) {
                
                activeBiome = null;
                currentX = 0;
                currentZ = 0;
                markDirty();
                world.playSound(null, pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 0.5F, 1.0F);
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
            }
            else {
                HashSet<BlockPos> positions = new HashSet<>(4);
                positions.add(pos.add(currentX, 0, currentZ));
                positions.add(pos.add(-currentX, 0, currentZ));
                positions.add(pos.add(-currentX, 0, -currentZ));
                positions.add(pos.add(currentX, 0, -currentZ));
                if (currentX * currentX + currentZ * currentZ < radius * radius) {
                    if (activeBiome.equals(IBiomeSelector.RESET)) {
                        positions.removeIf(set -> BiomeUtil.isNaturalBiomePresent(world, set));
                        if (!positions.isEmpty()) {
                            if (!impetusPaid) {
                                ConsumeResult consume = consumer.consume(5 * positions.size(), true);
                                if (consume.energyConsumed == 5 * positions.size()) {
                                    consumer.consume(5 * positions.size(), false);
                                    for (BlockPos set : positions)
                                        BiomeUtil.resetBiome(world, set);
                                    NodeHelper.syncAllImpetusTransactions(consume.paths);
                                    impetusPaid = true;
                                    markDirty();
                                }
                                
                                if (!essentiaPaid) {
                                    // do essentia stuff
                                    essentiaPaid = true;
                                    markDirty();
                                }
                                
                                if (!visPaid) {
                                    if (DoubleMath.fuzzyEquals(AuraHelper.drainVis(world, pos, 0.5F, true), 0.5F, 0.00001)) {
                                        AuraHelper.drainVis(world, pos, 0.5F, false);
                                        visPaid = true;
                                        markDirty();
                                    }
                                }
                            }
                        }
                        else {
                            impetusPaid = true;
                            essentiaPaid = true;
                            visPaid = true;
                            markDirty();
                        }
                    }
                    else {
                        Biome biome = Biome.REGISTRY.getObject(activeBiome);
                        if (biome != null) {
                            positions.removeIf(set -> BiomeUtil.areBiomesSame(world, set, biome));
                            if (!positions.isEmpty()) {
                                if (!impetusPaid) {
                                    ConsumeResult consume = consumer.consume(5 * positions.size(), true);
                                    if (consume.energyConsumed == 5 * positions.size()) {
                                        consumer.consume(5 * positions.size(), false);
                                        for (BlockPos set : positions)
                                            BiomeUtil.setBiome(world, set, biome);
                                        NodeHelper.syncAllImpetusTransactions(consume.paths);
                                        impetusPaid = true;
                                        markDirty();
                                    }
                                }
                                
                                if (!essentiaPaid) {
                                    // do essentia stuff
                                    essentiaPaid = true;
                                    markDirty();
                                }
                                
                                if (!visPaid) {
                                    if (DoubleMath.fuzzyEquals(AuraHelper.drainVis(world, pos, 0.25F, true), 0.25F, 0.00001)) {
                                        AuraHelper.drainVis(world, pos, 0.25F, false);
                                        visPaid = true;
                                        markDirty();
                                    }
                                }
                            }
                            else {
                                impetusPaid = true;
                                essentiaPaid = true;
                                visPaid = true;
                                markDirty();
                            }
                        }
                        else {
                            impetusPaid = true;
                            essentiaPaid = true;
                            visPaid = true;
                            markDirty();
                        }
                    }
                }
                else {
                    impetusPaid = true;
                    essentiaPaid = true;
                    visPaid = true;
                    markDirty();
                }
                
                if (impetusPaid && essentiaPaid && visPaid) {
                    impetusPaid = false;
                    essentiaPaid = false;
                    visPaid = false;
                    ++currentX;
                    if (currentX == radius) {
                        currentX = 0;
                        ++currentZ;
                        if (currentZ == radius) {
                            activeBiome = null;
                            currentX = 0;
                            currentZ = 0;
                            world.playSound(null, pos, SoundsTC.wand, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                        }
                    }
                    
                    markDirty();
                }
            }
        }
        else if (world.isRemote && activeBiome != null) {
            Biome biome = null;
            if (activeBiome.equals(IBiomeSelector.RESET))
                biome = BiomeUtil.getNaturalBiome(world, pos, Biomes.PLAINS);
            else
                biome = Biome.REGISTRY.getObject(activeBiome);
            
            if (biome != null) {
                Vec3d dir = new Vec3d(1.0, 0.5, 0.0).rotateYaw(world.getTotalWorldTime() % 20 / 20.0F * 360.0F).normalize();
                FXGeneric fx = new FXGeneric(world, pos.getX() + 0.5, pos.getY() + 1.6, pos.getZ() + 0.5, dir.x * 0.25, dir.y * 0.25, dir.z * 0.25);
                fx.setMaxAge(24 + world.rand.nextInt(12));
                int color = world.rand.nextInt(3);
                if (color == 0)
                    color = biome.getGrassColorAtPos(pos);
                else if (color == 1)
                    color = biome.getFoliageColorAtPos(pos);
                else
                    color = biome.getWaterColor() & 0x3F76E4;
                
                fx.setRBGColorF(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
                fx.setAlphaF(0.75F);
                fx.setGridSize(64);
                fx.setParticles(264, 8, 1);
                fx.setScale(2.0F);
                fx.setLayer(1);
                fx.setLoop(true);
                fx.setNoClip(false); // this is REALLY poorly named, it actually should be "setCollides", as that's what it does
                fx.setRotationSpeed(world.rand.nextFloat(), world.rand.nextBoolean() ? 1.0F : -1.0F);
                ParticleEngine.addEffect(world, fx);
            }
        }
    }
    
    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        consumer.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        consumer.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void onLoad() {
        consumer.init(world);
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(consumer);
    }
    
    @Override
    public void invalidate() {
        consumer.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(consumer);
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", inventory.serializeNBT());
        compound.setTag("node", consumer.serializeNBT());
        compound.setInteger("radius", radius);
        if (activeBiome != null) {
            compound.setString("biome", activeBiome.toString());
            compound.setInteger("currentX", currentX);
            compound.setInteger("currentZ", currentZ);
            compound.setBoolean("impetusPaid", impetusPaid);
            compound.setBoolean("essentiaPaid", essentiaPaid);
            compound.setBoolean("visPaid", visPaid);
        }
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        consumer.deserializeNBT(compound.getCompoundTag("node"));
        radius = compound.getInteger("radius");
        if (compound.hasKey("biome", NBT.TAG_STRING)) {
            activeBiome = new ResourceLocation(compound.getString("biome"));
            currentX = compound.getInteger("currentX");
            currentZ = compound.getInteger("currentZ");
            impetusPaid = compound.getBoolean("impetusPaid");
            essentiaPaid = compound.getBoolean("essentiaPaid");
            visPaid = compound.getBoolean("visPaid");
        }
        super.readFromNBT(compound);
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", consumer.serializeNBT());
        if (activeBiome != null)
            tag.setString("biome", activeBiome.toString());
        
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        consumer.init(world);
        if (tag.hasKey("biome", NBT.TAG_STRING))
            activeBiome = new ResourceLocation(tag.getString("biome"));
    }
    
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("biome", activeBiome != null ? activeBiome.toString() : "");
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        if (world.isRemote) {
            String id = packet.getNbtCompound().getString("biome");
            activeBiome = id.isEmpty() ? null : new ResourceLocation(id);
        }
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY  || capability == CapabilityImpetusNode.IMPETUS_NODE
                ? true : super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        else if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return CapabilityImpetusNode.IMPETUS_NODE.cast(consumer);
        else
            return super.getCapability(capability, facing);
    }
    
}
