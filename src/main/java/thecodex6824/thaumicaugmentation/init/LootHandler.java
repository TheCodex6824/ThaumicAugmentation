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

package thecodex6824.thaumicaugmentation.init;

import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.api.entities.ITaintedMob;
import thecodex6824.thaumicaugmentation.api.TALootTables;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.util.ItemHelper;

import java.util.stream.Collectors;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class LootHandler {
    
    public static void preInit() {
        for (ResourceLocation table : TALootTables.getAllLootTables())
            LootTableList.register(table);
    }
    
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        World world = event.getEntity().getEntityWorld();
        if (!world.isRemote && event.getEntity() instanceof ITaintedMob) {
            LootTable table = event.getEntity().getEntityWorld().getLootTableManager().getLootTableFromLocation(TALootTables.TAINT_MOB);
            LootContext context = new LootContext.Builder((WorldServer) event.getEntity().getEntityWorld())
                    .withDamageSource(event.getSource())
                    .withLootedEntity(event.getEntity())
                    .build();
            event.getDrops().addAll(table.generateLootForPools(world.rand, context).stream().map(
                    stack -> {
                        Entity entity = ItemHelper.makeItemEntity(world, event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, stack);
                        if (entity instanceof EntityItem)
                            return (EntityItem) entity;
                        else {
                            world.spawnEntity(entity);
                            return null;
                        }
                    }).filter(Predicates.notNull()).collect(Collectors.toList()));
        }
    }
    
}
