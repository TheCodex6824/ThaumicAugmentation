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

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.util.maze.Maze;
import thecodex6824.thaumicaugmentation.common.util.maze.MazeCell;
import thecodex6824.thaumicaugmentation.common.util.maze.MazeGenerator;

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
    
    protected static Rotation fromFacing(EnumFacing face) {
        switch (face) {
            case WEST: return Rotation.COUNTERCLOCKWISE_90;
            case NORTH: return Rotation.CLOCKWISE_180;
            case EAST: return Rotation.CLOCKWISE_90;
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
    
    protected static String pickMazeCellHall(Random rand, MazeCell cell) {
        switch (cell.getNumWalls()) {
            case 0: return "maze_cell_hall_cross_" + rand.nextInt(4);
            case 1: return "maze_cell_hall_t_" + rand.nextInt(4);
            case 2: {
                if (cell.isCorner())
                    return "maze_cell_hall_corner_" + rand.nextInt(4);
                else
                    return "maze_cell_hall_straight_" + rand.nextInt(4);
            }
            case 3: return "maze_cell_hall_end_" + rand.nextInt(4);
            default: return "maze_cell_closed";
        }
    }
    
    protected static String pickMazeCellRoomNormal(Random rand, MazeCell cell) {
        switch (cell.getNumWalls()) {
            case 0: return "maze_cell_room_cross_" + rand.nextInt(2);
            case 1: return "maze_cell_room_t_" + rand.nextInt(2);
            case 2: {
                if (cell.isCorner())
                    return "maze_cell_room_corner_" + rand.nextInt(2);
                else
                    return "maze_cell_room_straight_" + rand.nextInt(2);
            }
            case 3: return "maze_cell_room_end_" + rand.nextInt(2);
            default: return "maze_cell_closed";
        }
    }
    
    protected static String pickMazeCellRoomCrab(Random rand, MazeCell cell) {
        if (cell.getNumWalls() == 3)
            return "maze_cell_room_end_crab_" + rand.nextInt(4);
        else
            return "maze_cell_closed";
    }
    
    protected static String pickMazeCellRoomEntrance(Random rand, MazeCell cell) {
        switch (cell.getNumWalls()) {
            case 0: return "maze_cell_entrance_cross";
            case 1: return "maze_cell_entrance_t";
            case 2: {
                if (cell.isCorner())
                    return "maze_cell_entrance_corner";
                else
                    return "maze_cell_entrance_straight";
            }
            case 3: return "maze_cell_entrance_end";
            default: return "maze_cell_closed";
        }
    }
    
    protected static String pickMazeCellOverlayRoom(Random rand) {
        return "maze_cell_overlay_room_" + rand.nextInt(12);
    }
    
    protected static String pickMazeCellOverlayHall(Random rand) {
        return "maze_cell_overlay_hall_" + rand.nextInt(4);
    }
    
    protected static String pickMazeCellOverlayWeb(Random rand) {
        return "maze_cell_overlay_web_" + rand.nextInt(4);
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
        for (EnumFacing f : EnumFacing.HORIZONTALS) {
            EnumFacing face = mirror.mirror(f);
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
                    current.toImmutable(), rot.add(fromFacing(face)), mirror));
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
        
        Template cTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/connector"));
        BlockPos backup = current.add(transformOffset(0, template.getSize().getZ() + cTemplate.getSize().getZ(), rot, mirror));
        add = transformOffset(template.getSize().getX() / 2,
                template.getSize().getZ(), rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        add = transformOffset(-MathHelper.ceil(cTemplate.getSize().getX() / 2.0F), MathHelper.ceil(cTemplate.getSize().getZ() / 2.0F) + 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        Rotation cRot = mirror == Mirror.LEFT_RIGHT ? rot.add(Rotation.CLOCKWISE_180) : rot;
        pieces.add(new EldritchSpireTemplate(templateManager, cTemplate, "connector", false,
                current.toImmutable(), cRot.add(fromFacing(mirror.mirror(EnumFacing.WEST))), mirror));
        
        current.setPos(backup);
        generateMaze(templateManager, current, cTemplate.getSize(), rot, mirror, random, pieces);
        
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
    public static void generateMaze(TemplateManager templateManager, MutableBlockPos current,
            BlockPos baseSize, Rotation rot, Mirror mirror, Random random, List<EldritchSpireTemplate> pieces) {
        
        BlockPos origin = current.toImmutable().down(3);
        Template sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/maze_shell_bl"));
        BlockPos add = transformOffset((sTemplate.getSize().getX() - baseSize.getX()) / 2 + 1,
                sTemplate.getSize().getZ(), rot, mirror);
        current.setPos(origin.getX() + add.getX(), origin.getY(), origin.getZ() + add.getZ());
        pieces.add(new EldritchSpireTemplate(templateManager, sTemplate, "maze_shell_bl", false,
                current.toImmutable(), rot, mirror));
        
        sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/maze_shell_fl"));
        add = transformOffset((sTemplate.getSize().getX() - baseSize.getX()) / 2 + 1,
                -1, rot, mirror);
        current.setPos(origin.getX() + add.getX(), origin.getY(), origin.getZ() + add.getZ());
        pieces.add(new EldritchSpireTemplate(templateManager, sTemplate, "maze_shell_fl", false,
                current.toImmutable(), rot, mirror));
        
        sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/maze_shell_br"));
        add = transformOffset((-sTemplate.getSize().getX() - baseSize.getX()) / 2 + 1,
                sTemplate.getSize().getZ(), rot, mirror);
        current.setPos(origin.getX() + add.getX(), origin.getY(), origin.getZ() + add.getZ());
        pieces.add(new EldritchSpireTemplate(templateManager, sTemplate, "maze_shell_br", false,
                current.toImmutable(), rot, mirror));
        
        sTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/maze_shell_fr"));
        add = transformOffset((-sTemplate.getSize().getX() - baseSize.getX()) / 2 + 1,
                -1, rot, mirror);
        current.setPos(origin.getX() + add.getX(), origin.getY(), origin.getZ() + add.getZ());
        pieces.add(new EldritchSpireTemplate(templateManager, sTemplate, "maze_shell_fr", false,
                current.toImmutable(), rot, mirror));
        
        origin = origin.add(transformOffset(sTemplate.getSize().getX() + 1, sTemplate.getSize().getZ() + baseSize.getZ() + 8, rot, mirror).up(1));
        Maze maze = new MazeGenerator().withSize(5, 5).generate(random);
        int keyX = random.nextInt(maze.getWidth());
        for (int z = 0; z < maze.getLength(); ++z) {
            for (int x = 0; x < maze.getWidth(); ++x) {
                MazeCell cell = maze.getCell(x, z);
                String cellName = null, overlayName = null;
                if (z == maze.getLength() - 1 && x == maze.getWidth() / 2) {
                    cell.setWall(EnumFacing.SOUTH, false);
                    cellName = pickMazeCellRoomEntrance(random, cell);
                    // don't set overlay name, as entrance is static
                }
                else if (z == 0 && x == keyX) {
                    cellName = pickMazeCellRoomNormal(random, cell);
                    overlayName = "maze_cell_overlay_key";
                }
                else {
                    if (random.nextInt(4) == 0) {
                        if (cell.getNumWalls() == 3 && random.nextInt(2) == 0) {
                            cellName = pickMazeCellRoomCrab(random, cell);
                            // don't set overlay name as crabs are the overlay
                        }
                        else {
                            cellName = pickMazeCellRoomNormal(random, cell);
                            overlayName = pickMazeCellOverlayRoom(random);
                        }
                    }
                    else {
                        if (cell.getNumWalls() == 3 && random.nextInt(2) == 0) {
                            cellName = pickMazeCellRoomCrab(random, cell);
                            // don't set overlay name as crabs are the overlay
                        }
                        else {
                            cellName = pickMazeCellHall(random, cell);
                            if (random.nextInt(4) == 0)
                                overlayName = pickMazeCellOverlayWeb(random);
                            else
                                overlayName = pickMazeCellOverlayHall(random);
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
                
                BlockPos move = transformOffset(-11, -11, rot, mirror);
                BlockPos offsets = transformOffset(offsetX, offsetZ, rot, mirror);
                Rotation transformRot = rot == Rotation.CLOCKWISE_180 || rot == Rotation.COUNTERCLOCKWISE_90 ? 
                        rot.add(Rotation.CLOCKWISE_180) : rot;
                BlockPos xz = transformOffset(x, z, transformRot, mirror);
                add = new BlockPos(xz.getX() * move.getX() + offsets.getX(), 0, xz.getZ() * move.getZ() + offsets.getZ());
                BlockPos cellLoc = origin.add(add);
                pieces.add(new EldritchSpireTemplate(templateManager, templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                        new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + cellName)), cellName, false, cellLoc, rot.add(adjust), mirror));
                if (overlayName != null) {
                    pieces.add(new EldritchSpireTemplate(templateManager, templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                            new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + overlayName)), overlayName, false, cellLoc, rot.add(adjust), mirror));
                }
            }
        }
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
        
        Template cTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/connector"));
        add = transformOffset(template.getSize().getX() / 2,
                0, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        add = transformOffset(MathHelper.ceil(cTemplate.getSize().getX() / 2.0F), -cTemplate.getSize().getZ() + 1, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        Rotation cRot = mirror == Mirror.LEFT_RIGHT ? rot.add(Rotation.CLOCKWISE_180) : rot;
        pieces.add(new EldritchSpireTemplate(templateManager, cTemplate, "connector", false,
                current.toImmutable(), cRot.add(fromFacing(mirror.mirror(EnumFacing.EAST))), mirror));
        
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
        
        Template cTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/connector"));
        add = transformOffset(0,
                template.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        add = transformOffset(-cTemplate.getSize().getX(), -cTemplate.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        Rotation cRot = mirror == Mirror.LEFT_RIGHT ? rot.add(Rotation.CLOCKWISE_180) : rot;
        pieces.add(new EldritchSpireTemplate(templateManager, cTemplate, "connector", false,
                current.toImmutable(), cRot.add(fromFacing(mirror.mirror(EnumFacing.SOUTH))), mirror));
        
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
        
        Template cTemplate = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/connector"));
        add = transformOffset(template.getSize().getX(),
                template.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        add = transformOffset(cTemplate.getSize().getX() - 1, cTemplate.getSize().getZ() / 2, rot, mirror);
        current.setPos(current.getX() + add.getX(), current.getY(), current.getZ() + add.getZ());
        Rotation cRot = mirror == Mirror.LEFT_RIGHT ? rot.add(Rotation.CLOCKWISE_180) : rot;
        pieces.add(new EldritchSpireTemplate(templateManager, cTemplate, "connector", false,
                current.toImmutable(), cRot.add(fromFacing(mirror.mirror(EnumFacing.NORTH))), mirror));
        
        current.setPos(original.up(floor.getTemplate().getSize().getY()));
    }
    
}
