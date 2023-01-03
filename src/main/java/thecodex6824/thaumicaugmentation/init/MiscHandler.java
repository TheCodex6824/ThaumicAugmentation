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

package thecodex6824.thaumicaugmentation.init;

import baubles.api.BaublesApi;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.IItemHandler;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.capabilities.IPlayerWarp;
import thaumcraft.api.capabilities.IPlayerWarp.EnumWarpType;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.items.ItemsTC;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.aspect.AspectElementInteractionManager;
import thecodex6824.thaumicaugmentation.api.augment.AugmentAPI;
import thecodex6824.thaumicaugmentation.api.entity.AutocasterFocusRegistry;
import thecodex6824.thaumicaugmentation.api.entity.PrimalWispAttackRegistry;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectLight;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectVoidShield;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectWard;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectWater;

import java.util.ArrayList;
import java.util.Collections;

public final class MiscHandler {

    private MiscHandler() {}
    
    public static void preInit() {}
    
    @SuppressWarnings("deprecation")
    public static void init() {
        FocusEngine.registerElement(FocusEffectLight.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/foci/light.png"), 
                Aspect.LIGHT.getColor());
        FocusEngine.registerElement(FocusEffectWard.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/foci/ward.png"),
                Aspect.PROTECT.getColor());
        FocusEngine.registerElement(FocusEffectVoidShield.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/foci/shield.png"),
                0x5000C8);
        FocusEngine.registerElement(FocusEffectWater.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/foci/water.png"),
                Aspect.WATER.getColor());
        
        AugmentAPI.addAugmentableItemSource(new ResourceLocation(ThaumicAugmentationAPI.MODID, "default"), (entity) -> {
            if (entity instanceof EntityLivingBase)
                return entity.getEquipmentAndArmor();
            
            return Collections.emptyList();
        });
        AugmentAPI.addAugmentableItemSource(new ResourceLocation(ThaumicAugmentationAPI.MODID, "baubles"), (entity) -> {
            if (entity instanceof EntityPlayer) {
                IItemHandler handler = BaublesApi.getBaublesHandler((EntityPlayer) entity);
                ArrayList<ItemStack> stacks = new ArrayList<>(Collections.nCopies(handler.getSlots(), ItemStack.EMPTY));
                for (int i = 0; i < stacks.size(); ++i)
                    stacks.set(i, handler.getStackInSlot(i));
                
                return stacks;
            }
            
            return Collections.emptyList();
        });
        
        AutocasterFocusRegistry.registerMaxDistance("thaumcraft.BOLT", 16.0);
        AutocasterFocusRegistry.registerMaxDistance("thaumcraft.TOUCH", 4.0);
        
        registerWispAttacks();
        
        BlocksTC.stairsAncient.setTranslationKey("thaumcraft.stairs_ancient_tile");
        GameRegistry.addShapedRecipe(new ResourceLocation("thaumcraft", "StairsAncient"),
                new ResourceLocation(""), new ItemStack(BlocksTC.stairsAncient, 4, 0), 
                "S  ",
                "SS ",
                "SSS",
                'S', BlocksTC.stoneAncientTile
        );
        
        TAMaterials.VOID_BOOTS.setRepairItem(new ItemStack(ItemsTC.ingots, 1, 1));
        TAMaterials.THAUMIUM_ROBES.setRepairItem(new ItemStack(ItemsTC.ingots, 1, 0));
        
        // string name needs to be same as EntityList#getEntityString
        ThaumcraftApi.registerEntityTag(ThaumicAugmentationAPI.MODID + ".autocaster", new AspectList().add(Aspect.MECHANISM, 15).add(Aspect.AVERSION, 5).add(Aspect.SENSES, 3));
        ThaumcraftApi.registerEntityTag(ThaumicAugmentationAPI.MODID + ".autocaster_eldritch", new AspectList().add(Aspect.MECHANISM, 15).add(Aspect.ELDRITCH, 15).add(Aspect.AVERSION, 5).add(Aspect.SENSES, 3));
        ThaumcraftApi.registerEntityTag(ThaumicAugmentationAPI.MODID + ".eldritch_guardian", new AspectList().add(Aspect.ELDRITCH, 20).add(Aspect.DEATH, 20).add(Aspect.UNDEAD, 20));
        ThaumcraftApi.registerEntityTag(ThaumicAugmentationAPI.MODID + ".eldritch_warden", new AspectList().add(Aspect.ELDRITCH, 40).add(Aspect.DEATH, 40).add(Aspect.UNDEAD, 40));
        ThaumcraftApi.registerEntityTag(ThaumicAugmentationAPI.MODID + ".eldritch_golem", new AspectList().add(Aspect.ELDRITCH, 40).add(Aspect.ENERGY, 40).add(Aspect.MECHANISM, 40));
        ThaumcraftApi.registerEntityTag(ThaumicAugmentationAPI.MODID + ".primal_wisp", new AspectList().add(Aspect.AIR, 30).add(Aspect.EARTH, 30).add(Aspect.ENTROPY, 30).add(Aspect.FIRE, 30).add(Aspect.ORDER, 30).add(Aspect.WATER, 30));
        ThaumcraftApi.registerEntityTag(ThaumicAugmentationAPI.MODID + ".shield_focus", new AspectList().add(Aspect.PROTECT, 20).add(Aspect.MAGIC, 10).add(Aspect.ENERGY, 5).add(Aspect.VOID, 5));
        AspectElementInteractionManager.init();
    }
    
