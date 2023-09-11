package thecodex6824.thaumicaugmentation.api.block.property;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;

public interface IAugmentationStationPart {

	enum AugmentationStationPart implements IStringSerializable {
        
		// directions are from perspective of the station itself
        LOWER_RIGHT(0),
        LOWER_LEFT(1),
        UPPER_RIGHT(2),
        UPPER_LEFT(3);
        
        private final int meta;
        
        AugmentationStationPart(int m) {
            meta = m;
        }
        
        public int getMeta() {
            return meta;
        }
        
        @Override
        public String getName() {
            return name().toLowerCase();
        }
        
        public BlockPos getTilePos(BlockPos myPos, EnumFacing facing) {
        	switch (this) {
        		case LOWER_LEFT: return myPos.offset(facing.rotateY());
        		case UPPER_RIGHT: return myPos.down();
        		case UPPER_LEFT: return myPos.offset(facing.rotateY()).down();
        		case LOWER_RIGHT: 
        		default: return myPos.toImmutable();
        	}
        }
        
        public BlockPos getOffsetFromTile(BlockPos tilePos, EnumFacing facing) {
        	switch (this) {
	        	case LOWER_LEFT: return tilePos.offset(facing.rotateYCCW());
	    		case UPPER_RIGHT: return tilePos.up();
	    		case UPPER_LEFT: return tilePos.offset(facing.rotateYCCW()).up();
	    		case LOWER_RIGHT: 
	    		default: return tilePos.toImmutable();
        	}
        }
        
        public boolean hasTile() {
        	return this == LOWER_RIGHT;
        }
        
        public static Map<AugmentationStationPart, BlockPos> getAllPositions(BlockPos tilePos, EnumFacing facing) {
        	ImmutableMap.Builder<AugmentationStationPart, BlockPos> builder = ImmutableMap.builder();
        	builder.put(AugmentationStationPart.LOWER_RIGHT, tilePos.toImmutable());
        	builder.put(AugmentationStationPart.LOWER_LEFT, tilePos.offset(facing.rotateYCCW()));
        	builder.put(AugmentationStationPart.UPPER_RIGHT, tilePos.up());
        	builder.put(AugmentationStationPart.UPPER_LEFT, tilePos.offset(facing.rotateYCCW()).up());
        	return builder.build();
        }
        
        @Nullable
        public static AugmentationStationPart fromMeta(int id) {
            for (AugmentationStationPart type : values()) {
                if (type.getMeta() == id) {
                    return type;
                }
            }
            
            return null;
        }
        
    }
    
    PropertyEnum<AugmentationStationPart> AUGMENTATION_STATION_PART =
    		PropertyEnum.create("ta_augmentation_station_part", AugmentationStationPart.class);
	
}
