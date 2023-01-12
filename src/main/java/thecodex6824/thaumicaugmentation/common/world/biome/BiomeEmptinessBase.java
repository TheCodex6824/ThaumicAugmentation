package thecodex6824.thaumicaugmentation.common.world.biome;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;

public abstract class BiomeEmptinessBase extends Biome {

	protected IBlockState fluidBlock;
	protected float baseFluxConcentration;
	protected int baseGrassColor;
	protected NoiseGeneratorSimplex plantColorNoise;
	
	public BiomeEmptinessBase(BiomeProperties props, @Nullable IBlockState fluidState, float baseFlux, int grassColor) {
		super(props);
		spawnableCreatureList.clear();
        spawnableMonsterList.clear();
        spawnableWaterCreatureList.clear();
        spawnableCaveCreatureList.clear();
        flowers.clear();
        topBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_VOID);
        fillerBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_VOID);
        fluidBlock = fluidState != null ? fluidState : Blocks.AIR.getDefaultState();
        baseFluxConcentration = baseFlux;
        baseGrassColor = grassColor;
        plantColorNoise = new NoiseGeneratorSimplex(new Random(getBiomeName().hashCode()));
	}
	
	public IBlockState getFluidState() {
		return fluidBlock;
	}
	
	public float getBaseFluxConcentration() {
		return baseFluxConcentration;
	}
	
	@SideOnly(Side.CLIENT)
	public Vec3d getFogColor(Entity view, float angle, float partialTicks) {
		return Vec3d.ZERO;
	}
	
	@Override
    public void genTerrainBlocks(World world, Random rand, ChunkPrimer primer, int x, int z, double noiseVal) {
        for (int y = Math.min(world.getActualHeight(), 255); y >= 0; --y) {
            IBlockState current = BiomeUtil.getBlockStateInPrimer(primer, x, y, z);
            if (!current.getBlock().isAir(current, world, new BlockPos(x, y, z)) && current.isNormalCube()) {
            	BiomeUtil.setBlockStateInPrimer(primer, x, y, z, topBlock);
            	break;
            }
        }
    }
	
	@Override
    public int getFoliageColorAtPos(BlockPos pos) {
        double noise = plantColorNoise.getValue(pos.getX(), pos.getZ());
        double colorMod = 1.0 / (1.0 + Math.exp(-noise));
        return getModdedBiomeFoliageColor(
        		((int) (((baseGrassColor >> 16) & 0xff0000) * colorMod) << 16) +
        		((int) (((baseGrassColor >> 8) & 0x00ff00) * colorMod) << 8) +
        		(int) ((baseGrassColor & 0x0000ff) * colorMod)
        );
    }
    
    @Override
    public int getGrassColorAtPos(BlockPos pos) {
    	double noise = plantColorNoise.getValue(pos.getX(), pos.getZ());
        double colorMod = 1.0 / (1.0 + Math.exp(-noise));
        return getModdedBiomeGrassColor(
        		((int) (((baseGrassColor >> 16) & 0xff0000) * colorMod) << 16) +
        		((int) (((baseGrassColor >> 8) & 0x00ff00) * colorMod) << 8) +
        		(int) ((baseGrassColor & 0x0000ff) * colorMod)
        );
    }
    
    @Override
    public int getSkyColorByTemp(float currentTemperature) {
        return 0;
    }
	
}
