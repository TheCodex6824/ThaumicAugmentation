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

package thecodex6824.thaumicaugmentation.common.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.projectile.EntityFocusCloud;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.entity.IDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.world.DimensionalFractureTeleporter;
import thecodex6824.thaumicaugmentation.common.world.feature.FractureUtils;

public class EntityDimensionalFracture extends Entity implements IDimensionalFracture {

    protected static final int OPEN_TIME = 360;
    
    protected static final DataParameter<Boolean> open = EntityDataManager.createKey(EntityDimensionalFracture.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Long> timeOpened = EntityDataManager.createKey(EntityDimensionalFracture.class, EntityUtil.SERIALIZER_LONG);
    
    protected int linkedDim;
    protected BlockPos linkedTo;
    protected boolean linkLocated;
    protected boolean linkInvalid;
    
    public EntityDimensionalFracture(World world) {
        super(world);
        setSize(1.0F, 3.0F);
    }
    
    protected void verifyChunk(World worldToVerify, BlockPos pos) {
        IChunkProvider provider = worldToVerify.getChunkProvider();
        if (!worldToVerify.isChunkGeneratedAt(pos.getX() >> 4, pos.getZ() >> 4)) {
            worldToVerify.getChunk(pos.add(16, 0, 0));
            worldToVerify.getChunk(pos.add(0, 0, 16));
            worldToVerify.getChunk(pos.add(16, 0, 16));
            worldToVerify.getChunk(pos);
        }
        else if (provider.getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) == null)
            worldToVerify.getChunk(pos);
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    
    protected void onCollide(Entity entity) {
        if (!world.isRemote && entity.timeUntilPortal == 0 && !entity.isRiding() && !entity.isBeingRidden() && entity.isNonBoss()) {
            if (isOpen()) {
                if (linkInvalid) {
                    if (world.getTotalWorldTime() % 20 == 0 && entity instanceof EntityPlayer)
                        ((EntityPlayer) entity).sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.no_fracture_target"), true);
                }
                else if (linkedTo != null) {
                    World targetWorld = null;
                    try {
                        targetWorld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(linkedDim);
                    }
                    catch (IllegalArgumentException ex) {}
                    if (!linkLocated || targetWorld == null) {
                        if (targetWorld != null) {
                            BlockPos toComplete = linkedTo;
                            verifyChunk(targetWorld, toComplete);
                            for (int y = targetWorld.getActualHeight() - 1; y >= 0; --y) {
                                BlockPos check = toComplete.add(0, y, 0);
                                for (EntityDimensionalFracture fracture : targetWorld.getEntitiesWithinAABB(EntityDimensionalFracture.class, new AxisAlignedBB(check))) {
                                    BlockPos yAdjusted = new BlockPos(fracture.getLinkedPosition().getX(), getPosition().getY(), fracture.getLinkedPosition().getZ());
                                    if (getEntityBoundingBox().intersects(new AxisAlignedBB(yAdjusted))) {
                                        fracture.open(true);
                                        linkedTo = check.down(2);
                                        linkLocated = true;
                                        break;
                                    }
                                }
                                
                                if (linkLocated)
                                    break;
                            }
                        }

                        if (!linkLocated || targetWorld == null) {
                            ThaumicAugmentation.getLogger().warn("A fracture is invalid, due to the destination lacking a fracture. This is probably a result of adding/removing dimensions. Recalculating fracture...");
                            ThaumicAugmentation.getLogger().debug("Dest dim: " + (targetWorld != null ? targetWorld.provider.getDimension() : "null"));
                            ThaumicAugmentation.getLogger().debug("Dest pos (not including y): " + linkedTo);
                            ThaumicAugmentation.getLogger().debug("Src pos: " + getPosition());
                            
                            FractureUtils.redoFractureLinkage(this);
                            targetWorld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(linkedDim);
                            BlockPos toComplete = linkedTo;
                            verifyChunk(targetWorld, toComplete);
                            for (int y = targetWorld.getActualHeight() - 1; y >= 0; --y) {
                                BlockPos check = toComplete.add(0, y, 0);
                                for (EntityDimensionalFracture fracture : targetWorld.getEntitiesWithinAABB(EntityDimensionalFracture.class, new AxisAlignedBB(check))) {
                                    BlockPos yAdjusted = new BlockPos(fracture.getLinkedPosition().getX(), getPosition().getY(), fracture.getLinkedPosition().getZ());
                                    if (getEntityBoundingBox().intersects(new AxisAlignedBB(yAdjusted))) {
                                        fracture.open(true);
                                        linkedTo = check.down(2);
                                        linkLocated = true;
                                        break;
                                    }
                                }
                                
                                if (linkLocated)
                                    break;
                            }
                            
                            if (!linkLocated) {
                                ThaumicAugmentation.getLogger().warn("Fracture relink failed. This is probably due the fracture in the void pointing to a new dimension.");
                                linkInvalid = true;
                                return;
                            }
                        }
                    }
                    
                    if (!targetWorld.getWorldBorder().contains(linkedTo)) {
                        if (world.getTotalWorldTime() % 20 == 0 && entity instanceof EntityPlayer)
                            ((EntityPlayer) entity).sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.no_fracture_target"), true);
                    }
                    else {
                        verifyChunk(targetWorld, linkedTo);
                        if (!TAConfig.fracturesAlwaysTeleport.getValue() && targetWorld.getEntitiesWithinAABB(EntityDimensionalFracture.class, new AxisAlignedBB(linkedTo)).isEmpty()) {
                            ThaumicAugmentation.getLogger().warn("A fracture is invalid, due to the destination lacking a fracture. This fracture has passed verification before, suggesting that either the destination fracture was removed or new linkable dimensions were introduced to the world.");
                            ThaumicAugmentation.getLogger().debug("Dest dim: " + targetWorld.provider.getDimension());
                            ThaumicAugmentation.getLogger().debug("Dest pos (not including y): " + linkedTo);
                            ThaumicAugmentation.getLogger().debug("Src pos: " + getPosition());
                            linkInvalid = true;
                        }
                        else {
                            entity = entity.changeDimension(targetWorld.provider.getDimension(), new DimensionalFractureTeleporter(linkedTo));
                            // we don't prevent people from teleporting without leaving the fracture area first, so we need a longer
                            // cooldown to allow people to casually leave it
                            entity.timeUntilPortal = Math.max(entity.getPortalCooldown(), 100);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote && getDataManager().get(open) && world.getTotalWorldTime() % 20 == 0 && world.getGameRules().getBoolean("doMobSpawning") && world.rand.nextInt(2000) < world.getDifficulty().getId()) {
            if (world.isBlockNormalCube(getPosition().down(), false) || world.isBlockNormalCube(getPosition().down(2), false)) {
                EntityEldritchGuardian guardian = new EntityEldritchGuardian(world);
                guardian.setLocationAndAngles(posX, posY, posZ, world.rand.nextInt(360), 0);
                guardian.setAbsorptionAmount(guardian.getAbsorptionAmount() + 
                        (float) guardian.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() / 2);
                guardian.timeUntilPortal = guardian.getPortalCooldown();
                world.spawnEntity(guardian);
            }
        }
        if (!world.isRemote) {
            RayTraceResult blockCheck = world.rayTraceBlocks(new Vec3d(posX, posY, posZ), new Vec3d(posX, posY + 2.0, posZ));
            if (blockCheck != null && blockCheck.getBlockPos() != null && !world.isAirBlock(blockCheck.getBlockPos())) {
                IBlockState state = world.getBlockState(blockCheck.getBlockPos());
                if (state.getBlockHardness(world, blockCheck.getBlockPos()) >= 0.0F && state.getBlock().canCollideCheck(state, false))
                    world.destroyBlock(blockCheck.getBlockPos(), false);
            }
            for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox())) {
                if (!entity.isDead && !(entity instanceof EntityFocusCloud) && !(entity instanceof EntityAreaEffectCloud))
                    onCollide(entity);
            }
            
            if (!isDead && ticksExisted % 300 == 0)
                playSound(SoundsTC.evilportal, 0.1F + rand.nextFloat() / 5.0F, 0.75F + rand.nextFloat() / 2.0F);
        }
    }
    
    @Override
    protected void entityInit() {
        getDataManager().register(open, false);
        getDataManager().register(timeOpened, Long.MAX_VALUE);
    }
    
    @Override
    public void setLinkedPosition(BlockPos pos) {
        linkedTo = pos;
    }

    @Override
    public BlockPos getLinkedPosition() {
        return linkedTo;
    }

    @Override
    public void setLinkedDimension(int dim) {
        linkedDim = dim;
    }

    @Override
    public int getLinkedDimension() {
        return linkedDim;
    }

    @Override
    public void setLinkLocated() {
        setLinkLocated(true);
    }
    
    @Override
    public void setLinkLocated(boolean located) {
        linkLocated = located;
    }

    @Override
    public boolean wasLinkLocated() {
        return linkLocated;
    }

    @Override
    public void setLinkInvalid() {
        setLinkInvalid(true);
    }

    @Override
    public void setLinkInvalid(boolean invalid) {
        linkInvalid = invalid;
    }
    
    @Override
    public boolean isLinkInvalid() {
        return linkInvalid;
    }

    @Override
    public void open() {
        open(false);
    }
    
    @Override
    public void open(boolean skipTransition) {
        getDataManager().set(open, true);
        getDataManager().set(timeOpened, world.getTotalWorldTime() - (skipTransition ? OPEN_TIME : 0));
    }
    
    @Override
    public int getOpeningDuration() {
        return OPEN_TIME;
    }
    
    @Override
    public void close() {
        getDataManager().set(open, false);
    }

    @Override
    public boolean isOpening() {
        return getDataManager().get(open) && world.getTotalWorldTime() < getDataManager().get(timeOpened) + OPEN_TIME;
    }
    
    @Override
    public boolean isOpen() {
        return getDataManager().get(open) && world.getTotalWorldTime() >= getDataManager().get(timeOpened) + OPEN_TIME;
    }
    
    @Override
    public long getTimeOpened() {
        return getDataManager().get(timeOpened);
    }
    
    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("linkedPos", NBT.TAG_INT_ARRAY)) {
            linkedDim = compound.getInteger("linkedDim");
            int[] pos = compound.getIntArray("linkedPos");
            linkedTo = new BlockPos(pos[0], pos[1], pos[2]);
            linkLocated = compound.getBoolean("linkLocated");
            linkInvalid = compound.getBoolean("linkInvalid");
            getDataManager().set(open, compound.getBoolean("open"));
            getDataManager().set(timeOpened, compound.getLong("timeOpened"));
        }
    }
    
    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (linkedTo != null) {
            compound.setInteger("linkedDim", linkedDim);
            compound.setIntArray("linkedPos", new int[] {linkedTo.getX(), linkedTo.getY(), linkedTo.getZ()});
            compound.setBoolean("linkLocated", linkLocated);
            compound.setBoolean("linkInvalid", linkInvalid);
            compound.setBoolean("open", getDataManager().get(open));
            compound.setLong("timeOpened", getDataManager().get(timeOpened));
        }
    }
    
    @Override
    public boolean hasNoGravity() {
        return true;
    }
    
    @Override
    public boolean isBurning() {
        return false;
    }
    
    @Override
    public boolean canRenderOnFire() {
        return false;
    }
    
    @Override
    public void setFire(int seconds) {}
    
    @Override
    public void move(MoverType type, double x, double y, double z) {}
    
}
