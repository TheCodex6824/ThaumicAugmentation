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

public class TransformerAttemptTeleport extends Transformer {

    private static final String CLASS = "net.minecraft.entity.EntityLivingBase";
    
    @Override
    public boolean needToComputeFrames() {
        return false;
    }
    
    @Override
    public boolean isAllowedToFail() {
        return false;
    }
    
    @Override
    public boolean isTransformationNeeded(String transformedName) {
        return transformedName.equals(CLASS);
    }
    
    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
        try {
            MethodNode teleport = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/entity/EntityLivingBase", "func_184595_k",
                    Type.BOOLEAN_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE), "(DDD)Z");
            int ret = TransformUtil.findFirstInstanceOfOpcode(teleport, TransformUtil.findFirstLoad(teleport, 0, Opcodes.ILOAD, 17), Opcodes.IFEQ);
            if (ret != -1) {
                JumpInsnNode insertAfter = (JumpInsnNode) teleport.instructions.get(ret);
                teleport.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.IFEQ, insertAfter.label));
                teleport.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_COMMON,
                        "onAttemptTeleport",
                        "(Lnet/minecraft/entity/EntityLivingBase;DDD)Z",
                        false
                ));
                teleport.instructions.insert(insertAfter, new VarInsnNode(Opcodes.DLOAD, 11));
                teleport.instructions.insert(insertAfter, new VarInsnNode(Opcodes.DLOAD, 9));
                teleport.instructions.insert(insertAfter, new VarInsnNode(Opcodes.DLOAD, 7));
                teleport.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
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
