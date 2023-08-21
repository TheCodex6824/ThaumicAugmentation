package thecodex6824.thaumicaugmentation.common.world.noise;

import net.minecraft.util.math.MathHelper;

// Ported to Java from Steven Worley's C implementation
// Copyright information below:

/* Copyright 1994 - 2013 by Steven Worley
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to
* deal in the Software without restriction, including without limitation the
* rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
* sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
* IN THE SOFTWARE.
*/

public class NoiseGeneratorWorley {

	private static final int[] POISSON_COUNTS = new int[] {
			4,3,1,1,1,2,4,2,2,2,5,1,0,2,1,2,2,0,4,3,2,1,2,1,3,2,2,4,2,2,5,1,2,3,2,2,2,2,2,3,
			2,4,2,5,3,2,2,2,5,3,3,5,2,1,3,3,4,4,2,3,0,4,2,2,2,1,3,2,2,2,3,3,3,1,2,0,2,1,1,2,
			2,2,2,5,3,2,3,2,3,2,2,1,0,2,1,1,2,1,2,2,1,3,4,2,2,2,5,4,2,4,2,2,5,4,3,2,2,5,4,3,
			3,3,5,2,2,2,2,2,3,1,1,4,2,1,3,3,4,3,2,4,3,3,3,4,5,1,4,2,4,3,1,2,3,5,3,2,1,3,1,3,
			3,3,2,3,1,5,5,4,2,2,4,1,3,4,1,5,3,3,5,3,4,3,2,2,1,1,1,1,1,2,4,5,4,5,4,2,1,5,1,1,
			2,3,3,3,2,5,2,3,3,2,0,2,1,1,4,2,1,3,2,1,2,2,3,2,5,5,3,4,5,5,2,4,4,5,3,2,2,2,1,4,
			2,3,3,4,2,5,4,2,4,2,2,2,4,5,3,2
	};
	
	private static void addSamples(long seed, double[] distances, double posX, double posY, double posZ,
			int cubeX, int cubeY, int cubeZ) {
		
		seed ^= 702395077 * cubeX + 915488749 * cubeY + 2120969693 * cubeZ;
		int count = POISSON_COUNTS[(int) (seed >>> 56)];
		seed = 1402024253 * seed + 586950981;
		for (int i = 0; i < count; ++i) {
			seed = 1402024253 * seed + 586950981;
			double fx = ((seed & 0xFFFFFFFFL) + 0.5) / 4294967296.0;
			seed = 1402024253 * seed + 586950981;
			double fy = ((seed & 0xFFFFFFFFL) + 0.5) / 4294967296.0;
			seed = 1402024253 * seed + 586950981;
			double fz = ((seed & 0xFFFFFFFFL) + 0.5) / 4294967296.0;
			seed = 1402024253 * seed + 586950981;
			
			double dx = cubeX + fx - posX;
			double dy = cubeY + fy - posY;
			double dz = cubeZ + fz - posZ;
			
			double distance = dx * dx + dy * dy + dz * dz;
			if (distance < distances[0]) {
				distances[1] = distances[0];
				distances[0] = distance;
			}
			else if (distance < distances[1]) {
				distances[1] = distance;
			}
		}
	}
	
	public static float generate(long seed, double x, double y, double z) {
		x *= 0.025;
		y *= 0.025;
		z *= 0.025;
		double[] pos = new double[] { x, y, z };
		double[] distances = new double[] { 10.0, 10.0 };
		int cubeX = (int) x;
		int cubeY = (int) y;
		int cubeZ = (int) z;
		
		addSamples(seed, distances, x, y, z, cubeX, cubeY, cubeZ);
		double x2 = pos[0] - cubeX;
	    double y2 = pos[1] - cubeY;
	    double z2 = pos[2] - cubeZ;
	    double mx2 = (1.0 - x2) * (1.0 - x2);
	    double my2 = (1.0 - y2) * (1.0 - y2);
	    double mz2 = (1.0 - z2) * (1.0 - z2);
	    x2 *= x2;
	    y2 *= y2;
	    z2 *= z2;
	    
	    // 6 direct cube neighbors (closest)
	    if (x2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX - 1, cubeY, cubeZ);
	    }
	    if (y2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX, cubeY - 1, cubeZ);
	    }
	    if (z2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX, cubeY, cubeZ - 1);
	    }
	    
	    if (mx2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX + 1, cubeY, cubeZ);
	    }
	    if (my2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX, cubeY + 1, cubeZ);
	    }
	    if (mz2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX, cubeY, cubeZ + 1);
	    }
	    
	    // 12 neighbors on edges of cube
	    if (x2 + y2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX - 1, cubeY - 1, cubeZ);
	    }
	    if (x2 + z2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX - 1, cubeY, cubeZ - 1);
	    }
	    if (y2 + z2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX, cubeY - 1, cubeZ - 1);
	    }
	    
	    if (mx2 + my2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX + 1, cubeY + 1, cubeZ);
	    }
	    if (mx2 + mz2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX + 1, cubeY, cubeZ + 1);
	    }
	    if (my2 + mz2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX, cubeY + 1, cubeZ + 1);
	    }
	    
	    if (x2 + my2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX - 1, cubeY + 1, cubeZ);
	    }
	    if (x2 + mz2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX - 1, cubeY, cubeZ + 1);
	    }
	    if (y2 + mz2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX, cubeY - 1, cubeZ + 1);
	    }
	    if (mx2 + y2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX + 1, cubeY - 1, cubeZ);
	    }
	    if (mx2 + z2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX + 1, cubeY, cubeZ - 1);
	    }
	    if (my2 + z2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX, cubeY + 1, cubeZ - 1);
	    }
	    
	    // Final 8 cubes on each corner/vertex
	    if (x2 + y2 + z2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX - 1, cubeY - 1, cubeZ - 1);
	    }
	    if (x2 + y2 + mz2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX - 1, cubeY - 1, cubeZ + 1);
	    }
	    if (x2 + my2 + z2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX - 1, cubeY + 1, cubeZ - 1);
	    }
	    if (x2 + my2 + mz2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX - 1, cubeY + 1, cubeZ + 1);
	    }
	    if (mx2 + y2 + z2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX + 1, cubeY - 1, cubeZ - 1);
	    }
	    if (mx2 + y2 + mz2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX + 1, cubeY - 1, cubeZ + 1);
	    }
	    if (mx2 + my2 + z2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX + 1, cubeY + 1, cubeZ - 1);
	    }
	    if (mx2 + my2 + mz2 < distances[1]) {
	    	addSamples(seed, distances, x, y, z, cubeX + 1, cubeY + 1, cubeZ + 1);
	    }
	    
	    return MathHelper.sqrt(distances[1]) - MathHelper.sqrt(distances[0]);
	}
	
}
