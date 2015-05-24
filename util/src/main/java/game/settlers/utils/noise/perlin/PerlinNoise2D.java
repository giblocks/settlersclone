package game.settlers.utils.noise.perlin;

import game.settlers.helpers.MatrixHelper;
import game.settlers.utils.noise.NoiseGenerator;

import java.util.Random;

//TODO: this only does square grids at the moment
public class PerlinNoise2D implements NoiseGenerator {
	private final float size;

	private final long seed;
	private final int[] amplitudes;
	private final int[] frequencies;

	private Grid grids[];
	
	public PerlinNoise2D(float size, long seed, int[] frequencies, int[] amplitudes) {
		if(amplitudes.length != frequencies.length) {
			throw new RuntimeException("amplitudes and frequencies arrays are different lengths");
		}
		
		this.seed = seed;
		this.size = size;
		this.amplitudes = amplitudes;
		this.frequencies = frequencies;
		
		createGrids();
	}

	private void createGrids() {
		Random random = new Random(seed);
		grids = new Grid[frequencies.length];
		for(int i = 0; i < frequencies.length; i++) {
			grids[i] = new Grid(random, frequencies[i], amplitudes[i]);
		}
	}

	
	private class Grid {
		private float[][][] point;
		
		private Grid(Random random, int frequency, int amplitude) {
			point = new float[frequency][frequency][];

			for(int x = 0; x < frequency; x++) {
				for(int y = 0; y < frequency; y++) {
					point[x][y] = MatrixHelper.normalize(new float[] {random.nextFloat(), random.nextFloat(), random.nextFloat()});
//					point[x][y] = MatrixHelper.randomUnitVector(-random.nextFloat() * 180, 90-random.nextFloat() * 180);
				}
			}
		}
	}

	public float getNoise2D(float x, float y) {
		float result = 0.0f;
		
		while(x < 0) x += size;
		while(y < 0) y += size;

		for(int i = 0; i < frequencies.length ; i++) {
			float divisor = size / frequencies[i];
			float scaledX = x / divisor;
			float scaledY = y / divisor;
			int cellX = (int)Math.floor(scaledX);
			int cellY = (int)Math.floor(scaledY);
			int cellX1 = (cellX + 1) % frequencies[i];
			int cellY1 = (cellY + 1) % frequencies[i];
			
			float remainderX = scaledX - cellX;
			float remainderY = scaledY - cellY;

			float dotXY = MatrixHelper.dot(grids[i].point[cellX][cellY], new float[] {remainderX, 0f, remainderY});
			float dotX1Y = MatrixHelper.dot(grids[i].point[cellX1][cellY], new float[] {remainderX - 1f, 0f, remainderY});
			float dotXY1 = MatrixHelper.dot(grids[i].point[cellX][cellY1], new float[] {remainderX, 0f, remainderY - 1f});
			float dotX1Y1 = MatrixHelper.dot(grids[i].point[cellX1][cellY1], new float[] {remainderX - 1f, 0f, remainderY - 1f});
			
			// weghting equation = 6x5 - 15x4 + 10x3
			float weightX = (float) (6f * Math.pow(remainderX, 5) - 15f * Math.pow(remainderX, 4) + 10f * Math.pow(remainderX, 3));
			float weightY = (float) (6f * Math.pow(remainderY, 5) - 15f * Math.pow(remainderY, 4) + 10f * Math.pow(remainderY, 3));

			float avgY = dotXY * (1 - weightX) + dotX1Y * (weightX);
			float avgY1 = dotXY1 * (1 - weightX) + dotX1Y1 * (weightX);
			
			float avg = avgY * (1 - weightY) + avgY1 * (weightY);
			
			result += avg * amplitudes[i];
		}
		
		return result;
	}

	public float getNoise1D(float x) {
		// TODO Auto-generated method stub
		return 0;
	}

}
