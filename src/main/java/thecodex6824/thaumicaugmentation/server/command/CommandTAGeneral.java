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

package thecodex6824.thaumicaugmentation.server.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import thecodex6824.thaumicaugmentation.server.command.sub.ISubCommand;
import thecodex6824.thaumicaugmentation.server.command.sub.SubCommandFixAura;
import thecodex6824.thaumicaugmentation.server.command.sub.SubCommandWard;

public class CommandTAGeneral implements ICommand {

    protected static final ImmutableList<String> ALIASES = ImmutableList.of("thaumicaugmentation", "ta");
    protected HashMap<String, ISubCommand> subCommands;
    
    private void putSubCommand(ISubCommand c) {
        subCommands.put(c.getName().toLowerCase(), c);
    }
    
    public CommandTAGeneral() {
        subCommands = new HashMap<>();
        putSubCommand(new SubCommandFixAura());
        putSubCommand(new SubCommandWard());
    }
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(2, "thaumicaugmentation");
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1) {
            ISubCommand command = subCommands.get(args[0].toLowerCase());
            if (command != null)
                command.execute(server, sender, Arrays.copyOfRange(args, 1, args.length));
            else
                throw new CommandException("thaumicaugmentation.command.invalid_command");
        }
    }
    
    @Override
    public List<String> getAliases() {
        return ALIASES;
    }
    
    @Override
    public String getName() {
        return "thaumicaugmentation";
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos targetPos) {
        
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        StringBuilder base = new StringBuilder(getName() + " <");
        for (ISubCommand c : subCommands.values())
            base.append(c.getName() + "|");
        
        return base.append(">").toString();
    }
    
    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        if (args.length >= 1) {
            ISubCommand command = subCommands.get(args[0].toLowerCase());
            if (command != null)
                return command.isUsernameIndex(Arrays.copyOfRange(args, 1, args.length), index - 1);
        }
        
        return false;
    }
    
    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
    
}
