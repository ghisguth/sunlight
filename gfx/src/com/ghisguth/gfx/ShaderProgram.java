/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.gfx;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderProgram {
    private static String TAG = "ShaderProgram";

    private int program;
    private Shader vertexShader;
    private Shader fragmentShader;

    public ShaderProgram(Shader vertexShader, Shader fragmentShader) {
        this.program = 0;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;

        if (vertexShader == null || vertexShader.getShaderType() != GLES20.GL_VERTEX_SHADER) {
            throw new RuntimeException("Expecting vertex shader");
        }

        if (fragmentShader == null || fragmentShader.getShaderType() != GLES20.GL_FRAGMENT_SHADER) {
            throw new RuntimeException("Expecting fragment shader");
        }

        ShaderManager.getSingletonObject().registerShaderProgram(this);
    }

    protected void finalize() throws Throwable {
        unload();
        super.finalize();
    }

    public boolean load() {
        if (program != 0) {
            return true;
        }

        try {
            if (!vertexShader.load() || fragmentShader.load()) {
                return false;
            }

            program = GLES20.glCreateProgram();
            if (program != 0) {
                GLES20.glAttachShader(program, vertexShader.getShader());
                ErrorHelper.checkGlError(TAG, "glAttachShader vertex");
                GLES20.glAttachShader(program, fragmentShader.getShader());
                ErrorHelper.checkGlError(TAG, "glAttachShader fragment");

                GLES20.glLinkProgram(program);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] != GLES20.GL_TRUE) {
                    Log.e(TAG, "Could not link program: ");
                    Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                    GLES20.glDeleteProgram(program);
                    program = 0;
                }
            }
        } finally {
            if (program != 0) {
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }

        return program != 0;
    }

    public void unload() {
        if (program != 0) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
    }

    public int getProgram() {
        return program;
    }

    public Shader getVertexShader() {
        return vertexShader;
    }

    private Shader getFragmentShader() {
        return fragmentShader;
    }
}
