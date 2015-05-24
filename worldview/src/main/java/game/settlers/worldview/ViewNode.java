package game.settlers.worldview;

import java.util.EnumMap;

import game.settlers.helpers.MatrixHelper;
import game.settlers.worldmodel.ModelNode;
import game.settlers.worldmodel.ModelNodeListener;
import game.settlers.worldmodel.WorldModel;

public class ViewNode implements ModelNodeListener {
	private final WorldModel worldModel;
	private ViewNode[] surroundingNodes;
	
	private float[] vertex;
	private EnumMap<ShadingType, float[][]> normals = new EnumMap<ShadingType, float[][]>(ShadingType.class);
	
	private float[] color;
	
	public float[] getVertex() {
		return vertex;
	}

	public float[] getColor() {
		return color;
	}

	public void setColor(float[] color) {
		this.color = color;
	}

	public void setSurroundingNodes(ViewNode[] surroundingNodes) {
		this.surroundingNodes = surroundingNodes;
	}

	public ViewNode(WorldModel worldModel, int x, int z) {
		this.worldModel = worldModel;
//		vertex = new float[] {x / 2f + (float)(z & 1) * 0.25f, worldModel.getGroundNodes()[x][z].getHeight() / 10f, -z / 2f};
		vertex = new float[] {x / 2f - z * 0.25f, worldModel.getGroundNodes()[x][z].getHeight() / 10f, -z / 2f};
		color = new float[] {0.2f, 0.7f, 0.1f};
	}

	public void notifyModelNodeListener(ModelNode node) {
		setVerxexY(node.getHeight());
	}
	
	private void setVerxexY(float height) {
		setVertexY(height, true);
	}

	private void setVertexY(float height, boolean recalculateNormal) {
		vertex[1] = (float)height / 10f;
		if (recalculateNormal) {
			calculateNormals();
		}
	}
	
	public void calculateNormals() {
		float[][] faceNormals = getFaceNormals();
		normals.put(ShadingType.SMOOTH, new float[][] {averageNormals(faceNormals)});
		normals.put(ShadingType.FLAT, new float[][] {faceNormals[0], faceNormals[1]});
	}

	private float[][] getFaceNormals() {
		float[][] faceNormals = new float[6][];
		float[] thisCentered = new float[] {0.0f, vertex[1], 0.0f};
		for (int i = 0; i < surroundingNodes.length; i++) {
			int i2 = i < 5 ? i + 1 : i - 5;
			float[] aNode = surroundingNodes[i].vertex;
			float[] bNode = surroundingNodes[i2].vertex;

			float[] aFloat = MatrixHelper.multiplyVector(MatrixHelper.rotationY(60 * i), new float[] {1.0f, 0.0f, 0.0f, 1.0f});
			float[] bFloat = MatrixHelper.multiplyVector(MatrixHelper.rotationY(60 * (i2)), new float[] {1.0f, 0.0f, 0.0f, 1.0f});
			
			float[] a = MatrixHelper.minus(new float[] {aFloat[0], aNode[1], aFloat[2]}, thisCentered);
			float[] b = MatrixHelper.minus(new float[] {bFloat[0], bNode[1], bFloat[2]}, thisCentered);

/*			
			float[] a = MatrixHelper.minus(aNode, this.vertex);
			float[] b = MatrixHelper.minus(bNode, this.vertex);
 */
			
			a = MatrixHelper.normalize(a);
			b = MatrixHelper.normalize(b);
			
			faceNormals[i] = MatrixHelper.normalize(MatrixHelper.cross(b, a));
		}
		return faceNormals;
	}

	private float[] averageNormals(float[][] faceNormals) {
		float[] result = new float[] {0f, 0f, 0f};
		for(float[] faceNormal : faceNormals) {
			result = MatrixHelper.plus(result, faceNormal);
		}
		result = MatrixHelper.normalize(result);
		return result;
	}
	
	
	public float[][] getNormalsData(ShadingType shadingType) {
		return normals.get(shadingType);
	}
}
