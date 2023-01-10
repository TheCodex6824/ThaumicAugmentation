package thecodex6824.thaumicaugmentation.common.world.biome;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.world.biome.BiomeDecorator;
import thaumcraft.common.entities.monster.tainted.EntityTaintCrawler;
import thaumcraft.common.entities.monster.tainted.EntityTaintacle;
import thaumcraft.common.entities.monster.tainted.EntityTaintacleSmall;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAFluids;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;

public class BiomeTaintedSwamp extends BiomeEmptinessBase {
    
    public BiomeTaintedSwamp() {
        super(new BiomeProperties("Tainted Swamp").setBaseHeight(-0.2F).setHeightVariation(0.2F).setTemperature(0.35F).setWaterColor(0xFF00FF),
        		TAFluids.TAINTED_SLURRY.getBlock().getDefaultState(), 0.5F, 0x660066);
        topBlock = TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.SOIL_STONE_TAINT_NODECAY);
        spawnableMonsterList.add(new SpawnListEntry(EntityTaintCrawler.class, 100, 3, 5));
        spawnableMonsterList.add(new SpawnListEntry(EntityTaintacleSmall.class, 75, 1, 2));
        spawnableMonsterList.add(new SpawnListEntry(EntityTaintacle.class, 50, 1, 1));
        spawnableMonsterList.add(new SpawnListEntry(EntityEnderman.class, 1, 2, 2));
    }

    @Override
    public BiomeDecorator createBiomeDecorator() {
        return new BiomeDecoratorTaintedLands();
    }

}
