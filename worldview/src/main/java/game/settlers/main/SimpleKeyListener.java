package game.settlers.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class SimpleKeyListener implements KeyListener {

	public static final int FORWARD = 1;
	public static final int BACK = 2;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int ROT_LEFT = 16;
	public static final int ROT_RIGHT = 32;
	public static final int ROT_UP = 64;
	public static final int ROT_DOWN = 128;
	
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
}
