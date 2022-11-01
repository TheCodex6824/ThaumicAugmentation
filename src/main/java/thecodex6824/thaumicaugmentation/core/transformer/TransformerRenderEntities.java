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

package thecodex6824.thaumicaugmentation.core.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class TransformerRenderEntities  extends Transformer {

    private static final String CLASS = "net.minecraft.client.renderer.RenderGlobal";
    
    @Override
    public boolean needToComputeFrames() {
        return false;
    }
    
    @Override
    public boolean isTransformationNeeded(String transformedName) {
        return transformedName.equals(CLASS);
    }
    
    @Override
    public boolean isAllowedToFail() {
        return false;
    }
    
    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
        try {
            MethodNode render = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/client/renderer/RenderGlobal", "func_180446_a",
                    Type.VOID_TYPE, Type.getType("Lnet/minecraft/entity/Entity;"), Type.getType("Lnet/minecraft/client/renderer/culling/ICamera;"), Type.FLOAT_TYPE),
                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V");
            
            // we want to pick a spot after TE / entity rendering, but before GL state gets reset
            int call = TransformUtil.findLastInstanceOfMethodCall(render, render.instructions.size(), TransformUtil.remapMethodName("net/minecraft/client/renderer/RenderGlobal", "func_180443_s", Type.VOID_TYPE),
                    "()V", "net/minecraft/client/renderer/RenderGlobal");
            if (call != -1) {
                AbstractInsnNode insertAfter = render.instructions.get(call).getPrevious();
                render.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_CLIENT,
                        "onRenderEntities",
                        "(I)V",
                        false
                ));
                render.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ILOAD, 4));
            }
            else
                throw new TransformerException("Could not locate required instructions");
            
            return true;
        }
        catch (Throwable anything) {
            error = new RuntimeException(anything);
            return false;
        }
    }
    
}