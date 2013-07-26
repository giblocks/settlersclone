uniform vec4 lightSource;

varying vec4 normal;

void main() {
    float intensity = dot(normal, lightSource);

    gl_FragColor = vec4(0.3,0.8,0.5,1.0) * intensity * intensity + vec4(0.3,0.8,0.5,1.0) * 0.1;
}
