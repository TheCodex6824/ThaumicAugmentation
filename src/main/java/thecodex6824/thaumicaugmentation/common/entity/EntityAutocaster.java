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
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;

import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TALootTables;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;
import thecodex6824.thaumicaugmentation.api.ward.entity.CapabilityWardOwnerProvider;
import thecodex6824.thaumicaugmentation.api.ward.entity.WardOwnerProviderOwnable;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;
import thecodex6824.thaumicaugmentation.init.GUIHandler.TAInventory;

public class EntityAutocaster extends EntityAutocasterBase implements IEntityOwnable {

    protected static final DataParameter<Optional<UUID>> OWNER_ID = EntityDataManager.createKey(EntityAutocaster.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Byte> TARGETS = EntityDataManager.createKey(EntityAutocaster.class, DataSerializers.BYTE);
    
    protected WeakReference<Entity> ownerRef;
    protected EntityAINearestValidTarget targeting;
    protected WardOwnerProviderOwnable<EntityAutocaster> wardOwner;
    protected ImpetusStorage impetus;
    
    protected boolean teamCheck(EntityLivingBase target) {
        Team myTeam = getTeam();
        Team theirTeam = target.getTeam();
        boolean onSameTeam = myTeam != null && myTeam == theirTeam;
        if (onSameTeam && !getTargetFriendly())
            return false;
        else if (myTeam != null && !onSameTeam && getTargetFriendly())
            return false;
        if (target.equals(getOwner()) && !getTargetFriendly())
            return false;
        else if (target instanceof IEntityOwnable) {
            IEntityOwnable ownable = (IEntityOwnable) target;
            if (ownable.getOwner() != null && ownable.getOwner().equals(getOwner()) && !getTargetFriendly())
                return false;
            else if (ownable.getOwner() != null && (!onSameTeam && !ownable.getOwner().equals(getOwner())) && getTargetFriendly())
                return false;
        }
        
        return true;
    }
    
    protected boolean animalTargetSelector(EntityLivingBase entity) {
        if (entity instanceof IAnimals)
            return teamCheck(entity);
        else
            return false;
    }
    
    protected boolean mobTargetSelector(EntityLivingBase entity) {
        if (entity instanceof IMob)
            return teamCheck(entity);
        else
            return false;
    }
    
    protected boolean playerTargetSelector(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && ThaumicAugmentation.proxy.isPvPEnabled())
            return teamCheck(entity);
        else
            return false;
    }
    
