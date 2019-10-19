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
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.common.world.aura.AuraHandler;

public class SubCommandFixAura implements ISubCommand {
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 5) {
            int x = 0, z = 0, dim = 0;
            boolean force = false;
            if (args.length == 0) {
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
                if (args.length < 3)
                    throw new WrongUsageException("thaumicaugmentation.command.fixaura.usage");
                else {
                    Entity e = sender.getCommandSenderEntity();
                    x = (int) (CommandBase.parseCoordinate(e != null ? e.posX : 0, args[0], false).getResult());
                    z = (int) (CommandBase.parseCoordinate(e != null ? e.posZ : 0, args[1], false).getResult());
                    dim = CommandBase.parseInt(args[2]);
                    if (args.length == 4)
                        force = Boolean.parseBoolean(args[3]);
                }
            }
            
            World world = DimensionManager.getWorld(dim);
            if (world != null) {
                if (!world.isChunkGeneratedAt(x, z)) {
                    sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.chunk_not_generated"));
                    return;
                }
                else {
                    if (AuraHandler.getAuraChunk(dim, x, z) == null || force) {
                        sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.fixaura.aura_regen"));
                        Random random = new Random(world.getSeed());
                        long xSeed = random.nextLong() >> 2 + 1;
                        long zSeed = random.nextLong() >> 2 + 1;
                        random.setSeed((xSeed * x + zSeed * z) ^ world.getSeed());
                        AuraHandler.generateAura(world.getChunk(x, z), random);
                        CopyOnWriteArrayList<ChunkPos> list = AuraHandler.dirtyChunks.get(dim);
                        if (!AuraHandler.dirtyChunks.containsKey(dim)) {
                            list = new CopyOnWriteArrayList<>();
                            AuraHandler.dirtyChunks.put(dim, list);
                        }
                        
                        ChunkPos pos = new ChunkPos(x, z);
                        if (!list.contains(pos))
                            list.add(pos);
                        
                        sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.fixaura.aura_regen_done"));
                    }
                    else {
                        if (!world.getGameRules().getBoolean("doDaylightCycle"))
                            sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.fixaura.warn_daylight"));
                        
                        sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.fixaura.aura_ok"));
                    }
                }
            }
            else
                sender.sendMessage(new TextComponentTranslation("thaumicaugmentation.command.dim_unloaded"));
        }
        else
            throw new WrongUsageException("thaumicaugmentation.command.fixaura.usage");
    }
    
    @Override
    public String getName() {
        return "fixaura";
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
