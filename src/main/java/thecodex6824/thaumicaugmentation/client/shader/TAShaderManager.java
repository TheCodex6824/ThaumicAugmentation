/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.Consumer;

public final class TAShaderManager {

    private TAShaderManager() {}
    
    public static final class Shader {
        
        private final int id;
        
        private Shader(int id) {
            this.id = id;
        }
        
        public int getID() {
            return id;
        }
        
    }
    
    private static final String EMPTY_SHADER_NAME = "__reserved_empty_shader";
    private static final HashMap<String, Shader> SHADERS = new HashMap<>();
    private static boolean disabled = false;
    
    static {
        SHADERS.put(new ResourceLocation(ThaumicAugmentationAPI.MODID, EMPTY_SHADER_NAME).toString(), new Shader(0));
    }
    
    private static String loadFile(ResourceLocation file, String ext) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(TAShaderManager.class.getResourceAsStream(
                "/assets/" + file.getNamespace() + "/shaders/" + file.getPath() + ext), StandardCharsets.UTF_8))) {
            
            reader.lines().forEach(str -> {
                builder.append(str).append('\n');
            });
            
            return builder.toString();
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().error(ex);
            return "";
        }
    }
    
    private static String getExtensionForType(int type) {
        if (type == ARBVertexShader.GL_VERTEX_SHADER_ARB)
            return ".vert";
        else if (type == ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
            return ".frag";
        else
            return "";
    }
    
    private static int loadShader(ResourceLocation path, int type) {
        int id = ARBShaderObjects.glCreateShaderObjectARB(type);
        if (id == 0)
            return 0;
        
        ARBShaderObjects.glShaderSourceARB(id, loadFile(path, getExtensionForType(type)));
        ARBShaderObjects.glCompileShaderARB(id);
        if (ARBShaderObjects.glGetObjectParameteriARB(id, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == 0) {
            ThaumicAugmentation.getLogger().error("OpenGL shader error: " + 
                    ARBShaderObjects.glGetInfoLogARB(id, ARBShaderObjects.glGetObjectParameteriARB(id, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB)));
            ARBShaderObjects.glDeleteObjectARB(id);
            return 0;
        }
        else
            return id;
    }
    
    private static int loadShaderProgram(ResourceLocation path) {
        int vert = loadShader(path, ARBVertexShader.GL_VERTEX_SHADER_ARB);
        int frag = loadShader(path, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
        
        if (vert != 0 && frag != 0) {
            int program = ARBShaderObjects.glCreateProgramObjectARB();
            ARBShaderObjects.glAttachObjectARB(program, vert);
            ARBShaderObjects.glAttachObjectARB(program, frag);
            
            ARBShaderObjects.glLinkProgramARB(program);
            ARBShaderObjects.glDeleteObjectARB(vert);
            ARBShaderObjects.glDeleteObjectARB(frag);
            if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == 0) {
                ThaumicAugmentation.getLogger().error("OpenGL shader errror: " + 
                        ARBShaderObjects.glGetInfoLogARB(program, ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB)));
                return 0;
            }
            
            ARBShaderObjects.glValidateProgramARB(program);
            if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == 0) {
                ThaumicAugmentation.getLogger().error("OpenGL shader errror: " + 
                        ARBShaderObjects.glGetInfoLogARB(program, ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB)));
                return 0;
            }
            
            return program;
        }
        
        return 0;
    }
    
    public static void init() {
        disabled = TAConfig.disableShaders.getValue() || !OpenGlHelper.shadersSupported;
    }
    
    public static boolean shouldUseShaders() {
        return !disabled;
    }
    
    public static Shader registerShader(ResourceLocation shader) {
        if (shouldUseShaders()) {
            if (shader.toString().equals(EMPTY_SHADER_NAME))
                throw new RuntimeException("Invalid usage of empty shader name (use literally anything else): " + EMPTY_SHADER_NAME);
            
            Shader toAdd = new Shader(loadShaderProgram(shader));
            SHADERS.put(shader.toString(), toAdd);
            return toAdd;
        }
        
        return SHADERS.get(EMPTY_SHADER_NAME);
    }
    
    public static void enableShader(Shader shader) {
        enableShader(shader, null);
    }
    
    public static void enableShader(Shader shader, @Nullable Consumer<Shader> callback) {
        if (shouldUseShaders() && shader.getID() != 0) {
            ARBShaderObjects.glUseProgramObjectARB(shader.getID());
            int timeUniform = ARBShaderObjects.glGetUniformLocationARB(shader.getID(), "time");
            ARBShaderObjects.glUniform1iARB(timeUniform, Minecraft.getMinecraft().getRenderViewEntity() != null ?
                    Minecraft.getMinecraft().getRenderViewEntity().ticksExisted : Minecraft.getMinecraft().player.ticksExisted);
            
            if (callback != null)
                callback.accept(shader);
        }
    }
    
    public static void disableShader() {
        if (shouldUseShaders())
            ARBShaderObjects.glUseProgramObjectARB(0);
    }
    
    public static void destroyShaders() {
        for (Shader shader : SHADERS.values()) {
            if (shader.getID() != 0)
                ARBShaderObjects.glDeleteObjectARB(shader.getID());
        }
        
        SHADERS.clear();
        SHADERS.put(new ResourceLocation(ThaumicAugmentationAPI.MODID, EMPTY_SHADER_NAME).toString(), new Shader(0));
    }
    
}
