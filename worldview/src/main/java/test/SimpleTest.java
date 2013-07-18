package test;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.Animator;

public class SimpleTest implements GLEventListener {

	public static void main(String[] args) {
		Frame frame = new Frame("Simple Test");
		GLCanvas canvas = new GLCanvas();

		SimpleTest simpleTest = new SimpleTest();

		canvas.addGLEventListener(simpleTest);

		frame.add(canvas);
		frame.setSize(500, 500);
		final Animator animator = new Animator(canvas);
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// Run this on another thread than the AWT event queue to
				// make sure the call to Animator.stop() completes before
				// exiting
				new Thread(new Runnable() {
					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});
		
		frame.setVisible(true);
		animator.start();
	}

	
    private int vertexArrayObject;
    
    private int vertexBufferObject;
	private int indexBufferObject;

	private int numIndexes;

	private IntBuffer buffers;
	private int program;

	private int[] indicesArrays;

	private IntBuffer indices;

	
	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		gl.setSwapInterval(1);
		gl.glClearColor(0.2f, 0.2f, 0.5f, 1f);
		
		createProgram(gl);
		createData(gl);
	}

	private void createProgram(GL3 gl) {
		String vertexShaderCode =
				"#version 150\n" +
				"attribute vec4 vPosition;\n" + 
				"void main() {\n" +
				"gl_Position = vPosition;\n" + 
				"}";
		int vertexShader = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
		gl.glShaderSource(vertexShader, 1, new String[] {vertexShaderCode}, null, 0);
		gl.glCompileShader(vertexShader);

		String fragmentShaderCode = 
				"void main() {\n" +
				"    gl_FragColor = vec4(0.3,0.8,0.5,1.0);\n" +
				"}";
		int fragmentShader = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
		gl.glShaderSource(fragmentShader, 1, new String [] {fragmentShaderCode}, null, 0);
		gl.glCompileShader(fragmentShader);

		program = gl.glCreateProgram();
		gl.glAttachShader(program, vertexShader);
		gl.glAttachShader(program, fragmentShader);
		gl.glLinkProgram(program);

		gl.glUseProgram(program);
	}

	
	private void createData(GL3 gl) {
		buffers = Buffers.newDirectIntBuffer(3);
		gl.glGenBuffers(2, buffers);
		vertexBufferObject = buffers.get();
		indexBufferObject = buffers.get();
		buffers.rewind();

		int numTriangles = 1;
		int numVertices = 3;
		
		FloatBuffer vertices = Buffers.newDirectFloatBuffer(numVertices * 3);
		indices = Buffers.newDirectIntBuffer(numTriangles * 3);

		vertices.put(new float[] {
				0.5f, 1.0f, -1.0f,
				0.0f, 0.0f, -1.0f,
				1.0f, 0.0f, -1.0f
		});
		
		indices.put(new int[] {
				0, 1, 2
		});
		
		vertices.rewind();
		indices.rewind();
		
		numIndexes = indices.capacity();
		
		int bytesPerFloat = Float.SIZE / Byte.SIZE;
		int bytesPerInt = Integer.SIZE / Byte.SIZE;
		
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferObject);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertices.capacity() * bytesPerFloat, vertices, GL3.GL_STATIC_DRAW);

		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
		gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * bytesPerInt, indices, GL3.GL_STATIC_DRAW);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();

		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glUseProgram(program);

		int vertexLocation = gl.glGetAttribLocation(program, "vPosition");

		gl.glEnableVertexAttribArray(vertexLocation);

		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vertexBufferObject);
		gl.glVertexAttribPointer(vertexLocation, 3, GL3.GL_FLOAT, false, 0, 0);

//		gl.glDrawElements(GL3.GL_TRIANGLES, numIndexes, GL3.GL_UNSIGNED_INT, indices);

		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
		gl.glDrawElements(GL3.GL_TRIANGLES, numIndexes, GL3.GL_UNSIGNED_INT, 0);

//		gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);

		gl.glDisableVertexAttribArray(vertexLocation);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(0, 0, drawable.getWidth(), drawable.getHeight());
	}

	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glDeleteBuffers(2, buffers);
	}
}
