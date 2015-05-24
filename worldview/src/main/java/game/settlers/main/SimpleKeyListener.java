package game.settlers.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

public class SimpleKeyListener implements KeyListener, MouseListener {

	public static final int FORWARD = 1;
	public static final int BACK = 2;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int ROT_LEFT = 16;
	public static final int ROT_RIGHT = 32;
	public static final int ROT_UP = 64;
	public static final int ROT_DOWN = 128;
	public static final int VIEW_CHANGE = 256;
	public static final int RESET = 512;
	public static final int ZOOM_IN = 1024;
	public static final int ZOOM_OUT = 2048;
	public static final Integer CHANGE_SHADING = 4096;
	
	private int keysPressed;

	private static Map<Character, Integer> keyMap = new HashMap<Character, Integer>();
	static {
		keyMap.put('w', FORWARD);
		keyMap.put('s', BACK);
		keyMap.put('a', LEFT);
		keyMap.put('d', RIGHT);
		keyMap.put('q', ROT_LEFT);
		keyMap.put('e', ROT_RIGHT);
		keyMap.put('r', ROT_UP);
		keyMap.put('f', ROT_DOWN);
		keyMap.put('t', VIEW_CHANGE);
		keyMap.put('1', RESET);
		keyMap.put('3', ZOOM_IN);
		keyMap.put('2', ZOOM_OUT);
		keyMap.put('z', CHANGE_SHADING);
	}

	public SimpleKeyListener() {
	}

	
	public void keyTyped(KeyEvent e) {
	}
	

	public void keyPressed(KeyEvent e) {
		char keyChar = e.getKeyChar();
		Integer keyValue = keyMap.get(keyChar);
		if(keyValue != null) {
			keysPressed |= keyValue;
		}
	}

	public void keyReleased(KeyEvent e) {
		char keyChar = e.getKeyChar();
		Integer keyValue = keyMap.get(keyChar);
		if(keyValue != null) {
			keysPressed &= Integer.MAX_VALUE ^ keyValue;
		}
	}


	public int getKeysPressed() {
		return keysPressed;
	}


	
	
	
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void mousePressed(MouseEvent e) {
		int button = e.getButton();
		if(button == MouseEvent.BUTTON1) {
			
		}
		
	}


	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
