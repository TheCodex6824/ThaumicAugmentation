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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectEventProxy;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.AspectRegistryEvent;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.aspect.AspectElementInteractionManager;
import thecodex6824.thaumicaugmentation.common.block.BlockArcaneDoor;
import thecodex6824.thaumicaugmentation.common.block.BlockArcaneTrapdoor;
import thecodex6824.thaumicaugmentation.common.block.BlockCastedLight;
import thecodex6824.thaumicaugmentation.common.block.BlockTAStone;
import thecodex6824.thaumicaugmentation.common.block.BlockTaintFlower;
import thecodex6824.thaumicaugmentation.common.block.BlockVisRegenerator;
import thecodex6824.thaumicaugmentation.common.block.BlockWardedChest;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.entity.EntityDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.entity.EntityUtil;
import thecodex6824.thaumicaugmentation.common.item.ItemRiftEnergyGauntletAugment;
import thecodex6824.thaumicaugmentation.common.item.ItemArcaneDoor;
import thecodex6824.thaumicaugmentation.common.item.ItemElementalAugment;
import thecodex6824.thaumicaugmentation.common.item.ItemKey;
import thecodex6824.thaumicaugmentation.common.item.ItemRiftSeed;
import thecodex6824.thaumicaugmentation.common.item.ItemSealCopier;
import thecodex6824.thaumicaugmentation.common.item.ItemTieredCasterGauntlet;
import thecodex6824.thaumicaugmentation.common.item.ItemVoidBoots;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.recipe.AugmentAdditionRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.AugmentRemovalRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.AuthorizedKeyCreationRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.DyeableItemRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.ElementChangeRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.ThaumiumKeyCopyRecipe;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneDoor;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneTrapdoor;
import thecodex6824.thaumicaugmentation.common.tile.TileCastedLight;
import thecodex6824.thaumicaugmentation.common.tile.TileVisRegenerator;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedChest;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeEmptiness;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeEmptinessHighlands;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeTaintedLands;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class RegistryHandler {

    private RegistryHandler() {}
    
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
        registry.register(setupBlock(new BlockArcaneDoor(), "arcane_door"));
        registry.register(setupBlock(new BlockCastedLight(), "temporary_light"));
        registry.register(setupBlock(new BlockTAStone(), "stone"));
        registry.register(setupBlock(new BlockArcaneTrapdoor(Material.WOOD), "arcane_trapdoor_wood"));
        registry.register(setupBlock(new BlockArcaneTrapdoor(Material.IRON), "arcane_trapdoor_metal"));
        registry.register(setupBlock(new BlockTaintFlower(), "taint_flower"));

        GameRegistry.registerTileEntity(TileVisRegenerator.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "vis_regenerator"));
        GameRegistry.registerTileEntity(TileWardedChest.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "warded_chest"));
        GameRegistry.registerTileEntity(TileArcaneDoor.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "arcane_door"));
        GameRegistry.registerTileEntity(TileCastedLight.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "temporary_light"));
        GameRegistry.registerTileEntity(TileArcaneTrapdoor.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "arcane_trapdoor"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (Block b : TABlocks.getAllBlocks()) {
            if (b instanceof IItemBlockProvider)
                registry.register(((IItemBlockProvider) b).createItemBlock().setRegistryName(b.getRegistryName()));
        }

        registry.register(setupItem(new ItemTieredCasterGauntlet(), "gauntlet"));
        registry.register(setupItem(new ItemTABase("lattice", "warding_sigil", "amalgamated_gear"), "material"));
        registry.register(setupItem(new ItemSealCopier(), "seal_copier"));
        registry.register(setupItem(new ItemArcaneDoor(), "arcane_door"));
        registry.register(setupItem(new ItemKey(), "key"));
        registry.register(setupItem(new ItemVoidBoots(), "void_boots"));
        registry.register(setupItem(new ItemRiftSeed(), "rift_seed"));
        registry.register(setupItem(new ItemElementalAugment(), "augment_caster_elemental"));
        registry.register(setupItem(new ItemRiftEnergyGauntletAugment(), "augment_caster_rift_energy_storage"));
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(new DyeableItemRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "dyeable_item")));
        event.getRegistry().register(new AuthorizedKeyCreationRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "bound_key_creation")));
        event.getRegistry().register(new ThaumiumKeyCopyRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "thaumium_key_copy")));
        event.getRegistry().register(new AugmentAdditionRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "augment_addition")));
        event.getRegistry().register(new AugmentRemovalRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "augment_removal")));
        event.getRegistry().register(new ElementChangeRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "element_swap")));
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        event.getRegistry().register(new BiomeEmptiness().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "emptiness")));
        event.getRegistry().register(new BiomeTaintedLands().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "tainted_lands")));
        event.getRegistry().register(new BiomeEmptinessHighlands().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "emptiness_highlands")));
    }
    
    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        int id = 0;
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityDimensionalFracture.class).id(
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "dimensional_fracture"), id++).name(
                        ThaumicAugmentationAPI.MODID + ".dimensional_fracture").tracker(128, 4, false).build());
    }
    
    @SubscribeEvent
    public static void registerSerializers(RegistryEvent.Register<DataSerializerEntry> event) {
        event.getRegistry().register(new DataSerializerEntry(EntityUtil.SERIALIZER_LONG).setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "long")));
    }
    
    @SubscribeEvent
    public static void registerAspects(AspectRegistryEvent event) {
        AspectEventProxy proxy = event.register;
        proxy.registerComplexObjectTag(new ItemStack(TAItems.ARCANE_DOOR, 1, 0), new AspectList().add(Aspect.PROTECT, 23));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.ARCANE_DOOR, 1, 1), new AspectList().add(Aspect.PROTECT, 19));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.GAUNTLET, 1, 0), new AspectList().add(Aspect.MAGIC, 8));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.GAUNTLET, 1, 1), new AspectList().add(Aspect.ELDRITCH, 27).add(Aspect.VOID, 23));
        proxy.registerObjectTag(new ItemStack(TAItems.KEY, 1, 0), new AspectList().add(Aspect.PROTECT, 5).add(Aspect.MIND, 10).add(Aspect.METAL, 3));
        proxy.registerObjectTag(new ItemStack(TAItems.KEY, 1, 1), new AspectList().add(Aspect.PROTECT, 5).add(Aspect.MIND, 10).add(Aspect.METAL, 3));
        proxy.registerObjectTag(new ItemStack(TAItems.KEY, 1, 2), new AspectList().add(Aspect.PROTECT, 5).add(Aspect.MIND, 10).add(Aspect.METAL, 3));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.MATERIAL, 1, 0), new AspectList().add(Aspect.AURA, 7).add(Aspect.PLANT, 6));
        proxy.registerObjectTag(new ItemStack(TAItems.MATERIAL, 1, 1), new AspectList().add(Aspect.PROTECT, 15).add(Aspect.MIND, 10));
        proxy.registerObjectTag(new ItemStack(TAItems.RIFT_SEED), new AspectList());
        proxy.registerObjectTag(new ItemStack(TAItems.SEAL_COPIER), new AspectList().add(Aspect.MIND, 15).add(Aspect.TOOL, 5));
        proxy.registerComplexObjectTag(new ItemStack(TAItems.VOID_BOOTS), new AspectList().add(Aspect.ELDRITCH, 43).add(Aspect.VOID, 23));
        
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.ARCANE_TRAPDOOR_METAL), new AspectList().add(Aspect.PROTECT, 7));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.ARCANE_TRAPDOOR_WOOD), new AspectList().add(Aspect.PROTECT, 7));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 0), new AspectList().add(Aspect.EARTH, 5).add(Aspect.VOID, 5).add(Aspect.DARKNESS, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 1), new AspectList().add(Aspect.EARTH, 3).add(Aspect.VOID, 3).add(Aspect.DARKNESS, 3).add(Aspect.FLUX, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 2), new AspectList().add(Aspect.EARTH, 3).add(Aspect.VOID, 3).add(Aspect.DARKNESS, 3).add(Aspect.FLUX, 3));
        proxy.registerObjectTag(new ItemStack(TABlocks.TAINT_FLOWER), new AspectList().add(Aspect.FLUX, 10).add(Aspect.PLANT, 5));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.VIS_REGENERATOR), new AspectList().add(Aspect.AURA, 20).add(Aspect.MECHANISM, 15).add(Aspect.ENERGY, 5));
        proxy.registerComplexObjectTag(new ItemStack(TABlocks.WARDED_CHEST), new AspectList().add(Aspect.PROTECT, 7));
    
        AspectElementInteractionManager.init();
    }

}
