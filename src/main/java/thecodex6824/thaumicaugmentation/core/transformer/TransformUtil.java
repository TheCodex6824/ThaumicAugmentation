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

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class TransformUtil {

    private TransformUtil() {}
    
    public static MethodNode findMethod(ClassNode classNode, String deobf, String obf) {
        for (MethodNode m : classNode.methods) {
            if (m.name.equals(obf) || m.name.equals(deobf))
                return m;
        }
        
        return null;
    }
    
    public static MethodNode findMethod(ClassNode classNode, String deobf, String obf, String desc) {
        for (MethodNode m : classNode.methods) {
            if ((m.name.equals(obf) || m.name.equals(deobf)) && m.desc.equals(desc))
                return m;
        }
        
        return null;
    }
    
    public static int findFirstInstanceOfOpcode(MethodNode node, int startIndex, int opcode) {
        for (int i = startIndex; i < node.instructions.size(); ++i) {
            if (node.instructions.get(i).getOpcode() == opcode)
                return i;
        }
        
        return -1;
    }
    
}
