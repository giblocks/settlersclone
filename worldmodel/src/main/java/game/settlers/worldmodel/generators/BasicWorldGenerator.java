package game.settlers.worldmodel.generators;

import java.util.Random;

import game.settlers.worldmodel.ModelNode;

public class BasicWorldGenerator implements WorldGenerator {

	private int seed;

	public BasicWorldGenerator(int seed) {
		super();
		this.seed = seed;
	}

	public ModelNode[][] generateWorld(int size) {
		Random random = new Random(seed);
		
		ModelNode[][] modelNodes = new ModelNode[size][size];
		
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				modelNodes[i][j] = new ModelNode(random.nextInt(7));
			}
		}
		
		return modelNodes;
	}
}
