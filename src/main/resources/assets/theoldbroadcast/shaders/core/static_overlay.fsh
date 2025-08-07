#version 150

uniform float Time;
uniform float Opacity;
uniform vec2 ScreenSize;

in vec2 texCoord0;

out vec4 fragColor;

// Noise functions for procedural static
float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123);
}

float noise(vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);

    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) + (c - a)* u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

void main() {
    vec2 st = texCoord0 * ScreenSize * 0.5;

    // Animated TV static effect
    float timeOffset = Time * 10.0;
    float staticNoise = noise(st + timeOffset);
    staticNoise += noise(st * 2.0 + timeOffset * 1.5) * 0.5;
    staticNoise += noise(st * 4.0 + timeOffset * 2.0) * 0.25;
    staticNoise += noise(st * 8.0 + timeOffset * 3.0) * 0.125;

    // Scanlines effect
    float scanline = sin(texCoord0.y * ScreenSize.y * 2.0) * 0.1 + 0.9;

    // Horizontal interference bands
    float interference = sin(texCoord0.y * 20.0 + Time * 5.0) * 0.05;

    // Combine effects
    float finalStatic = staticNoise * scanline + interference;
    finalStatic = clamp(finalStatic, 0.0, 1.0);

    // Create flickering intensity
    float flicker = sin(Time * 30.0) * 0.1 + 0.9;

    // Output with opacity control
    fragColor = vec4(finalStatic, finalStatic, finalStatic, finalStatic * Opacity * flicker);
}
