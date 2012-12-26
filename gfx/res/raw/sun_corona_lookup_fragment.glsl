#version 100

precision mediump float;

varying vec2 vUv;
varying vec2 vUv2;
varying vec2 vUv3;
varying vec2 vUv4;
varying float vLevel;

uniform sampler2D sBaseTexture;
uniform sampler2D sNoiseTexture;
uniform sampler2D sColorTexture;

uniform float uColorOffset;
uniform float uColorAdd;
uniform float uColorMul;

float luminance(vec4 color)
{
    return dot(vec3(0.3, 0.6, 0.1), color.rgb);
}

void main() {
  vec4 color = texture2D(sColorTexture, vec2(0.0, uColorOffset));

	color = color / luminance(color);

	vec4 sample = texture2D(sBaseTexture, vUv);

	vec4 surfaceColor = (sample + uColorAdd) * color * uColorMul;

  vec4 noise2 = texture2D(sNoiseTexture, vUv3);
  vec4 noise3 = texture2D(sNoiseTexture, vUv4);

  float noise41 = 0.9 * smoothstep(0.1, 0.4, noise2.r * noise3.g);

  gl_FragColor = surfaceColor;

  gl_FragColor.a = noise41;
}
