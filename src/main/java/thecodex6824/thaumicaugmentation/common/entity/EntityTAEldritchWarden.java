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

import java.util.Random;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.common.entities.monster.boss.EntityEldritchWarden;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.api.event.EntityInOuterLandsEvent;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;

public class EntityTAEldritchWarden extends EntityEldritchWarden {

    protected static final DataParameter<Boolean> TRANSPARENT = EntityDataManager.createKey(EntityTAEldritchWarden.class,
            DataSerializers.BOOLEAN);
    
    protected static final String[] NAMES = new String[] {
            "Aphoom-Zhah", 
            "Basatan", 
            "Chaugnar Faugn", 
            "Mnomquah", 
            "Nyogtha", 
            "Oorn", 
            "Shaikorth", 
            "Rhan-Tegoth", 
            "Rhogog", 
            "Shudde M'ell", 
            "Vulthoom", 
            "Yag-Kosha", 
            "Yibb-Tstll", 
            "Zathog", 
            "Zushakon"
    };
    
    protected static String generateName(Random rng) {
        return NAMES[rng.nextInt(NAMES.length)];
    }
    
    public EntityTAEldritchWarden(World world) {
        super(world);
        setSize(0.8F, 2.25F);
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(TRANSPARENT, true);
    }
    
    public boolean isTransparent() {
        return dataManager.get(TRANSPARENT);
    }
    
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance diff, IEntityLivingData data) {
        EntityUtils.makeChampion(this, true);
        IEntityLivingData d = super.onInitialSpawn(diff, data);
        bossInfo.setName(getDisplayName());
        EntityInOuterLandsEvent event = new EntityInOuterLandsEvent(this);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.ALLOW || (event.getResult() == Result.DEFAULT && world.provider.getDimension() == TADimensions.EMPTINESS.getId()))
            dataManager.set(TRANSPARENT, false);
        
        return d;
    }
    
    @Override
    public void generateName() {
        int cIndex = (int) getEntityAttribute(ThaumcraftApiHelper.CHAMPION_MOD).getAttributeValue();
        setCustomNameTag(new TextComponentTranslation("thaumicaugmentation.text.entity.eldritch_warden", generateName(rand),
                ChampionModifier.mods[cIndex].getModNameLocalized()).getFormattedText());
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote && getSpawnTimer() == 1) {
            if (world.getDifficulty() == EnumDifficulty.HARD) {
                for (int i = 0; i < 3; ++i) {
                    EntityFocusShield shield = new EntityFocusShield(world);
                    shield.setOwner(this);
                    shield.setCasterID(getPersistentID());
                    shield.setColor(0x555555);
                    shield.setMaxHealth(150.0F, false);
                    shield.setInfiniteLifespan();
                    shield.setReflect(true);
                    shield.setYawOffset(i * 120.0F);
                    shield.setRotate(true);
                    shield.setHealth(shield.getMaxHealth());
                    world.spawnEntity(shield);
                    world.playSound(null, shield.getPosition(), SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 
                            SoundCategory.HOSTILE, 0.75F, 1.2F);
                }
            }
            else if (world.getDifficulty() == EnumDifficulty.NORMAL) {
                EntityFocusShield shield = new EntityFocusShield(world);
                shield.setOwner(this);
                shield.setCasterID(getPersistentID());
                shield.setColor(0x555555);
                shield.setMaxHealth(100.0F, false);
                shield.setInfiniteLifespan();
                shield.setReflect(true);
                shield.setHealth(shield.getMaxHealth());
                world.spawnEntity(shield);
                world.playSound(null, shield.getPosition(), SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 
                        SoundCategory.HOSTILE, 1.0F, 1.2F);
            }
            else {
                EntityFocusShield shield = new EntityFocusShield(world);
                shield.setOwner(this);
                shield.setCasterID(getPersistentID());
                shield.setColor(0x555555);
                shield.setMaxHealth(50.0F, false);
                shield.setInfiniteLifespan();
                shield.setHealth(shield.getMaxHealth());
                world.spawnEntity(shield);
                world.playSound(null, shield.getPosition(), SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 
                        SoundCategory.HOSTILE, 1.0F, 1.2F);
            }
        }
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        bossInfo.setName(getDisplayName());
    }
    
}
