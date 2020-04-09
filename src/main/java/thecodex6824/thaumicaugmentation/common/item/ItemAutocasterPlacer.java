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

package thecodex6824.thaumicaugmentation.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.common.entity.EntityAutocaster;
import thecodex6824.thaumicaugmentation.common.entity.EntityAutocasterBase;
import thecodex6824.thaumicaugmentation.common.entity.EntityAutocasterEldritch;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemAutocasterPlacer extends ItemTABase {

    public ItemAutocasterPlacer() {
        super("normal", "eldritch");
        setHasSubtypes(true);
    }
    
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
            float hitY, float hitZ, EnumHand hand) {
        
        if (world.isRemote)
            return EnumActionResult.PASS;
        else {
            boolean replace = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
            BlockPos loc = replace ? pos : pos.offset(side);
            if (!player.canPlayerEdit(loc, side, player.getHeldItem(hand)))
                return EnumActionResult.PASS;
            
            double x = loc.getX(), y = loc.getY(), z = loc.getZ();
            List<Entity> occupied = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1));
            if (!occupied.isEmpty())
                return EnumActionResult.PASS;
            
            EntityAutocasterBase entity = null;
            if (player.getHeldItem(hand).getMetadata() == 1)
                entity = new EntityAutocasterEldritch(world);
            else {
                entity = new EntityAutocaster(world);
                ((EntityAutocaster) entity).setOwner(player);
            }
            
            entity.setPosition(Math.floor(x) + 0.5, Math.floor(y), Math.floor(z) + 0.5);
            entity.setFacing(side);
            boolean cancel = MinecraftForge.EVENT_BUS.post(new LivingSpawnEvent.SpecialSpawn(entity, world, (float) entity.posX, (float) entity.posY, (float) entity.posZ, null));
            if (!cancel) {
                if (entity instanceof EntityAutocasterEldritch)
                    ((EntityAutocasterEldritch) entity).onInitialSpawn(world.getDifficultyForLocation(entity.getPosition()), null, true);
                else
                    entity.onInitialSpawn(world.getDifficultyForLocation(entity.getPosition()), null);
                
                if (world.spawnEntity(entity)) {
                    world.playSound(null, loc, SoundEvents.ENTITY_ARMORSTAND_PLACE, SoundCategory.BLOCKS, 0.75F, 0.8F);
                    if (!player.isCreative())
                        player.getHeldItem(hand).shrink(1);
                    
                    return EnumActionResult.SUCCESS;
                }
            }
            
            return EnumActionResult.PASS;
        }
    }
    
    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return TAMaterials.RARITY_ARCANE;
    }
    
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.getMetadata() == 1)
            tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.creative_only").setStyle(new Style().setColor(TextFormatting.DARK_PURPLE)).getFormattedText());
    }
    
}
