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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import thaumcraft.api.casters.FocusMediumRoot;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.monster.boss.EntityEldritchGolem;
import thaumcraft.common.entities.monster.boss.EntityEldritchWarden;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.items.casters.foci.FocusEffectAir;
import thaumcraft.common.items.casters.foci.FocusEffectCurse;
import thaumcraft.common.items.casters.foci.FocusEffectEarth;
import thaumcraft.common.items.casters.foci.FocusEffectFire;
import thaumcraft.common.items.casters.foci.FocusEffectFlux;
import thaumcraft.common.items.casters.foci.FocusEffectFrost;
import thaumcraft.common.items.casters.foci.FocusMediumBolt;
import thaumcraft.common.items.casters.foci.FocusMediumProjectile;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectWater;
import thecodex6824.thaumicaugmentation.common.util.WeightedRandom;

public class EntityAutocasterEldritch extends EntityAutocasterBase implements IMob, IEldritchMob {
    
    protected static final List<Integer> FOCUS_RANGE = IntStream.range(0, 7).boxed().collect(Collectors.toList());
    
    // all words based on R'lyehian words on https://www.yog-sothoth.com/wiki/index.php/R'lyehian
    protected static final String[] PREFIXES = new String[] {
            "c",
            "f'",
            "h'",
            "na",
            "nafl",
            "ng",
            "nnn",
            "ph'",
            "y"
    };
    
    protected static final String[] SUFFIXES = new String[] {
            "agl",
            "nyth",
            "og",
            "oth"
    };
    
    protected static final String[] CONJUNCTIONS = new String[] {
            "mg"
    };
    
    protected static final String[] WORDS = new String[] {
            "ah",
            "'ai",
            "athg",
            "'bthnk",
            "bug",
            "ch'",
            "chtenff",
            "ebumna",
            "ee",
            "ehye",
            "ep",
            "'fhalma",
            "fhtagn",
            "fm'latgh",
            "ftaghu",
            "geb",
            "gnaiih",
            "gof'nn",
            "goka",
            "gotha",
            "grah'n",
            "hafh'drn",
            "hai",
            "hlirgh",
            "hrii",
            "hupadgh",
            "ilyaa",
            "k'yarnak",
            "kadishtu",
            "kn'a",
            "li'hee",
            "llll",
            "lloig",
            "lw'nafh",
            "mnahn'",
            "n'gha",
            "n'ghft",
            "nglui",
            "nilgh'ri",
            "nog",
            "nw",
            "ooboshu",
            "orr'e",
            "phlegeth",
            "r'luh",
            "ron",
            "s'uhn",
            "sgn'wahl",
            "shagg",
            "shogg",
            "shtunggli",
            "shugg",
            "sll'ha",
            "stell'bsna",
            "syha'h",
            "tharanak",
            "throd",
            "uaaah",
            "uh'e",
            "uln",
            "vulgtlagln",
            "vulgtm",
            "wgah'n",
            "y'hah",
            "ya",
            "zhro"
    };
    
    public EntityAutocasterEldritch(World world) {
        super(world);
    }
    
    @Override
    protected void initEntityAI() {
        tasks.addTask(1, new EntityAIWatchTarget());
        tasks.addTask(2, new EntityAIWatchClosest(this, EntityEldritchWarden.class, 12.0F));
        tasks.addTask(2, new EntityAIWatchClosest(this, EntityEldritchGolem.class, 12.0F));
        tasks.addTask(2, new EntityAIWatchClosest(this, EntityEldritchGuardian.class, 12.0F));
        tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
        tasks.addTask(3, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        EntityAINearestValidTarget targeting = new EntityAINearestValidTarget(true, 5);
        targeting.addTargetSelector(entity -> entity instanceof EntityPlayer || entity instanceof EntityCultist);
        targetTasks.addTask(2, targeting);
    }
    
    protected void addWord(Random rand, StringBuilder builder) {
        if (rand.nextInt(5) == 0)
            builder.append(PREFIXES[rand.nextInt(PREFIXES.length)]);
        
        builder.append(WORDS[rand.nextInt(WORDS.length)]);
        if (rand.nextInt(5) == 0)
            builder.append(SUFFIXES[rand.nextInt(SUFFIXES.length)]);
    }
    
    protected String getRandomFocusName(Random rand) {
        int words = rand.nextInt(10);
        if (words == 9)
            words = 3;
        else if (words >= 2)
            words = 2;
        else
            words = 1;
        
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < words; ++i) {
            addWord(rand, str);
            if (i != words - 1) {
                str.append(" ");
                if (rand.nextInt(5) == 0)
                    str.append(CONJUNCTIONS[rand.nextInt(CONJUNCTIONS.length)] + " ");
            }
        }
        
        return str.toString();
    }
    
