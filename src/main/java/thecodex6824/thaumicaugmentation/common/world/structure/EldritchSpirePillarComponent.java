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

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thaumcraft.api.blocks.BlocksTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;

import javax.annotation.Nullable;
import java.util.UUID;

public class EldritchSpirePillarComponent extends EldritchSpireBaseComponent {

    protected PillarType type;
    
    // need default constructor for loading by minecraft
    public EldritchSpirePillarComponent() {
        super();
    }
    
    public EldritchSpirePillarComponent(TemplateManager templateManager, Template template, String templateName,
            boolean fillBelow, BlockPos position, Rotation rot, Mirror mi, UUID wardOwner, PillarType pillar) {
        
        super(templateManager, template, templateName, fillBelow, position, rot, mi, wardOwner);
        type = pillar;
    }
    
    public EldritchSpirePillarComponent(TemplateManager templateManager, String templateName, boolean fillBelow,
            BlockPos position, Rotation rot, Mirror mi, UUID wardOwner, PillarType pillar) {
        
        super(templateManager, templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, templateName)), templateName,
                fillBelow, position, rot, mi, wardOwner);
        type = pillar;
    }
    
    protected boolean isReplaceable(World world, BlockPos pos) {
        if (world.isAirBlock(pos))
            return true;
        else {
            IBlockState state = world.getBlockState(pos);
            if (state.getMaterial().isLiquid())
                return true;
            else if (state.getBlock() == TABlocks.STONE) {
                StoneType type = state.getValue(ITAStoneType.STONE_TYPE);
                return type == StoneType.STONE_VOID || type == StoneType.STONE_TAINT_NODECAY ||
                        type == StoneType.SOIL_STONE_TAINT_NODECAY;
            }
            else
                return false;
        }
    }
    
    @Override
    public void onPostGeneration(World world, StructureBoundingBox structurebb) {
        super.onPostGeneration(world, structurebb);
        if (structurebb.intersectsWith(boundingBox)) {
            boolean maxX = false, maxZ = false;
            Rotation rot = placeSettings.getRotation();
            switch (rot) {
                case NONE: {
                    maxX = true;
                    maxZ = true;
                    break;
                }
                case CLOCKWISE_90: {
                    maxZ = true;
                    break;
                }
                case COUNTERCLOCKWISE_90: {
                    maxX = true;
                    break;
                }
                default: break;
            }
            
            switch (placeSettings.getMirror()) {
                case FRONT_BACK: {
                    if (rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90)
                        maxZ = !maxZ;
                    else
                        maxX = !maxX;
                    
                    break;
                }
                case LEFT_RIGHT: {
                    if (rot == Rotation.NONE || rot == Rotation.CLOCKWISE_180)
                        maxZ = !maxZ;
                    else
                        maxX = !maxX;
                    
                    break;
                }
                default: break;
            }
            
            switch (type) {
                case GENERIC: {
                    int posX = maxX ? boundingBox.maxX - boundingBox.getXSize() / 2 : boundingBox.minX + boundingBox.getXSize() / 2;
                    int posZ = maxZ ? boundingBox.maxZ - boundingBox.getZSize() / 2 : boundingBox.minZ + boundingBox.getZSize() / 2;
                    MutableBlockPos mutable = new MutableBlockPos(posX, boundingBox.maxY, posZ);
                    BlockPos center = mutable.toImmutable();
                    for (int x = structurebb.minX; x <= structurebb.maxX; ++x) {
                        for (int z = structurebb.minZ; z <= structurebb.maxZ; ++z) {
                            mutable.setPos(x, boundingBox.maxY, z);
                            if (boundingBox.isVecInside(mutable) && !world.isAirBlock(mutable)) {
                                BlockPos distance = new BlockPos(Math.abs(mutable.getX() - center.getX()), 0,
                                        Math.abs(mutable.getZ() - center.getZ()));
                                int diff = 4 - (distance.getX() + distance.getZ());
                                if (diff >= 0) {
                                    IBlockState up = null;
                                    if (diff == 0) {
                                        if (distance.getX() == distance.getZ())
                                            up = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.ANCIENT_PILLAR);
                                        else if (distance.getX() > 0 && distance.getZ() > 0)
                                            up = BlocksTC.stoneAncient.getDefaultState();
                                        else
                                            continue;
                                    }
                                    else if (diff == 1 && (distance.getX() == 0 || distance.getZ() == 0))
                                        up = BlocksTC.stoneEldritchTile.getDefaultState();
                                    else
                                        up = BlocksTC.stoneAncient.getDefaultState();
                                    
                                    for (int y = boundingBox.maxY + 1; y < 256; ++y) {
                                        mutable.setY(y);
                                        if (!isReplaceable(world, mutable))
                                            break;
                    
                                        setWardedBlockState(world, mutable, up, 2);
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
                case MAZE: {
                    int posX = maxX ? boundingBox.maxX - boundingBox.getXSize() / 2 : boundingBox.minX + boundingBox.getXSize() / 2;
                    int posZ = maxZ ? boundingBox.maxZ - boundingBox.getZSize() / 2 : boundingBox.minZ + boundingBox.getZSize() / 2;
                    MutableBlockPos mutable = new MutableBlockPos(posX, boundingBox.maxY, posZ);
                    BlockPos center = mutable.toImmutable();
                    for (int x = structurebb.minX; x <= structurebb.maxX; ++x) {
                        for (int z = structurebb.minZ; z <= structurebb.maxZ; ++z) {
                            mutable.setPos(x, boundingBox.maxY, z);
                            if (!world.isAirBlock(mutable) && boundingBox.isVecInside(mutable)) {
                                BlockPos distance = new BlockPos(Math.abs(mutable.getX() - center.getX()), 0,
                                        Math.abs(mutable.getZ() - center.getZ()));
                                int diff = 2 - (distance.getX() + distance.getZ());
                                if (diff >= 0) {
                                    IBlockState up = null;
                                    if (diff == 0) {
                                        if (distance.getX() == 0 || distance.getZ() == 0)
                                            up = BlocksTC.stoneEldritchTile.getDefaultState();
                                        else
                                            up = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.ANCIENT_PILLAR);
                                    }
                                    else
                                        up = BlocksTC.stoneAncient.getDefaultState();
                                    
                                    for (int y = boundingBox.maxY + 1; y < 256; ++y) {
                                        mutable.setY(y);
                                        if (!isReplaceable(world, mutable))
                                            break;
                    
                                        setWardedBlockState(world, mutable, up, 2);
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
                case LIBRARY: {
                    MutableBlockPos mutable = new MutableBlockPos();
                    Vec3d center = new Vec3d(maxX ? boundingBox.maxX - boundingBox.getXSize() / 2.0 : boundingBox.minX + boundingBox.getXSize() / 2.0,
                            boundingBox.maxY, maxZ ? boundingBox.maxZ - boundingBox.getZSize() / 2.0 : boundingBox.minZ + boundingBox.getZSize() / 2.0);
                    for (int x = structurebb.minX; x <= structurebb.maxX; ++x) {
                        for (int z = structurebb.minZ; z <= structurebb.maxZ; ++z) {
                            mutable.setPos(x, boundingBox.maxY, z);
                            if (!world.isAirBlock(mutable) && boundingBox.isVecInside(mutable)) {
                                BlockPos distance = new BlockPos(Math.abs(mutable.getX() - center.x), 0,
                                        Math.abs(mutable.getZ() - center.z));
                                int diff = 2 - (distance.getX() + distance.getZ());
                                if (diff >= 0) {
                                    IBlockState up = null;
                                    if (diff == 0) {
                                        if (distance.getX() == 0 || distance.getZ() == 0)
                                            up = BlocksTC.stoneEldritchTile.getDefaultState();
                                        else
                                            up = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.ANCIENT_PILLAR);
                                    }
                                    else
                                        up = BlocksTC.stoneAncient.getDefaultState();
                                    
                                    for (int y = boundingBox.maxY + 1; y < 256; ++y) {
                                        mutable.setY(y);
                                        if (!isReplaceable(world, mutable))
                                            break;
                    
                                        setWardedBlockState(world, mutable, up, 2);
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
                default: break;
            }
        }
    }
    
    @Override
    protected void writeStructureToNBT(NBTTagCompound tag) {
        super.writeStructureToNBT(tag);
        tag.setInteger("pt", type.getID());
    }
    
    @Override
    protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager templateManager) {
        super.readStructureFromNBT(tag, templateManager);
        type = PillarType.fromID(tag.getInteger("pt"));
    }
    
    public enum PillarType {
        
        GENERIC(0),
        MAZE(1),
        LIBRARY(2);
        
        private final int id;
        
        PillarType(int i) {
            id = i;
        }
        
        public int getID() {
            return id;
        }
        
        @Nullable
        public static PillarType fromID(int id) {
            for (PillarType t : PillarType.values()) {
                if (t.getID() == id)
                    return t;
            }
            
            return null;
        }
        
    }
    
}
