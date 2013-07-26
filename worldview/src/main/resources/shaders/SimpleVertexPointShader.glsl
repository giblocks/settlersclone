#version 150

uniform mat4 pMatrix;
uniform mat4 mvMatrix;
uniform int usePointSize;

attribute vec4 vPosition;

void main() {
    vec4 eyePos = mvMatrix * vPosition;
    float length = length(eyePos);
    gl_Position = pMatrix * mvMatrix * vPosition;
    
    if(usePointSize == 1) {
        gl_PointSize = 20.0f / length;
    } else {
        gl_PointSize = 2.0f;
    }
}
