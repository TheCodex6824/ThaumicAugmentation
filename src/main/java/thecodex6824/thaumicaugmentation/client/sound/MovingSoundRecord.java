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

import java.util.function.Supplier;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

public class MovingSoundRecord extends MovingSound {

    protected Supplier<Vec3d> tickFunc;
    
    public MovingSoundRecord(SoundEvent sound, SoundCategory category, Supplier<Vec3d> tick) {
        
        this(sound, category, tick, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
    }
    
    public MovingSoundRecord(SoundEvent sound, SoundCategory category, Supplier<Vec3d> tick,
            float x, float y, float z) {
        
        this(sound, category, tick, x, y, z, 1.0F, 1.0F);
    }
    
    public MovingSoundRecord(SoundEvent sound, SoundCategory category, Supplier<Vec3d> tick,
            float x, float y, float z, float soundVolume, float soundPitch) {
        
        super(sound, category);
        tickFunc = tick;
        attenuationType = AttenuationType.LINEAR;
        repeat = true;
        repeatDelay = 0;
        volume = soundVolume;
        pitch = soundPitch;
        setPos(x, y, z);
    }
    
    public void setPos(float x, float y, float z) {
        xPosF = x;
        yPosF = y;
        zPosF = z;
    }
    
    public void stop() {
        donePlaying = true;
    }
    
    @Override
    public void update() {
        Vec3d pos = tickFunc.get();
        if (pos == null)
            donePlaying = true;
        else
            setPos((float) pos.x, (float) pos.y, (float) pos.z);
    }
    
}
