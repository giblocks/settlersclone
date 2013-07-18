package game.settlers.worldmodel.generators;

import game.settlers.worldmodel.ModelNode;

public interface WorldGenerator {

	ModelNode[][] generateWorld(int size);

}
