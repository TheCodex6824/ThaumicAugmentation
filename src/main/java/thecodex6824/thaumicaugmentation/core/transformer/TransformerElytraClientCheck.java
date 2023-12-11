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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TransformerElytraClientCheck extends Transformer {

    private static final String CLASS = "net.minecraft.client.entity.EntityPlayerSP";

    @Override
    public boolean needToComputeFrames() {
        return false;
    }

    @Override
    public boolean isTransformationNeeded(ClassNode node, String transformedName) {
        return transformedName.equals(CLASS);
    }

    @Override
    public boolean isAllowedToFail() {
        return false;
    }

    @Override
    public boolean transform(ClassNode classNode, String transformedName) {
        try {
            MethodNode livingUpdate = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName(
                    "net/minecraft/client/entity/EntityPlayerSP", "func_70636_d", Type.VOID_TYPE), "()V");
            int ret = TransformUtil.findFirstInstanceOfMethodCall(livingUpdate, 0,
                    TransformUtil.remapMethodName("net/minecraft/client/entity/EntityPlayerSP", "func_184582_a",
                            Type.getType("Lnet/minecraft/item/ItemStack;"),
                            Type.getType("Lnet/minecraft/inventory/EntityEquipmentSlot;")),
                    "(Lnet/minecraft/inventory/EntityEquipmentSlot;)Lnet/minecraft/item/ItemStack;",
                    "net/minecraft/client/entity/EntityPlayerSP");
            if (ret != -1) {
                AbstractInsnNode insertAfter = livingUpdate.instructions.get(ret).getNext();
                livingUpdate.instructions.insert(insertAfter,
                        new MethodInsnNode(Opcodes.INVOKESTATIC, TransformUtil.HOOKS_CLIENT, "checkElytra",
                                "(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false));
                livingUpdate.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
            }
            else {
                // Player API deletes the entire method, and moves it to its own (!)
                livingUpdate = TransformUtil.findMethod(classNode, "localOnLivingUpdate", "()V");
                ret = TransformUtil.findFirstInstanceOfMethodCall(livingUpdate, 0,
                        TransformUtil.remapMethodName("net/minecraft/client/entity/EntityPlayerSP", "func_184582_a",
                                Type.getType("Lnet/minecraft/item/ItemStack;"),
                                Type.getType("Lnet/minecraft/inventory/EntityEquipmentSlot;")),
                        "(Lnet/minecraft/inventory/EntityEquipmentSlot;)Lnet/minecraft/item/ItemStack;",
                        "net/minecraft/client/entity/EntityPlayerSP");
                if (ret != -1) {
                    AbstractInsnNode insertAfter = livingUpdate.instructions.get(ret).getNext();
                    livingUpdate.instructions.insert(insertAfter,
                            new MethodInsnNode(Opcodes.INVOKESTATIC, TransformUtil.HOOKS_CLIENT, "checkElytra",
                                    "(Lnet/minecraft/client/entity/EntityPlayerSP;)V", false));
                    livingUpdate.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
                }
                else
                    throw new TransformerException("Could not locate required instructions");
            }

            return true;
        }
        catch (Throwable anything) {
            error = new RuntimeException(anything);
            return false;
        }
    }

}
