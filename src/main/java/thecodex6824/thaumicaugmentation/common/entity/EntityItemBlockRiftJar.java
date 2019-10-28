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

package thecodex6824.thaumicaugmentation.common.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class EntityItemBlockRiftJar extends EntityItem {

    public EntityItemBlockRiftJar(World world) {
        super(world);
    }
    
    public EntityItemBlockRiftJar(World world, double x, double y, double z) {
        super(world, x, y, z);
    }
    
    public EntityItemBlockRiftJar(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        super.attackEntityFrom(source, amount);
        if (isDead) {
            ItemStack stack = getItem();
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("seed", NBT.TAG_INT) && 
                    stack.getTagCompound().hasKey("size", NBT.TAG_INT)) {
                
                int size = stack.getTagCompound().getInteger("size");
                if (size > 0) {
                    if (!ModConfig.CONFIG_MISC.wussMode) {
                        EntityFluxRift rift = new EntityFluxRift(world);
                        rift.setPositionAndRotation(posX, posY, posZ, rotationYaw, rotationPitch);
                        rift.setRiftSeed(stack.getTagCompound().getInteger("seed"));
                        rift.setRiftSize(size);
                        rift.setRiftStability(-50.0F);
                        if (world.spawnEntity(rift)) {
                            AuraHelper.polluteAura(world, rift.getPosition(), (float) Math.sqrt(size), true);
                            rift.playSound(SoundEvents.BLOCK_GLASS_BREAK, 0.75F, 1.0F);
                            rift.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 0.75F);
                            TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.EXPLOSION,
                                    rift.posX, rift.posY, rift.posZ), rift);
                        }
                    }
                    else {
                        playSound(SoundEvents.BLOCK_GLASS_BREAK, 0.75F, 1.0F);
                        playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 0.75F);
                        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.EXPLOSION,
                                posX, posY, posZ), this);
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    protected void outOfWorld() {
        ItemStack stack = getItem();
        if (!ModConfig.CONFIG_MISC.wussMode && stack.hasTagCompound() && stack.getTagCompound().hasKey("seed", NBT.TAG_INT) && 
                stack.getTagCompound().hasKey("size", NBT.TAG_INT)) {
            
            int size = stack.getTagCompound().getInteger("size");
            if (size > 0)
                AuraHelper.polluteAura(world, getPosition(), size, true);
        }
        
        super.outOfWorld();
    }
    
}
