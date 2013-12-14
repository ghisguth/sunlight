precision mediump float;

uniform mat4 uMVPMatrix;
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTextureCoord = aTextureCoord;
    gl_PointSize = 8.0;
}
