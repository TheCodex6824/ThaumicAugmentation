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

public class TransformerWardBlockGrassPath extends Transformer {

    private static final String CLASS = "net.minecraft.item.ItemSpade";

    @Override
    public boolean needToComputeFrames() {
        return false;
    }

    @Override
    public boolean isTransformationNeeded(ClassNode node, String transformedName) {
        return !ThaumicAugmentationCore.getConfig().getBoolean("DisableWardFocus", "gameplay.ward", false, "")
                && transformedName.equals(CLASS);
    }

    @Override
    public boolean isAllowedToFail() {
        return false;
    }

    @Override
    public boolean transform(ClassNode classNode, String transformedName) {
        try {
            MethodNode use = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName(
                    "net/minecraft/item/Item", "func_180614_a", Type.getType("Lnet/minecraft/util/EnumActionResult;"),
                    Type.getType("Lnet/minecraft/entity/player/EntityPlayer;"),
                    Type.getType("Lnet/minecraft/world/World;"), Type.getType("Lnet/minecraft/util/math/BlockPos;"),
                    Type.getType("Lnet/minecraft/util/EnumHand;"), Type.getType("Lnet/minecraft/util/EnumFacing;"),
                    Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE),
                    "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;");
            boolean found = false;
            int ret = TransformUtil.findFirstField(use, 0,
                    TransformUtil.remapFieldName("net/minecraft/init/Blocks", "field_150349_c"),
                    "Lnet/minecraft/block/BlockGrass;", "net/minecraft/init/Blocks");
            if (ret != -1) {
                AbstractInsnNode test = use.instructions.get(ret);
                if (test.getNext() instanceof JumpInsnNode) {
                    // flow appears to differ between dev and production (frames rewritten?)
                    JumpInsnNode jump = (JumpInsnNode) test.getNext();
                    if (jump.getOpcode() == Opcodes.IF_ACMPNE) {
                        use.instructions.insert(jump, new JumpInsnNode(Opcodes.IFEQ, jump.label));
                        use.instructions.insert(jump,
                                new MethodInsnNode(Opcodes.INVOKESTATIC, TransformUtil.HOOKS_COMMON, "checkWardGeneric",
                                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", false));
                        use.instructions.insert(jump, new VarInsnNode(Opcodes.ALOAD, 3));
                        use.instructions.insert(jump, new VarInsnNode(Opcodes.ALOAD, 2));

                        found = true;
                    }
                    else if (jump.getOpcode() == Opcodes.IF_ACMPEQ && ret >= 3) {
                        AbstractInsnNode threeBack = jump.getPrevious().getPrevious().getPrevious();
                        if (threeBack instanceof JumpInsnNode && threeBack.getOpcode() == Opcodes.IF_ACMPNE) {
                            jump = (JumpInsnNode) threeBack;
                            use.instructions.insert(jump, new JumpInsnNode(Opcodes.IFEQ, jump.label));
                            use.instructions.insert(jump,
                                    new MethodInsnNode(Opcodes.INVOKESTATIC, TransformUtil.HOOKS_COMMON,
                                            "checkWardGeneric",
                                            "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", false));
                            use.instructions.insert(jump, new VarInsnNode(Opcodes.ALOAD, 3));
                            use.instructions.insert(jump, new VarInsnNode(Opcodes.ALOAD, 2));

                            found = true;
                        }
                    }
                }
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
