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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class TransformerBipedRotationVanilla extends Transformer {

    private static final String CLASS = "net.minecraft.client.model.ModelBiped";
    
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
            MethodNode rot = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/client/model/ModelBiped", "func_78087_a",
                    Type.VOID_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE,
                    Type.getType("Lnet/minecraft/entity/Entity;")), "(FFFFFFLnet/minecraft/entity/Entity;)V");
            int ret = TransformUtil.findLastInstanceOfMethodCall(rot, rot.instructions.size(), TransformUtil.remapMethodName("net/minecraft/util/math/MathHelper",
                    "func_76134_b", Type.FLOAT_TYPE, Type.FLOAT_TYPE), "(F)F", "net/minecraft/util/math/MathHelper");
            ret = TransformUtil.findLastInstanceOfMethodCall(rot, ret, TransformUtil.remapMethodName("net/minecraft/util/math/MathHelper",
                    "func_76134_b", Type.FLOAT_TYPE, Type.FLOAT_TYPE), "(F)F", "net/minecraft/util/math/MathHelper");
            if (ret != -1) {
                AbstractInsnNode insertAfter = rot.instructions.get(ret).getPrevious();
                rot.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_CLIENT,
                        "correctRotationPoints",
                        "(Lnet/minecraft/client/model/ModelBiped;)V",
                        false
                ));
                rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
            }
            else
                throw new TransformerException("Could not locate required instructions");
            
            ret = 0;
            while ((ret = TransformUtil.findFirstInstanceOfOpcode(rot, ret, Opcodes.RETURN)) != -1) {
                AbstractInsnNode insertAfter = rot.instructions.get(ret).getPrevious();
                rot.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_CLIENT,
                        "handleBipedRotation",
                        "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;)V",
                        false
                ));
                rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 7));
                rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
                ret += 4;
            }
            
            return true;
        }
        catch (Throwable anything) {
            error = new RuntimeException(anything);
            return false;
        }
    }
    
}
