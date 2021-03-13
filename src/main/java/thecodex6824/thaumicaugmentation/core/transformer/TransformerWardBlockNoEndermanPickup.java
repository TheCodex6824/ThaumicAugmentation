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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import thecodex6824.thaumicaugmentation.core.ThaumicAugmentationCore;

public class TransformerWardBlockNoEndermanPickup extends Transformer {

    private static final String CLASS = "net.minecraft.entity.monster.EntityEnderman$AITakeBlock";
    
    @Override
    public boolean needToComputeFrames() {
        return false;
    }
    
    @Override
    public boolean isTransformationNeeded(String transformedName) {
        return !ThaumicAugmentationCore.getConfig().getBoolean("DisableWardFocus", "gameplay.ward", false, "") &&
                transformedName.equals(CLASS);
    }
    
    @Override
    public boolean isAllowedToFail() {
        return false;
    }
    
    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
        try {
            MethodNode pickup = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/entity/monster/EntityEnderman$AITakeBlock", "func_75246_d", Type.VOID_TYPE),
                    "()V");
            boolean found = false;
            int ret = pickup.instructions.size();
            while ((ret = TransformUtil.findLastInstanceOfOpcode(pickup, ret, Opcodes.IFEQ)) != -1) {
                AbstractInsnNode insertAfter = pickup.instructions.get(ret);
                pickup.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.IFEQ, ((JumpInsnNode) insertAfter).label));
                pickup.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_COMMON,
                        "checkWardGeneric",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
                        false
                ));
                pickup.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 6));
                pickup.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 2));
                ret -= 5;
                found = true;
            }
            
            if (!found)
                throw new TransformerException("Could not locate required instructions");
            
            return true;
        }
        catch (Throwable anything) {
            error = new RuntimeException(anything);
            return false;
        }
    }
    
}
