package thecodex6824.thaumicaugmentation.api.augment.impl.custom;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public interface IBuilderCannonStrengthProvider extends IBuilderStrengthProvider {

    double calculateStrength(ICustomAugment augment, ItemStack cannonStack, Entity user);
}
