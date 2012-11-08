precision mediump float;

uniform vec3 mColor;
varying float vColor;

void main() {
  gl_FragColor = vec4(vColor,vColor,vColor,1) * vec4(mColor.x,mColor.y,mColor.z,1);
}