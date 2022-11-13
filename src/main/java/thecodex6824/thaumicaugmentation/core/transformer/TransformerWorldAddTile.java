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

public class TransformerWorldAddTile extends Transformer {

    private static final String CLASS = "net.minecraft.world.World";
    
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
            Type tileType = Type.getType("Lnet/minecraft/tileentity/TileEntity;");
            Type blockPosType = Type.getType("Lnet/minecraft/util/math/BlockPos;");
            MethodNode update = TransformUtil.findMethod(classNode, TransformUtil.remapMethodName("net/minecraft/world/World", "func_72939_s",
                    Type.VOID_TYPE),
                    "()V");
            int call = TransformUtil.findLastInstanceOfMethodCall(update, update.instructions.size(), TransformUtil.remapMethodName(
                    "net/minecraft/world/chunk/Chunk","func_177426_a",
                        Type.VOID_TYPE, blockPosType, tileType),
                    "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", "net/minecraft/world/chunk/Chunk");
            if (call != -1) {
                LabelNode target = new LabelNode(new Label());
                InsnList insns = new InsnList();
                insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_COMMON,
                        "onAddTile",
                        "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)Z",
                        false
                ));
                insns.add(new JumpInsnNode(Opcodes.IFEQ, target));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 4));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 3));
                insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/tileentity/TileEntity",
                        TransformUtil.remapMethodName("net/minecraft/tileentity/TileEntity", "func_174877_v", blockPosType),
                        "()Lnet/minecraft/util/math/BlockPos;",
                        false
                ));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 3));

                update.instructions.insert(update.instructions.get(call), target);
                update.instructions.insert(update.instructions.get(call).getPrevious(), insns);
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