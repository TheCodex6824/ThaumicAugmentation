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
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

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
    
}
