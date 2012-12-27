/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.gfx;

import android.opengl.GLES20;
import android.util.Log;

import java.util.HashMap;

public class Program {
    private static String TAG = "Sunlight";
    private int program;
    private Shader vertexShader;
    private Shader fragmentShader;
    private HashMap<String, Integer> uniformLocations = new HashMap<String, Integer>();
    private HashMap<String, Integer> attributeLocations = new HashMap<String, Integer>();

    public Program(Shader vertexShader, Shader fragmentShader) {
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

    public void unload() {
        if (program != 0) {
            if (GLES20.glIsProgram(program)) {
                GLES20.glDeleteProgram(program);
                ErrorHelper.checkGlError(TAG, "glDeleteProgram");
            } else {
                Log.w(TAG, "unable to delete program " + program + " because it is not valid");
            }

            program = 0;
        }
        uniformLocations.clear();
        attributeLocations.clear();
    }

    public int getAttributeLocation(String name) {
        if (attributeLocations.containsKey(name)) {
            return attributeLocations.get(name);
        }
        int handle = GLES20.glGetAttribLocation(program, name);
        ErrorHelper.checkGlError(TAG, "glGetAttribLocation");
        attributeLocations.put(name, handle);
        return handle;
    }

    public Shader getFragmentShader() {
        return fragmentShader;
    }

    public int getProgram() {
        return program;
    }

    public int getUniformLocation(String name) {
        if (uniformLocations.containsKey(name)) {
            return uniformLocations.get(name);
        }
        int handle = GLES20.glGetUniformLocation(program, name);
        ErrorHelper.checkGlError(TAG, "glGetUniformLocation");
        uniformLocations.put(name, handle);
        return handle;
    }

    public Shader getVertexShader() {
        return vertexShader;
    }

    public boolean use() {
        if (!load()) {
            return false;
        }
        GLES20.glUseProgram(program);
        ErrorHelper.checkGlError(TAG, "glUseProgram");
        return true;
    }

    public boolean load() {
        if (program != 0) {
            return true;
        }

        try {
            if (!vertexShader.load() || !fragmentShader.load()) {
                Log.e(TAG, "Program cannot load vertex or fragment shader");
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
        } catch (RuntimeException ex) {
            if (program != 0) {
                GLES20.glDeleteProgram(program);
                program = 0;
            }
            throw ex;
        }

        return program != 0;
    }
}
