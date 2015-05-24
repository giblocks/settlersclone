package game.settlers.main;

import game.settlers.worldmodel.WorldModel;
import game.settlers.worldmodel.generators.BasicWorldGenerator;
import game.settlers.worldmodel.generators.SimplePerlinWorldGenerator;
import game.settlers.worldview.SimpleWorldViewOGL4;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.Animator;

public class Main {
	public static void main(String[] args) {
		Frame frame = new Frame("Settlers");
		GLCanvas canvas = new GLCanvas();
		canvas.setFocusable(false);

		final SimpleKeyListener keyListener = new SimpleKeyListener();
		frame.addKeyListener(keyListener);
		
		final WorldModel worldModel = new WorldModel(200, new SimplePerlinWorldGenerator(12345));
		final SimpleWorldViewOGL4 worldView = new SimpleWorldViewOGL4(worldModel, keyListener);

		
		canvas.addGLEventListener((GLEventListener) worldView);

		frame.add(canvas);
		frame.setSize(800, 800);
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
}
