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

package thecodex6824.thaumicaugmentation.common.tile;

import com.google.common.base.Predicates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.block.property.IAltarBlock;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAEldritchGolem;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAEldritchWarden;
import thecodex6824.thaumicaugmentation.common.entity.IEldritchSpireWardHolder;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.world.structure.MapGenEldritchSpire;

import javax.annotation.Nullable;

public class TileAltar extends TileEntity implements ITickable, IInteractWithCaster {

    protected static final BlockPos[] CAP_OFFSETS = new BlockPos[] {
            new BlockPos(-1, 0, -3),
            new BlockPos(-3, 0, -1),
            new BlockPos(-3, 0, 1),
            new BlockPos(-1, 0, 3),
            new BlockPos(1, 0, 3),
            new BlockPos(3, 0, 1),
            new BlockPos(3, 0, -1),
            new BlockPos(1, 0, -3)
    };
    
    protected int openTicks;
    protected boolean research;
    
    public TileAltar() {
        super();
        openTicks = Integer.MIN_VALUE;
    }
    
    public void open() {
        openTicks = 280;
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }
    
    public boolean isOpen() {
        return openTicks != Integer.MIN_VALUE;
    }
    
    public int getOpenTicks() {
        return openTicks;
    }
    
    public boolean isStructureAltar() {
        return research;
    }
    
    public void setStructureAltar(boolean structure) {
        research = structure;
    }
    
    protected EntityLiving createBoss() {
        if (world.rand.nextBoolean())
            return new EntityTAEldritchGolem(world);
        else
            return new EntityTAEldritchWarden(world);
    }
    
    @Override
    @SuppressWarnings("null")
    public void update() {
        if (openTicks > 0)
            --openTicks;
        
        if (!world.isRemote) {
            if (research) {
                for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).grow(9.0, 3.0, 9.0),
                        Predicates.and(Predicates.notNull(), EntitySelectors.NOT_SPECTATING))) {
                    if (!ThaumcraftCapabilities.knowsResearchStrict(player, "m_BOSSROOM")) {
                        RayTraceResult result = world.rayTraceBlocks(player.getPositionEyes(1.0F),
                                new Vec3d(pos.getX() + 0.5, pos.getY() + 1.15, pos.getZ() + 0.5), false, false, false);
                        if (result == null) {
                            ThaumcraftCapabilities.getKnowledge(player).addResearch("m_BOSSROOM");
                            player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.boss_room_spire").setStyle(
                                    new Style().setColor(TextFormatting.DARK_PURPLE)), true);
                        }
                    }
                }
            }
            
            if (openTicks == 0) {
                BlockPos check = pos.up(2);
                IBlockState state = world.getBlockState(check);
                if (state.getBlock() == TABlocks.OBELISK)
                    world.destroyBlock(check, false);
                
                EntityLiving boss = createBoss();
                boss.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
                boss.rotationYawHead = boss.rotationYaw;
                boss.renderYawOffset = boss.rotationYaw;
                if (boss instanceof IEldritchSpireWardHolder) {
                    MapGenStructureData data = (MapGenStructureData) world.getPerWorldStorage().getOrLoadData(MapGenStructureData.class, "EldritchSpire");
                    if (data != null) {
                        NBTTagCompound nbt = data.getTagCompound();
                        for (String s : nbt.getKeySet()) {
                            NBTTagCompound tag = nbt.getCompoundTag(s);
                            if (tag.hasKey("ChunkX", NBT.TAG_INT) && tag.hasKey("ChunkZ", NBT.TAG_INT)) {
                                StructureStart start = MapGenStructureIO.getStructureStart(tag, world);
                                if (start instanceof MapGenEldritchSpire.Start && start.getBoundingBox().isVecInside(pos)) {
                                    ((IEldritchSpireWardHolder) boss).setStructurePos(new DimensionalBlockPos(
                                            new BlockPos(start.getChunkPosX() << 4, 0, start.getChunkPosZ() << 4), world.provider.getDimension()));
                                }
                            }
                        }
                    }
                }
                
                boss.onInitialSpawn(world.getDifficultyForLocation(pos), null);
                if (world.spawnEntity(boss)) {
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.playSound(null, pos, TASounds.ALTAR_SUMMON, SoundCategory.BLOCKS, 1.5F, 1.0F);
                    TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.EXPLOSION, pos.getX() + 0.5,
                            pos.getY(), pos.getZ() + 0.5), boss);
                }
                
                world.destroyBlock(pos, false);
            }
            else if (openTicks <= 140 && openTicks > 60 && openTicks % 10 == 0) {
                BlockPos check = pos.add(CAP_OFFSETS[openTicks / 10 - 7]);
                IBlockState state = world.getBlockState(check);
                if (state.getBlock() == TABlocks.CAPSTONE && !state.getValue(IAltarBlock.ALTAR))
                    world.destroyBlock(check, false);
            }
            else if (openTicks == 175)
                world.playSound(null, pos, SoundsTC.shock, SoundCategory.BLOCKS, 0.5F, 0.85F);
            else if (openTicks == 250)
                world.playSound(null, pos, SoundsTC.evilportal, SoundCategory.BLOCKS, 0.25F, 1.0F);
        }
    }
    
    @Override
    public boolean onCasterRightClick(World world, ItemStack stack, EntityPlayer player,
            BlockPos pos, EnumFacing facing, EnumHand hand) {
        
        if (!world.isRemote && world.getDifficulty() != EnumDifficulty.PEACEFUL) {
            open();
            world.playSound(null, pos, SoundsTC.wand, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.playSound(null, pos, TASounds.ALTAR_SUMMON_START, SoundCategory.BLOCKS, 1.5F, 1.0F);
        }
        
        return true;
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock() || !newState.getValue(IAltarBlock.ALTAR);
    }
    
    @Override
    public double getMaxRenderDistanceSquared() {
        return 16384.0;
    }
    
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos).grow(1.0, 0.0, 1.0).expand(0.0, 2.0, 0.0);
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setInteger("openTicks", openTicks);
        return tag;
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("openTicks", openTicks);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        openTicks = pkt.getNbtCompound().getInteger("openTicks");
        world.markBlockRangeForRenderUpdate(pos, pos);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("openTicks", openTicks);
        compound.setBoolean("structureAltar", research);
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        openTicks = compound.getInteger("openTicks");
        research = compound.getBoolean("structureAltar");
    }
    
}
