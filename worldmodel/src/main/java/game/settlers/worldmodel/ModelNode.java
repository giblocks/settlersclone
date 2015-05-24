package game.settlers.worldmodel;

import java.util.HashSet;
import java.util.Set;

public class ModelNode {

	private float height;
	private Set<ModelNodeListener> listeners = new HashSet<ModelNodeListener>();

	public ModelNode(float height) {
		this.height = height;
	}

	public float getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
		notifyListeners();
	}

	
	public void addListener(ModelNodeListener listener) {
		listeners.add(listener);
	}
	
	private void notifyListeners() {
		for(ModelNodeListener listener : listeners) {
			listener.notifyModelNodeListener(this);
		}
	}
}
