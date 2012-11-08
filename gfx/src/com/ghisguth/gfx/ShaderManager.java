/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.gfx;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;

public class ShaderManager {
    private static String TAG = "ShaderManager";

    private HashSet<WeakReference<Shader>> shaders = new HashSet<WeakReference<Shader>>();
    private ReferenceQueue<Shader> shaderReferenceQueue = new ReferenceQueue<Shader>();
    private HashSet<WeakReference<ShaderProgram>> shaderPrograms = new HashSet<WeakReference<ShaderProgram>>();
    private ReferenceQueue<ShaderProgram> shaderProgramReferenceQueue = new ReferenceQueue<ShaderProgram>();


    private static ShaderManager singletonObject;

    private ShaderManager() {
    }

    public static synchronized ShaderManager getSingletonObject() {
        if (singletonObject == null) {
            singletonObject = new ShaderManager();
        }
        return singletonObject;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void registerShader(Shader shader) {
        synchronized (shaders) {
            WeakReference<Shader> weakReference = new WeakReference<Shader>(shader, shaderReferenceQueue);
            shaders.add(weakReference);
            processShaderReferenceQueueImpl();
        }
    }

    public void registerShaderProgram(ShaderProgram shaderProgram) {
        synchronized (shaderPrograms) {
            WeakReference<ShaderProgram> weakReference = new WeakReference<ShaderProgram>(shaderProgram, shaderProgramReferenceQueue);
            shaderPrograms.add(weakReference);
            processShaderProgramReferenceQueueImpl();
        }
    }

    private void processShaderReferenceQueueImpl() {
        Reference<?> reference = shaderReferenceQueue.poll();
        while (reference != null) {
            shaders.remove(reference);
            reference = shaderReferenceQueue.poll();
        }
    }

    private void processShaderReferenceQueue() {
        synchronized (shaderPrograms) {
            processShaderReferenceQueueImpl();
        }
    }

    private void processShaderProgramReferenceQueueImpl() {
        Reference<?> reference = shaderProgramReferenceQueue.poll();
        while (reference != null) {
            shaderPrograms.remove(reference);
            reference = shaderProgramReferenceQueue.poll();
        }
    }

    private void processShaderProgramReferenceQueue() {
        synchronized (shaderPrograms) {
            processShaderProgramReferenceQueueImpl();
        }
    }

    public void cleanUp() {
        processShaderReferenceQueue();
        processShaderProgramReferenceQueue();
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

    public void unloadAllShaderPrograms() {
        synchronized (shaderPrograms) {
            for (WeakReference<ShaderProgram> shaderProgramWeak : shaderPrograms) {
                ShaderProgram shaderProgram = shaderProgramWeak.get();
                if (shaderProgram != null) {
                    shaderProgram.unload();
                }
            }
        }
    }

    public void unloadAll() {
        unloadAllShaders();
        unloadAllShaderPrograms();
    }
}
