package game.settlers.worldmodel;

import game.settlers.worldmodel.generators.WorldGenerator;

public class WorldModel {

	private ModelNode[][] groundNodes;

	public WorldModel(int size, WorldGenerator worldGenerator) {
		groundNodes = worldGenerator.generateWorld(size);
	}

	public ModelNode[][] getGroundNodes() {
		// TODO Auto-generated method stub
		return groundNodes;
	}

}
