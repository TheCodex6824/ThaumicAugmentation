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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class TransformerTouchTrajectoryEntitySelection extends Transformer {

private static final String CLASS = "thaumcraft.common.items.casters.foci.FocusMediumTouch";
    
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
            MethodNode exe = TransformUtil.findMethod(classNode, "supplyTrajectories", "()[Lthaumcraft/api/casters/Trajectory;");
            int ret = TransformUtil.findFirstInstanceOfMethodCall(exe, 0, "getPointedEntityRay",
                    "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;DDFZ)Lnet/minecraft/util/math/RayTraceResult;",
                    "thaumcraft/common/lib/utils/EntityUtils");
            if (ret != -1) {
                AbstractInsnNode insertAfter = exe.instructions.get(ret);
                exe.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_COMMON,
                        "fireTrajectoryGetEntityEvent",
                        "(Lnet/minecraft/util/math/RayTraceResult;Lthaumcraft/common/items/casters/foci/FocusMediumTouch;Lthaumcraft/api/casters/Trajectory;D)Lnet/minecraft/util/math/RayTraceResult;",
                        false
                ));
                exe.instructions.insert(insertAfter, new VarInsnNode(Opcodes.DLOAD, 2));
                exe.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 7));
                exe.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 0));
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
