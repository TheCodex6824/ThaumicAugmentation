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
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TransformerFluxRiftDestroyBlock extends Transformer {

    private static final String CLASS = "thaumcraft.common.entities.EntityFluxRift";
    
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
            MethodNode check = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/entity/Entity", "func_70071_h_", Type.VOID_TYPE), "()V");
            int ret = TransformUtil.findFirstInstanceOfMethodCall(check, 0, TransformUtil.remapMethodName("net/minecraft/block/Block", "func_176209_a",
                    Type.BOOLEAN_TYPE, Type.getType("Lnet/minecraft/block/state/IBlockState;"), Type.BOOLEAN_TYPE), "(Lnet/minecraft/block/state/IBlockState;Z)Z", "net/minecraft/block/Block");
            if (ret != -1 && ret < check.instructions.size() - 1 && check.instructions.get(ret).getNext() instanceof JumpInsnNode) {
                JumpInsnNode insertAfter = (JumpInsnNode) check.instructions.get(ret).getNext();
                check.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.IFNE, insertAfter.label));
                check.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_COMMON,
                        "fireFluxRiftDestroyBlockEvent",
                        "(Lthaumcraft/common/entities/EntityFluxRift;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                        false
                ));
                check.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 6));
                check.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 5));
                check.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
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
