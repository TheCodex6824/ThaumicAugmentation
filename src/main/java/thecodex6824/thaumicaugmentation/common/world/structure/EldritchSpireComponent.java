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

package thecodex6824.thaumicaugmentation.common.world.structure;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.Template.BlockInfo;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.entities.monster.EntityEldritchCrab;
import thaumcraft.common.entities.monster.EntityInhabitedZombie;
import thaumcraft.common.entities.monster.EntityMindSpider;
import thaumcraft.common.entities.monster.EntityWisp;
import thaumcraft.common.tiles.crafting.TilePedestal;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TALootTables;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.*;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType.LockType;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskPart.ObeliskPart;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskType.ObeliskType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.api.block.property.IUrnType.UrnType;
import thecodex6824.thaumicaugmentation.api.ward.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorageServer;
import thecodex6824.thaumicaugmentation.common.entity.EntityAutocasterEldritch;
import thecodex6824.thaumicaugmentation.common.entity.EntityFocusShield;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAEldritchGuardian;
import thecodex6824.thaumicaugmentation.common.tile.TileAltar;
import thecodex6824.thaumicaugmentation.common.tile.TileObelisk;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

public class EldritchSpireComponent extends StructureComponentTemplate {
    
    protected String name;
    protected boolean fillBlocks;
    protected UUID ward;
    
    // need default constructor for loading by minecraft
    public EldritchSpireComponent() {
        super(0);
    }
    
    public EldritchSpireComponent(TemplateManager templateManager, Template template, String templateName,
            boolean fillBelow, BlockPos position, Rotation rot, Mirror mi, UUID wardOwner) {
        
        super(0);
        if (template == null) // TODO add fallback in case user template is removed
            throw new NullPointerException("Structure template is null (removed template?): " + templateName);
        
        name = templateName;
        fillBlocks = fillBelow;
        ward = wardOwner;
        templatePosition = position;
        PlacementSettings settings = new PlacementSettings().setIgnoreEntities(true).setRotation(rot).setMirror(mi);
        setup(new EldritchSpireTemplate(template), templatePosition, settings);
    }
    
