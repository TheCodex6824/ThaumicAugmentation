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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import thecodex6824.thaumicaugmentation.core.ThaumicAugmentationCore;

public class TransformerWardBlockNoSheepGrazing extends Transformer {

    private static final String CLASS = "net.minecraft.entity.ai.EntityAIEatGrass";
    
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
            MethodNode nom = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/entity/ai/EntityAIEatGrass", "func_75250_a",
                    Type.BOOLEAN_TYPE), "()Z");
            int tallGrass = TransformUtil.findLastInstanceOfOpcode(nom, nom.instructions.size(), Opcodes.IFEQ);
            int normalGrass = TransformUtil.findLastInstanceOfOpcode(nom, nom.instructions.size(), Opcodes.IF_ACMPNE);
            boolean found = false;
            if (tallGrass != -1 && normalGrass != -1) {
                AbstractInsnNode insertAfter = nom.instructions.get(tallGrass);
                AbstractInsnNode grassAfter = nom.instructions.get(normalGrass);
                if (insertAfter.getPrevious() instanceof MethodInsnNode && grassAfter.getPrevious() instanceof FieldInsnNode) {
                    MethodInsnNode insertPrev = (MethodInsnNode) insertAfter.getPrevious();
                    FieldInsnNode grassPrev = (FieldInsnNode) grassAfter.getPrevious();
                    if (insertPrev.name.equals("apply") && insertPrev.owner.equals("com/google/common/base/Predicate") && insertPrev.itf &&
                            grassPrev.name.equals(TransformUtil.remapFieldName("net/minecraft/init/Blocks", "field_150349_c")) && grassPrev.owner.equals("net/minecraft/init/Blocks") && grassPrev.desc.equals("Lnet/minecraft/block/BlockGrass;")) {
                        
                        nom.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.IFEQ, ((JumpInsnNode) insertAfter).label));
                        nom.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                TransformUtil.HOOKS_COMMON,
                                "checkWardGeneric",
                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
                                false
                        ));
                        nom.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 1));
                        nom.instructions.insert(insertAfter, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/ai/EntityAIEatGrass",
                                TransformUtil.remapFieldName("net/minecraft/entity/ai/EntityAIEatGrass", "field_151501_c"), "Lnet/minecraft/world/World;"));
                        nom.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
                        
                        nom.instructions.insert(grassAfter, new JumpInsnNode(Opcodes.IFEQ, ((JumpInsnNode) grassAfter).label));
                        nom.instructions.insert(grassAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                TransformUtil.HOOKS_COMMON,
                                "checkWardGeneric",
                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
                                false
                        ));
                        nom.instructions.insert(grassAfter, new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                "net/minecraft/util/math/BlockPos",
                                TransformUtil.remapMethodName("net/minecraft/util/math/BlockPos", "func_177977_b", Type.getType("Lnet/minecraft/util/math/BlockPos;")),
                                "()Lnet/minecraft/util/math/BlockPos;",
                                false
                        ));
                        nom.instructions.insert(grassAfter, new VarInsnNode(Opcodes.ALOAD, 1));
                        nom.instructions.insert(grassAfter, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/ai/EntityAIEatGrass",
                                TransformUtil.remapFieldName("net/minecraft/entity/ai/EntityAIEatGrass", "field_151501_c"), "Lnet/minecraft/world/World;"));
                        nom.instructions.insert(grassAfter, new VarInsnNode(Opcodes.ALOAD, 0));
                        found = true;
                    }
                }
            }
            
            if (!found)
                throw new TransformerException("Could not locate required instructions, locations: " + tallGrass + ", " + normalGrass);
            
            return true;
        }
        catch (Throwable anything) {
            error = new RuntimeException(anything);
            return false;
        }
    }
    
}
