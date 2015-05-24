package game.settlers.utils.noise.perlin;

import game.settlers.helpers.MatrixHelper;

import java.util.Random;

//TODO: this only does square grids at the moment
public class CopyOfPerlinNoise1D {
	private final float size;

	private final long seed;
	private final int[] amplitudes;
	private final int[] frequencies;

	private Line lines[];
	
	public CopyOfPerlinNoise1D(float size, long seed, int[] frequencies, int[] amplitudes) {
		if(amplitudes.length != frequencies.length) {
			throw new RuntimeException("amplitudes and frequencies arrays are different lengths");
		}
		
		this.seed = seed;
		this.size = size;
		this.amplitudes = amplitudes;
		this.frequencies = frequencies;
		
		createLines(seed);
	}

	public void createLines(long seed) {
		Random random = new Random(seed);
		lines = new Line[frequencies.length];
		for(int i = 0; i < frequencies.length; i++) {
			lines[i] = new Line(random, frequencies[i], amplitudes[i]);
		}
	}

	
	public class Line {
		private float[][] point;
		
		private Line(Random random, int frequency, int amplitude) {
			point = new float[frequency][frequency];

			for(int x = 0; x < frequency; x++) {
				point[x] = MatrixHelper.multiplyVector(MatrixHelper.rotationZ(random.nextFloat() * 180), new float[] {0.0f, 1.0f, 0.0f, 1.0f});
				point[x] = new float[] {point[x][0], point[x][1], point[x][2]};
			}
		}
	}
	
	public float[] getVector(int index, int x) {
		return lines[index].point[x % lines[index].point.length];
	}

	public float getPoint(float x) {
		float result = 0.0f;
		
		while(x < 0) x += size;

		for(int i = 0; i < frequencies.length ; i++) {
			float divisor = size / frequencies[i];
			float scaledX = x / divisor;
			int cellX = (int)Math.floor(scaledX);
			int cellX1 = (cellX + 1) % frequencies[i];
			
			float remainderX = scaledX - cellX;
			
			if(remainderX == 0f) {
				continue;
			}
			
			System.out.println("divisor: " + divisor);
			System.out.println("x: " + x);
			System.out.println("scaledX: " + scaledX);
			System.out.println("cellX: " + cellX);
			System.out.println("remainderX: " + remainderX);
			
			float dotX = MatrixHelper.dot(lines[i].point[cellX], new float[] {remainderX, 0.0f, 0.0f});
			float dotX1 = MatrixHelper.dot(lines[i].point[cellX1], new float[] {remainderX - 1.0f, 0.0f, 0.0f});
			
			// weghting equation = 6x5 - 15x4 + 10x3
			float weightX = (float) (6f * Math.pow(remainderX, 5) - 15f * Math.pow(remainderX, 4) + 10f * Math.pow(remainderX, 3));

			float avg = dotX * (1 - weightX) + dotX1 * (weightX);
			
			result += avg * amplitudes[i];
		}
		
		return result;
	}
}
