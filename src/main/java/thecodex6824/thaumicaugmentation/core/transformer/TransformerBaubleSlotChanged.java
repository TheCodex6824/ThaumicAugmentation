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
import org.objectweb.asm.tree.*;

public class TransformerBaubleSlotChanged extends Transformer {

    private static final String CLASS = "baubles.api.cap.BaublesContainer";
    
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
            MethodNode set = TransformUtil.findMethod(classNode, "setChanged", "(IZ)V");
            int ret = 0;
            while ((ret = TransformUtil.findFirstInstanceOfOpcode(set, ret, Opcodes.RETURN)) != -1) {
                AbstractInsnNode insertAfter = set.instructions.get(ret).getPrevious();
                set.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_COMMON,
                        "onBaubleChanged",
                        "(Lnet/minecraft/entity/EntityLivingBase;)V",
                        false
                ));
                set.instructions.insert(insertAfter, new FieldInsnNode(Opcodes.GETFIELD, "baubles/api/cap/BaublesContainer",
                        "player", "Lnet/minecraft/entity/EntityLivingBase;"));
                set.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
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
