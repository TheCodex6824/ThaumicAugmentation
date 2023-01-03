/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.ConsumeResult;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.SimpleImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

import javax.annotation.Nullable;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class TileCreativeImpetusSink extends TileEntity implements ITickable {
    
    protected SimpleImpetusConsumer consumer;
    protected int ticks;
    
    public TileCreativeImpetusSink() {
        super();
        consumer = new SimpleImpetusConsumer(Integer.MAX_VALUE, 0);
        ticks = ThreadLocalRandom.current().nextInt(20);
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 20 == 0) {
            ConsumeResult result = consumer.consume(Long.MAX_VALUE, false);
            if (result.energyConsumed > 0) {
                NodeHelper.syncAllImpetusTransactions(result.paths.keySet());
                for (Map.Entry<Deque<IImpetusNode>, Long> entry : result.paths.entrySet())
                    NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
            }
        }
    }
    
    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        if (world != null)
            consumer.setLocation(new DimensionalBlockPos(pos.toImmutable(), world.provider.getDimension()));
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        consumer.setLocation(new DimensionalBlockPos(pos.toImmutable(), world.provider.getDimension()));
    }
    
    @Override
    public void onLoad() {
        consumer.init(world);
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(consumer);
    }
    
    @Override
    public void invalidate() {
        if (!world.isRemote)
            NodeHelper.syncDestroyedImpetusNode(consumer);
        
        consumer.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(consumer);
        super.invalidate();
    }
    
    @Override
    public void onChunkUnload() {
        consumer.unload();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(consumer);
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", consumer.serializeNBT());
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        consumer.init(world);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("node", consumer.serializeNBT());
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        consumer.deserializeNBT(nbt.getCompoundTag("node"));
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityImpetusNode.IMPETUS_NODE ||
                super.hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return CapabilityImpetusNode.IMPETUS_NODE.cast(consumer);
        else
            return super.getCapability(capability, facing);
    }
    
}
