package game.settlers.worldview;

import game.settlers.helpers.GLHelper;
import game.settlers.main.SimpleKeyListener;
import game.settlers.worldmodel.ModelNode;
import game.settlers.worldmodel.WorldModel;
import game.settlers.worldview.Camera.ProjectionMode;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.apache.commons.math.geometry.Vector3D;

import com.jogamp.common.nio.Buffers;

public class SimpleWorldViewOGL4 implements GLEventListener {
	

	private final WorldModel worldModel;
	private int traingleProgram;
	
	private ViewNode[][] viewNodes;

    private int vertexBufferObject;
	private int normalBufferObject;
	private int indexBufferObject;

	
	private IntBuffer buffers;
	
	private int pointProgram;
	
	
	private final Camera camera;
	private ShadingType shadingType = ShadingType.FLAT;
	private int numIndices;

	
	public SimpleWorldViewOGL4(WorldModel worldModel, SimpleKeyListener keyListener) {
		this.worldModel = worldModel;
		camera = new Camera(keyListener);
		generateViewModel();
	}

	private void generateViewModel() {
		ModelNode[][] groundNodes = worldModel.getGroundNodes();
		viewNodes = new ViewNode[groundNodes.length][groundNodes[0].length];
		for(int x = 0; x < groundNodes.length; x++) {
			for(int z = 0; z < groundNodes[0].length; z++) {
				viewNodes[x][z] = new ViewNode(worldModel, x, z);
			}
		}
		for(int x = 0; x < groundNodes.length; x++) {
			for(int z = 0; z < groundNodes[0].length; z++) {
				int xm1 = x - 1 < 0 ? groundNodes[0].length - 1 : x - 1;
				int zm1 = z - 1 < 0 ? groundNodes.length - 1 : z - 1;
				int xp1 = x + 1 == groundNodes[0].length ? 0 : x + 1;
				int zp1 = z + 1 == groundNodes.length ? 0 : z + 1;
				viewNodes[x][z].setSurroundingNodes(new ViewNode[] {
						viewNodes[xp1][z],
						viewNodes[xp1][zp1],
						viewNodes[x][zp1],
						viewNodes[xm1][z],
						viewNodes[xm1][zm1],
						viewNodes[x][zm1]
				});
			}
		}
		for(int x = 0; x < groundNodes.length; x++) {
			for(int z = 0; z < groundNodes[0].length; z++) {
				viewNodes[x][z].calculateNormals();
			}
		}
	}

	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		System.out.println("GL Version: " + gl.glGetString(GL3.GL_VERSION));
		gl.setSwapInterval(1);
		gl.glEnable(GL3.GL_DEPTH_TEST);
		gl.glEnable(GL3.GL_CULL_FACE);

