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

import java.util.List;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.MapGenStructureIO;
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
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.entities.monster.EntityEldritchCrab;
import thaumcraft.common.entities.monster.EntityInhabitedZombie;
import thaumcraft.common.tiles.crafting.TilePedestal;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TALootTables;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType.LockType;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.common.entity.EntityAutocasterEldritch;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAEldritchGuardian;

public class EldritchSpireComponents {

    public static void register() {
        MapGenStructureIO.registerStructureComponent(EldritchSpireTemplate.class, "est");
    }
    
    public static void generate(TemplateManager templateManager, BlockPos position,
            Rotation rot, Random random, List<EldritchSpireTemplate> pieces) {
        
        Mirror mirror = Mirror.values()[random.nextInt(Mirror.values().length)];
        MutableBlockPos current = new MutableBlockPos(position);
        BlockPos baseSize = generateBase(templateManager, current, rot, mirror, random, pieces);
        generateGroundFloor(templateManager, current, baseSize, rot, mirror, random, pieces);
        generateFirstFloor(templateManager, current, baseSize, rot, mirror, random, pieces);
        generateSecondFloor(templateManager, current, baseSize, rot, mirror, random, pieces);
        generateThirdFloor(templateManager, current, baseSize, rot, mirror, random, pieces);
        generateFourthFloor(templateManager, current, baseSize, rot, mirror, random, pieces);
    }
    
    protected static BlockPos transformOffset(int x, int z, Rotation rot, Mirror mi) {
        return new BlockPos(mi == Mirror.FRONT_BACK ? -x : x, 0,
                mi == Mirror.LEFT_RIGHT ? -z : z).rotate(rot);
    }
    
    protected static Rotation fromFacing(EnumFacing face, Mirror mi) {
        switch (face) {
            case WEST: return mi != Mirror.NONE ? Rotation.COUNTERCLOCKWISE_90 :
                Rotation.CLOCKWISE_90;
            case NORTH: return Rotation.CLOCKWISE_180;
            case EAST: return mi != Mirror.NONE ? Rotation.CLOCKWISE_90 :
                Rotation.COUNTERCLOCKWISE_90;
            default: return Rotation.NONE;
        }
    }
    
    protected static String pickBase(Random rand) {
        return "base";
    }
    
    protected static String pickStairs(Random rand) {
        return "stairs_" + rand.nextInt(4);
    }
    
    protected static String pickGroundFloor(Random rand) {
        return "ground_floor";
    }
    
    protected static String pickFirstFloor(Random rand) {
        return "first_floor";
    }
    
    protected static String pickSecondFloor(Random rand) {
        return "second_floor";
    }
    
    protected static String pickThirdFloor(Random rand) {
        return "third_floor";
    }
    
    protected static String pickFourthFloor(Random rand) {
        return "fourth_floor";
    }
    
