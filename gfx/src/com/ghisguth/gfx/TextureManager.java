/**
 * This file is a part of sunlight project Copyright (c) $today.year sunlight authors (see file
 * `COPYRIGHT` for the license)
 */
package com.ghisguth.gfx;

import android.content.res.Resources;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;

public class TextureManager {
    private static TextureManager singletonObject;
    private HashSet<WeakReference<Texture>> textures = new HashSet<WeakReference<Texture>>();
    private ReferenceQueue<Texture> textureReferenceQueue = new ReferenceQueue<Texture>();
    private HashSet<WeakReference<FrameBuffer>> frameBuffers =
            new HashSet<WeakReference<FrameBuffer>>();
    private ReferenceQueue<FrameBuffer> frameBufferReferenceQueue =
            new ReferenceQueue<FrameBuffer>();

    private TextureManager() {}

    public static synchronized TextureManager getSingletonObject() {
        if (singletonObject == null) {
            singletonObject = new TextureManager();
        }
        return singletonObject;
    }

    public void cleanUp() {
        processTextureReferenceQueue();
        processFrameBufferReferenceQueue();
    }

    private void processFrameBufferReferenceQueue() {
        synchronized (frameBuffers) {
            processFrameBufferReferenceQueueImpl();
        }
    }

    private void processTextureReferenceQueue() {
        synchronized (textures) {
            processTextureReferenceQueueImpl();
        }
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public FrameBuffer createFrameBuffer(RenderTexture renderTexture) {
        return new FrameBuffer(renderTexture);
    }

    public RenderTexture createRenderTexture(int width, int height) {
        return new RenderTexture(width, height);
    }

    public Texture createTexture(
            Resources resources,
            int resource,
            boolean compressed,
            int minFilter,
            int maxFilter,
            int wrapS,
            int wrapT) {
        return new Texture(resources, resource, compressed, minFilter, maxFilter, wrapS, wrapT);
    }

    public void registerFrameBuffer(FrameBuffer frameBuffer) {
        synchronized (frameBuffers) {
            WeakReference<FrameBuffer> weakReference =
                    new WeakReference<FrameBuffer>(frameBuffer, frameBufferReferenceQueue);
            frameBuffers.add(weakReference);
            processFrameBufferReferenceQueueImpl();
        }
    }

    private void processFrameBufferReferenceQueueImpl() {
        Reference<?> reference = frameBufferReferenceQueue.poll();
        while (reference != null) {
            frameBuffers.remove(reference);
            reference = frameBufferReferenceQueue.poll();
        }
    }

    public void registerTexture(Texture texture) {
        synchronized (textures) {
            WeakReference<Texture> weakReference =
                    new WeakReference<Texture>(texture, textureReferenceQueue);
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

    public void unloadAll() {
        unloadAllTextures();
        unloadAllFrameBuffers();
    }

    public void unloadAllFrameBuffers() {
        synchronized (frameBuffers) {
            for (WeakReference<FrameBuffer> frameBufferWeak : frameBuffers) {
                FrameBuffer frameBuffer = frameBufferWeak.get();
                if (frameBuffer != null) {
                    frameBuffer.unload();
                }
            }
        }
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
}
