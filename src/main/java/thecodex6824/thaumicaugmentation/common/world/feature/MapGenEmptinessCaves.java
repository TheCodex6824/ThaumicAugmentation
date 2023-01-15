package thecodex6824.thaumicaugmentation.common.world.feature;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenCaves;
import thecodex6824.thaumicaugmentation.api.TAFluids;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;

public class MapGenEmptinessCaves extends MapGenCaves {

	@Override
	protected void recursiveGenerate(World world, int chunkX, int chunkZ, int originalX, int originalZ,
			ChunkPrimer primer) {
		
		int attempts = 0;
		if (rand.nextInt(7) == 0) {
			attempts = rand.nextInt(rand.nextInt(rand.nextInt(15) + 1) + 1);
		}

        for (int i = 0; i < attempts; ++i) {
            double x = chunkX * 16 + rand.nextInt(16);
            double y = rand.nextInt(rand.nextInt(120) + 8);
            double z = chunkZ * 16 + rand.nextInt(16);
            int tunnels = 1;
            if (rand.nextInt(4) == 0) {
                addRoom(rand.nextLong(), originalX, originalZ, primer, x, y, z);
                tunnels += rand.nextInt(4);
            }

            for (int j = 0; j < tunnels; ++j) {
                double caveAngle = rand.nextDouble() * Math.PI * 2.0;
                double tunnelAngle = (rand.nextDouble() - 0.5) * 2.0 / 8.0;
                double scaleHoriz = rand.nextDouble() * 2.0 + rand.nextDouble() + 1.0;
                if (rand.nextInt(10) == 0) {
                    scaleHoriz *= rand.nextDouble() * rand.nextDouble() * 3.0 + 1.0;
                }

                addTunnel(rand.nextLong(), originalX, originalZ, primer, x, y, z, scaleHoriz, 1.0, caveAngle, tunnelAngle, 0, 0);
            }
        }
	}
	
	@Override
	protected void addRoom(long seed, int originalX, int originalZ, ChunkPrimer primer,
			double startX, double startY, double startZ) {
		
		addTunnel(seed, originalX, originalZ, primer, startX, startY, startZ, 1.0 + rand.nextDouble() * 4.0, rand.nextDouble() + 1.0, 0.0, 0.0, -1, -1);
	}
	
	@Override
	protected void addTunnel(long seed, int originalX, int originalZ, ChunkPrimer primer,
			double startX, double startY, double startZ, float scaleHoriz, float caveAngle,
			float tunnelAngle, int minY, int maxY, double scaleVert) {
	
		addTunnel(seed, originalX, originalZ, primer, startX, startY, startZ, scaleHoriz, scaleVert, caveAngle, tunnelAngle, minY, maxY);
	}
	
