#version 150

uniform float Time;
uniform float Opacity;
uniform vec2 ScreenSize;

in vec2 texCoord0;

out vec4 fragColor;

// Barrel distortion (lens/camera effect)
vec2 barrelDistort(vec2 uv, float strength) {
    vec2 center = vec2(0.5, 0.5);
    vec2 offset = uv - center;
    float r2 = dot(offset, offset);
    return center + offset * (1.0 + strength * r2);
}

// Procedural noise for static
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
    vec2 uv = texCoord0;
    // Barrel distortion
    float lensStrength = 0.75;
    uv = barrelDistort(uv, lensStrength);
    uv = clamp(uv, 0.0, 1.0);

    // Simulate chromatic aberration (RGB split) by offsetting color channels
    float aberrStrength = 0.1;
    float angle = atan(uv.y - 0.5, uv.x - 0.5);
    float dist = distance(uv, vec2(0.5));
    float offset = aberrStrength * dist * dist;
    vec2 rUV = uv + vec2(offset * cos(angle), offset * sin(angle));
    vec2 bUV = uv - vec2(offset * cos(angle), offset * sin(angle));

    // Fake a sampled color (since we can't sample the screen, use a stylized color)
    float base = 0.7 + 0.3 * noise(uv * ScreenSize * 0.5 + Time * 10.0);
    float r = base + 0.08 * noise(rUV * ScreenSize * 2.0 + Time * 20.0);
    float g = base + 0.08 * noise(uv * ScreenSize * 2.0 + Time * 20.0);
    float b = base + 0.08 * noise(bUV * ScreenSize * 2.0 + Time * 20.0);
    vec3 color = vec3(r, g, b);

    // Add scanlines
    float scanline = 0.85 + 0.15 * sin(uv.y * ScreenSize.y * 2.0 + Time * 20.0);
    color *= scanline;

    // Add flicker
    float flicker = 0.95 + 0.05 * sin(Time * 60.0 + uv.x * 10.0);
    color *= flicker;

    // Add static
    float staticNoise = noise(uv * ScreenSize * 4.0 + Time * 40.0);
    color = mix(color, vec3(staticNoise), 0.15);

    // Subtle greenish tint for old camera
    color.g += 0.04;

    fragColor = vec4(color, Opacity);
}
