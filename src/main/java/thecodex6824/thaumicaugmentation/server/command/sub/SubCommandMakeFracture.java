/**
 *  Thaumic Augmentation
 *  Copyright (c) 2019 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.server.command.sub;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import thecodex6824.thaumicaugmentation.api.world.capability.CapabilityFractureLocations;
import thecodex6824.thaumicaugmentation.api.world.capability.IFractureLocations;
import thecodex6824.thaumicaugmentation.common.entity.EntityDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.util.FractureLocatorSearchManager;

public class SubCommandMakeFracture implements ISubCommand {

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        int x1 = 0, y1 = 0, z1 = 0, dim1 = 0;
        int x2 = 0, y2 = 0, z2 = 0, dim2 = 0;
        Entity e = sender.getCommandSenderEntity();
        if (args.length == 6) {
            if (e == null) {
                sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.not_entity"));
                return;
            }
            else {
                x1 = (int) (CommandBase.parseCoordinate(e.posX, args[0], true).getResult());
                y1 = (int) (CommandBase.parseCoordinate(e.posY, args[1], true).getResult());
                z1 = (int) (CommandBase.parseCoordinate(e.posZ, args[2], true).getResult());
                x2 = (int) (CommandBase.parseCoordinate(e.posX, args[3], true).getResult());
                y2 = (int) (CommandBase.parseCoordinate(e.posY, args[4], true).getResult());
                z2 = (int) (CommandBase.parseCoordinate(e.posZ, args[5], true).getResult());
                dim1 = e.dimension;
                dim2 = e.dimension;
            }
        }
        else if (args.length == 8) {
            BlockPos origin = e != null ? e.getPosition() : BlockPos.ORIGIN;
            x1 = (int) (CommandBase.parseCoordinate(origin.getX(), args[0], true).getResult());
            y1 = (int) (CommandBase.parseCoordinate(origin.getY(), args[1], true).getResult());
            z1 = (int) (CommandBase.parseCoordinate(origin.getZ(), args[2], true).getResult());
            dim1 = e != null && args[3].equals("~") ? e.dimension : CommandBase.parseInt(args[3]);
            x2 = (int) (CommandBase.parseCoordinate(origin.getX(), args[4], true).getResult());
            y2 = (int) (CommandBase.parseCoordinate(origin.getY(), args[5], true).getResult());
            z2 = (int) (CommandBase.parseCoordinate(origin.getZ(), args[6], true).getResult());
            dim2 = e != null && args[7].equals("~") ? e.dimension : CommandBase.parseInt(args[7]);
        }
        else
            throw new WrongUsageException("thaumicaugmentation.command.makefracture.usage");
        
        WorldServer world1 = DimensionManager.getWorld(dim1, true);
        WorldServer world2 = DimensionManager.getWorld(dim2, true);
        if (world1 == null || world2 == null) {
            sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.dim_unloaded"));
            return;
        }
        
        BlockPos placeAt = new BlockPos(x1, y1, z1);
        EntityDimensionalFracture fracture = new EntityDimensionalFracture(world1);
        fracture.setLocationAndAngles(x1, y1, z1, ThreadLocalRandom.current().nextInt(360), 0.0F);
        fracture.setLinkedDimension(dim2);
        fracture.setLinkedPosition(new BlockPos(x2, y2, z2));
        fracture.setLinkLocated(true);
        world1.spawnEntity(fracture);
        IFractureLocations loc = world1.getChunk(placeAt).getCapability(CapabilityFractureLocations.FRACTURE_LOCATIONS, null);
        if (loc != null) {
            loc.addFractureLocation(placeAt);
            FractureLocatorSearchManager.addFractureLocation(world1, placeAt);
        }
        
        placeAt = new BlockPos(x2, y2, z2);
        fracture = new EntityDimensionalFracture(world2);
        fracture.setLocationAndAngles(x2, y2, z2, ThreadLocalRandom.current().nextInt(360), 0.0F);
        fracture.setLinkedDimension(dim1);
        fracture.setLinkedPosition(new BlockPos(x1, y1, z1));
        fracture.setLinkLocated(true);
        world2.spawnEntity(fracture);
        loc = world2.getChunk(placeAt).getCapability(CapabilityFractureLocations.FRACTURE_LOCATIONS, null);
        if (loc != null) {
            loc.addFractureLocation(placeAt);
            FractureLocatorSearchManager.addFractureLocation(world2, placeAt);
        }
        
        sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.makefracture.done"));
    }
    
    @Override
    public String getName() {
        return "makefracture";
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos targetPos) {
        
        return Collections.emptyList();
    }
    
    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
    
}
