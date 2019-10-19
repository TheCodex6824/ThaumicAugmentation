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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import thecodex6824.thaumicaugmentation.core.ThaumicAugmentationCore;

public class TransformerWardBlockHardness extends Transformer {

    private static final String CLASS = "net.minecraft.block.state.BlockStateContainer$StateImplementation";
    
    @Override
    public boolean isTransformationNeeded(String transformedName) {
        return !ThaumicAugmentationCore.getConfig().getBoolean("DisableWardFocus", "gameplay.ward", false, "") &&
                transformedName.equals(CLASS);
    }
    
    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
        try {
            MethodNode hardness = TransformUtil.findMethod(classNode, "getBlockHardness", "func_185887_b");
            boolean found = false;
            int ret = 0;
            while ((ret = TransformUtil.findFirstInstanceOfOpcode(hardness, ret, Opcodes.FRETURN)) != -1) {
                AbstractInsnNode insertAfter = hardness.instructions.get(ret).getPrevious();
                hardness.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "thecodex6824/thaumicaugmentation/common/internal/TAHooks",
                        "checkWardHardness",
                        "(FLnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)F",
                        false
                ));
                hardness.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 2));
                hardness.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 1));
                ret += 4;
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
