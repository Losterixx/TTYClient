#version 150

uniform float LineWidth;

in vec4 vertexColor;
in vec3 boxCoord;

out vec4 fragColor;

float edgeFactor(vec3 coord) {
    vec3 d = fwidth(coord);
    vec3 a3 = smoothstep(vec3(0.0), d * LineWidth, coord);
    vec3 b3 = smoothstep(vec3(0.0), d * LineWidth, vec3(1.0) - coord);
    return min(min(min(a3.x, b3.x), min(a3.y, b3.y)), min(a3.z, b3.z));
}

void main() {
    float edge = 1.0 - edgeFactor(fract(boxCoord));
    fragColor = vec4(vertexColor.rgb, vertexColor.a * edge);
}

