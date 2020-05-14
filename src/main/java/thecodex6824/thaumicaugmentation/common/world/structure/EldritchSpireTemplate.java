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

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import thecodex6824.thaumicaugmentation.api.warded.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.storage.IWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.storage.IWardStorageServer;

/**
 * It's time for another Thaumic Augmentation wrapper class, yay!
 * We need to have much more control over the block placement process than what
 * is offered by the default Template class. Namely:
 * - Know exactly which blocks get placed so they can be warded
 * - Support per-template block processors in a clean way
 */
public class EldritchSpireTemplate extends Template {

    protected Template wrapped;
    
    public EldritchSpireTemplate(Template wrappedTemplate) {
        wrapped = wrappedTemplate;
    }
    
    @Override
    public void addBlocksToWorld(World world, BlockPos pos, @Nullable ITemplateProcessor templateProcessor,
            PlacementSettings placement, int flags) {
        
        addBlocksToWorld(world, pos, new VanillaToAdvancedTemplateProcessor(templateProcessor),
                placement, flags, IWardStorageServer.NIL_UUID);
    }
    
    public void addBlocksToWorld(World world, BlockPos pos, IAdvancedTemplateProcessor templateProcessor,
            PlacementSettings placement, int flags, UUID ward) {
        
        BlockPos size = wrapped.getSize();
        if ((!wrapped.blocks.isEmpty() || !placement.getIgnoreEntities() && !wrapped.entities.isEmpty()) &&
                size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
            
            Block replaced = placement.getReplacedBlock();
            StructureBoundingBox bb = placement.getBoundingBox();
            for (Template.BlockInfo info : wrapped.blocks) {
                BlockPos blockpos = transformedBlockPos(placement, info.pos).add(pos);
                if (bb != null && !bb.isVecInside(blockpos))
                    continue;
                
                info = templateProcessor.processBlock(world, blockpos, info);
                if (info != null) {
                    Block block = info.blockState.getBlock();
                    if ((replaced == null || replaced != block) && (!placement.getIgnoreStructureBlock() || block != Blocks.STRUCTURE_BLOCK) &&
                            (bb == null || bb.isVecInside(blockpos))) {
                        
                        IBlockState state = info.blockState.withMirror(placement.getMirror());
                        state = state.withRotation(placement.getRotation());
                        if (info.tileentityData != null) {
                            TileEntity tile = world.getTileEntity(blockpos);
                            if (tile != null) {
                                if (tile instanceof IInventory)
                                    ((IInventory) tile).clear();

                                world.setBlockState(blockpos, Blocks.BARRIER.getDefaultState(), 4);
                            }
                        }

                        if (world.setBlockState(blockpos, state, flags)) {
                            if (info.tileentityData != null) {
                                TileEntity tile = world.getTileEntity(blockpos);
                                if (tile != null) {
                                    info.tileentityData.setInteger("x", blockpos.getX());
                                    info.tileentityData.setInteger("y", blockpos.getY());
                                    info.tileentityData.setInteger("z", blockpos.getZ());
                                    tile.readFromNBT(info.tileentityData);
                                    tile.mirror(placement.getMirror());
                                    tile.rotate(placement.getRotation());
                                }
                            }
                            
                            if (!ward.equals(IWardStorageServer.NIL_UUID) &&
                                    templateProcessor.shouldBlockBeWarded(world, pos, info)) {
                                
                                // we don't know if the config will allow warded tiles always,
                                // because it can change after generation
                                // so we assume the strictest setting of no wards on tiles
                                IWardStorage storage = world.getChunk(blockpos).getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                                if (storage instanceof IWardStorageServer)
                                    ((IWardStorageServer) storage).setWard(world, blockpos, ward);
                            }
                        }
                    }
                }
            }

            for (Template.BlockInfo info : wrapped.blocks) {
                if (replaced == null || replaced != info.blockState.getBlock()) {
                    BlockPos position = transformedBlockPos(placement, info.pos).add(pos);
                    if (bb == null || bb.isVecInside(position)) {
                        world.notifyNeighborsRespectDebug(position, info.blockState.getBlock(), false);
                        if (info.tileentityData != null) {
                            TileEntity tile = world.getTileEntity(position);
                            if (tile != null)
                                tile.markDirty();
                        }
                    }
                }
            }

            if (!placement.getIgnoreEntities())
                wrapped.addEntitiesToWorld(world, pos, placement.getMirror(), placement.getRotation(), bb);
        }
    }
    
    @Override
    public BlockPos calculateConnectedPos(PlacementSettings placementIn, BlockPos p_186262_2_,
            PlacementSettings p_186262_3_, BlockPos p_186262_4_) {
        
        return wrapped.calculateConnectedPos(placementIn, p_186262_2_, p_186262_3_, p_186262_4_);
    }
    
    @Override
    public String getAuthor() {
        return wrapped.getAuthor();
    }
    
    @Override
    public Map<BlockPos, String> getDataBlocks(BlockPos pos, PlacementSettings placementIn) {
        return wrapped.getDataBlocks(pos, placementIn);
    }
    
    @Override
    public BlockPos getSize() {
        return wrapped.getSize();
    }
    
    @Override
    public BlockPos getZeroPositionWithTransform(BlockPos p_189961_1_, Mirror p_189961_2_, Rotation p_189961_3_) {
        return wrapped.getZeroPositionWithTransform(p_189961_1_, p_189961_2_, p_189961_3_);
    }
    
    @Override
    public void read(NBTTagCompound compound) {
        wrapped.read(compound);
    }
    
    @Override
    public void setAuthor(String authorIn) {
        wrapped.setAuthor(authorIn);
    }
    
    @Override
    public void takeBlocksFromWorld(World worldIn, BlockPos startPos, BlockPos size, boolean takeEntities,
            @Nullable Block toIgnore) {
        
        wrapped.takeBlocksFromWorld(worldIn, startPos, size, takeEntities, toIgnore);
    }
    
    @Override
    public BlockPos transformedSize(Rotation rotationIn) {
        return wrapped.transformedSize(rotationIn);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        return wrapped.writeToNBT(nbt);
    }
    
    public static class VanillaToAdvancedTemplateProcessor implements IAdvancedTemplateProcessor {
        
        protected ITemplateProcessor wrap;
        
        public VanillaToAdvancedTemplateProcessor(@Nullable ITemplateProcessor vanilla) {
            wrap = vanilla;
        }
        
        @Override
        @Nullable
        public BlockInfo processBlock(World world, BlockPos pos, BlockInfo blockInfo) {
            return wrap != null ? wrap.processBlock(world, pos, blockInfo) : blockInfo;
        }
        
        @Override
        public boolean shouldBlockBeWarded(World world, BlockPos pos, BlockInfo blockInfo) {
            return blockInfo.tileentityData == null;
        }
        
    }
    
}
