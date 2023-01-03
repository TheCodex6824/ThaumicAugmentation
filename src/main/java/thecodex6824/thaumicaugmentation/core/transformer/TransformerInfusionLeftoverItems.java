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
import org.objectweb.asm.tree.*;

public class TransformerInfusionLeftoverItems extends Transformer {

    private static final String CLASS = "thaumcraft.common.tiles.crafting.TileInfusionMatrix";
    
    @Override
    public boolean needToComputeFrames() {
        return false;
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
            MethodNode finish = TransformUtil.findMethod(classNode, "craftCycle", "()V");
            int offset = TransformUtil.findLastInstanceOfMethodCall(finish, finish.instructions.size(), "getContainerItem",
                    "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", "net/minecraft/item/Item");
            if (offset != -1) {
                AbstractInsnNode insertAfter = finish.instructions.get(offset);
                finish.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_COMMON,
                        "getLeftoverInfusionIngredientStack",
                        "(Lnet/minecraft/item/ItemStack;Ljava/lang/Object;)Lnet/minecraft/item/ItemStack;",
                        false
                ));
                finish.instructions.insert(insertAfter, new FieldInsnNode(Opcodes.GETFIELD, "thaumcraft/common/tiles/crafting/TileInfusionMatrix",
                        "recipeOutput", "Ljava/lang/Object;"));
                finish.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
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
