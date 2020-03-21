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

import java.lang.ref.WeakReference;

import net.minecraft.client.audio.ISound.AttenuationType;
import thecodex6824.thaumicaugmentation.common.util.ISoundHandle;

public class SoundHandleSpecialSound implements ISoundHandle {

    protected WeakReference<MovingSoundRecord> sound;
    
    public SoundHandleSpecialSound(MovingSoundRecord audio) {
        sound = new WeakReference<>(audio);
    }
    
    public void setAttenuationType(AttenuationType newType) {
        MovingSoundRecord s = sound.get();
        if (s != null)
            s.setAttenuationType(newType);
    }
    
    @Override
    public void stop() {
        MovingSoundRecord s = sound.get();
        if (s != null)
            s.stop();
    }
    
}
