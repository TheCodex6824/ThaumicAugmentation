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
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thaumcraft.api.blocks.BlocksTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.ward.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorageServer;

import java.util.UUID;

public class EldritchSpireBaseComponent extends EldritchSpireComponent {

    // need default constructor for loading by minecraft
    public EldritchSpireBaseComponent() {
        super();
    }
    
    public EldritchSpireBaseComponent(TemplateManager templateManager, Template template, String templateName,
            boolean fillBelow, BlockPos position, Rotation rot, Mirror mi, UUID wardOwner) {
        
        super(templateManager, template, templateName, fillBelow, position, rot, mi, wardOwner);
    }
    
    public EldritchSpireBaseComponent(TemplateManager templateManager, String templateName, boolean fillBelow,
            BlockPos position, Rotation rot, Mirror mi, UUID wardOwner) {
        
        super(templateManager, templateManager.get(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(ThaumicAugmentationAPI.MODID, templateName)), templateName,
                fillBelow, position, rot, mi, wardOwner);
    }
    
    protected boolean isEldritchBlock(IBlockState state) {
        return state.getBlock() == BlocksTC.stoneEldritchTile || state.getBlock() == BlocksTC.slabEldritch ||
                state.getBlock() == BlocksTC.doubleSlabEldritch || state.getBlock() == TABlocks.STAIRS_ELDRITCH_TILE;
    }
    
    protected void setWardedBlockState(World world, BlockPos pos, IBlockState toPlace, int flags) {
        world.setBlockState(pos, toPlace, flags);
        IWardStorage storage = world.getChunk(pos).getCapability(CapabilityWardStorage.WARD_STORAGE, null);
        if (storage instanceof IWardStorageServer)
            ((IWardStorageServer) storage).setWard(pos, ward, world);
    }
    
    @Override
    public void onPostGeneration(World world, StructureBoundingBox structurebb) {
        if (structurebb.intersectsWith(boundingBox)) {
            int minY = boundingBox.minY;
            MutableBlockPos mutable = new MutableBlockPos();
            for (int x = structurebb.minX; x <= structurebb.maxX; ++x) {
                for (int z = structurebb.minZ; z <= structurebb.maxZ; ++z) {
                    mutable.setPos(x, minY, z);
                    if (boundingBox.isVecInside(mutable) && !world.isAirBlock(mutable)) {
                        IBlockState place = BlocksTC.stoneAncient.getDefaultState();
                        IBlockState above = world.getBlockState(mutable);
                        if (isEldritchBlock(above))
                            place = BlocksTC.stoneEldritchTile.getDefaultState();
                
                        for (int y = minY - 1; y >= 0; --y) {
                            mutable.setY(y);
                            if (!world.isAirBlock(mutable) && !world.getBlockState(mutable).getMaterial().isLiquid())
                                break;
        
                            setWardedBlockState(world, mutable, place, 2);
                        }
                    }
                }
            }
        }
    }
    
}
