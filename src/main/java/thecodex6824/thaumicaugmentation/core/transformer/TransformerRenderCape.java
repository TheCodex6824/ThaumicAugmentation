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

package thecodex6824.thaumicaugmentation.core.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TransformerRenderCape  extends Transformer {

    private static final String CLASS = "net.minecraft.client.renderer.entity.layers.LayerCape";
    
    @Override
    public boolean needToComputeFrames() {
        return false;
    }
    
    @Override
    public boolean isAllowedToFail() {
        return true;
    }
    
    @Override
    public boolean isTransformationNeeded(String transformedName) {
        return transformedName.equals(CLASS);
    }
    
    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
        try {
            MethodNode render = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/client/renderer/entity/layers/LayerCape", "func_177141_a",
                    Type.VOID_TYPE, Type.getType("Lnet/minecraft/client/entity/AbstractClientPlayer;"), Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
                    "(Lnet/minecraft/client/entity/AbstractClientPlayer;FFFFFFF)V");
            
            int check = TransformUtil.findFirstInstanceOfOpcode(render, 0, Opcodes.IF_ACMPEQ);
            if (check != -1) {
                AbstractInsnNode insertAfter = render.instructions.get(check);
                LabelNode label = ((JumpInsnNode) insertAfter).label;
                render.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.IFEQ, label));
                render.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_CLIENT,
                        "shouldRenderCape",
                        "(Lnet/minecraft/client/entity/AbstractClientPlayer;)Z",
                        false
                ));
                render.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 1));
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