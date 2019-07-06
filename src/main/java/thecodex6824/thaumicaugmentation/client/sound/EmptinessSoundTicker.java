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

package thecodex6824.thaumicaugmentation.client.sound;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;

public class EmptinessSoundTicker implements ITickable {

    private static ISound LOOP = new PositionedSoundRecord(TASounds.EMPTINESS_MUSIC.getSoundName(), SoundCategory.WEATHER, 1.0F, 1.0F,
            true, 0, AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
    private static ISound AMBIENCE = new PositionedSoundRecord(TASounds.EMPTINESS_AMBIENCE.getSoundName(), SoundCategory.AMBIENT, 1.0F, 1.0F,
            false, 0, AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
    
    private static int MIN_TICKS = 1200;
    private static int MAX_TICKS = 3600;
    
    private Minecraft mc;
    private boolean playingMusic;
    private Random rand;
    private int ticksLeftToPlaySound;

    public EmptinessSoundTicker(Minecraft mcIn) {
        mc = mcIn;
        rand = new Random();
        ticksLeftToPlaySound = MathHelper.getInt(rand, MIN_TICKS, MAX_TICKS);
    }

    private void playMusic() {
        mc.getSoundHandler().playSound(LOOP);
        playingMusic = true;
    }
    
    private void playSound() {
        if (!mc.getSoundHandler().isSoundPlaying(AMBIENCE))
            mc.getSoundHandler().playSound(AMBIENCE);
        
        ticksLeftToPlaySound = MathHelper.getInt(rand, MIN_TICKS, MAX_TICKS);
    }
    
    @Override
    public void update() {
        if (mc.world == null && playingMusic) {
            mc.getSoundHandler().stopSound(LOOP);
            playingMusic = false;
        }
        else if (mc.world != null) {
            if (mc.world.provider.getDimension() != TADimensions.EMPTINESS.getId() && playingMusic) {
                mc.getSoundHandler().stopSound(LOOP);
                playingMusic = false;
            }
            else if (mc.world.provider.getDimension() == TADimensions.EMPTINESS.getId()) {
                --ticksLeftToPlaySound;
                if (ticksLeftToPlaySound <= 0)
                    playSound();
                
                if (!playingMusic)
                    playMusic();
            }
        }
    }
    
}
