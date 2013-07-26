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

	
	private int numIndexes;

	private IntBuffer buffers;
	
	private int numVertices;
	private int pointProgram;
	
	
	private final Camera camera;

	
	public SimpleWorldViewOGL4(WorldModel worldModel, SimpleKeyListener keyListener) {
		this.worldModel = worldModel;
		camera = new Camera(keyListener);
		generateViewModel();
	}

	private void generateViewModel() {
		ModelNode[][] groundNodes = worldModel.getGroundNodes();
		viewNodes = new ViewNode[groundNodes.length][groundNodes[0].length];
		for(int x = 0; x < groundNodes.length; x++) {
			for(int y = 0; y < groundNodes[0].length; y++) {
				viewNodes[x][y] = new ViewNode(worldModel, x, y);
			}
		}
		for(int x = 0; x < groundNodes.length; x++) {
			for(int y = 0; y < groundNodes[0].length; y++) {
				int xl = x - 1 < 0 ? groundNodes[0].length - 1 : x - 1;
				int yu = y - 1 < 0 ? groundNodes.length - 1 : y - 1;
				int xr = x + 1 == groundNodes[0].length ? 0 : x + 1;
				int yd = y + 1 == groundNodes.length ? 0 : y + 1;
				viewNodes[x][y].setSurroundingNodes(new ViewNode[] {
						viewNodes[xr][y],
						viewNodes[xr][yd],
						viewNodes[x][yd],
						viewNodes[xl][y],
						viewNodes[xl][yu],
						viewNodes[x][yu]
				});
			}
		}
		for(int x = 0; x < groundNodes.length; x++) {
			for(int y = 0; y < groundNodes[0].length; y++) {
				viewNodes[x][y].calculateNormal();
			}
		}
	}

	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		System.out.println("GL Version: " + gl.glGetString(GL3.GL_VERSION));
		gl.setSwapInterval(1);
		gl.glEnable(GL3.GL_DEPTH_TEST);

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

	
	private void createData(GL3 gl) {
		buffers = Buffers.newDirectIntBuffer(3);
		gl.glGenBuffers(3, buffers);
		vertexBufferObject = buffers.get();
		normalBufferObject = buffers.get();
		indexBufferObject = buffers.get();
		buffers.rewind();

		/*		
		int numTriangles = (viewNodes.length - 1) * (viewNodes[0].length - 1) * 2;
		int numVertices = numTriangles * 3;
		 */		
		int numTriangles = (viewNodes.length - 1) * (viewNodes[0].length - 1) * 2;
		numVertices = viewNodes.length * viewNodes[0].length;

		FloatBuffer vertices = Buffers.newDirectFloatBuffer(numVertices * 3);
		FloatBuffer normals = Buffers.newDirectFloatBuffer(numVertices * 3);
		IntBuffer indices = Buffers.newDirectIntBuffer(numTriangles * 3);

//		getSharpData(vertices, normals);

		getSmoothData(vertices, normals, indices);
		
		
		vertices.rewind();
		normals.rewind();
		indices.rewind();
		
		/*
		int i;
		i = 0;
		while(true) {
			try {
				System.out.println(i++ + ": " + vertices.get() + ", " + vertices.get() + ", " + vertices.get());
			} catch (Exception e) {
				vertices.rewind();
				break;
			}
		}
		i = 0;
		while(true) {
			try {
				System.out.println(i++ + ": " + normals.get() + ", " + normals.get() + ", " + normals.get());
			} catch (Exception e) {
				normals.rewind();
				break;
			}
		}
		i = 0;
		while(true) {
			try {
				System.out.println(i++ + ": " + indices.get() + ", " + indices.get() + ", " + indices.get());
			} catch (Exception e) {
				indices.rewind();
				break;
			}
		}
*/		
		numIndexes = indices.capacity();
		
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferObject);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertices.capacity() * Buffers.SIZEOF_FLOAT, vertices, GL3.GL_STATIC_DRAW);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, normalBufferObject);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, normals.capacity() * Buffers.SIZEOF_FLOAT, normals, GL3.GL_STATIC_DRAW);

		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
		gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * Buffers.SIZEOF_INT, indices, GL3.GL_STATIC_DRAW);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	private void getSmoothData(FloatBuffer vertices, FloatBuffer normals,
			IntBuffer indices) {
		for(int x = 0; x < viewNodes.length; x++) {
			for(int y = 0; y < viewNodes.length; y++) {
				vertices.put(viewNodes[x][y].getVertex());

				normals.put(viewNodes[x][y].getNormal());
				
				if(x < viewNodes.length - 1 && y < viewNodes[0].length - 1) {
					indices.put(x * viewNodes.length + y);
					indices.put((x + 1) * viewNodes.length + y);
					indices.put((x + 1) * viewNodes.length + (y + 1));
					
					indices.put(x * viewNodes.length + y);
					indices.put((x + 1) * viewNodes.length + (y + 1));
					indices.put(x * viewNodes.length + (y + 1));
				}
			}
		}
	}

	private void getSharpData(FloatBuffer vertices, FloatBuffer normals) {
		for(int x = 0; x < viewNodes.length - 1; x++) {
			for(int y = 0; y < viewNodes.length - 1; y++) {
				vertices.put(viewNodes[x][y].getTrainglesVertexData());

				normals.put(viewNodes[x][y].getTrainglesNormalData());
			}
		}
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
		gl.glDrawElements(GL3.GL_TRIANGLES, numIndexes, GL3.GL_UNSIGNED_INT, 0);
		/*		
		gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numIndexes);
		 */

		gl.glDisableVertexAttribArray(vertexLocation);
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
		gl.glDrawArrays(GL3.GL_POINTS, 0, numVertices);

		
		
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
