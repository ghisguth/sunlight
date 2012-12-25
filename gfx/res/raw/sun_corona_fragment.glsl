#version 100

precision mediump float;

varying vec2 vUv;
varying vec2 vUv2;
varying vec2 vUv3;
varying vec2 vUv4;
varying float vLevel;

uniform sampler2D sBaseTexture;
uniform sampler2D sNoiseTexture;

float luminance(vec4 color)
{
    return dot(vec3(0.3, 0.6, 0.1), color.rgb);
}

void main() {
  vec4 sample = texture2D(sBaseTexture, vUv);
  vec4 noise1 = texture2D(sNoiseTexture, vUv2);
  vec4 noise2 = texture2D(sNoiseTexture, vUv3);
  vec4 noise3 = texture2D(sNoiseTexture, vUv4);

  float noise31 = smoothstep(0.65, 0.9, noise1.r);
  vec4 baseColor = sample  + vec4(noise31,noise31,noise31,0);

  float noise = 1.25*pow(noise2.r * noise3.g, 2.0);
  vec4 noiseVec = vec4(noise,noise,noise,0);

  float noise41 = 0.9 * smoothstep(0.1, 0.4, noise2.r * noise3.g);

  gl_FragColor = sample;

  gl_FragColor.a = noise41;
}
