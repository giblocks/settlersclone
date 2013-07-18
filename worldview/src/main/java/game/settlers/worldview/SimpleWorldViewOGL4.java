package game.settlers.worldview;

import game.settlers.helpers.GLHelper;
import game.settlers.helpers.MatrixHelper;
import game.settlers.main.SimpleKeyListener;
import game.settlers.worldmodel.ModelNode;
import game.settlers.worldmodel.WorldModel;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Date;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.apache.commons.math.geometry.Vector3D;

import com.jogamp.common.nio.Buffers;

public class SimpleWorldViewOGL4 implements GLEventListener {
	private final WorldModel worldModel;
	private int program;
	
	private ViewNode[][] viewNodes;

	private float[] projectionMatrix;
    private float[] modelviewMatrix = new float[16];

    private int vertexBufferObject;
	private int normalBufferObject;
	private int indexBufferObject;

	
	private int numIndexes;

	private IntBuffer buffers;
	private SimpleKeyListener keyListener;
	
	private long lastMoveTime = new Date().getTime();
	private static float x = 0f, y = 0f, z = 0.0f, rx = 20f, ry;


	private void move() {
		int keysPressed = keyListener.getKeysPressed();
		long thisMoveTime = new Date().getTime();
		long moveTimeInterval = thisMoveTime - lastMoveTime;
		lastMoveTime = thisMoveTime;
		
		updateModelViewMatrix(keysPressed, moveTimeInterval);
	}
	
	private void updateModelViewMatrix(int keysPressed, long moveTimeInterval) {
		float moveMultiplier = (float)moveTimeInterval / 100f;
		double angle = Math.toRadians(ry);

		if((keysPressed & SimpleKeyListener.FORWARD) > 0) {
			z -= 1.0f * moveMultiplier * Math.cos(angle);
			x += 1.0f * moveMultiplier * Math.sin(angle);
		}
		if((keysPressed & SimpleKeyListener.BACK) > 0) {
			z += 1.0f * moveMultiplier * Math.cos(angle);
			x -= 1.0f * moveMultiplier * Math.sin(angle);
		}

		if((keysPressed & SimpleKeyListener.LEFT) > 0) {
			z -= 1.0f * moveMultiplier * Math.sin(angle);
			x -= 1.0f * moveMultiplier * Math.cos(angle);
		}
		if((keysPressed & SimpleKeyListener.RIGHT) > 0) {
			z += 1.0f * moveMultiplier * Math.sin(angle);
			x += 1.0f * moveMultiplier * Math.cos(angle);
		}
		
		if((keysPressed & SimpleKeyListener.ROT_LEFT) > 0) {
			ry -= 10.0f * moveMultiplier;
		}
		if((keysPressed & SimpleKeyListener.ROT_RIGHT) > 0) {
			ry += 10.0f * moveMultiplier;
		}
		ry = ry < 0 ? ry + 360f : ry > 360f ? ry - 360f : ry;
		
		if((keysPressed & SimpleKeyListener.ROT_UP) > 0) {
			rx -= 1.0f * moveMultiplier;
		}
		if((keysPressed & SimpleKeyListener.ROT_DOWN) > 0) {
			rx += 1.0f * moveMultiplier;
		}
		rx = rx < 10f ? 10f : rx > 50f ? 50f : rx;
		
		
		
		System.out.println("x: " + x + "\ty: " + y + "\tz: " + z + "\trx: " + rx + "\try: " + ry);

		updateModelViewMatrix();
	}

	private void updateModelViewMatrix() {
		float[] center = new float[] {x, y, z};
		
		float[] eyeDirection = new float[] {0.0f, 0.0f, 1.0f, 1.0f};
		eyeDirection = MatrixHelper.multiplyVector(MatrixHelper.rotationY(ry), eyeDirection);
		float[] eye = MatrixHelper.plus(center, MatrixHelper.scalarMultiply(eyeDirection, rx));

		eye[1] = rx * rx / 10f;
		
		System.out.println(String.format("c: %f,  %f, %f         e: %f, %f, %f", center[0], center[1], center[2], eye[0], eye[1], eye[2]));

		modelviewMatrix = MatrixHelper.lookAt(eye, center, new float[] {0.0f, 1.0f, 0.0f});
	}

	
	public void setModelviewMatrix(float[] modelviewMatrix) {
		this.modelviewMatrix = modelviewMatrix;
	}

