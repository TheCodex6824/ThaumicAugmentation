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

package thecodex6824.thaumicaugmentation.client.sound;

import com.google.common.math.DoubleMath;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.function.Function;

public class MovingSoundRecord extends MovingSound {

    protected Function<Vec3d, Vec3d> tickFunc;
    protected int fadeIn;
    protected float fadeOut;
    protected int ticks;
    protected boolean inFadeOut;
    
    public MovingSoundRecord(SoundEvent sound, SoundCategory category, Function<Vec3d, Vec3d> tick) {
        
        this(sound, category, tick, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
    }
    
    public MovingSoundRecord(SoundEvent sound, SoundCategory category, Function<Vec3d, Vec3d> tick,
            float x, float y, float z) {
        
        this(sound, category, tick, x, y, z, 1.0F, 1.0F);
    }
    
    public MovingSoundRecord(SoundEvent sound, SoundCategory category, Function<Vec3d, Vec3d> tick,
            float x, float y, float z, float soundVolume, float soundPitch) {
        
        this(sound, category, tick, x, y, z, soundVolume, soundPitch, false, 0);
    }
    
    public MovingSoundRecord(SoundEvent sound, SoundCategory category, Function<Vec3d, Vec3d> tick,
            float x, float y, float z, float soundVolume, float soundPitch, boolean repeatSound,
            int repeatDelayTicks) {
        
        super(sound, category);
        tickFunc = tick;
        attenuationType = AttenuationType.LINEAR;
        repeat = repeatSound;
        repeatDelay = repeatDelayTicks;
        volume = soundVolume;
        pitch = soundPitch;
        setPos(x, y, z);
        fadeIn = -1;
        fadeOut = -1F;
    }
    
    public void setPos(float x, float y, float z) {
        xPosF = x;
        yPosF = y;
        zPosF = z;
    }
    
    public void setAttenuationType(AttenuationType newType) {
        attenuationType = newType;
    }
    
    public void setFadeIn(int fade) {
        fadeIn = fade;
    }
    
    public void setFadeOut(int fade) {
        fadeOut = volume / fade;
    }
    
    public void stop() {
        donePlaying = true;
    }
    
    @Override
    public void update() {
        Vec3d orig = new Vec3d(xPosF, yPosF, zPosF);
        Vec3d pos = inFadeOut ? orig : tickFunc.apply(orig);
        if (pos == null) {
            if (fadeOut > 0)
                inFadeOut = true;
            else
                donePlaying = true;
        }
        else {
            setPos((float) pos.x, (float) pos.y, (float) pos.z);
            if (ticks <= fadeIn)
                volume = (float) ticks / fadeIn;
            else if (inFadeOut) {
                volume -= fadeOut;
                if (DoubleMath.fuzzyEquals(volume, 0.0F, 0.001))
                    donePlaying = true;
            }
        }
        
        ++ticks;
    }
    
}
