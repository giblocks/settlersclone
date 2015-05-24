package game.settlers.helpers;

import java.util.Arrays;

public class MatrixHelper {
	/**
	 * Helper method that creates a perspective matrix
	 * 
	 * @param fovy
	 *            The fov in y-direction, in degrees
	 * 
	 * @param aspect
	 *            The aspect ratio
	 * @param zNear
	 *            The near clipping plane
	 * @param zFar
	 *            The far clipping plane
	 * @return A perspective matrix
	 */
	public static float[] perspective(float fovy, float aspect, float zNear,
			float zFar) {
		float radians = (float) Math.toRadians(fovy / 2);
		float deltaZ = zNear - zFar;
		float sine = (float) Math.sin(radians);
		if ((deltaZ == 0) || (sine == 0) || (aspect == 0)) {
			return identity();
		}
		float cotangent = (float) Math.cos(radians) / sine;
		float m[] = identity();
		m[0 * 4 + 0] = cotangent / aspect;
		m[1 * 4 + 1] = cotangent;
		m[2 * 4 + 2] = (zFar + zNear) / deltaZ;
		m[2 * 4 + 3] = -1;
		m[3 * 4 + 2] = 2 * zNear * zFar / deltaZ;
		m[3 * 4 + 3] = 0;
		return m;
	}

	/**
	 * Creates an identity matrix
	 * 
	 * @return An identity matrix
	 */
	public static float[] identity() {
		float m[] = new float[16];
		Arrays.fill(m, 0);
		m[0] = m[5] = m[10] = m[15] = 1.0f;
		return m;
	}

