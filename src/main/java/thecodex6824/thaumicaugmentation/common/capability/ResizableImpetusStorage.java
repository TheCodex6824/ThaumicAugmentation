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

package thecodex6824.thaumicaugmentation.common.capability;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;

import java.util.Map;

public class ResizableImpetusStorage extends ImpetusStorage {

    protected Object2LongOpenHashMap<String> storages;

    public ResizableImpetusStorage(long maxEnergy) {
        this(maxEnergy, maxEnergy, maxEnergy, 0);
    }

    public ResizableImpetusStorage(long maxEnergy, long maxTransfer) {
        this(maxEnergy, maxTransfer, maxTransfer, 0);
    }

    public ResizableImpetusStorage(long maxEnergy, long maxReceive, long maxExtract) {
        this(maxEnergy, maxReceive, maxExtract, 0);
    }

    public ResizableImpetusStorage(long maxEnergy, long maxReceive, long maxExtract, long initialEnergy) {
        super(maxEnergy, maxReceive, maxExtract, initialEnergy);
        storages = new Object2LongOpenHashMap<>();
    }

    public long ensureEnergyStorage(ResourceLocation key, long amount) {
        long old = storages.put(key.toString(), amount);
        maxEnergy += amount - old;
        energy = Math.min(energy, maxEnergy);
        return old;
    }

    public long removeEnergyStorage(ResourceLocation key) {
        long res = storages.removeLong(key);
        energy = Math.min(energy, maxEnergy);
        return res;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        storages.clear();
        NBTTagCompound sources = nbt.getCompoundTag("storages");
        if (sources != null) {
            long total = 0;
            for (String k : sources.getKeySet()) {
                if (sources.hasKey(k, Constants.NBT.TAG_LONG)) {
                    long v = sources.getLong(k);
                    storages.put(k, v);
                    total += v;
                }
            }

            maxEnergy = total;
            energy = Math.min(energy, maxEnergy);
        }
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        NBTTagCompound sources = new NBTTagCompound();
        for (Map.Entry<String, Long> entry : storages.entrySet())
            sources.setLong(entry.getKey(), entry.getValue());

        tag.setTag("storages", sources);
        return tag;
    }
    
}
