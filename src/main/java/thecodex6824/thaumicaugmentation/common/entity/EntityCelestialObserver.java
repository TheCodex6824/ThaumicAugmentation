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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;

import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.common.entity.ai.EntityLookHelperUnlimitedPitch;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;
import thecodex6824.thaumicaugmentation.init.GUIHandler.TAInventory;

public class EntityCelestialObserver extends EntityCreature implements IEntityOwnable {

    protected static final DataParameter<Optional<UUID>> OWNER_ID = EntityDataManager.createKey(EntityCelestialObserver.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Byte> SCANS = EntityDataManager.createKey(EntityCelestialObserver.class, DataSerializers.BYTE);
    
    @Nonnull
    protected static final ItemStack PAPER = new ItemStack(Items.PAPER);
    
    protected WeakReference<Entity> ownerRef;
    protected int[] lastScanTimes;
    protected ItemStackHandler inventory;
    
    public EntityCelestialObserver(World world) {
        super(world);
        lookHelper = new EntityLookHelperUnlimitedPitch(this, false);
        ownerRef = new WeakReference<>(null);
        lastScanTimes = new int[6];
        Arrays.fill(lastScanTimes, -1);
        setSize(1.0F, 1.0F);
        height = 2.0F;
        inventory = new ItemStackHandler(19) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == 0)
                    return super.isItemValid(slot, stack) && OreDictionary.itemMatches(PAPER, stack, false);
                else
                    return false;
            }
        };
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(13.0);
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OWNER_ID, Optional.absent());
        dataManager.register(SCANS, (byte) 7);
    }
    
    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAILookAtScan());
        tasks.addTask(1, new EntityAILookAtCelestialBody());
    }
    
    protected boolean hasPaper() {
        boolean paper = false;
        for (int i = 0; i < inventory.getSlots(); ++i) {
            if (OreDictionary.itemMatches(PAPER, inventory.getStackInSlot(i), false)) {
                paper = true;
                break;
            }
        }
        
        return paper;
    }
    
    protected boolean consumePaper() {
        boolean paper = false;
        for (int i = 0; i < inventory.getSlots(); ++i) {
            if (OreDictionary.itemMatches(PAPER, inventory.getStackInSlot(i), false) &&
                    OreDictionary.itemMatches(PAPER, inventory.extractItem(i, 1, false), false)) {
                
                paper = true;
                break;
            }
        }
        
        return paper;
    }
    
    protected boolean tryInsert(@Nonnull ItemStack toInsert, EnumFacing side) {
        TileEntity test = world.getTileEntity(getPosition().offset(side));
        if (test != null) {
            IItemHandler other = test.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
            if (other != null && ItemHandlerHelper.insertItem(other, toInsert, false).isEmpty())
                return true;
        }
        
        return false;
    }
    
    @SuppressWarnings("null")
    protected boolean checkOrBypassResearch(String key, int cacheIndex, int day) {
        if (!dataManager.get(OWNER_ID).isPresent())
            return true;
        
        NBTTagCompound writeback = null;
        IPlayerKnowledge cap = null;
        Entity owner = getOwner();
        if (owner == null && TAConfig.allowOfflinePlayerResearch.getValue()) {
            writeback = ThaumicAugmentation.proxy.getOfflinePlayerNBT(dataManager.get(OWNER_ID).get());
            if (writeback == null)
                return true;
            
            cap = ThaumcraftCapabilities.KNOWLEDGE.getDefaultInstance();
            if (cap != null)
                cap.deserializeNBT(writeback.getCompoundTag("ForgeCaps").getCompoundTag("thaumcraft:knowledge"));
        }
        else if (owner != null)
            cap = owner.getCapability(ThaumcraftCapabilities.KNOWLEDGE, null);
        
        if (cap == null)
            return true;
        
        if (cap.isResearchKnown(key)) {
            lastScanTimes[cacheIndex] = (int) (world.getTotalWorldTime() / 24000);
            return false;
        }
        else {
            cap.addResearch(key);
            ArrayList<String> list = new ArrayList<>();
            for (String k : cap.getResearchList()) {
                if (k.startsWith("CEL_") && !k.startsWith("CEL_" + day))
                    list.add(k); 
            } 
            for (String k : list)
                cap.removeResearch(k); 
            
            if (writeback != null) {
                writeback.getCompoundTag("ForgeCaps").setTag("thaumcraft:knowledge", cap.serializeNBT());
                ThaumicAugmentation.proxy.saveOfflinePlayerNBT(dataManager.get(OWNER_ID).get(), writeback);
            }
            else if (owner instanceof EntityPlayerMP)
                cap.sync((EntityPlayerMP) owner);
            
            return true;
        }
    }
    
    public boolean isDisabled() {
        if (world.isBlockPowered(getPosition()))
            return true;
        else {
            IBlockState rail = world.getBlockState(getPosition().down());
            if (rail.getBlock() == BlocksTC.activatorRail && rail.getValue(BlockRailPowered.POWERED).booleanValue())
                return true;
        }
        
        return false;
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            rotationYaw = rotationYawHead = MathHelper.wrapDegrees(rotationYawHead);
            prevRotationYaw = prevRotationYawHead;
            if (!isDisabled()) {
                if (ticksExisted % 20 == 0) {
                    if (ticksExisted % 120 == 0)
                        heal(1.0F);
                    
                    boolean filled = false;
                    TileEntity test = world.getTileEntity(getPosition());
                    if (test != null) {
                        IItemHandler other = test.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                        if (other != null) {
                            for (int i = 0; i < other.getSlots(); ++i) {
                                ItemStack contained = other.getStackInSlot(i);
                                if (OreDictionary.itemMatches(PAPER, contained, false)) {
                                    ItemStack result = inventory.insertItem(0, contained, true);
                                    if (result != contained) {
                                        ItemStack extract = other.extractItem(i, inventory.getSlotLimit(0) - inventory.getStackInSlot(0).getCount(), false);
                                        ItemStack remain = inventory.insertItem(0, extract, false);
                                        if (!remain.isEmpty())
                                            other.insertItem(i, remain, false);
                                        
                                        filled = true;
                                        world.playSound(null, getPosition(), SoundsTC.page, SoundCategory.NEUTRAL, 0.5F, 1.0F);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    if (!filled && (posY - Math.floor(posY)) < 0.51) {
                        test = world.getTileEntity(getPosition().down());
                        if (test != null) {
                            IItemHandler other = test.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                            if (other != null) {
                                for (int i = 0; i < other.getSlots(); ++i) {
                                    ItemStack contained = other.getStackInSlot(i);
                                    if (OreDictionary.itemMatches(PAPER, contained, false)) {
                                        ItemStack result = inventory.insertItem(0, contained, true);
                                        if (result != contained) {
                                            ItemStack extract = other.extractItem(i, inventory.getSlotLimit(0) - inventory.getStackInSlot(0).getCount(), false);
                                            ItemStack remain = inventory.insertItem(0, extract, false);
                                            if (!remain.isEmpty())
                                                other.insertItem(i, remain, false);
                                            
                                            filled = true;
                                            world.playSound(null, getPosition(), SoundsTC.page, SoundCategory.NEUTRAL, 0.5F, 1.0F);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    ArrayList<IItemHandler> outputs = new ArrayList<>();
                    for (EnumFacing f : EnumFacing.HORIZONTALS) {
                        test = world.getTileEntity(getPosition().offset(f));
                        if (test != null) {
                            IItemHandler other = test.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite());
                            if (other != null)
                                outputs.add(other);
                        }
                    }
                    
                    if (!outputs.isEmpty()) {
                        for (int slot = 1; slot < inventory.getSlots(); ++slot) {
                            ItemStack contained = inventory.getStackInSlot(slot);
                            if (!contained.isEmpty()) {
                                for (IItemHandler output : outputs) {
                                    if (ItemHandlerHelper.insertItem(output, contained, true) != contained) {
                                        ItemStack out = inventory.extractItem(slot, contained.getCount(), false);
                                        ItemStack remain = ItemHandlerHelper.insertItem(output, out, false);
                                        if (!remain.isEmpty()) {
                                            inventory.insertItem(slot, remain, false);
                                            contained = inventory.getStackInSlot(slot);
                                        }
                                        else
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
                else if (!isAIDisabled() && ticksExisted % 50 == 0 && hasPaper() && !world.isRainingAt(new BlockPos(getLookVec().add(posX, posY + 1.0, posZ))) &&
                        world.canSeeSky(new BlockPos(getLookVec().add(posX, posY + 1.0, posZ))) &&
                        world.provider.getDimensionType() == DimensionType.OVERWORLD) {
                    
                    float y = (rotationYaw + 90.0F) % 360.0F;
                    float x = -rotationPitch;
                    float angle = ((world.getCelestialAngle(0.0F) + 0.25F) * 360.0F) % 360.0F;
                    boolean night = angle > 180;
                    if ((!night && angle > 15.0F) || (night && angle > 195.0F)) {
                        boolean inRangeYaw = false;
                        boolean inRangePitch = false;
                        if (night)
                            angle -= 180.0F;
                        if (angle > 90) {
                            inRangeYaw = Math.abs(Math.abs(y) - 180) < 1.5F;
                            inRangePitch = Math.abs(180 - angle - x) < 1.5F;
                        }
                        else {
                            inRangeYaw = Math.abs(y) < 1.5F;
                            inRangePitch = Math.abs(angle - x) < 1.5F;
                        } 
                        
                        boolean scanned = false;
                        if (inRangeYaw && inRangePitch && (night ? getScanMoon() : getScanSun())) {
                            int day = (int) (world.getTotalWorldTime() / 24000);
                            int meta = night ? (5 + world.provider.getMoonPhase(world.getWorldTime())) : 0;
                            ItemStack toMake = new ItemStack(ItemsTC.celestialNotes, 1, meta);
                            if (lastScanTimes[night ? 1 : 0] != day) {
                                if (ItemHandlerHelper.insertItem(inventory, toMake, true).isEmpty() &&
                                        checkOrBypassResearch("CEL_" + day + "_" + (night ? "Moon" + (meta - 5) : "Sun"), night ? 1 : 0, day)) {
                                    
                                    ItemHandlerHelper.insertItem(inventory, toMake, false);
                                    consumePaper();
                                    lastScanTimes[night ? 1 : 0] = day;
                                    world.playSound(null, getPosition(), SoundsTC.scan, SoundCategory.NEUTRAL, 0.5F, 0.8F);
                                    scanned = true;
                                }
                            }
                        }
                        
                        if (night && !scanned && getScanStars()) {
                            EnumFacing face = getAdjustedHorizontalFacing();
                            int day = (int) (world.getTotalWorldTime() / 24000);
                            int meta = face.getIndex() - 1;
                            ItemStack toMake = new ItemStack(ItemsTC.celestialNotes, 1, meta);
                            if (lastScanTimes[face.getIndex()] != day && Math.abs(MathHelper.wrapDegrees(rotationYaw - face.getHorizontalAngle())) < 1.5F) {
                                if (ItemHandlerHelper.insertItem(inventory, toMake, true).isEmpty() &&
                                        checkOrBypassResearch("CEL_" + day + "_Star" + (meta - 1), face.getIndex(), day)) {
                                    
                                    ItemHandlerHelper.insertItem(inventory, toMake, false);
                                    consumePaper();
                                    lastScanTimes[face.getIndex()] = day;
                                    world.playSound(null, getPosition(), SoundsTC.scan, SoundCategory.NEUTRAL, 0.5F, 0.8F);
                                    scanned = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    protected void updateEntityActionState() {
        if (isDisabled()) {
            Vec3d base = new Vec3d(posX + 0.35, posY + 0.35, posZ);
            lookHelper.setLookPosition(base.x, base.y, base.z, getHorizontalFaceSpeed(), getVerticalFaceSpeed());
            lookHelper.onUpdateLook();
        }
        else
            super.updateEntityActionState();
    }
    
    @Override
    @Nullable
    public Entity getOwner() {
        if (ownerRef.get() == null && dataManager.get(OWNER_ID).isPresent()) {
            List<Entity> entities = world.getEntities(Entity.class, entity -> entity != null && entity.getPersistentID().equals(dataManager.get(OWNER_ID).get()));
            if (!entities.isEmpty())
                ownerRef = new WeakReference<>(entities.get(0));
            else {
                List<? extends EntityPlayer> players;
                if (!world.isRemote)
                    players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
                else
                    players = world.getPlayers(EntityPlayer.class, Predicates.alwaysTrue());
                
                for (EntityPlayer p : players) {
                    if (p.getUniqueID().equals(dataManager.get(OWNER_ID).get())) {
                        ownerRef = new WeakReference<>(p);
                        break;
                    }
                }
            }
        }
        
        return ownerRef.get();
    }
    
    @Override
    @Nullable
    public UUID getOwnerId() {
        return dataManager.get(OWNER_ID).orNull();
    }
    
    public void setOwner(Entity newOwner) {
        dataManager.set(OWNER_ID, Optional.of(newOwner.getPersistentID()));
        ownerRef = new WeakReference<>(newOwner);
    }
    
    @Override
    public boolean isOnSameTeam(Entity other) {
        Entity owner = getOwner();
        if (other.equals(owner))
            return true;
        else if (owner != null && other.isOnSameTeam(getOwner()))
            return true;
        else
            return super.isOnSameTeam(other);
    }
    
    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }
    
    @Override
    public float getEyeHeight() {
        return 1.2F;
    }
    
    @Override
    protected boolean canDespawn() {
        return false;
    }
    
    @Override
    public int getTalkInterval() {
        return 240;
    }
    
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundsTC.clack;
    }
    
    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundsTC.clack;
    }
    
    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundsTC.tool;
    }
    
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
    
    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }
    
    @Override
    public boolean isPotionApplicable(PotionEffect potioneffectIn) {
        return false;
    }
    
    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(TAItems.CELESTIAL_OBSERVER_PLACER);
    }
    
    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && !isDead) {
            if (player.equals(getOwner())) {
                if (player.isSneaking()) {
                    playSound(SoundsTC.zap, 1.0F, 1.0F);
                    entityDropItem(new ItemStack(TAItems.CELESTIAL_OBSERVER_PLACER), 0.5F);
                    for (int i = 0; i < inventory.getSlots(); ++i)
                        entityDropItem(inventory.extractItem(i, inventory.getSlotLimit(i), false), 0.5F);
                    
                    setDead();
                    player.swingArm(hand);
                    return true;
                }
                else {
                    player.openGui(ThaumicAugmentation.instance, TAInventory.CELESTIAL_OBSERVER.getID(), world, getEntityId(), 0, 0);
                    return true;
                }
            }
            else {
                player.sendStatusMessage(new TextComponentTranslation("tc.notowned").setStyle(
                        new Style().setColor(TextFormatting.DARK_PURPLE).setItalic(true)), true);
                return false;
            }
        }
        else
            return super.processInteract(player, hand);
    }
    
    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!world.isRemote) {
            for (int i = 0; i < inventory.getSlots(); ++i)
                entityDropItem(inventory.extractItem(i, inventory.getSlotLimit(i), false), 0.5F);
        }
    }
    
    @Override
    public int getVerticalFaceSpeed() {
        return 1;
    }
    
    @Override
    public int getHorizontalFaceSpeed() {
        return 2;
    }
    
    @Override
    public void knockBack(Entity entity, float strength, double xRatio, double zRatio) {
        super.knockBack(entity, strength / 10.0F, xRatio, zRatio);
    }
    
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 || pass == 1;
    }
    
    public void setLastScanTimes(int[] times) {
        lastScanTimes = Arrays.copyOf(times, lastScanTimes.length);
    }
    
    public int[] getLastScanTimes() {
        return lastScanTimes;
    }
    
    public void setScanSun(boolean scan) {
        dataManager.set(SCANS, (byte) BitUtil.setOrClearBit(dataManager.get(SCANS), 0, scan));
    }
    
    public boolean getScanSun() {
        return BitUtil.isBitSet(dataManager.get(SCANS), 0);
    }
    
    public void setScanMoon(boolean scan) {
        dataManager.set(SCANS, (byte) BitUtil.setOrClearBit(dataManager.get(SCANS), 1, scan));
    }
    
    public boolean getScanMoon() {
        return BitUtil.isBitSet(dataManager.get(SCANS), 1);
    }
    
    public void setScanStars(boolean scan) {
        dataManager.set(SCANS, (byte) BitUtil.setOrClearBit(dataManager.get(SCANS), 2, scan));
    }
    
    public boolean getScanStars() {
        return BitUtil.isBitSet(dataManager.get(SCANS), 2);
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (dataManager.get(OWNER_ID).isPresent())
            compound.setUniqueId("owner", dataManager.get(OWNER_ID).get());
        
        compound.setTag("inventory", inventory.serializeNBT());
        compound.setIntArray("scanTimes", lastScanTimes);
        compound.setByte("scans", dataManager.get(SCANS));
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("owner"))
            dataManager.set(OWNER_ID, Optional.of(compound.getUniqueId("owner")));
        
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        lastScanTimes = compound.getIntArray("scanTimes");
        if (lastScanTimes.length != 6)
            lastScanTimes = new int[6];
        
        dataManager.set(SCANS, compound.getByte("scans"));
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
                super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        else
            return super.getCapability(capability, facing);
    }
    
    protected class EntityAILookAtCelestialBody extends EntityAIBase {
        
        public EntityAILookAtCelestialBody() {
            setMutexBits(2);
        }
        
        @Override
        public boolean shouldExecute() {
            return !isDisabled() && !world.isRainingAt(new BlockPos(getLookVec().add(posX, posY + 1.0, posZ)));
        }
        
        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }
        
        @Override
        public void startExecuting() {
            float angle = ((world.getCelestialAngle(0.0F) + 0.25F) * 360.0F) % 180.0F;
            Vec3d target = getVectorForRotation(-angle, 270).scale(48.0F);
            getLookHelper().setLookPosition(posX + target.x, posY + target.y, posZ + target.z,
                    getHorizontalFaceSpeed(), getVerticalFaceSpeed());
        }
        
    }
    
    protected class EntityAILookAtScan extends EntityAIBase {
        
        protected int currentTask = -1;
        
        public EntityAILookAtScan() {
            setMutexBits(2);
        }
        
        @Override
        public boolean shouldExecute() {
            if (!isDisabled() && !world.isRainingAt(new BlockPos(getLookVec().add(posX, posY + 1.0, posZ)))) {
                int day = (int) (world.getTotalWorldTime() / 24000);
                float angle = (world.getCelestialAngle(0.0F) + 0.25F) * 360.0F % 360.0F;
                if (angle > 195.0F) {
                    for (int i = 1; i < lastScanTimes.length; ++i) {
                        if (lastScanTimes[i] != day && (i == 1 ? getScanMoon() : getScanStars()))
                            return true;
                    }
                    
                    return false;
                }
                else if (angle > 15.0F && getScanSun())
                    return lastScanTimes[0] != day;
            }
            
            return false;
        }
        
        @Override
        public boolean shouldContinueExecuting() {
            if (!isDisabled()) {
                int day = (int) (world.getTotalWorldTime() / 24000);
                return currentTask != -1 && lastScanTimes[currentTask] != day;
            }
            
            return false;
        }
        
        @Override
        public void resetTask() {
            currentTask = -1;
        }
        
        @Override
        public void startExecuting() {
            int day = (int) (world.getTotalWorldTime() / 24000);
            float angle = (world.getCelestialAngle(0.0F) + 0.25F) * 360.0F % 360.0F;
            if (angle > 195.0F) {
                for (int i = 1; i < lastScanTimes.length; ++i) {
                    if (lastScanTimes[i] != day && (i == 1 ? getScanMoon() : getScanStars())) {
                        currentTask = i;
                        break;
                    }
                }
            }
            else if (angle > 15.0F && lastScanTimes[0] != day && getScanSun())
                currentTask = 0;
        }
        
        @Override
        public void updateTask() {
            switch (currentTask) {
                case 0:
                case 1: {
                    float angle = ((world.getCelestialAngle(0.0F) + 0.25F) * 360.0F) % 180.0F;
                    Vec3d target = getVectorForRotation(-angle, 270).scale(48.0F);
                    getLookHelper().setLookPosition(posX + target.x, posY + target.y, posZ + target.z,
                            getHorizontalFaceSpeed(), getVerticalFaceSpeed());
                    break;
                }
                case 2:
                case 3:
                case 4:
                case 5: {
                    EnumFacing f = EnumFacing.byIndex(currentTask);
                    getLookHelper().setLookPosition(posX + f.getXOffset() * 128.0F, posY + 64.0F, posZ + f.getZOffset() * 128.0F,
                            getHorizontalFaceSpeed(), getVerticalFaceSpeed());
                    break;
                }
                default: {
                    resetTask();
                    break;
                }
            }
        }
        
    }
    
}
