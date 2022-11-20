/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.golem;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.ISealGui;
import thaumcraft.api.golems.seals.SealPos;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.tasks.TaskHandler;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAEnums;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;

import java.util.concurrent.ThreadLocalRandom;

public class SealRecharge implements ISeal, ISealGui {

    private static final ResourceLocation ICON = new ResourceLocation(ThaumicAugmentationAPI.MODID, "items/seal/seal_attack");

    protected int ticks = ThreadLocalRandom.current().nextInt(20);
    protected Object2IntOpenHashMap<SealPos> currentTasks = new Object2IntOpenHashMap<>();
    
    @Override
    public String getKey() {
        return ThaumicAugmentationAPI.MODID + ":recharge";
    }
    
    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (golem.getGolemEntity() != null) {
            IImpetusStorage storage = golem.getGolemEntity().getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
            return storage != null && storage.getMaxEnergyStored() > 0;
        }

        return false;
    }
    
    @Override
    public boolean canPlaceAt(World world, BlockPos pos, EnumFacing face) {
        return !world.isAirBlock(pos);
    }

    @Override
    public void onTaskStarted(World world, IGolemAPI golem, Task task) {}

    @Override
    public boolean onTaskCompletion(World world, IGolemAPI golem, Task task) {
        IImpetusStorage storage = golem.getGolemEntity().getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
        if (storage == null || storage.getEnergyStored() == storage.getMaxEnergyStored()) {
            task.setSuspended(true);
            return true;
        }

        return false;
    }
    
    @Override
    public void tickSeal(World world, ISealEntity seal) {
        if (++ticks % 40 == 0) {
            boolean generateTask = !currentTasks.containsKey(seal.getSealPos());
            if (!generateTask) {
                Task task = TaskHandler.getTask(world.provider.getDimension(), currentTasks.getInt(seal.getSealPos()));
                if (task == null || task.isReserved() || task.isCompleted())
                    generateTask = true;
            }

            if (generateTask) {
                Task task = new Task(seal.getSealPos(), seal.getSealPos().pos);
                task.setPriority(seal.getPriority());
                task.setLifespan(Short.MAX_VALUE);
                TaskHandler.addTask(world.provider.getDimension(), task);
                currentTasks.put(seal.getSealPos(), task.getId());
            }
        }
    }
    
    @Override
    public ResourceLocation getSealIcon() {
        return ICON;
    }
    
    @Override
    public int[] getGuiCategories() {
        return new int[] {0, 4};
    }
    
    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[] { TAEnums.TRAIT_IMPETUS };
    }
    
    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }
    
    @Override
    public Object returnContainer(World world, EntityPlayer player, BlockPos pos, EnumFacing face, ISealEntity seal) {
        return ThaumicAugmentation.proxy.getSealContainer(world, player, pos, face, seal);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public Object returnGui(World world, EntityPlayer player, BlockPos pos, EnumFacing face, ISealEntity seal) {
        return ThaumicAugmentation.proxy.getSealGUI(world, player, pos, face, seal);
    }
    
    @Override
    public void onTaskSuspension(World world, Task task) {}
    
    @Override
    public void onRemoval(World world, BlockPos pos, EnumFacing face) {
        SealPos sealPos = new SealPos(pos, face);
        if (currentTasks.containsKey(sealPos)) {
            int task = currentTasks.removeInt(sealPos);
            Task t = TaskHandler.getTask(world.provider.getDimension(), task);
            if (t != null)
                t.setSuspended(true);
        }
    }
    
    @Override
    public void readCustomNBT(NBTTagCompound tag) {}
    
    @Override
    public void writeCustomNBT(NBTTagCompound tag) {}
    
}
