/**
 * This file is a part of sunlight project Copyright (c) $today.year sunlight authors (see file
 * `COPYRIGHT` for the license)
 */
package com.ghisguth.gfx;

import android.opengl.GLES20;
import android.util.Log;
import javax.microedition.khronos.opengles.GL10;

public class FrameBuffer {
    private static String TAG = "Sunlight";
    private int frameBuffer;
    private RenderTexture renderTexture;

    public FrameBuffer(RenderTexture renderTexture) {
        this.renderTexture = renderTexture;
        TextureManager.getSingletonObject().registerFrameBuffer(this);
    }

    public void bind() {
        if (frameBuffer == 0) {
            throw new RuntimeException("Cannot bind frameBuffer because it is not loaded");
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
    }

    protected void finalize() throws Throwable {
        unload();
        super.finalize();
    }

    public void unload() {
        if (frameBuffer != 0) {
            if (GLES20.glIsFramebuffer(frameBuffer)) {
                int[] frameBuffers = new int[1];
                frameBuffers[0] = frameBuffer;
                GLES20.glDeleteFramebuffers(1, frameBuffers, 0);
                ErrorHelper.checkGlError(TAG, "glDeleteFramebuffers");
            } else {
                Log.w(
                        TAG,
                        "unable to delete frameBuffer " + frameBuffer + " because it is not valid");
            }
            frameBuffer = 0;
        }

        if (renderTexture != null) {
            renderTexture.unload();
        }
    }

    public int getFrameBuffer() {
        return frameBuffer;
    }

    public boolean load() {
        if (frameBuffer != 0) {
            return true;
        }

        if (renderTexture == null || !renderTexture.load()) {
            return false;
        }

        int[] frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);
        frameBuffer = frameBuffers[0];

        if (frameBuffer == 0) {
            return false;
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);

        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GL10.GL_TEXTURE_2D,
                renderTexture.getTexture(),
                0);

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException(
                    "Framebuffer is not complete: " + Integer.toHexString(status));
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return true;
    }

    public void unbind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }
}
