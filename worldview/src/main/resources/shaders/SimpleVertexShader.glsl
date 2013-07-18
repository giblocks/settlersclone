#version 150

uniform mat4 pMatrix;
uniform mat4 mvMatrix;

attribute vec4 vPosition;
attribute vec4 vNormal;

varying vec4 normal;

void main() {
    gl_Position = pMatrix * mvMatrix * vPosition;
    normal = vNormal;
}
