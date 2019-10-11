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

package thecodex6824.thaumicaugmentation.init;

import java.lang.reflect.Field;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApi.BluePrint;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IDustTrigger;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.Part;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.blocks.basic.BlockPillar;
import thaumcraft.common.lib.crafting.DustTriggerMultiblock;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.CasterAugmentBuilder;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;
import thecodex6824.thaumicaugmentation.common.recipe.ElementalAugmentCraftingRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.FluxSeedGrowthRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.MorphicToolBindingRecipe;

public final class RecipeHandler {

    private RecipeHandler() {}
    
    private static final Field TRIGGER_RESEARCH;
    
    static {
        Field f = null;
        try {
            f = DustTriggerMultiblock.class.getDeclaredField("research");
            f.setAccessible(true);
        }
        catch (Exception ex) {
            FMLCommonHandler.instance().raiseException(ex, "Failed to access Thaumcraft's DustTriggerMultiblock#research", true);
        }
        
        TRIGGER_RESEARCH = f;
    }
    
    public static void initInfusionRecipes() {
        ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "GauntletVoid"), 
                new InfusionRecipe("GAUNTLET_VOID", new ItemStack(TAItems.GAUNTLET, 1, 1), 6, 
                        new AspectList().add(Aspect.ENERGY, 50).add(Aspect.ELDRITCH, 75).add(Aspect.VOID, 75), 
                        new ItemStack(ItemsTC.charmVoidseer), new Object[] {
                                ItemsTC.fabric, "plateVoid", "plateVoid", "plateVoid", "plateVoid", ItemsTC.salisMundus
                        }
            ));
            ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "SealCopier"), 
                new InfusionRecipe("SEAL_COPIER", new ItemStack(TAItems.SEAL_COPIER), 1, 
                        new AspectList().add(Aspect.MIND, 25).add(Aspect.MECHANISM, 10), 
                        new ItemStack(ItemsTC.golemBell), new Object[] { 
                                ItemsTC.brain, ItemsTC.brain, new ItemStack(ItemsTC.seals, 1, 0)
                        }
            ));
            ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "BootsVoid"),
                new InfusionRecipe("BOOTS_VOID", new ItemStack(TAItems.VOID_BOOTS), 6,
                        new AspectList().add(Aspect.VOID, 50).add(Aspect.ELDRITCH, 50).add(Aspect.MOTION, 150).add(Aspect.FLIGHT, 150), 
                        ItemsTC.travellerBoots, new Object[] {
                                ItemsTC.fabric, ItemsTC.fabric, "plateVoid", "plateVoid", "feather",
                                Items.FISH, ItemsTC.primordialPearl, "quicksilver"
                        }
            ));

            for (int i = 1; i < 4; ++i) {
                ItemStack in = new ItemStack(TAItems.RIFT_SEED, 1, 1);
                in.setTagCompound(new NBTTagCompound());
                in.getTagCompound().setInteger("flux", 100);
                in.getTagCompound().setBoolean("grown", false);
                ItemStack out = in.copy();
                out.getTagCompound().setInteger("flux", i == 3 ? 1000 : i * 100 + 100);
                out.getTagCompound().setBoolean("grown", true);
                Object[] inputs = new Object[i == 3 ? 9 : i];
                for (int k = 0; k < (i == 3 ? 9 : i); ++k)
                    inputs[k] = ThaumcraftApiHelper.makeCrystal(Aspect.FLUX);

                ThaumcraftApi.addFakeCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "FluxSeedGrowthFake" + (i == 3 ? 1000 : i * 100 + 100)), 
                        new InfusionRecipe("RIFT_STUDIES", out, i == 3 ? 9 : i, new AspectList().add(Aspect.FLUX, i == 3 ? 500 : i * 50),
                                in, inputs));
            }

            ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "FluxSeedGrowth"), 
                    new FluxSeedGrowthRecipe());
            
            ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "RiftEnergyCell"),
                    new InfusionRecipe("RIFT_POWER@2", new ItemStack(TAItems.MATERIAL, 1, 3), 7, new AspectList().add(Aspect.ELDRITCH, 25).add(Aspect.VOID, 50).add(Aspect.ENERGY, 100),
                    new ItemStack(ItemsTC.voidSeed), new Object[] {
                            "plateVoid", ItemsTC.primordialPearl, "plateVoid", ThaumcraftApiHelper.makeCrystal(Aspect.ELDRITCH),
                            "plateVoid", "dustRedstone", "plateVoid", "gemAmber"
                    }
            ));
            
            ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "MorphicToolBinding"),
                    new MorphicToolBindingRecipe());
            
            ItemStack morphicSample = new ItemStack(TAItems.MORPHIC_TOOL);
            IMorphicTool tool = morphicSample.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null);
            tool.setDisplayStack(new ItemStack(Items.GOLDEN_SWORD));
            tool.setFunctionalStack(new ItemStack(Items.DIAMOND_SWORD));
            ThaumcraftApi.addFakeCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "MorphicToolBindingFake"),
                    new InfusionRecipe("MORPHIC_TOOL", morphicSample, 5, new AspectList().add(Aspect.VOID, 15),
                    Items.DIAMOND_SWORD, new Object[] {
                            ItemsTC.primordialPearl, Items.GOLDEN_SWORD, ItemsTC.quicksilver
                    }
            ));
            
            ItemStack cutter = new ItemStack(TAItems.PRIMAL_CUTTER);
            EnumInfusionEnchantment.addInfusionEnchantment(cutter, EnumInfusionEnchantment.ARCING, 2);
            EnumInfusionEnchantment.addInfusionEnchantment(cutter, EnumInfusionEnchantment.BURROWING, 1);
            ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "PrimalCutter"),
                    new InfusionRecipe("PRIMAL_CUTTER", cutter, 6, new AspectList().add(Aspect.PLANT, 75).add(
                    Aspect.TOOL, 50).add(Aspect.MAGIC, 50).add(Aspect.VOID, 50).add(Aspect.AVERSION, 75).add(
                    Aspect.ELDRITCH, 50).add(Aspect.DESIRE, 50), ItemsTC.primordialPearl, new Object[] {
                            ItemsTC.voidAxe, ItemsTC.voidSword, ItemsTC.elementalAxe, ItemsTC.elementalSword
                    }
            ));
    }
    
    public static void initCrucibleRecipes() {
        ItemStack fluxSeedStack = new ItemStack(TAItems.RIFT_SEED, 1, 1);
        fluxSeedStack.setTagCompound(new NBTTagCompound());
        fluxSeedStack.getTagCompound().setInteger("flux", 100);
        ThaumcraftApi.addCrucibleRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "FluxSeed"), 
                new CrucibleRecipe("RIFT_STUDIES", fluxSeedStack, ItemsTC.voidSeed, 
                        new AspectList().add(Aspect.FLUX, 50)));
        
        ThaumcraftApi.addCrucibleRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "EffectProviderPower"),
                new CrucibleRecipe("GAUNTLET_AUGMENTATION@2", CasterAugmentBuilder.createStackForEffectProvider(
                        new ResourceLocation(ThaumicAugmentationAPI.MODID, "effect_power")),
                        ThaumcraftApiHelper.makeCrystal(Aspect.ORDER), new AspectList().add(Aspect.AVERSION, 15).add(Aspect.CRYSTAL, 10).add(Aspect.MAGIC, 5)));
        ThaumcraftApi.addCrucibleRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "EffectProviderCost"),
                new CrucibleRecipe("GAUNTLET_AUGMENTATION@2", CasterAugmentBuilder.createStackForEffectProvider(
                        new ResourceLocation(ThaumicAugmentationAPI.MODID, "effect_cost")),
                        ThaumcraftApiHelper.makeCrystal(Aspect.ORDER), new AspectList().add(Aspect.AURA, 15).add(Aspect.CRYSTAL, 10).add(Aspect.MAGIC, 5)));
        ThaumcraftApi.addCrucibleRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "EffectProviderCastSpeed"),
                new CrucibleRecipe("GAUNTLET_AUGMENTATION@2", CasterAugmentBuilder.createStackForEffectProvider(
                        new ResourceLocation(ThaumicAugmentationAPI.MODID, "effect_cast_speed")),
                        ThaumcraftApiHelper.makeCrystal(Aspect.ORDER), new AspectList().add(Aspect.ENERGY, 15).add(Aspect.CRYSTAL, 10).add(Aspect.MAGIC, 5)));
    
        ThaumcraftApi.addCrucibleRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "SealSecure"),
                new CrucibleRecipe("SEAL_SECURE", GolemHelper.getSealStack(ThaumicAugmentationAPI.MODID + ":attack"), ItemsTC.seals,
                        new AspectList().add(Aspect.AVERSION, 30).add(Aspect.PROTECT, 10)));
        
        ThaumcraftApi.addCrucibleRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "SealSecureAdvanced"),
                new CrucibleRecipe("SEAL_SECURE&&MINDBIOTHAUMIC", GolemHelper.getSealStack(ThaumicAugmentationAPI.MODID + ":attack_advanced"), GolemHelper.getSealStack(ThaumicAugmentationAPI.MODID + ":attack"),
                        new AspectList().add(Aspect.SENSES, 20).add(Aspect.MIND, 20)));
        
        ThaumcraftApi.addCrucibleRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "EldritchStone"),
                new CrucibleRecipe("VOID_STONE_USAGE", new ItemStack(BlocksTC.stoneEldritchTile), "stoneVoid", new AspectList().add(Aspect.ELDRITCH, 8)));
    }
    
    public static void initArcaneCraftingRecipes() {
        // so the "group" is just a recipe book thing, which we don't have to care about
        ResourceLocation defaultGroup = new ResourceLocation("");
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "GauntletThaumium"), new ShapedArcaneRecipe(
                defaultGroup, "GAUNTLET_THAUMIUM", 250, 
                new AspectList().add(Aspect.AIR, 2).add(Aspect.EARTH, 2).add(Aspect.ENTROPY, 2).add(Aspect.FIRE, 2).add(
                        Aspect.ORDER, 2).add(Aspect.WATER, 2), new ItemStack(TAItems.GAUNTLET, 1, 0), new Object[] {
                                "PPP",
                                "FRF",
                                "FTF",
                                'P', "plateThaumium", 'F', ItemsTC.fabric, 'R', ItemsTC.visResonator,
                                'T', ItemsTC.thaumometer
                        }
        ));
        
        /**
         * Materials
         */
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "Lattice"), new ShapedArcaneRecipe(
                defaultGroup, "VIS_REGENERATOR", 25, 
                new AspectList().add(Aspect.AIR, 1).add(Aspect.WATER, 1), 
                new ItemStack(TAItems.MATERIAL, 1, 0), new Object[] {
                        "SLS",
                        "LFL",
                        "SLS",
                        'S', BlocksTC.plankSilverwood, 'L', BlocksTC.leafSilverwood, 'F', ItemsTC.filter
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "VisRegenerator"), new ShapedArcaneRecipe(
                defaultGroup, "VIS_REGENERATOR", 100, 
                new AspectList().add(Aspect.AIR, 2), 
                new ItemStack(TABlocks.VIS_REGENERATOR, 1, 0), new Object[] {
                        "GIG",
                        "BLB",
                        "GEG",
                        'G', BlocksTC.plankGreatwood, 'I', Blocks.IRON_BARS, 'B', "plateIron",
                        'L', new ItemStack(TAItems.MATERIAL, 1, 0), 'E', ItemsTC.mechanismSimple
                }
        ));

        /**
         * Warded Stuff
         */

        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "WardingSigil"), new ShapedArcaneRecipe(
                defaultGroup, "WARDED_ARCANA@2", 10, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1), 
                new ItemStack(TAItems.MATERIAL, 1, 1), new Object[] {
                        " T ",
                        "PBP",
                        " T ",
                        'T', ItemsTC.tallow, 'P', "dyePurple", 'B', ItemsTC.brain
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "WardedChest"), new ShapedArcaneRecipe(
                defaultGroup, "WARDED_ARCANA", 75, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),  
                new ItemStack(TABlocks.WARDED_CHEST), new Object[] {
                        " S ",
                        "TCT",
                        "TTT",
                        'T', "plateThaumium", 'S', new ItemStack(TAItems.MATERIAL, 1, 1), 'C', "chestWood"
                }
        ));
        
        /*
         * Arcane Doors
         */
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorGreatwood"), new ShapedArcaneRecipe(
                defaultGroup, "ARCANE_DOOR", 100, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1), 
                new ItemStack(TAItems.ARCANE_DOOR, 1, 0), new Object[] {
                        "GBG",
                        "GSG",
                        "GBG",
                        'G', BlocksTC.plankGreatwood, 'B', "plateBrass", 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorThaumium"), new ShapedArcaneRecipe(
                defaultGroup, "ARCANE_DOOR", 100, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),  
                new ItemStack(TAItems.ARCANE_DOOR, 1, 1), new Object[] {
                        "TIT",
                        "TST",
                        "TIT",
                        'T', "plateThaumium", 'I', "plateIron", 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorSilverwood"), new ShapedArcaneRecipe(
                defaultGroup, "ARCANE_DOOR", 100, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),  
                new ItemStack(TAItems.ARCANE_DOOR, 1, 2), new Object[] {
                        "PIP",
                        "PSP",
                        "PIP",
                        'P', BlocksTC.plankSilverwood, 'I', "plateIron", 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
                }
        ));

        /**
         * Keys
         */

        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "KeyIron"), new ShapedArcaneRecipe(
                defaultGroup, "WARD_KEYS", 15, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1), 
                new ItemStack(TAItems.KEY, 1, 0), new Object[] {
                        "  B",
                        " NN",
                        "NN ",
                        'B', ItemsTC.brain, 'N', "nuggetIron"
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "KeyBrass"), new ShapedArcaneRecipe(
                defaultGroup, "WARD_KEYS", 15, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1), 
                new ItemStack(TAItems.KEY, 1, 1), new Object[] {
                        "  B",
                        " NN",
                        "NN ",
                        'B', ItemsTC.brain, 'N', "nuggetBrass"
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "KeyThaumium"), new ShapedArcaneRecipe(
                defaultGroup, "WARD_KEYS", 15, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1), 
                new ItemStack(TAItems.KEY, 1, 2), new Object[] {
                        "  B",
                        " NN",
                        "NN ",
                        'B', ItemsTC.brain, 'N', "nuggetThaumium"
                }
        ));
        
        /*
         * Trapdoors
         */
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneTrapdoorGreatwood"), new ShapedArcaneRecipe(
                defaultGroup, "ARCANE_DOOR", 100, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),
                new ItemStack(TABlocks.ARCANE_TRAPDOOR_WOOD, 2), new Object[] {
                        "WWW",
                        "BSB",
                        "WWW",
                        'W', BlocksTC.plankGreatwood, 'B', "plateBrass", 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneTrapdoorThaumium"), new ShapedArcaneRecipe(
                defaultGroup, "ARCANE_DOOR", 100, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),
                new ItemStack(TABlocks.ARCANE_TRAPDOOR_METAL, 2), new Object[] {
                        "TTT",
                        "ISI",
                        "TTT",
                        'T', "plateThaumium", 'I', "plateIron", 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneTrapdoorSilverwood"), new ShapedArcaneRecipe(
                defaultGroup, "ARCANE_DOOR", 100, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),
                new ItemStack(TABlocks.ARCANE_TRAPDOOR_SILVERWOOD, 2), new Object[] {
                        "WWW",
                        "BSB",
                        "WWW",
                        'W', BlocksTC.plankSilverwood, 'B', "plateIron", 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
                }
        ));
        
        /*
         * Augments
         */
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "StrengthProviderElemental"), new ElementalAugmentCraftingRecipe());
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "StrengthProviderOverworld"), new ShapelessArcaneRecipe(
                defaultGroup, "DIMENSIONAL_MODIFIERS", 25, new AspectList().add(Aspect.WATER, 1).add(Aspect.EARTH, 1),
                CasterAugmentBuilder.createStackForStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_overworld")),
                new Object[] {
                        ItemsTC.visResonator, "dirt", "stone"
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "StrengthProviderNether"), new ShapelessArcaneRecipe(
                defaultGroup, "DIMENSIONAL_MODIFIERS", 25, new AspectList().add(Aspect.FIRE, 1).add(Aspect.ENTROPY, 1),
                CasterAugmentBuilder.createStackForStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_nether")),
                new Object[] {
                        ItemsTC.visResonator, "netherrack", "glowstone"
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "StrengthProviderEnd"), new ShapelessArcaneRecipe(
                defaultGroup, "DIMENSIONAL_MODIFIERS", 25, new AspectList().add(Aspect.AIR, 1).add(Aspect.ORDER, 1),
                CasterAugmentBuilder.createStackForStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_end")),
                new Object[] {
                        ItemsTC.visResonator, "endstone", "obsidian"
                }
        ));
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "StrengthProviderEmptiness"), new ShapelessArcaneRecipe(
                defaultGroup, "EMPTINESS_MODIFIER", 25, new AspectList().add(Aspect.ENTROPY, 1).add(Aspect.EARTH, 1),
                CasterAugmentBuilder.createStackForStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_emptiness")),
                new Object[] {
                        ItemsTC.visResonator, "stoneVoid", 
                        new ItemStack(TABlocks.STONE, 1, StoneType.STONE_TAINT_NODECAY.getMeta())
                }
        ));
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "StrengthProviderFrenzy"), new ShapelessArcaneRecipe(
                defaultGroup, "FRENZY_MODIFIER", 25, new AspectList().add(Aspect.ENTROPY, 1).add(Aspect.FIRE, 1),
                CasterAugmentBuilder.createStackForStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_frenzy")),
                new Object[] {
                        ItemsTC.visResonator, new ItemStack(ItemsTC.modules, 1, 1), ItemsTC.mechanismSimple
                }
        ));
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "StrengthProviderExperience"), new ShapelessArcaneRecipe(
                defaultGroup, "EXPERIENCE_MODIFIER", 25, new AspectList().add(Aspect.ORDER, 1),
                CasterAugmentBuilder.createStackForStrengthProvider(new ResourceLocation(ThaumicAugmentationAPI.MODID, "strength_experience")),
                new Object[] {
                        ItemsTC.visResonator, Items.EMERALD, Items.EMERALD, new ItemStack(Items.DYE, 1, 4)
                }
        ));
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "AugmentGauntletRiftEnergyStorage"), new ShapelessArcaneRecipe(
                defaultGroup, "RIFT_POWER@2", 25, 
                new AspectList().add(Aspect.AIR, 1).add(Aspect.ENTROPY, 1).add(Aspect.FIRE, 1), new ItemStack(TAItems.AUGMENT_CASTER_RIFT_ENERGY_STORAGE), new Object[] {
                        "plateThaumium", new ItemStack(TAItems.MATERIAL, 1, 3)
                }
        ));
        
        /*
         * Misc stuff
         */
        
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "FractureLocator"), new ShapedArcaneRecipe(
                defaultGroup, "ENTERING_FRACTURE@1", 65, new AspectList().add(Aspect.AIR, 1).add(Aspect.ENTROPY, 1),
                new ItemStack(TAItems.FRACTURE_LOCATOR), new Object[] {
                        "BCB",
                        "CEC",
                        "BCB",
                        'B', "plateBrass", 'C', ThaumcraftApiHelper.makeCrystal(Aspect.VOID), 'E', new ItemStack(ItemsTC.nuggets, 1, 10)
                }
        ));
    }
    
    public static void initMultiblocks() {
        Part matrix = new Part(BlocksTC.infusionMatrix, TABlocks.IMPETUS_MATRIX);
        Part base = new Part(BlocksTC.pedestalEldritch, TABlocks.IMPETUS_MATRIX_BASE);
        Part[][][] blueprint = new Part[][][] {
            {
                {base}
            },
            {
                {matrix}
            },
            {
                {base}
            }
        };
        
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("FIRSTSTEPS", blueprint));
        ThaumcraftApi.addMultiblockRecipeToCatalog(new ResourceLocation("thaumicaugmentation", "impetus_matrix"), new BluePrint("IMPETUS_MATRIX", blueprint, new ItemStack[] {
                new ItemStack(BlocksTC.infusionMatrix),
                new ItemStack(BlocksTC.pedestalEldritch, 2)
        }));
        
        fixInfusionAltarMultiblocks();
    }
    
    public static String getDustTriggerResearch(DustTriggerMultiblock trigger) {
        try {
            return (String) TRIGGER_RESEARCH.get(trigger);
        }
        catch (Exception ex) {
            FMLCommonHandler.instance().raiseException(ex, "Failed to invoke Thaumcraft's DustTriggerMultiblock#research", true);
            return null;
        }
    }
    
    /**
     * The multiblock recipes registered by TC require a pedestal with a meta value
     * of 1 for ancient and 2 for eldritch. However, the meta value is for the redstone inlay power
     * going through the pedestal (the arcane/ancient/eldritch pillars are their own blocks).
     * Removing the metadata requirement fixes the multiblocks. This was probably an oversight in
     * the last few betas (since the stablizier mechanics were reworked).
     */
    public static void fixInfusionAltarMultiblocks() {
        Part matrix = new Part(BlocksTC.infusionMatrix, null);
        
        Part ancientStone = new Part(BlocksTC.stoneAncient, "AIR");
        Part ancientPillarEast = new Part(BlocksTC.stoneAncient, new ItemStack(BlocksTC.pillarAncient, 1, BlockPillar.calcMeta(EnumFacing.EAST)));
        Part ancientPillarNorth = new Part(BlocksTC.stoneAncient, new ItemStack(BlocksTC.pillarAncient, 1, BlockPillar.calcMeta(EnumFacing.NORTH)));
        Part ancientPillarSouth = new Part(BlocksTC.stoneAncient, new ItemStack(BlocksTC.pillarAncient, 1, BlockPillar.calcMeta(EnumFacing.SOUTH)));
        Part ancientPillarWest = new Part(BlocksTC.stoneAncient, new ItemStack(BlocksTC.pillarAncient, 1, BlockPillar.calcMeta(EnumFacing.WEST)));
        Part ancientPedestal = new Part(BlocksTC.pedestalAncient, null);
        Part[][][] ancientBlueprint = new Part[][][] {
            {
                {null, null, null},
                {null, matrix, null},
                {null, null, null}
            },
            {
                {ancientStone, null, ancientStone},
                {null, null, null},
                {ancientStone, null, ancientStone}
            },
            {
                {ancientPillarEast, null, ancientPillarNorth},
                {null, ancientPedestal, null},
                {ancientPillarSouth, null, ancientPillarWest}
            }
        };
        
        // IDustTrigger stores an ArrayList of triggers, so we need to find / remove ourselves
        for (int i = 0; i < IDustTrigger.triggers.size(); ++i) {
            IDustTrigger trigger = IDustTrigger.triggers.get(i);
            if (trigger instanceof DustTriggerMultiblock && getDustTriggerResearch((DustTriggerMultiblock) trigger).equals("INFUSIONANCIENT")) {
                IDustTrigger.triggers.remove(i);
                break;
            }
        }
        
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("INFUSIONANCIENT", ancientBlueprint));
        // the catalog is just a map so this is ok
        ThaumcraftApi.addMultiblockRecipeToCatalog(new ResourceLocation("thaumcraft", "infusionaltarancient"), new BluePrint("INFUSIONANCIENT", ancientBlueprint, new ItemStack[] {
                new ItemStack(BlocksTC.stoneAncient, 8),
                new ItemStack(BlocksTC.pedestalAncient),
                new ItemStack(BlocksTC.infusionMatrix)
        }));
        
        Part eldritchStone = new Part(BlocksTC.stoneEldritchTile, "AIR");
        Part eldritchPillarEast = new Part(BlocksTC.stoneEldritchTile, new ItemStack(BlocksTC.pillarEldritch, 1, BlockPillar.calcMeta(EnumFacing.EAST)));
        Part eldritchPillarNorth = new Part(BlocksTC.stoneEldritchTile, new ItemStack(BlocksTC.pillarEldritch, 1, BlockPillar.calcMeta(EnumFacing.NORTH)));
        Part eldritchPillarSouth = new Part(BlocksTC.stoneEldritchTile, new ItemStack(BlocksTC.pillarEldritch, 1, BlockPillar.calcMeta(EnumFacing.SOUTH)));
        Part eldritchPillarWest = new Part(BlocksTC.stoneEldritchTile, new ItemStack(BlocksTC.pillarEldritch, 1, BlockPillar.calcMeta(EnumFacing.WEST)));
        Part eldritchPedestal = new Part(BlocksTC.pedestalEldritch, null);
        Part[][][] eldritchBlueprint = new Part[][][] {
            {
                {null, null, null},
                {null, matrix, null},
                {null, null, null}
            },
            {
                {eldritchStone, null, eldritchStone},
                {null, null, null},
                {eldritchStone, null, eldritchStone}
            },
            {
                {eldritchPillarEast, null, eldritchPillarNorth},
                {null, eldritchPedestal, null},
                {eldritchPillarSouth, null, eldritchPillarWest}
            }
        };
        
        for (int i = 0; i < IDustTrigger.triggers.size(); ++i) {
            IDustTrigger trigger = IDustTrigger.triggers.get(i);
            if (trigger instanceof DustTriggerMultiblock && getDustTriggerResearch((DustTriggerMultiblock) trigger).equals("INFUSIONELDRITCH")) {
                IDustTrigger.triggers.remove(i);
                break;
            }
        }
        
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("INFUSIONELDRITCH", eldritchBlueprint));
        ThaumcraftApi.addMultiblockRecipeToCatalog(new ResourceLocation("thaumcraft", "infusionaltareldritch"), new BluePrint("INFUSIONELDRITCH", eldritchBlueprint, new ItemStack[] {
                new ItemStack(BlocksTC.stoneEldritchTile, 8),
                new ItemStack(BlocksTC.pedestalEldritch),
                new ItemStack(BlocksTC.infusionMatrix)
        }));
    }
    
}