    public static BlockPos generateBase(TemplateManager templateManager, MutableBlockPos current,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireTemplate> pieces) {
        
        BlockPos original = current.toImmutable();
        EldritchSpireTemplate base = new EldritchSpireTemplate(templateManager, pickBase(random),
                true, original, rot, mirror);
        pieces.add(base);
        BlockPos offset = current.add(transformOffset(base.getTemplate().getSize().getX() / 2,
                base.getTemplate().getSize().getZ() / 2, rot, mirror));
        current.setPos(offset);
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            String tName = pickStairs(random);
            Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + tName));
            BlockPos add = new BlockPos(transformOffset((base.getTemplate().getSize().getX() / 2 + 1) * face.getXOffset(),
                    (base.getTemplate().getSize().getZ() / 2 + 1) * face.getZOffset(), rot, mirror));
            current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
            
            // always use the x size because that's what matters when rotated
            int sizeX = template.getSize().getX();
            add = transformOffset(-sizeX / 2 * face.getZOffset(), sizeX / 2 * face.getXOffset(), rot, mirror);
            current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
            pieces.add(new EldritchSpireTemplate(templateManager, template, tName, true,
                    current.toImmutable(), rot.add(fromFacing(face, mirror)), mirror));
            current.setPos(offset);
        }
        
        current.setPos(original.up(base.getTemplate().getSize().getY()));
        return base.getTemplate().getSize();
    }
    
    public static void generateGroundFloor(TemplateManager templateManager, MutableBlockPos current,
            BlockPos baseSize, Rotation rot, Mirror mirror, Random random, List<EldritchSpireTemplate> pieces) {
        
        String tName = pickGroundFloor(random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + tName));
        BlockPos original = current.toImmutable();
        BlockPos add = transformOffset((baseSize.getX() - template.getSize().getX()) / 2,
                (baseSize.getZ() - template.getSize().getZ()) / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        EldritchSpireTemplate floor = new EldritchSpireTemplate(templateManager, template, tName,
                true, current.toImmutable(), rot, mirror);
        pieces.add(floor);
        
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static void generateFirstFloor(TemplateManager templateManager, MutableBlockPos current,
            BlockPos baseSize, Rotation rot, Mirror mirror, Random random, List<EldritchSpireTemplate> pieces) {
        
        String tName = pickFirstFloor(random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + tName));
        BlockPos original = current.toImmutable();
        BlockPos add = transformOffset((baseSize.getX() - template.getSize().getX()) / 2,
                (baseSize.getZ() - template.getSize().getZ()) / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        EldritchSpireTemplate floor = new EldritchSpireTemplate(templateManager, template, tName,
                true, current.toImmutable(), rot, mirror);
        pieces.add(floor);
        
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static void generateSecondFloor(TemplateManager templateManager, MutableBlockPos current,
            BlockPos baseSize, Rotation rot, Mirror mirror, Random random, List<EldritchSpireTemplate> pieces) {
        
        String tName = pickSecondFloor(random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + tName));
        BlockPos original = current.toImmutable();
        BlockPos add = transformOffset((baseSize.getX() - template.getSize().getX()) / 2,
                (baseSize.getZ() - template.getSize().getZ()) / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        EldritchSpireTemplate floor = new EldritchSpireTemplate(templateManager, template, tName,
                true, current.toImmutable(), rot, mirror);
        pieces.add(floor);
        
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static void generateThirdFloor(TemplateManager templateManager, MutableBlockPos current,
            BlockPos baseSize, Rotation rot, Mirror mirror, Random random, List<EldritchSpireTemplate> pieces) {
        
        String tName = pickThirdFloor(random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + tName));
        BlockPos original = current.toImmutable();
        BlockPos add = transformOffset((baseSize.getX() - template.getSize().getX()) / 2,
                (baseSize.getZ() - template.getSize().getZ()) / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        EldritchSpireTemplate floor = new EldritchSpireTemplate(templateManager, template, tName,
                true, current.toImmutable(), rot, mirror);
        pieces.add(floor);
        
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static void generateFourthFloor(TemplateManager templateManager, MutableBlockPos current,
            BlockPos baseSize, Rotation rot, Mirror mirror, Random random, List<EldritchSpireTemplate> pieces) {
        
        String tName = pickFourthFloor(random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + tName));
        BlockPos original = current.toImmutable();
        BlockPos add = transformOffset((baseSize.getX() - template.getSize().getX()) / 2,
                (baseSize.getZ() - template.getSize().getZ()) / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        EldritchSpireTemplate floor = new EldritchSpireTemplate(templateManager, template, tName,
                true, current.toImmutable(), rot, mirror);
        pieces.add(floor);
        
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static class EldritchSpireTemplate extends StructureComponentTemplate {
        
        protected String name;
        protected boolean fillBlocks;
        
        // need default constructor for loading by minecraft
        public EldritchSpireTemplate() {
            super(0);
        }
        
        public EldritchSpireTemplate(TemplateManager templateManager, Template template, String templateName,
                boolean fillBelow, BlockPos position, Rotation rot, Mirror mi) {
            
            super(0);
            if (template == null)
                throw new NullPointerException("Structure template is null (should be impossible)");
            
            name = templateName;
            fillBlocks = fillBelow;
            templatePosition = position;
            PlacementSettings settings = new PlacementSettings().setIgnoreEntities(true).setRotation(rot).setMirror(mi);
            setup(template, templatePosition, settings);
        }
        
        public EldritchSpireTemplate(TemplateManager templateManager, String templateName, boolean fillBelow,
                BlockPos position, Rotation rot, Mirror mi) {
            
            this(templateManager, templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + templateName)), templateName,
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
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + name));
            PlacementSettings settings = new PlacementSettings().setIgnoreEntities(true).setRotation(rot).setMirror(mi);
            setup(template, templatePosition, settings);
        }
        
        @Override
        protected void handleDataMarker(String function, BlockPos pos, World world, Random rand,
                StructureBoundingBox sbb) {
            
            if (function.startsWith("loot_")) {
                IBlockState toPlace = null;
                String id = function.substring(5);
                if (id.equals("2"))
                    toPlace = BlocksTC.lootUrnRare.getDefaultState();
                else if (id.equals("1"))
                    toPlace = BlocksTC.lootUrnUncommon.getDefaultState();
                else
                    toPlace = BlocksTC.lootUrnCommon.getDefaultState();
                
                world.setBlockState(pos, toPlace, 2);
            }
            else if (function.startsWith("pedestal_")) {
                IBlockState toPlace = null;
                String type = function.substring(9, 1);
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
            else if (function.startsWith("lock_front_")) {
                EnumFacing face = null;
                String dir = function.substring(11, 12);
                if (dir.equals("e"))
                    face = placeSettings.getRotation().rotate(EnumFacing.EAST);
                else if (dir.equals("s"))
                    face = placeSettings.getRotation().rotate(EnumFacing.SOUTH);
                else if (dir.equals("w"))
                    face = placeSettings.getRotation().rotate(EnumFacing.WEST);
                else
                    face = placeSettings.getRotation().rotate(EnumFacing.NORTH);
                
                IBlockState state = TABlocks.ELDRITCH_LOCK_IMPETUS.getDefaultState();
                state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, face);
                world.setBlockState(pos, state, 2);
            }
            else if (function.startsWith("lock_")) {
                EnumFacing face = null;
                String type = function.substring(5, 6);
                if (type.equals("e"))
                    face = placeSettings.getRotation().rotate(EnumFacing.EAST);
                else if (type.equals("s"))
                    face = placeSettings.getRotation().rotate(EnumFacing.SOUTH);
                else if (type.equals("w"))
                    face = placeSettings.getRotation().rotate(EnumFacing.WEST);
                else
                    face = placeSettings.getRotation().rotate(EnumFacing.NORTH);
                
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
                String type = function.substring(4, 1);
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
        }
        
    }
    
}
