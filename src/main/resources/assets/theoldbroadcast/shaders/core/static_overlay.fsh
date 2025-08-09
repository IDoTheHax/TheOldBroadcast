#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform float Opacity;
uniform vec2 ScreenSize;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

// Simple noise function for static effect
float noise(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

// TV static noise
float tvStatic(vec2 uv, float time) {
    float n = noise(uv * 100.0 + time * 50.0);
    return n * n * n; // Cube for more contrast
}

// Lens distortion effect
vec2 lensDistortion(vec2 uv) {
    vec2 center = vec2(0.5, 0.5);
    vec2 offset = uv - center;
    float distance = length(offset);

    // Barrel distortion parameters
    float k1 = 0.1;
    float k2 = 0.05;

    float distortion = 1.0 + k1 * distance * distance + k2 * distance * distance * distance * distance;
    return center + offset * distortion;
}

// Chromatic aberration
vec3 chromaticAberration(sampler2D tex, vec2 uv) {
    vec2 center = vec2(0.5, 0.5);
    vec2 offset = uv - center;
    float distance = length(offset);

    float aberration = distance * 0.01; // Aberration strength

    float r = texture(tex, uv + offset * aberration).r;
    float g = texture(tex, uv).g;
    float b = texture(tex, uv - offset * aberration).b;

    return vec3(r, g, b);
}

void main() {
    vec2 uv = texCoord0;

    // Apply lens distortion
    vec2 distortedUV = lensDistortion(uv);

    // Check if distorted UV is within valid range
    if (distortedUV.x < 0.0 || distortedUV.x > 1.0 || distortedUV.y < 0.0 || distortedUV.y > 1.0) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0); // Black borders
        return;
    }

    // Sample the screen texture with chromatic aberration
    vec3 color = chromaticAberration(Sampler0, distortedUV);

    // Add TV static based on opacity level
    float staticIntensity = Opacity * 0.3;
    float staticNoise = tvStatic(uv, Time) * staticIntensity;
    color = mix(color, vec3(staticNoise), staticIntensity);

    // Add scanlines
    float scanline = sin(uv.y * ScreenSize.y * 2.0) * 0.1 * Opacity;
    color -= scanline;

    // Vintage TV color grading
    color = mix(color, color * vec3(1.2, 1.0, 0.8), Opacity * 0.5);

    // Apply opacity
    fragColor = vec4(color, 1.0);
}
