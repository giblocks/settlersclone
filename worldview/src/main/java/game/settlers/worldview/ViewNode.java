package game.settlers.worldview;

import game.settlers.helpers.MatrixHelper;
import game.settlers.worldmodel.ModelNode;
import game.settlers.worldmodel.ModelNodeListener;
import game.settlers.worldmodel.WorldModel;

public class ViewNode implements ModelNodeListener {
	private final WorldModel worldModel;
	private ViewNode[] surroundingNodes;
	
	private float[] vertex;
	private float[] normal;
	private float[] color;
/*	
	private static final Vector3D rEdge, drEdge, dlEdge;
	static {
		float[] rEdgeFloat = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
		rEdge = new Vector3D(rEdgeFloat[0], rEdgeFloat[1], rEdgeFloat[2]);

		float[] rotationMatrix = MatrixHelper.rotationY(60);
		float[] drEdgeFloat = MatrixHelper.multiplyVector(rotationMatrix, rEdgeFloat);
		drEdge = new Vector3D(drEdgeFloat[0], drEdgeFloat[1], drEdgeFloat[2]);
		
		rotationMatrix = MatrixHelper.rotationY(120);
		float[] dlEdgeFloat = MatrixHelper.multiplyVector(rotationMatrix, rEdgeFloat);
		dlEdge = new Vector3D(dlEdgeFloat[0], dlEdgeFloat[1], dlEdgeFloat[2]);
	}
*/	
	
	public float[] getVertex() {
		return vertex;
	}

	public float[] getNormal() {
		return normal;
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
	
	private void setVerxexY(int height) {
		setVertexY(height, true);
	}

	private void setVertexY(int height, boolean recalculateNormal) {
		vertex[1] = (float)height / 10f;
		if (recalculateNormal) {
			calculateNormal();
		}
	}
	
	protected void calculateNormal() {
		float[][] faceNormals = getFaceNormals();
		normal = averageNormals(faceNormals);
	}

	private float[][] getFaceNormals() {
		float[][] faceNormals = new float[6][];
		float[] thisCentered = new float[] {0.0f, vertex[1], 0.0f};
		for (int i = 0; i < surroundingNodes.length; i++) {
			int i2 = i < 5 ? i + 1 : i - 5;
			ViewNode aNode = surroundingNodes[i];
			ViewNode bNode = surroundingNodes[i2];
			
			float[] aFloat = MatrixHelper.multiplyVector(MatrixHelper.rotationY(60 * i), new float[] {1.0f, 0.0f, 0.0f, 1.0f});
			float[] bFloat = MatrixHelper.multiplyVector(MatrixHelper.rotationY(60 * (i2)), new float[] {1.0f, 0.0f, 0.0f, 1.0f});
			
			float[] a = MatrixHelper.minus(new float[] {aFloat[0], aNode.getVertex()[1], aFloat[2]}, thisCentered);
			float[] b = MatrixHelper.minus(new float[] {bFloat[0], bNode.getVertex()[1], bFloat[2]}, thisCentered);
			a = MatrixHelper.normalize(a);
			b = MatrixHelper.normalize(b);
			
			faceNormals[i] = MatrixHelper.cross(b, a);
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
	
	public float[] getTrainglesVertexData() {
		float[] vertexData = new float[6 * 3];
		int[] indexes = new int[] {-1, 2, 1, -1, 1, 0};
		
		for(int i = 0; i < 6; i++) {
			float[] vertex;
			if (indexes[i] < 0) {
				vertex = this.getVertex();
			} else {
				vertex = surroundingNodes[indexes[i]].getVertex();
			}


			vertexData[i * 3 + 0] = vertex[0];
			vertexData[i * 3 + 1] = vertex[1];
			vertexData[i * 3 + 2] = vertex[2];
		}
		
		return vertexData;
	}
	
	public float[] getTrainglesNormalData() {
		float[] normalData = new float[6 * 3];
		int[] indexes = new int[] {1, 1, 1, 0, 0, 0};

		float[][] faceNormals = getFaceNormals();
		
		for(int i = 0; i < 6; i++) {
			float[] vertex;
			if (indexes[i] < 0) {
				vertex = this.getNormal();
			} else {
				vertex = faceNormals[indexes[i]];
			}
			
			normalData[i * 3 + 0] = vertex[0];
			normalData[i * 3 + 1] = vertex[1];
			normalData[i * 3 + 2] = vertex[2];
		}		
		
		return normalData;
	}
}
