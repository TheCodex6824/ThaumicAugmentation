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

package thecodex6824.thaumicaugmentation.api.augment.builder.impulsecannon;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.common.item.ItemImpulseCannonConversion;

import java.util.Map;

public interface IImpulseCannonConversion extends IImpulseCannonAugment {

    /**
     * @return the model resource location corresponding to the cannon model when this conversion is installed.
     * Must be registered as a variant of the cannon item using
     * {@link net.minecraftforge.client.model.ModelLoader#registerItemVariants(Item, ResourceLocation...)}
     */
    default @NotNull ModelResourceLocation getLensModel() {
        return ItemImpulseCannonConversion.LENS_BEAM;
    }

    /**
     * @param cannonStack  the itemstack of the cannon
     * @param augmentStack the itemstack of this augment
     * @param user         the entity using the cannon
     * @return whether the cannon should be ticked. Must be true for
     * {@link #onCannonTick(ItemStack, ItemStack, EntityLivingBase, int, double, IImpetusStorage, Map)} and
     * {@link #onStopCannonTick(ItemStack, ItemStack, EntityLivingBase, int, IImpetusStorage, Map)}
     * to be called or {@link #getImpetusCostPerTick(ItemStack, ItemStack, EntityLivingBase, int, IImpetusStorage)} to be considered.
     */
    public boolean isTickable(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user);

    /**
     * Called when the cannon this conversion is in starts being used.
     *
     * @param cannonStack     the itemstack of the cannon
     * @param augmentStack    the itemstack of this augment
     * @param user            the entity using the impulse cannon this conversion is in.
     * @param impetusConsumed the impetus consumed to start using the cannon.
     * @param augments        the list of augments that may modify the cannon's behavior. THE IMPLEMENTOR WILL BE IN THIS LIST
     */
    public default void onCannonUsage(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, long impetusConsumed, IImpetusStorage buffer, Map<ItemStack, IImpulseCannonAugment> augments) {}

    /**
     * Called every tick the cannon is used in.
     *
     * @param cannonStack     the itemstack of the cannon
     * @param augmentStack    the itemstack of this augment
     * @param user            the entity using the impulse cannon this conversion is in.
     * @param useTicksLeft    the number of ticks left in {@link #getMaxUsageDuration(ItemStack, ItemStack)}
     * @param impetusConsumed the impetus consumed this firing tick to use the cannon.
     * @param augments        the list of augments that may modify the cannon's behavior. THE IMPLEMENTOR WILL BE IN THIS LIST
     */
    public default void onCannonTick(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, int useTicksLeft, double impetusConsumed, IImpetusStorage buffer, Map<ItemStack, IImpulseCannonAugment> augments) {}

    /**
     * Called when the cannon stops being used.
     *
     * @param cannonStack  the itemstack of the cannon
     * @param augmentStack the itemstack of this augment
     * @param user         the entity no longer using the impulse cannon this conversion is in.
     * @param useTicksLeft the number of ticks left in {@link #getMaxUsageDuration(ItemStack, ItemStack)}
     * @param augments     the list of augments that may modify the cannon's behavior. THE IMPLEMENTOR WILL BE IN THIS LIST
     */
    public default void onStopCannonTick(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, int useTicksLeft, IImpetusStorage buffer, Map<ItemStack, IImpulseCannonAugment> augments) {}

    /**
     * @param cannonStack  the itemstack of the cannon
     * @param augmentStack the itemstack of this augment
     * @param user         the entity attempting to use the impulse cannon this conversion is in.
     * @param buffer       the associated impetus buffer. Should not be modified here.
     * @return the impetus that must be consumed for the cannon to start being used.
     */
    public default long getImpetusCostPerUsage(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer) {
        return 0;
    }

    /**
     * @param cannonStack  the itemstack of the cannon
     * @param augmentStack the itemstack of this augment
     * @param user         the entity currently using the impulse cannon this conversion is in.
     * @param useTicksLeft the number of ticks left in {@link #getMaxUsageDuration(ItemStack, ItemStack)}
     * @param buffer       the associated impetus buffer. Should not be modified here.
     * @return the impetus to consume this firing tick. Supports floating point.
     */
    public default double getImpetusCostPerTick(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, int useTicksLeft, IImpetusStorage buffer) {
        return 0.0;
    }

    /**
     * @param cannonStack  the itemstack of the cannon
     * @param augmentStack the itemstack of this augment
     * @return the number of ticks the conversion can be used for.
     */
    default int getMaxUsageDuration(ItemStack cannonStack, ItemStack augmentStack) {
        return 72000;
    }

    /**
     * Utility method for putting the cannon into cooldown.
     * @param user the entity currently using the impulse cannon this conversion is in.
     * @param cooldownTicks the number of ticks the cannon should cool down for.
     */
    default void handleCooldown(EntityLivingBase user, int cooldownTicks) {
        if (user instanceof EntityPlayer player) {
            player.getCooldownTracker().setCooldown(TAItems.IMPULSE_CANNON, cooldownTicks);
        }
    }

    @Override
    default boolean isCompatible(ItemStack otherAugment, IAugment otherAugmentCap) {
        return !(otherAugmentCap instanceof IImpulseCannonConversion);
    }
}
