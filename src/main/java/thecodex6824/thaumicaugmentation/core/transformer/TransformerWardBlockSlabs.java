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
import thecodex6824.thaumicaugmentation.core.ThaumicAugmentationCore;

public class TransformerWardBlockSlabs extends Transformer {

    private static final String CLASS = "net.minecraft.item.ItemSlab";
    
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
            MethodNode use = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/item/Item", "func_180614_a", Type.getType("Lnet/minecraft/util/EnumActionResult;"),
                    Type.getType("Lnet/minecraft/entity/player/EntityPlayer;"), Type.getType("Lnet/minecraft/world/World;"), Type.getType("Lnet/minecraft/util/math/BlockPos;"),
                            Type.getType("Lnet/minecraft/util/EnumHand;"), Type.getType("Lnet/minecraft/util/EnumFacing;"), Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
                    "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;");
            int ret = TransformUtil.findFirstInstanceOfMethodCall(use, 0, TransformUtil.remapMethodName("net/minecraft/entity/player/EntityPlayer", "func_175151_a", Type.BOOLEAN_TYPE,
                            Type.getType("Lnet/minecraft/util/math/BlockPos;"), Type.getType("Lnet/minecraft/util/EnumFacing;"), Type.getType("Lnet/minecraft/item/ItemStack;")),
                    "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/item/ItemStack;)Z", "net/minecraft/entity/player/EntityPlayer");
            if (ret != -1 && use.instructions.get(ret).getNext() instanceof JumpInsnNode) {
                JumpInsnNode insertAfter = (JumpInsnNode) use.instructions.get(ret).getNext();
                use.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.IFEQ, insertAfter.label));
                use.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_COMMON,
                        "checkWardSlab",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/item/ItemStack;)Z",
                        false
                ));
                use.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 9));
                use.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 5));
                use.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 3));
                use.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 2));
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
