package test;

import game.settlers.helpers.MatrixHelper;
import game.settlers.utils.noise.perlin.PerlinNoise1D;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Date;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.Animator;

public class PerlinNoise1DViewer implements GLEventListener {

	private static PerlinNoise1D noiseGenerator;
	
	public static void main(String[] args) {
		Frame frame = new Frame("Perlin noise 1D viewer");
		GLCanvas canvas = new GLCanvas();

		noiseGenerator = new PerlinNoise1D(100, new Date().getTime(), new int[] {4}, new int[] {1});
		PerlinNoise1DViewer simpleTest = new PerlinNoise1DViewer();

		canvas.addGLEventListener(simpleTest);

		frame.add(canvas);
		frame.setSize(500, 500);
		final Animator animator = new Animator(canvas);
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				new Thread(new Runnable() {
					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});
		
		frame.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {
			    
			    noiseGenerator.createLines(new Date().getTime());

			}
			
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		frame.setVisible(true);
		animator.start();
	}

	
	
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.setSwapInterval(1);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1f);
	}


	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		gl.glLoadIdentity();
		gl.glTranslatef(-0.5f, 0.0f, 0.0f);

		gl.glBegin(GL2.GL_LINES);                      // Drawing Using Lines
		for(int x = 0; x < 5; x++) {
			float[] vector = noiseGenerator.getVector(0, x);
			vector = MatrixHelper.scalarMultiply(vector, 0.1f);
			gl.glVertex3f(x * 0.25f, 0.0f, 0.0f);
			gl.glVertex3f(x * 0.25f + vector[0], vector[1], vector[2]);
		}
		gl.glEnd();  
		
		gl.glBegin(GL2.GL_POINTS);                      // Drawing the Pointa
		for(int x = 0; x < 100; x++) {
			gl.glVertex3f(x * 0.01f, noiseGenerator.getNoise1D(x), 0.0f);              // Top
			
		}
	    	gl.glVertex3f(-1.0f,-1.0f, 0.0f);              // Bottom Left
	    	gl.glVertex3f( 1.0f,-1.0f, 0.0f);              // Bottom Right
	    gl.glEnd();
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, drawable.getWidth(), drawable.getHeight());
	}

	public void dispose(GLAutoDrawable drawable) {
	}
}
