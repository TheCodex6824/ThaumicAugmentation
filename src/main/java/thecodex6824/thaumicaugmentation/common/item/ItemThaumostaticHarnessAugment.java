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

import com.google.common.math.DoubleMath;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.IThaumostaticHarnessAugment;
import thecodex6824.thaumicaugmentation.common.capability.provider.SimpleCapabilityProviderNoSave;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

import javax.annotation.Nullable;

public class ItemThaumostaticHarnessAugment extends ItemTABase {

    protected static abstract class HarnessAugment implements IThaumostaticHarnessAugment {
        
        @Override
        public boolean canBeAppliedToItem(ItemStack augmentable) {
            return augmentable.getItem() == TAItems.THAUMOSTATIC_HARNESS;
        }
        
        @Override
        public boolean isCompatible(ItemStack otherAugment) {
            return !(otherAugment.getCapability(CapabilityAugment.AUGMENT, null) instanceof HarnessAugment);
        }
    }
    
    public ItemThaumostaticHarnessAugment() {
        super("gyroscope", "girdle");
        setMaxStackSize(1);
        setHasSubtypes(true);
    }
    
    protected IThaumostaticHarnessAugment createAugmentForStack(ItemStack stack) {
        if (stack.getMetadata() == 0) {
            return new HarnessAugment() {
                @Override
                public boolean shouldAllowSprintFly(EntityPlayer wearer) {
                    return false;
                }
                
                @Override
                public double getVisCostPerTick(EntityPlayer wearer) {
                    return TAConfig.gyroscopeHarnessCost.getValue();
                }
                
                @Override
                public int getVisCapacity() {
                    return 150;
                }
                
                @Override
                public float getFlySpeed(EntityPlayer wearer) {
                    return TAConfig.gyroscopeHarnessSpeed.getValue();
                }
                
                @Override
                public void applyDrift(EntityPlayer wearer) {
                    double factor = 1.0;
                    if (DoubleMath.fuzzyEquals(wearer.moveForward, 0.0, 0.001) && DoubleMath.fuzzyEquals(wearer.moveStrafing, 0.0, 0.001))
                        factor = 0.75;
                    
                    wearer.motionX *= factor;
                    wearer.motionY *= factor;
                    wearer.motionZ *= factor;
                }
                
            };
        }
        else {
            // need to allow all metas for mods that create stacks with wildcard meta
            return new HarnessAugment() {
                @Override
                public boolean shouldAllowSprintFly(EntityPlayer wearer) {
                    return true;
                }
                
                @Override
                public double getVisCostPerTick(EntityPlayer wearer) {
                    return TAConfig.girdleHarnessCost.getValue();
                }
                
                @Override
                public int getVisCapacity() {
                    return 250;
                }
                
                @Override
                public float getFlySpeed(EntityPlayer wearer) {
                    return TAConfig.girdleHarnessSpeed.getValue();
                }
                
                @Override
                public void applyDrift(EntityPlayer wearer) {
                    double factor = 1.0;
                    if (DoubleMath.fuzzyEquals(wearer.moveForward, 0.0, 0.001) && DoubleMath.fuzzyEquals(wearer.moveStrafing, 0.0, 0.001))
                        factor = 1.0199999;
                    
                    wearer.motionX *= factor;
                    wearer.motionY *= factor;
                    wearer.motionZ *= factor;
                }
                
            };
        }
    }
    
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        SimpleCapabilityProviderNoSave<IAugment> provider =
                new SimpleCapabilityProviderNoSave<>(createAugmentForStack(stack), CapabilityAugment.AUGMENT);
        if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
            provider.deserializeNBT(nbt.getCompoundTag("Parent"));
        
        return provider;
    }
    
}
