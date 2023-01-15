package thecodex6824.thaumicaugmentation.common.world.feature;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.Constants.BlockFlags;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.blocks.world.ore.BlockCrystal;
import thaumcraft.common.blocks.world.ore.ShardType;
import thaumcraft.common.world.biomes.BiomeHandler;
import thecodex6824.thaumicaugmentation.common.world.biome.BiomeEmptinessBase;

public class WorldGenCrystals extends WorldGenerator {

	public WorldGenCrystals() {
		super();
	}
	
	@Override
	protected void setBlockAndNotifyAdequately(World world, BlockPos pos, IBlockState state) {
		world.setBlockState(pos, state, BlockFlags.SEND_TO_CLIENTS | BlockFlags.NO_OBSERVERS);
	}
	
	@Nullable
	protected ShardType getCrystalTypeForBiome(Biome biome, Random rand) {
		if (biome instanceof BiomeEmptinessBase) {
			List<ShardType> types = ((BiomeEmptinessBase) biome).getCrystalTypesForWorldGen();
			if (types.isEmpty()) {
				return null;
			}
			else if (types.size() == 1) {
				return types.get(0);
			}
			else {
				return types.get(rand.nextInt(types.size()));
			}
		}
		else {
			return ShardType.byMetadata(ShardType.getMetaByAspect(BiomeHandler.getRandomBiomeTag(Biome.getIdForBiome(biome), rand)));
		}
	}
	
	@Override
	public boolean generate(World world, Random rand, BlockPos position) {
		boolean didSomething = false;
		for (int i = 0; i < rand.nextInt(4) + 1; ++i) {
			int size = rand.nextInt(3) + 2;
			BlockPos pos = position.add(rand.nextInt(16) + 8, rand.nextInt(40) + 1, rand.nextInt(16) + 8);
			ShardType type = getCrystalTypeForBiome(world.getBiome(pos), rand);
			if (type != null) {
				Block block = null;
				switch (type) {
					case AIR: {
						block = BlocksTC.crystalAir;
						break;
					}
					case EARTH: {
						block = BlocksTC.crystalEarth;
						break;
					}
					case ENTROPY: {
						block = BlocksTC.crystalEntropy;
						break;
					}
					case FIRE: {
						block = BlocksTC.crystalFire;
						break;
					}
					case ORDER: {
						block = BlocksTC.crystalOrder;
						break;
					}
					case WATER: {
						block = BlocksTC.crystalWater;
						break;
					}
					case FLUX:
					default: {
						block = BlocksTC.crystalTaint;
						break;
					}
				}
				
				MutableBlockPos mutable = new MutableBlockPos(pos);
				for (int z = -size; z <= size; ++z) {
					for (int x = -size; x <= size; ++x) {
						for (int y = -size; y <= size; ++y) {
							if (pos.getY() + y > 0 && pos.getY() + y < 256) {
								int taxiDist = Math.abs(z) + Math.abs(x) + Math.abs(y);
								if (taxiDist <= size || taxiDist <= size + 1 && rand.nextBoolean()) {
									mutable.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
									if (block.canPlaceBlockAt(world, mutable)) {
										IBlockState crystal = block.getDefaultState().withProperty(BlockCrystal.SIZE, rand.nextInt(2) + 1);
										setBlockAndNotifyAdequately(world, mutable, crystal);
										didSomething = true;
									}
								}
							}
						}
					}
				}
			}
		}
		
		return didSomething;
	}
	
}
