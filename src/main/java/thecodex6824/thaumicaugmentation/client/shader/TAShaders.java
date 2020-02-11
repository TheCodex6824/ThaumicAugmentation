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

package thecodex6824.thaumicaugmentation.client.shader;

import java.util.function.Consumer;

import org.lwjgl.opengl.ARBShaderObjects;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import thecodex6824.thaumicaugmentation.client.shader.TAShaderManager.Shader;

public class TAShaders {

    public static Shader FRACTURE;
    public static Shader EMPTINESS_SKY;
    public static Shader FLUX_RIFT;
    public static Shader MIRROR;
    
    public static final Consumer<Shader> SHADER_CALLBACK_GENERIC_SPHERE = shader -> {
        Minecraft mc = Minecraft.getMinecraft();
        Entity view = mc.getRenderViewEntity() != null ? mc.getRenderViewEntity() : mc.player;
        float yaw = view.rotationYaw;
        float pitch = -view.rotationPitch;
        if (mc.gameSettings.thirdPersonView == 2)
            pitch *= -1;
        
        int x = ARBShaderObjects.glGetUniformLocationARB(shader.getID(), "yaw");
        ARBShaderObjects.glUniform1fARB(x, (float) (yaw * 2.0F * Math.PI / 360.0));
        
        int z = ARBShaderObjects.glGetUniformLocationARB(shader.getID(), "pitch");
        ARBShaderObjects.glUniform1fARB(z, (float) (pitch * 2.0F * Math.PI / 360.0));
    };
    
    public static final Consumer<Shader> SHADER_CALLBACK_CONSTANT_SPHERE = shader -> {
        int x = ARBShaderObjects.glGetUniformLocationARB(shader.getID(), "yaw");
        ARBShaderObjects.glUniform1fARB(x, 0.0F);
        
        int z = ARBShaderObjects.glGetUniformLocationARB(shader.getID(), "pitch");
        ARBShaderObjects.glUniform1fARB(z, 0.0F);
    };
    
}
