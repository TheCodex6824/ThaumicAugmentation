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
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class TransformerRunicShieldingAllowBaublesCap extends Transformer {

    private static final String CLASS = "thaumcraft.common.lib.crafting.InfusionRunicAugmentRecipe";
    
    @Override
    public boolean needToComputeFrames() {
        return false;
    }
    
    @Override
    public boolean isTransformationNeeded(String transformedName) {
        return transformedName.equals(CLASS);
    }
    
    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
        try {
            MethodNode match = TransformUtil.findMethod(classNode, "matches",
                    "(Ljava/util/List;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Z");
            int offset = 0;
            while ((offset = TransformUtil.findFirstInstanceOfOpcode(match, offset, Opcodes.INSTANCEOF)) != -1) {
                TypeInsnNode check = (TypeInsnNode) match.instructions.get(offset);
                if (check.desc.equals("baubles/api/IBauble")) {
                    AbstractInsnNode insertAfter = match.instructions.get(offset).getNext();
                    match.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.IFNE, ((JumpInsnNode) insertAfter).label));
                    match.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                            TransformUtil.HOOKS_COMMON,
                            "shouldAllowRunicShield",
                            "(Lnet/minecraft/item/ItemStack;)Z",
                            false
                    ));
                    match.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 2));
                    
                    return true;
                }
                
                offset += 2;
            }
            
            throw new RuntimeException("Could not locate required instructions");
        }
        catch (Throwable anything) {
            error = new RuntimeException(anything);
            return false;
        }
    }
    
}
