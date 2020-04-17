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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType.LockType;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.common.entity.EntityAutocasterEldritch;

public class EldritchSpireComponents {

    public static void register() {
        MapGenStructureIO.registerStructureComponent(EldritchSpireTemplate.class, "est");
    }
    
    public static void generate(TemplateManager templateManager, BlockPos position,
            Rotation rot, Random random, List<EldritchSpireTemplate> pieces) {
        
        MutableBlockPos current = new MutableBlockPos(position);
        EldritchSpireTemplate base = new EldritchSpireTemplate(templateManager, pickBase(random),
                true, current.toImmutable(), rot, pickMirror(random));
        pieces.add(base);
        BlockPos offset = current.add(base.getTemplate().getSize().getX() / 2, 0, -base.getTemplate().getSize().getZ() / 2);
        current.setPos(offset);
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            String tName = pickStairs(random);
            Template template = templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(ThaumicAugmentationAPI.MODID, "spire/" + tName));
            if (template == null)
                throw new NullPointerException("Structure template is null (should be impossible)");
            
            current.move(face, face.getXOffset() != 0 ? base.getTemplate().getSize().getX() / 2 + 1 :
                base.getTemplate().getSize().getZ() / 2 + 1);
            
            // always use the x size because that's what matters when rotated
            int sizeX = template.getSize().getX();
            if (face.getXOffset() != 0)
                current.setPos(current.getX(), current.getY(), current.getZ() + sizeX / 2 * face.getXOffset());
            else
                current.setPos(current.getX() - sizeX / 2 * face.getZOffset(), current.getY(), current.getZ());
                
            pieces.add(new EldritchSpireTemplate(templateManager, template, tName, true,
                    current.toImmutable(), fromFacing(rot, face), random.nextBoolean() ? Mirror.LEFT_RIGHT : Mirror.NONE));
            current.setPos(offset);
        }
    }
    
    protected static Rotation fromFacing(Rotation base, EnumFacing face) {
        switch (face) {
            case WEST: return base.add(Rotation.COUNTERCLOCKWISE_90);
            case SOUTH: return base.add(Rotation.CLOCKWISE_180);
            case EAST: return base.add(Rotation.CLOCKWISE_90);
            default: return base;
        }
    }
    
    protected static Mirror pickMirror(Random rand) {
        return Mirror.values()[rand.nextInt(Mirror.values().length)];
    }
    
    protected static String pickBase(Random rand) {
        return "base";
    }
    
    protected static String pickStairs(Random rand) {
        return "stairs_" + rand.nextInt(4);
    }
    
    public static class EldritchSpireTemplate extends StructureComponentTemplate {
        
        protected String name;
        protected BlockPos pos;
        protected boolean fillBlocks;
        
        public EldritchSpireTemplate(TemplateManager templateManager, Template template, String templateName,
                boolean fillBelow, BlockPos position, Rotation rot, Mirror mi) {
            
            super(0);
            if (template == null)
                throw new NullPointerException("Structure template is null (should be impossible)");
            
            name = templateName;
            fillBlocks = fillBelow;
            pos = position;
            PlacementSettings settings = new PlacementSettings().setIgnoreEntities(true).setRotation(rot).setMirror(mi);
            setup(template, pos, settings);
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
            setup(template, pos, settings);
        }
        
        @Override
        protected void handleDataMarker(String function, BlockPos pos, World world, Random rand,
                StructureBoundingBox sbb) {
            
            if (function.startsWith("autocaster")) {
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
            else if (function.startsWith("lock_impetus_")) {
                EnumFacing face = EnumFacing.NORTH;
                String dir = function.substring(13, 14);
                if (dir.equals("e"))
                    face = EnumFacing.EAST;
                else if (dir.equals("s"))
                    face = EnumFacing.SOUTH;
                else if (dir.equals("w"))
                    face = EnumFacing.WEST;
                
                IBlockState state = TABlocks.ELDRITCH_LOCK_IMPETUS.getDefaultState();
                state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, face);
                world.setBlockState(pos, state, 2);
            }
            else if (function.startsWith("lock_")) {
                EnumFacing face = EnumFacing.NORTH;
                String dir = function.substring(5, 6);
                if (dir.equals("e"))
                    face = EnumFacing.EAST;
                else if (dir.equals("s"))
                    face = EnumFacing.SOUTH;
                else if (dir.equals("w"))
                    face = EnumFacing.WEST;
                
                LockType lock = LockType.BOSS;
                String type = function.substring(6);
                if (type.equals("maze"))
                    lock = LockType.LABYRINTH;
                else if (type.equals("prison"))
                    lock = LockType.PRISON;
                else if (type.equals("library"))
                    lock = LockType.LIBRARY;
                
                IBlockState state = TABlocks.ELDRITCH_LOCK.getDefaultState();
                state = state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, face);
                state = state.withProperty(IEldritchLockType.LOCK_TYPE, lock);
                world.setBlockState(pos, state, 2);
            }
        }
        
    }
    
}
