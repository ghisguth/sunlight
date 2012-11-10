#version 100

precision mediump float;

uniform mat4 uMVPMatrix;
attribute vec4 aPosition;
varying vec4 vColor;

void main() {
    vColor = aPosition;
    gl_Position = uMVPMatrix * vec4(aPosition.x, aPosition.y, aPosition.z, aPosition.w);
}