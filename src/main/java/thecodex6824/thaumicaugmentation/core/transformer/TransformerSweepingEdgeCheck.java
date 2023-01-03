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

public class TransformerSweepingEdgeCheck extends Transformer {

    private static final String CLASS = "net.minecraft.entity.player.EntityPlayer";
    
    @Override
    public boolean needToComputeFrames() {
        return true;
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
            MethodNode attack = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/entity/player/EntityPlayer", "func_71059_n",
                    Type.VOID_TYPE, Type.getType("Lnet/minecraft/entity/Entity;")), "(Lnet/minecraft/entity/Entity;)V");
            int ret = TransformUtil.findFirstInstanceOf(attack, 3, "net/minecraft/item/ItemSword");
            if (ret != -1 && ret < attack.instructions.size() - 1 && attack.instructions.get(ret).getNext() instanceof JumpInsnNode) {
                AbstractInsnNode insertAfter = attack.instructions.get(ret).getNext();
                LabelNode newLabel = new LabelNode(new Label());
                attack.instructions.insert(insertAfter, newLabel);

                insertAfter = attack.instructions.get(ret).getPrevious().getPrevious().getPrevious();
                attack.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.IFNE, newLabel));
                attack.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_COMMON,
                        "checkSweepingEdge",
                        "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Z",
                        false
                ));
                attack.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 13));
                attack.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
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
