package game.settlers.worldview;

import java.util.Date;

import game.settlers.helpers.MatrixHelper;
import game.settlers.main.SimpleKeyListener;

public class Camera {
	
	public enum ProjectionMode {
		ORTHOGONAL,
		PERSPECTIVE
	}
	
	private SimpleKeyListener keyListener;
	
	private float[] projectionMatrix;
	private float[] viewMatrix;

	private float x = 0f, y = 0f, z = 0.0f, rx = 20f, ry = 0f;
	private int screenWidth, screenHeight;
	
	private long lastMoveTime;
	private ProjectionMode projectionMode = ProjectionMode.PERSPECTIVE;
	
	private int lastKeysPressed;
	
	public Camera(SimpleKeyListener keyListener) {
		this.keyListener = keyListener;
		updateProjectionMatrix();
		updateViewMatrix(false);
	}


	
	public ProjectionMode getProjectionMode() {
		return projectionMode;
	}



	public float[] getProjectionMatrix() {
		return projectionMatrix;
	}


	public float[] getViewMatrix() {
		return viewMatrix;
	}
	
	public void update() {
		long thisMoveTime = new Date().getTime();
		if (lastMoveTime != 0) {
			int keysPressed = keyListener.getKeysPressed();
			long moveTimeInterval = thisMoveTime - lastMoveTime;
			
			updateViewMatrix(keysPressed, moveTimeInterval);
			updateProjectionMatrix();
		}
		lastMoveTime = thisMoveTime;
		
	}
	
	private void updateViewMatrix(int keysPressed, long moveTimeInterval) {
		float moveMultiplier = (float)moveTimeInterval / 100f;
		double angle = Math.toRadians(ry);
		
		if((keysPressed & SimpleKeyListener.RESET) > 0) {
			x = 0.0f;
			z = 0.0f;
			rx = 90.0f;
			ry = 0.0f;
		}
		
		
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
		rx = rx < 1f ? 1f : rx > 90f ? 90f : rx;
		
//		System.out.println("x: " + x + "\ty: " + y + "\tz: " + z + "\trx: " + rx + "\try: " + ry);

		if (keysPressed > 0) {
			updateViewMatrix((keysPressed & SimpleKeyListener.VIEW_CHANGE) > 0);
		}

		if((lastKeysPressed & SimpleKeyListener.VIEW_CHANGE) == 0 && (keysPressed & SimpleKeyListener.VIEW_CHANGE) > 0) {
			if (projectionMode == ProjectionMode.ORTHOGONAL) {
				projectionMode = ProjectionMode.PERSPECTIVE;
			} else {
				projectionMode = ProjectionMode.ORTHOGONAL;
			}
			updateProjectionMatrix();
		}
		
		lastKeysPressed = keysPressed;
	}

	private void updateViewMatrix(boolean topDown) {
		float[] center = new float[] {x, y, z};
		
		float[] eyeDirection = new float[] {0.0f, 0.0f, 1.0f, 1.0f};
		eyeDirection = MatrixHelper.multiplyVector(MatrixHelper.rotationY(ry), eyeDirection);
		float[] eye = MatrixHelper.plus(center, MatrixHelper.scalarMultiply(eyeDirection, rx));
		float[] up;
		
		if(topDown) {
//			System.out.println("top down");
			eye = new float[] {center[0], center[1] + rx, center[2]};
			up = MatrixHelper.multiplyVector(MatrixHelper.rotationY(ry), new float[] {0.0f, 0.0f, -1.0f, 1.0f});
		} else {
			eye[1] = rx * rx / 10f;
			up = new float[] {0.0f, 1.0f, 0.0f};
		}
		System.out.println(String.format("c: %f,  %f, %f         e: %f, %f, %f", center[0], center[1], center[2], eye[0], eye[1], eye[2]));
		
		viewMatrix = MatrixHelper.lookAt(eye, center, up);
/*
		viewMatrix = MatrixHelper.identity();
		viewMatrix = MatrixHelper.multiplyMatrix(modelviewMatrix, MatrixHelper.translation(-x, 0.0f, -z));
		viewMatrix = MatrixHelper.multiplyMatrix(modelviewMatrix, MatrixHelper.rotationY(ry));
		viewMatrix = MatrixHelper.multiplyMatrix(modelviewMatrix, MatrixHelper.rotationX(rx));
//		viewMatrix = MatrixHelper.multiplyMatrix(modelviewMatrix, MatrixHelper.translation(2.0f * x, -2.0f * z, -20.0f));
*/
		
	}

	
	private void updateModelViewMatrixOrtho(boolean topDown) {
		float[] center = new float[] {x, y, z};
		
		float[] eyeDirection = MatrixHelper.multiplyVector(MatrixHelper.rotationX(rx), new float[] {0.0f, 0.0f, -1.0f, 1.0f});
		eyeDirection = MatrixHelper.multiplyVector(MatrixHelper.rotationY(ry), eyeDirection);
		float[] eye = MatrixHelper.plus(center, MatrixHelper.scalarMultiply(eyeDirection, 10f));

		float[] translationMatrix = MatrixHelper.translation(eye[0], eye[1], eye[2]);
		float[] rotationMatrixX = MatrixHelper.rotationX(rx);
		float[] rotationMatrixY = MatrixHelper.rotationY(ry);
		
		
		System.out.println(String.format("c: %f,  %f, %f         e: %f, %f, %f", center[0], center[1], center[2], eye[0], eye[1], eye[2]));
		
		float[] rotationMatrix = MatrixHelper.multiplyMatrix(rotationMatrixY, rotationMatrixX);
		viewMatrix = MatrixHelper.multiplyMatrix(translationMatrix, rotationMatrix);
//		viewMatrix = rotationMatrix;
	}

	public void reshape(int width, int height) {
		screenWidth = width;
		screenHeight = height;
	}

	private void updateProjectionMatrix() {
		float aspect = (float) screenWidth / (float) screenHeight;

		switch (projectionMode) {
		case ORTHOGONAL:
			projectionMatrix = MatrixHelper.ortho(10.0f * aspect, 10.0f, 0.1f, 100.0f);
			break;
		case PERSPECTIVE:
			projectionMatrix = MatrixHelper.perspective(50, aspect, 0.1f, 100.0f);
			break;
		}
	}
}
