#version 100

precision mediump float;

uniform mat4 uMVPMatrix;
uniform float uAnimationTime;
uniform float uAnimationTime2;
uniform float uAnimationTime3;
uniform float uLevel;
attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec4 vColor;
varying vec2 vTextureCoord;
varying vec2 vTextureCoord2;
varying vec2 vTextureCoord3;
varying vec2 vTextureCoord4;
varying float vLevel;

void main() {
    vColor = vec4(1,1,1,1);//normalize(aPosition);//+0.2*vec4(0.5 + aTextureCoord.x * 0.5, 0.5 + aTextureCoord.y * 0.5, 0, 1));
    vTextureCoord = aTextureCoord;
    vTextureCoord2 = aTextureCoord + vec2(-uAnimationTime, 0.0);
    vTextureCoord3 = (aTextureCoord + vec2(-uAnimationTime2, 0.0)) * 2.0;
    vTextureCoord4 = (aTextureCoord + vec2(uAnimationTime3, 0.0)) * 6.0;
    gl_Position = uMVPMatrix * aPosition;
    gl_PointSize = 4.0;
    vLevel = uLevel;
}