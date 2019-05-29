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
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.block.BlockArcaneDoor;
import thecodex6824.thaumicaugmentation.common.block.BlockArcaneTrapdoor;
import thecodex6824.thaumicaugmentation.common.block.BlockCastedLight;
import thecodex6824.thaumicaugmentation.common.block.BlockDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.block.BlockTAStone;
import thecodex6824.thaumicaugmentation.common.block.BlockTaintFlower;
import thecodex6824.thaumicaugmentation.common.block.BlockVisRegenerator;
import thecodex6824.thaumicaugmentation.common.block.BlockWardedChest;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.item.ItemArcaneDoor;
import thecodex6824.thaumicaugmentation.common.item.ItemKey;
import thecodex6824.thaumicaugmentation.common.item.ItemRiftSeed;
import thecodex6824.thaumicaugmentation.common.item.ItemSealCopier;
import thecodex6824.thaumicaugmentation.common.item.ItemTieredCasterGauntlet;
import thecodex6824.thaumicaugmentation.common.item.ItemVoidBoots;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.recipe.AuthorizedKeyCreationRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.DyeableItemRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.ThaumiumKeyCopyRecipe;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneDoor;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneTrapdoor;
import thecodex6824.thaumicaugmentation.common.tile.TileCastedLight;
import thecodex6824.thaumicaugmentation.common.tile.TileDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.tile.TileVisRegenerator;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedChest;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeEmptiness;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeTaintedLands;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class RegistryHandler {

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
        registry.register(setupBlock(new BlockDimensionalFracture(), "dimensional_fracture"));
        registry.register(setupBlock(new BlockArcaneTrapdoor(), "arcane_trapdoor_wood"));
        registry.register(setupBlock(new BlockArcaneTrapdoor(), "arcane_trapdoor_metal"));
        registry.register(setupBlock(new BlockTaintFlower(), "taint_flower"));

        GameRegistry.registerTileEntity(TileVisRegenerator.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "vis_regenerator"));
        GameRegistry.registerTileEntity(TileWardedChest.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "warded_chest"));
        GameRegistry.registerTileEntity(TileArcaneDoor.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "arcane_door"));
        GameRegistry.registerTileEntity(TileCastedLight.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "temporary_light"));
        GameRegistry.registerTileEntity(TileDimensionalFracture.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "dimensional_fracture"));
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
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(new DyeableItemRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "dyeable_item")));
        event.getRegistry().register(new AuthorizedKeyCreationRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "bound_key_creation")));
        event.getRegistry().register(new ThaumiumKeyCopyRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "thaumium_key_copy")));
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        event.getRegistry().register(new BiomeEmptiness().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "emptiness")));
        event.getRegistry().register(new BiomeTaintedLands().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "tainted_lands")));
    }

}
