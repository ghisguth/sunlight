#version 100

precision mediump float;

varying vec2 vUv;
varying vec2 vUv2;
varying vec2 vUv3;

uniform sampler2D sBaseTexture;
uniform sampler2D sNoiseTexture;

uniform float uAlpha;

void main() {
  vec4 sample = texture2D(sBaseTexture, vUv);
  vec4 noise1 = texture2D(sNoiseTexture, vUv2);
  vec4 noise2 = texture2D(sNoiseTexture, vUv3);

  float alpha = uAlpha * 0.9 * smoothstep(0.1, 0.4, noise1.r * noise2.g);

  gl_FragColor = sample;
  gl_FragColor.a = alpha;
}
