package game.settlers.worldmodel.generators;

import game.settlers.utils.noise.NoiseGenerator;
import game.settlers.utils.noise.perlin.PerlinNoise2D;
import game.settlers.worldmodel.ModelNode;

public class SimplePerlinWorldGenerator implements WorldGenerator{

	private long seed;
	private float scale = 1.0f;

	public SimplePerlinWorldGenerator(long seed) {
		this.seed = seed;
		
	}
	
	public ModelNode[][] generateWorld(int size) {
		ModelNode[][] modelNodes = new ModelNode[size][size];
		
//		NoiseGenerator noiseGenerator = new PerlinNoise2D(size * scale, seed, new int[] {3, 5, 7}, new int[] {10, 5, 3});
		NoiseGenerator noiseGenerator = new PerlinNoise2D(size * scale, seed, new int[] {10}, new int[] {2});
//		NoiseGenerator noiseGenerator = new BasicNoise2D(size * scale, seed, new int[] {4}, new int[] {10});
//		NoiseGenerator noiseGenerator = new BasicNoise2D(size * scale, seed, new int[] {4, 12}, new int[] {20, 10});
		
		float yFactor = (float) Math.cos(Math.toRadians(30)) * scale;
		for(int x = 0; x < size; x++) {
			for(int y = 0; y < size; y++) {
				float xReal = x;// - scale / 2f;
				float yReal = y;
				float hReal = noiseGenerator.getNoise2D(xReal, yReal);
//				modelNodes[x][y] = new ModelNode(hReal);
				modelNodes[x][y] = new ModelNode((int)(hReal * 10));
			}
		}
		
		return modelNodes;
	}

}