	public SimpleWorldViewOGL4(WorldModel worldModel, SimpleKeyListener keyListener) {
		this.worldModel = worldModel;
		this.keyListener = keyListener;
		generateViewModel();
	}

	public SimpleWorldViewOGL4() {
		worldModel = null;
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
		
		program = gl.glCreateProgram();
		gl.glAttachShader(program, vertexShader);
		gl.glAttachShader(program, fragmentShader);
		gl.glLinkProgram(program);

		gl.glUseProgram(program);
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
		int numVertices = viewNodes.length * viewNodes[0].length;
*/		
		int numTriangles = (viewNodes.length - 1) * (viewNodes[0].length - 1) * 2;
		int numVertices = numTriangles * 3;
		
		FloatBuffer vertices = Buffers.newDirectFloatBuffer(numVertices * 3);
		FloatBuffer normals = Buffers.newDirectFloatBuffer(numVertices * 3);
		IntBuffer indices = Buffers.newDirectIntBuffer(numTriangles * 3);

		for(int x = 0; x < viewNodes.length - 1; x++) {
			for(int y = 0; y < viewNodes.length - 1; y++) {
				vertices.put(viewNodes[x][y].getTrainglesVertexData());

				normals.put(viewNodes[x][y].getTrainglesNormalData());
				
//				indices.put();
			}
		}
/*		
		for(int x = 0; x < viewNodes.length; x++) {
			for(int y = 0; y < viewNodes.length; y++) {
				vertices.put((float)viewNodes[x][y].getVertex().getX());
				vertices.put((float)viewNodes[x][y].getVertex().getY());
				vertices.put((float)viewNodes[x][y].getVertex().getZ());

				normals.put((float)viewNodes[x][y].getNormal().getX());
				normals.put((float)viewNodes[x][y].getNormal().getY());
				normals.put((float)viewNodes[x][y].getNormal().getZ());
				
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
*/
		
		
		vertices.rewind();
		normals.rewind();
		indices.rewind();
		
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

	public void display(GLAutoDrawable drawable) {
		move();
		
		GL3 gl = drawable.getGL().getGL3();

		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		gl.glUseProgram(program);
		
		float[] pMatrix = projectionMatrix;
		int pMatrixLocation = gl.glGetUniformLocation(program, "pMatrix");
		gl.glUniformMatrix4fv(pMatrixLocation, 1, false, pMatrix, 0);
		
		float[] mvMatrix = modelviewMatrix;
		int mvMatrixLocation = gl.glGetUniformLocation(program, "mvMatrix");
		gl.glUniformMatrix4fv(mvMatrixLocation, 1, false, mvMatrix, 0);

		Vector3D lightVector = new Vector3D(-10.0f, 50.0f, 5.0f);
		Vector3D normalize = lightVector.normalize();
		float[] lightSource = new float[] {(float)normalize.getX(), (float)normalize.getY(), (float)normalize.getZ()};
		int lightSourceLocation = gl.glGetUniformLocation(program, "lightSource");
		gl.glUniform4fv(lightSourceLocation, 1, lightSource, 0);
		
/*
		int colorLocation = gl.glGetUniformLocation(program, "vColor");
		gl.glUniform4fv(colorLocation, 1, color, 0);
*/		

		int vertexLocation = gl.glGetAttribLocation(program, "vPosition");
		int normalLocation = gl.glGetAttribLocation(program, "vNormal");

		gl.glEnableVertexAttribArray(vertexLocation);
		gl.glEnableVertexAttribArray(normalLocation);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferObject);
		gl.glVertexAttribPointer(vertexLocation, 3, GL3.GL_FLOAT, false, 0, 0);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, normalBufferObject);
		gl.glVertexAttribPointer(normalLocation, 3, GL3.GL_FLOAT, false, 0, 0);
		
		/*		
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
		gl.glDrawElements(GL3.GL_TRIANGLES, numIndexes, GL3.GL_UNSIGNED_INT, 0);
		 */
		gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numIndexes);

		gl.glDisableVertexAttribArray(vertexLocation);
//		gl.glDisableVertexAttribArray(normalLocation);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL3 gl = drawable.getGL().getGL3();

        gl.glViewport(0, 0, drawable.getWidth(), drawable.getHeight());

        float aspect = (float) drawable.getWidth() / drawable.getHeight();
        projectionMatrix = MatrixHelper.perspective(50, aspect, 0.1f, 100.0f);
	}

	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glDeleteBuffers(3, buffers);
	}
}
