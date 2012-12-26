#version 100

precision mediump float;

varying vec2 vUv;
varying vec2 vUv2;
varying vec2 vUv3;
varying vec2 vUv4;

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
	vec4 sample = texture2D(sBaseTexture, vUv);

	color = color / luminance(color);

	vec4 surfaceColor = (sample + uColorAdd) * color * uColorMul;
  
  vec4 noise1 = texture2D(sNoiseTexture, vUv2);
  vec4 noise2 = texture2D(sNoiseTexture, vUv3);
  vec4 noise3 = texture2D(sNoiseTexture, vUv4);

  float noise31 = smoothstep(0.65, 0.9, noise1.r);
  vec4 baseColor = surfaceColor  + vec4(noise31,noise31,noise31,0);

  float noise = 1.25*pow(noise2.r * noise3.g, 2.0);
  vec4 noiseVec = vec4(noise,noise,noise,0);

  float noise41 = 0.9 * smoothstep(0.1, 0.3, noise2.r * noise3.g);

  gl_FragColor = (baseColor+noiseVec) * (noise41 + 1.0);
}
