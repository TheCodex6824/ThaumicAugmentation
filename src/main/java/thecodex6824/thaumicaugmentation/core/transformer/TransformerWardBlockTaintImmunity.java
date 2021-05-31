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

import thecodex6824.thaumicaugmentation.core.ThaumicAugmentationCore;

public class TransformerWardBlockTaintImmunity extends Transformer {

private static final String CLASS = "thaumcraft.common.blocks.world.taint.TaintHelper";
    
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
            MethodNode spread = TransformUtil.findMethod(classNode, "spreadFibres", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)V");
            int offset = TransformUtil.findFirstInstanceOfMethodCall(spread, 0, TransformUtil.remapMethodName("net/minecraft/block/Block", "func_176195_g", Type.FLOAT_TYPE,
                    Type.getType("Lnet/minecraft/block/state/IBlockState;"), Type.getType("Lnet/minecraft/world/World;"), Type.getType("Lnet/minecraft/util/math/BlockPos;")),
                    "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)F", "net/minecraft/block/Block");
            if (offset != -1) {
                AbstractInsnNode insertAfter = spread.instructions.get(offset).getNext();
                spread.instructions.insert(insertAfter, new VarInsnNode(Opcodes.FSTORE, 10));
                spread.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKEINTERFACE, 
                        "net/minecraft/block/state/IBlockProperties",
                        TransformUtil.remapMethodName("net/minecraft/block/state/IBlockProperties", "func_185887_b", Type.FLOAT_TYPE,
                                Type.getType("Lnet/minecraft/world/World;"), Type.getType("Lnet/minecraft/util/math/BlockPos;")),
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)F",
                        true
                ));
                spread.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 7));
                spread.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
                spread.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 8));
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
