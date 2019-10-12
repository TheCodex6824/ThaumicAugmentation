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

import java.util.HashMap;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectEventProxy;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.AspectRegistryEvent;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.aspect.AspectElementInteractionManager;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.common.block.BlockArcaneDoor;
import thecodex6824.thaumicaugmentation.common.block.BlockArcaneTrapdoor;
import thecodex6824.thaumicaugmentation.common.block.BlockCastedLight;
import thecodex6824.thaumicaugmentation.common.block.BlockImpetusDiffuser;
import thecodex6824.thaumicaugmentation.common.block.BlockImpetusDrainer;
import thecodex6824.thaumicaugmentation.common.block.BlockImpetusMatrix;
import thecodex6824.thaumicaugmentation.common.block.BlockImpetusMatrixBase;
import thecodex6824.thaumicaugmentation.common.block.BlockImpetusRelay;
import thecodex6824.thaumicaugmentation.common.block.BlockRiftFeeder;
import thecodex6824.thaumicaugmentation.common.block.BlockTAStone;
import thecodex6824.thaumicaugmentation.common.block.BlockTaintFlower;
import thecodex6824.thaumicaugmentation.common.block.BlockVisRegenerator;
import thecodex6824.thaumicaugmentation.common.block.BlockWardedChest;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.entity.EntityDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.item.ItemArcaneDoor;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterAugment;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterEffectProvider;
import thecodex6824.thaumicaugmentation.common.item.ItemCustomCasterStrengthProvider;
import thecodex6824.thaumicaugmentation.common.item.ItemFractureLocator;
import thecodex6824.thaumicaugmentation.common.item.ItemKey;
import thecodex6824.thaumicaugmentation.common.item.ItemMorphicTool;
import thecodex6824.thaumicaugmentation.common.item.ItemPrimalCutter;
import thecodex6824.thaumicaugmentation.common.item.ItemRiftEnergyCasterAugment;
import thecodex6824.thaumicaugmentation.common.item.ItemRiftSeed;
import thecodex6824.thaumicaugmentation.common.item.ItemSealCopier;
import thecodex6824.thaumicaugmentation.common.item.ItemTieredCasterGauntlet;
import thecodex6824.thaumicaugmentation.common.item.ItemVoidBoots;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.recipe.AugmentAdditionRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.AugmentRemovalRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.AuthorizedKeyCreationRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.CustomAugmentCreationRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.DyeableItemRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.ElementChangeRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.MorphicToolUnbindingRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.ThaumiumKeyCopyRecipe;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneDoor;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneTrapdoor;
import thecodex6824.thaumicaugmentation.common.tile.TileCastedLight;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusDiffuser;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusDrainer;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusMatrix;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusRelay;
import thecodex6824.thaumicaugmentation.common.tile.TileRiftFeeder;
import thecodex6824.thaumicaugmentation.common.tile.TileVisRegenerator;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedChest;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeEmptiness;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeEmptinessHighlands;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeTaintedLands;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class RegistryHandler {

    private RegistryHandler() {}
    
    private static final HashMap<ResourceLocation, Supplier<Item>> ITEM_REMAP = new HashMap<>();
    private static final HashMap<ResourceLocation, Supplier<Block>> BLOCK_REMAP = new HashMap<>();
    
    static {
        ITEM_REMAP.put(new ResourceLocation(ThaumicAugmentationAPI.MODID, "augment_caster_elemental"), () -> TAItems.AUGMENT_CUSTOM);
    
        BLOCK_REMAP.put(new ResourceLocation(ThaumicAugmentationAPI.MODID, "arcane_door"), () -> TABlocks.ARCANE_DOOR_THAUMIUM);
    }
    
    private static Block setupBlock(Block block, String name) {
        return block.setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, name)).setTranslationKey(
                ThaumicAugmentationAPI.MODID + "." + name).setCreativeTab(TAItems.CREATIVE_TAB);
    }

    private static Item setupItem(Item item, String name) {
        return item.setRegistryName(name).setTranslationKey(ThaumicAugmentationAPI.MODID + "." + name).setCreativeTab(
                TAItems.CREATIVE_TAB);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(setupBlock(new BlockVisRegenerator(), "vis_regenerator"));
        registry.register(setupBlock(new BlockWardedChest(), "warded_chest"));
        registry.register(setupBlock(new BlockArcaneDoor(Material.WOOD, 0), "arcane_door_greatwood"));
        registry.register(setupBlock(new BlockArcaneDoor(Material.IRON, 1), "arcane_door_thaumium"));
        registry.register(setupBlock(new BlockArcaneDoor(Material.WOOD, 2), "arcane_door_silverwood"));
        registry.register(setupBlock(new BlockCastedLight(), "temporary_light"));
        registry.register(setupBlock(new BlockTAStone(), "stone"));
        registry.register(setupBlock(new BlockArcaneTrapdoor(Material.WOOD), "arcane_trapdoor_wood"));
        registry.register(setupBlock(new BlockArcaneTrapdoor(Material.IRON), "arcane_trapdoor_metal"));
        registry.register(setupBlock(new BlockArcaneTrapdoor(Material.WOOD), "arcane_trapdoor_silverwood"));
        registry.register(setupBlock(new BlockTaintFlower(), "taint_flower"));
        registry.register(setupBlock(new BlockImpetusDrainer(), "impetus_drainer"));
        registry.register(setupBlock(new BlockImpetusRelay(), "impetus_relay"));
        registry.register(setupBlock(new BlockImpetusDiffuser(), "impetus_diffuser"));
        registry.register(setupBlock(new BlockImpetusMatrix(), "impetus_matrix"));
        registry.register(setupBlock(new BlockImpetusMatrixBase(), "impetus_matrix_base"));
        registry.register(setupBlock(new BlockRiftFeeder(), "rift_feeder"));

        GameRegistry.registerTileEntity(TileVisRegenerator.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "vis_regenerator"));
        GameRegistry.registerTileEntity(TileWardedChest.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "warded_chest"));
        GameRegistry.registerTileEntity(TileArcaneDoor.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "arcane_door"));
        GameRegistry.registerTileEntity(TileCastedLight.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "temporary_light"));
        GameRegistry.registerTileEntity(TileArcaneTrapdoor.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "arcane_trapdoor"));
        GameRegistry.registerTileEntity(TileImpetusDrainer.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "impetus_drainer"));
        GameRegistry.registerTileEntity(TileImpetusRelay.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "impetus_relay"));
        GameRegistry.registerTileEntity(TileImpetusDiffuser.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "impetus_diffuser"));
        GameRegistry.registerTileEntity(TileImpetusMatrix.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "impetus_matrix"));
        GameRegistry.registerTileEntity(TileRiftFeeder.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "rift_feeder"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (Block b : TABlocks.getAllBlocks()) {
            if (b instanceof IItemBlockProvider)
                registry.register(((IItemBlockProvider) b).createItemBlock().setRegistryName(b.getRegistryName()));
        }

        registry.register(setupItem(new ItemTieredCasterGauntlet(), "gauntlet"));
        registry.register(setupItem(new ItemTABase("lattice", "warding_sigil", "amalgamated_gear", "rift_energy_cell"), "material"));
        registry.register(setupItem(new ItemSealCopier(), "seal_copier"));
        registry.register(setupItem(new ItemArcaneDoor(), "arcane_door"));
        registry.register(setupItem(new ItemKey(), "key"));
        registry.register(setupItem(new ItemVoidBoots(), "void_boots"));
        registry.register(setupItem(new ItemRiftSeed(), "rift_seed"));
        registry.register(setupItem(new ItemRiftEnergyCasterAugment(), "augment_caster_rift_energy_storage"));
        registry.register(setupItem(new ItemFractureLocator(), "fracture_locator"));
        registry.register(setupItem(new ItemCustomCasterStrengthProvider(), "augment_builder_power"));
        registry.register(setupItem(new ItemCustomCasterEffectProvider(), "augment_builder_effect"));
        registry.register(setupItem(new ItemCustomCasterAugment(), "augment_custom"));
        registry.register(setupItem(new ItemMorphicTool(), "morphic_tool"));
        registry.register(setupItem(new ItemPrimalCutter(), "primal_cutter"));
        
        AugmentHandler.registerAugmentBuilderComponents();
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        OreDictionary.registerOre("stoneVoid", new ItemStack(TABlocks.STONE, 1, StoneType.STONE_VOID.getMeta()));
        
        RecipeHandler.initInfusionRecipes();
        RecipeHandler.initCrucibleRecipes();
        RecipeHandler.initArcaneCraftingRecipes();
        RecipeHandler.initMultiblocks();
        
        event.getRegistry().register(new DyeableItemRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "dyeable_item")));
        event.getRegistry().register(new AuthorizedKeyCreationRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "bound_key_creation")));
        event.getRegistry().register(new ThaumiumKeyCopyRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "thaumium_key_copy")));
        event.getRegistry().register(new AugmentAdditionRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "augment_addition")));
        event.getRegistry().register(new AugmentRemovalRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "augment_removal")));
        event.getRegistry().register(new ElementChangeRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "element_swap")));
        event.getRegistry().register(new CustomAugmentCreationRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "custom_augment")));
        event.getRegistry().register(new MorphicToolUnbindingRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "morphic_tool_unbinding")));
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        Biome emptiness = new BiomeEmptiness().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "emptiness"));
        event.getRegistry().register(emptiness);
        BiomeDictionary.addTypes(emptiness, Type.COLD, Type.SPARSE, Type.SPOOKY, Type.VOID);
        
        Biome tainted = new BiomeTaintedLands().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "tainted_lands"));
        event.getRegistry().register(tainted);
        BiomeDictionary.addTypes(tainted, Type.COLD, Type.SPOOKY, Type.VOID);
        
        Biome highlands = new BiomeEmptinessHighlands().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "emptiness_highlands"));
        event.getRegistry().register(highlands);
        BiomeDictionary.addTypes(highlands, Type.COLD, Type.SPARSE, Type.SPOOKY, Type.VOID, Type.HILLS, Type.MOUNTAIN);
    }
    
    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        int id = 0;
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityDimensionalFracture.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "dimensional_fracture"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".dimensional_fracture").tracker(128, 4, false).build());
    }
    
    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(TASounds.getAllSounds());
    }
    
    @SubscribeEvent
    public static void registerAspects(AspectRegistryEvent event) {
        AspectEventProxy proxy = event.register;
        proxy.registerComplexObjectTag(new ItemStack(TAItems.ARCANE_DOOR, 1, 0), new AspectList().add(Aspect.PROTECT, 19));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.ARCANE_DOOR, 1, 1), new AspectList().add(Aspect.PROTECT, 23));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.ARCANE_DOOR, 1, 2), new AspectList().add(Aspect.PROTECT, 19));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.GAUNTLET, 1, 0), new AspectList().add(Aspect.MAGIC, 8));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.GAUNTLET, 1, 1), new AspectList().add(Aspect.ELDRITCH, 27).add(Aspect.VOID, 23));
        proxy.registerObjectTag(new ItemStack(TAItems.KEY, 1, 0), new AspectList().add(Aspect.PROTECT, 5).add(Aspect.MIND, 10).add(Aspect.METAL, 3));
        proxy.registerObjectTag(new ItemStack(TAItems.KEY, 1, 1), new AspectList().add(Aspect.PROTECT, 5).add(Aspect.MIND, 10).add(Aspect.METAL, 3));
        proxy.registerObjectTag(new ItemStack(TAItems.KEY, 1, 2), new AspectList().add(Aspect.PROTECT, 5).add(Aspect.MIND, 10).add(Aspect.METAL, 3));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.MATERIAL, 1, 0), new AspectList().add(Aspect.AURA, 7).add(Aspect.PLANT, 6));
        proxy.registerObjectTag(new ItemStack(TAItems.MATERIAL, 1, 1), new AspectList().add(Aspect.PROTECT, 15).add(Aspect.MIND, 10));
        proxy.registerObjectTag(new ItemStack(TAItems.MATERIAL, 1, 3), new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.VOID, 15).add(Aspect.MECHANISM, 5));
        proxy.registerObjectTag(new ItemStack(TAItems.RIFT_SEED), new AspectList());
        proxy.registerObjectTag(new ItemStack(TAItems.SEAL_COPIER), new AspectList().add(Aspect.MIND, 15).add(Aspect.TOOL, 5));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.VOID_BOOTS), new AspectList().add(Aspect.ELDRITCH, 43).add(Aspect.VOID, 23));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.FRACTURE_LOCATOR), new AspectList().add(Aspect.VOID, 3).add(Aspect.TOOL, 5));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.AUGMENT_CASTER_RIFT_ENERGY_STORAGE), new AspectList().add(Aspect.AVERSION, 3).add(Aspect.TOOL, 5).add(Aspect.MECHANISM, 5).add(Aspect.VOID, 5));
        
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.ARCANE_TRAPDOOR_METAL), new AspectList().add(Aspect.PROTECT, 7));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.ARCANE_TRAPDOOR_WOOD), new AspectList().add(Aspect.PROTECT, 7));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.ARCANE_TRAPDOOR_SILVERWOOD), new AspectList().add(Aspect.PROTECT, 7));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 0), new AspectList().add(Aspect.EARTH, 5).add(Aspect.VOID, 5).add(Aspect.DARKNESS, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 1), new AspectList().add(Aspect.EARTH, 3).add(Aspect.VOID, 3).add(Aspect.DARKNESS, 3).add(Aspect.FLUX, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 2), new AspectList().add(Aspect.EARTH, 3).add(Aspect.VOID, 3).add(Aspect.DARKNESS, 3).add(Aspect.FLUX, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.TAINT_FLOWER), new AspectList().add(Aspect.FLUX, 10).add(Aspect.PLANT, 5));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.VIS_REGENERATOR), new AspectList().add(Aspect.AURA, 20).add(Aspect.MECHANISM, 15).add(Aspect.ENERGY, 5));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.WARDED_CHEST), new AspectList().add(Aspect.PROTECT, 7));
    
        AspectElementInteractionManager.init();
    }
    
    @SubscribeEvent
    public static void onMissingItemMapping(RegistryEvent.MissingMappings<Item> event) {
        for (Mapping<Item> mapping : event.getMappings()) {
            if (ITEM_REMAP.containsKey(mapping.key))
                mapping.remap(ITEM_REMAP.get(mapping.key).get());
        }
    }
    
    @SubscribeEvent
    public static void onMissingBlockMapping(RegistryEvent.MissingMappings<Block> event) {
        for (Mapping<Block> mapping : event.getMappings()) {
            if (BLOCK_REMAP.containsKey(mapping.key))
                mapping.remap(BLOCK_REMAP.get(mapping.key).get());
        }
    }

}
