package thecodex6824.thaumicaugmentation.common.world.biome;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeDecoratorTaintedSwamp extends BiomeDecoratorEmptinessBase {

	@Override
	public void decorate(World world, Random random, Biome biome, BlockPos pos) {
		super.decorate(world, random, biome, pos);
		int numTrees = random.nextInt(3);
		for (int i = 0; i < numTrees; ++i) {
			int centerX = random.nextInt(16) + 8;
			int centerZ = random.nextInt(16) + 8;
			BlockPos start = world.getHeight(pos.add(centerX, 0, centerZ)).add(0, random.nextInt(4) + 2, 0);
			if (start.getY() >= world.provider.getAverageGroundLevel()) {
				WorldGenAbstractTree tree = biome.getRandomTreeFeature(random);
				tree.setDecorationDefaults();
				tree.generate(world, random, start);
			}
		}
	}
	
}
