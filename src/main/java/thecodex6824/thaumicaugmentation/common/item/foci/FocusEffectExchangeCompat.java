/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.item.foci;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.items.casters.foci.FocusEffectExchange;
import thaumcraft.common.lib.events.ServerEvents;

/*
 * Wrapper + copy and paste of FocusEffectExchange to make it work with
 * addon gauntlets that implement ICaster (since the original checks for
 * ItemCaster)
 */
public class FocusEffectExchangeCompat extends FocusEffectExchange {

    protected FocusEffectExchange wrapped;

    public FocusEffectExchangeCompat(FocusEffectExchange toWrap) {
        super();
        wrapped = toWrap;
        initialize();
    }

    @Override
    public String getResearch() {
        return wrapped.getResearch();
    }

    @Override
    public String getKey() {
        return wrapped.getKey();
    }

    @Override
    public Aspect getAspect() {
        return wrapped.getAspect();
    }

    @Override
    public int getComplexity() {
        return wrapped.getComplexity();
    }

    @Override
    public boolean canSupply(EnumSupplyType arg0) {
        return wrapped.canSupply(arg0);
    }

    @Override
    public NodeSetting[] createSettings() {
        return wrapped != null ? wrapped.createSettings() : null;
    }

    @Override
    public boolean execute(RayTraceResult target, Trajectory trajectory, float finalPower, int something) {
        if (target.typeOfHit == RayTraceResult.Type.BLOCK) {
            ItemStack casterStack = ItemStack.EMPTY;
            if (getPackage().getCaster().getHeldItemMainhand() != null && getPackage().getCaster().getHeldItemMainhand().getItem() instanceof ICaster)
                casterStack = getPackage().getCaster().getHeldItemMainhand();
            else if (getPackage().getCaster().getHeldItemOffhand() != null && getPackage().getCaster().getHeldItemOffhand().getItem() instanceof ICaster)
                casterStack = getPackage().getCaster().getHeldItemOffhand();

            if (casterStack.isEmpty())
                return false;

            boolean silk = wrapped.getSettingValue("silk") > 0;
            int fortune = wrapped.getSettingValue("fortune");
            if (getPackage().getCaster() instanceof EntityPlayer && ((ICaster) casterStack.getItem()).getPickedBlock(casterStack) != null && 
                    !((ICaster) casterStack.getItem()).getPickedBlock(casterStack).isEmpty()) {

                ServerEvents.addSwapper(getPackage().world, target.getBlockPos(), 
                        getPackage().world.getBlockState(target.getBlockPos()), ((ICaster) casterStack.getItem()).getPickedBlock(casterStack), true, 0, 
                        (EntityPlayer) getPackage().getCaster(), true, false, 8038177, true, silk, fortune, 
                        ServerEvents.DEFAULT_PREDICATE, 0.25F + (silk ? 0.25F : 0.0F) + fortune * 0.1F);
            }

            return true;
        }

        return false;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return wrapped.getDamageForDisplay(finalPower);
    }

    @Override
    public float getPowerMultiplier() {
        return wrapped.getPowerMultiplier();
    }

    @Override
    public EnumUnitType getType() {
        return wrapped.getType();
    }

    @Override
    public String getUnlocalizedName() {
        return wrapped.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedText() {
        return wrapped.getUnlocalizedText();
    }

    @Override
    public boolean isExclusive() {
        return wrapped.isExclusive();
    }

    @Override
    public void onCast(Entity caster) {
        wrapped.onCast(caster);
    }

    @Override
    public void renderParticleFX(World world, double x, double y, double z, double vx, double vy, double vz) {
        wrapped.renderParticleFX(world, x, y, z, vx, vy, vz);
    }

    @Override
    public void setParent(FocusNode parent) {
        wrapped.setParent(parent);
    }

    @Override
    public RayTraceResult[] supplyTargets() {
        return wrapped.supplyTargets();
    }

    @Override
    public Trajectory[] supplyTrajectories() {
        return wrapped.supplyTrajectories();
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return wrapped.willSupply();
    }

}
