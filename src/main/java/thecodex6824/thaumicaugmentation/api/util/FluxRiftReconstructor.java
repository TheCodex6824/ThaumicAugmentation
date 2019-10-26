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

package thecodex6824.thaumicaugmentation.api.util;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class FluxRiftReconstructor {

    protected int seed;
    protected int size;
    protected Vec3d[] points;
    protected float[] widths;
    protected AxisAlignedBB dimensions;
    
    public FluxRiftReconstructor(int riftSeed, int riftSize) {
        seed = riftSeed;
        size = riftSize;
        
        Random rand = new Random(seed);
        Vec3d right = new Vec3d(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian()).normalize();
        Vec3d left = right.scale(-1.0);
        Vec3d currentRight = new Vec3d(0.0, 0.0, 0.0);
        Vec3d currentLeft = new Vec3d(0.0, 0.0, 0.0);
        int steps = (int) Math.ceil(size / 3.0);
        
        ArrayList<Vec3d> p = new ArrayList<>();
        ArrayList<Float> w = new ArrayList<>();
        float girth = size / 300.0F;
        float girthReduction = girth / steps;
        for (int i = 0; i < steps; ++i) {
            girth -= girthReduction;
            right = right.rotatePitch((float) (rand.nextGaussian() * 0.33));
            right = right.rotateYaw((float) (rand.nextGaussian() * 0.33));
            currentRight = currentRight.add(right.scale(0.2));
            p.add(currentRight);
            w.add(girth);
            
            left = left.rotatePitch((float) (rand.nextGaussian() * 0.33));
            left = left.rotateYaw((float) (rand.nextGaussian() * 0.33));
            currentLeft = currentLeft.add(left.scale(0.2));
            p.add(0, currentLeft);
            w.add(0, girth);
        }
        
        currentRight = currentRight.add(right.scale(0.1));
        p.add(currentRight);
        w.add(0.0F);
        
        currentLeft = currentLeft.add(left.scale(0.1));
        p.add(0, currentLeft);
        w.add(0, girth);
        
        points = p.toArray(new Vec3d[p.size()]);
        widths = new float[w.size()];
        for (int i = 0; i < widths.length; ++i)
            widths[i] = w.get(i);
        
        double x1 = Double.MAX_VALUE;
        double y1 = Double.MAX_VALUE;
        double z1 = Double.MAX_VALUE;
        double x2 = Double.MIN_VALUE;
        double y2 = Double.MIN_VALUE;
        double z2 = Double.MIN_VALUE;
        for (Vec3d point : points) {
            if (point.x < x1)
                x1 = point.x;
            if (point.y < y1)
                y1 = point.y;
            if (point.z < z1)
                z1 = point.z;
            if (point.x > x2)
                x2 = point.x;
            if (point.y > y2)
                y2 = point.y;
            if (point.z > z2)
                z2 = point.z;
        }
        
        dimensions = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }
    
    public int getRiftSeed() {
        return seed;
    }
    
    public int getRiftSize() {
        return size;
    }
    
    public Vec3d[] getPoints() {
        return points;
    }
    
    public float[] getWidths() {
        return widths;
    }
    
    public AxisAlignedBB getBoundingBox() {
        return dimensions;
    }
}