    protected FocusNode generateEffectForDifficulty(DifficultyInstance difficulty, Random rand, FocusNode medium) {
        ImmutableList<Integer> weights = ImmutableList.of(2, 2, 2, 2, 1, 1, world.getDifficulty().getId());
        WeightedRandom<Integer> picker = new WeightedRandom<>(FOCUS_RANGE, weights);
        FocusNode effect = null;
        switch (picker.get(rand)) {
            case 0: {
                effect = new FocusEffectAir();
                effect.getSetting("power").setValue(5);
                break;
            }
            case 1: {
                effect = new FocusEffectEarth();
                effect.getSetting("power").setValue(5);
                break;
            }
            case 2: {
                effect = new FocusEffectFire();
                effect.getSetting("power").setValue(5);
                effect.getSetting("duration").setValue(rand.nextInt(3) + 3);
                break;
            }
            case 3: {
                effect = new FocusEffectFrost();
                effect.getSetting("power").setValue(5);
                effect.getSetting("duration").setValue(rand.nextInt(6) + 5);
                break;
            }
            case 4: {
                effect = new FocusEffectWater();
                effect.getSetting("power").setValue(5);
                break;
            }
            case 5: {
                effect = new FocusEffectCurse();
                effect.getSetting("power").setValue(5);
                int durationOffset = 5;
                // maxed out projectile + curse takes 36 complexity
                if (medium instanceof FocusMediumProjectile) {
                    FocusMediumProjectile p = (FocusMediumProjectile) medium;
                    if (p.getSetting("speed").getValue() == 5 && p.getSetting("option").getValue() == 2)
                        durationOffset = 4;
                }
                
                effect.getSetting("duration").setValue(rand.nextInt(6) + durationOffset);
                break;
            }
            case 6:
            default: {
                effect = new FocusEffectFlux();
                effect.getSetting("power").setValue(5);
                break;
            }
        }
        
        return effect;
    }
    
    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        return onInitialSpawn(difficulty, livingdata, false);
    }
    
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata, boolean randomFocus) {
        if (!world.isRemote) {
            Random random = null;
            if (!randomFocus) {
                random = new Random(world.getSeed());
                long xSeed = random.nextLong();
                long ySeed = random.nextLong();
                long zSeed = random.nextLong();
                random.setSeed((xSeed * (int) posX + ySeed * (int) posY + zSeed * (int) posZ) ^ world.getSeed());
            }
            else
                random = rand;
            
            ItemStack focus = new ItemStack(TAItems.FOCUS_ANCIENT);
            FocusPackage core = new FocusPackage();
            FocusNode root = new FocusMediumRoot();
            core.addNode(root);
            FocusNode medium = null;
            if (random.nextBoolean())
                medium = new FocusMediumBolt();
            else {
                medium = new FocusMediumProjectile();
                medium.getSetting("speed").setValue(random.nextInt(3) + 3);
                int special = random.nextInt(10);
                if (special >= 8)
                    medium.getSetting("option").setValue(2);
                else if (special >= 6)
                    medium.getSetting("option").setValue(1);
            }
            medium.setParent(root);
            core.addNode(medium);
            FocusNode effect = generateEffectForDifficulty(difficulty, random, medium);
            effect.setParent(medium);
            core.addNode(effect);
            focus.setStackDisplayName(getRandomFocusName(random));
           
            int complexity = 0;
            Object2IntOpenHashMap<String> nodeCounts = new Object2IntOpenHashMap<>(core.nodes.size());
            for (IFocusElement node : core.nodes) {
                if (node instanceof FocusNode) {
                    int count = nodeCounts.getOrDefault(node.getKey(), 0) + 2;
                    double complexityMultiplier = 0.5 * count;
                    nodeCounts.addTo(node.getKey(), 1);
                    complexity += ((FocusNode) node).getComplexity() * complexityMultiplier;
                }
            }
            
            core.setComplexity(complexity);
            ItemFocus.setPackage(focus, core);
            setHeldItem(EnumHand.MAIN_HAND, focus);
        }
        
        return super.onInitialSpawn(difficulty, livingdata);
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(35.0);
    }
    
    @Override
    public int getTotalArmorValue() {
        return 8;
    }
    
    @Override
    public boolean isOnSameTeam(Entity entity) {
        return entity instanceof IEldritchMob;
    }
    
    @Override
    protected void dropItemFromPlacement() {}
    
    @Override
    protected int getHealRate() {
        return 80;
    }
    
    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!world.isRemote)
            entityDropItem(new ItemStack(ItemsTC.plate, rand.nextInt(3) + 1, 3), 0.5F);
    }

}
