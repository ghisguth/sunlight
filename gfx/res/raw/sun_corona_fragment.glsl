#version 100

precision mediump float;
varying vec4 vColor;
varying vec2 vTextureCoord;
varying vec2 vTextureCoord2;
varying vec2 vTextureCoord3;
varying vec2 vTextureCoord4;
uniform sampler2D sTexture;
uniform sampler2D sTexture2;
uniform sampler2D sTexture3;
uniform sampler2D sTexture4;
varying float vLevel;

float luminance(vec4 color)
{
    return dot(vec3(0.3, 0.6, 0.1), color.rgb);
}


void main() {
  vec4 sample = texture2D(sTexture, vTextureCoord);
  vec4 sample2 = texture2D(sTexture3, vTextureCoord2);
  vec4 sample3 = texture2D(sTexture3, vTextureCoord3);
  vec4 sample4 = texture2D(sTexture3, vTextureCoord4);
  //vec4 color = sample*sample2 + (sample3 * sample3.a) + (sample4 * sample4.a);
  //gl_FragColor = sample*sample2 + (sample3 + sample4)*2.0;
  float noise = pow(sample3.r * sample4.g, 2.0);
  //float noise = smoothstep(0.30, 0.7, sample3.r * sample4.g);
  //float noise2 = smoothstep(0.05, 0.3, sample3.b);

  //gl_FragColor = sample + vec4(noise,noise,noise,0);
  //gl_FragColor = vec4(noise,noise,noise,0);
  float noise3 = smoothstep(0.7, 0.9, sample2.r);
  vec4 baseColor = sample  + vec4(noise3,noise3,noise3,0);
  vec4 noiseVec = vec4(noise,noise,noise,0);
  gl_FragColor = baseColor+noiseVec;
  float noise4 = smoothstep(0.1, 0.3, sample3.r * sample4.g);
  //gl_FragColor.rgb = vec3(noise4, noise4, noise4);
  gl_FragColor.a = noise4 * vLevel + 0.3;//sample2.g * vLevel;

  //gl_FragColor = (sample + vec4(noise, noise, noise, 0))*vec4(noise2, noise2, noise2, 1);
  //gl_FragColor = vec4(noise2, noise2, noise2, 1);
  //gl_FragColor = vec4(noise+noise2, noise+noise2, noise+noise2, 1.0);
  //float lum = luminance(sample3+sample4);
  //lum = smoothstep(0.0, 0.75, lum);
  //if(lum < 0.02) {
  //  lum = 0.02;
  //}
  //if(lum > 0.98) {
  //    lum = 0.98;
  //  }
  //gl_FragColor = vec4(lum, lum, lum, 1.0);
  //vec4 lum2 = texture2D(sTexture2, vec2(lum, 0));
  //gl_FragColor = lum2;
  //gl_FragColor = sample + lum2 * 0.5;//vec4(lum, lum, lum, 1.0);

  //gl_FragColor = sample + vec4(lum, lum, lum, 1.0);
}
