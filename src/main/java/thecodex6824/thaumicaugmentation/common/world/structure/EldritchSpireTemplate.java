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

package thecodex6824.thaumicaugmentation.common.world.structure;

import java.util.Random;

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
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
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
import thecodex6824.thaumicaugmentation.api.block.property.IAltarBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType.LockType;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskPart;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskPart.ObeliskPart;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskType;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskType.ObeliskType;
import thecodex6824.thaumicaugmentation.api.block.property.IUrnType;
import thecodex6824.thaumicaugmentation.api.block.property.IUrnType.UrnType;
import thecodex6824.thaumicaugmentation.common.entity.EntityAutocasterEldritch;
import thecodex6824.thaumicaugmentation.common.entity.EntityFocusShield;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAEldritchGuardian;

public class EldritchSpireTemplate extends StructureComponentTemplate {
    
    protected String name;
    protected boolean fillBlocks;
    
    // need default constructor for loading by minecraft
    public EldritchSpireTemplate() {
        super(0);
    }
    
    public EldritchSpireTemplate(TemplateManager templateManager, Template template, String templateName,
            boolean fillBelow, BlockPos position, Rotation rot, Mirror mi) {
        
        super(0);
        if (template == null) // TODO add fallback in case user template is removed
            throw new NullPointerException("Structure template is null (removed template?): " + templateName);
        
        name = templateName;
        fillBlocks = fillBelow;
        templatePosition = position;
        PlacementSettings settings = new PlacementSettings().setIgnoreEntities(true).setRotation(rot).setMirror(mi);
        setup(template, templatePosition, settings);
    }
    
    public EldritchSpireTemplate(TemplateManager templateManager, String templateName, boolean fillBelow,
            BlockPos position, Rotation rot, Mirror mi) {
        
        this(templateManager, templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, templateName)), templateName,
                fillBelow, position, rot, mi);
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
    }
    
    @Override
    protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager templateManager) {
        super.readStructureFromNBT(tag, templateManager);
        name = tag.getString("tn");
        Rotation rot = Rotation.valueOf(tag.getString("rot"));
        Mirror mi = Mirror.valueOf(tag.getString("mi"));
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, name));
        PlacementSettings settings = new PlacementSettings().setIgnoreEntities(true).setRotation(rot).setMirror(mi);
        setup(template, templatePosition, settings);
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
            
            world.setBlockState(pos, toPlace, 2);
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
            
            world.setBlockState(pos, toPlace, 2);
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
            IBlockState vent = TABlocks.CRAB_VENT.getDefaultState();
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos checkPos = pos.offset(facing.getOpposite());
                if (world.getBlockState(checkPos).isSideSolid(world, checkPos, facing)) {
                    vent = vent.withProperty(IDirectionalBlock.DIRECTION, facing);
                    break;
                }
            }
            
            world.setBlockState(pos, vent, 2);
        }
        else if (function.startsWith("obelisk_")) {
            String type = function.substring(8);
            ObeliskType oType = null;
            if (type.equals("e"))
                oType = ObeliskType.ELDRITCH;
            else
                oType = ObeliskType.ANCIENT;
        
            world.setBlockState(pos, TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.CAP).withProperty(IObeliskType.OBELISK_TYPE, oType), 2);
            world.setBlockState(pos.up(), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.INNER).withProperty(IObeliskType.OBELISK_TYPE, oType), 2);
            world.setBlockState(pos.up(2), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.MIDDLE).withProperty(IObeliskType.OBELISK_TYPE, oType), 2);
            world.setBlockState(pos.up(3), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.INNER).withProperty(IObeliskType.OBELISK_TYPE, oType), 2);
            world.setBlockState(pos.up(4), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.CAP).withProperty(IObeliskType.OBELISK_TYPE, oType), 2);
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
            world.setBlockState(pos, state, 2);
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
            world.setBlockState(pos, state, 2);
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
            
            world.setBlockState(pos, toPlace, 2);
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
            
            world.setBlockState(pos, Blocks.MOB_SPAWNER.getDefaultState(), 2);
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
            
            world.setBlockState(pos, toPlace.withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
            world.setBlockState(pos.up(2), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.CAP).withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
            world.setBlockState(pos.up(3), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.INNER).withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
            world.setBlockState(pos.up(4), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.MIDDLE).withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
            world.setBlockState(pos.up(5), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.INNER).withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
            world.setBlockState(pos.up(6), TABlocks.OBELISK.getDefaultState().withProperty(
                    IObeliskPart.OBELISK_PART, ObeliskPart.CAP).withProperty(IObeliskType.OBELISK_TYPE, obeliskType), 2);
        }
        else if (function.equals("eg_mb")) {
            EntityTAEldritchGuardian entity = new EntityTAEldritchGuardian(world);
            entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rand.nextInt(360), 0);
            entity.enablePersistence();
            if (!MinecraftForge.EVENT_BUS.post(new LivingSpawnEvent(entity, world, (float) entity.posX,
                    (float) entity.posY, (float) entity.posZ))) {
                
                entity.onInitialSpawn(world.getDifficultyForLocation(pos), null);
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
    
}
