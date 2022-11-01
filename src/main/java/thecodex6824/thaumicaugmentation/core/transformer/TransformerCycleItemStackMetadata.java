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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class TransformerCycleItemStackMetadata extends Transformer {

    private static final String CLASS = "thaumcraft.common.lib.utils.InventoryUtils";
    
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
            MethodNode cycle = TransformUtil.findMethod(classNode, "cycleItemStack", "(Ljava/lang/Object;I)Lnet/minecraft/item/ItemStack;");
            int ret = TransformUtil.findFirstInstanceOfMethodCall(cycle, 0, TransformUtil.remapMethodName("net/minecraft/item/ItemStack", "func_77984_f", Type.BOOLEAN_TYPE), "()Z", "net/minecraft/item/ItemStack");
            if (ret != -1) {
                JumpInsnNode target = (JumpInsnNode) cycle.instructions.get(ret).getNext();
                Label targetLabel = target.label.getLabel();
                int labelIndex = TransformUtil.findExactLabel(cycle, targetLabel);
                if (labelIndex != -1) {
                    ret = TransformUtil.findFirstInstanceOfOpcode(cycle, labelIndex, Opcodes.ARETURN);
                    if (ret != -1) {
                        InsnList insns = new InsnList();
                        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
                        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                TransformUtil.HOOKS_COMMON,
                                "cycleItemStack",
                                "(Lnet/minecraft/item/ItemStack;Ljava/lang/Object;I)Lnet/minecraft/item/ItemStack;",
                                false
                        ));

                        cycle.instructions.insert(cycle.instructions.get(ret).getPrevious(), insns);
                    }
                    else
                        throw new TransformerException("Could not locate return after jump target");
                }
                else
                    throw new TransformerException("Could not locate jump target label");
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
