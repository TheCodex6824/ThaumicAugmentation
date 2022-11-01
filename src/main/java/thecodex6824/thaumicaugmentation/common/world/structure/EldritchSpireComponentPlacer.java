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

package thecodex6824.thaumicaugmentation.common.world.structure;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.util.maze.Maze;
import thecodex6824.thaumicaugmentation.common.util.maze.MazeCell;
import thecodex6824.thaumicaugmentation.common.util.maze.MazeGenerator;
import thecodex6824.thaumicaugmentation.common.world.ITAChunkGenerator;
import thecodex6824.thaumicaugmentation.common.world.structure.EldritchSpirePillarComponent.PillarType;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EldritchSpireComponentPlacer {

    public static void register() {
        MapGenStructureIO.registerStructureComponent(EldritchSpireComponent.class, "est");
        MapGenStructureIO.registerStructureComponent(EldritchSpireBaseComponent.class, "estb");
        MapGenStructureIO.registerStructureComponent(EldritchSpireMazeComponent.class, "estm");
        MapGenStructureIO.registerStructureComponent(EldritchSpirePillarComponent.class, "estp");
    }
    
    public static void generate(World world, ITAChunkGenerator generator, TemplateManager templateManager, BlockPos position,
            Rotation rot, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        Mirror mirror = Mirror.values()[random.nextInt(Mirror.values().length)];
        MutableBlockPos current = new MutableBlockPos(position);
        BlockPos baseSize = generateBase(world, generator, templateManager, current, rot, mirror, random, pieces, ward);
        generateGroundFloor(world, generator, templateManager, current, baseSize, rot, mirror, random, pieces, ward);
        generateFirstFloor(world, generator, templateManager, current, baseSize, rot, mirror, random, pieces, ward);
        generateSecondFloor(world, generator, templateManager, current, baseSize, rot, mirror, random, pieces, ward);
        generateThirdFloor(world, generator, templateManager, current, baseSize, rot, mirror, random, pieces, ward);
        generateFourthFloor(world, generator, templateManager, current, baseSize, rot, mirror, random, pieces, ward);
    }
    
    protected static BlockPos transformOffset(int x, int z, Rotation rot, Mirror mi) {
        return new BlockPos(mi == Mirror.FRONT_BACK ? -x : x, 0,
                mi == Mirror.LEFT_RIGHT ? -z : z).rotate(rot);
    }
    
    protected static Rotation fromFacing(EnumFacing face) {
        switch (face) {
            case WEST: return Rotation.CLOCKWISE_90;
            case NORTH: return Rotation.CLOCKWISE_180;
            case EAST: return Rotation.COUNTERCLOCKWISE_90;
            default: return Rotation.NONE;
        }
    }
    
    protected static String pickTemplate(String type, Random rand) {
        int c = templateCounts.get(type);
        if (c == 1)
            return type + "0";
        else
            return type + rand.nextInt(c);
    }
    
    protected static String getCellTypeHall(MazeCell cell) {
        switch (cell.getNumWalls()) {
            case 0: return MAZE_CELL_HALL_CROSS;
            case 1: return MAZE_CELL_HALL_T;
            case 2: {
                if (cell.isCorner())
                    return MAZE_CELL_HALL_CORNER;
                else
                    return MAZE_CELL_HALL_STRAIGHT;
            }
            case 3: return MAZE_CELL_HALL_END;
            default: return "spire/maze/cell_closed"; // should never appear
        }
    }
    
    protected static String getCellTypeNormalRoom(MazeCell cell) {
        switch (cell.getNumWalls()) {
            case 0: return MAZE_CELL_ROOM_CROSS;
            case 1: return MAZE_CELL_ROOM_T;
            case 2: {
                if (cell.isCorner())
                    return MAZE_CELL_ROOM_CORNER;
                else
                    return MAZE_CELL_ROOM_STRAIGHT;
            }
            case 3: return MAZE_CELL_ROOM_END;
            default: return "spire/maze/cell_closed";
        }
    }
    
    protected static String getCellTypeCrabRoom(MazeCell cell) {
        if (cell.getNumWalls() == 3)
            return MAZE_CELL_ROOM_END_CRAB;
        else
            return "spire/maze/cell_closed";
    }
    
    protected static String getCellTypeEntrance(MazeCell cell) {
        switch (cell.getNumWalls()) {
            case 0: return MAZE_CELL_ENTRANCE_CROSS;
            case 1: return MAZE_CELL_ENTRANCE_T;
            case 2: {
                if (cell.isCorner())
                    return MAZE_CELL_ENTRANCE_CORNER;
                else
                    return MAZE_CELL_ENTRANCE_STRAIGHT;
            }
            case 3: return MAZE_CELL_ENTRANCE_END;
            default: return "spire/maze/cell_closed";
        }
    }
    
    // ChunkPrimer's version seems to be wrong and adds an extra 256 to the index
    // This gave me IndexOutOfBounds exceptions since I actually pass
    // things other than (7, 7) to it
    private static int findGroundBlockInPrimer(ChunkPrimer primer, int x, int z) {
        for (int y = 255; y >= 0; --y) {
            IBlockState state = primer.getBlockState(x, y, z);
            if (state.getMaterial() != Material.AIR)
                return y;
        }

        return 0;
    }
    
    public static void generateGenericPillar(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, Rotation rot,
            Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        BlockPos backup = current.toImmutable();
        String name = pickTemplate(PILLAR, random);
        Template sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, name));
        BlockPos add = transformOffset(-sTemplate.getSize().getX() / 2 - 1, -sTemplate.getSize().getZ() / 2 - 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, sTemplate, name, false,
                current.toImmutable(), rot, mirror, ward));
        
        name = pickTemplate(PILLAR_BASE, random);
        sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, name));
        add = transformOffset(-sTemplate.getSize().getX() / 2 - 1, -sTemplate.getSize().getZ() / 2 - 1, rot, mirror);
        current.setPos(backup.getX() + add.getX(), backup.getY(), backup.getZ() + add.getZ());
        ChunkPrimer primer = new ChunkPrimer();
        generator.populatePrimerWithHeightmap(current.getX() >> 4, current.getZ() >> 4, primer);
        current.setY(Math.max(findGroundBlockInPrimer(primer, current.getX() & 15, current.getZ() & 15), 1));
        pieces.add(new EldritchSpirePillarComponent(templateManager, sTemplate, name, false,
                current.toImmutable(), rot, mirror, ward, PillarType.GENERIC));
        
        current.setPos(backup);
    }
    
    public static BlockPos generateBase(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        BlockPos original = current.toImmutable();
        EldritchSpireComponent base = new EldritchSpireBaseComponent(templateManager, pickTemplate(BASE, random),
                true, original, rot, mirror, ward);
        pieces.add(base);
        BlockPos offset = current.add(transformOffset(base.getTemplate().getSize().getX() / 2,
                base.getTemplate().getSize().getZ() / 2, rot, mirror));
        current.setPos(offset);
        for (EnumFacing f : EnumFacing.HORIZONTALS) {
            EnumFacing face = rot.rotate(mirror.mirror(f));
            String tName = pickTemplate(STAIR, random);
            Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
            BlockPos add = transformOffset(base.getTemplate().getSize().getX() / 2 + 1,
                    base.getTemplate().getSize().getZ() / 2 + 1, rot.add(fromFacing(face)), mirror);
            current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
            
            add = transformOffset((-template.getSize().getX() - base.getTemplate().getSize().getX()) / 2, 0, rot.add(fromFacing(face)), mirror);
            current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
            pieces.add(new EldritchSpireBaseComponent(templateManager, template, tName, true,
                    current.toImmutable(), rot.add(fromFacing(face)), mirror, ward));
            current.setPos(offset);
        }
        
        current.setPos(original.up(base.getTemplate().getSize().getY()));
        return base.getTemplate().getSize();
    }
    
    public static void generateGroundFloor(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos baseSize,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        String tName = pickTemplate(GROUND_FLOOR, random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        BlockPos original = current.toImmutable();
        BlockPos add = transformOffset((baseSize.getX() - template.getSize().getX()) / 2,
                (baseSize.getZ() - template.getSize().getZ()) / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        EldritchSpireComponent floor = new EldritchSpireComponent(templateManager, template, tName,
                true, current.toImmutable(), rot, mirror, ward);
        pieces.add(floor);
        
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static void generateFirstFloor(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos baseSize,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        String tName = pickTemplate(FIRST_FLOOR, random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        BlockPos original = current.toImmutable();
        BlockPos add = transformOffset((baseSize.getX() - template.getSize().getX()) / 2,
                (baseSize.getZ() - template.getSize().getZ()) / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        EldritchSpireComponent floor = new EldritchSpireComponent(templateManager, template, tName,
                true, current.toImmutable(), rot, mirror, ward);
        pieces.add(floor);
        
        String cName = pickTemplate(CONNECTOR_MAZE, random);
        Template cTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, cName));
        BlockPos backup = current.add(transformOffset(0, template.getSize().getZ() + cTemplate.getSize().getZ() + 1, rot, mirror));
        add = transformOffset(template.getSize().getX() / 2,
                template.getSize().getZ(), rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        add = transformOffset(cTemplate.getSize().getX() / 2, cTemplate.getSize().getZ() - 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        Rotation cRot = mirror == Mirror.LEFT_RIGHT ? rot.add(Rotation.CLOCKWISE_180) : rot;
        pieces.add(new EldritchSpireComponent(templateManager, cTemplate, cName, false,
                current.toImmutable(), cRot.add(fromFacing(mirror.mirror(EnumFacing.NORTH))), mirror, ward));
        
        current.setPos(backup);
        generateMaze(world, generator, templateManager, current, cTemplate.getSize(), rot, mirror, random, pieces, ward);
        
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static void generateMazePillar(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, Rotation rot,
            Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        BlockPos backup = current.toImmutable();
        String name = pickTemplate(MAZE_PILLAR, random);
        Template sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, name));
        BlockPos add = transformOffset(-sTemplate.getSize().getX() / 2 - 1, -sTemplate.getSize().getZ() / 2 - 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, sTemplate, name, false,
                current.toImmutable(), rot, mirror, ward));
        
        name = pickTemplate(MAZE_PILLAR_BASE, random);
        sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, name));
        add = transformOffset(-sTemplate.getSize().getX() / 2 - 1, -sTemplate.getSize().getZ() / 5 - 1, rot, mirror);
        current.setPos(backup.getX() + add.getX(), backup.getY(), backup.getZ() + add.getZ());
        ChunkPrimer primer = new ChunkPrimer();
        generator.populatePrimerWithHeightmap(current.getX() >> 4, current.getZ() >> 4, primer);
        current.setY(Math.max(findGroundBlockInPrimer(primer, current.getX() & 15, current.getZ() & 15), 1));
        pieces.add(new EldritchSpirePillarComponent(templateManager, sTemplate, name, false,
                current.toImmutable(), rot, mirror, ward, PillarType.MAZE));
        
        current.setPos(backup);
    }
    
    public static void generateMaze(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos baseSize,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        BlockPos origin = current.toImmutable().down(4);
        String shellName = pickTemplate(MAZE_SHELL_BL, random);
        Template sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, shellName));
        BlockPos add = transformOffset(MathHelper.ceil((sTemplate.getSize().getX() - baseSize.getX()) / 2.0F) + 1,
                sTemplate.getSize().getZ(), rot, mirror);
        current.setPos(origin.getX() + add.getX(), origin.getY(), origin.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, sTemplate, shellName, false,
                current.toImmutable(), rot, mirror, ward));
        
        add = transformOffset(sTemplate.getSize().getX() - 4, 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 2, current.getZ() + add.getZ());
        generateMazePillar(world, generator, templateManager, current, mirror != Mirror.NONE ? rot.add(Rotation.COUNTERCLOCKWISE_90) : rot.add(Rotation.CLOCKWISE_90),
                mirror, random, pieces, ward);
        current.setPos(current.getX() - add.getX(), current.getY(), current.getZ() - add.getZ());
        add = transformOffset(-1, sTemplate.getSize().getZ() - 4, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        generateMazePillar(world, generator, templateManager, current, rot.add(Rotation.CLOCKWISE_180), mirror, random, pieces, ward);
        
        shellName = pickTemplate(MAZE_SHELL_FL, random);
        sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, shellName));
        add = transformOffset(MathHelper.ceil((sTemplate.getSize().getX() - baseSize.getX()) / 2.0F) + 1,
                -1, rot, mirror);
        current.setPos(origin.getX() + add.getX(), origin.getY(), origin.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, sTemplate, shellName, false,
                current.toImmutable(), rot, mirror, ward));
        
        shellName = pickTemplate(MAZE_SHELL_BR, random);
        sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, shellName));
        add = transformOffset((-sTemplate.getSize().getX() - baseSize.getX()) / 2 + 2,
                sTemplate.getSize().getZ(), rot, mirror);
        current.setPos(origin.getX() + add.getX(), origin.getY(), origin.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, sTemplate, shellName, false,
                current.toImmutable(), rot, mirror, ward));
        
        add = transformOffset(3, -2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 2, current.getZ() + add.getZ());
        generateMazePillar(world, generator, templateManager, current, mirror != Mirror.NONE ? rot.add(Rotation.CLOCKWISE_90) : rot.add(Rotation.COUNTERCLOCKWISE_90),
                mirror, random, pieces, ward);
        
        shellName = pickTemplate(MAZE_SHELL_FR, random);
        sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, shellName));
        add = transformOffset((-sTemplate.getSize().getX() - baseSize.getX()) / 2 + 2,
                -1, rot, mirror);
        current.setPos(origin.getX() + add.getX(), origin.getY(), origin.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, sTemplate, shellName, false,
                current.toImmutable(), rot, mirror, ward));
        
        origin = origin.add(transformOffset(sTemplate.getSize().getX() + 1, sTemplate.getSize().getZ() + baseSize.getZ() + 6, rot, mirror).up(2));
        Maze maze = new MazeGenerator().withSize(5, 5).generate(random);
        int keyX = random.nextInt(maze.getWidth());
        for (int z = 0; z < maze.getLength(); ++z) {
            for (int x = 0; x < maze.getWidth(); ++x) {
                MazeCell cell = maze.getCell(x, z);
                String cellName = null, overlayName = null;
                if (z == maze.getLength() - 1 && x == maze.getWidth() / 2) {
                    cell.setWall(EnumFacing.SOUTH, false);
                    cellName = pickTemplate(getCellTypeEntrance(cell), random);
                    // don't set overlay name, as entrance is static
                }
                else if (z == 0 && x == keyX) {
                    cellName = pickTemplate(getCellTypeNormalRoom(cell), random);
                    overlayName = pickTemplate(MAZE_OVERLAY_KEY, random);
                }
                else {
                    if (random.nextInt(4) == 0) {
                        if (cell.getNumWalls() == 3 && random.nextInt(2) == 0) {
                            cellName = pickTemplate(getCellTypeCrabRoom(cell), random);
                            // don't set overlay name as crabs are the overlay
                        }
                        else {
                            cellName = pickTemplate(getCellTypeNormalRoom(cell), random);
                            overlayName = pickTemplate(MAZE_OVERLAY_ROOM, random);
                        }
                    }
                    else {
                        if (cell.getNumWalls() == 3 && random.nextInt(2) == 0) {
                            cellName = pickTemplate(getCellTypeCrabRoom(cell), random);
                            // don't set overlay name as crabs are the overlay
                        }
                        else {
                            cellName = pickTemplate(getCellTypeHall(cell), random);
                            if (random.nextInt(8) == 0)
                                overlayName = pickTemplate(MAZE_OVERLAY_WEB, random);
                            else
                                overlayName = pickTemplate(MAZE_OVERLAY_HALL, random);
                        }
                    }
                }
                
                Rotation adjust = Rotation.NONE;
                if (cell.getNumWalls() == 1) {
                    for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                        if (cell.hasWall(dir)) {
                            switch (mirror.mirror(dir)) {
                                case EAST: {
                                    adjust = Rotation.CLOCKWISE_90;
                                    break;
                                }
                                case SOUTH: {
                                    adjust = Rotation.CLOCKWISE_180;
                                    break;
                                }
                                case WEST: {
                                    adjust = Rotation.COUNTERCLOCKWISE_90;
                                    break;
                                }
                                default: break;
                            }
                            
                            if (mirror == Mirror.LEFT_RIGHT)
                                adjust = adjust.add(Rotation.CLOCKWISE_180);
                            
                            break;
                        }
                    }
                }
                else if (cell.getNumWalls() == 2) {
                    if (cell.isCorner()) {
                        EnumFacing open1 = null;
                        EnumFacing open2 = null;
                        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                            if (!cell.hasWall(dir)) {
                                if (open1 == null)
                                    open1 = mirror.mirror(dir);
                                else {
                                    open2 = mirror.mirror(dir);
                                    break;
                                }
                            }
                        }
                        
                        int angle1 = (int) open1.getHorizontalAngle();
                        int angle2 = (int) open2.getHorizontalAngle();
                        
                        if (mirror == Mirror.FRONT_BACK) {
                            angle1 = (angle1 + 270) % 360;
                            angle2 = (angle2 + 270) % 360;
                        }
                        else if (mirror == Mirror.LEFT_RIGHT) {
                            angle1 = (angle1 + 90) % 360;
                            angle2 = (angle2 + 90) % 360;
                        }
                        
                        int temp = Math.min(angle1, angle2);
                        if (temp == angle2) {
                            angle2 = angle1;
                            angle1 = temp;
                        }
                        
                        if (angle1 == 0 && angle2 == 90)
                            adjust = Rotation.CLOCKWISE_90;
                        else if (angle1 == 90 && angle2 == 180)
                            adjust = Rotation.CLOCKWISE_180;
                        else if (angle1 == 180 && angle2 == 270)
                            adjust = Rotation.COUNTERCLOCKWISE_90;
                    }
                    else {
                        boolean wallNorth = cell.hasWall(EnumFacing.NORTH);
                        if (wallNorth)
                            adjust = Rotation.CLOCKWISE_90;
                    }
                }
                else if (cell.getNumWalls() == 3) {
                    for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                        if (!cell.hasWall(dir)) {
                            switch (mirror.mirror(dir.getOpposite())) {
                                case EAST: {
                                    adjust = Rotation.CLOCKWISE_90;
                                    break;
                                }
                                case SOUTH: {
                                    adjust = Rotation.CLOCKWISE_180;
                                    break;
                                }
                                case WEST: {
                                    adjust = Rotation.COUNTERCLOCKWISE_90;
                                    break;
                                }
                                default: break;
                            }
                            
                            if (mirror == Mirror.LEFT_RIGHT)
                                adjust = adjust.add(Rotation.CLOCKWISE_180);
                        
                            break;
                        }
                    }
                }
                
                int offsetX = 0;
                int offsetZ = 0;
                switch (adjust) {
                    case NONE: {
                        offsetX = 0;
                        offsetZ = 0;
                        break;
                    }
                    case CLOCKWISE_90: {
                        offsetX = 10;
                        offsetZ = 0;
                        break;
                    }
                    case CLOCKWISE_180: {
                        offsetX = 10;
                        offsetZ = 10;
                        break;
                    }
                    case COUNTERCLOCKWISE_90: {
                        offsetX = 0;
                        offsetZ = 10;
                        break;
                    }
                    default: break;
                }
                
                if (mirror != Mirror.NONE) {
                    int temp = offsetX;
                    offsetX = offsetZ;
                    offsetZ = temp;
                }
                
                BlockPos move = transformOffset(-11, -11, Rotation.NONE, mirror);
                BlockPos offsets = transformOffset(offsetX, offsetZ, rot, mirror);
                Rotation transformRot = rot;
                if (mirror != Mirror.NONE) {
                    switch (rot) {
                        case CLOCKWISE_90: {
                            transformRot = Rotation.COUNTERCLOCKWISE_90;
                            break;
                        }
                        case CLOCKWISE_180: {
                            transformRot = Rotation.CLOCKWISE_180;
                            break;
                        }
                        case COUNTERCLOCKWISE_90: {
                            transformRot = Rotation.CLOCKWISE_90;
                            break;
                        }
                        default: {
                            transformRot = Rotation.NONE;
                            break;
                        }
                    }
                }
                BlockPos xz = transformOffset(x, z, transformRot, Mirror.NONE);
                add = new BlockPos(xz.getX() * move.getX() + offsets.getX(), 0, xz.getZ() * move.getZ() + offsets.getZ());
                BlockPos cellLoc = origin.add(add);
                pieces.add(new EldritchSpireMazeComponent(templateManager, templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                        new ResourceLocation(ThaumicAugmentationAPI.MODID, cellName)), cellName, false, cellLoc, rot.add(adjust), mirror, ward));
                if (overlayName != null) {
                    pieces.add(new EldritchSpireMazeComponent(templateManager, templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                            new ResourceLocation(ThaumicAugmentationAPI.MODID, overlayName)), overlayName, false, cellLoc, rot.add(adjust), mirror, ward));
                }
            }
        }
    }
    
    public static void generateSecondFloor(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos baseSize,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        String tName = pickTemplate(SECOND_FLOOR, random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        BlockPos original = current.toImmutable();
        BlockPos add = transformOffset((baseSize.getX() - template.getSize().getX()) / 2,
                (baseSize.getZ() - template.getSize().getZ()) / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        EldritchSpireComponent floor = new EldritchSpireComponent(templateManager, template, tName,
                true, current.toImmutable(), rot, mirror, ward);
        pieces.add(floor);
        
        String cName = pickTemplate(CONNECTOR_PRISON, random);
        Template cTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, cName));
        BlockPos backup = current.add(transformOffset(cTemplate.getSize().getX() / 2, 0, rot, mirror));
        add = transformOffset(template.getSize().getX() / 2,
                0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        add = transformOffset(cTemplate.getSize().getX() / 2, -1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        Rotation cRot = mirror == Mirror.LEFT_RIGHT ? rot.add(Rotation.CLOCKWISE_180) : rot;
        pieces.add(new EldritchSpireComponent(templateManager, cTemplate, cName, false,
                current.toImmutable(), cRot.add(fromFacing(mirror.mirror(EnumFacing.NORTH))), mirror, ward));
        
        current.setPos(backup);
        generatePrison(world, generator, templateManager, current, cTemplate.getSize(), rot, mirror, random, pieces, ward);
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static void generatePrison(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos baseSize,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        String piece = pickTemplate(PRISON_ENTRANCE, random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        BlockPos add = transformOffset(template.getSize().getX(),
                -template.getSize().getZ() - baseSize.getZ(), rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 3, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, piece, false,
                current.toImmutable(), rot, mirror, ward));
        int entranceSizeX = template.getSize().getX();
        
        add = transformOffset(template.getSize().getX() / 2, 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        BlockPos backup = current.toImmutable();
        
        // set up transformations for reflected edges
        Mirror reflection = null;
        Rotation reflectedRot = rot;
        switch (mirror) {
            case NONE: {
                reflection = Mirror.FRONT_BACK;
                break;
            }
            case LEFT_RIGHT: {
                reflection = Mirror.NONE;
                reflectedRot = reflectedRot.add(Rotation.CLOCKWISE_180);
                break;
            }
            case FRONT_BACK: {
                reflection = Mirror.NONE;
                break;
            }
            default: {
                reflection = mirror;
                break;
            }
        }
        
        piece = pickTemplate(PRISON_EDGE, random);
        Template edge1 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(edge1.getSize().getX() + entranceSizeX / 2, 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, edge1, piece, false,
                current.toImmutable(), reflectedRot, reflection, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_EDGE, random);
        Template edge2 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(-edge2.getSize().getX() - entranceSizeX / 2, 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, edge2, piece, false,
                current.toImmutable(), rot, mirror, ward));
        
        current.setPos(backup);
        generatePrisonBlock0(world, generator, templateManager, current, edge1.getSize(), edge2.getSize(), template.getSize(),
                rot, mirror, random, pieces, ward);
        
        add = transformOffset(template.getSize().getX() / 2, 0, rot, mirror);
        piece = pickTemplate(PRISON_BLOCK_CONNECTOR, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        add = transformOffset(-template.getSize().getX() / 2, -template.getSize().getZ(), rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 5, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, piece, false,
                current.toImmutable(), rot, mirror, ward));
        
        add = transformOffset(template.getSize().getX() / 2, 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + 5, current.getZ() + add.getZ());
        generatePrisonBlock1(world, generator, templateManager, current, template.getSize(), rot, mirror, random, pieces, ward);
    }
    
    public static void generatePrisonBlock0(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos edgeSize1,
            BlockPos edgeSize2, BlockPos baseSize, Rotation rot, Mirror mirror, Random random,
            List<EldritchSpireComponent> pieces, UUID ward) {
        
        BlockPos backup = current.toImmutable();
        String piece = pickTemplate(PRISON_0_CORNER, random);
        Template corner1 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        BlockPos add = transformOffset(corner1.getSize().getX() + edgeSize1.getX() + baseSize.getX() / 2, 5, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, corner1, piece, false,
                current.toImmutable(), rot.add(Rotation.CLOCKWISE_180), mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_0_CORNER, random);
        Template corner2 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(-corner2.getSize().getX() - edgeSize2.getX() - baseSize.getX() / 2, 5, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, corner2, piece, false, current.toImmutable(),
                mirror == Mirror.NONE ? rot.add(Rotation.COUNTERCLOCKWISE_90) : rot.add(Rotation.CLOCKWISE_90), mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_0_SIDE, random);
        Template side1 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(corner1.getSize().getX() + edgeSize1.getX() + baseSize.getX() / 2,
                -side1.getSize().getX() - corner1.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, side1, piece, false, current.toImmutable(),
                mirror == Mirror.NONE ? rot.add(Rotation.CLOCKWISE_90) : rot.add(Rotation.COUNTERCLOCKWISE_90), mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_0_SIDE, random);
        Template side2 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(-corner2.getSize().getX() - edgeSize2.getX() - baseSize.getX() / 2,
                -corner1.getSize().getZ() / 2 - 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, side2, piece, false, current.toImmutable(),
                mirror == Mirror.NONE ? rot.add(Rotation.COUNTERCLOCKWISE_90) : rot.add(Rotation.CLOCKWISE_90), mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_0_CORNER, random);
        Template corner3 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(corner3.getSize().getX() + edgeSize1.getX() + baseSize.getX() / 2,
                -corner3.getSize().getZ() - side1.getSize().getX() - corner1.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, corner3, piece, false, current.toImmutable(),
                mirror == Mirror.NONE ? rot.add(Rotation.CLOCKWISE_90) : rot.add(Rotation.COUNTERCLOCKWISE_90), mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_0_CORNER, random);
        Template corner4 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(-corner4.getSize().getX() - edgeSize2.getX() - baseSize.getX() / 2,
                -corner4.getSize().getZ() - side2.getSize().getX() - corner2.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, corner4, piece, false,
                current.toImmutable(), rot, mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_0, random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(-template.getSize().getX() / 2, -template.getSize().getZ(), rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 5, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, piece, false,
                current.toImmutable(), rot, mirror, ward));
        
        add = transformOffset(template.getSize().getX() / 2 - baseSize.getX() / 2, 0, rot, mirror);
        BlockPos resume = new BlockPos(current.getX() + add.getX(), current.getY() + 5, current.getZ() + add.getZ());
        
        add = transformOffset(template.getSize().getX() / 2 + 1, template.getSize().getZ() / 2 + 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        generateGenericPillar(world, generator, templateManager, current, rot, mirror, random, pieces, ward);
        
        current.setPos(resume);
    }
    
    public static void generatePrisonBlock1(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos baseSize,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        BlockPos backup = current.toImmutable();
        String piece = pickTemplate(PRISON_1_CORNER, random);
        Template corner1 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        BlockPos add = transformOffset(corner1.getSize().getX() + baseSize.getX() / 2, 5, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, corner1, piece, false,
                current.toImmutable(), rot.add(Rotation.CLOCKWISE_180), mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_1_CORNER, random);
        Template corner2 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(-corner2.getSize().getX() - baseSize.getX() / 2, 5, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, corner2, piece, false, current.toImmutable(),
                mirror == Mirror.NONE ? rot.add(Rotation.COUNTERCLOCKWISE_90) : rot.add(Rotation.CLOCKWISE_90), mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_1_SIDE, random);
        Template side1 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(corner1.getSize().getX() + baseSize.getX() / 2,
                -side1.getSize().getX() - corner1.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, side1, piece, false, current.toImmutable(),
                mirror == Mirror.NONE ? rot.add(Rotation.CLOCKWISE_90) : rot.add(Rotation.COUNTERCLOCKWISE_90), mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_1_SIDE, random);
        Template side2 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(-corner2.getSize().getX() - baseSize.getX() / 2,
                -corner1.getSize().getZ() / 2 - 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, side2, piece, false, current.toImmutable(),
                mirror == Mirror.NONE ? rot.add(Rotation.COUNTERCLOCKWISE_90) : rot.add(Rotation.CLOCKWISE_90), mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_1_CORNER, random);
        Template corner3 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(corner3.getSize().getX() + baseSize.getX() / 2,
                -corner3.getSize().getZ() - side1.getSize().getX() - corner1.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, corner3, piece, false, current.toImmutable(),
                mirror == Mirror.NONE ? rot.add(Rotation.CLOCKWISE_90) : rot.add(Rotation.COUNTERCLOCKWISE_90), mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_1_CORNER, random);
        Template corner4 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(-corner4.getSize().getX() - baseSize.getX() / 2,
                -corner4.getSize().getZ() - side2.getSize().getX() - corner2.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, corner4, piece, false,
                current.toImmutable(), rot, mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_1_SIDE, random);
        Template side3 = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(-side3.getSize().getX() / 2,
                -corner4.getSize().getZ() - side2.getSize().getX() - corner2.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, side3, piece, false,
                current.toImmutable(), rot, mirror, ward));
        
        current.setPos(backup);
        piece = pickTemplate(PRISON_1, random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, piece));
        add = transformOffset(-template.getSize().getX() / 2, -template.getSize().getZ(), rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 5, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, piece, false,
                current.toImmutable(), rot, mirror, ward));
        
        add = transformOffset(template.getSize().getX() / 2 + 1, template.getSize().getZ() / 2 + 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        generateGenericPillar(world, generator, templateManager, current, rot, mirror, random, pieces, ward);
    }
    
    public static void generateThirdFloor(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos baseSize,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        String tName = pickTemplate(THIRD_FLOOR, random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        BlockPos original = current.toImmutable();
        BlockPos add = transformOffset((baseSize.getX() - template.getSize().getX()) / 2,
                (baseSize.getZ() - template.getSize().getZ()) / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        EldritchSpireComponent floor = new EldritchSpireComponent(templateManager, template, tName,
                true, current.toImmutable(), rot, mirror, ward);
        pieces.add(floor);
        
        String cName = pickTemplate(CONNECTOR_LIBRARY, random);
        Template cTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, cName));
        BlockPos backup = current.add(transformOffset(-template.getSize().getZ() - cTemplate.getSize().getZ(), 0, rot, mirror));
        add = transformOffset(0,
                template.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        add = transformOffset(-cTemplate.getSize().getZ(), cTemplate.getSize().getX() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        Rotation cRot = mirror == Mirror.LEFT_RIGHT ? rot.add(Rotation.CLOCKWISE_180) : rot;
        pieces.add(new EldritchSpireComponent(templateManager, cTemplate, cName, false,
                current.toImmutable(), cRot.add(fromFacing(mirror.mirror(EnumFacing.EAST))), mirror, ward));
        
        current.setPos(backup);
        generateLibrary(world, generator, templateManager, current, cTemplate.getSize(), rot, mirror, random, pieces, ward);
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static void generateLibraryPillar(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, Rotation rot,
            Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        BlockPos backup = current.toImmutable();
        String name = pickTemplate(LIBRARY_PILLAR, random);
        Template sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, name));
        BlockPos add = transformOffset(-sTemplate.getSize().getX() / 2 - 1, -sTemplate.getSize().getZ() / 2 - 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, sTemplate, name, false,
                current.toImmutable(), rot, mirror, ward));
        
        name = pickTemplate(LIBRARY_PILLAR_BASE, random);
        sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, name));
        add = transformOffset(-sTemplate.getSize().getX() / 2 - 1, -sTemplate.getSize().getZ() / 2 + 2, rot, mirror);
        current.setPos(backup.getX() + add.getX(), backup.getY(), backup.getZ() + add.getZ());
        ChunkPrimer primer = new ChunkPrimer();
        generator.populatePrimerWithHeightmap(current.getX() >> 4, current.getZ() >> 4, primer);
        current.setY(Math.max(findGroundBlockInPrimer(primer, current.getX() & 15, current.getZ() & 15), 1));
        pieces.add(new EldritchSpirePillarComponent(templateManager, sTemplate, name, false,
                current.toImmutable(), rot, mirror, ward, PillarType.LIBRARY));
        
        current.setPos(backup);
    }
    
    public static void generateLibrary(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos connectorSize,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        BlockPos backup = current.toImmutable();
        String tName = pickTemplate(LIBRARY_GROUND_FLOOR_FRONT, random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        BlockPos add = transformOffset(-connectorSize.getZ() / 2 - 1, -2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 3, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        MutableBlockPos mainSize = new MutableBlockPos(template.getSize());
        
        tName = pickTemplate(LIBRARY_GROUND_FLOOR_BACK, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-template.getSize().getZ() - 1, 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        
        tName = pickTemplate(LIBRARY_BACK, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-template.getSize().getX(), template.getSize().getZ() / 2 - 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + 13, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        int backSizeZ = template.getSize().getZ();
        BlockPos cornerSideBackup = current.toImmutable();
        
        tName = pickTemplate(LIBRARY_CORNER, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(0, template.getSize().getZ() - (template.getSize().getZ() - backSizeZ), rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 11, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        
        current.setPos(backup);
        tName = pickTemplate(LIBRARY_SIDE_FRONT, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-connectorSize.getZ() / 2 - 1, backSizeZ + template.getSize().getZ() + 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + 13, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        BlockPos sideSize = template.getSize();
        
        tName = pickTemplate(LIBRARY_SIDE_BACK, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-template.getSize().getX(), 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        
        Mirror reflection = null;
        Rotation reflectedRot = rot;
        switch (mirror) {
            case NONE: {
                reflection = Mirror.LEFT_RIGHT;
                break;
            }
            case LEFT_RIGHT: {
                reflection = Mirror.NONE;
                break;
            }
            case FRONT_BACK: {
                reflection = Mirror.NONE;
                reflectedRot = reflectedRot.add(Rotation.CLOCKWISE_180);
                break;
            }
            default: {
                reflection = mirror;
                break;
            }
        }
        
        current.setPos(cornerSideBackup);
        tName = pickTemplate(LIBRARY_CORNER, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(0, -1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 11, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), reflectedRot, reflection, ward));
        
        current.setPos(backup);
        tName = pickTemplate(LIBRARY_SIDE_FRONT, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-connectorSize.getZ() / 2 - 1, -3, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + 13, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), reflectedRot, reflection, ward));
        
        tName = pickTemplate(LIBRARY_SIDE_BACK, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-template.getSize().getX(), 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), reflectedRot, reflection, ward));
        
        current.setPos(backup);
        tName = pickTemplate(LIBRARY_HALL_LOWER, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(connectorSize.getZ() / 2, -template.getSize().getZ() - 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 2, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        
        BlockPos pillarTemp = current.toImmutable();
        add = transformOffset(template.getSize().getX() / 2 + 1, 5, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 2, current.getZ() + add.getZ());
        generateLibraryPillar(world, generator, templateManager, current, rot, mirror, random, pieces, ward);
        current.setPos(pillarTemp);
        
        tName = pickTemplate(LIBRARY_HALL_LOWER, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-template.getSize().getX(), 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        
        tName = pickTemplate(LIBRARY_HALL_LOWER_BACK, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-template.getSize().getX(), 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        
        add = transformOffset(template.getSize().getX() / 2 + 1, 5, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 2, current.getZ() + add.getZ());
        generateLibraryPillar(world, generator, templateManager, current, rot, mirror, random, pieces, ward);
        
        current.setPos(backup);
        tName = pickTemplate(LIBRARY_HALL_LOWER, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(connectorSize.getZ() / 2, mainSize.getZ() + template.getSize().getZ() - 3, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 2, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), reflectedRot, reflection, ward));
        
        pillarTemp = current.toImmutable();
        add = transformOffset(template.getSize().getX() / 2 + 1, 5, reflectedRot, reflection);
        current.setPos(current.getX() + add.getX(), current.getY() - 2, current.getZ() + add.getZ());
        generateLibraryPillar(world, generator, templateManager, current, reflectedRot, reflection, random, pieces, ward);
        current.setPos(pillarTemp);
        
        tName = pickTemplate(LIBRARY_HALL_LOWER, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-template.getSize().getX(), 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), reflectedRot, reflection, ward));
        
        tName = pickTemplate(LIBRARY_HALL_LOWER_BACK, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-template.getSize().getX(), 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), reflectedRot, reflection, ward));
        
        add = transformOffset(template.getSize().getX() / 2 + 1, 5, reflectedRot, reflection);
        current.setPos(current.getX() + add.getX(), current.getY() - 2, current.getZ() + add.getZ());
        generateLibraryPillar(world, generator, templateManager, current, reflectedRot, reflection, random, pieces, ward);
        
        current.setPos(backup);
        tName = pickTemplate(LIBRARY_HALL_UPPER, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(template.getSize().getX() - 2, -template.getSize().getZ() - sideSize.getZ() - 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + 14, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        
        for (int i = 0; i < 2; ++i) {
            tName = pickTemplate(LIBRARY_HALL_UPPER, random);
            template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                        new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
            add = transformOffset(-template.getSize().getX() - 8, 0, rot, mirror);
            current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
            pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                    current.toImmutable(), rot, mirror, ward));
        }
        
        current.setPos(backup);
        tName = pickTemplate(LIBRARY_HALL_UPPER, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(template.getSize().getX() - 2, mainSize.getZ() + template.getSize().getZ() + sideSize.getZ() - 3, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + 14, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), reflectedRot, reflection, ward));
        
        for (int i = 0; i < 2; ++i) {
            tName = pickTemplate(LIBRARY_HALL_UPPER, random);
            template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                        new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
            add = transformOffset(-template.getSize().getX() - 8, 0, rot, mirror);
            current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
            pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                    current.toImmutable(), reflectedRot, reflection, ward));
        }
        
        current.setPos(backup);
        tName = pickTemplate(LIBRARY_FIRST_FLOOR_FRONT, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-connectorSize.getZ() / 2 - 1, -2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + mainSize.getY() - 3, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        
        tName = pickTemplate(LIBRARY_FIRST_FLOOR_BACK, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-template.getSize().getZ() - 1, 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        int heightAdd = template.getSize().getY();
        
        int keyAlcove = random.nextInt(14);
        current.setPos(backup);
        tName = pickTemplate(keyAlcove == 0 ? LIBRARY_ALCOVE_KEY : LIBRARY_ALCOVE, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(connectorSize.getZ() / 2 + template.getSize().getX() + 1, -template.getSize().getZ() - 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + mainSize.getY() + template.getSize().getY() - 2, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        
        for (int i = 1; i < 6; ++i) {
            tName = pickTemplate(keyAlcove == i ? LIBRARY_ALCOVE_KEY : LIBRARY_ALCOVE, random);
            template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                        new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
            add = transformOffset(-template.getSize().getX() - (i == 3 ? 2 : 1), 0, rot, mirror);
            current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
            pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                    current.toImmutable(), rot, mirror, ward));
        }
        
        current.setPos(backup);
        tName = pickTemplate(keyAlcove == 6 ? LIBRARY_ALCOVE_KEY : LIBRARY_ALCOVE, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(connectorSize.getZ() / 2 + template.getSize().getX() + 1, mainSize.getZ() + template.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + mainSize.getY() + template.getSize().getY() - 2, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), reflectedRot, reflection, ward));
        
        for (int i = 7; i < 12; ++i) {
            tName = pickTemplate(keyAlcove == i ? LIBRARY_ALCOVE_KEY : LIBRARY_ALCOVE, random);
            template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                        new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
            add = transformOffset(-template.getSize().getX() - (i == 9 ? 2 : 1), 0, rot, mirror);
            current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
            pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                    current.toImmutable(), reflectedRot, reflection, ward));
        }
        
        current.setPos(backup);
        tName = pickTemplate(keyAlcove == 12 ? LIBRARY_ALCOVE_KEY : LIBRARY_ALCOVE, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-connectorSize.getZ() / 2 - mainSize.getX() - template.getSize().getZ() - 1, template.getSize().getX() + 5, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + mainSize.getY() + template.getSize().getY() - 2, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false, current.toImmutable(),
                mirror == Mirror.NONE ? rot.add(Rotation.COUNTERCLOCKWISE_90) : rot.add(Rotation.CLOCKWISE_90), mirror, ward));
        
        tName = pickTemplate(keyAlcove == 13 ? LIBRARY_ALCOVE_KEY : LIBRARY_ALCOVE, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(0, template.getSize().getX() + 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false, current.toImmutable(),
                mirror == Mirror.NONE ? rot.add(Rotation.COUNTERCLOCKWISE_90) : rot.add(Rotation.CLOCKWISE_90), mirror, ward));
        
        mainSize.setY(mainSize.getY() + heightAdd);
        current.setPos(backup);
        tName = pickTemplate(LIBRARY_SECOND_FLOOR_FRONT, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-connectorSize.getZ() / 2 - 1, -2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() + mainSize.getY() - 3, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
        
        tName = pickTemplate(LIBRARY_SECOND_FLOOR_BACK, random);
        template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        add = transformOffset(-template.getSize().getZ() - 1, 0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, template, tName, false,
                current.toImmutable(), rot, mirror, ward));
    }
    
    public static void generateFourthFloor(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos baseSize,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        String tName = pickTemplate(FOURTH_FLOOR, random);
        Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, tName));
        BlockPos original = current.toImmutable();
        BlockPos add = transformOffset((baseSize.getX() - template.getSize().getX()) / 2,
                (baseSize.getZ() - template.getSize().getZ()) / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        EldritchSpireComponent floor = new EldritchSpireComponent(templateManager, template, tName,
                true, current.toImmutable(), rot, mirror, ward);
        pieces.add(floor);
        
        String cName = pickTemplate(CONNECTOR_BOSS, random);
        Template cTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, cName));
        BlockPos backup = current.add(transformOffset(template.getSize().getZ() + cTemplate.getSize().getZ() + 1, 0, rot, mirror));
        add = transformOffset(template.getSize().getX(),
                template.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        add = transformOffset(cTemplate.getSize().getZ() - 1, -cTemplate.getSize().getX() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        Rotation cRot = mirror == Mirror.LEFT_RIGHT ? rot.add(Rotation.CLOCKWISE_180) : rot;
        pieces.add(new EldritchSpireComponent(templateManager, cTemplate, cName, false,
                current.toImmutable(), cRot.add(fromFacing(mirror.mirror(EnumFacing.WEST))), mirror, ward));
        
        current.setPos(backup);
        generateBossRoom(world, generator, templateManager, current, cTemplate.getSize(), rot, mirror, random, pieces, ward);
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static void generateBossRoom(World world, ITAChunkGenerator generator, TemplateManager templateManager, MutableBlockPos current, BlockPos baseSize,
            Rotation rot, Mirror mirror, Random random, List<EldritchSpireComponent> pieces, UUID ward) {
        
        String bossName = pickTemplate(BOSS, random);
        Template bTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, bossName));
        BlockPos add = transformOffset(-1, -2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 2, current.getZ() + add.getZ());
        pieces.add(new EldritchSpireComponent(templateManager, bTemplate, bossName, false,
                current.toImmutable(), rot, mirror, ward));
        
        add = transformOffset(bTemplate.getSize().getX() / 2 + 1, bTemplate.getSize().getZ() / 2 + 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY() - 1, current.getZ() + add.getZ());
        generateGenericPillar(world, generator, templateManager, current, rot, mirror, random, pieces, ward);
    }
    
    protected static final String BASE = "spire/base_";
    protected static final String STAIR = "spire/stairs_";
    protected static final String GROUND_FLOOR = "spire/ground_floor_";
    protected static final String FIRST_FLOOR = "spire/first_floor_";
    protected static final String SECOND_FLOOR = "spire/second_floor_";
    protected static final String THIRD_FLOOR = "spire/third_floor_";
    protected static final String FOURTH_FLOOR = "spire/fourth_floor_";
    protected static final String PILLAR = "spire/pillar_";
    protected static final String PILLAR_BASE = "spire/pillar_b_";
    protected static final String CONNECTOR_MAZE = "spire/connector_maze_";
    protected static final String CONNECTOR_PRISON = "spire/connector_prison_";
    protected static final String CONNECTOR_LIBRARY = "spire/connector_library_";
    protected static final String CONNECTOR_BOSS = "spire/connector_boss_";
    
    protected static final String MAZE_SHELL_BL = "spire/maze/shell_bl_";
    protected static final String MAZE_SHELL_BR = "spire/maze/shell_br_";
    protected static final String MAZE_SHELL_FL = "spire/maze/shell_fl_";
    protected static final String MAZE_SHELL_FR = "spire/maze/shell_fr_";
    protected static final String MAZE_PILLAR = "spire/maze/pillar_";
    protected static final String MAZE_PILLAR_BASE = "spire/maze/pillar_b_";
    protected static final String MAZE_CELL_HALL_CROSS = "spire/maze/cell_hall_cross_";
    protected static final String MAZE_CELL_HALL_T = "spire/maze/cell_hall_t_";
    protected static final String MAZE_CELL_HALL_CORNER = "spire/maze/cell_hall_corner_";
    protected static final String MAZE_CELL_HALL_STRAIGHT = "spire/maze/cell_hall_straight_";
    protected static final String MAZE_CELL_HALL_END = "spire/maze/cell_hall_end_";
    protected static final String MAZE_CELL_ROOM_CROSS = "spire/maze/cell_room_cross_";
    protected static final String MAZE_CELL_ROOM_T = "spire/maze/cell_room_t_";
    protected static final String MAZE_CELL_ROOM_CORNER = "spire/maze/cell_room_corner_";
    protected static final String MAZE_CELL_ROOM_STRAIGHT = "spire/maze/cell_room_straight_";
    protected static final String MAZE_CELL_ROOM_END = "spire/maze/cell_room_end_";
    protected static final String MAZE_CELL_ENTRANCE_CROSS = "spire/maze/cell_entrance_cross_";
    protected static final String MAZE_CELL_ENTRANCE_T = "spire/maze/cell_entrance_t_";
    protected static final String MAZE_CELL_ENTRANCE_CORNER = "spire/maze/cell_entrance_corner_";
    protected static final String MAZE_CELL_ENTRANCE_STRAIGHT = "spire/maze/cell_entrance_straight_";
    protected static final String MAZE_CELL_ENTRANCE_END = "spire/maze/cell_entrance_end_";
    protected static final String MAZE_CELL_ROOM_END_CRAB = "spire/maze/cell_room_end_crab_";
    protected static final String MAZE_OVERLAY_ROOM = "spire/maze/cell_overlay_room_";
    protected static final String MAZE_OVERLAY_HALL = "spire/maze/cell_overlay_hall_";
    protected static final String MAZE_OVERLAY_WEB = "spire/maze/cell_overlay_web_";
    protected static final String MAZE_OVERLAY_KEY = "spire/maze/cell_overlay_key_";
    
    protected static final String PRISON_ENTRANCE = "spire/prison/entrance_";
    protected static final String PRISON_0 = "spire/prison/0_";
    protected static final String PRISON_0_CORNER = "spire/prison/0_cell_corner_";
    protected static final String PRISON_0_SIDE = "spire/prison/0_cell_side_";
    protected static final String PRISON_BLOCK_CONNECTOR = "spire/prison/connector_";
    protected static final String PRISON_1 = "spire/prison/1_";
    protected static final String PRISON_1_CORNER = "spire/prison/1_cell_corner_";
    protected static final String PRISON_1_SIDE = "spire/prison/1_cell_side_";
    protected static final String PRISON_EDGE = "spire/prison/cell_edge_";
    
    protected static final String LIBRARY_GROUND_FLOOR_FRONT = "spire/library/ground0_";
    protected static final String LIBRARY_GROUND_FLOOR_BACK = "spire/library/ground1_";
    protected static final String LIBRARY_FIRST_FLOOR_FRONT = "spire/library/first0_";
    protected static final String LIBRARY_FIRST_FLOOR_BACK = "spire/library/first1_";
    protected static final String LIBRARY_SECOND_FLOOR_FRONT = "spire/library/second0_";
    protected static final String LIBRARY_SECOND_FLOOR_BACK = "spire/library/second1_";
    protected static final String LIBRARY_BACK = "spire/library/back_";
    protected static final String LIBRARY_CORNER = "spire/library/corner_";
    protected static final String LIBRARY_SIDE_FRONT = "spire/library/side0_";
    protected static final String LIBRARY_SIDE_BACK = "spire/library/side1_";
    protected static final String LIBRARY_HALL_LOWER = "spire/library/lower_hall_";
    protected static final String LIBRARY_HALL_LOWER_BACK = "spire/library/lower_hall_back_";
    protected static final String LIBRARY_HALL_UPPER = "spire/library/upper_hall_";
    protected static final String LIBRARY_ALCOVE = "spire/library/alcove_";
    protected static final String LIBRARY_ALCOVE_KEY = "spire/library/alcove_key_";
    protected static final String LIBRARY_PILLAR = "spire/library/pillar_";
    protected static final String LIBRARY_PILLAR_BASE = "spire/library/pillar_b_";
    
    protected static final String BOSS = "spire/boss/boss_";
    
    // so we don't need to have an individual loop for every single path
    protected static final ImmutableSet<String> TEMPLATE_PATHS = ImmutableSet.<String>builder().add(
            BASE, STAIR, GROUND_FLOOR, FIRST_FLOOR, SECOND_FLOOR, THIRD_FLOOR, FOURTH_FLOOR, CONNECTOR_MAZE, PILLAR, PILLAR_BASE,
            CONNECTOR_PRISON, CONNECTOR_LIBRARY, CONNECTOR_BOSS, MAZE_SHELL_BL, MAZE_SHELL_BR, MAZE_SHELL_FL, MAZE_SHELL_FR, MAZE_PILLAR,
            MAZE_PILLAR_BASE, MAZE_CELL_HALL_CROSS, MAZE_CELL_HALL_T, MAZE_CELL_HALL_CORNER, MAZE_CELL_HALL_STRAIGHT, MAZE_CELL_HALL_END,
            MAZE_CELL_ROOM_CROSS, MAZE_CELL_ROOM_T, MAZE_CELL_ROOM_CORNER, MAZE_CELL_ROOM_STRAIGHT, MAZE_CELL_ROOM_END,
            MAZE_CELL_ENTRANCE_CROSS, MAZE_CELL_ENTRANCE_T, MAZE_CELL_ENTRANCE_CORNER, MAZE_CELL_ENTRANCE_STRAIGHT, MAZE_CELL_ENTRANCE_END,
            MAZE_CELL_ROOM_END_CRAB, MAZE_OVERLAY_ROOM, MAZE_OVERLAY_HALL, MAZE_OVERLAY_WEB, MAZE_OVERLAY_KEY,
            PRISON_ENTRANCE, PRISON_0, PRISON_0_CORNER, PRISON_0_SIDE, PRISON_BLOCK_CONNECTOR, PRISON_1, PRISON_1_CORNER,
            PRISON_1_SIDE, PRISON_EDGE, LIBRARY_GROUND_FLOOR_FRONT, LIBRARY_GROUND_FLOOR_BACK, LIBRARY_FIRST_FLOOR_FRONT,
            LIBRARY_FIRST_FLOOR_BACK, LIBRARY_SECOND_FLOOR_FRONT, LIBRARY_SECOND_FLOOR_BACK, LIBRARY_BACK,
            LIBRARY_CORNER, LIBRARY_SIDE_FRONT, LIBRARY_SIDE_BACK, LIBRARY_HALL_LOWER, LIBRARY_HALL_LOWER_BACK,
            LIBRARY_HALL_UPPER, LIBRARY_ALCOVE, LIBRARY_ALCOVE_KEY, LIBRARY_PILLAR, LIBRARY_PILLAR_BASE, BOSS
    ).build();
    
    protected static ImmutableMap<String, Integer> templateCounts = ImmutableMap.of();
    
    public static void findTemplateVariants(TemplateManager manager) {
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        for (String s : TEMPLATE_PATHS) {
            int i = 0;
            do {
                if (manager.get(server, new ResourceLocation(ThaumicAugmentationAPI.MODID, s + i)) == null)
                    break;
                
                ++i;
            } while (true);
            
            builder.put(s, i);
        }
        
        templateCounts = builder.build();
    }
    
}
