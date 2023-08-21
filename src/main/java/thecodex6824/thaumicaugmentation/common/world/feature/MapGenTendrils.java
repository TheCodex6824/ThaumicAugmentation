package thecodex6824.thaumicaugmentation.common.world.feature;

import java.util.Random;

import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.world.TABiomes;

public class MapGenTendrils extends MapGenBase {

	@Override
	protected void recursiveGenerate(World world, int chunkX, int chunkZ, int originalX, int originalZ,
			ChunkPrimer primer) {
		
		boolean biomeOk = false;
		Biome[] biomes = world.getBiomeProvider().getBiomes(null, originalX * 16 - 16, originalZ * 16 - 16, 48, 48);
		for (Biome biome : biomes) {
			if (biome == TABiomes.SERPENTINE_ABYSS) {
				biomeOk = true;
				break;
			}
		}
		
		if (biomeOk) {
			rand.nextDouble();
	        double x = chunkX * 16 + rand.nextInt(16);
	        double y = rand.nextInt(rand.nextInt(world.getSeaLevel() - 7) + 1) + 8;
	        double z = chunkZ * 16 + rand.nextInt(16);
	        double caveAngle = rand.nextDouble() * Math.PI * 2.0;
	        double tunnelAngle = (rand.nextDouble() - 0.5) * 2.0 / 8.0;
	        double scaleHoriz = rand.nextDouble() + 0.5;
	        addTendril(rand.nextLong(), originalX, originalZ, primer, x, y, z, scaleHoriz, 1.0, caveAngle, tunnelAngle, 0, 0);
		}
	}
	
	protected void addTendril(long seed, int originalX, int originalZ, ChunkPrimer primer,
			double startX, double startY, double startZ, double scaleHoriz, double scaleVert,
			double caveAngle, double tunnelAngle, int minY, int maxY) {
		
		Random random = new Random(seed);
		double x = originalX * 16 + 8;
        double z = originalZ * 16 + 8;
        double caveAngleChange = (random.nextFloat() - 0.5F) * scaleHoriz;
        double tunnelAngleChange = (random.nextFloat() - 0.5F) * scaleHoriz;

        if (maxY <= 0) {
            maxY = 96;
        }
        if (minY <= 0) {
        	minY = maxY / 4;
        }

        if (startY < minY || (startY < minY + 5 && Math.sin(tunnelAngle) < 0) ||
        		startY >= (int) (maxY / 1.5F) || ((startY >= (int) (maxY / 1.5F) + 5 && Math.sin(tunnelAngle) > 0))) {
        	return;
        }
        
        boolean foundOtherBiome = false;
        MutableBlockPos biomeGetter = new MutableBlockPos();
        int length = random.nextInt(5) + 3;
        for (int i = 0; i < length; ++i) {
	        for (int y = minY; y < maxY; ++y) {
	            double regionSizeHoriz = 1.0 + scaleHoriz;
	            if (regionSizeHoriz < 1.0) {
	            	return;
	            }
	            
	            double regionSizeVert = regionSizeHoriz * scaleVert;
	            double tunnelAngleHoriz = Math.cos(tunnelAngle);
	            double tunnelAngleVert = Math.sin(tunnelAngle);
	            startX += Math.cos(caveAngle) * tunnelAngleHoriz;
	            startY += tunnelAngleVert;
	            if (startY >= maxY) {
	            	return;
	            }
	            
	            startZ += Math.sin(caveAngle) * tunnelAngleHoriz;
	            biomeGetter.setPos(startX, startY, startZ);
	            if (foundOtherBiome || (startY > world.getSeaLevel() && world.getBiomeProvider().getBiome(biomeGetter) != TABiomes.SERPENTINE_ABYSS)) {
	            	scaleHoriz -= 0.1;
	            	foundOtherBiome = true;
	            }
            	tunnelAngle *= 0.7F;
	            tunnelAngle += tunnelAngleChange * 0.1F;
	            caveAngle += caveAngleChange * 0.1F;
	            tunnelAngleChange *= 0.9F;
	            caveAngleChange *= 0.75F;
	            tunnelAngleChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
	            caveAngleChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat();
	            
	            if (startX >= x - 16.0 - regionSizeHoriz * 2.0 && startZ >= z - 16.0 - regionSizeHoriz * 2.0 && startX <= x + 16.0 + regionSizeHoriz * 2.0 && startZ <= z + 16.0 + regionSizeHoriz * 2.0) {
	            	int x1 = Math.max(MathHelper.floor(startX - regionSizeHoriz) - originalX * 16 - 1, 0);
	                int x2 = Math.min(MathHelper.floor(startX + regionSizeHoriz) - originalX * 16 + 1, 16);
	                int y1 = Math.max(MathHelper.floor(startY - regionSizeVert) - 1, 12);
	                int y2 = Math.min(MathHelper.floor(startY + regionSizeVert) + 1, maxY);
	                int z1 = Math.max(MathHelper.floor(startZ - regionSizeHoriz) - originalZ * 16 - 1, 0);
	                int z2 = Math.min(MathHelper.floor(startZ + regionSizeHoriz) - originalZ * 16 + 1, 16);
	                for (int xCheck = x1; xCheck < x2; ++xCheck) {
	                    double rX = ((xCheck + originalX * 16) + 0.5 - startX) / regionSizeHoriz;
	                    for (int zCheck = z1; zCheck < z2; ++zCheck) {
	                        double rZ = ((zCheck + originalZ * 16) + 0.5 - startZ) / regionSizeHoriz;
	                        if (rX * rX + rZ * rZ < 1.0) {
	                            for (int yCheck = y2; yCheck >= y1; --yCheck) {
	                                double rY = ((yCheck - 1) + 0.5 - startY) / regionSizeVert;
	                                if (rY > -0.7 && rX * rX + rY * rY + rZ * rZ < 1.0) {
	                                    primer.setBlockState(xCheck, yCheck, zCheck, TABlocks.STONE.getDefaultState());
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        
	        scaleHoriz *= 0.9;
        }
	}

}
