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

import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.math.DoubleMath;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.ConsumeResult;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.SimpleImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.item.CapabilityBiomeSelector;
import thecodex6824.thaumicaugmentation.api.item.IBiomeSelector;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;
import thecodex6824.thaumicaugmentation.common.network.PacketTerraformerWork;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeUtil;

public class TileArcaneTerraformer extends TileEntity implements IInteractWithCaster, ITickable, IBreakCallback, IEssentiaTransport {

    protected static final int MAX_ESSENTIA = 30; // per aspect
    protected static final Cache<Biome, Object2IntOpenHashMap<Aspect>> BIOME_COSTS =
            CacheBuilder.newBuilder().softValues().concurrencyLevel(1).build();
    protected static final EnumFacing[] VALID_SIDES = new EnumFacing[] {EnumFacing.DOWN, EnumFacing.NORTH,
            EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
    
    protected ItemStackHandler inventory;
    protected SimpleImpetusConsumer consumer;
    protected int radius;
    protected ResourceLocation activeBiome;
    protected MutableBlockPos currentPos;
    protected int blocksChecked;
    protected boolean impetusPaid, essentiaPaid, visPaid;
    protected Object2IntOpenHashMap<Aspect> essentia;
    protected HashSet<ChunkPos> chunks;
    protected boolean circle;
    protected int ticks;
    
    public TileArcaneTerraformer() {
        super();
        inventory = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
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
        currentPos = new MutableBlockPos(0, 0, 0);
        essentia = new Object2IntOpenHashMap<>(5);
        circle = true;
        chunks = new HashSet<>();
        ticks = ThreadLocalRandom.current().nextInt(20);
    }
    
    @Nullable
    public EnumFacing getSideForAspect(Aspect aspect) {
        if (aspect == Aspect.EXCHANGE)
            return EnumFacing.DOWN;
        else if (aspect == Aspect.FIRE)
            return EnumFacing.WEST;
        else if (aspect == Aspect.AIR)
            return EnumFacing.NORTH;
        else if (aspect == Aspect.WATER)
            return EnumFacing.EAST;
        else if (aspect == Aspect.EARTH)
            return EnumFacing.SOUTH;
        else
            return null;
    }
    
    @Nullable
    public Aspect getAspectForSide(EnumFacing side) {
        return getAspectForSide(side.getIndex());
    }
    
    @Nullable
    public Aspect getAspectForSide(int side) {
        switch (side) {
            case 0: return Aspect.EXCHANGE;
            case 2: return Aspect.AIR;
            case 3: return Aspect.EARTH;
            case 4: return Aspect.FIRE;
            case 5: return Aspect.WATER;
            default: return null;
        }
    }
    
    public int getRadius() {
        return radius;
    }
    
    public void setRadius(int radius) {
        this.radius = Math.max(Math.min(radius, 32), 1);
        if (!world.isRemote) {
            markDirty();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
        }
    }
    
    public boolean isCircle() {
        return circle;
    }
    
    public void setCircle(boolean circle) {
        this.circle = circle;
        if (!world.isRemote) {
            markDirty();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
        }
    }
    
    public boolean isRunning() {
        return activeBiome != null;
    }
    
    @Nullable
    public ResourceLocation getActiveBiome() {
        return activeBiome;
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
                        currentPos.setPos(pos.getX(), pos.getY(), pos.getZ());
                        blocksChecked = 0;
                        chunks.clear();
                        markDirty();
                        world.playSound(null, pos, SoundsTC.craftstart, SoundCategory.BLOCKS, 0.5F, 1.0F);
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                    }
                }
            }
            else
                endTerraforming(true);
        }
        
        return true;
    }
    
    public void endTerraforming(boolean fail) {
        if (activeBiome != null) {
            activeBiome = null;
            currentPos.setPos(0, 0, 0);
            blocksChecked = 0;
            for (ChunkPos c : chunks) {
                BlockPos base = new BlockPos(c.getXStart(), pos.getY(), c.getZStart());
                BiomeUtil.generateNewAura(world, base, true);
                for (EnumFacing f : EnumFacing.HORIZONTALS)
                    BiomeUtil.generateNewAura(world, base.offset(f, 16), true);
            }
            chunks.clear();
            markDirty();
            if (fail)
                world.playSound(null, pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 0.5F, 1.0F);
            else    
                world.playSound(null, pos, SoundsTC.wand, SoundCategory.BLOCKS, 0.5F, 1.0F);
            
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
        }
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 5 == 0 && activeBiome != null) {
            ItemStack inv = inventory.getStackInSlot(0);
            if (inv.isEmpty() ||
                    !inv.getCapability(CapabilityBiomeSelector.BIOME_SELECTOR, null).getBiomeID().equals(activeBiome)) {
                
                activeBiome = null;
                currentPos.setPos(0, 0, 0);
                blocksChecked = 0;
                chunks.clear();
                markDirty();
                world.playSound(null, pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 0.5F, 1.0F);
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
            }
            else {
                for (EnumFacing facing : VALID_SIDES) {
                    Aspect aspect = getAspectForSide(facing);
                    if (essentia.getInt(aspect) < MAX_ESSENTIA) {
                        TileEntity tile = ThaumcraftApiHelper.getConnectableTile(world, pos, facing);
                        if (tile != null) {
                            IEssentiaTransport t = (IEssentiaTransport) tile;
                            if (t.canOutputTo(facing.getOpposite()) && t.getEssentiaType(facing) == aspect) {
                                if (t.getEssentiaAmount(facing.getOpposite()) > 0 && t.getSuctionAmount(facing.getOpposite()) < getSuctionAmount(facing) &&
                                        getSuctionAmount(facing) >= t.getMinimumSuction()) {
                                    
                                    essentia.addTo(aspect, t.takeEssentia(aspect, 1, facing.getOpposite()));
                                }
                            }
                        }
                    }
                }
                
                boolean skipSet = false;
                while (true) {
                    skipSet = false;
                    if (!circle || (currentPos.getX() - pos.getX()) * (currentPos.getX() - pos.getX()) + (currentPos.getZ() - pos.getZ()) *
                            (currentPos.getZ() - pos.getZ()) < radius * radius) {
                        
                        if ((activeBiome.equals(IBiomeSelector.RESET) && !BiomeUtil.isNaturalBiomePresent(world, currentPos)) ||
                                (!activeBiome.equals(IBiomeSelector.RESET) && !BiomeUtil.areBiomesSame(world, currentPos, Biome.REGISTRY.getObject(activeBiome)))) {
                        
                            if (!impetusPaid) {
                                long cost = TAConfig.terraformerImpetusCost.getValue();
                                ConsumeResult consume = consumer.consume(cost, true);
                                if (consume.energyConsumed == cost) {
                                    consume = consumer.consume(cost, false);
                                    NodeHelper.syncAllImpetusTransactions(consume.paths.keySet());
                                    for (Map.Entry<Deque<IImpetusNode>, Long> entry : consume.paths.entrySet())
                                        NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
                                    
                                    impetusPaid = true;
                                    markDirty();
                                }
                            }
                                
                            if (!essentiaPaid) {
                                Biome biome = activeBiome.equals(IBiomeSelector.RESET) ? BiomeUtil.getNaturalBiome(world, currentPos, Biomes.PLAINS) : Biome.REGISTRY.getObject(activeBiome);
                                Object2IntOpenHashMap<Aspect> neededAspects = null;
                                try {
                                    neededAspects = BIOME_COSTS.get(biome, () -> {
                                        Object2IntOpenHashMap<Aspect> map = new Object2IntOpenHashMap<>();
                                        map.put(Aspect.EXCHANGE, 1);
                                        for (BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
                                            Aspect aspect = BiomeUtil.getAspectForType(type, Aspect.EXCHANGE);
                                            if (aspect != null) {
                                                if (aspect == Aspect.ORDER || aspect == Aspect.ENTROPY)
                                                    map.addTo(Aspect.EXCHANGE, 1);
                                                else if (aspect.isPrimal() || aspect == Aspect.EXCHANGE)
                                                    map.addTo(aspect, 1);
                                            }
                                        }
                                        
                                        return map;
                                    });
                                }
                                catch (ExecutionException ex) {
                                    ThaumicAugmentation.getLogger().error("An exception was somehow thrown when it really should not have!");
                                    throw new RuntimeException(ex);
                                }
                                
                                boolean enoughEssentia = true;
                                for (Object2IntOpenHashMap.Entry<Aspect> entry : neededAspects.object2IntEntrySet()) {
                                    if (essentia.getInt(entry.getKey()) < entry.getIntValue()) {
                                        enoughEssentia = false;
                                        break;
                                    }
                                }
                                
                                if (enoughEssentia) {
                                    for (Object2IntOpenHashMap.Entry<Aspect> entry : neededAspects.object2IntEntrySet())
                                        essentia.addTo(entry.getKey(), -entry.getIntValue());
                                    
                                    essentiaPaid = true;
                                    markDirty();
                                }
                            }
                            
                            if (!visPaid) {
                                if (DoubleMath.fuzzyEquals(AuraHelper.drainVis(world, pos, 0.5F, true), 0.5F, 0.00001)) {
                                    AuraHelper.drainVis(world, pos, 0.5F, false);
                                    visPaid = true;
                                    markDirty();
                                }
                            }
                        }
                        else {
                            impetusPaid = true;
                            essentiaPaid = true;
                            visPaid = true;
                            skipSet = true;
                            markDirty();
                        }
                    }
                    else {
                        impetusPaid = true;
                        essentiaPaid = true;
                        visPaid = true;
                        skipSet = true;
                        markDirty();
                    }
                    
                    if (impetusPaid && essentiaPaid && visPaid) {
                        impetusPaid = false;
                        essentiaPaid = false;
                        visPaid = false;
                        if (!skipSet) {
                            if (activeBiome.equals(IBiomeSelector.RESET))
                                BiomeUtil.resetBiome(world, currentPos);
                            else
                                BiomeUtil.setBiome(world, currentPos, Biome.REGISTRY.getObject(activeBiome));
                            
                            chunks.add(new ChunkPos(currentPos));
                            int y = world.getHeight(currentPos.getX(), currentPos.getZ());
                            TargetPoint track = new TargetPoint(world.provider.getDimension(), currentPos.getX(), y, currentPos.getZ(), 64.0);
                            TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SPARK, currentPos.getX(),
                                    y, currentPos.getZ(), 8.0, Aspect.EXCHANGE.getColor()), track);
                            TANetwork.INSTANCE.sendToAllTracking(new PacketTerraformerWork(pos.getX(), pos.getY(), pos.getZ()), track);
                            world.playSound(null, currentPos, SoundsTC.zap, SoundCategory.BLOCKS, 0.15F, 1.0F);
                            break;
                        }
                        
                        if (Math.abs(currentPos.getX() - pos.getX()) <= Math.abs(currentPos.getZ() - pos.getZ()) && ((currentPos.getX() - pos.getX()) != (currentPos.getZ() - pos.getZ()) || (currentPos.getX() - pos.getX()) >= 0))
                            currentPos.setPos(currentPos.getX() + ((currentPos.getZ() - pos.getZ()) >= 0 ? 1 : -1), currentPos.getY(), currentPos.getZ());
                        else
                            currentPos.setPos(currentPos.getX(), currentPos.getY(), currentPos.getZ() + ((currentPos.getX() - pos.getX()) >= 0 ? -1 : 1));
                            
                        ++blocksChecked;
                        if (blocksChecked >= (radius * 2 - 1) * (radius * 2 - 1) + 1) {
                            endTerraforming(false);
                            break;
                        }
                        
                        markDirty();
                    }
                    else
                        break;
                }
            }
        }
    }
    
    @Override
    public boolean canInputFrom(EnumFacing facing) {
        return facing != EnumFacing.UP && activeBiome != null;
    }
    
    @Override
    public boolean canOutputTo(EnumFacing facing) {
        return facing != EnumFacing.UP && activeBiome == null;
    }
    
    @Override
    public int getEssentiaAmount(EnumFacing facing) {
        return facing == EnumFacing.UP ? 0 : essentia.getInt(getAspectForSide(facing));
    }
    
    @Override
    public Aspect getEssentiaType(EnumFacing facing) {
        return facing == EnumFacing.UP ? null : getAspectForSide(facing);
    }
    
    @Override
    public int getMinimumSuction() {
        return 0;
    }
    
    @Override
    public int getSuctionAmount(EnumFacing facing) {
        return facing == EnumFacing.UP ? 0 : 64;
    }
    
    @Override
    public Aspect getSuctionType(EnumFacing facing) {
        return facing == EnumFacing.UP ? null : getAspectForSide(facing);
    }
    
    @Override
    public boolean isConnectable(EnumFacing facing) {
        return facing != EnumFacing.UP;
    }
    
    @Override
    public void setSuction(Aspect aspect, int amount) {}
    
    @Override
    public int addEssentia(Aspect aspect, int amount, EnumFacing facing) {
        if (aspect == getAspectForSide(facing)) {
            int taken = Math.min(amount, MAX_ESSENTIA - essentia.getInt(aspect));
            essentia.addTo(aspect, taken);
            return taken;
        }
        
        return 0;
    }
    
    @Override
    public int takeEssentia(Aspect aspect, int amount, EnumFacing facing) {
        if (aspect == getAspectForSide(facing)) {
            int taken = Math.min(essentia.getInt(aspect), amount);
            essentia.addTo(aspect, -taken);
            return taken;
        }
        
        return 0;
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
    public void onBlockBroken() {
        if (!world.isRemote)
            NodeHelper.syncDestroyedImpetusNode(consumer);
        
        consumer.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(consumer);
    }
    
    @Override
    public void onChunkUnload() {
        consumer.unload();
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
        compound.setBoolean("circle", circle);
        if (activeBiome != null) {
            compound.setString("biome", activeBiome.toString());
            compound.setInteger("currentX", currentPos.getX());
            compound.setInteger("currentZ", currentPos.getZ());
            compound.setInteger("checked", blocksChecked);
            compound.setBoolean("impetusPaid", impetusPaid);
            compound.setBoolean("essentiaPaid", essentiaPaid);
            compound.setBoolean("visPaid", visPaid);
            NBTTagList cList = new NBTTagList();
            for (ChunkPos c : chunks)
                cList.appendTag(new NBTTagIntArray(new int[] {c.x, c.z}));
            
            compound.setTag("chunks", cList);
        }
        
        int[] e = new int[5];
        e[0] = essentia.getInt(Aspect.EXCHANGE);
        e[1] = essentia.getInt(Aspect.AIR);
        e[2] = essentia.getInt(Aspect.EARTH);
        e[3] = essentia.getInt(Aspect.FIRE);
        e[4] = essentia.getInt(Aspect.WATER);
        compound.setIntArray("essentia", e);
        
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        consumer.deserializeNBT(compound.getCompoundTag("node"));
        radius = compound.getInteger("radius");
        circle = compound.getBoolean("circle");
        if (compound.hasKey("biome", NBT.TAG_STRING)) {
            activeBiome = new ResourceLocation(compound.getString("biome"));
            currentPos.setPos(compound.getInteger("currentX"), pos.getY(), compound.getInteger("currentZ"));
            blocksChecked = compound.getInteger("checked");
            impetusPaid = compound.getBoolean("impetusPaid");
            essentiaPaid = compound.getBoolean("essentiaPaid");
            visPaid = compound.getBoolean("visPaid");
            NBTTagList cList = compound.getTagList("chunks", NBT.TAG_INT_ARRAY);
            for (NBTBase tag : cList) {
                if (tag instanceof NBTTagIntArray) {
                    NBTTagIntArray arr = (NBTTagIntArray) tag;
                    if (arr.getIntArray().length == 2)
                        chunks.add(new ChunkPos(arr.getIntArray()[0], arr.getIntArray()[1]));
                }
            }
        }
        
        int[] e = compound.getIntArray("essentia");
        if (e.length == 5) {
            essentia.put(Aspect.EXCHANGE, e[0]);
            essentia.put(Aspect.AIR, e[1]);
            essentia.put(Aspect.EARTH, e[2]);
            essentia.put(Aspect.FIRE, e[3]);
            essentia.put(Aspect.WATER, e[4]);
        }
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", consumer.serializeNBT());
        tag.setTag("inventory", inventory.serializeNBT());
        tag.setInteger("radius", radius);
        tag.setBoolean("circle", circle);
        if (activeBiome != null)
            tag.setString("biome", activeBiome.toString());
        
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        consumer.init(world);
    }
    
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("biome", activeBiome != null ? activeBiome.toString() : "");
        tag.setTag("inventory", inventory.serializeNBT());
        tag.setInteger("radius", radius);
        tag.setBoolean("circle", circle);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        if (world.isRemote) {
            String id = packet.getNbtCompound().getString("biome");
            activeBiome = id.isEmpty() ? null : new ResourceLocation(id);
            inventory.deserializeNBT(packet.getNbtCompound().getCompoundTag("inventory"));
            radius = packet.getNbtCompound().getInteger("radius");
            circle = packet.getNbtCompound().getBoolean("circle");
            world.markBlockRangeForRenderUpdate(pos, pos.up());
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
