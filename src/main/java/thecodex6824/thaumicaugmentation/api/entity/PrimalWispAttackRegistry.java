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

package thecodex6824.thaumicaugmentation.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundCategory;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.util.QuadConsumer;
import thecodex6824.thaumicaugmentation.common.entity.EntityPrimalWisp;
import thecodex6824.thaumicaugmentation.common.network.PacketWispZap;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;

public final class PrimalWispAttackRegistry {

    private PrimalWispAttackRegistry() {}
    
    private static final IdentityHashMap<Aspect, QuadConsumer<EntityPrimalWisp, EntityLivingBase, Aspect, Integer>> ATTACKS = new IdentityHashMap<>();
    
    public static void registerAttack(Aspect aspect, QuadConsumer<EntityPrimalWisp, EntityLivingBase, Aspect, Integer> attack) {
        ATTACKS.put(aspect, attack);
    }
    
    @Nullable
    public static QuadConsumer<EntityPrimalWisp, EntityLivingBase, Aspect, Integer> getAttack(Aspect aspect) {
        return ATTACKS.get(aspect);
    }
    
    public static void createWispZap(Entity source, Entity target, int color) {
        createWispZap(source, target, color, true);
    }
    
    public static void createWispZap(Entity source, Entity target, int color, boolean sound) {
        if (!(source instanceof EntityPlayer))
            source.playSound(SoundsTC.zap, 1.0F, 1.1F);
        else
            source.getEntityWorld().playSound(null, source.getPosition(), SoundsTC.zap, SoundCategory.PLAYERS, 1.0F, 1.1F);
        
        PacketWispZap packet = new PacketWispZap(source.getEntityId(), target.getEntityId(), color);
        if (source instanceof EntityPlayerMP)
            TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) source);
        
        TANetwork.INSTANCE.sendToAllTracking(packet, source);
    }
    
}
