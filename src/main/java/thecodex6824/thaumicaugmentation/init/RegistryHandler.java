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

import net.minecraft.block.Block;
import net.minecraft.block.BlockPressurePlate.Sensitivity;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.parts.GolemHead;
import thaumcraft.api.golems.parts.PartModel;
import thaumcraft.common.golems.seals.SealHandler;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.common.block.*;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.entity.*;
import thecodex6824.thaumicaugmentation.common.golem.GolemHeadAwakened;
import thecodex6824.thaumicaugmentation.common.golem.SealAttack;
import thecodex6824.thaumicaugmentation.common.golem.SealAttackAdvanced;
import thecodex6824.thaumicaugmentation.common.golem.SealRecharge;
import thecodex6824.thaumicaugmentation.common.item.*;
import thecodex6824.thaumicaugmentation.common.item.block.ItemBlockImpetusMirror;
import thecodex6824.thaumicaugmentation.common.item.block.ItemBlockRiftJar;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.recipe.*;
import thecodex6824.thaumicaugmentation.common.tile.*;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeEmptiness;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeEmptinessHighlands;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeTaintedLands;

import java.util.HashMap;
import java.util.function.Supplier;

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
        registry.register(setupBlock(new BlockRiftMoverInput(), "rift_mover_input"));
        registry.register(setupBlock(new BlockRiftMoverOutput(), "rift_mover_output"));
        registry.register(setupBlock(new BlockRiftJar(), "rift_jar"));
        registry.register(setupBlock(new BlockVoidRechargePedestal(), "void_recharge_pedestal"));
        registry.register(setupBlock(new BlockImpetusMirror(), "impetus_mirror"));
        registry.register(setupBlock(new BlockArcaneTerraformer(), "arcane_terraformer"));
        registry.register(setupBlock(new BlockRiftMonitor(), "rift_monitor"));
        registry.register(setupBlock(new BlockImpetusGenerator(), "impetus_generator"));
        registry.register(setupBlock(new BlockStabilityFieldGenerator(), "stability_field_generator"));
        registry.register(setupBlock(new BlockImpetusGate(), "impetus_gate"));
        registry.register(setupBlock(new BlockTASlab.Half(), "slab"));
        registry.register(setupBlock(new BlockTASlab.Double(), "slab_double"));
        registry.register(setupBlock(new BlockTABars(), "bars"));
        registry.register(setupBlock(new BlockFortifiedGlass(), "fortified_glass"));
        registry.register(setupBlock(new BlockFortifiedGlassPane(), "fortified_glass_pane"));
        registry.register(setupBlock(new BlockStarfieldGlass(), "starfield_glass"));
        registry.register(setupBlock(new BlockObelisk(), "obelisk"));
        registry.register(setupBlock(new BlockCapstone(), "capstone"));
        registry.register(setupBlock(new BlockStrangeCrystal(), "strange_crystal"));
        registry.register(setupBlock(new BlockCrabVent(), "crab_vent"));
        registry.register(setupBlock(new BlockEldritchLock(), "eldritch_lock"));
        registry.register(setupBlock(new BlockRiftBarrier(), "rift_barrier"));
        registry.register(setupBlock(new BlockEldritchLockImpetus(), "eldritch_lock_impetus"));
        registry.register(setupBlock(new BlockTAButton(SoundType.WOOD, 30, true), "button_greatwood"));
        registry.register(setupBlock(new BlockTAButton(SoundType.WOOD, 30, true), "button_silverwood"));
        registry.register(setupBlock(new BlockTAButton(SoundType.STONE, 20, false), "button_arcane_stone"));
        registry.register(setupBlock(new BlockWardedButton(SoundType.WOOD, MapColor.BROWN, 30, true), "warded_button_greatwood"));
        registry.register(setupBlock(new BlockWardedButton(SoundType.WOOD, MapColor.SILVER, 30, true), "warded_button_silverwood"));
        registry.register(setupBlock(new BlockWardedButton(SoundType.STONE, MapColor.GRAY, 20, false), "warded_button_arcane_stone"));
        registry.register(setupBlock(new BlockTAPressurePlate(Material.WOOD, Sensitivity.EVERYTHING, SoundType.WOOD), "pressure_plate_greatwood"));
        registry.register(setupBlock(new BlockTAPressurePlate(Material.WOOD, Sensitivity.EVERYTHING, SoundType.WOOD), "pressure_plate_silverwood"));
        registry.register(setupBlock(new BlockTAPressurePlate(Material.ROCK, Sensitivity.MOBS, SoundType.STONE), "pressure_plate_arcane_stone"));
        registry.register(setupBlock(new BlockWardedPressurePlate(Material.WOOD, Sensitivity.EVERYTHING, SoundType.WOOD), "warded_pressure_plate_greatwood"));
        registry.register(setupBlock(new BlockWardedPressurePlate(Material.WOOD, Sensitivity.EVERYTHING, SoundType.WOOD), "warded_pressure_plate_silverwood"));
        registry.register(setupBlock(new BlockWardedPressurePlate(Material.ROCK, Sensitivity.MOBS, SoundType.STONE), "warded_pressure_plate_arcane_stone"));
        registry.register(setupBlock(new BlockTAUrn(), "urn"));
        registry.register(setupBlock(new BlockItemGrate(), "item_grate"));
        registry.register(setupBlock(new BlockGlassTube(), "glass_tube"));
        registry.register(setupBlock(new BlockImpetusCreative(), "impetus_creative"));
        
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
        GameRegistry.registerTileEntity(TileRiftJar.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "rift_jar"));
        GameRegistry.registerTileEntity(TileRiftMoverInput.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "rift_mover_input"));
        GameRegistry.registerTileEntity(TileRiftMoverOutput.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "rift_mover_output"));
        GameRegistry.registerTileEntity(TileVoidRechargePedestal.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "void_recharge_pedestal"));
        GameRegistry.registerTileEntity(TileImpetusMirror.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "impetus_mirror"));
        GameRegistry.registerTileEntity(TileArcaneTerraformer.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "arcane_terraformer"));
        GameRegistry.registerTileEntity(TileRiftMonitor.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "rift_monitor"));
        GameRegistry.registerTileEntity(TileImpetusGenerator.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "impetus_generator"));
        GameRegistry.registerTileEntity(TileStabilityFieldGenerator.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "stability_field_generator"));
        GameRegistry.registerTileEntity(TileImpetusGate.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "impetus_gate"));
        GameRegistry.registerTileEntity(TileStarfieldGlass.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "starfield_glass"));
        GameRegistry.registerTileEntity(TileObelisk.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "obelisk"));
        GameRegistry.registerTileEntity(TileObeliskVisual.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "obelisk_visual"));
        GameRegistry.registerTileEntity(TileAltar.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "altar"));
        GameRegistry.registerTileEntity(TileCrabVent.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "crab_vent"));
        GameRegistry.registerTileEntity(TileEldritchLock.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "eldritch_lock"));
        GameRegistry.registerTileEntity(TileRiftBarrier.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "rift_barrier"));
        GameRegistry.registerTileEntity(TileWardedButton.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "warded_button"));
        GameRegistry.registerTileEntity(TileWardedPressurePlate.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "warded_pressure_plate"));
        GameRegistry.registerTileEntity(TileItemGrate.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "item_grate"));
        GameRegistry.registerTileEntity(TileGlassTube.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "glass_tube"));
        GameRegistry.registerTileEntity(TileCreativeImpetusSource.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "impetus_source_creative"));
        GameRegistry.registerTileEntity(TileCreativeImpetusSink.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "impetus_sink_creative"));
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerBlocksLater(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(setupBlock(new BlockTAStairs(BlocksTC.stoneAncient.getDefaultState()), "stairs_ancient"));
        registry.register(setupBlock(new BlockTAStairs(BlocksTC.stoneEldritchTile.getDefaultState()), "stairs_eldritch_tile"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (Block b : TABlocks.getAllBlocks()) {
            if (b instanceof IItemBlockProvider)
                registry.register(((IItemBlockProvider) b).createItemBlock().setRegistryName(b.getRegistryName()));
        }

        registry.register(setupItem(new ItemTieredCasterGauntlet(), "gauntlet"));
        registry.register(setupItem(new ItemTABase("lattice", "warding_sigil", "amalgamated_gear", "rift_energy_cell", "harness_base", "impetus_resonator"), "material"));
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
        registry.register(setupItem(new ItemBlockRiftJar(), "rift_jar"));
        registry.register(setupItem(new ItemBlockImpetusMirror(), "impetus_mirror"));
        registry.register(setupItem(new ItemImpetusLinker(), "impetus_linker"));
        registry.register(setupItem(new ItemBiomeSelector(), "biome_selector").setCreativeTab(TAItems.BIOME_SELECTOR_CREATIVE_TAB));
        registry.register(setupItem(new ItemThaumostaticHarness(), "thaumostatic_harness"));
        registry.register(setupItem(new ItemThaumostaticHarnessAugment(), "thaumostatic_harness_augment"));
        registry.register(setupItem(new ItemElytraHarness(), "elytra_harness"));
        registry.register(setupItem(new ItemElytraHarnessAugment(), "elytra_harness_augment"));
        registry.register(setupItem(new ItemAutocasterPlacer(), "autocaster_placer"));
        registry.register(setupItem(new ItemImpulseCannon(), "impulse_cannon"));
        registry.register(setupItem(new ItemImpulseCannonAugment(), "impulse_cannon_augment"));
        registry.register(new ItemFocusAncient()); // had to setup in constructor due to TC doing things to the item
        registry.register(setupItem(new ItemEldritchLockKey(), "eldritch_lock_key"));
        registry.register(setupItem(new ItemObeliskPlacer(), "obelisk_placer"));
        registry.register(setupItem(new ItemResearchNotes(), "research_notes"));
        registry.register(setupItem(new ItemVisBatteryCasterAugment(), "augment_vis_battery"));
        registry.register(setupItem(new ItemThaumiumRobes(EntityEquipmentSlot.HEAD), "thaumium_robes_hood"));
        registry.register(setupItem(new ItemThaumiumRobes(EntityEquipmentSlot.CHEST), "thaumium_robes_chestplate"));
        registry.register(setupItem(new ItemThaumiumRobes(EntityEquipmentSlot.LEGS), "thaumium_robes_leggings"));
        registry.register(setupItem(new ItemCelestialObserverPlacer(), "celestial_observer_placer"));
        
        AugmentHandler.registerAugmentBuilderComponents();
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerItemsLater(RegistryEvent.Register<Item> event) {
        // to force classloading here, due to registrations in static block
        try {
            Class.forName("thaumcraft.common.golems.GolemProperties");
        }
        catch (Exception ex) {}

        GolemHead.register(new GolemHead("HEAD_AWAKENED", new String[] {"BASEGOLEMANCY"},
                new ResourceLocation("thaumcraft", "textures/misc/golem/head_smart.png"),
                new PartModel(new ResourceLocation("thaumcraft", "models/obj/golem_head_smart.obj"), new ResourceLocation("thaumcraft", "textures/entity/golems/golem_head_other.png"), PartModel.EnumAttachPoint.HEAD),
                new Object[] {},
                new GolemHeadAwakened(),
                new EnumGolemTrait[] { EnumGolemTrait.SCOUT, EnumGolemTrait.SMART }));
        SealHandler.registerSeal(new SealAttack());
        SealHandler.registerSeal(new SealAttackAdvanced());
        SealHandler.registerSeal(new SealRecharge());
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        OreDictionary.registerOre("stoneVoid", new ItemStack(TABlocks.STONE, 1, StoneType.STONE_VOID.getMeta()));
        OreDictionary.registerOre("barsIron", Blocks.IRON_BARS);
        OreDictionary.registerOre("trapdoorWood", Blocks.TRAPDOOR);
        OreDictionary.registerOre("blockAmber", BlocksTC.amberBlock);
        OreDictionary.registerOre("blockAmber", BlocksTC.amberBrick);
        
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
        event.getRegistry().register(new MorphicArmorUnbindingRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "morphic_armor_unbinding")));
        event.getRegistry().register(new PrimalCutterAbilityRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "primal_cutter_ability")));
        event.getRegistry().register(new BiomeSelectorSpecialResetRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "biome_focus_special_reset")));
        event.getRegistry().register(new ThaumiumHoodStyleRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "style_cycle")));
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
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityItemBlockRiftJar.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "item_rift_jar"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".item_rift_jar").tracker(64, 20, true).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityItemIndestructible.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "item_indestructible"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".item_important").tracker(64, 20, true).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityFocusShield.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "shield_focus"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".shield_focus").tracker(256, 1, false).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityAutocaster.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "autocaster"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".autocaster").tracker(64, 3, false).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityAutocasterEldritch.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "autocaster_eldritch"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".autocaster_eldritch").tracker(64, 3, false).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityTAEldritchGuardian.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "eldritch_guardian"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".eldritch_guardian").tracker(64, 3, true).egg(
                        0x808080, 0x880000).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityTAEldritchWarden.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "eldritch_warden"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".eldritch_warden").tracker(64, 3, true).egg(
                        0x383882, 0x880000).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityPrimalWisp.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "primal_wisp"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".primal_wisp").tracker(64, 1, true).egg(
                        0xC71585, 0xFF1493).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityTAEldritchGolem.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "eldritch_golem"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".eldritch_golem").tracker(64, 3, true).egg(
                        0x383882, 0x888888).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityTAGolemOrb.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "golem_orb"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".golem_orb").tracker(64, 1, true).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityCelestialObserver.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "celestial_observer"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".celestial_observer").tracker(64, 1, true).build());
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityItemImportant.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "item_important"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".item_important").tracker(64, 20, true).build());
    }
    
    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(TASounds.getAllSounds());
    }
    
    @SubscribeEvent
    public static void registerAspects(AspectRegistryEvent event) {
        /*
         * =======================================================================================
         * This must be used for registering aspect tags on objects (aka not entities) ONLY!
         * Thaumic Speedup only calls this code at first pack load, not when using cached aspects!
         * =======================================================================================
         */
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
        proxy.registerObjectTag(new ItemStack(TAItems.MATERIAL, 1, 5), new AspectList().add(Aspect.ORDER, 15).add(Aspect.ENERGY, 10));
        proxy.registerObjectTag(new ItemStack(TAItems.RIFT_SEED), new AspectList());
        proxy.registerObjectTag(new ItemStack(TAItems.SEAL_COPIER), new AspectList().add(Aspect.MIND, 15).add(Aspect.TOOL, 5));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.VOID_BOOTS), new AspectList().add(Aspect.ELDRITCH, 43).add(Aspect.VOID, 23));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.FRACTURE_LOCATOR), new AspectList().add(Aspect.VOID, 3).add(Aspect.TOOL, 5));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.AUGMENT_CASTER_RIFT_ENERGY_STORAGE), new AspectList().add(Aspect.AVERSION, 3).add(Aspect.TOOL, 5).add(Aspect.MECHANISM, 5).add(Aspect.VOID, 5));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.THAUMOSTATIC_HARNESS), new AspectList().add(Aspect.FLIGHT, 55));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.ELYTRA_HARNESS), new AspectList().add(Aspect.FLIGHT, 35));
        proxy.registerObjectTag(new ItemStack(TAItems.FOCUS_ANCIENT), new AspectList().add(Aspect.CRYSTAL, 30).add(Aspect.ENERGY, 20).add(Aspect.AURA, 25).add(Aspect.ELDRITCH, 50).add(Aspect.MAGIC, 15));
        
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.ARCANE_TRAPDOOR_METAL), new AspectList().add(Aspect.PROTECT, 7));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.ARCANE_TRAPDOOR_WOOD), new AspectList().add(Aspect.PROTECT, 7));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.ARCANE_TRAPDOOR_SILVERWOOD), new AspectList().add(Aspect.PROTECT, 7));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 0), new AspectList().add(Aspect.EARTH, 5).add(Aspect.VOID, 5).add(Aspect.DARKNESS, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 1), new AspectList().add(Aspect.EARTH, 3).add(Aspect.VOID, 3).add(Aspect.DARKNESS, 3).add(Aspect.FLUX, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 2), new AspectList().add(Aspect.EARTH, 3).add(Aspect.VOID, 3).add(Aspect.DARKNESS, 3).add(Aspect.FLUX, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 3), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5).add(Aspect.MIND, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 4), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5).add(Aspect.MIND, 10));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 5), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5).add(Aspect.ORDER, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 6), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5).add(Aspect.ENTROPY, 3).add(Aspect.LIFE, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 7), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5).add(Aspect.ENTROPY, 3).add(Aspect.LIFE, 3).add(Aspect.LIGHT, 10));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 8), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5).add(Aspect.ORDER, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 9), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5).add(Aspect.ORDER, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 11), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5).add(Aspect.LIGHT, 10));
        proxy.registerObjectTag(new ItemStack(TABlocks.TAINT_FLOWER), new AspectList().add(Aspect.FLUX, 10).add(Aspect.PLANT, 5));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.VIS_REGENERATOR), new AspectList().add(Aspect.AURA, 20).add(Aspect.MECHANISM, 15).add(Aspect.ENERGY, 5));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.WARDED_CHEST), new AspectList().add(Aspect.PROTECT, 7));
        proxy.registerObjectTag(new ItemStack(TABlocks.BARS), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5).add(Aspect.TRAP, 5));
        proxy.registerObjectTag(new ItemStack(TABlocks.FORTIFIED_GLASS), new AspectList().add(Aspect.CRYSTAL, 30).add(Aspect.PROTECT, 12));
        proxy.registerObjectTag(new ItemStack(TABlocks.FORTIFIED_GLASS_PANE), new AspectList().add(Aspect.CRYSTAL, 5).add(Aspect.PROTECT, 2));
        proxy.registerObjectTag(new ItemStack(TABlocks.STARFIELD_GLASS, 1, 0), new AspectList().add(Aspect.CRYSTAL, 30).add(Aspect.PROTECT, 12).add(Aspect.ELDRITCH, 10));
        proxy.registerObjectTag(new ItemStack(TABlocks.STARFIELD_GLASS, 1, 1), new AspectList().add(Aspect.CRYSTAL, 30).add(Aspect.PROTECT, 12).add(Aspect.VOID, 5).add(Aspect.FLUX, 5));
        proxy.registerObjectTag(new ItemStack(TABlocks.STARFIELD_GLASS, 1, 1), new AspectList().add(Aspect.CRYSTAL, 30).add(Aspect.PROTECT, 12).add(Aspect.MAGIC, 5).add(Aspect.AURA, 5));
        proxy.registerObjectTag(new ItemStack(TABlocks.STRANGE_CRYSTAL), new AspectList().add(Aspect.AIR, 25).add(Aspect.EARTH, 25).add(Aspect.ENTROPY, 25).add(Aspect.FIRE, 25).add(Aspect.ORDER, 25).add(Aspect.WATER, 25));
        proxy.registerObjectTag(new ItemStack(TABlocks.RIFT_JAR), new AspectList());
        proxy.registerObjectTag(new ItemStack(TABlocks.URN, 1, 0), new AspectList().add(Aspect.DESIRE, 15).add(Aspect.EARTH, 5));
        proxy.registerObjectTag(new ItemStack(TABlocks.URN, 1, 1), new AspectList().add(Aspect.DESIRE, 30).add(Aspect.EARTH, 5));
        proxy.registerObjectTag(new ItemStack(TABlocks.URN, 1, 2), new AspectList().add(Aspect.DESIRE, 50).add(Aspect.EARTH, 5));
        proxy.registerObjectTag(new ItemStack(TABlocks.CAPSTONE, 1, 0), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 10));
        proxy.registerObjectTag(new ItemStack(TABlocks.CAPSTONE, 1, 1), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 10));
        proxy.registerObjectTag(new ItemStack(TABlocks.CAPSTONE, 1, 2), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 15).add(Aspect.MAGIC, 5).add(Aspect.SOUL, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.CAPSTONE, 1, 3), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 15).add(Aspect.MAGIC, 5).add(Aspect.SOUL, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.OBELISK), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 10));
        AspectList shared = new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 15).add(Aspect.MECHANISM, 15);
        for (int i = 0; i < 16; ++i)
            proxy.registerObjectTag(new ItemStack(TABlocks.ELDRITCH_LOCK, 1, i), shared.copy());
        
        shared = new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 15).add(Aspect.MECHANISM, 15).add(Aspect.ENERGY, 10);
        for (int i = 0; i < 4; ++i)
            proxy.registerObjectTag(new ItemStack(TABlocks.ELDRITCH_LOCK_IMPETUS, 1, i), shared.copy());
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
