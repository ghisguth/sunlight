#version 100

precision mediump float;
varying vec4 vColor;

void main() {
  gl_FragColor = vec4(1,vColor.y,0,1);
}