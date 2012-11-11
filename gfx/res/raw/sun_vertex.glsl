#version 100

precision mediump float;

uniform mat4 uMVPMatrix;
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec4 vColor;
varying vec2 vTextureCoord;

void main() {
    vColor = normalize(aPosition);
    vTextureCoord = aTextureCoord;
    gl_Position = uMVPMatrix * aPosition;
    gl_PointSize = 4.0;
}