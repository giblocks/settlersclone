package game.settlers.utils.noise;

public interface NoiseGenerator {
	
	float getNoise1D(float x);
	float getNoise2D(float x, float y);
}