    public EldritchSpireComponent(TemplateManager templateManager, String templateName, boolean fillBelow,
            BlockPos position, Rotation rot, Mirror mi, UUID wardOwner) {
        
        this(templateManager, templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, templateName)), templateName,
                fillBelow, position, rot, mi, wardOwner);
    }
    
    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox bb) {
        placeSettings.setBoundingBox(bb);
        ((EldritchSpireTemplate) template).addBlocksToWorld(world, templatePosition,
                new TemplateProcessor(templatePosition, placeSettings), placeSettings, 18, ward);
        Map<BlockPos, String> map = template.getDataBlocks(templatePosition, placeSettings);
        for (Entry<BlockPos, String> entry : map.entrySet()) {
            String s = entry.getValue();
            handleDataMarker(s, entry.getKey(), world, random, bb);
        }

        return true;
    }
    
    public Template getTemplate() {
        return template;
    }
    
    public boolean shouldFillBlocksBelow() {
        return fillBlocks;
    }
    
    @Override
    protected void writeStructureToNBT(NBTTagCompound tag) {
        super.writeStructureToNBT(tag);
        tag.setString("tn", name);
        tag.setString("rot", placeSettings.getRotation().name());
        tag.setString("mi", placeSettings.getMirror().name());
        tag.setUniqueId("w", ward);
    }
    
    @Override
    protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager templateManager) {
        super.readStructureFromNBT(tag, templateManager);
        ward = tag.getUniqueId("w");
        name = tag.getString("tn");
        Rotation rot = Rotation.valueOf(tag.getString("rot"));
        Mirror mi = Mirror.valueOf(tag.getString("mi"));
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, name));
        PlacementSettings settings = new PlacementSettings().setIgnoreEntities(true).setRotation(rot).setMirror(mi);
        setup(new EldritchSpireTemplate(template), templatePosition, settings);
    }
    
    protected void setBlockStateClearWard(World world, BlockPos pos, IBlockState toPlace, int flags) {
        world.setBlockState(pos, toPlace, flags);
        IWardStorage storage = world.getChunk(pos).getCapability(CapabilityWardStorage.WARD_STORAGE, null);
        if (storage instanceof IWardStorageServer)
            ((IWardStorageServer) storage).clearWard(pos, world);
    }
    
    @Override
    protected void handleDataMarker(String function, BlockPos pos, World world, Random rand,
            StructureBoundingBox sbb) {
        
        if (function.startsWith("loot_")) {
            IBlockState toPlace = TABlocks.URN.getDefaultState();
            String id = function.substring(5);
            if (id.equals("2"))
                toPlace = toPlace.withProperty(IUrnType.URN_TYPE, UrnType.URN_RARE);
            else if (id.equals("1"))
                toPlace = toPlace.withProperty(IUrnType.URN_TYPE, UrnType.URN_UNCOMMON);
            else
                toPlace = toPlace.withProperty(IUrnType.URN_TYPE, UrnType.URN_COMMON);
            
            setBlockStateClearWard(world, pos, toPlace, 2);
        }
        else if (function.startsWith("pedestal_")) {
            IBlockState toPlace = null;
            String type = function.substring(9, 10);
            if (type.equals("e"))
                toPlace = BlocksTC.pedestalEldritch.getDefaultState();
            else if (type.equals("a"))
                toPlace = BlocksTC.pedestalAncient.getDefaultState();
            else
                toPlace = BlocksTC.pedestalArcane.getDefaultState();
            
            int tableNum = 0;
            type = function.substring(11);
            if (type.equals("2"))
                tableNum = 2;
            else if (type.equals("1"))
                tableNum = 1;
            
            setBlockStateClearWard(world, pos, toPlace, 2);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TilePedestal) {
                LootTable table = null;
                switch (tableNum) {
                    case 1: {
                        table = world.getLootTableManager().getLootTableFromLocation(
                                TALootTables.PEDESTAL_UNCOMMON);
                        break;
                    }
                    case 2: {
                        table = world.getLootTableManager().getLootTableFromLocation(
                                TALootTables.PEDESTAL_RARE);
                        break;
                    }
                    default: {
                        table = world.getLootTableManager().getLootTableFromLocation(
                                TALootTables.PEDESTAL_COMMON);
                        break;
                    }
                }
                
                LootContext context = new LootContext.Builder((WorldServer) world).build();
                // imagine using IInventory in 2020
                ((TilePedestal) tile).setInventorySlotContents(0, table.generateLootForPools(rand, context).get(0));
            }
        }
        else if (function.startsWith("autocaster")) {
            if (!function.equals("autocaster_random") || rand.nextBoolean()) {
                EntityAutocasterEldritch autocaster = new EntityAutocasterEldritch(world);
                autocaster.setPosition(Math.floor(pos.getX()) + 0.5, Math.floor(pos.getY()),
                        Math.floor(pos.getZ()) + 0.5);
                for (EnumFacing facing : EnumFacing.VALUES) {
                    BlockPos checkPos = pos.offset(facing.getOpposite());
                    if (world.getBlockState(checkPos).isSideSolid(world, checkPos, facing)) {
                        autocaster.setFacing(facing);
                        break;
                    }
                }
                
                if (!MinecraftForge.EVENT_BUS.post(new LivingSpawnEvent(autocaster, world, (float) autocaster.posX,
                        (float) autocaster.posY, (float) autocaster.posZ))) {
                    
                    autocaster.onInitialSpawn(world.getDifficultyForLocation(pos), null);
                    world.spawnEntity(autocaster);
                }
            }
        }
        else if (function.equals("eg")) {
            EntityTAEldritchGuardian entity = new EntityTAEldritchGuardian(world);
            entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rand.nextInt(360), 0);
            entity.enablePersistence();
            if (!MinecraftForge.EVENT_BUS.post(new LivingSpawnEvent(entity, world, (float) entity.posX,
                    (float) entity.posY, (float) entity.posZ))) {
                
                entity.onInitialSpawn(world.getDifficultyForLocation(pos), null);
                world.spawnEntity(entity);
            }
        }
        else if (function.equals("inz")) {
            EntityInhabitedZombie entity = new EntityInhabitedZombie(world);
            entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rand.nextInt(360), 0);
            entity.enablePersistence();
            if (!MinecraftForge.EVENT_BUS.post(new LivingSpawnEvent(entity, world, (float) entity.posX,
                    (float) entity.posY, (float) entity.posZ))) {
                
                entity.onInitialSpawn(world.getDifficultyForLocation(pos), null);
                entity.setChild(false);
                world.spawnEntity(entity);
            }
        }
        else if (function.equals("crab")) {
            EntityEldritchCrab entity = new EntityEldritchCrab(world);
            entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rand.nextInt(360), 0);
            entity.enablePersistence();
            if (!MinecraftForge.EVENT_BUS.post(new LivingSpawnEvent(entity, world, (float) entity.posX,
                    (float) entity.posY, (float) entity.posZ))) {
                
                entity.onInitialSpawn(world.getDifficultyForLocation(pos), null);
                world.spawnEntity(entity);
            }
        }
        else if (function.equals("vent")) {
            boolean dirFound = false;
            IBlockState vent = TABlocks.CRAB_VENT.getDefaultState();
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos checkPos = pos.offset(facing.getOpposite());
                if (world.getBlockState(checkPos).isSideSolid(world, checkPos, facing)) {
                    vent = vent.withProperty(IDirectionalBlock.DIRECTION, facing);
                    dirFound = true;
                    break;
                }
            }
            
            // side we need not generated yet, so force one for now
            // yes this solution is not great but it's better than floating vents
            if (!dirFound) {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    BlockPos checkPos = pos.offset(facing.getOpposite());
                    if (!sbb.isVecInside(checkPos)) {
                        setBlockStateClearWard(world, checkPos,
                                TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_CRUSTED), 2);
                        vent = vent.withProperty(IDirectionalBlock.DIRECTION, facing);
                        break;
                    }
                }
            }
            
            setBlockStateClearWard(world, pos, vent, 2);
        }
        else if (function.startsWith("obelisk_")) {
            String type = function.substring(8);
            ObeliskType oType = null;
            if (type.equals("e"))
                oType = ObeliskType.ELDRITCH;
            else
                oType = ObeliskType.ANCIENT;
        
            setBlockStateClearWard(world, pos, TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.CAP).withProperty(IObeliskType.OBELISK_TYPE, oType), 2);
            setBlockStateClearWard(world, pos.up(), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.INNER).withProperty(IObeliskType.OBELISK_TYPE, oType), 2);
            setBlockStateClearWard(world, pos.up(2), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.MIDDLE).withProperty(IObeliskType.OBELISK_TYPE, oType), 2);
            setBlockStateClearWard(world, pos.up(3), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.INNER).withProperty(IObeliskType.OBELISK_TYPE, oType), 2);
            setBlockStateClearWard(world, pos.up(4), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.CAP).withProperty(IObeliskType.OBELISK_TYPE, oType), 2);
        
            TileEntity tile = world.getTileEntity(pos.up(2));
            if (tile instanceof TileObelisk)
                ((TileObelisk) tile).setBoundWard(ward);
        }
        else if (function.startsWith("lock_front_")) {
            EnumFacing face = null;
            String dir = function.substring(11, 12);
            if (dir.equals("e"))
                face = placeSettings.getRotation().rotate(placeSettings.getMirror().mirror(EnumFacing.EAST));
            else if (dir.equals("s"))
                face = placeSettings.getRotation().rotate(placeSettings.getMirror().mirror(EnumFacing.SOUTH));
            else if (dir.equals("w"))
                face = placeSettings.getRotation().rotate(placeSettings.getMirror().mirror(EnumFacing.WEST));
            else
                face = placeSettings.getRotation().rotate(placeSettings.getMirror().mirror(EnumFacing.NORTH));
            
            IBlockState state = TABlocks.ELDRITCH_LOCK_IMPETUS.getDefaultState();
            state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, face);
            setBlockStateClearWard(world, pos, state, 2);
        }
        else if (function.startsWith("lock_")) {
            EnumFacing face = null;
            String type = function.substring(5, 6);
            if (type.equals("e"))
                face = placeSettings.getRotation().rotate(placeSettings.getMirror().mirror(EnumFacing.EAST));
            else if (type.equals("s"))
                face = placeSettings.getRotation().rotate(placeSettings.getMirror().mirror(EnumFacing.SOUTH));
            else if (type.equals("w"))
                face = placeSettings.getRotation().rotate(placeSettings.getMirror().mirror(EnumFacing.WEST));
            else
                face = placeSettings.getRotation().rotate(placeSettings.getMirror().mirror(EnumFacing.NORTH));
            
            LockType lock = null;
            type = function.substring(7);
            if (type.equals("maze"))
                lock = LockType.LABYRINTH;
            else if (type.equals("prison"))
                lock = LockType.PRISON;
            else if (type.equals("library"))
                lock = LockType.LIBRARY;
            else
                lock = LockType.BOSS;
            
            IBlockState state = TABlocks.ELDRITCH_LOCK.getDefaultState();
            state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, face);
            state = state.withProperty(IEldritchLockType.LOCK_TYPE, lock);
            setBlockStateClearWard(world, pos, state, 2);
        }
        else if (function.startsWith("key_")) {
            IBlockState toPlace = null;
            String type = function.substring(4, 5);
            if (type.equals("e"))
                toPlace = BlocksTC.pedestalEldritch.getDefaultState();
            else if (type.equals("a"))
                toPlace = BlocksTC.pedestalAncient.getDefaultState();
            else
                toPlace = BlocksTC.pedestalArcane.getDefaultState();
            
            LockType lock = null;
            type = function.substring(6);
            if (type.equals("maze"))
                lock = LockType.LABYRINTH;
            else if (type.equals("prison"))
                lock = LockType.PRISON;
            else if (type.equals("library"))
                lock = LockType.LIBRARY;
            else
                lock = LockType.BOSS;
            
            setBlockStateClearWard(world, pos, toPlace, 2);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TilePedestal)
                ((TilePedestal) tile).setInventorySlotContents(0, lock.getKey());
        }
        else if (function.startsWith("spawner_")) {
            String type = function.substring(8);
            ResourceLocation spawn = null;
            if (type.equals("mspider"))
                spawn = EntityRegistry.getEntry(EntityMindSpider.class).getRegistryName();
            else if (type.equals("crab"))
                spawn = EntityRegistry.getEntry(EntityEldritchCrab.class).getRegistryName();
            else
                spawn = EntityRegistry.getEntry(EntityWisp.class).getRegistryName();
            
            setBlockStateClearWard(world, pos, Blocks.MOB_SPAWNER.getDefaultState(), 2);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityMobSpawner) {
                MobSpawnerBaseLogic logic = ((TileEntityMobSpawner) tile).getSpawnerBaseLogic();
                logic.potentialSpawns.clear();
                logic.setEntityId(spawn);
            }
        }
        else if (function.startsWith("altar_")) {
            String type = function.substring(6);
            IBlockState toPlace = TABlocks.CAPSTONE.getDefaultState().withProperty(IAltarBlock.ALTAR, true);
            ObeliskType obeliskType = null;
            if (type.equals("e"))
                obeliskType = ObeliskType.ELDRITCH;
            else
                obeliskType = ObeliskType.ANCIENT;
            
            setBlockStateClearWard(world, pos, toPlace.withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
            setBlockStateClearWard(world, pos.up(2), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.CAP).withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
            setBlockStateClearWard(world, pos.up(3), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.INNER).withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
            setBlockStateClearWard(world, pos.up(4), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.MIDDLE).withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
            setBlockStateClearWard(world, pos.up(5), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.INNER).withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
            setBlockStateClearWard(world, pos.up(6), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.CAP).withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
        
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileAltar)
                ((TileAltar) tile).setStructureAltar(true);
        }
        else if (function.equals("eg_mb")) {
            EntityTAEldritchGuardian entity = new EntityTAEldritchGuardian(world);
            entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rand.nextInt(360), 0);
            entity.enablePersistence();
            entity.setCustomNameTag(new TextComponentTranslation("thaumicaugmentation.text.entity.eldritch_guardian_mb").getFormattedText());
            if (!MinecraftForge.EVENT_BUS.post(new LivingSpawnEvent(entity, world, (float) entity.posX,
                    (float) entity.posY, (float) entity.posZ))) {
                
                entity.onInitialSpawn(world.getDifficultyForLocation(pos), null);
                entity.setAbsorptionAmount(entity.getAbsorptionAmount() * 2);
                if (world.spawnEntity(entity)) {
                    EntityFocusShield shield = new EntityFocusShield(world);
                    shield.setOwner(entity);
                    shield.setCasterID(entity.getPersistentID());
                    shield.setColor(0x606060);
                    shield.setInfiniteLifespan();
                    if (world.getDifficulty() == EnumDifficulty.HARD) {
                        shield.setMaxHealth(100.0F, false);
                        shield.setReflect(true);
                    }
                    else if (world.getDifficulty() == EnumDifficulty.NORMAL)
                        shield.setMaxHealth(50.0F, false);
                    else
                        shield.setMaxHealth(20.0F, false);
                    
                    shield.setHealth(shield.getMaxHealth());
                    world.spawnEntity(shield);
                }
            }
        }
    }
    
    public void onPostGeneration(World world, StructureBoundingBox structurebb) {}
    
    public static class TemplateProcessor implements IAdvancedTemplateProcessor {
        
        protected float chance;
        protected Random rand;
        
        public TemplateProcessor(BlockPos pos, PlacementSettings settings) {
            chance = settings.getIntegrity();
            rand = settings.getRandom(pos);
        }
        
        @Override
        @Nullable
        public BlockInfo processBlock(World world, BlockPos pos, BlockInfo blockInfo) {
            return chance < 1.0F && rand.nextFloat() > chance ? null : blockInfo;
        }
        
        protected boolean isStateWardable(IBlockState state) {
            if (state.getBlock() == TABlocks.STONE) {
                StoneType stone = state.getValue(ITAStoneType.STONE_TYPE);
                return stone != StoneType.STONE_CRUSTED && stone != StoneType.STONE_CRUSTED_GLOWING;
            }
            else if (state.getBlock() == BlocksTC.stonePorous)
                return false;
            else if (state.getBlock() == TABlocks.STRANGE_CRYSTAL)
                return false;
            else if (state.getBlock() == Blocks.WEB)
                return false;
            else return !state.getMaterial().isLiquid();
        }
        
        @Override
        public boolean shouldBlockBeWarded(World world, BlockPos pos, BlockInfo blockInfo) {
            return blockInfo.tileentityData == null && isStateWardable(blockInfo.blockState);
        }
        
    }
    
}