		createProgram(gl);
		createData(gl);
	}

	private void createProgram(GL3 gl) {
		int vertexShader = GLHelper.createShader(gl, GL3.GL_VERTEX_SHADER, "SimpleVertexShader");
		int fragmentShader = GLHelper.createShader(gl, GL3.GL_FRAGMENT_SHADER, "SimpleFragmentShader");
		
		traingleProgram = gl.glCreateProgram();
		gl.glAttachShader(traingleProgram, vertexShader);
		gl.glAttachShader(traingleProgram, fragmentShader);
		gl.glLinkProgram(traingleProgram);

		
		int vertexPointShader = GLHelper.createShader(gl, GL3.GL_VERTEX_SHADER, "SimpleVertexPointShader");
		int fragmentPointShader = GLHelper.createShader(gl, GL3.GL_FRAGMENT_SHADER, "SimpleFragmentPointShader");
		
		pointProgram = gl.glCreateProgram();
		gl.glAttachShader(pointProgram, vertexPointShader);
		gl.glAttachShader(pointProgram, fragmentPointShader);
		gl.glLinkProgram(pointProgram);
	}

	private class DataMap {
		private final FloatBuffer vertices;
		private final FloatBuffer normals;
		private final IntBuffer indices;

		private DataMap(FloatBuffer vertices, FloatBuffer normals, IntBuffer indices) {
			super();
			this.vertices = vertices;
			this.normals = normals;
			this.indices = indices;
		};
	}
	
	private void createData(GL3 gl) {
		buffers = Buffers.newDirectIntBuffer(3);
		gl.glGenBuffers(3, buffers);
		vertexBufferObject = buffers.get();
		normalBufferObject = buffers.get();
		indexBufferObject = buffers.get();
		buffers.rewind();
		
		fillBufferObjects(gl);
	}
	
	public void changeShadingType(GL3 gl, ShadingType shadingType) {
		this.shadingType = shadingType;
		fillBufferObjects(gl);
	}
	
	private void fillBufferObjects(GL3 gl) {
		DataMap dataMap;
		if(shadingType  == ShadingType.FLAT) {
			dataMap = createFlatData(gl);
		} else {
			dataMap = createSmoothData(gl);
		}
		
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferObject);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, dataMap.vertices.capacity() * Buffers.SIZEOF_FLOAT, dataMap.vertices, GL3.GL_STATIC_DRAW);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, normalBufferObject);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, dataMap.normals.capacity() * Buffers.SIZEOF_FLOAT, dataMap.normals, GL3.GL_STATIC_DRAW);

		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
		gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, dataMap.indices.capacity() * Buffers.SIZEOF_INT, dataMap.indices, GL3.GL_STATIC_DRAW);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	private DataMap createFlatData(GL3 gl) {
		int numVertices = (viewNodes.length - 1) * (viewNodes.length - 1) * 18;
		int numNormals = (viewNodes.length - 1) * (viewNodes.length - 1) * 18;
		numIndices = (viewNodes.length - 1) * (viewNodes.length - 1) * 6;
		
		FloatBuffer vertices = Buffers.newDirectFloatBuffer(numVertices);
		FloatBuffer normals = Buffers.newDirectFloatBuffer(numNormals);
		IntBuffer indices = Buffers.newDirectIntBuffer(numIndices);
		DataMap dataMap = new DataMap(vertices, normals, indices);
		
		for(int x = 0; x < viewNodes.length - 1; x++) {
			for(int z = 0; z < viewNodes.length - 1; z++) {
				vertices.put(viewNodes[x][z].getVertex());
				vertices.put(viewNodes[x+1][z+1].getVertex());
				vertices.put(viewNodes[x][z+1].getVertex());

				vertices.put(viewNodes[x][z].getVertex());
				vertices.put(viewNodes[x+1][z].getVertex());
				vertices.put(viewNodes[x+1][z+1].getVertex());
				
				float[][] faceNormals = viewNodes[x][z].getNormalsData(ShadingType.FLAT);
//				System.out.println("x: " + x + ", z: " + z + ", normal0: " + faceNormals[0][0] + ", " + faceNormals[0][0] + ", " + faceNormals[0][0]);
//				System.out.println("x: " + x + ", z: " + z + ", normal1: " + faceNormals[1][0] + ", " + faceNormals[1][0] + ", " + faceNormals[1][0]);
				normals.put(faceNormals[0]);
				normals.put(faceNormals[0]);
				normals.put(faceNormals[0]);

				normals.put(faceNormals[1]);
				normals.put(faceNormals[1]);
				normals.put(faceNormals[1]);
				
				indices.put(6 * (x * (viewNodes.length - 1) + z) + 0);
				indices.put(6 * (x * (viewNodes.length - 1) + z) + 1);
				indices.put(6 * (x * (viewNodes.length - 1) + z) + 2);

				indices.put(6 * (x * (viewNodes.length - 1) + z) + 3);
				indices.put(6 * (x * (viewNodes.length - 1) + z) + 4);
				indices.put(6 * (x * (viewNodes.length - 1) + z) + 5);
			}
		}

		vertices.rewind();
		normals.rewind();
		indices.rewind();

		return dataMap;
	}

	private DataMap createSmoothData(GL3 gl) {
		int numVertices = viewNodes.length * viewNodes.length * 3;
		int numNormals = viewNodes.length * viewNodes.length * 3;
		numIndices = viewNodes.length * viewNodes.length * 6;
		
		FloatBuffer vertices = Buffers.newDirectFloatBuffer(numVertices);
		FloatBuffer normals = Buffers.newDirectFloatBuffer(numNormals);
		IntBuffer indices = Buffers.newDirectIntBuffer(numIndices);
		DataMap dataMap = new DataMap(vertices, normals, indices);
		
		for(int x = 0; x < viewNodes.length; x++) {
			for(int z = 0; z < viewNodes.length; z++) {
				vertices.put(viewNodes[x][z].getVertex());
				normals.put(viewNodes[x][z].getNormalsData(ShadingType.SMOOTH)[0]);
				
				if(x < viewNodes.length - 1 && z < viewNodes[0].length - 1) {
					indices.put(x * viewNodes.length + z);
					indices.put((x + 1) * viewNodes.length + z);
					indices.put((x + 1) * viewNodes.length + z + 1);

					indices.put(x * viewNodes.length + z);
					indices.put((x + 1) * viewNodes.length + z + 1);
					indices.put(x * viewNodes.length + z + 1);
				}
			}
		}
		
		vertices.rewind();
		normals.rewind();
		indices.rewind();
		
		return dataMap;
	}
	
	public void display(GLAutoDrawable drawable) {
		camera.update();
		
		GL3 gl = drawable.getGL().getGL3();

		gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

		gl.glUseProgram(traingleProgram);
		
		gl.glEnable(GL3.GL_DEPTH_TEST);
		
		int pMatrixLocation = gl.glGetUniformLocation(traingleProgram, "pMatrix");
		gl.glUniformMatrix4fv(pMatrixLocation, 1, false, camera.getProjectionMatrix(), 0);
		
		int mvMatrixLocation = gl.glGetUniformLocation(traingleProgram, "mvMatrix");
		gl.glUniformMatrix4fv(mvMatrixLocation, 1, false, camera.getViewMatrix(), 0);

		Vector3D lightVector = new Vector3D(-10.0f, 50.0f, 5.0f);
		Vector3D normalize = lightVector.normalize();
		float[] lightSource = new float[] {(float)normalize.getX(), (float)normalize.getY(), (float)normalize.getZ()};
		int lightSourceLocation = gl.glGetUniformLocation(traingleProgram, "lightSource");
		gl.glUniform4fv(lightSourceLocation, 1, lightSource, 0);
		
/*
		int colorLocation = gl.glGetUniformLocation(program, "vColor");
		gl.glUniform4fv(colorLocation, 1, color, 0);
*/		

		int vertexLocation = gl.glGetAttribLocation(traingleProgram, "vPosition");
		int normalLocation = gl.glGetAttribLocation(traingleProgram, "vNormal");

		gl.glEnableVertexAttribArray(vertexLocation);
		gl.glEnableVertexAttribArray(normalLocation);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferObject);
		gl.glVertexAttribPointer(vertexLocation, 3, GL3.GL_FLOAT, false, 0, 0);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, normalBufferObject);
		gl.glVertexAttribPointer(normalLocation, 3, GL3.GL_FLOAT, false, 0, 0);
		
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
		gl.glDrawElements(GL3.GL_TRIANGLES, numIndices, GL3.GL_UNSIGNED_INT, 0);

