package game.settlers.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;

import org.apache.commons.io.FileUtils;

import com.jogamp.common.nio.Buffers;

public class GLHelper {

	public static int createShader(GL3 gl, int shaderType, String shaderName) {
		int shader = gl.glCreateShader(shaderType);
		try {
			String resourceLocation = "/shaders/" + shaderName + ".glsl";
			System.out.println("Resource location: " + resourceLocation);
			File shaderFile = FileUtils.toFile(GLHelper.class.getResource(resourceLocation));
			String shaderCode = FileUtils.readFileToString(shaderFile);
			
			gl.glShaderSource(shader, 1, new String [] {shaderCode}, null, 0);
			gl.glCompileShader(shader);

			IntBuffer intBuffer = Buffers.newDirectIntBuffer(20);
			gl.glGetShaderiv(shader, GL3.GL_COMPILE_STATUS, intBuffer);
			if (intBuffer.get(0) == GL3.GL_FALSE) { 
	            gl.glGetShaderiv(shader, GL3.GL_INFO_LOG_LENGTH, intBuffer); 
	            final int length = intBuffer.get(0); 
	            String out = null; 
	            if (length > 0) { 
	                ByteBuffer infoLog = Buffers.newDirectByteBuffer(length); 
	                gl.glGetShaderInfoLog(shader, infoLog.limit(), intBuffer, infoLog); 
	                final byte[] infoBytes = new byte[length]; 
	                infoLog.get(infoBytes); 
	                out = new String(infoBytes); 
	            } 
	            
	            System.out.println("Compile Error:\n" + out);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load shader file for shader '" + shaderName + "'", e);
		}
		
		return shader;
	}

}
