precision mediump float;

uniform vec3 uColor;
varying float vColor;

void main() {
  gl_FragColor = vec4(vColor,vColor,vColor,1) * vec4(uColor.x,uColor.y,uColor.z,1);
}