	protected void addTunnel(long seed, int originalX, int originalZ, ChunkPrimer primer,
			double startX, double startY, double startZ, double scaleHoriz, double scaleVert,
			double caveAngle, double tunnelAngle, int minY, int maxY) {
		
		double x = originalX * 16 + 8;
        double z = originalZ * 16 + 8;
        float caveAngleChange = 0.0F;
        float tunnelAngleChange = 0.0F;
        Random random = new Random(seed);

        if (maxY <= 0) {
            int rangeBlocks = range * 16 - 16;
            maxY = rangeBlocks - random.nextInt(rangeBlocks / 4);
        }

        boolean forceEnd = false;
        if (minY == -1) {
            minY = maxY / 4;
            forceEnd = true;
        }

        int tunnelHeight = random.nextInt(maxY / 2) + maxY / 4;
        boolean crazyTunnel = random.nextInt(6) == 0;
        for (/* nothing */; minY < maxY; ++minY) {
            double regionSizeHoriz = 1.5 + Math.sin(minY * Math.PI / maxY) * scaleHoriz;
            double regionSizeVert = regionSizeHoriz * scaleVert;
            double tunnelAngleHoriz = Math.cos(tunnelAngle);
            double tunnelAngleVert = Math.sin(tunnelAngle);
            startX += Math.cos(caveAngle) * tunnelAngleHoriz;
            startY += tunnelAngleVert;
            startZ += Math.sin(caveAngle) * tunnelAngleHoriz;

            // vanilla makes flatter tunnels here, but this is more fun
            if (crazyTunnel) {
                tunnelAngle += (random.nextDouble() - 0.5) * Math.PI / 2.0;
            }
            else {
                tunnelAngle *= 0.7F;
            }

            tunnelAngle += tunnelAngleChange * 0.1F;
            caveAngle += caveAngleChange * 0.1F;
            tunnelAngleChange *= 0.9F;
            caveAngleChange *= 0.75F;
            tunnelAngleChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            caveAngleChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (!forceEnd && minY == tunnelHeight && scaleHoriz > 1.0F && maxY > 0) {
                addTunnel(random.nextLong(), originalX, originalZ, primer, startX, startY, startZ, random.nextDouble() * 0.75 + 0.5, random.nextDouble() + 1.0, caveAngle - Math.PI / 2.0, tunnelAngle / 3.0, minY, maxY);
                addTunnel(random.nextLong(), originalX, originalZ, primer, startX, startY, startZ, random.nextDouble() * 0.75 + 0.5, random.nextDouble() + 1.0, caveAngle + Math.PI / 2.0, tunnelAngle / 3.0, minY, maxY);
                return;
            }

            if (forceEnd || random.nextInt(4) != 0) {
                double distX = startX - x;
                double distZ = startZ - z;
                double distY = maxY - minY;
                double maxDistHoriz = scaleHoriz + 2.0F + 16.0F;
                if (distX * distX + distZ * distZ - distY * distY > maxDistHoriz * maxDistHoriz) {
                    return;
                }

                if (startX >= x - 16.0 - regionSizeHoriz * 2.0 && startZ >= z - 16.0 - regionSizeHoriz * 2.0 && startX <= x + 16.0 + regionSizeHoriz * 2.0 && startZ <= z + 16.0 + regionSizeHoriz * 2.0) {
                    int x1 = Math.max(MathHelper.floor(startX - regionSizeHoriz) - originalX * 16 - 1, 0);
                    int x2 = Math.min(MathHelper.floor(startX + regionSizeHoriz) - originalX * 16 + 1, 16);
                    // vanilla limits this to y=1, but making this 0 allows scary void holes
                    int y1 = Math.max(MathHelper.floor(startY - regionSizeVert) - 1, 0);
                    int y2 = Math.min(MathHelper.floor(startY + regionSizeVert) + 1, 248);
                    int z1 = Math.max(MathHelper.floor(startZ - regionSizeHoriz) - originalZ * 16 - 1, 0);
                    int z2 = Math.min(MathHelper.floor(startZ + regionSizeHoriz) - originalZ * 16 + 1, 16);
                    boolean hitLiquid = false;
                    for (int xCheck = x1; !hitLiquid && xCheck < x2; ++xCheck) {
                        for (int zCheck = z1; !hitLiquid && zCheck < z2; ++zCheck) {
                            for (int yCheck = y2 + 1; !hitLiquid && yCheck >= y1 - 1; --yCheck) {
                            	if (yCheck < 0) {
                            		break;
                            	}
                            	
                                if (isOceanBlock(primer, xCheck, yCheck, zCheck, originalX, originalZ)) {
                                	hitLiquid = true;
                                }

                                // skip checking anywhere that is not on the edge of the region
                                if (yCheck != y1 - 1 && xCheck != x1 && xCheck != x2 - 1 && zCheck != z1 && zCheck != z2 - 1) {
                                    yCheck = y1;
                                }
                            }
                        }
                    }

                    if (!hitLiquid) {
                        for (int xCheck = x1; xCheck < x2; ++xCheck) {
                            double rX = ((xCheck + originalX * 16) + 0.5 - startX) / regionSizeHoriz;
                            for (int zCheck = z1; zCheck < z2; ++zCheck) {
                                double rZ = ((zCheck + originalZ * 16) + 0.5 - startZ) / regionSizeHoriz;
                                boolean topFound = false;
                                if (rX * rX + rZ * rZ < 1.0) {
                                    for (int yCheck = y2; yCheck >= y1; --yCheck) {
                                        double rY = ((yCheck - 1) + 0.5 - startY) / regionSizeVert;
                                        if (rY > -0.7 && rX * rX + rY * rY + rZ * rZ < 1.0) {
                                            IBlockState state = primer.getBlockState(xCheck, yCheck, zCheck);
                                            IBlockState above = primer.getBlockState(xCheck, yCheck + 1, zCheck);
                                            if (isTopBlock(primer, xCheck, yCheck, zCheck, originalX, originalZ)) {
                                                topFound = true;
                                            }

                                            digBlock(primer, xCheck, yCheck, zCheck, originalX, originalZ, topFound, state, above);
                                        }
                                    }
                                }
                            }
                        }

                        if (forceEnd) {
                            break;
                        }
                    }
                }
            }
        }
	}
	
	@Override
	protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop,
			IBlockState state, IBlockState up) {
		
		Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
        IBlockState top = biome.topBlock;
        IBlockState filler = biome.fillerBlock;
        if (canReplaceBlock(state, up) || state.getBlock() == top.getBlock() || state.getBlock() == filler.getBlock()) {
            data.setBlockState(x, y, z, BLK_AIR);
            if (foundTop && y > 0 && data.getBlockState(x, y - 1, z).getBlock() == filler.getBlock()) {
                data.setBlockState(x, y - 1, z, top.getBlock().getDefaultState());
            }
        }
	}
	
	@Override
	protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
		return data.getBlockState(x, y, z).getBlock() == TAFluids.TAINTED_SLURRY.getBlock();
	}
	
	@Override
	protected boolean canReplaceBlock(IBlockState state, IBlockState up) {
		if (state.getBlock() == Blocks.AIR || state.getBlock() == TAFluids.TAINTED_SLURRY.getBlock()) {
			return true;
		}
		else if (state.getPropertyKeys().contains(ITAStoneType.STONE_TYPE)) {
			StoneType type = state.getValue(ITAStoneType.STONE_TYPE);
			switch (type) {
				case SOIL_STONE_TAINT_NODECAY:
				case STONE_TAINT_NODECAY:
				case STONE_ANCIENT_BLUE:
				case STONE_VOID:
					return true;
				default: return false;
			}
		}
		
		return false;
	}
	
	private boolean isTopBlock(ChunkPrimer primer, int x, int y, int z, int chunkX, int chunkZ) {
		Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
        return primer.getBlockState(x, y, z) == biome.topBlock;
	}
	
}
