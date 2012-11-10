precision mediump float;

uniform mat4 uMVPMatrix;
uniform float uDelta;
uniform float uBrightness;
attribute vec4 aPosition;
varying float vColor;

void main() {
  float z = aPosition.z + uDelta;
  if(z > 1.0)
    z = z - 1.0;
  vColor = uBrightness * z;
  gl_Position = uMVPMatrix * vec4(aPosition.x, aPosition.y, z, aPosition.w);
}