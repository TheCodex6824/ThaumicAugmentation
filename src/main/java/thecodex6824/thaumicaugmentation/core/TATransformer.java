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

package thecodex6824.thaumicaugmentation.core;

import java.util.ArrayList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;
import thecodex6824.thaumicaugmentation.core.transformer.ITransformer;
import thecodex6824.thaumicaugmentation.core.transformer.TransformerThaumostaticHarnessSprintCheck;
import thecodex6824.thaumicaugmentation.core.transformer.TransformerWardBlockFireEncouragement;
import thecodex6824.thaumicaugmentation.core.transformer.TransformerWardBlockFlammability;
import thecodex6824.thaumicaugmentation.core.transformer.TransformerWardBlockHardness;
import thecodex6824.thaumicaugmentation.core.transformer.TransformerWardBlockNoEndermanPickup;
import thecodex6824.thaumicaugmentation.core.transformer.TransformerWardBlockNoRabbitSnacking;
import thecodex6824.thaumicaugmentation.core.transformer.TransformerWardBlockNoSheepGrazing;
import thecodex6824.thaumicaugmentation.core.transformer.TransformerWardBlockNoVillagerFarming;
import thecodex6824.thaumicaugmentation.core.transformer.TransformerWardBlockRandomTick;
import thecodex6824.thaumicaugmentation.core.transformer.TransformerWardBlockResistance;

public class TATransformer implements IClassTransformer {

    private static final ArrayList<ITransformer> TRANSFORMERS = new ArrayList<>(5);
    
    static {
        TRANSFORMERS.add(new TransformerWardBlockHardness());
        TRANSFORMERS.add(new TransformerWardBlockResistance());
        TRANSFORMERS.add(new TransformerWardBlockFlammability());
        TRANSFORMERS.add(new TransformerWardBlockFireEncouragement());
        TRANSFORMERS.add(new TransformerWardBlockRandomTick());
        
        // sadly EntityMobGriefingEvent does not provide a blockpos, so it can't be used
        // the position is also not determined and put into the AI fields until the event has already passed
        TRANSFORMERS.add(new TransformerWardBlockNoEndermanPickup());
        TRANSFORMERS.add(new TransformerWardBlockNoRabbitSnacking());
        TRANSFORMERS.add(new TransformerWardBlockNoSheepGrazing());
        TRANSFORMERS.add(new TransformerWardBlockNoVillagerFarming());
        TRANSFORMERS.add(new TransformerThaumostaticHarnessSprintCheck());
    }
    
    public TATransformer() {}
    
    private boolean isTransformNeeded(String transformedName) {
        if (!ThaumicAugmentationCore.isEnabled())
            return false;
        
        for (ITransformer t : TRANSFORMERS) {
            if (t.isTransformationNeeded(transformedName))
                return true;
        }
        
        return false;
    }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (isTransformNeeded(transformedName)) {
            ClassNode node = new ClassNode();
            ClassReader reader = new ClassReader(basicClass);
            reader.accept(node, 0);
            
            for (ITransformer transformer : TRANSFORMERS) {
                if (transformer.isTransformationNeeded(transformedName)) {
                    if (!transformer.transform(node, name, transformedName)) {
                        ThaumicAugmentationCore.getLogger().error("A class transformer has failed! This is probably very bad...");
                        ThaumicAugmentationCore.getLogger().error("Class: " + transformedName + ", Transformer: " + transformer.getClass());
                        if (transformer.getRaisedException() != null) {
                            ThaumicAugmentationCore.getLogger().error("Additional information:", transformer.getRaisedException());
                            throw transformer.getRaisedException();
                        }
                        else
                            throw new RuntimeException();
                    }
                }
            }
            
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            ThaumicAugmentationCore.getLogger().info("Successfully transformed class " + transformedName);
            return writer.toByteArray();
        }
        
        return basicClass;
    }
    
}
