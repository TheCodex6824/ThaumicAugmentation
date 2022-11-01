/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
 *
 *  This file is part of Thaumic Augmentation.
 *
 *  Thaumic Augmentation is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Thaumic Augmentation is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Thaumic Augmentation.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumicaugmentation.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.codechicken.lib.raytracer.ExtendedMOP;
import thaumcraft.codechicken.lib.raytracer.IndexedCuboid6;
import thaumcraft.codechicken.lib.raytracer.RayTracer;
import thaumcraft.codechicken.lib.vec.BlockCoord;
import thaumcraft.codechicken.lib.vec.Cuboid6;
import thaumcraft.codechicken.lib.vec.Vector3;
import thaumcraft.common.lib.utils.InventoryUtils;
import thecodex6824.thaumicaugmentation.api.tile.IEssentiaTube;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.tile.TileGlassTube;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockGlassTube extends BlockTABase implements IItemBlockProvider {

    public enum ConnectionType implements IStringSerializable {
        NONE("none"),
        GLASS("glass"),
        CONNECTOR("connector");
        
        private final String name;
        
        ConnectionType(String n) {
            name = n;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
    }
    
    public static final PropertyEnum<ConnectionType> NORTH = PropertyEnum.create("north", ConnectionType.class);
    public static final PropertyEnum<ConnectionType> EAST = PropertyEnum.create("east", ConnectionType.class);
    public static final PropertyEnum<ConnectionType> SOUTH = PropertyEnum.create("south", ConnectionType.class);
    public static final PropertyEnum<ConnectionType> WEST = PropertyEnum.create("west", ConnectionType.class);
    public static final PropertyEnum<ConnectionType> UP = PropertyEnum.create("up", ConnectionType.class);
    public static final PropertyEnum<ConnectionType> DOWN = PropertyEnum.create("down", ConnectionType.class);
    
    
    
    protected static RayTracer raytracer = new RayTracer();
    
    public BlockGlassTube() {
        super(Material.GLASS);
        setHardness(0.5F);
        setResistance(5.0F);
        setSoundType(SoundType.GLASS);
        setLightOpacity(0);
        setDefaultState(getDefaultState().withProperty(NORTH, ConnectionType.NONE).withProperty(EAST, ConnectionType.NONE).withProperty(
                SOUTH, ConnectionType.NONE).withProperty(WEST, ConnectionType.NONE).withProperty(UP, ConnectionType.NONE).withProperty(DOWN, ConnectionType.NONE));
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }
    
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
    
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isTranslucent(IBlockState state) {
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IEssentiaTube) {
            IEssentiaTube tube = (IEssentiaTube) tile;
            for (EnumFacing f : EnumFacing.VALUES) {
                if (tube.isConnectable(f) && (!(world instanceof World) || ((World) world).isBlockLoaded(pos.offset(f)))) {
                    TileEntity other = ThaumcraftApiHelper.getConnectableTile(world, pos, f);
                    if (other instanceof TileGlassTube)
                        state = state.withProperty((IProperty<ConnectionType>) blockState.getProperty(f.getName()), ConnectionType.GLASS);
                    else if (other != null)
                        state = state.withProperty((IProperty<ConnectionType>) blockState.getProperty(f.getName()), ConnectionType.CONNECTOR);
                }
            }
        }
        
        return state;
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        float minX = 0.3125F, maxX = 0.6875F;
        float minY = 0.3125F, maxY = 0.6875F;
        float minZ = 0.3125F, maxZ = 0.6875F;
        for (EnumFacing side : EnumFacing.VALUES) {
            if (!(source instanceof World) || ((World) source).isBlockLoaded(pos.offset(side))) {
                TileEntity te = ThaumcraftApiHelper.getConnectableTile(source, pos, side);
                if (te != null) {
                    switch (side) {
                        case DOWN:
                            minY = 0.0F;
                            break;
                        case UP:
                            maxY = 1.0F;
                            break;
                        case NORTH:
                            minZ = 0.0F;
                            break;
                        case SOUTH:
                            maxZ = 1.0F;
                            break;
                        case WEST:
                            minX = 0.0F;
                            break;
                        case EAST:
                            maxX = 1.0F;
                            break;
                        default: break;
                    }  
                }
            }
        } 
        
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (InventoryUtils.isHoldingItem(player, ICaster.class) != null ||
                InventoryUtils.isHoldingItem(player, ItemsTC.resonator) != null) {
            
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IEssentiaTube) {
                Vec3d start = player.getPositionEyes(Minecraft.getMinecraft().getRenderPartialTicks());
                Vec3d look = player.getLookVec();
                double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
                RayTraceResult result = world.getBlockState(pos).collisionRayTrace(world, pos, start,
                        start.add(look.x * reach, look.y * reach, look.z * reach));
                if (result != null && result.subHit >= 0 && result.subHit <= 6) {
                    ArrayList<IndexedCuboid6> cubes = new ArrayList<>();
                    addCubes(world, pos, cubes);
                    for (IndexedCuboid6 c : cubes) {
                        if (((Integer) c.data) == result.subHit) {
                            return new AxisAlignedBB((float) c.min.x, (float) c.min.y, (float) c.min.z,
                                    (float) c.max.x, (float) c.max.y, (float) c.max.z);
                        } 
                    }  
                }
            }
        }
        
        return super.getSelectedBoundingBox(state, world, pos);
    }
    
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IEssentiaTube) {
            IEssentiaTube tube = (IEssentiaTube) tile;
            if (tube.getEssentiaAmount(EnumFacing.UP) > 0) {
                if (!world.isRemote) {
                    AuraHelper.polluteAura(world, pos, tube.getEssentiaAmount(EnumFacing.UP), true);
                    world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS,
                            0.1F, 1.0F + world.rand.nextFloat() * 0.1F);
                }
                else {
                    for (int i = 0; i < 5; ++i) {
                        FXDispatcher.INSTANCE.drawVentParticles(pos.getX() + 0.33 + world.rand.nextFloat() * 0.33, pos.getY() + 0.33 + world.rand.nextFloat() * 0.33,
                                pos.getZ() + 0.33 + world.rand.nextFloat() * 0.33, 0.0, 0.0, 0.0, Aspect.FLUX.getColor()); 
                    }
                }
            }
        }
        
        super.breakBlock(world, pos, state);
    }
    
    protected void addCubes(World world, BlockPos pos, List<IndexedCuboid6> cubes) {
        Cuboid6 base = new Cuboid6(pos.getX() + 0.375F, pos.getY() + 0.375F,
                pos.getZ() + 0.375F, pos.getX() + 0.625F, pos.getY() + 0.625F, pos.getZ() + 0.625F);
        for (EnumFacing f : EnumFacing.VALUES) {
            if (world.getTileEntity(pos.offset(f)) instanceof IEssentiaTransport) {
                IndexedCuboid6 cube = new IndexedCuboid6(f.getIndex(), base);
                boolean positive = f.getAxisDirection() == AxisDirection.POSITIVE;
                switch (f.getAxis()) {
                    case X:
                        if (positive) {
                            cube.min.x += 0.25F;
                            cube.max.x += 0.375F;
                        }
                        else {
                            cube.min.x -= 0.375F;
                            cube.max.x -= 0.25F;
                        }
                        
                        break;
                    case Y:
                        if (positive) {
                            cube.min.y += 0.25F;
                            cube.max.y += 0.375F;
                        }
                        else {
                            cube.min.y -= 0.375F;
                            cube.max.y -= 0.25F;
                        }
                        
                        break;
                    case Z:
                        if (positive) {
                            cube.min.z += 0.25F;
                            cube.max.z += 0.375F;
                        }
                        else {
                            cube.min.z -= 0.375F;
                            cube.max.z -= 0.25F;
                        }
                        
                        break;
                    default: break;
                }
                
                cubes.add(cube);
            }
        }
        
        cubes.add(new IndexedCuboid6(6, base));
    }
    
    @Override
    @Nullable
    @SuppressWarnings("deprecation")
    public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d start,
            Vec3d end) {
        
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IEssentiaTube) {
            ArrayList<IndexedCuboid6> cubes = new ArrayList<>();
            addCubes(world, pos, cubes);
            ArrayList<ExtendedMOP> out = new ArrayList<>();
            raytracer.rayTraceCuboids(new Vector3(start), new Vector3(end), cubes, new BlockCoord(pos), this, out);
            if (!out.isEmpty())
                return out.get(0);
        }
        
        return super.collisionRayTrace(blockState, world, pos, start, end);
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    
    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileGlassTube();
    }
    
    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
        if (!world.isRemote)
            return true;
        else {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null)
                return tile.receiveClientEvent(id, param);
            else
                return false;
        }
    }
    
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
    
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
}
