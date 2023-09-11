package thecodex6824.thaumicaugmentation.api.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import thecodex6824.thaumicaugmentation.api.internal.TAInternals;

public class EquipmentInventoryRegistry {

	private static final HashMap<String, Function<Entity, IItemHandler>> additionalItemSources = new HashMap<>();
	
	/**
     * Registers a callback that returns a source of ItemStacks to check for items on an entity.
     * @param key A unique identifier for this source
     * @param source The callback that returns the ItemStack instances to check for items
     */
    public static void registerEquipmentSource(ResourceLocation key, Function<Entity, IItemHandler> source) {
        additionalItemSources.put(key.toString(), source);
    }
    
    /**
     * Removes a previously registered item source.
     * @param key The unique identifier of the callback to remove
     * @return If a callback matching the key existed and was removed
     */
    public static boolean unregisterEquipmentSource(ResourceLocation key) {
        return additionalItemSources.remove(key.toString()) != null;
    }
    
    /**
     * Returns a collection of all item sources.
     * @return All item sources
     */
    public static Collection<Function<Entity, IItemHandler>> getAllEquipmentSources() {
        return additionalItemSources.values();
    }
    
    public static IItemHandler getCombinedEquipmentView(Entity input) {
    	return TAInternals.createMultiHandlerView(additionalItemSources.values().stream()
    			.map(s -> s.apply(input))
    			.iterator());
    }
	
}
