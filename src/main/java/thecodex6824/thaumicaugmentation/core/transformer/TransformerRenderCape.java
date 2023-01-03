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

package thecodex6824.thaumicaugmentation.core.transformer;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class TransformerRenderCape  extends Transformer {

    private static final String CLASS = "net.minecraft.client.renderer.entity.layers.LayerCape";
    
    @Override
    public boolean needToComputeFrames() {
        return true;
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
            
            int check = TransformUtil.findFirstField(render, 0, TransformUtil.remapFieldName("net/minecraft/init/Items", "field_185160_cR"),
                    "Lnet/minecraft/item/Item;", "net/minecraft/init/Items");
            if (check != -1) {
                // apparently the checks/jumps are inverted outside dev (?)
                if (render.instructions.get(check).getNext().getOpcode() == Opcodes.IF_ACMPEQ) {
                    AbstractInsnNode insertAfter = render.instructions.get(check).getNext();
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
                else {
                    AbstractInsnNode insertAfter = render.instructions.get(check).getPrevious().getPrevious().getPrevious();
                    LabelNode newLabel = new LabelNode(new Label());
                    render.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.IFEQ, newLabel));
                    render.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                            TransformUtil.HOOKS_CLIENT,
                            "shouldRenderCape",
                            "(Lnet/minecraft/client/entity/AbstractClientPlayer;)Z",
                            false
                    ));
                    render.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 1));
                    
                    check = TransformUtil.findFirstInstanceOfOpcode(render, check, Opcodes.RETURN);
                    if (check != -1)
                        render.instructions.insert(render.instructions.get(check).getPrevious(), newLabel);
                    else
                        throw new TransformerException("Could not locate required instructions, complex case");
                }
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