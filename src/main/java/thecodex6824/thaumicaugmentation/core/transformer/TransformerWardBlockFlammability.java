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
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import thecodex6824.thaumicaugmentation.core.ThaumicAugmentationCore;

public class TransformerWardBlockFlammability extends Transformer {

    private static final String CLASS = "net.minecraft.block.BlockFire";

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
	    MethodNode fire = TransformUtil.findMethod(
		    classNode,
		    "tryCatchFire",
		    "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILjava/util/Random;ILnet/minecraft/util/EnumFacing;)V"
		    );

	    int ret = 0;
	    if ((ret = TransformUtil.findFirstInstanceOfMethodCall(
		    fire,
		    ret,
		    "getFlammability",
		    "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I",
		    "net/minecraft/block/Block"
		    )
		    ) != -1) {
		InsnList toInsert = new InsnList();
		toInsert.add(new InsnNode(Opcodes.DUP));
		toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
		toInsert.add(new VarInsnNode(Opcodes.ALOAD, 2));
		toInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
			TransformUtil.HOOKS_COMMON,
			"checkWardFlammability",
			"(ILnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I",
			false
			));
		toInsert.add(new InsnNode(Opcodes.IAND));

		fire.instructions.insert(fire.instructions.get(ret), toInsert);
		return true;
	    }

	    throw new TransformerException("Could not locate required instructions");
	}
	catch (Throwable anything) {
	    error = new RuntimeException(anything);
	    return false;
	}
    }

}
