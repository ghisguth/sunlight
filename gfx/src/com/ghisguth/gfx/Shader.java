/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */
package com.ghisguth.gfx;

import android.opengl.GLES20;
import android.util.Log;

public class Shader {
    private static String TAG = "Sunlight";
    private int shader;
    private int shaderType;
    private String source;

    public Shader(int shaderType, String source) {
        this.shader = 0;
        this.shaderType = shaderType;
        this.source = source;
        ShaderManager.getSingletonObject().registerShader(this);
    }

    protected void finalize() throws Throwable {
        unload();
        super.finalize();
    }

    public void unload() {
        if (shader != 0) {

            if (GLES20.glIsShader(shader)) {
                GLES20.glDeleteShader(shader);
                ErrorHelper.checkGlError(TAG, "glDeleteShader");
            } else {
                Log.w(TAG, "unable to delete shader " + shader + " because it is not valid");
            }
            shader = 0;
        }
    }

    public int getShader() {
        return shader;
    }

    public int getShaderType() {
        return shaderType;
    }

    public String getSource() {
        return source;
    }

    public boolean load() {
        if (shader != 0) {
            return true;
        }
        shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);

            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }

        return shader != 0;
    }
}
