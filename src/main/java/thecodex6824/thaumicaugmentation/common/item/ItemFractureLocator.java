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

package thecodex6824.thaumicaugmentation.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.network.PacketFractureLocatorUpdate;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.util.FractureLocatorSearchManager;

public class ItemFractureLocator extends ItemTABase {
    
    public ItemFractureLocator() {
        super();
        setHasSubtypes(true);
    }
    
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (!world.isRemote && entity instanceof EntityPlayerMP) {
            if (FractureLocatorSearchManager.canPlayerRequestLocation((EntityPlayer) entity)) {
                BlockPos nearest = FractureLocatorSearchManager.findNearestFracture(world, entity.getPosition());
                if (nearest != null)
                    TANetwork.INSTANCE.sendTo(new PacketFractureLocatorUpdate(nearest), (EntityPlayerMP) entity);
                else
                    TANetwork.INSTANCE.sendTo(new PacketFractureLocatorUpdate(), (EntityPlayerMP) entity);
                
                FractureLocatorSearchManager.resetPlayerLocationTime((EntityPlayer) entity);
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    private double getLookYaw(Entity entity) {
        double yaw = 0;
        if (entity instanceof EntityLivingBase)
            yaw = ((EntityLivingBase) entity).rotationYawHead;
        else
            yaw = entity.rotationYaw;
        
        return Math.floorMod((long) yaw + 90, 360);
    }
    
    @SideOnly(Side.CLIENT)
    private double normalize(double input) {
        input = (input + Math.PI) % (Math.PI * 2);
        if (input < 0)
            input += Math.PI * 2;
        
        return input - Math.PI;
    }
    
    @SideOnly(Side.CLIENT)
    private double angleDifference(double a1, double a2) {
        return normalize(a2 - a1);
    }
    
    @SideOnly(Side.CLIENT)
    private double calcError(double value, double expected) {
        return Math.abs(angleDifference(expected, value)) / Math.PI;
    }
    
    @SideOnly(Side.CLIENT)
    public int getTintColor(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("found")) {
            int[] pos = stack.getTagCompound().getIntArray("pos");
            if (pos.length == 3) {
                Vec3d fracture = new Vec3d(pos[0], pos[1], pos[2]);
                Entity entity = stack.getItemFrame() != null ? stack.getItemFrame() : Minecraft.getMinecraft().player;
                float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
                Vec3d playerPos = entity.getPositionEyes(partialTicks);
                double optimalAngle = normalize(Math.atan2(fracture.z + 0.5 - playerPos.z, fracture.x + 0.5 - playerPos.x));
                double currentYaw = getLookYaw(entity) * Math.PI / 180.0;
                int factor = (int) ((1.0 - MathHelper.clamp(calcError(currentYaw, optimalAngle), 0.0, 0.85)) * 255);
                return factor | (factor << 8) | (factor << 16);
            }
        }
        
        return 0;
    }
    
}
