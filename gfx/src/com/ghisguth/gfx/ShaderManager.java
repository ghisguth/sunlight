/**
 * This file is a part of sunlight project Copyright (c) $today.year sunlight authors (see file
 * `COPYRIGHT` for the license)
 */
package com.ghisguth.gfx;

import android.opengl.GLES20;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;

public class ShaderManager {
    private static ShaderManager singletonObject;
    private HashSet<WeakReference<Shader>> shaders = new HashSet<WeakReference<Shader>>();
    private ReferenceQueue<Shader> shaderReferenceQueue = new ReferenceQueue<Shader>();
    private HashSet<WeakReference<Program>> shaderPrograms = new HashSet<WeakReference<Program>>();
    private ReferenceQueue<Program> shaderProgramReferenceQueue = new ReferenceQueue<Program>();

    private ShaderManager() {}

    public static synchronized ShaderManager getSingletonObject() {
        if (singletonObject == null) {
            singletonObject = new ShaderManager();
        }
        return singletonObject;
    }

    public void cleanUp() {
        processShaderReferenceQueue();
        processShaderProgramReferenceQueue();
    }

    private void processShaderProgramReferenceQueue() {
        synchronized (shaderPrograms) {
            processShaderProgramReferenceQueueImpl();
        }
    }

    private void processShaderReferenceQueue() {
        synchronized (shaders) {
            processShaderReferenceQueueImpl();
        }
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public Shader createFragmentShader(String source) {
        return new Shader(GLES20.GL_FRAGMENT_SHADER, source);
    }

    public Program createShaderProgram(Shader vertexShader, Shader fragmentShader) {
        return new Program(vertexShader, fragmentShader);
    }

    public Shader createVertexShader(String source) {
        return new Shader(GLES20.GL_VERTEX_SHADER, source);
    }

    public void registerShader(Shader shader) {
        synchronized (shaders) {
            WeakReference<Shader> weakReference =
                    new WeakReference<Shader>(shader, shaderReferenceQueue);
            shaders.add(weakReference);
            processShaderReferenceQueueImpl();
        }
    }

    private void processShaderReferenceQueueImpl() {
        Reference<?> reference = shaderReferenceQueue.poll();
        while (reference != null) {
            shaders.remove(reference);
            reference = shaderReferenceQueue.poll();
        }
    }

    public void registerShaderProgram(Program shaderProgram) {
        synchronized (shaderPrograms) {
            WeakReference<Program> weakReference =
                    new WeakReference<Program>(shaderProgram, shaderProgramReferenceQueue);
            shaderPrograms.add(weakReference);
            processShaderProgramReferenceQueueImpl();
        }
    }

    private void processShaderProgramReferenceQueueImpl() {
        Reference<?> reference = shaderProgramReferenceQueue.poll();
        while (reference != null) {
            shaderPrograms.remove(reference);
            reference = shaderProgramReferenceQueue.poll();
        }
    }

    public void unloadAll() {
        unloadAllShaders();
        unloadAllShaderPrograms();
    }

    public void unloadAllShaderPrograms() {
        synchronized (shaderPrograms) {
            for (WeakReference<Program> shaderProgramWeak : shaderPrograms) {
                Program shaderProgram = shaderProgramWeak.get();
                if (shaderProgram != null) {
                    shaderProgram.unload();
                }
            }
        }
    }

    public void unloadAllShaders() {
        synchronized (shaders) {
            for (WeakReference<Shader> shaderWeak : shaders) {
                Shader shader = shaderWeak.get();
                if (shader != null) {
                    shader.unload();
                }
            }
        }
    }
}