    private static void registerWispAttacks() {
        PrimalWispAttackRegistry.registerAttack(Aspect.AIR, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            target.knockBack(wisp, damage * 0.15F, Math.sin(wisp.rotationYaw * 0.015F), -Math.cos(wisp.rotationYaw * 0.015F));
            wisp.playSound(SoundEvents.ENTITY_ENDERDRAGON_FLAP, 0.5F, 0.66F);
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), false);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.AVERSION, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage * 1.5F);
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.COLD, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            PotionEffect slow = new PotionEffect(MobEffects.SLOWNESS, MathHelper.clamp(total / 10, 1, 20));
            if (target.isPotionApplicable(slow))
                target.addPotionEffect(slow);
            
            wisp.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.2F, 1.0F + (float) wisp.getRNG().nextGaussian() * 0.05F);
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), false);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.DARKNESS, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            PotionEffect blind = new PotionEffect(MobEffects.BLINDNESS, MathHelper.clamp(total / 10, 1, 20));
            if (target.isPotionApplicable(blind))
                target.addPotionEffect(blind);
            
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.DEATH, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            PotionEffect wither = new PotionEffect(MobEffects.WITHER, MathHelper.clamp(total / 10, 1, 20));
            if (target.isPotionApplicable(wither))
                target.addPotionEffect(wither);
            
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.DESIRE, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            PotionEffect haste = new PotionEffect(MobEffects.HASTE, MathHelper.clamp(total / 10, 1, 20));
            if (target.isPotionApplicable(haste))
                target.addPotionEffect(haste);
            
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.EARTH, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage * 1.2F);
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.FIRE, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            if (!target.isImmuneToFire())
                target.setFire(MathHelper.clamp(total / 10, 1, 20));
            
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.FLIGHT, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            PotionEffect levitate = new PotionEffect(MobEffects.WITHER, MathHelper.clamp(total / 20, 1, 10));
            if (target.isPotionApplicable(levitate))
                target.addPotionEffect(levitate);
            
            wisp.playSound(SoundEvents.ENTITY_ENDERDRAGON_FLAP, 0.5F, 0.66F);
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), false);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.FLUX, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage().setDamageBypassesArmor(), damage);
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.LIFE, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage * 0.8F);
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.MOTION, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            PotionEffect effect = new PotionEffect(MobEffects.GLOWING, MathHelper.clamp(total / 20, 1, 10));
            if (target.isPotionApplicable(effect))
                target.addPotionEffect(effect);
            
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.MIND, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            if (target instanceof EntityPlayer && wisp.getRNG().nextBoolean()) {
                IPlayerWarp warp = target.getCapability(ThaumcraftCapabilities.WARP, null);
                if (warp != null && warp.get(EnumWarpType.TEMPORARY) > 0)
                    ThaumcraftApi.internalMethods.addWarpToPlayer((EntityPlayer) target, -warp.get(EnumWarpType.TEMPORARY), EnumWarpType.TEMPORARY);
            }
            
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.MOTION, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            PotionEffect effect = new PotionEffect(wisp.getRNG().nextBoolean() ? MobEffects.SPEED : MobEffects.JUMP_BOOST,
                    MathHelper.clamp(total / 20, 1, 10));
            if (target.isPotionApplicable(effect))
                target.addPotionEffect(effect);
            
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.SENSES, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            PotionEffect effect = new PotionEffect(wisp.getRNG().nextBoolean() ? MobEffects.NIGHT_VISION : MobEffects.NAUSEA,
                    MathHelper.clamp(total / 20, 1, 10));
            if (target.isPotionApplicable(effect))
                target.addPotionEffect(effect);
            
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.TRAP, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            PotionEffect effect = new PotionEffect(MobEffects.SLOWNESS, MathHelper.clamp(total / 20, 1, 10));
            if (target.isPotionApplicable(effect))
                target.addPotionEffect(effect);
            
            effect = new PotionEffect(MobEffects.WEAKNESS, MathHelper.clamp(total / 20, 1, 10));
            if (target.isPotionApplicable(effect))
                target.addPotionEffect(effect);
            
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), true);
        });
        PrimalWispAttackRegistry.registerAttack(Aspect.WATER, (wisp, target, aspect, total) -> {
            float damage = (float) wisp.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(DamageSource.causeMobDamage(wisp).setMagicDamage(), damage);
            target.setFire(0);
            wisp.playSound(SoundEvents.ENTITY_GENERIC_SPLASH, 0.2F, 1.2F);
            PrimalWispAttackRegistry.createWispZap(wisp, target, aspect.getColor(), false);
        });
    }

}
