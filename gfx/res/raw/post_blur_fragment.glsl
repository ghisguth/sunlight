precision mediump float;

uniform float uBlur;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;

void main() {
  gl_FragColor = texture2D(sTexture, vTextureCoord) * vec4(1,1,1,uBlur);
}
