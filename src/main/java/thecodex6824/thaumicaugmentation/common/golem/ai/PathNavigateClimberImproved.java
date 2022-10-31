package thecodex6824.thaumicaugmentation.common.golem.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PathNavigateClimberImproved extends PathNavigateGround {

    protected Vec3d targetPosition;

    public PathNavigateClimberImproved(EntityLiving entity) {
        super(entity, entity.world);
    }

    @Override
    public Path getPathToPos(BlockPos pos) {
        targetPosition = new Vec3d(pos).add(0.5, 0.5, 0.5);
        return super.getPathToPos(pos);
    }

    @Override
    public Path getPathToEntityLiving(Entity entity) {
        targetPosition = entity.getPositionVector();
        return super.getPathToEntityLiving(entity);
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity entity, double speed) {
        Path path = getPathToEntityLiving(entity);
        if (path != null)
            return setPath(path, speed);
        else {
            targetPosition = entity.getPositionVector();
            this.speed = speed;
            return true;
        }
    }

    @Override
    public void onUpdateNavigation() {
        if (noPath()) {
            if (targetPosition != null) {
                if (entity.getDistanceSq(targetPosition.x, targetPosition.y, targetPosition.z) >= 1.25 &&
                        (entity.posY <= MathHelper.floor(targetPosition.y) || entity.getDistanceSq(targetPosition.x, MathHelper.floor(entity.posY), targetPosition.z) >= 1.25)) {

                    entity.getMoveHelper().setMoveTo(targetPosition.x, targetPosition.y, targetPosition.z, speed);
                }
                else
                    targetPosition = null;
            }
        }
        else
            super.onUpdateNavigation();
    }

}
