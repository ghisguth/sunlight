/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.gfx;

import android.opengl.GLES20;
import android.util.Log;

public class RenderTexture extends Texture {

    private static String TAG = "Sunlight";
    private int width;
    private int height;

    public RenderTexture(int width, int height) {
        super(null, 0, false, GLES20.GL_LINEAR, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);
        this.width = width;
        this.height = height;
    }

    public boolean load() {
        if (texture != 0) {
            return true;
        }

        texture = createTexture();
        if (texture == 0) {
            Log.e(TAG, "Unable to create render texture");
            return false;
        }

        setUpRenderTexture();

        return true;
    }

    public void update(int width, int height) {
        this.width = width;
        this.height = height;

        if (texture != 0) {
            setUpRenderTexture();
        }
    }

    private void setUpRenderTexture() {
        bindTexture();

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width,
                height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        ErrorHelper.checkGlError(TAG, "glTexImage2D texture");

        setUpTextureParameters();
    }
}
