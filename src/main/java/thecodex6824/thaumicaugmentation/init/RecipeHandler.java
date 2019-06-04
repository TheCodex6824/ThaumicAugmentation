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
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.blocks.basic.BlockPillar;
import thaumcraft.common.lib.crafting.DustTriggerMultiblock;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.recipe.ElementalAugmentCraftingRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.FluxSeedGrowthRecipe;

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
    
    private static void initInfusionRecipes() {
        ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "GauntletVoid"), 
                new InfusionRecipe("GAUNTLET_VOID", new ItemStack(TAItems.GAUNTLET, 1, 1), 6, 
                        new AspectList().add(Aspect.ENERGY, 50).add(Aspect.ELDRITCH, 75).add(Aspect.VOID, 75), 
                        new ItemStack(ItemsTC.charmVoidseer), new Object[] {
                                new ItemStack(ItemsTC.fabric), "plateVoid", "plateVoid", "plateVoid", "plateVoid", new ItemStack(ItemsTC.salisMundus)
                        }
            ));
            ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "SealCopier"), 
                new InfusionRecipe("SEAL_COPIER", new ItemStack(TAItems.SEAL_COPIER), 1, 
                        new AspectList().add(Aspect.MIND, 25).add(Aspect.MECHANISM, 10), 
                        new ItemStack(ItemsTC.golemBell), new Object[] { 
                                new ItemStack(ItemsTC.brain), new ItemStack(ItemsTC.brain), new ItemStack(ItemsTC.seals, 1, 0)
                        }
            ));
            ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "BootsVoid"),
                new InfusionRecipe("BOOTS_VOID", new ItemStack(TAItems.VOID_BOOTS), 6,
                        new AspectList().add(Aspect.VOID, 50).add(Aspect.ELDRITCH, 50).add(Aspect.MOTION, 150).add(Aspect.FLIGHT, 150), 
                        new ItemStack(ItemsTC.travellerBoots), new Object[] {
                                new ItemStack(ItemsTC.fabric), new ItemStack(ItemsTC.fabric), "plateVoid", "plateVoid", new ItemStack(Items.FEATHER),
                                new ItemStack(Items.FISH), new ItemStack(ItemsTC.primordialPearl), new ItemStack(ItemsTC.quicksilver)
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
            
            ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "EldritchStoneInfusion"), 
                    new InfusionRecipe("VOID_STONE", new ItemStack(BlocksTC.stoneEldritchTile), 12, new AspectList().add(Aspect.ELDRITCH, 30), 
                    new ItemStack(TABlocks.STONE, 1, 0), new Object[] {
                            ThaumcraftApiHelper.makeCrystal(Aspect.ELDRITCH), ThaumcraftApiHelper.makeCrystal(Aspect.ELDRITCH)
                    }
            ));
    }
    
    private static void initCrucibleRecipes() {
        ItemStack fluxSeedStack = new ItemStack(TAItems.RIFT_SEED, 1, 1);
        fluxSeedStack.setTagCompound(new NBTTagCompound());
        fluxSeedStack.getTagCompound().setInteger("flux", 100);
        ThaumcraftApi.addCrucibleRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "FluxSeed"), 
                new CrucibleRecipe("RIFT_STUDIES", fluxSeedStack, ItemsTC.voidSeed, 
                        new AspectList().add(Aspect.FLUX, 50)));
    }
    
    private static void initArcaneCraftingRecipes() {
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "GauntletThaumium"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "GAUNTLET_THAUMIUM"), "GAUNTLET_THAUMIUM", 250, 
                new AspectList().add(Aspect.AIR, 2).add(Aspect.EARTH, 2).add(Aspect.ENTROPY, 2).add(Aspect.FIRE, 2).add(
                        Aspect.ORDER, 2).add(Aspect.WATER, 2), new ItemStack(TAItems.GAUNTLET, 1, 0), new Object[] {
                                "PPP",
                                "FRF",
                                "FTF",
                                'P', new ItemStack(ItemsTC.plate, 1, 2), 'F', new ItemStack(ItemsTC.fabric), 'R', new ItemStack(ItemsTC.visResonator),
                                'T', new ItemStack(ItemsTC.thaumometer)
                        }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "Lattice"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "LATTICE"), "VIS_REGENERATOR", 25, 
                new AspectList().add(Aspect.AIR, 1).add(Aspect.WATER, 1), 
                new ItemStack(TAItems.MATERIAL, 1, 0), new Object[] {
                        "SLS",
                        "LFL",
                        "SLS",
                        'S', new ItemStack(BlocksTC.plankSilverwood), 'L', new ItemStack(BlocksTC.leafSilverwood), 'F', new ItemStack(ItemsTC.filter)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "VisRegenerator"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "VIS_REGENERATOR"), "VIS_REGENERATOR", 100, 
                new AspectList().add(Aspect.AIR, 2), 
                new ItemStack(TABlocks.VIS_REGENERATOR, 1, 0), new Object[] {
                        "GIG",
                        "BLB",
                        "GEG",
                        'G', new ItemStack(BlocksTC.plankGreatwood), 'I', new ItemStack(Blocks.IRON_BARS), 'B', new ItemStack(ItemsTC.plate, 1, 0),
                        'L', new ItemStack(TAItems.MATERIAL, 1, 0), 'E', new ItemStack(ItemsTC.mechanismSimple)
                }
        ));

        /**
         * Warded Stuff
         */

        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "WardingSigil"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "WardingSigil"), "WARDED_ARCANA@2", 10, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1), 
                new ItemStack(TAItems.MATERIAL, 1, 1), new Object[] {
                        " T ",
                        "PBP",
                        " T ",
                        'T', new ItemStack(ItemsTC.tallow), 'P', "dyePurple", 'B', new ItemStack(ItemsTC.brain)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "WardedChest"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "WardedChest"), "WARDED_ARCANA", 75, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),  
                new ItemStack(TABlocks.WARDED_CHEST), new Object[] {
                        " S ",
                        "TCT",
                        "TTT",
                        'T', new ItemStack(ItemsTC.plate, 1, 2), 'S', new ItemStack(TAItems.MATERIAL, 1, 1), 'C', "chestWood"
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorWood"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorWood"), "ARCANE_DOOR", 100, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1), 
                new ItemStack(TAItems.ARCANE_DOOR, 1, 0), new Object[] {
                        "GBG",
                        "GSG",
                        "GBG",
                        'G', new ItemStack(BlocksTC.plankGreatwood), 'B', new ItemStack(ItemsTC.plate, 1, 0), 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorMetal"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorMetal"), "ARCANE_DOOR", 100, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),  
                new ItemStack(TAItems.ARCANE_DOOR, 1, 1), new Object[] {
                        "TIT",
                        "TST",
                        "TIT",
                        'T', new ItemStack(ItemsTC.plate, 1, 2), 'I', new ItemStack(ItemsTC.plate, 1, 1), 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
                }
        ));

        /**
         * Keys
         */

        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "KeyIron"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "KeyIron"), "WARD_KEYS", 15, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1), 
                new ItemStack(TAItems.KEY, 1, 0), new Object[] {
                        "  B",
                        " NN",
                        "NN ",
                        'B', new ItemStack(ItemsTC.brain), 'N', "nuggetIron"
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "KeyBrass"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "KeyBrass"), "WARD_KEYS", 15, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1), 
                new ItemStack(TAItems.KEY, 1, 1), new Object[] {
                        "  B",
                        " NN",
                        "NN ",
                        'B', new ItemStack(ItemsTC.brain), 'N', new ItemStack(ItemsTC.nuggets, 1, 8)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "KeyThaumium"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "KeyThaumium"), "WARD_KEYS", 15, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1), 
                new ItemStack(TAItems.KEY, 1, 2), new Object[] {
                        "  B",
                        " NN",
                        "NN ",
                        'B', new ItemStack(ItemsTC.brain), 'N', new ItemStack(ItemsTC.nuggets, 1, 6)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneTrapdoorWood"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneTrapdoorWood"), "ARCANE_DOOR", 100, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),
                new ItemStack(TABlocks.ARCANE_TRAPDOOR_WOOD, 2), new Object[] {
                        "WWW",
                        "BSB",
                        "WWW",
                        'W', new ItemStack(BlocksTC.plankGreatwood), 'B', new ItemStack(ItemsTC.plate, 1, 0), 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneTrapdoorMetal"), new ShapedArcaneRecipe(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneTrapdoorMetal"), "ARCANE_DOOR", 100, 
                new AspectList().add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1),
                new ItemStack(TABlocks.ARCANE_TRAPDOOR_METAL, 2), new Object[] {
                        "TTT",
                        "ISI",
                        "TTT",
                        'T', new ItemStack(ItemsTC.plate, 1, 2), 'I', new ItemStack(ItemsTC.plate, 1, 1), 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
                }
        ));
        ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "AugmentationGauntletElemental"), new ElementalAugmentCraftingRecipe());
    }
    
    private static String getDustTriggerResearch(DustTriggerMultiblock trigger) {
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
     * the last few betas (since the stablizier mechanics were reworked)
     */
    private static void fixInfusionAltarMultiblocks() {
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
    
    public static void init() {
        initInfusionRecipes();
        initCrucibleRecipes();
        initArcaneCraftingRecipes();
        fixInfusionAltarMultiblocks();
    }

}