	/**
	 * Multiplies the given matrices and returns the result
	 * 
	 * @param m0
	 *            The first matrix
	 * @param m1
	 *            The second matrix
	 * @return The product m0*m1
	 */
	public static float[] multiplyMatrix(float m0[], float m1[]) {
		float m[] = new float[16];
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				m[x * 4 + y] = m0[x * 4 + 0] * m1[y + 0] + m0[x * 4 + 1]
						* m1[y + 4] + m0[x * 4 + 2] * m1[y + 8] + m0[x * 4 + 3]
						* m1[y + 12];
			}
		}
		return m;
	}

	/**
	 * Multiplies the given matrices and returns the result
	 * 
	 * @param m
	 *            The matrix
	 * @param v
	 *            The vector
	 * @return The product m0*m1
	 */
	public static float[] multiplyVector(float m[], float v[]) {
		float result[] = new float[4];

		for (int x = 0; x < 4; x++) {
			result[x] = 0.0f;
			for (int y = 0; y < 4; y++) {
				result[x] += m[x * 4 + y] * v[y];
			}
		}

		return result;
	}

	/**
	 * Creates a translation matrix
	 * 
	 * @param x
	 *            The x translation
	 * @param y
	 *            The y translation
	 * @param z
	 *            The z translation
	 * @return A translation matrix
	 */
	public static float[] translation(float x, float y, float z) {
		float m[] = identity();
		m[12] = x;
		m[13] = y;
		m[14] = z;
		return m;
	}

	/**
	 * Creates a matrix describing a rotation around the x-axis
	 * 
	 * @param angleDeg
	 *            The rotation angle, in degrees
	 * @return The rotation matrix
	 */
	public static float[] rotationX(float angleDeg) {
		float m[] = identity();
		float angleRad = (float) Math.toRadians(angleDeg);
		float ca = (float) Math.cos(angleRad);
		float sa = (float) Math.sin(angleRad);
		m[5] = ca;
		m[6] = sa;
		m[9] = -sa;
		m[10] = ca;
		return m;
	}

	/**
	 * Creates a matrix describing a rotation around the y-axis
	 * 
	 * @param angleDeg
	 *            The rotation angle, in degrees
	 * @return The rotation matrix
	 */
	public static float[] rotationY(float angleDeg) {
		float m[] = identity();
		float angleRad = (float) Math.toRadians(angleDeg);
		float ca = (float) Math.cos(angleRad);
		float sa = (float) Math.sin(angleRad);
		m[0] = ca;
		m[2] = -sa;
		m[8] = sa;
		m[10] = ca;
		return m;
	}

	/**
	 * Creates a matrix describing a rotation around the z-axis
	 * 
	 * @param angleDeg
	 *            The rotation angle, in degrees
	 * @return The rotation matrix
	 */
	public static float[] rotationZ(float angleDeg) {
		float m[] = identity();
		float angleRad = (float) Math.toRadians(angleDeg);
		float ca = (float) Math.cos(angleRad);
		float sa = (float) Math.sin(angleRad);
		m[0] = ca;
		m[4] = -sa;
		m[1] = sa;
		m[5] = ca;
		return m;
	}

	/**
	 * Returns the transpose of a 4x4 matrix
	 * 
	 * @param m
	 *            The matrix to transpose
	 * @param result
	 *            The place to store the transposed matrix
	 **/
	public static float[] transpose(float[] m) {
		float[] result = new float[m.length];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				result[j * 4 + i] = m[i * 4 + j];
			}
		}
		
		return result;
	}

	/**
	 * Converts this vector into a normalized (unit length) vector <b>Modifies
	 * the input parameter</b>
	 * 
	 **/
	public static float[] normalize(float[] vector) {
		return scalarMultiply(vector, 1 / magnitude(vector));
	}

	/**
	 * Copy a vector from <code>from</code> into <code>to</code>
	 * 
	 * @param from
	 *            The source
	 * @param to
	 *            The destination
	 **/
	public static void copy(float[] from, float[] to) {
		for (int i = 0; i < from.length; i++) {
			to[i] = from[i];
		}
	}

	/**
	 * Multiply two matrices by each other and store the result. result = m1 x
	 * m2
	 * 
	 * @param m1
	 *            The first matrix
	 * @param m2
	 *            The second matrix
	 * @param reuslt
	 *            Where to store the product of m1 x m2
	 **/
	public static void multiply(float[][] m1, float[][] m2, float[][] result) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				result[i][j] = m1[i][0] * m2[0][j] + m1[i][1] * m2[1][j]
						+ m1[i][2] * m2[2][j] + m1[i][3] * m2[3][j];
			}
		}
	}

	/**
	 * Multiply a vector by a scalar. <b>Modifies the input vector</b>
	 * 
	 * @param vector
	 *            The vector
	 * @param scalar
	 *            The scalar
	 **/
	public static float[] scalarMultiply(float[] vector, float scalar) {
		float[] result = new float[vector.length];
		for (int i = 0; i < vector.length; i++) {
			result[i] = vector[i] * scalar;
		}
		return result;
	}

	/**
	 * Compute the dot product of two vectors
	 * 
	 * @param v1
	 *            The first vector
	 * @param v2
	 *            The second vector
	 * @return v1 dot v2
	 **/
	public static float dot(float[] v1, float[] v2) {
		float res = 0;
		for (int i = 0; i < v1.length; i++)
			res += v1[i] * v2[i];
		return res;
	}

	/**
	 * Compute the cross product of two vectors
	 * 
	 * @param v1
	 *            The first vector
	 * @param v2
	 *            The second vector
	 * @param result
	 *            Where to store the cross product
	 **/
	public static float[] cross(float[] p1, float[] p2) {
		float[] result = new float[p1.length];
		
		result[0] = p1[1] * p2[2] - p2[1] * p1[2];
		result[1] = p1[2] * p2[0] - p2[2] * p1[0];
		result[2] = p1[0] * p2[1] - p2[0] * p1[1];
		
		return result;
	}

	/**
	 * Compute the magnitude (length) of a vector
	 * 
	 * @param vector
	 *            The vector
	 * @return The magnitude of the vector
	 **/
	public static float magnitude(float[] vector) {
		return (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]
				+ vector[2] * vector[2]);
	}

	/**
	 * Pretty print a vector
	 * 
	 * @param vec
	 *            The vector to print
	 **/
	public static void printVector(float[] vec) {
		for (int i = 0; i < vec.length; i++)
			System.out.println(vec[i]);
	}
	
	/**
	 * Pretty print a matrix to stdout.
	 * 
	 * @param matrix
	 *            The matrix
	 **/
	public static void printMatrix(float[] matrix) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				System.out.print(matrix[i + j * 4] + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * Homogenize a point (divide by its last element)
	 **/
	public static float[] homogenize(float[] pt) {
		return scalarMultiply(pt, 1 / pt[3]);
	}

	/**
	 * Subtracts two vectors (a-b).
	 **/
	public static float[] minus(float[] a, float[] b) {
		float[] result = new float[Math.max(a.length, b.length)];
		
		for (int i = 0; i < Math.min(a.length, b.length); i++) {
			result[i] = a[i] - b[i];
		}
		
		return result;
	}

	/**
	 * Adds two vectors (a+b).
	 * 
	 * @param a
	 *            The first vector
	 * @param b
	 *            The second vector
	 * @param result
	 *            Storage for the result, if null, store in a.
	 **/
	public static float[] plus(float[] a, float[] b) {
		float[] result = new float[Math.max(a.length, b.length)];

		for (int i = 0; i < Math.min(a.length, b.length); i++) {
			result[i] = a[i] + b[i];
		}

		return result;
	}
	
	
	
	public static float[] lookAt(float eye[], float[] center, float[] up) {
	    float[] ev = new float[] {eye[0], eye[1], eye[2]};
	    float[] cv = new float[] {center[0], center[1], center[2]};
	    float[] uv = new float[] {up[0], up[1], up[2]};

	    float[] n = normalize(minus(ev, cv));
	    float[] u = normalize(cross(uv, n));
	    float[] v = cross(n, u);

	    float[] m = { u[0], v[0], n[0], 0.0f,
	        u[1], v[1], n[1], 0.0f,
	        u[2], v[2], n[2], 0.0f,
	        dot(negative(u), ev),
	        dot(negative(v), ev),
	        dot(negative(n), ev),
	        1.0f };

	    return m;
	}

	public static float[] negative(float[] a) {
		float[] result = new float[a.length];
		for(int i = 0; i < a.length; i++) {
			result[i] = -a[i];
		}
		return result;
	}

	public static float[] ortho(float w, float h, float n, float f) {
		float[] result = new float[] {
				1.0f / w, 0.0f, 0.0f, 0.0f,
				0.0f, 1.0f / h, 0.0f, 0.0f,
				0.0f, 0.0f, 1.0f / (n - f), 0.0f,
				0.0f, 0.0f, 0.0f, 1.0f
		};
		
		return result;
	}
	
	public static float[] randomUnitVector(float rotationX, float rotationY) {
		float[] rotationMatrix = multiplyMatrix(rotationY(rotationY), rotationX(rotationX));
		float[] rotatedVector = multiplyVector(rotationMatrix, new float[] {0.0f, 1.0f, 0.0f, 1.0f});
		float[] result = new float[] {rotatedVector[0], rotatedVector[1], rotatedVector[2]};
		System.out.println("rx: " + rotationX + ", ry: " + rotationY + " : " + rotatedVector[0] + ", " + rotatedVector[1] + ", " + rotatedVector[2]);
		return result;
	}
}