//		gl.glDisableVertexAttribArray(vertexLocation);
//		gl.glDisableVertexAttribArray(normalLocation);


		// Draw points
		gl.glUseProgram(pointProgram);

		pMatrixLocation = gl.glGetUniformLocation(pointProgram, "pMatrix");
		gl.glUniformMatrix4fv(pMatrixLocation, 1, false, camera.getProjectionMatrix(), 0);
		
		mvMatrixLocation = gl.glGetUniformLocation(pointProgram, "mvMatrix");
		gl.glUniformMatrix4fv(mvMatrixLocation, 1, false, camera.getViewMatrix(), 0);

		int pointLocation = gl.glGetAttribLocation(pointProgram, "vPosition");
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferObject);
		gl.glVertexAttribPointer(pointLocation, 3, GL3.GL_FLOAT, false, 0, 0);

		gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
		int usePointSizeLocation = gl.glGetUniformLocation(pointProgram, "usePointSize");
		if(camera.getProjectionMode() == ProjectionMode.PERSPECTIVE) {
			gl.glUniform1i(usePointSizeLocation, 1);
		} else {
			gl.glUniform1i(usePointSizeLocation, 0);
		}
//		gl.glDisable(GL3.GL_DEPTH_TEST);
		gl.glDrawElements(GL3.GL_TRIANGLES, numIndices, GL3.GL_UNSIGNED_INT, 0);
//		gl.glDrawArrays(GL3.GL_POINTS, 0, numVertices);

		
		
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL3 gl = drawable.getGL().getGL3();

        gl.glViewport(0, 0, drawable.getWidth(), drawable.getHeight());
        camera.reshape(width, height);
	}

	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glDeleteBuffers(3, buffers);
	}
}
