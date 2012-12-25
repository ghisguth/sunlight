/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.gfx;

import android.content.res.Resources;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;

public class TextureManager {
    private HashSet<WeakReference<Texture>> textures = new HashSet<WeakReference<Texture>>();
    private ReferenceQueue<Texture> textureReferenceQueue = new ReferenceQueue<Texture>();

    private static TextureManager singletonObject;

    private TextureManager() {
    }

    public static synchronized TextureManager getSingletonObject() {
        if (singletonObject == null) {
            singletonObject = new TextureManager();
        }
        return singletonObject;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public Texture createTexture(Resources resources, int resource, boolean compressed, int minFilter, int maxFilter, int wrapS, int wrapT) {
        return new Texture(resources, resource, compressed, minFilter, maxFilter, wrapS, wrapT);
    }

    public void registerTexture(Texture texture) {
        synchronized (textures) {
            WeakReference<Texture> weakReference = new WeakReference<Texture>(texture, textureReferenceQueue);
            textures.add(weakReference);
            processTextureReferenceQueueImpl();
        }
    }

    private void processTextureReferenceQueueImpl() {
        Reference<?> reference = textureReferenceQueue.poll();
        while (reference != null) {
            textures.remove(reference);
            reference = textureReferenceQueue.poll();
        }
    }

    private void processTextureReferenceQueue() {
        synchronized (textures) {
            processTextureReferenceQueueImpl();
        }
    }

    public void cleanUp() {
        processTextureReferenceQueue();
    }

    public void unloadAllTextures() {
        synchronized (textures) {
            for (WeakReference<Texture> textureWeak : textures) {
                Texture texture = textureWeak.get();
                if (texture != null) {
                    texture.unload();
                }
            }
        }
    }

    public void unloadAll() {
        unloadAllTextures();
    }

}
