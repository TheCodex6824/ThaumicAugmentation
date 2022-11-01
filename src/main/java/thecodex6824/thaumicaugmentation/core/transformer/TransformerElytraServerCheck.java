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

public class TransformerElytraServerCheck extends Transformer {

    private static final String CLASS = "net.minecraft.network.NetHandlerPlayServer";
    
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
            MethodNode check = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/network/NetHandlerPlayServer", "func_147357_a",
                    Type.VOID_TYPE, Type.getType("Lnet/minecraft/network/play/client/CPacketEntityAction;")), "(Lnet/minecraft/network/play/client/CPacketEntityAction;)V");
            int ret = TransformUtil.findFirstInstanceOfMethodCall(check, 3, TransformUtil.remapMethodName("net/minecraft/entity/player/EntityPlayerMP", "func_184582_a",
                    Type.getType("Lnet/minecraft/item/ItemStack;"), Type.getType("Lnet/minecraft/inventory/EntityEquipmentSlot;")),
                    "(Lnet/minecraft/inventory/EntityEquipmentSlot;)Lnet/minecraft/item/ItemStack;", "net/minecraft/entity/player/EntityPlayerMP");
            if (ret != -1) {
                AbstractInsnNode insertAfter = check.instructions.get(ret).getNext();
                check.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_COMMON,
                        "checkElytra",
                        "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayerMP;)V",
                        false
                ));
                check.instructions.insert(insertAfter, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/NetHandlerPlayServer",
                        TransformUtil.remapFieldName("net/minecraft/network/NetHandlerPlayServer", "field_147369_b"), "Lnet/minecraft/entity/player/EntityPlayerMP;"));
                check.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
                check.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 2));
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
