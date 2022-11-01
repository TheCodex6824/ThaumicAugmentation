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

package thecodex6824.thaumicaugmentation.server.command.sub;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thecodex6824.thaumicaugmentation.api.ward.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorageServer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class SubCommandWard implements ISubCommand {

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1 && args.length < 5 && args[0].equalsIgnoreCase("clear")) {
            int x = 0, z = 0, dim = 0;
            if (args.length == 1) {
                Entity e = sender.getCommandSenderEntity();
                if (e == null) {
                    sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.not_entity"));
                    return;
                }
                else {
                    x = e.chunkCoordX;
                    z = e.chunkCoordZ;
                    dim = e.dimension;
                }
            }
            else {
                if (args.length < 4)
                    throw new WrongUsageException("thaumicaugmentation.command.ward.usage");
                else {
                    Entity e = sender.getCommandSenderEntity();
                    x = (int) (CommandBase.parseCoordinate(e != null ? e.posX : 0, args[1], false).getResult());
                    z = (int) (CommandBase.parseCoordinate(e != null ? e.posZ : 0, args[2], false).getResult());
                    dim = e != null && args[3].equals("~") ? e.dimension : CommandBase.parseInt(args[3]);
                }
            }
            
            World world = DimensionManager.getWorld(dim);
            if (world != null) {
                if (!world.isChunkGeneratedAt(x, z)) {
                    sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.chunk_not_generated"));
                }
                else {
                    IWardStorage storage = world.getChunk(x, z).getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                    if (storage instanceof IWardStorageServer) {
                        ((IWardStorageServer) storage).clearAllWards(world, new BlockPos(x * 16, 0, z * 16));
                        sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.ward.cleared"));
                    }
                    else
                        sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.ward.not_server"));
                }
            }
            else
                sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.dim_unloaded"));
        }
        else
            throw new WrongUsageException("thaumicaugmentation.command.ward.usage");
    }
    
    @Override
    public String getName() {
        return "ward";
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos targetPos) {

        if (args.length == 1)
            return CommandBase.getListOfStringsMatchingLastWord(args, "clear");

        return Collections.emptyList();
    }
    
    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
    
}
