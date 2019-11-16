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

package thecodex6824.thaumicaugmentation.api.augment;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;

/**
 * Interface for the Augment capability. This interface allows arbitrary items to be added to
 * an {@link IAugmentableItem}, and respond to events created by the wearer.
 * @author TheCodex6824
 */
public interface IAugment {
    
    /**
     * Called on both the client and server when the augment is equipped on the user. This includes when the
     * user is first loaded!
     * @param user The entity that has this augment
     */
    public default void onEquip(Entity user) {}
    
    /**
     * Called on both the client and server when the augment is unequipped on the user.
     * @param user The entity that has this augment
     */
    public default void onUnequip(Entity user) {}
    
    /**
     * Called when the owning entity attempts to use a focus using a Thaumcraft caster. Note that this requires support from
     * the caster to work - Thaumic Augmentation casters all support this event, but both the default Thaumcraft
     * caster and possibly third party casters will need to fire events themselves.
     * @param caster The stack of the casting item
     * @param focus The FocusWrapper that is being used by the caster
     * @param user The entity that has this augment and is casting
     */
    public default void onCastPre(ItemStack caster, FocusWrapper focus, Entity user) {}
    
    /**
     * Called when the owning entity successfully uses a focus using a Thaumcraft caster. Note that this requires support from
     * the caster to work - Thaumic Augmentation casters all support this event, but both the default Thaumcraft
     * caster and possibly third party casters will need to fire events themselves.
     * @param caster The stack of the casting item
     * @param focus The FocusWrapper that is being used by the caster - at this point any modifications
     * to values here will have no effect
     * @param user The entity that has this augment and is casting
     */
    public default void onCastPost(ItemStack caster, FocusWrapper focus, Entity user) {}
    
    /**
     * Called when the owning entity is ticked. This is called on both the client and server sides.
     * @param user The entity that has this augment
     */
    public default void onTick(Entity user) {}
    
    /**
     * Called when the user hurts another entity.
     * @param user The entity that has this augment
     * @param attacked The entity that was attacked
     */
    public default void onHurtEntity(Entity user, Entity attacked) {}
    
    /**
     * Called when the user damages another entity.
     * @param user The entity that has this augment
     * @param attacked The entity that was damages
     */
    public default void onDamagedEntity(Entity user, Entity attacked) {}
    
    /**
     * Called when the user is hurt by another entity.
     * @param user The entity that has this augment
     * @param attacker The entity that attacked the user
     */
    public default void onHurt(Entity user, @Nullable Entity attacker) {}
    
    /**
     * Called when the user is damaged by another entity.
     * @param user The entity that has this augment
     * @param attacker The entity that damaged the user
     */
    public default void onDamaged(Entity user, @Nullable Entity attacker) {}
    
    /**
     * Called when the user interacts with an entity while holding the augmentable item.
     * @param user The entity that has this augment
     * @param used The stack of the augmentable item used with the interaction
     * @param target The entity that was interacted with
     * @param hand The hand that the user used for the interaction
     */
    public default void onInteractEntity(Entity user, ItemStack used, Entity target, EnumHand hand) {}
    
    /**
     * Called when the user interacts with a block while holding the augmentable item.
     * @param user The entity that has this augment
     * @param used The stack of the augmentable item used with the interaction
     * @param target The position of the block interacted with
     * @param face The face of the block that was interacted with
     * @param hand The hand that the user used for the interaction
     */
    public default void onInteractBlock(Entity user, ItemStack used, BlockPos target, EnumFacing face, EnumHand hand) {}  
    
    /**
     * Called when the user interacts with the air (no block or entity in range) while holding the augmentable item.
     * @param user The entity that has this augment
     * @param used The stack of the augmentable item used with the interaction
     * @param hand The hand that the user used for the interaction
     */
    public default void onInteractAir(Entity user, ItemStack used, EnumHand hand) {}
    
    /**
     * Called when the augmentable item is used, regardless of the type of usage.
     * @param user The entity that has this augment
     * @param used The stack of the augmentable item used
     */
    public default void onUseItem(Entity user, ItemStack used) {}
    
    /**
     * Returns if this augment can coexist on the same augmentable item as the passed augment.
     * @param otherAugment The augment to check
     * @return If this augment can exist on the same augmentable item as the passed augment
     */
    public default boolean isCompatible(ItemStack otherAugment) {
        return true;
    }
    
    /**
     * Returns if this augment can be applied to the passed augmentable item.
     * @param augmentable The augmentable item to check
     * @return If this augment can be applied to the passed augmentable item
     */
    public default boolean canBeAppliedToItem(ItemStack augmentable) {
        return true;
    }
    
    /**
     * Returns if this augment has an additional tooltip that should be rendered on the augmentable item. It is guaranteed that
     * {@link #appendAdditionalAugmentTooltip} will be called if this returns true.
     * @return If this augment has an additional tooltip
     */
    public default boolean hasAdditionalAugmentTooltip() {
        return false;
    }
    
    /**
     * Appends the custom augment tooltip.
     * @param tooltip The list to append the tooltip to
     */
    public default void appendAdditionalAugmentTooltip(List<String> tooltip) {}
    
    /**
     * Returns if this augment should be synced from the server to the client. This is guaranteed to only be called once per augment
     * per tick, so setting an internal flag to unmark the augment for syncing when this is called is safe.
     * @return If this augment should be synced to the client
     */
    public default boolean shouldSync() {
        return false;
    }
    
}
