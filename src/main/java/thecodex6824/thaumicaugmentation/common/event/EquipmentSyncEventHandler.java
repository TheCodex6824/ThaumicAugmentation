package thecodex6824.thaumicaugmentation.common.event;

import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.event.AugmentEventHelper;
import thecodex6824.thaumicaugmentation.api.item.EquipmentInventoryRegistry;
import thecodex6824.thaumicaugmentation.common.capability.CapabilityEquipmentTracker;
import thecodex6824.thaumicaugmentation.common.capability.IEquipmentTracker;
import thecodex6824.thaumicaugmentation.common.capability.CustomSyncBaubleItem;
import thecodex6824.thaumicaugmentation.common.network.PacketCapabilityItemSync;
import thecodex6824.thaumicaugmentation.common.network.PacketCapabilityItemSync.CapabilityType;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class EquipmentSyncEventHandler {
    
    private static CustomSyncBaubleItem getBauble(ItemStack stack) {
    	IBauble bauble = stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null);
    	return bauble instanceof CustomSyncBaubleItem ? (CustomSyncBaubleItem) bauble : null;
    }
    
    public static void onEquipmentChange(EntityLivingBase entity) {
    	IEquipmentTracker tracker = entity.getCapability(CapabilityEquipmentTracker.EQUIPMENT_TRACKER, null);
    	if (tracker != null) {
	    	IItemHandler stacks = EquipmentInventoryRegistry.getCombinedEquipmentView(entity);
            for (int i = 0; i < stacks.getSlots(); ++i) {
            	ItemStack current = stacks.getStackInSlot(i);
            	ItemStack old = tracker.getRecordedEquipmentStack(i);
                if (!ItemStack.areItemStacksEqual(current, old)) {
                    IAugmentableItem oldCap = old.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                    if (oldCap != null) {
                        AugmentEventHelper.fireUnequipEvent(oldCap, entity);
                    }
                
                    IAugmentableItem currentCap = current.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                    if (currentCap != null) {
                        AugmentEventHelper.fireEquipEvent(currentCap, entity);
                        if (!entity.getEntityWorld().isRemote) {
                        	if (entity instanceof EntityPlayerMP) {
	                    		TANetwork.INSTANCE.sendTo(new PacketCapabilityItemSync(CapabilityType.AUGMENT,
		                    			entity.getEntityId(), i, currentCap.getSyncNBT(true)), (EntityPlayerMP) entity);
	                    	}
                        	
                        	TANetwork.INSTANCE.sendToAllTracking(new PacketCapabilityItemSync(CapabilityType.AUGMENT,
                            		entity.getEntityId(), i, currentCap.getSyncNBT(false)), entity);
                        }
                    }
                    
                    if (!entity.getEntityWorld().isRemote) {
                    	CustomSyncBaubleItem bauble = getBauble(current);
	                    if (bauble != null) {
	                    	if (entity instanceof EntityPlayerMP) {
	                    		TANetwork.INSTANCE.sendTo(new PacketCapabilityItemSync(CapabilityType.BAUBLE,
		                    			entity.getEntityId(), i, bauble.getSyncNBT(true)), (EntityPlayerMP) entity);
	                    	}
	                    	
	                    	TANetwork.INSTANCE.sendToAllTracking(new PacketCapabilityItemSync(CapabilityType.BAUBLE,
	                    			entity.getEntityId(), i, bauble.getSyncNBT(false)), entity);
	                    }
                    }
                
                    tracker.recordEquipmentStack(i, current);
                }
            }
    	}
    }
	
}
