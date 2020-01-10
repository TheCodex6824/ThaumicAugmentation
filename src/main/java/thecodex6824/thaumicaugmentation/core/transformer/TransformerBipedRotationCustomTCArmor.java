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

import net.minecraft.entity.Entity;

/**
 * Patches the ModelCustomArmor class to call the super method in setRotationAngles to fix
 * a whole lot of issues with other mods that hook into ModelBiped. For some reason, instead of calling the
 * super method, Azanor copied + pasted the code...
 */
public class TransformerBipedRotationCustomTCArmor extends Transformer {

    private static final String CLASS = "thaumcraft.client.renderers.models.gear.ModelCustomArmor";
    
    @Override
    public boolean isTransformationNeeded(String transformedName) {
        return transformedName.equals(CLASS);
    }
    
    @Override
    public boolean transform(ClassNode classNode, String name, String transformedName) {
        try {
            String rotationAngles = TransformUtil.remapMethodName("thaumcraft/client/renderers/models/gear/ModelCustomArmor", "func_78087_a",
                    Type.VOID_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE,
                    Type.getType(Entity.class));
            MethodNode rot = TransformUtil.findMethod(classNode, rotationAngles, "(FFFFFFLnet/minecraft/entity/Entity;)V");
            if (rot.instructions.size() != 1009 || rot.localVariables.size() != 15)
                throw new TransformerException("setRotationAngles function is not the expected size, this transformer will almost certainly break it");
       
            int ret = TransformUtil.findLineNumber(rot, 38);
            if (ret == -1)
                throw new TransformerException("Could not locate required instructions");
            
            AbstractInsnNode insertAfter = rot.instructions.get(ret).getNext();
            rot.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESPECIAL,
                    "net/minecraft/client/model/ModelBiped",
                    rotationAngles,
                    "(FFFFFFLnet/minecraft/entity/Entity;)V",
                    false
            ));
            rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 7));
            rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.FLOAD, 6));
            rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.FLOAD, 5));
            rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.FLOAD, 4));
            rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.FLOAD, 3));
            rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.FLOAD, 2));
            rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.FLOAD, 1));
            rot.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
            ret += 11;
            
            // remove copied+pasted code that is now done in super call
            // PSA: DO NOT REMOVE INSTRUCTIONS UNLESS THERE IS NO OTHER OPTION, IT BREAKS EVERYTHING
            int end = TransformUtil.findLineNumber(rot, 206);
            if (end == -1)
                throw new TransformerException("Could not locate required instructions");
            
            for (int i = 0; i < end - ret - 1; ++i)
                rot.instructions.remove(rot.instructions.get(ret));
            
            // strip now unused locals
            for (int i = 0; i < 7; ++i)
                rot.localVariables.remove(rot.localVariables.get(i));
            
            rot.maxLocals -= 7;
            return true;
        }
        catch (Throwable anything) {
            error = new RuntimeException(anything);
            return false;
        }
    }
    
}
