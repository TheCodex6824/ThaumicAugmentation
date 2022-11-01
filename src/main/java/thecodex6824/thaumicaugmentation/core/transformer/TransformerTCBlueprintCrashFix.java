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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class TransformerTCBlueprintCrashFix extends Transformer {

    private static final String CLASS = "thaumcraft.client.gui.GuiResearchPage";
    
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
            MethodNode render = TransformUtil.findMethod(classNode, "renderBluePrint",
                    "(Lthaumcraft/client/gui/GuiResearchPage$BlueprintBlockAccess;IIF[[[Lthaumcraft/api/crafting/Part;II[Lnet/minecraft/item/ItemStack;)V");
            int offset = TransformUtil.findFirstInstanceOfMethodCall(render, 0, "createTileEntity",
                    "(Lnet/minecraft/world/World;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/tileentity/TileEntity;",
                    "net/minecraft/block/Block");
            int nextLabel = TransformUtil.findFirstLabel(render, offset);
            int afterRenderJump = TransformUtil.findLastInstanceOfOpcode(render, offset, Opcodes.IFNE);
            int afterRegularTileRender = TransformUtil.findFirstInstanceOfMethodCall(render, offset,
                    TransformUtil.remapMethodName("net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "func_147549_a",
                            Type.VOID_TYPE, Type.getType("Lnet/minecraft/tileentity/TileEntity;"), Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.FLOAT_TYPE), "(Lnet/minecraft/tileentity/TileEntity;DDDF)V",
                    "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher");
            if (offset != -1 && nextLabel != -1 && afterRenderJump != -1 && afterRegularTileRender != -1) {
                LabelNode nLabel = (LabelNode) render.instructions.get(nextLabel);
                JumpInsnNode jump = (JumpInsnNode) render.instructions.get(afterRenderJump);
                AbstractInsnNode afterRender = render.instructions.get(afterRegularTileRender);
                render.instructions.insert(afterRender, new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/client/renderer/texture/TextureManager",
                        TransformUtil.remapMethodName("net/minecraft/client/renderer/texture/TextureManager", "func_110577_a", Type.VOID_TYPE,
                                Type.getType("Lnet/minecraft/util/ResourceLocation;")),
                        "(Lnet/minecraft/util/ResourceLocation;)V",
                        false
                ));
                render.instructions.insert(afterRender, new FieldInsnNode(Opcodes.GETSTATIC,
                        "net/minecraft/client/renderer/texture/TextureMap",
                        TransformUtil.remapFieldName("net/minecraft/client/renderer/texture/TextureMap", "field_110575_b"),
                        "Lnet/minecraft/util/ResourceLocation;"
                ));
                render.instructions.insert(afterRender, new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/client/Minecraft",
                        TransformUtil.remapMethodName("net/minecraft/client/Minecraft", "func_110434_K", Type.getType("Lnet/minecraft/client/renderer/texture/TextureManager;")),
                        "()Lnet/minecraft/client/renderer/texture/TextureManager;",
                        false
                ));
                render.instructions.insert(afterRender, new FieldInsnNode(Opcodes.GETFIELD,
                        "thaumcraft/client/gui/GuiResearchPage",
                        TransformUtil.remapFieldName("net/minecraft/client/gui/GuiScreen", "field_146297_k"),
                        "Lnet/minecraft/client/Minecraft;"
                ));
                render.instructions.insert(afterRender, new VarInsnNode(Opcodes.ALOAD, 0));
                
                AbstractInsnNode insertAfter = render.instructions.get(offset).getNext();
                render.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.GOTO, jump.label));
                render.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        TransformUtil.HOOKS_CLIENT,
                        "renderFastTESRBlueprint",
                        "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/math/BlockPos;Lthaumcraft/client/gui/GuiResearchPage$BlueprintBlockAccess;)V",
                        false
                ));
                render.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 1));
                render.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 17));
                render.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 22));
                render.instructions.insert(insertAfter, new JumpInsnNode(Opcodes.IFEQ, nLabel));
                render.instructions.insert(insertAfter, new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/tileentity/TileEntity",
                        "hasFastRenderer",
                        "()Z",
                        false
                ));
                render.instructions.insert(insertAfter, new VarInsnNode(Opcodes.ALOAD, 22));
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