    public EntityAutocaster(World world) {
        super(world);
        ownerRef = new WeakReference<>(null);
        setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
        impetus = new ImpetusStorage(50, 25);
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(25.0);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0);
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OWNER_ID, Optional.absent());
        dataManager.register(TARGETS, (byte) 2);
    }
    
    @Override
    protected void initEntityAI() {
        tasks.addTask(1, new EntityAIWatchTarget());
        tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
        tasks.addTask(3, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        targeting = new EntityAINearestValidTarget(true, 2);
        targeting.addTargetSelector(this::mobTargetSelector);
        targetTasks.addTask(2, targeting);
    }
    
    public boolean getTargetAnimals() {
        return BitUtil.isBitSet(dataManager.get(TARGETS), 0);
    }
    
    public void setTargetAnimals(boolean target) {
        dataManager.set(TARGETS, (byte) BitUtil.setOrClearBit(dataManager.get(TARGETS), 0, target));
        if (targeting != null) {
            if (target)
                targeting.addTargetSelector(this::animalTargetSelector);
            else
                targeting.removeTargetSelector(this::animalTargetSelector);
            
            targeting.resetTask();
        }
    }
    
    public boolean getTargetMobs() {
        return BitUtil.isBitSet(dataManager.get(TARGETS), 1);
    }
    
    public void setTargetMobs(boolean target) {
        dataManager.set(TARGETS, (byte) BitUtil.setOrClearBit(dataManager.get(TARGETS), 1, target));
        if (targeting != null) {
            if (target)
                targeting.addTargetSelector(this::mobTargetSelector);
            else
                targeting.removeTargetSelector(this::mobTargetSelector);
            
            targeting.resetTask();
        }
    }
    
    public boolean getTargetPlayers() {
        return BitUtil.isBitSet(dataManager.get(TARGETS), 2);
    }
    
    public void setTargetPlayers(boolean target) {
        dataManager.set(TARGETS, (byte) BitUtil.setOrClearBit(dataManager.get(TARGETS), 2, target));
        if (targeting != null) {
            if (target)
                targeting.addTargetSelector(this::playerTargetSelector);
            else
                targeting.removeTargetSelector(this::playerTargetSelector);
            
            targeting.resetTask();
        }
    }
    
    public boolean getTargetFriendly() {
        return BitUtil.isBitSet(dataManager.get(TARGETS), 3);
    }
    
    public void setTargetFriendly(boolean target) {
        dataManager.set(TARGETS, (byte) BitUtil.setOrClearBit(dataManager.get(TARGETS), 3, target));
        if (targeting != null)
            targeting.resetTask();
    }
    
    public boolean getRedstoneControl() {
        return BitUtil.isBitSet(dataManager.get(TARGETS), 4);
    }
    
    public void setRedstoneControl(boolean redstone) {
        dataManager.set(TARGETS, (byte) BitUtil.setOrClearBit(dataManager.get(TARGETS), 4, redstone));
        if (targeting != null)
            targeting.resetTask();
    }
    
    @Override
    protected void updateEntityActionState() {
        if (isDisabled()) {
            targeting.resetTask();
            BlockPos base = getPosition().down();
            lookHelper.setLookPosition(base.getX() + 0.5, base.getY() + 0.5, base.getZ() + 0.5, getHorizontalFaceSpeed(), getVerticalFaceSpeed());
            lookHelper.onUpdateLook();
        }
        else
            super.updateEntityActionState();
    }
    
    @Override
    @Nullable
    public Entity getOwner() {
        if ((ownerRef.get() == null || ownerRef.get().isDead) && dataManager.get(OWNER_ID).isPresent()) {
            ownerRef.clear();
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
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && !isDead && getHealth() > 0.0F) {
            if (player.equals(getOwner())) {
                if (player.isSneaking()) {
                    playSound(SoundsTC.zap, 1.0F, 1.0F);
                    dropFocus();
                    entityDropItem(new ItemStack(TAItems.AUTOCASTER_PLACER), 0.5F);
                    setDead();
                    player.swingArm(hand);
                    return true;
                }
                else {
                    player.openGui(ThaumicAugmentation.instance, TAInventory.AUTOCASTER.getID(), world, getEntityId(), 0, 0);
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
    protected boolean isDisabled() {
        if (BitUtil.isBitSet(dataManager.get(TARGETS), 4)) {
            if (world.isBlockPowered(getPosition()))
                return true;
            else {
                IBlockState rail = world.getBlockState(getPosition().down());
                if (rail.getBlock() == BlocksTC.activatorRail && rail.getValue(BlockRailPowered.POWERED).booleanValue())
                    return true;
            }
        }
        
        return false;
    }
    
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return TALootTables.AUTOCASTER;
    }
    
    @Override
    protected void dropItemFromPlacement() {
        dropFocus();
        entityDropItem(new ItemStack(TAItems.AUTOCASTER_PLACER), 0.5F);
    }
    
    @Override
    protected int getHealRate() {
        return 100;
    }
    
    protected void dropFocus() {
        if (getHeldItemMainhand() != null && !getHeldItemMainhand().isEmpty())
            entityDropItem(getHeldItemMainhand(), 0.5F);
    }
    
    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!world.isRemote)
            dropFocus();
    }
    
    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(TAItems.AUTOCASTER_PLACER);
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("energy", impetus.serializeNBT());
        if (dataManager.get(OWNER_ID).isPresent())
            compound.setUniqueId("owner", dataManager.get(OWNER_ID).get());
        
        compound.setInteger("dir", dataManager.get(FACING).getIndex());
        compound.setInteger("cooldown", cooldown);
        compound.setByte("targets", dataManager.get(TARGETS));
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        impetus.deserializeNBT(compound.getCompoundTag("energy"));
        if (compound.hasUniqueId("owner"))
            dataManager.set(OWNER_ID, Optional.of(compound.getUniqueId("owner")));
        
        dataManager.set(FACING, EnumFacing.byIndex(compound.getInteger("dir")));
        cooldown = compound.getInteger("cooldown");
        dataManager.set(TARGETS, compound.getByte("targets"));
        // sync value to AI fields
        setTargetAnimals(getTargetAnimals());
        setTargetMobs(getTargetMobs());
        setTargetPlayers(getTargetPlayers());
        setTargetFriendly(getTargetFriendly());
    }
    
    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
        if (BitUtil.isBitSet(dataManager.get(TARGETS), 4) && world.isBlockPowered(getPosition().offset(dataManager.get(FACING).getOpposite())))
            return false;
        else if (getTargetAnimals() && IAnimals.class.isAssignableFrom(cls) && !IMob.class.isAssignableFrom(cls))
            return true;
        else if (getTargetMobs() && IMob.class.isAssignableFrom(cls))
            return true;
        else if (getTargetPlayers() && EntityPlayer.class.isAssignableFrom(cls))
            return true;
        else
            return false;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityWardOwnerProvider.WARD_OWNER ||
                capability == CapabilityImpetusStorage.IMPETUS_STORAGE ? true :
                super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityWardOwnerProvider.WARD_OWNER) {
            if (wardOwner == null)
                wardOwner = new WardOwnerProviderOwnable<>(this);
            
            return CapabilityWardOwnerProvider.WARD_OWNER.cast(wardOwner);
        }
        else if (capability == CapabilityImpetusStorage.IMPETUS_STORAGE)
            return CapabilityImpetusStorage.IMPETUS_STORAGE.cast(impetus);
        else
            return super.getCapability(capability, facing);
    }
    
}
