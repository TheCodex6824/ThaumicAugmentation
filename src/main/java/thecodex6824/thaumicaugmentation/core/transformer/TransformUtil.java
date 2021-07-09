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

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public final class TransformUtil {

    private TransformUtil() {}
    
    public static final String HOOKS_COMMON = "thecodex6824/thaumicaugmentation/common/internal/TAHooksCommon";
    public static final String HOOKS_CLIENT = "thecodex6824/thaumicaugmentation/client/internal/TAHooksClient";
    
    public static String remapFieldName(String internalName, String fieldName) {
        String internal = FMLDeobfuscatingRemapper.INSTANCE.unmap(internalName);
        return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(internal, fieldName, null);
    }
    
    public static String remapMethodName(String internalName, String methodName, Type returnType, Type... parameterTypes) {
        String internal = FMLDeobfuscatingRemapper.INSTANCE.unmap(internalName);
        String desc = Type.getMethodDescriptor(returnType, parameterTypes);
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(internal, methodName, desc);
    }
    
    public static MethodNode findMethod(ClassNode classNode, String name) {
        for (MethodNode m : classNode.methods) {
            if (m.name.equals(name))
                return m;
        }
        
        return null;
    }
    
    public static MethodNode findMethod(ClassNode classNode, String name, String desc) {
        for (MethodNode m : classNode.methods) {
            if (m.name.equals(name) && m.desc.equals(desc))
                return m;
        }
        
        return null;
    }
    
    public static int findFirstInstanceOfOpcode(MethodNode node, int startIndex, int opcode) {
        if (startIndex < 0 || startIndex >= node.instructions.size())
            return -1;
        
        for (int i = startIndex; i < node.instructions.size(); ++i) {
            if (node.instructions.get(i).getOpcode() == opcode)
                return i;
        }
        
        return -1;
    }
    
    public static int findLastInstanceOfOpcode(MethodNode node, int endIndex, int opcode) {
        if ((endIndex - 1) < 0 || (endIndex - 1) >= node.instructions.size())
            return -1;
        
        for (int i = endIndex - 1; i >= 0; --i) {
            if (node.instructions.get(i).getOpcode() == opcode)
                return i;
        }
        
        return -1;
    }
    
    public static int findFirstInstanceOfMethodCall(MethodNode node, int startIndex, String name, String desc,
            String owningClass) {
        
        if (startIndex < 0 || startIndex >= node.instructions.size())
            return -1;
        
        for (int i = startIndex; i < node.instructions.size(); ++i) {
            AbstractInsnNode insn = node.instructions.get(i);
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode method = (MethodInsnNode) insn;
                if (method.name.equals(name) && method.desc.equals(desc) && 
                        method.owner.equals(owningClass))
                    return i;
            }
        }
        
        return -1;
    }
    
    public static int findLastInstanceOfMethodCall(MethodNode node, int endIndex, String name, String desc,
            String owningClass) {
        
        if ((endIndex - 1) < 0 || (endIndex - 1) >= node.instructions.size())
            return -1;
        
        for (int i = endIndex - 1; i >= 0; --i) {
            AbstractInsnNode insn = node.instructions.get(i);
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode method = (MethodInsnNode) insn;
                if (method.name.equals(name) && method.desc.equals(desc) && 
                        method.owner.equals(owningClass))
                    return i;
            }
        }
        
        return -1;
    }
    
    public static int findFirstLabel(MethodNode node, int startIndex) {
        
        if (startIndex < 0 || startIndex >= node.instructions.size())
            return -1;
        
        for (int i = startIndex; i < node.instructions.size(); ++i) {
            AbstractInsnNode insn = node.instructions.get(i);
            if (insn instanceof LabelNode)
                return i;
        }
        
        return -1;
    }
    
    public static int findLastLabel(MethodNode node, int endIndex) {
        
        if ((endIndex - 1) < 0 || (endIndex - 1) >= node.instructions.size())
            return -1;
        
        for (int i = endIndex - 1; i >= 0; --i) {
            AbstractInsnNode insn = node.instructions.get(i);
            if (insn instanceof LabelNode)
                return i;
        }
        
        return -1;
    }
    
    public static int findLineNumber(MethodNode node, int number) {
        
        for (int i = 0; i < node.instructions.size(); ++i) {
            AbstractInsnNode insn = node.instructions.get(i);
            if (insn instanceof LineNumberNode && ((LineNumberNode) insn).line == number)
                return i;
        }
        
        return -1;
    }
    
    public static int findFirstField(MethodNode node, int startIndex, String name, String desc,
            String owningClass) {
        
        if (startIndex < 0 || startIndex >= node.instructions.size())
            return -1;
        
        for (int i = startIndex; i < node.instructions.size(); ++i) {
            AbstractInsnNode insn = node.instructions.get(i);
            if (insn instanceof FieldInsnNode) {
                FieldInsnNode field = (FieldInsnNode) insn;
                if (field.name.equals(name) && field.desc.equals(desc) &&
                        field.owner.equals(owningClass))
                    return i;
            }
        }
        
        return -1;
        
    }
    
    public static int findLastField(MethodNode node, int endIndex, String name, String desc,
            String owningClass) {
        
        if ((endIndex - 1) < 0 || (endIndex - 1) >= node.instructions.size())
            return -1;
        
        for (int i = endIndex - 1; i >= 0; --i) {
            AbstractInsnNode insn = node.instructions.get(i);
            if (insn instanceof FieldInsnNode) {
                FieldInsnNode field = (FieldInsnNode) insn;
                if (field.name.equals(name) && field.desc.equals(desc) &&
                        field.owner.equals(owningClass))
                    return i;
            }
        }
        
        return -1;
        
    }
    
}
