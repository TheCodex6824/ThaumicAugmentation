package thecodex6824.thaumicaugmentation.common.world.feature;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import thecodex6824.thaumicaugmentation.api.TAFluids;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.common.world.noise.NoiseGeneratorOpenSimplex2;
import thecodex6824.thaumicaugmentation.common.world.noise.NoiseGeneratorWorley;

public class MapGenEmptinessCaves extends MapGenBase {

	protected static final IBlockState BLK_AIR = Blocks.AIR.getDefaultState();
	
	public MapGenEmptinessCaves() {
		range = 0;
	}
	
	protected boolean generateCaveRoom(int x, int y, int z) {
		float roomNoise = NoiseGeneratorOpenSimplex2.generate(world.getSeed(), 4, x, y, z,
				0.0075F, 0.015F, 0.0075F);
		float heightFactor = y / 255.0F * (1.0F - 0.25F) + 0.25F;
		return roomNoise > heightFactor;
	}
	
	protected boolean generateTunnel(int x, int y, int z) {
		float tunnelNoiseSimplex1 = NoiseGeneratorOpenSimplex2.generate(world.getSeed() + 1000, 4, x, y, z,
				0.001F, 0.001F, 0.001F);
		float tunnelNoiseSimplex2 = NoiseGeneratorOpenSimplex2.generate(~world.getSeed(), 4, x, y, z,
				0.01F, 0.01F, 0.01F);
		float tunnelNoiseWorley = NoiseGeneratorWorley.generate(world.getSeed(), x + 0.5, y + 0.5, z + 0.5);
		return ((tunnelNoiseSimplex1 > -0.0075F && tunnelNoiseSimplex1 < 0.0075F) ||
				(tunnelNoiseSimplex2 > -0.075F && tunnelNoiseSimplex2 < 0.075F)) && tunnelNoiseWorley < 0.075F;
	}
	
	@Override
	public void generate(World world, int chunkX, int chunkZ, ChunkPrimer primer) {
		this.world = world;
		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				for (int y = 255; y >= 0; --y) {
					if (primer.getBlockState(x, y, z).getMaterial() != Material.AIR) {
						int realX = x + chunkX * 16;
						int realZ = z + chunkZ * 16;
						if (generateCaveRoom(realX, y, realZ) || generateTunnel(realX, y, realZ)) {
							IBlockState state = primer.getBlockState(x, y, z);
	                        IBlockState above = primer.getBlockState(x, y + 1, z);
	                        digBlock(primer, x, y, z, chunkX, chunkZ,
	                        		isTopBlock(primer, x, y, z, chunkX, chunkZ), state, above);
						}
					}
				}
			}
		}
	}
	
	protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop,
			IBlockState state, IBlockState up) {
		
		Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
        IBlockState top = biome.topBlock;
        IBlockState filler = biome.fillerBlock;
        if (isOceanBlock(up)) {
        	data.setBlockState(x, y, z, up);
        }
        else if (canReplaceBlock(state, up) || state.getBlock() == top.getBlock() || state.getBlock() == filler.getBlock()) {
            data.setBlockState(x, y, z, BLK_AIR);
            if (foundTop && y > 0 && data.getBlockState(x, y - 1, z).getBlock() == filler.getBlock()) {
                data.setBlockState(x, y - 1, z, top.getBlock().getDefaultState());
            }
        }
	}
	
	protected boolean isOceanBlock(IBlockState state) {
		return state.getBlock() == TAFluids.TAINTED_SLURRY.getBlock();
	}
	
	protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
		return isOceanBlock(data.getBlockState(x, y, z));
	}
	
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
	
	protected boolean isTopBlock(ChunkPrimer primer, int x, int y, int z, int chunkX, int chunkZ) {
		Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
        return primer.getBlockState(x, y, z) == biome.topBlock;
	}
	
}
