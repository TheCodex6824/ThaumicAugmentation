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

package thecodex6824.thaumicaugmentation.common.golem;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.ISealGui;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.client.gui.SealBaseContainer;
import thaumcraft.common.golems.client.gui.SealBaseGUI;
import thaumcraft.common.golems.tasks.TaskHandler;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

public class SealAttack implements ISeal, ISealGui {

    private static final ResourceLocation ICON = new ResourceLocation(ThaumicAugmentationAPI.MODID, "items/seal/seal_attack");
    
    protected int ticks = ThreadLocalRandom.current().nextInt(20);
    protected ISealConfigToggles.SealToggle[] props = {
            new ISealConfigToggles.SealToggle(true, "pmob", "golem.prop.mob"),
            new ISealConfigToggles.SealToggle(false, "panimal", "golem.prop.animal"),
            new ISealConfigToggles.SealToggle(false, "pplayer", "golem.prop.player"),
            new ISealConfigToggles.SealToggle(false, "pother", "golem.prop.other"),
            new ISealConfigToggles.SealToggle(true, "psight", "golem.prop.sight")
    };
    
    @Override
    public String getKey() {
        return ThaumicAugmentationAPI.MODID + ":attack";
    }
    
    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (golem == task.getEntity())
            return false;
        else if (task.getEntity().getDistanceSqToCenter(golem.getGolemEntity().getPosition()) > 
            Math.pow(((EntityCreature) golem.getGolemEntity()).getMaximumHomeDistance(), 2)) {
            
            return false;
        }
        else if (task.getEntity() instanceof IEntityOwnable) {
            if (((IEntityOwnable) task.getEntity()).getOwnerId() != null && ((IEntityOwnable) task.getEntity()).getOwnerId().equals(((IEntityOwnable) golem.getGolemEntity()).getOwnerId()))
                return false;
        }
        
        return !golem.getGolemEntity().isOnSameTeam(task.getEntity()) && 
                (!props[4].getValue() || ((EntityLiving) golem.getGolemEntity()).getEntitySenses().canSee(task.getEntity()));
    }
    
    @Override
    public boolean canPlaceAt(World world, BlockPos pos, EnumFacing face) {
        return !world.isAirBlock(pos);
    }
    
    protected boolean isValidTarget(EntityLivingBase entity) {
        if (props[0].getValue() && entity instanceof IMob)
            return true;
        else if (props[1].getValue() && entity instanceof IAnimals)
            return true;
        else if (props[2].getValue() && FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled() && 
                entity instanceof EntityPlayer)
            return true;
        else if (props[3].getValue() && !(entity instanceof IMob) && !(entity instanceof IAnimals) && !(entity instanceof EntityPlayer))
            return true;
        else
            return false;
    }
    
    @Override
    public void onTaskStarted(World world, IGolemAPI golem, Task task) {
        if (task.getEntity() instanceof EntityLivingBase && isValidTarget((EntityLivingBase) task.getEntity())) {
            ((EntityLiving) golem.getGolemEntity()).setAttackTarget((EntityLivingBase) task.getEntity());
        }
    }
    
    @Override
    public boolean onTaskCompletion(World world, IGolemAPI golem, Task task) {
        task.setSuspended(true);
        return true;
    }
    
    @Override
    public void tickSeal(World world, ISealEntity seal) {
        if (++ticks % 20 == 0) {
            BlockPos sealPos = seal.getSealPos().pos;
            AxisAlignedBB box = new AxisAlignedBB(sealPos).grow(48 / 2);
            List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
            targets.sort((entity1, entity2) -> {
                return (int) Math.signum(entity1.getDistanceSqToCenter(sealPos) - entity2.getDistanceSqToCenter(sealPos));
            });
            for (EntityLivingBase entity : targets) {
                if (isValidTarget(entity)) {
                    Task task = new Task(seal.getSealPos(), entity);
                    task.setPriority(seal.getPriority());
                    task.setLifespan((short) 10);
                    TaskHandler.addTask(world.provider.getDimension(), task);
                }
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
        return new EnumGolemTrait[] {EnumGolemTrait.FIGHTER};
    }
    
    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }
    
    @Override
    public Object returnContainer(World world, EntityPlayer player, BlockPos pos, EnumFacing face, ISealEntity seal) {
        return new SealBaseContainer(player.inventory, world, seal);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public Object returnGui(World world, EntityPlayer player, BlockPos pos, EnumFacing face, ISealEntity seal) {
        return new SealBaseGUI(player.inventory, world, seal);
    }
    
    @Override
    public void onTaskSuspension(World world, Task task) {}
    
    @Override
    public void onRemoval(World world, BlockPos pos, EnumFacing face) {}
    
    @Override
    public void readCustomNBT(NBTTagCompound tag) {}
    
    @Override
    public void writeCustomNBT(NBTTagCompound tag) {}
    
}
