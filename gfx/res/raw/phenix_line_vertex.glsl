precision mediump float;

uniform mat4 uMVPMatrix;
uniform float delta;
uniform float brightness;
attribute vec4 aPosition;
varying float vColor;

void main() {
  float z = aPosition.z + delta;
  if(z > 1.0)
    z = z - 1.0;
  vColor = brightness * z;
  gl_Position = uMVPMatrix * vec4(aPosition.x, aPosition.y, z, aPosition.w);
}