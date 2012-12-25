#version 100

precision mediump float;

uniform mat4 uMVPMatrix;
uniform float uTime;
uniform float uTime2;
uniform float uTime3;

attribute vec4 aPosition;
attribute vec2 aTextureCoord;

varying vec2 vUv;
varying vec2 vUv2;
varying vec2 vUv3;
varying vec2 vUv4;
varying vec2 vUv5;

void main() {
    vUv = aTextureCoord;
    vUv2 = aTextureCoord + vec2(-uTime, 0.0);
    vUv3 = (aTextureCoord + vec2(-uTime2, 0.0)) * 2.0;
    vUv4 = (aTextureCoord + vec2(uTime3, 0.0)) * 8.0;
    gl_Position = uMVPMatrix * aPosition;
}